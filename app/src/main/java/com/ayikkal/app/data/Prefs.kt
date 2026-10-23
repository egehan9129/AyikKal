package com.ayikkal.app.data

import android.content.Context
import android.content.SharedPreferences
import com.ayikkal.app.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SafeContact(val name: String, val number: String)

/**
 * Ayarlar ve anlık koruma durumu. SharedPreferences tercih edildi çünkü erişilebilirlik servisi
 * ve alarm alıcıları durumu senkron ve hızlı okumak zorunda.
 */
data class GuardState(
    val onboardingDone: Boolean = false,
    val setupDone: Boolean = false,
    val profile: UserProfile = UserProfile(),
    val safeContact: SafeContact? = null,
    val homeAddress: String = "",
    val unlockHour: Int = 8,
    val unlockMinute: Int = 0,
    val testIntervalMin: Int = 45,
    /** 0 = kapalı. Tahmini promil bu değeri geçerse test beklenmeden kalkan açılır. */
    val autoLockBac: Double = 0.0,
    /** true: sarhoş modunda güvenli kişi dışındaki tüm giden aramalar kesilir. */
    val callBlockAll: Boolean = false,
    val waterReminder: Boolean = true,

    val activeSessionId: Long = 0,
    val sessionStartedAt: Long = 0,

    val drunkActive: Boolean = false,
    val drunkSince: Long = 0,
    val unlockAt: Long = 0,
    val drunkReason: String = "",

    val testPending: Boolean = false,
    val testDeadline: Long = 0,
    val testInProgress: Boolean = false,
    val missCount: Int = 0,
    val nextTestAt: Long = 0,

    val lastSoberAttemptAt: Long = 0,
    val baselineReactionMs: Int = 0,

    val bypassPackages: Set<String> = emptySet(),
    val bypassUntil: Long = 0,
) {
    val sessionActive: Boolean get() = activeSessionId != 0L
}

class Prefs(context: Context) {
    private val sp: SharedPreferences = context.getSharedPreferences("ayikkal", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<GuardState> = _state.asStateFlow()
    val current: GuardState get() = _state.value

    // Alan olarak tutuluyor; SharedPreferences dinleyicileri zayıf referansla saklar.
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> _state.value = read() }

    init {
        sp.registerOnSharedPreferenceChangeListener(listener)
    }

    @Synchronized
    fun edit(block: SharedPreferences.Editor.() -> Unit) {
        sp.edit().apply(block).commit()
        _state.value = read()
    }

    private fun read(): GuardState = GuardState(
        onboardingDone = sp.getBoolean(K.ONBOARDING_DONE, false),
        setupDone = sp.getBoolean(K.SETUP_DONE, false),
        profile = UserProfile(
            male = sp.getBoolean(K.MALE, true),
            weightKg = sp.getInt(K.WEIGHT, 75),
            heightCm = sp.getInt(K.HEIGHT, 175),
            age = sp.getInt(K.AGE, 25),
            fullStomach = sp.getBoolean(K.FULL_STOMACH, true),
        ),
        safeContact = sp.getString(K.SAFE_NUMBER, null)?.let { SafeContact(sp.getString(K.SAFE_NAME, "") ?: "", it) },
        homeAddress = sp.getString(K.HOME_ADDRESS, "") ?: "",
        unlockHour = sp.getInt(K.UNLOCK_HOUR, 8),
        unlockMinute = sp.getInt(K.UNLOCK_MINUTE, 0),
        testIntervalMin = sp.getInt(K.TEST_INTERVAL, 45),
        autoLockBac = sp.getFloat(K.AUTO_LOCK_BAC, 0f).toDouble(),
        callBlockAll = sp.getBoolean(K.CALL_BLOCK_ALL, false),
        waterReminder = sp.getBoolean(K.WATER_REMINDER, true),
        activeSessionId = sp.getLong(K.ACTIVE_SESSION, 0),
        sessionStartedAt = sp.getLong(K.SESSION_STARTED_AT, 0),
        drunkActive = sp.getBoolean(K.DRUNK_ACTIVE, false),
        drunkSince = sp.getLong(K.DRUNK_SINCE, 0),
        unlockAt = sp.getLong(K.UNLOCK_AT, 0),
        drunkReason = sp.getString(K.DRUNK_REASON, "") ?: "",
        testPending = sp.getBoolean(K.TEST_PENDING, false),
        testDeadline = sp.getLong(K.TEST_DEADLINE, 0),
        testInProgress = sp.getBoolean(K.TEST_IN_PROGRESS, false),
        missCount = sp.getInt(K.MISS_COUNT, 0),
        nextTestAt = sp.getLong(K.NEXT_TEST_AT, 0),
        lastSoberAttemptAt = sp.getLong(K.LAST_SOBER_ATTEMPT, 0),
        baselineReactionMs = sp.getInt(K.BASELINE_REACTION, 0),
        bypassPackages = sp.getStringSet(K.BYPASS_PACKAGES, emptySet()) ?: emptySet(),
        bypassUntil = sp.getLong(K.BYPASS_UNTIL, 0),
    )

    object K {
        const val ONBOARDING_DONE = "onboarding_done"
        const val SETUP_DONE = "setup_done"
        const val MALE = "male"
        const val WEIGHT = "weight"
        const val HEIGHT = "height"
        const val AGE = "age"
        const val FULL_STOMACH = "full_stomach"
        const val SAFE_NAME = "safe_name"
        const val SAFE_NUMBER = "safe_number"
        const val HOME_ADDRESS = "home_address"
        const val UNLOCK_HOUR = "unlock_hour"
        const val UNLOCK_MINUTE = "unlock_minute"
        const val TEST_INTERVAL = "test_interval"
        const val AUTO_LOCK_BAC = "auto_lock_bac"
        const val CALL_BLOCK_ALL = "call_block_all"
        const val WATER_REMINDER = "water_reminder"
        const val ACTIVE_SESSION = "active_session"
        const val SESSION_STARTED_AT = "session_started_at"
        const val DRUNK_ACTIVE = "drunk_active"
        const val DRUNK_SINCE = "drunk_since"
        const val UNLOCK_AT = "unlock_at"
        const val DRUNK_REASON = "drunk_reason"
        const val TEST_PENDING = "test_pending"
        const val TEST_DEADLINE = "test_deadline"
        const val TEST_IN_PROGRESS = "test_in_progress"
        const val MISS_COUNT = "miss_count"
        const val NEXT_TEST_AT = "next_test_at"
        const val LAST_SOBER_ATTEMPT = "last_sober_attempt"
        const val BASELINE_REACTION = "baseline_reaction"
        const val BYPASS_PACKAGES = "bypass_packages"
        const val BYPASS_UNTIL = "bypass_until"
    }
}
