package com.ayikkal.app.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ayikkal.app.AyikKalApp
import kotlinx.coroutines.launch

/**
 * README'de WorkManager da geçiyor; ancak WorkManager 15 dk altı ve dakika hassasiyetli
 * tetikleme garanti etmez. "2 dakikan var" gibi kesin süreler için AlarmManager kullanılıyor.
 */
class AlarmScheduler(private val context: Context) {
    private val am = context.getSystemService(AlarmManager::class.java)

    fun scheduleTestDue(at: Long) = set(ACTION_TEST_DUE, REQ_TEST_DUE, at)
    fun scheduleTestDeadline(at: Long) = set(ACTION_TEST_DEADLINE, REQ_TEST_DEADLINE, at)
    fun scheduleUnlock(at: Long) = set(ACTION_UNLOCK, REQ_UNLOCK, at)

    fun cancelTestDue() = cancel(ACTION_TEST_DUE, REQ_TEST_DUE)
    fun cancelTestDeadline() = cancel(ACTION_TEST_DEADLINE, REQ_TEST_DEADLINE)
    fun cancelUnlock() = cancel(ACTION_UNLOCK, REQ_UNLOCK)

    fun cancelTestAlarms() {
        cancelTestDue()
        cancelTestDeadline()
    }

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()

    private fun set(action: String, requestCode: Int, at: Long) {
        val pi = pending(action, requestCode)
        if (canScheduleExact()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    private fun cancel(action: String, requestCode: Int) = am.cancel(pending(action, requestCode))

    private fun pending(action: String, requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        Intent(context, AlarmReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val ACTION_TEST_DUE = "com.ayikkal.app.TEST_DUE"
        const val ACTION_TEST_DEADLINE = "com.ayikkal.app.TEST_DEADLINE"
        const val ACTION_UNLOCK = "com.ayikkal.app.UNLOCK"
        private const val REQ_TEST_DUE = 101
        private const val REQ_TEST_DEADLINE = 102
        private const val REQ_UNLOCK = 103
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = (context.applicationContext as AyikKalApp).repository
        val pending = goAsync()
        AyikKalApp.appScope.launch {
            try {
                when (intent.action) {
                    AlarmScheduler.ACTION_TEST_DUE -> repo.onTestDue()
                    AlarmScheduler.ACTION_TEST_DEADLINE -> repo.onTestDeadline()
                    AlarmScheduler.ACTION_UNLOCK -> repo.unlock("Sabah kilit saati geldi")
                }
            } finally {
                pending.finish()
            }
        }
    }
}

/** Telefon yeniden başlarsa alarmlar silinir; durum Prefs'ten geri kurulur. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        ) return
        val repo = (context.applicationContext as AyikKalApp).repository
        val pending = goAsync()
        AyikKalApp.appScope.launch {
            try {
                repo.reconcile()
            } finally {
                pending.finish()
            }
        }
    }
}
