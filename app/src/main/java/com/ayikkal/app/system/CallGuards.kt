package com.ayikkal.app.system

import android.content.Intent
import android.net.Uri
import android.telecom.Call
import android.telecom.CallRedirectionService
import android.telecom.CallScreeningService
import android.telecom.PhoneAccountHandle
import com.ayikkal.app.AyikKalApp
import com.ayikkal.app.data.db.EventType
import com.ayikkal.app.ui.block.BlockActivity

/**
 * Giden arama engeli. README'deki iki yöntemden (Telefon uygulamasını kilitlemek / varsayılan
 * arama uygulaması olmak) daha temiz bir yol: Android 10+ "Arama yönlendirme" rolü. Kullanıcının
 * kendi Telefon uygulaması çalışmaya devam eder, yalnızca kara listedeki numaralar kesilir.
 * Acil numaralar (112) sistem tarafından bu servise hiç gönderilmez.
 */
class AyikCallRedirectionService : CallRedirectionService() {
    override fun onPlaceCall(handle: Uri, initialPhoneAccount: PhoneAccountHandle, allowInteractiveResponse: Boolean) {
        val app = application as AyikKalApp
        val repo = app.repository
        val state = repo.prefs.current
        val number = handle.schemeSpecificPart ?: ""
        if (!state.drunkActive || repo.isSafeContact(number)) {
            placeCallUnmodified()
            return
        }
        val blocked = repo.findBlacklisted(number)
        val name = when {
            blocked != null -> blocked.name
            state.callBlockAll -> number
            else -> null
        }
        if (name == null) {
            placeCallUnmodified()
            return
        }
        cancelCall()
        repo.log(EventType.CALL_BLOCKED, "$name|$number")
        app.notifier.showCallBlocked(name)
        runCatching {
            startActivity(
                Intent(this, BlockActivity::class.java)
                    .putExtra(BlockActivity.EXTRA_LABEL, name)
                    .putExtra(BlockActivity.EXTRA_KIND, BlockActivity.KIND_CALL)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

/** Gelen arama filtresi: Sarhoş Modu'nda kara listedeki kişi ararsa sessizce reddedilir. */
class AyikCallScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val repo = (application as AyikKalApp).repository
        val number = details.handle?.schemeSpecificPart ?: ""
        val incoming = details.callDirection == Call.Details.DIRECTION_INCOMING
        val blocked = if (incoming && repo.prefs.current.drunkActive) repo.findBlacklisted(number) else null
        val response = if (blocked != null) {
            repo.log(EventType.INCOMING_BLOCKED, "${blocked.name}|$number")
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(false)
                .setSkipCallLog(false)
                .build()
        } else {
            CallResponse.Builder().build()
        }
        respondToCall(details, response)
    }
}
