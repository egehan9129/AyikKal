package com.ayikkal.app.data

import com.ayikkal.app.data.db.AppDatabase
import com.ayikkal.app.data.db.BlockedContact
import com.ayikkal.app.data.db.DrinkCategory
import com.ayikkal.app.data.db.DrinkEntry
import com.ayikkal.app.data.db.DrinkSeed
import com.ayikkal.app.data.db.DrinkType
import com.ayikkal.app.data.db.EventType
import com.ayikkal.app.data.db.GuardEvent
import com.ayikkal.app.data.db.LockedApp
import com.ayikkal.app.data.db.Session
import com.ayikkal.app.domain.BacCalculator
import com.ayikkal.app.domain.Dose
import com.ayikkal.app.domain.PhoneNumbers
import com.ayikkal.app.domain.TestMode
import com.ayikkal.app.system.AlarmScheduler
import com.ayikkal.app.system.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar

/** Tüm iş kuralları burada: seans, testler, Sarhoş Modu geçişleri ve olay kaydı. */
@OptIn(ExperimentalCoroutinesApi::class)
class Repository(
    private val db: AppDatabase,
    val prefs: Prefs,
    private val alarms: AlarmScheduler,
    private val notifier: Notifier,
    private val scope: CoroutineScope,
) {
    private val mutex = Mutex()

    val state: StateFlow<GuardState> get() = prefs.state

    val blockedContacts: StateFlow<List<BlockedContact>> =
        db.blockedContacts().observeAll().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val lockedApps: StateFlow<List<LockedApp>> =
        db.lockedApps().observeAll().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val drinkTypes: Flow<List<DrinkType>> = db.drinkTypes().observeAll()

    val endedSessions: Flow<List<Session>> = db.sessions().observeEnded()

    val activeEntries: Flow<List<DrinkEntry>> = prefs.state
        .map { it.activeSessionId }
        .distinctUntilChanged()
        .flatMapLatest { id -> if (id == 0L) flowOf(emptyList()) else db.drinkEntries().observeForSession(id) }

    val activeEvents: Flow<List<GuardEvent>> = prefs.state
        .map { it.activeSessionId }
        .distinctUntilChanged()
        .flatMapLatest { id -> if (id == 0L) flowOf(emptyList()) else db.events().observeForSession(id) }

    val recentTests: Flow<List<GuardEvent>> = db.events().observeRecent(
        listOf(EventType.TEST_PASSED, EventType.TEST_FAILED, EventType.TEST_MISSED, EventType.SOBER_TEST_FAILED),
        limit = 8,
    )

    fun entriesFor(sessionId: Long): Flow<List<DrinkEntry>> = db.drinkEntries().observeForSession(sessionId)
    fun eventsFor(sessionId: Long): Flow<List<GuardEvent>> = db.events().observeForSession(sessionId)
    fun observeSession(id: Long): Flow<Session?> = db.sessions().observe(id)

    fun launch(block: suspend Repository.() -> Unit) {
        scope.launch { block() }
    }

    suspend fun seedIfNeeded() {
        if (db.drinkTypes().count() == 0) db.drinkTypes().insertAll(DrinkSeed.items)
    }

    // ---------------------------------------------------------------- Seans

    suspend fun startSession(): Unit = mutex.withLock {
        if (prefs.current.sessionActive) return@withLock
        startSessionUnlocked()
        refreshSessionNotification()
    }

    /** Sarhoş Modu açıkken seans bitirilemez; aksi halde kalkan tek tuşla delinebilirdi. */
    suspend fun endSession(): Boolean = mutex.withLock { endSessionLocked(force = false) }

    private suspend fun endSessionLocked(force: Boolean): Boolean {
        val s = prefs.current
        if (!s.sessionActive) return false
        if (s.drunkActive && !force) return false
        val now = System.currentTimeMillis()
        val doses = doses(s.activeSessionId)
        val curve = BacCalculator.curve(doses, s.profile, maxOf(now, BacCalculator.soberAt(doses, s.profile, now) ?: now))
        val peak = curve.maxByOrNull { it.bac }
        db.sessions().get(s.activeSessionId)?.let {
            db.sessions().update(it.copy(endedAt = now, peakBac = peak?.bac ?: 0.0, peakAt = peak?.at))
        }
        log(EventType.SESSION_END)
        prefs.edit {
            putLong(Prefs.K.ACTIVE_SESSION, 0)
            putBoolean(Prefs.K.TEST_PENDING, false)
            putBoolean(Prefs.K.TEST_IN_PROGRESS, false)
            putInt(Prefs.K.MISS_COUNT, 0)
            putLong(Prefs.K.NEXT_TEST_AT, 0)
        }
        alarms.cancelTestAlarms()
        notifier.cancelSession()
        notifier.cancelTest()
        return true
    }

    suspend fun addDrink(type: DrinkType): Unit = mutex.withLock {
        if (!prefs.current.sessionActive) startSessionUnlocked()
        val s = prefs.current
        db.drinkEntries().insert(
            DrinkEntry(
                sessionId = s.activeSessionId,
                drinkTypeId = type.id,
                name = type.name,
                servingLabel = type.servingLabel,
                category = type.category,
                volumeMl = type.volumeMl,
                abv = type.abv,
                consumedAt = System.currentTimeMillis(),
            )
        )
        val count = db.drinkEntries().forSession(s.activeSessionId).size
        if (s.waterReminder && count % 2 == 0) notifier.showWaterReminder(count)
        refreshSessionNotification()
        if (!s.drunkActive) {
            if (checkAutoLock()) return@withLock
            // README akışı: her 3. kadehten sonra kontrol testi.
            if (count % 3 == 0 && !s.testPending) triggerTestLocked("$count. kadeh")
        }
    }

    private suspend fun startSessionUnlocked() {
        val now = System.currentTimeMillis()
        val id = db.sessions().insert(Session(startedAt = now))
        val next = now + prefs.current.testIntervalMin * 60_000L
        prefs.edit {
            putLong(Prefs.K.ACTIVE_SESSION, id)
            putLong(Prefs.K.SESSION_STARTED_AT, now)
            putInt(Prefs.K.MISS_COUNT, 0)
            putBoolean(Prefs.K.TEST_PENDING, false)
            putBoolean(Prefs.K.TEST_IN_PROGRESS, false)
            putLong(Prefs.K.NEXT_TEST_AT, next)
        }
        alarms.scheduleTestDue(next)
        log(EventType.SESSION_START)
    }

    suspend fun removeLatestDrink(typeId: Long): Unit = mutex.withLock {
        val s = prefs.current
        if (!s.sessionActive) return@withLock
        db.drinkEntries().deleteLatestOfType(s.activeSessionId, typeId)
        refreshSessionNotification()
    }

    suspend fun addCustomDrink(name: String, category: DrinkCategory, volumeMl: Int, abv: Double): DrinkType {
        val type = DrinkType(
            category = category,
            name = name.trim(),
            servingLabel = "${volumeMl}ml",
            volumeMl = volumeMl,
            abv = abv,
            custom = true,
        )
        val id = db.drinkTypes().insert(type)
        return type.copy(id = id)
    }

    suspend fun addWater() {
        log(EventType.WATER)
        notifier.cancelWater()
    }

    // ---------------------------------------------------------------- Testler

    suspend fun triggerTest(reason: String): Unit = mutex.withLock { triggerTestLocked(reason) }

    private suspend fun triggerTestLocked(reason: String) {
        val s = prefs.current
        if (!s.sessionActive || s.drunkActive || s.testInProgress) return
        val deadline = System.currentTimeMillis() + TEST_RESPONSE_MS
        prefs.edit {
            putBoolean(Prefs.K.TEST_PENDING, true)
            putLong(Prefs.K.TEST_DEADLINE, deadline)
        }
        alarms.cancelTestDue()
        alarms.scheduleTestDeadline(deadline)
        notifier.showTestDue(s.missCount, deadline, reason)
    }

    /** Alarm: periyodik test zamanı geldi. */
    suspend fun onTestDue(): Unit = mutex.withLock {
        val s = prefs.current
        if (!s.sessionActive || s.drunkActive) return@withLock
        if (checkAutoLock()) return@withLock
        triggerTestLocked("Periyodik kontrol")
    }

    suspend fun onTestStarted(mode: TestMode): Unit = mutex.withLock {
        if (mode != TestMode.PERIODIC && mode != TestMode.VOLUNTARY) return@withLock
        if (!prefs.current.sessionActive) return@withLock
        // Başlanan test yarıda bırakılırsa süre bitiminde başarısız sayılır.
        val deadline = System.currentTimeMillis() + TEST_RUN_MS
        prefs.edit {
            putBoolean(Prefs.K.TEST_IN_PROGRESS, true)
            putBoolean(Prefs.K.TEST_PENDING, false)
            putLong(Prefs.K.TEST_DEADLINE, deadline)
        }
        alarms.cancelTestDue()
        alarms.scheduleTestDeadline(deadline)
        notifier.cancelTest()
    }

    /** Alarm: test için verilen süre doldu. */
    suspend fun onTestDeadline(): Unit = mutex.withLock { onTestDeadlineLocked() }

    private suspend fun onTestDeadlineLocked() {
        val s = prefs.current
        if (!s.sessionActive || s.drunkActive) return
        when {
            s.testInProgress -> {
                log(EventType.TEST_FAILED, "Test yarıda bırakıldı")
                activateDrunkModeLocked("Testi yarıda bıraktın")
            }
            s.testPending -> {
                val misses = s.missCount + 1
                log(EventType.TEST_MISSED, "$misses. kaçırma")
                if (misses >= 2) {
                    activateDrunkModeLocked("Kontrol bildirimini 2 kez yok saydın")
                } else {
                    val deadline = System.currentTimeMillis() + TEST_RESPONSE_MS
                    prefs.edit {
                        putInt(Prefs.K.MISS_COUNT, misses)
                        putLong(Prefs.K.TEST_DEADLINE, deadline)
                    }
                    alarms.scheduleTestDeadline(deadline)
                    notifier.showTestDue(misses, deadline, "Son uyarı")
                }
            }
        }
    }

    suspend fun onTestFinished(mode: TestMode, passed: Boolean, summary: String, avgReactionMs: Int?): Unit = mutex.withLock {
        val s = prefs.current
        when (mode) {
            TestMode.PRACTICE -> {
                if (passed && avgReactionMs != null && avgReactionMs > 0) {
                    prefs.edit { putInt(Prefs.K.BASELINE_REACTION, avgReactionMs) }
                }
            }
            TestMode.SOBER_UP -> {
                prefs.edit { putLong(Prefs.K.LAST_SOBER_ATTEMPT, System.currentTimeMillis()) }
                if (passed) {
                    unlockLocked("Ayılma testini geçtin")
                } else {
                    log(EventType.SOBER_TEST_FAILED, summary)
                    val newUnlock = prefs.current.unlockAt + SOBER_FAIL_PENALTY_MS
                    prefs.edit { putLong(Prefs.K.UNLOCK_AT, newUnlock) }
                    alarms.scheduleUnlock(newUnlock)
                }
            }
            TestMode.PERIODIC, TestMode.VOLUNTARY -> {
                alarms.cancelTestDeadline()
                if (!s.sessionActive) return@withLock
                if (passed) {
                    log(EventType.TEST_PASSED, summary)
                    val next = System.currentTimeMillis() + nextIntervalMs()
                    prefs.edit {
                        putBoolean(Prefs.K.TEST_IN_PROGRESS, false)
                        putBoolean(Prefs.K.TEST_PENDING, false)
                        putInt(Prefs.K.MISS_COUNT, 0)
                        putLong(Prefs.K.NEXT_TEST_AT, next)
                    }
                    alarms.scheduleTestDue(next)
                } else {
                    log(EventType.TEST_FAILED, summary)
                    activateDrunkModeLocked("Sarhoşluk testini geçemedin")
                }
            }
        }
    }

    /** Promil yükseldikçe testler sıklaşır. */
    private suspend fun nextIntervalMs(): Long {
        val base = prefs.current.testIntervalMin
        val bac = currentBac()
        val minutes = when {
            bac >= 1.0 -> minOf(base, 20)
            bac >= 0.5 -> minOf(base, 30)
            else -> base
        }
        return minutes * 60_000L
    }

    // ---------------------------------------------------------------- Sarhoş Modu

    suspend fun activateDrunkMode(reason: String): Unit = mutex.withLock { activateDrunkModeLocked(reason) }

    private suspend fun activateDrunkModeLocked(reason: String) {
        if (prefs.current.drunkActive) return
        // Seans dışında gönüllü kilit: gece yine de raporlansın diye seans açılır.
        if (!prefs.current.sessionActive) startSessionUnlocked()
        val s = prefs.current
        val now = System.currentTimeMillis()
        val unlockAt = computeUnlockAt(now, s.unlockHour, s.unlockMinute)
        prefs.edit {
            putBoolean(Prefs.K.DRUNK_ACTIVE, true)
            putLong(Prefs.K.DRUNK_SINCE, now)
            putLong(Prefs.K.UNLOCK_AT, unlockAt)
            putString(Prefs.K.DRUNK_REASON, reason)
            putBoolean(Prefs.K.TEST_PENDING, false)
            putBoolean(Prefs.K.TEST_IN_PROGRESS, false)
            putLong(Prefs.K.NEXT_TEST_AT, 0)
        }
        alarms.cancelTestAlarms()
        alarms.scheduleUnlock(unlockAt)
        notifier.cancelTest()
        log(EventType.DRUNK_MODE_ON, reason)
        notifier.showDrunkMode(reason, unlockAt)
        refreshSessionNotification()
    }

    suspend fun unlock(reason: String): Unit = mutex.withLock { unlockLocked(reason) }

    private suspend fun unlockLocked(reason: String) {
        if (!prefs.current.drunkActive) return
        log(EventType.UNLOCKED, reason)
        prefs.edit {
            putBoolean(Prefs.K.DRUNK_ACTIVE, false)
            putLong(Prefs.K.UNLOCK_AT, 0)
            putString(Prefs.K.DRUNK_REASON, "")
        }
        alarms.cancelUnlock()
        // Kilit açıldığında gece biter; sabah raporu hazırdır.
        endSessionLocked(force = true)
        notifier.showUnlocked(reason)
    }

    /** Uygulama açılışında ve yeniden başlatmada alarmları ve gecikmiş durumları toparlar. */
    suspend fun reconcile(): Unit = mutex.withLock {
        val s = prefs.current
        val now = System.currentTimeMillis()
        if (s.drunkActive) {
            if (now >= s.unlockAt) unlockLocked("Sabah kilit saati") else alarms.scheduleUnlock(s.unlockAt)
        }
        val st = prefs.current
        if (st.sessionActive && !st.drunkActive) {
            when {
                st.testPending || st.testInProgress ->
                    if (now >= st.testDeadline) onTestDeadlineLocked() else alarms.scheduleTestDeadline(st.testDeadline)
                st.nextTestAt > 0 -> alarms.scheduleTestDue(maxOf(st.nextTestAt, now + 5_000))
            }
            refreshSessionNotification()
        }
    }

    // ---------------------------------------------------------------- Koruma yardımcıları

    fun findBlacklisted(number: String): BlockedContact? =
        blockedContacts.value.firstOrNull { PhoneNumbers.matches(it.number, number) }

    fun isSafeContact(number: String): Boolean =
        prefs.current.safeContact?.let { PhoneNumbers.matches(it.number, number) } ?: false

    /** Güvenli eylemler (112, güvenli kişiyi arama) için kilitli paketlere kısa süreli geçiş izni. */
    fun grantBypass(packages: Set<String>, durationMs: Long) {
        prefs.edit {
            putStringSet(Prefs.K.BYPASS_PACKAGES, packages)
            putLong(Prefs.K.BYPASS_UNTIL, System.currentTimeMillis() + durationMs)
        }
    }

    fun log(type: EventType, detail: String = "") {
        val sessionId = prefs.current.activeSessionId.takeIf { it != 0L }
        scope.launch { db.events().insert(GuardEvent(sessionId = sessionId, at = System.currentTimeMillis(), type = type, detail = detail)) }
    }

    suspend fun addBlockedContact(name: String, number: String) {
        val normalized = PhoneNumbers.normalize(number)
        if (normalized.isEmpty() || blockedContacts.value.any { it.normalized == normalized }) return
        db.blockedContacts().insert(BlockedContact(name = name.ifBlank { number }, number = number, normalized = normalized))
    }

    suspend fun removeBlockedContact(c: BlockedContact) {
        if (prefs.current.drunkActive) return
        db.blockedContacts().delete(c)
    }

    suspend fun setLockedApp(packageName: String, label: String, enabled: Boolean) {
        if (prefs.current.drunkActive && !enabled) return
        db.lockedApps().upsert(LockedApp(packageName, label, enabled))
    }

    suspend fun addLockedAppIfMissing(packageName: String, label: String) {
        db.lockedApps().insertIfMissing(LockedApp(packageName, label, true))
    }

    suspend fun removeLockedApp(app: LockedApp) {
        if (prefs.current.drunkActive) return
        db.lockedApps().delete(app)
    }

    suspend fun markReportSeen(sessionId: Long) {
        db.sessions().get(sessionId)?.let { db.sessions().update(it.copy(reportSeen = true)) }
    }

    // ---------------------------------------------------------------- Promil

    private suspend fun doses(sessionId: Long): List<Dose> =
        db.drinkEntries().forSession(sessionId).map { Dose(it.consumedAt, it.volumeMl, it.abv) }

    suspend fun currentBac(): Double {
        val s = prefs.current
        if (!s.sessionActive) return 0.0
        return BacCalculator.current(doses(s.activeSessionId), s.profile, System.currentTimeMillis())
    }

    /** Eşik açıksa, emilmekte olan alkol dahil tahmini zirve eşiği geçince kalkanı açar. */
    private suspend fun checkAutoLock(): Boolean {
        val s = prefs.current
        if (s.autoLockBac <= 0.0 || !s.sessionActive || s.drunkActive) return false
        val peak = BacCalculator.projectedPeak(doses(s.activeSessionId), s.profile, System.currentTimeMillis())
        if (peak < s.autoLockBac) return false
        activateDrunkModeLocked("Tahmini promil %.2f‰ eşiğini aştı".format(s.autoLockBac))
        return true
    }

    private suspend fun refreshSessionNotification() {
        val s = prefs.current
        if (!s.sessionActive) return
        val entries = db.drinkEntries().forSession(s.activeSessionId)
        val bac = BacCalculator.current(entries.map { Dose(it.consumedAt, it.volumeMl, it.abv) }, s.profile, System.currentTimeMillis())
        notifier.showSession(bac, entries.size, s.drunkActive)
    }

    companion object {
        /** README akışı: "Testi çözmek için 2 dakikan var." */
        const val TEST_RESPONSE_MS = 2 * 60_000L
        const val TEST_RUN_MS = 3 * 60_000L
        const val SOBER_FAIL_PENALTY_MS = 60 * 60_000L
        const val SOBER_COOLDOWN_MS = 30 * 60_000L
        private const val MIN_LOCK_MS = 2 * 60 * 60_000L

        /** Bir sonraki sabah kilit saati; en az 2 saat kilit garantisi. */
        fun computeUnlockAt(now: Long, hour: Int, minute: Int): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            return maxOf(cal.timeInMillis, now + MIN_LOCK_MS)
        }
    }
}
