package com.ayikkal.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShareLocation
import androidx.compose.material.icons.rounded.SportsBar
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayikkal.app.data.db.EventType
import com.ayikkal.app.domain.BacCalculator
import com.ayikkal.app.domain.BacLevel
import com.ayikkal.app.domain.TestMode
import com.ayikkal.app.system.SafeActions
import com.ayikkal.app.ui.AppViewModel
import com.ayikkal.app.ui.Routes
import com.ayikkal.app.ui.components.BacRing
import com.ayikkal.app.ui.components.BigButton
import com.ayikkal.app.ui.components.CounterControls
import com.ayikkal.app.ui.components.GlassCard
import com.ayikkal.app.ui.components.IconBadge
import com.ayikkal.app.ui.components.InfoBanner
import com.ayikkal.app.ui.components.PillButton
import com.ayikkal.app.ui.components.SectionHeader
import com.ayikkal.app.ui.components.SquareIconButton
import com.ayikkal.app.ui.components.Tag
import com.ayikkal.app.ui.components.glow
import com.ayikkal.app.ui.components.levelColor
import com.ayikkal.app.ui.fmtBac
import com.ayikkal.app.ui.fmtCountdown
import com.ayikkal.app.ui.fmtDuration
import com.ayikkal.app.ui.fmtTime
import com.ayikkal.app.ui.rememberNow
import com.ayikkal.app.ui.theme.Ak
import kotlinx.coroutines.launch

@Composable
fun TrackScreen(
    vm: AppViewModel,
    accessibilityOn: Boolean,
    onOpenTest: (TestMode) -> Unit,
    onOpenTab: (String) -> Unit,
    onOpenPermissions: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.sessionActive) {
            ActiveSession(vm, onOpenTest, onOpenTab, onAdd = { showAdd = true })
        } else {
            IdleHome(vm, accessibilityOn, onOpenTab, onOpenPermissions, onAdd = { showAdd = true })
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showAdd) AddDrinkSheet(vm, onDismiss = { showAdd = false })
}

// ---------------------------------------------------------------------------- Seans yokken

@Composable
private fun IdleHome(
    vm: AppViewModel,
    accessibilityOn: Boolean,
    onOpenTab: (String) -> Unit,
    onOpenPermissions: () -> Unit,
    onAdd: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val contacts by vm.blockedContacts.collectAsStateWithLifecycle()
    val apps by vm.lockedApps.collectAsStateWithLifecycle()
    val sessions by vm.endedSessions.collectAsStateWithLifecycle()
    val pulse = rememberInfiniteTransition(label = "start")
    val scale by pulse.animateFloat(1f, 1.06f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "s")

    GlassCard(Modifier.glow(Ak.Primary, alpha = 0.16f), padding = androidx.compose.foundation.layout.PaddingValues(24.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Tag("BU GECE İÇİN HAZIR MISIN?", Ak.Primary)
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .size(200.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Ak.Primary, Ak.Emerald, Color(0xFF065F46))))
                    .border(6.dp, Ak.Primary.copy(alpha = 0.25f), CircleShape)
                    .clickable { vm.startSession() },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.SportsBar, null, tint = Ak.OnPrimary, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("İçmeye\nBaşladım", style = MaterialTheme.typography.headlineMedium, color = Ak.OnPrimary, textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Butona bastığın an AyıkKal arka planda promilini hesaplar ve belirli aralıklarla seni test eder.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ak.OnVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "İlk kadehi doğrudan ekle",
                style = MaterialTheme.typography.labelLarge,
                color = Ak.Primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAdd)
                    .padding(8.dp),
            )
        }
    }

    SectionHeader(Icons.Rounded.VerifiedUser, "Güvenlik Duvarı Durumu")
    GlassCard {
        ReadinessRow(Icons.Rounded.PersonOff, "Kara liste", if (contacts.isEmpty()) "Kimse eklenmedi" else "${contacts.size} kişi engelli", contacts.isNotEmpty()) { onOpenTab(Routes.SETTINGS) }
        ReadinessRow(Icons.Rounded.Lock, "Kilitlenecek uygulamalar", "${apps.count { it.enabled }} uygulama", apps.any { it.enabled }) { onOpenTab(Routes.SETTINGS) }
        ReadinessRow(Icons.Rounded.Call, "Güvenli kişi", state.safeContact?.name ?: "Seçilmedi", state.safeContact != null) { onOpenTab(Routes.SETTINGS) }
        ReadinessRow(Icons.Rounded.AccessibilityNew, "Kilit servisi (Erişilebilirlik)", if (accessibilityOn) "Açık" else "Kapalı — kilit çalışmaz", accessibilityOn, onOpenPermissions)
        ReadinessRow(Icons.Rounded.NightsStay, "Kilit açılma saati", "%02d:%02d".format(state.unlockHour, state.unlockMinute), true) { onOpenTab(Routes.SETTINGS) }
    }

    sessions.firstOrNull()?.let { last ->
        GlassCard(onClick = { onOpenTab(Routes.REPORT) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.Insights, Ak.Secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("SON GECE", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                    Text("Zirve promil ${fmtBac(last.peakBac)}‰", style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface)
                    Text("Raporu görüntüle", style = MaterialTheme.typography.labelMedium, color = Ak.Secondary)
                }
            }
        }
    }

    InfoBanner(
        "Promil değeri bir tahmindir. Araç kullanıp kullanmamaya asla bu uygulamaya bakarak karar verme.",
        Ak.Secondary,
        Icons.Rounded.DirectionsCar,
    )
}

@Composable
private fun ReadinessRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, ok: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Ak.Muted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Ak.OnSurface)
            Text(value, style = MaterialTheme.typography.labelMedium, color = if (ok) Ak.Muted else Ak.Secondary)
        }
        Icon(
            if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            null,
            tint = if (ok) Ak.Primary else Ak.Secondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

// ---------------------------------------------------------------------------- Aktif seans

@Composable
private fun ActiveSession(vm: AppViewModel, onOpenTest: (TestMode) -> Unit, onOpenTab: (String) -> Unit, onAdd: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val bac by vm.bac.collectAsStateWithLifecycle()
    val groups by vm.drinkGroups.collectAsStateWithLifecycle()
    val events by vm.sessionEvents.collectAsStateWithLifecycle()
    val now = rememberNow()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmEnd by remember { mutableStateOf(false) }
    val water = events.count { it.type == EventType.WATER }
    val drinks = groups.sumOf { it.count }
    val color = levelColor(bac.level)

    // Oturum çubuğu
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Ak.Container)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(if (state.drunkActive) Ak.Crimson else Ak.Secondary))
        Spacer(Modifier.width(10.dp))
        Text(
            if (state.drunkActive) "Sarhoş Modu Aktif" else "İçme Seansı Aktif",
            style = MaterialTheme.typography.titleSmall,
            color = Ak.OnSurface,
            modifier = Modifier.weight(1f),
        )
        Tag("⏱ ${fmtDuration(now - state.sessionStartedAt)}", Ak.Secondary)
    }

    if (state.testPending) PendingTestCard(state.testDeadline, now) { onOpenTest(TestMode.PERIODIC) }
    if (state.drunkActive) {
        GlassCard(container = Ak.Crimson.copy(alpha = 0.12f), border = Ak.Crimson.copy(alpha = 0.35f), onClick = { onOpenTab(Routes.SHIELD) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.Lock, Ak.Crimson)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Kalkan devrede", style = MaterialTheme.typography.titleMedium, color = Ak.Tertiary)
                    Text("Kilitler ${fmtTime(state.unlockAt)}'de açılacak. Ayrıntılar için dokun.", style = MaterialTheme.typography.bodySmall, color = Ak.OnVariant)
                }
            }
        }
    }

    // Promil halkası
    GlassCard(Modifier.glow(color, alpha = 0.18f), padding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BacRing(bac.current) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TAHMİNİ PROMİL", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(fmtBac(bac.current), style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp), color = color)
                        Text("‰", style = MaterialTheme.typography.headlineMedium, color = color, modifier = Modifier.padding(bottom = 6.dp, start = 2.dp))
                    }
                    Tag(bac.level.label, color)
                }
            }
            Spacer(Modifier.height(14.dp))
            if (bac.current >= BacCalculator.LEGAL_DRIVING_LIMIT || bac.projectedPeak >= BacCalculator.LEGAL_DRIVING_LIMIT) {
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Ak.ErrorContainer.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.DirectionsCar, null, tint = Ak.Tertiary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Yasal sürüş limitinin (0.50‰) üzerinde", style = MaterialTheme.typography.labelLarge, color = Ak.Tertiary)
                }
                Spacer(Modifier.height(10.dp))
            }
            Text(advice(bac.level), style = MaterialTheme.typography.bodyMedium, color = Ak.OnVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MiniStat("Zirve (tahmini)", "${fmtBac(maxOf(bac.peak, bac.projectedPeak))}‰")
                MiniStat("Ayılma", bac.soberAt?.let { fmtTime(it) } ?: "—")
                MiniStat("Kadeh", "$drinks")
            }
        }
    }

    if (bac.current >= 2.5) {
        GlassCard(container = Ak.Rose.copy(alpha = 0.15f), border = Ak.Rose.copy(alpha = 0.4f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.LocalHospital, Ak.Rose)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Alkol zehirlenmesi riski", style = MaterialTheme.typography.titleMedium, color = Ak.Tertiary)
                    Text("Kusma, bilinç bulanıklığı veya yavaş nefes varsa hemen 112'yi ara. Yalnız kalma.", style = MaterialTheme.typography.bodySmall, color = Ak.OnVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            PillButton("112 Acil", Icons.Rounded.LocalHospital, Ak.Rose, { SafeActions.callEmergency(context) }, Modifier.fillMaxWidth(), filled = true)
        }
    }

    // Sonraki test
    if (!state.drunkActive && !state.testPending) {
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.Psychology, Ak.Secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("BİLİŞSEL KONTROL", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                    Text("Sonraki Test", style = MaterialTheme.typography.headlineSmall, color = Ak.OnSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Alarm, null, tint = Ak.Secondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        val left = state.nextTestAt - now
                        Text(
                            if (state.nextTestAt == 0L) "Planlanmadı" else if (left > 0) "${fmtDuration(left + 59_000)} kaldı" else "Birazdan",
                            style = MaterialTheme.typography.labelMedium,
                            color = Ak.Secondary,
                        )
                    }
                }
                PillButton("Hemen Test Yap", null, Ak.Primary, { onOpenTest(TestMode.VOLUNTARY) })
            }
        }
    }

    // Su
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.WaterDrop, Ak.Primary, shape = RoundedCornerShape(50))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                val due = (drinks / 2) - water
                Text(
                    if (due > 0) "$due bardak su borcun var" else "Su dengen iyi ($water bardak)",
                    style = MaterialTheme.typography.titleSmall,
                    color = Ak.OnSurface,
                )
                Text("Her 2 kadehte 1 bardak su; akşamdan kalmayı ve baş ağrısını önle.", style = MaterialTheme.typography.bodySmall, color = Ak.OnVariant)
            }
            Spacer(Modifier.width(8.dp))
            PillButton("Su Ekle", Icons.Rounded.WaterDrop, Ak.Primary, { vm.addWater() })
        }
    }

    // Tüketilenler
    SectionHeader(Icons.Rounded.SportsBar, "Bu Gece Tüketilenler") { Tag("$drinks Kadeh", Ak.OnVariant) }
    if (groups.isEmpty()) {
        Text("Henüz bir şey eklemedin. İlk kadehini aşağıdan ekle.", style = MaterialTheme.typography.bodyMedium, color = Ak.Muted)
    }
    groups.forEach { g ->
        GlassCard(padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(categoryIcon(g.category), categoryColor(g.category))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("${g.serving} ${g.name}", style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface, maxLines = 2)
                    Text("Hacim: %${"%.1f".format(g.abv)} Alkol", style = MaterialTheme.typography.labelMedium, color = Ak.Muted)
                }
                CounterControls(
                    count = g.count,
                    onMinus = { vm.removeDrink(g.typeId) },
                    onPlus = { vm.drinkTypes.value.firstOrNull { it.id == g.typeId }?.let(vm::addDrink) },
                )
            }
        }
    }

    BigButton("Yeni Kadeh / Shot Ekle", Icons.Rounded.AddCircleOutline, onAdd, height = 72.dp)

    // Güvenli kişi
    state.safeContact?.let { safe ->
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.VerifiedUser, Ak.Tertiary, shape = RoundedCornerShape(50))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("ACİL GÜVENLİ KİŞİ", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                    Text(safe.name, style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface, maxLines = 1)
                }
                SquareIconButton(Icons.Rounded.ShareLocation, Ak.High, Ak.Primary, { scope.launch { SafeActions.sendLocation(context) } })
                Spacer(Modifier.width(8.dp))
                PillButton("Ara", Icons.Rounded.Call, Ak.Tertiary, { SafeActions.callSafeContact(context) })
            }
        }
    }

    // Eve dönüş + bitir
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.Home, Ak.Primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Eve vardın mı?", style = MaterialTheme.typography.titleSmall, color = Ak.OnSurface)
                Text(
                    if (state.drunkActive) "Kalkan açıkken seans bitmez; kilit sabah kendiliğinden açılır." else "Seansı bitir, testler dursun ve gece raporun oluşsun.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ak.OnVariant,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PillButton("Eve Rota", Icons.Rounded.Home, Ak.Secondary, { SafeActions.openRouteHome(context) }, Modifier.weight(1f))
            PillButton("Seansı Bitir", Icons.Rounded.Schedule, Ak.Primary, { confirmEnd = true }, Modifier.weight(1f))
        }
    }

    if (confirmEnd) {
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            containerColor = Ak.Container,
            title = { Text("Gece bitti mi?") },
            text = { Text(if (state.drunkActive) "Sarhoş Modu açıkken seans bitirilemez. Kilit sabah açıldığında gece kendiliğinden kapanır." else "Testler duracak ve sabah raporun hazırlanacak.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmEnd = false
                    if (!state.drunkActive) vm.endSession { if (it) onOpenTab(Routes.REPORT) }
                }) { Text(if (state.drunkActive) "Tamam" else "Bitir") }
            },
            dismissButton = { if (!state.drunkActive) TextButton(onClick = { confirmEnd = false }) { Text("Vazgeç") } },
        )
    }
}

@Composable
private fun PendingTestCard(deadline: Long, now: Long, onStart: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "urgent")
    val alpha by pulse.animateFloat(0.25f, 0.6f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "a")
    GlassCard(
        Modifier.glow(Ak.Crimson, alpha = alpha * 0.5f),
        container = Ak.Crimson.copy(alpha = 0.14f),
        border = Ak.Crimson.copy(alpha = alpha),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.Warning, Ak.Crimson)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Kontrol zamanı!", style = MaterialTheme.typography.headlineSmall, color = Ak.Tertiary)
                Text("Başlamak için ${fmtCountdown(deadline - now)} kaldı. Kaçırırsan uyarı sayılır.", style = MaterialTheme.typography.bodySmall, color = Ak.OnVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        BigButton("Teste Başla", Icons.Rounded.Psychology, onStart, color = Ak.Crimson, contentColor = Color.White, height = 56.dp)
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
    }
}

private fun advice(level: BacLevel): String = when (level) {
    BacLevel.SOBER -> "Şimdilik her şey kontrol altında. Su içmeyi unutma."
    BacLevel.LIGHT -> "Hafif etkiler başladı. Yavaşla, arada su iç."
    BacLevel.TIPSY -> "Refleksler ve derinlik algısı yavaşladı. Direksiyon başına kesinlikle geçmeyin."
    BacLevel.DRUNK -> "Karar verme yeteneğin zayıfladı. Bu gece mesaj ve arama yok."
    BacLevel.VERY_DRUNK -> "Denge ve konuşma bozuldu. Bırak, su iç ve güvenli kişine haber ver."
    BacLevel.DANGER -> "Tehlikeli seviye! İçmeyi hemen bırak ve yalnız kalma."
}
