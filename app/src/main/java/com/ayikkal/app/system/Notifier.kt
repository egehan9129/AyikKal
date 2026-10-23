package com.ayikkal.app.system

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ayikkal.app.MainActivity
import com.ayikkal.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Notifier(private val context: Context) {
    private val nm = NotificationManagerCompat.from(context)
    private val timeFmt = SimpleDateFormat("HH:mm", Locale("tr", "TR"))

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CH_TEST, "Sarhoşluk testleri", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Zorunlu kontrol testleri. Kapatılırsa testler kaçırılmış sayılır."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 800)
                    enableLights(true)
                    lightColor = Color.RED
                },
                NotificationChannel(CH_SHIELD, "Koruma kalkanı", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Sarhoş Modu, engellenen aramalar ve kilit açılışı"
                },
                NotificationChannel(CH_WATER, "Su hatırlatıcısı", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CH_SESSION, "Aktif seans", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "İçme seansı sürerken tahmini promil"
                    setShowBadge(false)
                },
            )
        )
    }

    private fun openApp(dest: String, requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_DEST, dest)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun showTestDue(missCount: Int, deadline: Long, reason: String) {
        val text = if (missCount == 0) {
            "Hey! Kontrol zamanı. Testi çözmek için 2 dakikan var."
        } else {
            "Son uyarı! Bu bildirimi de yok sayarsan Sarhoş Modu açılacak."
        }
        val open = openApp(MainActivity.DEST_TEST_RUN, 1)
        val n = NotificationCompat.Builder(context, CH_TEST)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle("🚨 Zorunlu Sarhoşluk Testi")
            .setContentText(text)
            .setSubText(reason)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(open)
            .setFullScreenIntent(open, true)
            .addAction(0, "Teste Başla", open)
            .setWhen(deadline)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setTimeoutAfter(deadline - System.currentTimeMillis())
            .setAutoCancel(true)
            .build()
        notify(ID_TEST, n)
    }

    fun cancelTest() = nm.cancel(ID_TEST)

    fun showDrunkMode(reason: String, unlockAt: Long) {
        val text = "$reason. Kilitli uygulamalar sabah ${timeFmt.format(Date(unlockAt))}'e kadar kapalı. Git su iç ve uyu bro."
        val n = NotificationCompat.Builder(context, CH_SHIELD)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle("🛡️ Sarhoş Modu devrede!")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setColor(0xFFEF4444.toInt())
            .setContentIntent(openApp(MainActivity.DEST_SHIELD, 2))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notify(ID_SHIELD, n)
    }

    fun showUnlocked(reason: String) {
        val n = NotificationCompat.Builder(context, CH_SHIELD)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle("☀️ Günaydın! Kilitler açıldı")
            .setContentText("$reason. Dün gecenin raporu hazır.")
            .setContentIntent(openApp(MainActivity.DEST_REPORT, 3))
            .setAutoCancel(true)
            .build()
        notify(ID_SHIELD, n)
    }

    fun showCallBlocked(name: String) {
        val n = NotificationCompat.Builder(context, CH_SHIELD)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle("📵 Arama engellendi")
            .setContentText("$name kişisini aramaktan son anda kurtuldun. Sabah bana teşekkür edersin.")
            .setContentIntent(openApp(MainActivity.DEST_SHIELD, 4))
            .setAutoCancel(true)
            .build()
        notify(ID_CALL, n)
    }

    fun showWaterReminder(drinkCount: Int) {
        val n = NotificationCompat.Builder(context, CH_WATER)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle("💧 Su molası")
            .setContentText("$drinkCount kadeh oldu. Baş ağrısı çekmemek için şimdi 1 bardak su iç.")
            .setContentIntent(openApp(MainActivity.DEST_TRACK, 5))
            .setAutoCancel(true)
            .build()
        notify(ID_WATER, n)
    }

    fun cancelWater() = nm.cancel(ID_WATER)

    fun showSession(bac: Double, drinks: Int, drunk: Boolean) {
        val title = if (drunk) "Sarhoş Modu aktif" else "İçme seansı aktif"
        val n = NotificationCompat.Builder(context, CH_SESSION)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(title)
            .setContentText("Tahmini promil %.2f‰ · %d kadeh".format(bac, drinks))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openApp(MainActivity.DEST_TRACK, 6))
            .build()
        notify(ID_SESSION, n)
    }

    fun cancelSession() = nm.cancel(ID_SESSION)

    @SuppressLint("MissingPermission")
    private fun notify(id: Int, n: android.app.Notification) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33
        if (granted) nm.notify(id, n)
    }

    companion object {
        const val CH_TEST = "tests"
        const val CH_SHIELD = "shield"
        const val CH_WATER = "water"
        const val CH_SESSION = "session"
        private const val ID_TEST = 10
        private const val ID_SHIELD = 11
        private const val ID_WATER = 12
        private const val ID_SESSION = 13
        private const val ID_CALL = 14
    }
}
