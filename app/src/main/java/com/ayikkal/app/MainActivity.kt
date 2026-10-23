package com.ayikkal.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.graphics.Color
import androidx.compose.runtime.mutableStateOf
import com.ayikkal.app.ui.AppRoot
import com.ayikkal.app.ui.theme.AyikKalTheme

class MainActivity : ComponentActivity() {
    private val pendingDest = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT))
        handle(intent)
        setContent {
            AyikKalTheme {
                AppRoot(pendingDest)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    private fun handle(intent: Intent?) {
        val dest = intent?.getStringExtra(EXTRA_DEST) ?: return
        // Test bildirimi tam ekran açıldığında kilit ekranının üstünde görünsün.
        val urgent = dest == DEST_TEST_RUN
        setShowWhenLocked(urgent)
        setTurnScreenOn(urgent)
        pendingDest.value = dest
        intent.removeExtra(EXTRA_DEST)
    }

    companion object {
        const val EXTRA_DEST = "dest"
        const val DEST_TRACK = "track"
        const val DEST_TEST_RUN = "test_run"
        const val DEST_SHIELD = "shield"
        const val DEST_REPORT = "report"
    }
}
