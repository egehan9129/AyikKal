package com.ayikkal.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.BreakfastDining
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.PhoneMissed
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsBar
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayikkal.app.data.db.DrinkCategory
import com.ayikkal.app.data.db.EventType
import com.ayikkal.app.data.db.GuardEvent
import com.ayikkal.app.domain.BacCalculator
import com.ayikkal.app.system.SafeActions
import com.ayikkal.app.ui.AppViewModel
import com.ayikkal.app.ui.NightReport
import com.ayikkal.app.ui.components.BigButton
import com.ayikkal.app.ui.components.GhostButton
import com.ayikkal.app.ui.components.GlassCard
import com.ayikkal.app.ui.components.IconBadge
import com.ayikkal.app.ui.components.SectionHeader
import com.ayikkal.app.ui.components.Tag
import com.ayikkal.app.ui.components.glow
import com.ayikkal.app.ui.components.levelColor
import com.ayikkal.app.domain.BacLevel
import com.ayikkal.app.ui.fmtBac
import com.ayikkal.app.ui.fmtTime
import com.ayikkal.app.ui.theme.Ak
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Crisis(val icon: ImageVector, val color: Color, val title: String, val badge: String, val text: String, val foot: String)

@Composable
fun ReportScreen(vm: AppViewModel) {
    val sessions by vm.endedSessions.collectAsStateWithLifecycle()
    val selected by vm.selectedReportId.collectAsStateWithLifecycle()
    val report by vm.report.collectAsStateWithLifecycle()

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (sessions.isEmpty()) {
            GlassCard(padding = PaddingValues(28.dp)) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    IconBadge(Icons.Rounded.NightsStay, Ak.Secondary, size = 72.dp, shape = RoundedCornerShape(50))
                    Spacer(Modifier.height(16.dp))
                    Text("Henüz rapor yok", style = MaterialTheme.typography.headlineMedium, color = Ak.OnSurface)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "İlk geceni bitirdiğinde (ya da kilitler sabah açıldığında) burada gecenin kara kutusu çıkacak.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ak.OnVariant,
                    )
                }
            }
            return@Column
        }

        if (sessions.size > 1) {
            val dateFmt = remember { SimpleDateFormat("d MMM EEE", Locale("tr", "TR")) }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sessions.forEachIndexed { i, s ->
                    val isSel = (selected ?: sessions.first().id) == s.id
                    Text(
                        dateFmt.format(Date(s.startedAt)),
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) Ak.Secondary else Ak.Container)
                            .clickable { vm.selectedReportId.value = if (i == 0) null else s.id }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSel) Ak.Bg else Ak.OnSurface,
                    )
                }
            }
        }

        report?.let { ReportBody(it, vm) }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ReportBody(r: NightReport, vm: AppViewModel) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    val ev = r.events
    val drunkOn = ev.firstOrNull { it.type == EventType.DRUNK_MODE_ON }
    val unlocked = ev.lastOrNull { it.type == EventType.UNLOCKED }
    val crises = buildCrises(ev)
    val prevented = ev.count { it.type in setOf(EventType.APP_BLOCKED, EventType.CALL_BLOCKED, EventType.INCOMING_BLOCKED, EventType.SETTINGS_BLOCKED) }
    val water = ev.count { it.type == EventType.WATER }
    val passed = ev.count { it.type == EventType.TEST_PASSED }
    val testsTotal = passed + ev.count { it.type == EventType.TEST_FAILED || it.type == EventType.TEST_MISSED }
    val reactions = ev.mapNotNull { Regex("refleks (\\d+)ms").find(it.detail)?.groupValues?.get(1)?.toIntOrNull() }
    val drinks = r.entries.size
    val peak = r.curve.maxByOrNull { it.bac }
    val peakBac = peak?.bac ?: r.session.peakBac

    // ------------------------------------------------------------- Başlık
    GlassCard(Modifier.glow(if (prevented > 0) Ak.Secondary else Ak.Primary, 0.9f, 0.16f), padding = PaddingValues(22.dp)) {
        if (unlocked != null) Tag("☀️ GÜNAYDIN! KİLİTLER AÇILDI (${fmtTime(unlocked.at)})", Ak.Secondary)
        else Tag("🌙 ${fmtTime(r.session.startedAt)} – ${r.session.endedAt?.let(::fmtTime) ?: "…"}", Ak.Secondary)
        Spacer(Modifier.height(12.dp))
        Text(
            when {
                prevented > 0 -> "Dün Gece Büyük Bir Faciadan Kurtuldun!"
                drunkOn != null -> "Kalkan seni sabaha kadar korudu"
                else -> "Kontrollü bir geceydi 👏"
            },
            style = MaterialTheme.typography.displaySmall,
            color = Ak.OnSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (prevented > 0) "Sabah bize teşekkür edeceğini söylemiştik. İşte dün gecenin kara kutusu ve kurtarılan itibarın:"
            else "İşte dün gecenin özeti. Su içmeyi ve dinlenmeyi unutma.",
            style = MaterialTheme.typography.bodyMedium,
            color = Ak.OnVariant,
        )
        if (prevented > 0) {
            Spacer(Modifier.height(12.dp))
            Tag("🛡️ $prevented Tehlikeli Hamle Başarıyla Engellendi", Ak.Tertiary)
        }
    }

    // ------------------------------------------------------------- Krizler
    if (crises.isNotEmpty()) {
        SectionHeader(Icons.Rounded.PhoneDisabled, "Engellenen Krizler", Ak.Crimson) { Tag("${crises.size}/${crises.size} Zafer", Ak.Crimson) }
        crises.forEach { c ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Ak.Container)
            ) {
                Box(Modifier.width(5.dp).fillMaxHeight().background(c.color))
                Row(Modifier.padding(14.dp)) {
                    IconBadge(c.icon, c.color, size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.title, style = MaterialTheme.typography.titleSmall, color = Ak.OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Tag(c.badge, c.color)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(c.text, style = MaterialTheme.typography.bodyMedium, color = Ak.OnSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(c.foot, style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------- Karne
    SectionHeader(Icons.Rounded.Speed, "Tüketim & Gece Karnesi", Ak.Primary)
    val byCategory = r.entries.groupBy { it.category }.toList().sortedByDescending { it.second.size }
    val grams = r.entries.sumOf { BacCalculator.grams(it.volumeMl, it.abv) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            "Toplam Alkol", Icons.Rounded.SportsBar, Ak.Secondary, Modifier.weight(1f),
            value = byCategory.firstOrNull()?.let { "${it.second.size} ${it.first.label}" } ?: "0",
            sub = byCategory.drop(1).joinToString { "+${it.second.size} ${it.first.label}" }.ifEmpty { "Tek tür" },
            foot = "≈ ${"%.0f".format(grams)} g saf alkol",
        )
        StatCard(
            "Zirve Promil", Icons.Rounded.TrendingUp, Ak.TertiaryContainer, Modifier.weight(1f),
            value = "${fmtBac(peakBac)} ‰",
            sub = peak?.let { "Zirve: ${fmtTime(it.at)}" } ?: "",
            foot = if (peakBac >= BacCalculator.LEGAL_DRIVING_LIMIT) "Yasal sınırın üstü" else "Yasal sınırın altı",
            valueColor = levelColor(BacLevel.of(peakBac)),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            "Bilişsel Test", Icons.Rounded.Psychology, Ak.Secondary, Modifier.weight(1f),
            value = "$passed / $testsTotal Geçti",
            sub = if (reactions.isNotEmpty()) "Refleks: ${reactions.average().toInt()}ms" else "Refleks ölçülmedi",
            foot = if (state.baselineReactionMs > 0 && reactions.isNotEmpty()) "Normalin: ${state.baselineReactionMs}ms" else "Referans için deneme testi yap",
        )
        val needed = drinks / 2
        val score = if (needed == 0) 100 else (water * 100 / needed).coerceAtMost(100)
        StatCard(
            "İçilen Su", Icons.Rounded.WaterDrop, Ak.Primary, Modifier.weight(1f),
            value = "$water Bardak",
            sub = "Su skoru: %$score",
            foot = if (score >= 80) "Dehidrasyon önlendi" else "Daha fazla su gerekti",
            valueColor = Ak.Primary,
        )
    }

    // ------------------------------------------------------------- Eğri
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Gece Promil Eğrisi & Olaylar", style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface, modifier = Modifier.weight(1f))
            Text("${fmtTime(r.session.startedAt)} – ${r.session.endedAt?.let(::fmtTime) ?: ""}", style = MaterialTheme.typography.labelMedium, color = Ak.Muted)
        }
        Spacer(Modifier.height(14.dp))
        BacChart(r)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${fmtTime(r.session.startedAt)} Başlangıç", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
            peak?.let { Text("${fmtTime(it.at)} Tepe", style = MaterialTheme.typography.labelSmall, color = Ak.Secondary) }
            Text("${r.curve.lastOrNull()?.let { fmtTime(it.at) } ?: ""} Ayılma", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
        }
    }

    // ------------------------------------------------------------- Toparlanma
    SectionHeader(Icons.Rounded.WbSunny, "Hangover & Toparlanma Rehberi", Ak.Secondary)
    val risk = ((peakBac * 35) + ((drinks / 2.0 - water).coerceAtLeast(0.0) * 8)).toInt().coerceIn(5, 95)
    val severity = when {
        risk < 35 -> "Hafif" to Ak.Primary
        risk < 65 -> "Orta Şiddet" to Ak.Secondary
        else -> "Ağır" to Ak.Crimson
    }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bugün nasıl hissedeceksin?", style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface, modifier = Modifier.weight(1f))
            Tag(severity.first, severity.second)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Ak.Primary, Ak.Secondary, Ak.Crimson)))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(risk / 100f)
                    .height(10.dp)
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Hafif sersemlik", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
            Text("Baş ağrısı riski %$risk", style = MaterialTheme.typography.labelSmall, color = severity.second)
            Text("Ağır hangover", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
        }
    }
    val done = remember(r.session.id) { mutableStateListOf<Int>() }
    val tips = listOf(
        Triple(Icons.Rounded.LocalDrink, "Büyük bardak elektrolit / maden suyu", "Sodyum ve magnezyum dengesini onarır"),
        Triple(Icons.Rounded.BreakfastDining, "Yağsız, yumurtalı kahvaltı yap", "Sistein, asetaldehit yıkımına yardım eder"),
        Triple(Icons.Rounded.Coffee, "Kafeini 2 saat ertele", "Mide asidini korumak için önce ılık su"),
        Triple(Icons.Rounded.Medication, "Ağrı kesiciyi aç karnına alma", "Alkol sonrası ilaç seçiminde emin değilsen eczacına sor"),
    )
    tips.forEachIndexed { i, (icon, title, sub) ->
        val checked = i in done
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Ak.Container)
                .clickable { if (checked) done.remove(i) else done.add(i) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(icon, listOf(Ak.Primary, Ak.Secondary, Ak.Tertiary, Ak.Orange)[i], size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = if (checked) Ak.Muted else Ak.OnSurface)
                Text(sub, style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
            }
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (checked) Ak.Primary else Ak.High)
                    .border(1.dp, Ak.Hairline, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Check, null, tint = if (checked) Ak.Bg else Ak.Muted, modifier = Modifier.size(18.dp)) }
        }
    }

    // ------------------------------------------------------------- Eylemler
    BigButton("Raporu Paylaş (Anonim Kriz Karnesi)", Icons.Rounded.Share, {
        val text = buildString {
            append("🍻 AyıkKal gece karnem\n")
            append("• $drinks kadeh, zirve ${fmtBac(peakBac)}‰\n")
            append("• $passed/$testsTotal test geçti, $water bardak su\n")
            if (prevented > 0) append("• 🛡️ $prevented pişmanlık engellendi\n")
            append("Sarhoş halin, ayık halinin kararlarını bozamasın.")
        }
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Paylaş"))
    }, height = 60.dp)
    state.safeContact?.let { safe ->
        GhostButton("Güvenli Kişiye (${safe.name}) \"İyiyim\" Bildirimi At", Icons.Rounded.Send, { SafeActions.sendImOk(context) }, color = Ak.Primary)
    }
    if (!r.session.reportSeen) {
        GhostButton("Geceyi Arşivle", Icons.Rounded.Archive, { vm.markReportSeen(r.session.id) }, color = Ak.Muted)
    }
}

private fun buildCrises(events: List<GuardEvent>): List<Crisis> {
    val out = mutableListOf<Crisis>()
    fun times(list: List<GuardEvent>) = list.joinToString(" ve ") { fmtTime(it.at) }

    events.filter { it.type == EventType.CALL_BLOCKED }.groupBy { it.detail.substringBefore('|') }.forEach { (name, list) ->
        out += Crisis(Icons.Rounded.PhoneDisabled, Ak.Crimson, name, "✨ Facia Önlendi", "Gece ${times(list)}'de ${list.size} kez aranmaya çalışıldı. AyıkKal aramayı kesti!", "Arama engeli devredeydi • Karşı tarafa bildirim gitmedi")
    }
    events.filter { it.type == EventType.INCOMING_BLOCKED }.groupBy { it.detail.substringBefore('|') }.forEach { (name, list) ->
        out += Crisis(Icons.Rounded.PhoneMissed, Ak.Orange, name, "🔕 Sessize Alındı", "Seni ${times(list)}'de ${list.size} kez aradı. Çağrı sessizce reddedildi.", "Gelen arama filtresi devredeydi")
    }
    events.filter { it.type == EventType.APP_BLOCKED }.groupBy { it.detail.substringAfter('|') }.forEach { (label, list) ->
        out += Crisis(Icons.Rounded.Apps, Ak.Primary, label, "🔒 Kilit Devredeydi", "Gece ${fmtTime(list.first().at)}'te başlayarak ${list.size} kez açılmaya çalışıldı.", "${list.size} başarısız deneme kaydedildi")
    }
    events.filter { it.type == EventType.SETTINGS_BLOCKED }.takeIf { it.isNotEmpty() }?.let { list ->
        out += Crisis(Icons.Rounded.Settings, Ak.Secondary, "AyıkKal'ı kapatma girişimi", "😏 Yakalandın", "${list.size} kez kalkanı ayarlardan kapatmaya çalıştın.", "Kaçış koruması devredeydi")
    }
    return out
}

@Composable
private fun StatCard(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier,
    value: String,
    sub: String,
    foot: String,
    valueColor: Color = Ak.OnSurface,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Ak.Container)
            .border(1.dp, Ak.Hairline, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Ak.Muted, modifier = Modifier.weight(1f))
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = valueColor, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(sub, style = MaterialTheme.typography.bodySmall, color = Ak.OnVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Text("• $foot", style = MaterialTheme.typography.labelSmall, color = Ak.Muted, maxLines = 2)
    }
}

@Composable
private fun BacChart(r: NightReport) {
    val curve = r.curve
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        if (curve.size < 2) return@Canvas
        val start = curve.first().at
        val end = maxOf(curve.last().at, start + 1)
        val maxBac = maxOf(curve.maxOf { it.bac }, 0.6) * 1.15
        fun x(t: Long) = ((t - start).toFloat() / (end - start)) * size.width
        fun y(b: Double) = size.height - (b / maxBac).toFloat() * size.height

        // Yasal sınır çizgisi
        val ly = y(BacCalculator.LEGAL_DRIVING_LIMIT)
        drawLine(Ak.Crimson.copy(alpha = 0.5f), Offset(0f, ly), Offset(size.width, ly), 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))

        val line = Path()
        curve.forEachIndexed { i, p -> if (i == 0) line.moveTo(x(p.at), y(p.bac)) else line.lineTo(x(p.at), y(p.bac)) }
        val fill = Path().apply {
            addPath(line)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(Ak.Secondary.copy(alpha = 0.35f), Color.Transparent)))
        drawPath(line, Brush.horizontalGradient(listOf(Ak.Primary, Ak.Secondary, Ak.Orange)), style = Stroke(5f, cap = StrokeCap.Round))

        // Kadeh işaretleri
        r.entries.forEach { e ->
            drawCircle(Ak.OnVariant.copy(alpha = 0.6f), 5f, Offset(x(e.consumedAt), size.height - 4f))
        }
        // Engellenen girişimler
        r.events.filter { it.type == EventType.APP_BLOCKED || it.type == EventType.CALL_BLOCKED }.forEach { e ->
            if (e.at in start..end) drawCircle(Ak.Crimson, 6f, Offset(x(e.at), y(curve.minByOrNull { kotlin.math.abs(it.at - e.at) }?.bac ?: 0.0)))
        }
        curve.maxByOrNull { it.bac }?.let { p ->
            drawCircle(Ak.TertiaryContainer, 9f, Offset(x(p.at), y(p.bac)))
            drawCircle(Color.White, 4f, Offset(x(p.at), y(p.bac)))
        }
    }
}
