package com.ayikkal.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ayikkal.app.AyikKalApp
import com.ayikkal.app.data.GuardState
import com.ayikkal.app.data.Prefs
import com.ayikkal.app.data.SafeContact
import com.ayikkal.app.data.db.BlockedContact
import com.ayikkal.app.data.db.DrinkCategory
import com.ayikkal.app.data.db.DrinkEntry
import com.ayikkal.app.data.db.DrinkType
import com.ayikkal.app.data.db.GuardEvent
import com.ayikkal.app.data.db.LockedApp
import com.ayikkal.app.data.db.Session
import com.ayikkal.app.domain.BacCalculator
import com.ayikkal.app.domain.BacLevel
import com.ayikkal.app.domain.BacPoint
import com.ayikkal.app.domain.Dose
import com.ayikkal.app.domain.UserProfile
import com.ayikkal.app.system.AppCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BacInfo(
    val current: Double = 0.0,
    val peak: Double = 0.0,
    val projectedPeak: Double = 0.0,
    val soberAt: Long? = null,
    val level: BacLevel = BacLevel.SOBER,
)

data class DrinkGroup(val typeId: Long, val name: String, val serving: String, val category: DrinkCategory, val abv: Double, val count: Int)

data class NightReport(
    val session: Session,
    val entries: List<DrinkEntry>,
    val events: List<GuardEvent>,
    val curve: List<BacPoint>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as AyikKalApp).repository

    val state: StateFlow<GuardState> = repo.state

    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(20_000)
        }
    }

    val entries: StateFlow<List<DrinkEntry>> =
        repo.activeEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessionEvents: StateFlow<List<GuardEvent>> =
        repo.activeEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val drinkGroups: StateFlow<List<DrinkGroup>> = entries.map { list ->
        list.groupBy { it.drinkTypeId }.map { (id, items) ->
            val f = items.first()
            DrinkGroup(id, f.name, f.servingLabel, f.category, f.abv, items.size)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bac: StateFlow<BacInfo> = combine(entries, state, ticker) { list, s, now ->
        val doses = list.map { Dose(it.consumedAt, it.volumeMl, it.abv) }
        val curve = BacCalculator.curve(doses, s.profile, now)
        val current = curve.lastOrNull()?.bac ?: 0.0
        BacInfo(
            current = current,
            peak = curve.maxOfOrNull { it.bac } ?: 0.0,
            projectedPeak = BacCalculator.projectedPeak(doses, s.profile, now),
            soberAt = BacCalculator.soberAt(doses, s.profile, now),
            level = BacLevel.of(current),
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BacInfo())

    val drinkTypes: StateFlow<List<DrinkType>> =
        repo.drinkTypes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lockedApps: StateFlow<List<LockedApp>> = repo.lockedApps
    val blockedContacts: StateFlow<List<BlockedContact>> = repo.blockedContacts

    val endedSessions: StateFlow<List<Session>> =
        repo.endedSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentTests: StateFlow<List<GuardEvent>> =
        repo.recentTests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Rapor ekranında seçili gece; null = en son biten gece. */
    val selectedReportId = MutableStateFlow<Long?>(null)

    val report: StateFlow<NightReport?> = combine(selectedReportId, endedSessions) { sel, sessions ->
        sessions.firstOrNull { it.id == sel } ?: sessions.firstOrNull()
    }.flatMapLatest { session ->
        if (session == null) flowOf(null)
        else combine(repo.entriesFor(session.id), repo.eventsFor(session.id), state) { e, ev, s ->
            val doses = e.map { Dose(it.consumedAt, it.volumeMl, it.abv) }
            // Gece, seans bittiğinde değil alkol vücuttan atıldığında biter; eğri oraya kadar çizilir.
            val ended = session.endedAt ?: System.currentTimeMillis()
            val sober = BacCalculator.soberAt(doses, s.profile, ended) ?: ended
            NightReport(session, e, ev, BacCalculator.curve(doses, s.profile, maxOf(ended, sober)))
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ------------------------------------------------------------ Eylemler

    private fun io(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { block() }
    }

    fun startSession() = io { repo.startSession() }
    fun endSession(onResult: (Boolean) -> Unit = {}) = io {
        val ok = repo.endSession()
        launchMain { onResult(ok) }
    }

    fun addDrink(type: DrinkType) = io { repo.addDrink(type) }
    fun removeDrink(typeId: Long) = io { repo.removeLatestDrink(typeId) }
    fun addWater() = io { repo.addWater() }
    fun addCustomDrink(name: String, category: DrinkCategory, volumeMl: Int, abv: Double) = io {
        repo.addDrink(repo.addCustomDrink(name, category, volumeMl, abv))
    }

    fun triggerTestNow() = io { repo.triggerTest("Gönüllü test") }
    fun lockNow(reason: String) = io { repo.activateDrunkMode(reason) }

    fun completeOnboarding() = repo.prefs.edit { putBoolean(Prefs.K.ONBOARDING_DONE, true) }

    fun saveProfile(p: UserProfile) = repo.prefs.edit {
        putBoolean(Prefs.K.MALE, p.male)
        putInt(Prefs.K.WEIGHT, p.weightKg)
        putInt(Prefs.K.HEIGHT, p.heightCm)
        putInt(Prefs.K.AGE, p.age)
        putBoolean(Prefs.K.FULL_STOMACH, p.fullStomach)
    }

    fun markSetupDone() = repo.prefs.edit { putBoolean(Prefs.K.SETUP_DONE, true) }

    fun setSafeContact(c: SafeContact?) = repo.prefs.edit {
        if (c == null) {
            remove(Prefs.K.SAFE_NAME); remove(Prefs.K.SAFE_NUMBER)
        } else {
            putString(Prefs.K.SAFE_NAME, c.name); putString(Prefs.K.SAFE_NUMBER, c.number)
        }
    }

    fun setHomeAddress(a: String) = repo.prefs.edit { putString(Prefs.K.HOME_ADDRESS, a) }
    fun setUnlockTime(h: Int, m: Int) = repo.prefs.edit { putInt(Prefs.K.UNLOCK_HOUR, h); putInt(Prefs.K.UNLOCK_MINUTE, m) }
    fun setTestInterval(min: Int) = repo.prefs.edit { putInt(Prefs.K.TEST_INTERVAL, min) }
    fun setAutoLock(bac: Double) = repo.prefs.edit { putFloat(Prefs.K.AUTO_LOCK_BAC, bac.toFloat()) }
    fun setCallBlockAll(v: Boolean) = repo.prefs.edit { putBoolean(Prefs.K.CALL_BLOCK_ALL, v) }
    fun setWaterReminder(v: Boolean) = repo.prefs.edit { putBoolean(Prefs.K.WATER_REMINDER, v) }

    fun addBlockedContact(name: String, number: String) = io { repo.addBlockedContact(name, number) }
    fun removeBlockedContact(c: BlockedContact) = io { repo.removeBlockedContact(c) }
    fun setLockedApp(pkg: String, label: String, enabled: Boolean) = io { repo.setLockedApp(pkg, label, enabled) }
    fun removeLockedApp(app: LockedApp) = io { repo.removeLockedApp(app) }

    fun addSuggestedApps() = io {
        val ctx = getApplication<Application>()
        AppCatalog.suggested.filter { AppCatalog.isInstalled(ctx, it) }.forEach {
            repo.addLockedAppIfMissing(it, AppCatalog.label(ctx, it))
        }
    }

    fun markReportSeen(id: Long) = io { repo.markReportSeen(id) }

    private fun launchMain(block: () -> Unit) {
        viewModelScope.launch(Dispatchers.Main) { block() }
    }
}
