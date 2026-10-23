package com.ayikkal.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.GppGood
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayikkal.app.data.Repository
import com.ayikkal.app.data.db.EventType
import com.ayikkal.app.data.db.LockedApp
import com.ayikkal.app.system.AppCatalog
import com.ayikkal.app.ui.AppViewModel
import com.ayikkal.app.ui.components.BigButton
import com.ayikkal.app.ui.components.GhostButton
import com.ayikkal.app.ui.components.GlassCard
import com.ayikkal.app.ui.components.IconBadge
import com.ayikkal.app.ui.components.PillButton
import com.ayikkal.app.ui.components.SafeExits
import com.ayikkal.app.ui.components.SectionHeader
import com.ayikkal.app.ui.components.StatusPill
import com.ayikkal.app.ui.components.glow
import com.ayikkal.app.ui.fmtDuration
import com.ayikkal.app.ui.fmtTime
import com.ayikkal.app.ui.rememberNow
import com.ayikkal.app.ui.theme.Ak

val drunkMessages = listOf(
    "Git su iç ve uyu bro!",
    "Bu mesaj sabah daha iyi durur.",
    "Yarın bana teşekkür edeceksin.",
    "Bugünlük bu kadar kahraman.",
    "Eski sevgili uyuyor, sen de uyu.",
)

@Composable
fun ShieldScreen(vm: AppViewModel, onSoberTest: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val apps by vm.lockedApps.collectAsStateWithLifecycle()
    val contacts by vm.blockedContacts.collectAsStateWithLifecycle()
    val events by vm.sessionEvents.collectAsStateWithLifecycle()
    val now = rememberNow(15_000)
    var confirmLock by remember { mutableStateOf(false) }
    val enabledApps = apps.filter { it.enabled }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.drunkActive) {
            val message = remember(state.drunkSince) { drunkMessages.random() }
            val water = events.count { it.type == EventType.WATER && it.at >= state.drunkSince }
            val blocked = events.count { it.type == EventType.APP_BLOCKED || it.type == EventType.CALL_BLOCKED || it.type == EventType.INCOMING_BLOCKED }
            GlassCard(
                Modifier.glow(Ak.Crimson, 0.9f, 0.28f),
                container = Ak.Container.copy(alpha = 0.9f),
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
                    ) { Icon(Icons.Rounded.Shield, null, tint = Ak.TertiaryContainer, modifier = Modifier.size(56.dp)) }
                    Spacer(Modifier.height(16.dp))
                    StatusPill("🛡️ SARHOŞ MODU DEVREDE!", Ak.Crimson, pulsing = true)
                    Spacer(Modifier.height(16.dp))
                    Text(message, style = MaterialTheme.typography.displaySmall, color = Ak.TertiaryContainer, textAlign = TextAlign.Center)
                    Text("🛌💧", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Saat ${fmtTime(state.drunkSince)}. ${state.drunkReason}. Sabah bize teşekkür edeceksin.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ak.OnVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    PillButton("1 Bardak Su İçtim ($water)", Icons.Rounded.WaterDrop, Ak.Primary, { vm.addWater() })
                }
            }

            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Rounded.HourglassBottom, Ak.Secondary, size = 60.dp, shape = RoundedCornerShape(50))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("KİLİTLERİN OTOMATİK AÇILMA SAATİ", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(fmtTime(state.unlockAt), style = MaterialTheme.typography.displaySmall, color = Ak.OnSurface)
                            Spacer(Modifier.width(8.dp))
                            Text("(${fmtDuration(state.unlockAt - now)} kaldı)", style = MaterialTheme.typography.labelLarge, color = Ak.Secondary, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        if (blocked > 0) Text("Bu gece $blocked engellenmiş deneme", style = MaterialTheme.typography.labelMedium, color = Ak.Tertiary)
                    }
                }
            }
        } else {
            GlassCard(Modifier.glow(Ak.Primary, 0.9f, 0.18f), padding = PaddingValues(24.dp)) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Ak.Lowest)
                            .border(2.dp, Ak.Primary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Rounded.GppGood, null, tint = Ak.Primary, modifier = Modifier.size(56.dp)) }
                    Spacer(Modifier.height(16.dp))
                    Text("Kalkan hazırda bekliyor", style = MaterialTheme.typography.headlineMedium, color = Ak.OnSurface, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Testi geçemezsen, süreyi kaçırırsan veya bildirimi 2 kez yok sayarsan Sarhoş Modu otomatik açılır ve kilitler sabah ${"%02d:%02d".format(state.unlockHour, state.unlockMinute)}'e kadar sürer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ak.OnVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        SectionHeader(Icons.Rounded.Lock, "Koruma Altındaki Uygulamalar", if (state.drunkActive) Ak.Tertiary else Ak.Primary) {
            Text(if (state.drunkActive) "Aktif Korumada" else "Hazırda", style = MaterialTheme.typography.labelMedium, color = if (state.drunkActive) Ak.Tertiary else Ak.Primary)
        }
        if (enabledApps.isEmpty()) {
            Text("Henüz kilitlenecek uygulama seçmedin. Ayarlar > Kilitlenecek Uygulamalar.", style = MaterialTheme.typography.bodyMedium, color = Ak.Muted)
        } else {
            enabledApps.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { AppTile(it, state.drunkActive, Modifier.weight(1f)) }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        if (contacts.isNotEmpty()) {
            GlassCard(padding = PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Rounded.PhoneDisabled, Ak.Crimson, size = 44.dp, shape = RoundedCornerShape(50))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("KARA LİSTE GİDEN ARAMA ENGELİ", style = MaterialTheme.typography.labelSmall, color = Ak.TertiaryContainer)
                        Text(contacts.joinToString(" ve ") { it.name }, style = MaterialTheme.typography.titleSmall, color = Ak.OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (state.callBlockAll) Text("Güvenli kişi dışındaki tüm aramalar da kesilir", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                    }
                }
            }
        }

        SafeExits(state.safeContact, state.homeAddress)

        if (state.drunkActive) {
            val nextTry = maxOf(state.drunkSince, state.lastSoberAttemptAt) + Repository.SOBER_COOLDOWN_MS
            val canTry = now >= nextTry
            GhostButton(
                if (canTry) "Gerçekten ayıldım (Zorlu Ayılma Testi – 5 Soru)" else "Ayılma testi ${fmtTime(nextTry)}'de açılır",
                Icons.Rounded.Psychology,
                onSoberTest,
                enabled = canTry,
            )
            Text(
                "Testi başarıyla geçemezseniz kilit süresi 1 saat daha uzatılır.",
                style = MaterialTheme.typography.bodySmall,
                color = Ak.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            BigButton(
                "Şimdi Kilitle",
                Icons.Rounded.LockClock,
                { confirmLock = true },
                color = Ak.Crimson.copy(alpha = 0.85f),
                contentColor = androidx.compose.ui.graphics.Color.White,
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    if (confirmLock) {
        AlertDialog(
            onDismissRequest = { confirmLock = false },
            containerColor = Ak.Container,
            title = { Text("Kalkanı şimdi aç?") },
            text = { Text("Kilitli uygulamalar sabah ${"%02d:%02d".format(state.unlockHour, state.unlockMinute)}'e kadar açılmayacak. Erken çıkış ancak zorlu ayılma testiyle mümkün.") },
            confirmButton = { TextButton(onClick = { confirmLock = false; vm.lockNow("Gönüllü kilit: kendini tanıdın") }) { Text("Kilitle") } },
            dismissButton = { TextButton(onClick = { confirmLock = false }) { Text("Vazgeç") } },
        )
    }
}

@Composable
private fun AppTile(app: LockedApp, active: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    val icon = remember(app.packageName) { AppCatalog.icon(context, app.packageName)?.toBitmap(96, 96)?.asImageBitmap() }
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Ak.Container)
            .border(1.dp, Ak.Hairline, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Image(icon, null, Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)))
        } else {
            IconBadge(Icons.Rounded.Apps, Ak.OnVariant, size = 44.dp, shape = RoundedCornerShape(50))
        }
        Spacer(Modifier.height(8.dp))
        Text(app.label, style = MaterialTheme.typography.titleSmall, color = Ak.OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lock, null, tint = if (active) Ak.TertiaryContainer else Ak.Muted, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text(if (active) "Kilitli" else "Hazır", style = MaterialTheme.typography.labelMedium, color = if (active) Ak.TertiaryContainer else Ak.Muted)
        }
    }
}
