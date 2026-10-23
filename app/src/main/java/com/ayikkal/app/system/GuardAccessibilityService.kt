package com.ayikkal.app.system

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ayikkal.app.AyikKalApp
import com.ayikkal.app.R
import com.ayikkal.app.data.db.EventType
import com.ayikkal.app.ui.block.BlockActivity

/**
 * Ön plana gelen uygulamayı izler; Sarhoş Modu'nda kilitli bir uygulama açılırsa ana ekrana
 * BlockActivity ile üstünü kapatır. Erişilebilirlik servisleri arka plandan aktivite başlatma
 * kısıtlamasından muaf olduğu için README'deki SYSTEM_ALERT_WINDOW iznine gerek kalmaz.
 */
class GuardAccessibilityService : AccessibilityService() {
    private val repo get() = (application as AyikKalApp).repository
    private var dialerPackages: Set<String> = emptySet()
    private var lastBlockPkg: String? = null
    private var lastBlockAt = 0L


    override fun onServiceConnected() {
        super.onServiceConnected()
        dialerPackages = AppCatalog.dialerPackages(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        val state = repo.prefs.current
        if (!state.drunkActive) return

        val now = System.currentTimeMillis()
        if (now >= state.unlockAt) {
            repo.launch { unlock("Sabah kilit saati geldi") }
            return
        }
        if (now < state.bypassUntil && pkg in state.bypassPackages) return

        val isStateChange = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        val locked = repo.lockedApps.value.any { it.enabled && it.packageName == pkg }

        if (locked && isStateChange) {
            // Arama sürerken çağrı ekranını kapatmak, güvenli kişiyle konuşmayı da keserdi.
            if (pkg in dialerPackages && inCall()) return
            block(pkg, EventType.APP_BLOCKED)
            return
        }

        if (pkg in SELF_PROTECT_PACKAGES) {
            // Olay anında aktif pencere henüz yeni ekran olmayabilir; ekran oturduktan sonra bak.
            handler.removeCallbacks(selfCheck)
            handler.postDelayed(selfCheck, 400)
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val selfCheck = Runnable {
        if (repo.prefs.current.drunkActive) screenMentionsUs()?.let { block(it, EventType.SETTINGS_BLOCKED) }
    }

    /**
     * Kaçış koruması: Sarhoş Modu'nda ayarlar/kaldırıcı ekranında AyıkKal görünüyorsa
     * (erişilebilirliği kapatma, uygulamayı kaldırma, verileri silme) ekran kapatılır.
     * Eşleşirse o pencerenin paket adını döndürür.
     */
    private fun screenMentionsUs(): String? {
        val root = rootInActiveWindow ?: return null
        val pkg = root.packageName?.toString() ?: return null
        if (pkg !in SELF_PROTECT_PACKAGES) return null
        return try {
            pkg.takeIf { containsText(root, getString(R.string.app_name), budget = intArrayOf(800)) }
        } catch (_: Exception) {
            null
        }
    }

    /** findAccessibilityNodeInfosByText Compose ekranlarında (yeni Ayarlar) çalışmadığı için elle arama. */
    private fun containsText(node: AccessibilityNodeInfo, needle: String, budget: IntArray): Boolean {
        if (budget[0]-- <= 0) return false
        if (node.text?.contains(needle, ignoreCase = true) == true) return true
        if (node.contentDescription?.contains(needle, ignoreCase = true) == true) return true
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (containsText(child, needle, budget)) return true
        }
        return false
    }

    private fun inCall(): Boolean {
        val mode = getSystemService(AudioManager::class.java)?.mode ?: return false
        return mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION || mode == AudioManager.MODE_RINGTONE
    }

    private fun block(pkg: String, type: EventType) {
        val now = System.currentTimeMillis()
        val label = AppCatalog.label(this, pkg)
        // Aynı uygulama için art arda gelen olayları tek deneme say.
        if (pkg != lastBlockPkg || now - lastBlockAt > 4_000) {
            repo.log(type, "$pkg|$label")
        }
        lastBlockPkg = pkg
        lastBlockAt = now
        // GLOBAL_ACTION_HOME burada kullanılmaz: asenkron çalışıp launcher'ı engel ekranının üstüne getirir.
        // BlockActivity kilitli uygulamanın üstünü kapatır; çıkışta kullanıcıyı ana ekrana gönderir.
        startActivity(
            Intent(this, BlockActivity::class.java)
                .putExtra(BlockActivity.EXTRA_LABEL, label)
                .putExtra(BlockActivity.EXTRA_KIND, if (type == EventType.SETTINGS_BLOCKED) BlockActivity.KIND_SETTINGS else BlockActivity.KIND_APP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        )
    }

    override fun onInterrupt() = Unit

    companion object {
        private val SELF_PROTECT_PACKAGES = setOf(
            "com.android.settings",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.samsung.android.packageinstaller",
            "com.miui.securitycenter",
        )
    }
}
