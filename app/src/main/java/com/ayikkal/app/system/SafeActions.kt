package com.ayikkal.app.system

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ayikkal.app.AyikKalApp
import com.ayikkal.app.data.db.EventType
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/** Sarhoşken bile izin verilen güvenli çıkışlar: güvenli kişi, konum, taksi, 112. */
object SafeActions {
    private const val BITAKSI = "com.bitaksi.musteri"
    private const val UBER = "com.ubercab"

    private fun repo(context: Context) = (context.applicationContext as AyikKalApp).repository

    private fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun callSafeContact(context: Context) {
        val contact = repo(context).prefs.current.safeContact ?: run {
            Toast.makeText(context, "Önce Ayarlar'dan güvenli kişi seç", Toast.LENGTH_SHORT).show()
            return
        }
        val repo = repo(context)
        repo.grantBypass(AppCatalog.dialerPackages(context), 3 * 60_000L)
        repo.log(EventType.SAFE_CALL, contact.name)
        val uri = Uri.parse("tel:${contact.number}")
        val intent = if (granted(context, Manifest.permission.CALL_PHONE)) Intent(Intent.ACTION_CALL, uri) else Intent(Intent.ACTION_DIAL, uri)
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun callEmergency(context: Context) {
        repo(context).grantBypass(AppCatalog.dialerPackages(context), 5 * 60_000L)
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Konumu SMS ile güvenli kişiye gönderir; SMS izni yoksa mesaj uygulamasını hazır metinle açar. */
    suspend fun sendLocation(context: Context, extra: String = "Biraz fazla içtim.") {
        val repo = repo(context)
        val contact = repo.prefs.current.safeContact
        if (contact == null) {
            Toast.makeText(context, "Önce Ayarlar'dan güvenli kişi seç", Toast.LENGTH_SHORT).show()
            return
        }
        val location = currentLocation(context)
        val link = location?.let { "https://maps.google.com/?q=%.6f,%.6f".format(java.util.Locale.US, it.latitude, it.longitude) }
        val body = buildString {
            append("AyıkKal: ").append(extra)
            if (link != null) append(" Şu an buradayım: ").append(link) else append(" Konumumu alamadım, beni arar mısın?")
        }
        repo.log(EventType.LOCATION_SENT, contact.name)
        if (granted(context, Manifest.permission.SEND_SMS)) {
            val sms = if (Build.VERSION.SDK_INT >= 31) context.getSystemService(SmsManager::class.java) else @Suppress("DEPRECATION") SmsManager.getDefault()
            val parts = sms.divideMessage(body)
            sms.sendMultipartTextMessage(contact.number, null, parts, null, null)
            Toast.makeText(context, "Konum ${contact.name} kişisine gönderildi", Toast.LENGTH_LONG).show()
        } else {
            AppCatalog.smsPackage(context)?.let { repo.grantBypass(setOf(it), 3 * 60_000L) }
            context.startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.number}"))
                    .putExtra("sms_body", body)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** "İyiyim" mesajı: sabah raporundan güvenli kişiye. */
    fun sendImOk(context: Context) {
        val contact = repo(context).prefs.current.safeContact ?: return
        context.startActivity(
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.number}"))
                .putExtra("sms_body", "Günaydın! Eve sağ salim vardım, iyiyim. Dün gece kolladığın için teşekkürler 🙏")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openBiTaksi(context: Context) {
        val address = repo(context).prefs.current.homeAddress
        if (address.isNotBlank()) {
            context.getSystemService(ClipboardManager::class.java)
                .setPrimaryClip(ClipData.newPlainText("Ev adresi", address))
            Toast.makeText(context, "Ev adresin panoya kopyalandı", Toast.LENGTH_SHORT).show()
        }
        repo(context).grantBypass(setOf(BITAKSI), 10 * 60_000L)
        openAppOrStore(context, BITAKSI)
    }

    fun openUber(context: Context) {
        val address = repo(context).prefs.current.homeAddress
        repo(context).grantBypass(setOf(UBER), 10 * 60_000L)
        val drop = if (address.isNotBlank()) "&dropoff[formatted_address]=${Uri.encode(address)}" else ""
        val deep = Intent(Intent.ACTION_VIEW, Uri.parse("uber://?action=setPickup&pickup=my_location$drop"))
        if (AppCatalog.isInstalled(context, UBER)) {
            context.startActivity(deep.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            val web = Uri.parse("https://m.uber.com/ul/?action=setPickup&pickup=my_location$drop")
            context.startActivity(Intent(Intent.ACTION_VIEW, web).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun openRouteHome(context: Context) {
        val address = repo(context).prefs.current.homeAddress
        if (address.isBlank()) {
            Toast.makeText(context, "Ayarlar'a ev adresini ekle", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(address)}&travelmode=transit")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun openAppOrStore(context: Context, pkg: String) {
        val launch = context.packageManager.getLaunchIntentForPackage(pkg)
        val intent = launch ?: Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(context: Context): Location? {
        val fine = granted(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = granted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!fine && !coarse) return null
        val lm = context.getSystemService(LocationManager::class.java)
        val providers = lm.getProviders(true)
        val fresh = if (Build.VERSION.SDK_INT >= 30) {
            val provider = when {
                fine && LocationManager.GPS_PROVIDER in providers -> LocationManager.GPS_PROVIDER
                LocationManager.NETWORK_PROVIDER in providers -> LocationManager.NETWORK_PROVIDER
                else -> providers.firstOrNull()
            }
            provider?.let { p ->
                withTimeoutOrNull(8_000) {
                    suspendCancellableCoroutine<Location?> { cont ->
                        val executor = Executors.newSingleThreadExecutor()
                        lm.getCurrentLocation(p, null, executor) { loc ->
                            executor.shutdown()
                            if (cont.isActive) cont.resume(loc)
                        }
                    }
                }
            }
        } else null
        return fresh ?: providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }
    }
}
