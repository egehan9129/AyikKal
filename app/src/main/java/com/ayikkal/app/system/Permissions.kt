package com.ayikkal.app.system

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

enum class PermissionKey {
    NOTIFICATIONS,
    ACCESSIBILITY,
    EXACT_ALARM,
    FULL_SCREEN,
    CALL_REDIRECTION,
    CALL_SCREENING,
    BATTERY,
    CALL_PHONE,
    SMS,
    LOCATION,
}

data class PermissionStatus(
    val key: PermissionKey,
    val title: String,
    val why: String,
    val granted: Boolean,
    val critical: Boolean,
)

object Permissions {
    fun all(context: Context): List<PermissionStatus> = listOf(
        PermissionStatus(
            PermissionKey.ACCESSIBILITY, "Erişilebilirlik servisi",
            "Kilitli uygulamaları açıldığı an algılayıp engellemek için. Kalkanın kalbi bu.",
            isAccessibilityEnabled(context), critical = true,
        ),
        PermissionStatus(
            PermissionKey.NOTIFICATIONS, "Bildirimler",
            "Zorunlu test bildirimleri gelmezse koruma çalışmaz.",
            NotificationManagerCompat.from(context).areNotificationsEnabled(), critical = true,
        ),
        PermissionStatus(
            PermissionKey.EXACT_ALARM, "Tam zamanlı alarmlar",
            "Testlerin ve sabah kilit açılışının dakikası dakikasına çalışması için.",
            Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms(), critical = true,
        ),
        PermissionStatus(
            PermissionKey.FULL_SCREEN, "Tam ekran bildirim",
            "Ekran kilitliyken bile test ekranı alarm gibi açılsın.",
            Build.VERSION.SDK_INT < 34 || context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent(), critical = false,
        ),
        PermissionStatus(
            PermissionKey.CALL_REDIRECTION, "Giden arama engeli",
            "Kara listedeki numaraları aradığın an aramayı keser. Telefon uygulaman normal çalışır.",
            isRoleHeld(context, RoleManager.ROLE_CALL_REDIRECTION), critical = false,
        ),
        PermissionStatus(
            PermissionKey.CALL_SCREENING, "Gelen arama filtresi",
            "Sarhoşken kara listedeki kişi ararsa çağrıyı sessizce reddeder.",
            isRoleHeld(context, RoleManager.ROLE_CALL_SCREENING), critical = false,
        ),
        PermissionStatus(
            PermissionKey.BATTERY, "Pil optimizasyonu muafiyeti",
            "Bazı telefonlar arka plandaki uygulamaları öldürür; muafiyet alarmları korur.",
            context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName), critical = false,
        ),
        PermissionStatus(
            PermissionKey.CALL_PHONE, "Doğrudan arama",
            "Güvenli kişiyi tek dokunuşla, numara çevirmeden aramak için.",
            granted(context, Manifest.permission.CALL_PHONE), critical = false,
        ),
        PermissionStatus(
            PermissionKey.SMS, "SMS gönderme",
            "Konumunu tek tuşla güvenli kişiye göndermek için.",
            granted(context, Manifest.permission.SEND_SMS), critical = false,
        ),
        PermissionStatus(
            PermissionKey.LOCATION, "Konum",
            "Canlı konum mesajına harita linki eklemek için. Konum hiçbir sunucuya gitmez.",
            granted(context, Manifest.permission.ACCESS_FINE_LOCATION), critical = false,
        ),
    )

    fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun isAccessibilityEnabled(context: Context): Boolean {
        val expected = ComponentName(context, GuardAccessibilityService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    fun isRoleHeld(context: Context, role: String): Boolean {
        val rm = context.getSystemService(RoleManager::class.java) ?: return false
        return rm.isRoleAvailable(role) && rm.isRoleHeld(role)
    }

    fun roleIntent(context: Context, role: String): Intent? {
        val rm = context.getSystemService(RoleManager::class.java) ?: return null
        return if (rm.isRoleAvailable(role)) rm.createRequestRoleIntent(role) else null
    }

    fun accessibilitySettings() = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    fun exactAlarmSettings(context: Context) =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))

    fun fullScreenSettings(context: Context) =
        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${context.packageName}"))

    @android.annotation.SuppressLint("BatteryLife")
    fun batterySettings(context: Context) =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))

    fun notificationSettings(context: Context) =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    fun appDetails(context: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
}
