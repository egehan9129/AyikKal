package com.ayikkal.app.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Telephony
import android.telecom.TelecomManager

data class InstalledApp(val packageName: String, val label: String, val icon: Drawable?)

/** Yüklü uygulamalar ve telefon/SMS gibi sistem rollerinin paket adları. */
object AppCatalog {
    /** Sarhoşken en çok pişmanlık yaratan uygulamalar; kurulumda "Önerilenleri ekle" ile seçilir. */
    val suggested = listOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.instagram.android",
        "com.twitter.android",
        "com.tinder",
        "com.bumble.app",
        "org.telegram.messenger",
        "com.snapchat.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.zhiliaoapp.musically",
        "com.linkedin.android",
        "com.slack",
        "com.microsoft.teams",
        "com.google.android.gm",
    )

    fun launchableApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo.applicationInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { InstalledApp(it.packageName, pm.getApplicationLabel(it).toString(), runCatching { pm.getApplicationIcon(it) }.getOrNull()) }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun label(context: Context, packageName: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)

    fun isInstalled(context: Context, packageName: String): Boolean = runCatching {
        context.packageManager.getPackageInfo(packageName, 0); true
    }.getOrDefault(false)

    fun icon(context: Context, packageName: String): Drawable? =
        runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()

    /** Varsayılan arama uygulaması, çağrı ekranı ve rehber paketleri. */
    fun dialerPackages(context: Context): Set<String> {
        val pm = context.packageManager
        val result = mutableSetOf(
            "com.android.server.telecom",
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.samsung.android.incallui",
            "com.android.incallui",
        )
        context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage?.let { result += it }
        pm.queryIntentActivities(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0")), PackageManager.MATCH_DEFAULT_ONLY)
            .forEach { result += it.activityInfo.packageName }
        return result
    }

    fun smsPackage(context: Context): String? = Telephony.Sms.getDefaultSmsPackage(context)
}
