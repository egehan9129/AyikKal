package com.ayikkal.app.ui.block

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayikkal.app.AyikKalApp
import com.ayikkal.app.ui.components.GhostButton
import com.ayikkal.app.ui.components.GlassCard
import com.ayikkal.app.ui.components.SafeExits
import com.ayikkal.app.ui.components.StatusPill
import com.ayikkal.app.ui.components.glow
import com.ayikkal.app.ui.fmtDuration
import com.ayikkal.app.ui.fmtTime
import com.ayikkal.app.ui.rememberNow
import com.ayikkal.app.ui.screens.drunkMessages
import com.ayikkal.app.ui.theme.Ak
import com.ayikkal.app.ui.theme.AyikKalTheme

/** Kilitli bir uygulama veya kara listedeki bir arama denendiğinde çıkan tam ekran engel. */
class BlockActivity : ComponentActivity() {
    private val label = mutableStateOf("")
    private val kind = mutableStateOf(KIND_APP)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT))
        read(intent)
        val repo = (application as AyikKalApp).repository
        setContent {
            AyikKalTheme {
                val state by repo.state.collectAsStateWithLifecycle()
                // Kilit açıldıysa (sabah oldu / ayılma testi) bu ekranın anlamı kalmaz.
                LaunchedEffect(state.drunkActive) { if (!state.drunkActive) finish() }
                BackHandler { goHome() }
                BlockContent(label.value, kind.value, state.unlockAt, state.safeContact, state.homeAddress, ::goHome)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        read(intent)
    }

    private fun read(intent: Intent) {
        label.value = intent.getStringExtra(EXTRA_LABEL) ?: ""
        kind.value = intent.getStringExtra(EXTRA_KIND) ?: KIND_APP
    }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    companion object {
        const val EXTRA_LABEL = "label"
        const val EXTRA_KIND = "kind"
        const val KIND_APP = "app"
        const val KIND_CALL = "call"
        const val KIND_SETTINGS = "settings"
    }
}

@Composable
private fun BlockContent(
    label: String,
    kind: String,
    unlockAt: Long,
    safe: com.ayikkal.app.data.SafeContact?,
    homeAddress: String,
    onHome: () -> Unit,
) {
    val now = rememberNow(30_000)
    val message = remember(label) { drunkMessages.random() }
    val (title, body) = when (kind) {
        BlockActivity.KIND_CALL -> "$label aranmıyor." to "Bu aramayı kestim. Sabah hâlâ istiyorsan ayık kafayla ararsın."
        BlockActivity.KIND_SETTINGS -> "Güzel deneme 😏" to "Sarhoş Modu'ndayken AyıkKal'ı kapatamaz, kaldıramazsın."
        else -> "Şu an sarhoşsun." to "$label sabaha kadar kapalı. $message"
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(Ak.Bg)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GlassCard(
            Modifier.glow(Ak.Crimson, 0.9f, 0.3f),
            border = Ak.Crimson.copy(alpha = 0.3f),
            padding = PaddingValues(24.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Ak.Lowest)
                        .border(2.dp, Ak.Crimson.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        when (kind) {
                            BlockActivity.KIND_CALL -> Icons.Rounded.PhoneDisabled
                            BlockActivity.KIND_SETTINGS -> Icons.Rounded.Shield
                            else -> Icons.Rounded.Lock
                        },
                        null,
                        tint = Ak.TertiaryContainer,
                        modifier = Modifier.size(56.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                StatusPill("SARHOŞ MODU DEVREDE", Ak.Crimson, pulsing = true)
                Spacer(Modifier.height(16.dp))
                Text(title, style = MaterialTheme.typography.displaySmall, color = Ak.TertiaryContainer, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(body, style = MaterialTheme.typography.bodyLarge, color = Ak.OnVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Kilit ${fmtTime(unlockAt)}'de açılır · ${fmtDuration(unlockAt - now)} kaldı",
                    style = MaterialTheme.typography.labelLarge,
                    color = Ak.Secondary,
                )
            }
        }
        SafeExits(safe, homeAddress)
        GhostButton("Ana Ekrana Dön", Icons.Rounded.Home, onHome)
    }
}
