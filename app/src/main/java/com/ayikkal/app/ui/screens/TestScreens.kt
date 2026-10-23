package com.ayikkal.app.ui.screens

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ayikkal.app.data.db.EventType
import com.ayikkal.app.domain.TestGenerator
import com.ayikkal.app.domain.TestMode
import com.ayikkal.app.domain.TestStep
import com.ayikkal.app.ui.AppViewModel
import com.ayikkal.app.ui.components.BigButton
import com.ayikkal.app.ui.components.GhostButton
import com.ayikkal.app.ui.components.GlassCard
import com.ayikkal.app.ui.components.IconBadge
import com.ayikkal.app.ui.components.InfoBanner
import com.ayikkal.app.ui.components.SectionHeader
import com.ayikkal.app.ui.components.Tag
import com.ayikkal.app.ui.components.glow
import com.ayikkal.app.ui.fmtCountdown
import com.ayikkal.app.ui.fmtDuration
import com.ayikkal.app.ui.fmtTime
import com.ayikkal.app.ui.rememberNow
import com.ayikkal.app.ui.theme.Ak
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.random.Random

// ============================================================================ Test sekmesi

@Composable
fun TestHubScreen(vm: AppViewModel, onStart: (TestMode) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val recent by vm.recentTests.collectAsStateWithLifecycle()
    val now = rememberNow()
    var confirmVoluntary by remember { mutableStateOf(false) }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when {
            state.drunkActive -> GlassCard(container = Ak.Crimson.copy(alpha = 0.12f), border = Ak.Crimson.copy(alpha = 0.35f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Rounded.Lock, Ak.Crimson)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Sarhoş Modu açık", style = MaterialTheme.typography.titleMedium, color = Ak.Tertiary)
                        Text("Periyodik testler durdu. Erken çıkmak için Kalkan sekmesindeki Ayılma Testi'ni kullan.", style = MaterialTheme.typography.bodySmall, color = Ak.OnVariant)
                    }
                }
            }
            state.testPending -> GlassCard(Modifier.glow(Ak.Crimson, alpha = 0.2f), container = Ak.Crimson.copy(alpha = 0.14f), border = Ak.Crimson.copy(alpha = 0.4f)) {
                Text("🚨 Zorunlu Sarhoşluk Testi", style = MaterialTheme.typography.headlineSmall, color = Ak.Tertiary)
                Spacer(Modifier.height(6.dp))
                Text("Başlamak için ${fmtCountdown(state.testDeadline - now)} kaldı.", style = MaterialTheme.typography.bodyMedium, color = Ak.OnVariant)
                Spacer(Modifier.height(14.dp))
                BigButton("Teste Başla", Icons.Rounded.Psychology, { onStart(TestMode.PERIODIC) }, color = Ak.Crimson, contentColor = Color.White)
            }
            state.sessionActive -> GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Rounded.Timer, Ak.Secondary)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("SONRAKİ ZORUNLU TEST", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                        Text(
                            if (state.nextTestAt > now) "${fmtTime(state.nextTestAt)} · ${fmtDuration(state.nextTestAt - now + 59_000)} sonra" else "Birazdan",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ak.OnSurface,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                BigButton("Hemen Test Yap", Icons.Rounded.Psychology, { confirmVoluntary = true }, height = 56.dp)
            }
            else -> GlassCard(Modifier.glow(Ak.Primary, alpha = 0.12f)) {
                Text("Ayıkken referansını oluştur", style = MaterialTheme.typography.headlineSmall, color = Ak.OnSurface)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Bir kez ayıkken deneme testi yap. Refleks süren kaydedilir; gece testlerinde senin normalinle karşılaştırılırsın. Deneme testinin cezası yok.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ak.OnVariant,
                )
                Spacer(Modifier.height(14.dp))
                BigButton("Deneme Testi", Icons.Rounded.School, { onStart(TestMode.PRACTICE) }, height = 56.dp)
            }
        }

        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.Speed, Ak.Primary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("KİŞİSEL REFLEKS REFERANSI", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                    Text(
                        if (state.baselineReactionMs > 0) "${state.baselineReactionMs} ms" else "Henüz yok",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ak.OnSurface,
                    )
                }
                if (!state.sessionActive && state.baselineReactionMs > 0) {
                    TextButton(onClick = { onStart(TestMode.PRACTICE) }) { Text("Yenile") }
                }
            }
        }

        SectionHeader(Icons.Rounded.Psychology, "Test nasıl işler?")
        GlassCard {
            HowRow(Icons.Rounded.Balance, "Denge", "Telefonu dik tut; el titremesi ve eğilim ölçülür.")
            HowRow(Icons.Rounded.Psychology, "Bilişsel matematik", "İki hızlı işlem. Tek yanlış = başarısız.")
            HowRow(Icons.Rounded.Keyboard, "Yazım", "Ekrandaki cümleyi harfi harfine yaz. Otomatik düzeltme kapalı.")
            HowRow(Icons.Rounded.TouchApp, "Refleks", "Kaçan noktaya 3 kez dokun.")
            Spacer(Modifier.height(8.dp))
            Text(
                "Bildirime 2 dakika içinde yanıt vermezsen uyarı sayılır; 2. kaçırmada kalkan açılır. Başladığın testi yarıda bırakmak başarısızlık sayılır.",
                style = MaterialTheme.typography.bodySmall,
                color = Ak.Muted,
            )
        }

        if (recent.isNotEmpty()) {
            SectionHeader(Icons.Rounded.Timer, "Son Testler")
            GlassCard {
                recent.forEach { e ->
                    val ok = e.type == EventType.TEST_PASSED
                    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline, null, tint = if (ok) Ak.Primary else Ak.Crimson, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(fmtTime(e.at), style = MaterialTheme.typography.labelLarge, color = Ak.OnSurface)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            when (e.type) {
                                EventType.TEST_PASSED -> "Geçti"
                                EventType.TEST_MISSED -> "Kaçırıldı"
                                EventType.SOBER_TEST_FAILED -> "Ayılma testi başarısız"
                                else -> "Kaldı"
                            } + if (e.detail.isNotBlank()) " · ${e.detail}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ak.Muted,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (confirmVoluntary) {
        AlertDialog(
            onDismissRequest = { confirmVoluntary = false },
            containerColor = Ak.Container,
            title = { Text("Gönüllü test bağlayıcıdır") },
            text = { Text("Başarısız olursan Sarhoş Modu açılır. Geçersen bir sonraki zorunlu test ertelenir.") },
            confirmButton = { TextButton(onClick = { confirmVoluntary = false; onStart(TestMode.VOLUNTARY) }) { Text("Başla") } },
            dismissButton = { TextButton(onClick = { confirmVoluntary = false }) { Text("Vazgeç") } },
        )
    }
}

@Composable
private fun HowRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, text: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon, Ak.Primary, size = 36.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Ak.OnSurface)
            Text(text, style = MaterialTheme.typography.bodySmall, color = Ak.Muted)
        }
    }
}

// ============================================================================ Test akışı

@Composable
fun TestRunScreen(mode: TestMode, onExit: (failedIntoLock: Boolean) -> Unit) {
    val context = LocalContext.current
    val vm: TestRunViewModel = viewModel(
        key = "test-$mode",
        factory = TestRunViewModel.Factory(context.applicationContext as Application, mode),
    )
    val s by vm.state.collectAsStateWithLifecycle()
    var confirmQuit by remember { mutableStateOf(false) }
    val binding = mode == TestMode.PERIODIC || mode == TestMode.VOLUNTARY

    BackHandler(enabled = s.result == null) { confirmQuit = true }

    val result = s.result
    if (result != null) {
        TestResultView(mode, result) {
            onExit(!result.passed && binding)
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val title = when (mode) {
            TestMode.PERIODIC -> "🚨 Zorunlu Sarhoşluk Testi"
            TestMode.VOLUNTARY -> "Gönüllü Sarhoşluk Testi"
            TestMode.PRACTICE -> "Deneme Testi"
            TestMode.SOBER_UP -> "🔓 Zorlu Ayılma Testi"
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Ak.OnSurface, modifier = Modifier.weight(1f))
            val urgent = s.remainingMs < 5_000
            Row(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (urgent) Ak.ErrorContainer else Ak.High)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Timer, null, tint = if (urgent) Color.White else Ak.Secondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(fmtCountdown(s.remainingMs + 999), style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace), color = if (urgent) Color.White else Ak.Secondary)
            }
        }
        val progress by animateFloatAsState((s.index.toFloat()) / s.steps.size, label = "p")
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Ak.Secondary,
            trackColor = Ak.High,
            drawStopIndicator = {},
        )
        if (binding) {
            InfoBanner(
                "Ayık olduğunu kanıtlamak için ${s.steps.size} adımı hatasız tamamla. Başarısız olursan Koruma Kalkanı devreye girecek!",
                Ak.Secondary,
                Icons.Rounded.Warning,
            )
        } else if (mode == TestMode.SOBER_UP) {
            InfoBanner("5 zor soru. Tek hata kilit süresini 1 saat uzatır.", Ak.Tertiary, Icons.Rounded.Warning)
        }

        val step = s.step ?: return@Column
        AnimatedContent(targetState = s.index, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "step") { idx ->
            val current = s.steps[idx]
            GlassCard(padding = PaddingValues(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Ak.Primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) { Text("${idx + 1}", color = Ak.Primary, style = MaterialTheme.typography.labelLarge) }
                    Spacer(Modifier.width(10.dp))
                    Text(current.title, style = MaterialTheme.typography.headlineSmall, color = Ak.OnSurface, modifier = Modifier.weight(1f))
                    Tag("${idx + 1}/${s.steps.size}", Ak.Secondary)
                }
                Text(current.subtitle, style = MaterialTheme.typography.bodySmall, color = Ak.Muted, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
                when (current) {
                    is TestStep.Choice -> ChoiceStepView(current, s.feedback, vm::answer)
                    is TestStep.Typing -> TypingStepView(current, onPass = vm::pass, onFail = vm::fail)
                    is TestStep.Reflex -> ReflexStepView(current, vm)
                    is TestStep.Balance -> BalanceStepView(current, onPass = vm::pass)
                    is TestStep.Memory -> MemoryStepView(current, onPass = vm::pass, onFail = vm::fail)
                }
            }
        }
        if (step !is TestStep.Typing) Spacer(Modifier.height(4.dp))
        if (binding) {
            GhostButton("Pes Ettim, Sarhoş Modunu Aç", Icons.Rounded.Lock, { confirmQuit = true }, color = Ak.Tertiary)
        } else {
            GhostButton("Testten Çık", Icons.Rounded.Close, { confirmQuit = true })
        }
        Spacer(Modifier.height(12.dp))
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            containerColor = Ak.Container,
            title = { Text(if (mode == TestMode.PRACTICE) "Denemeden çık?" else "Testten vazgeç?") },
            text = {
                Text(
                    when (mode) {
                        TestMode.PRACTICE -> "Referansın güncellenmeyecek."
                        TestMode.SOBER_UP -> "Vazgeçmek başarısız deneme sayılır ve kilit 1 saat uzar."
                        else -> "Vazgeçmek testi başarısız saymak demek. Sarhoş Modu hemen açılır."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmQuit = false
                    if (mode == TestMode.PRACTICE) onExit(false) else vm.fail("Kullanıcı vazgeçti")
                }) { Text("Evet") }
            },
            dismissButton = { TextButton(onClick = { confirmQuit = false }) { Text("Devam et") } },
        )
    }
}

@Composable
private fun TestResultView(mode: TestMode, result: TestResult, onClose: () -> Unit) {
    val ok = result.passed
    val color = if (ok) Ak.Primary else Ak.Crimson
    val (title, body) = when {
        mode == TestMode.PRACTICE && ok -> "Referans kaydedildi" to "Ortalama refleks süren ${result.avgReactionMs ?: "-"} ms. Gece testlerinde bununla karşılaştırılacaksın."
        mode == TestMode.PRACTICE -> "Deneme tamamlanamadı" to "${result.reason}. Ayıkken bir daha dene; referans ancak başarılı denemede kaydedilir."
        mode == TestMode.SOBER_UP && ok -> "Ayıldın, kilitler açıldı!" to "Gerçekten ayık olduğunu kanıtladın. Gece raporun hazır."
        mode == TestMode.SOBER_UP -> "Henüz ayık değilsin" to "${result.reason}. Kilit süresi 1 saat uzatıldı. 30 dakika sonra tekrar deneyebilirsin."
        ok -> "İyi eğlenceler!" to "Testi geçtin. Bir sonraki kontrolde görüşürüz. Arada su içmeyi unutma 💧"
        else -> "ALARM! Sarhoş Modu Aktif!" to "${result.reason}. Kilitli uygulamalar sabaha kadar kapalı. Git su iç ve uyu bro, yarın bana teşekkür edeceksin."
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(140.dp)
                .glow(color, 1.2f, 0.35f)
                .clip(CircleShape)
                .background(Ak.Container)
                .border(2.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Shield, null, tint = color, modifier = Modifier.size(72.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text(title, style = MaterialTheme.typography.displaySmall, color = color, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge, color = Ak.OnVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        BigButton(
            if (!ok && (mode == TestMode.PERIODIC || mode == TestMode.VOLUNTARY)) "Kalkanı Gör" else "Tamam",
            null,
            onClose,
            color = color,
            contentColor = if (ok) Ak.OnPrimary else Color.White,
        )
    }
}

// ---------------------------------------------------------------------------- Adımlar

@Composable
private fun ChoiceStepView(step: TestStep.Choice, feedback: Pair<Int, Boolean>?, onAnswer: (Int) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Ak.Lowest)
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(step.caption, style = MaterialTheme.typography.labelMedium, color = Ak.Muted)
        Spacer(Modifier.height(8.dp))
        Text(
            step.prompt,
            style = MaterialTheme.typography.displayMedium,
            color = step.promptColor?.let { Color(it) } ?: Ak.OnSurface,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.height(14.dp))
    step.options.chunked(2).forEachIndexed { row, pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            pair.forEachIndexed { col, option ->
                val i = row * 2 + col
                val bg = when {
                    feedback?.first == i && feedback.second -> Ak.Primary
                    feedback?.first == i -> Ak.Crimson
                    else -> Ak.High
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bg)
                        .clickable(enabled = feedback == null) { onAnswer(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(option, style = MaterialTheme.typography.headlineMedium, color = if (feedback?.first == i) Ak.Bg else Ak.OnSurface)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TypingStepView(step: TestStep.Typing, onPass: () -> Unit, onFail: (String) -> Unit) {
    var text by remember(step) { mutableStateOf("") }
    var errors by remember(step) { mutableIntStateOf(0) }
    var lastError by remember(step) { mutableStateOf<String?>(null) }
    val target = step.sentence
    val mismatch = TestGenerator.firstMismatch(text, target)
    val correctPrefix = mismatch ?: text.length
    val accuracy = (correctPrefix * 100 / target.length).coerceIn(0, 100)
    val focus = remember(step) { FocusRequester() }
    LaunchedEffect(step) { focus.requestFocus() }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Ak.Lowest)
            .padding(16.dp)
    ) {
        Text("Aşağıdaki cümleyi harfi harfine yaz:", style = MaterialTheme.typography.labelMedium, color = Ak.Muted)
        Spacer(Modifier.height(6.dp))
        Text(target, style = MaterialTheme.typography.titleLarge, color = Ak.OnSurface)
    }
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = text,
        onValueChange = { new ->
            // Yapıştırma ve klavye önerisiyle kelime eklemeyi engelle: her seferde en fazla 1 harf.
            if (new.length - text.length > 1) return@OutlinedTextField
            val grew = new.length > text.length
            val mm = TestGenerator.firstMismatch(new, target)
            // Hata, doğru giden metne yanlış harf eklendiği an sayılır; düzeltene kadar yeni hata sayılmaz.
            if (grew && mm != null && TestGenerator.firstMismatch(text, target) == null) {
                errors++
                val typedChar = new.getOrNull(mm)
                val expected = target.getOrNull(mm)
                lastError = if (expected == null) "Cümle bitti, fazladan karakter yazdın" else "'$typedChar' harfi yanlış (Doğrusu: '$expected')"
                if (errors > step.allowedErrors) {
                    onFail("Yazım hatası: $lastError")
                    return@OutlinedTextField
                }
            }
            text = new
            if (TestGenerator.normalizeTyping(new) == TestGenerator.normalizeTyping(target)) onPass()
        },
        modifier = Modifier.fillMaxWidth().focusRequester(focus),
        placeholder = { Text("Buraya yaz…", color = Ak.Muted) },
        textStyle = MaterialTheme.typography.titleMedium,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text,
        ),
        shape = RoundedCornerShape(16.dp),
        trailingIcon = { Tag("Doğruluk: %$accuracy", if (mismatch == null) Ak.Primary else Ak.Crimson, Modifier.padding(end = 8.dp)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Ak.Lowest,
            unfocusedContainerColor = Ak.Lowest,
            focusedBorderColor = if (mismatch == null) Ak.Primary else Ak.Crimson,
            unfocusedBorderColor = Ak.Hairline,
        ),
    )
    Spacer(Modifier.height(10.dp))
    if (mismatch != null && lastError != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.ErrorOutline, null, tint = Ak.Crimson, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Hata algılandı: $lastError — silip düzelt", style = MaterialTheme.typography.labelMedium, color = Ak.Crimson)
        }
        Spacer(Modifier.height(8.dp))
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ak.Lowest)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Keyboard, null, tint = Ak.Muted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Kalan hata hakkı: ${(step.allowedErrors - errors).coerceAtLeast(0)}",
            style = MaterialTheme.typography.labelMedium,
            color = Ak.OnSurface,
            modifier = Modifier.weight(1f),
        )
        Text("Süre bitmeden bitir!", style = MaterialTheme.typography.labelMedium, color = Ak.Secondary)
    }
}

@Composable
private fun ReflexStepView(step: TestStep.Reflex, vm: TestRunViewModel) {
    val density = LocalDensity.current
    var hits by remember(step) { mutableIntStateOf(0) }
    var misses by remember(step) { mutableIntStateOf(0) }
    var dot by remember(step) { mutableStateOf(Offset(0.5f, 0.5f)) }
    var shownAt by remember(step) { mutableLongStateOf(System.currentTimeMillis()) }
    var moveTick by remember(step) { mutableIntStateOf(0) }
    val dotSize = 64.dp

    LaunchedEffect(step, moveTick) {
        delay(step.moveIntervalMs)
        dot = Offset(Random.nextFloat(), Random.nextFloat())
        shownAt = System.currentTimeMillis()
        moveTick++
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Tag("İsabet: $hits/${step.hitsNeeded}", Ak.Primary)
        Tag("Iska: $misses/${step.maxMisses}", if (misses > 0) Ak.Crimson else Ak.Muted)
    }
    Spacer(Modifier.height(12.dp))
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Ak.Lowest)
            .border(1.dp, Ak.Hairline, RoundedCornerShape(18.dp))
    ) {
        val wPx = with(density) { (maxWidth - dotSize).toPx() }
        val hPx = with(density) { (maxHeight - dotSize).toPx() }
        val dotPx = with(density) { dotSize.toPx() }
        val center = Offset(dot.x * wPx + dotPx / 2, dot.y * hPx + dotPx / 2)
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(step) {
                    detectTapGestures { p ->
                        val c = Offset(dot.x * wPx + dotPx / 2, dot.y * hPx + dotPx / 2)
                        if ((p - c).getDistance() <= dotPx * 0.75f) {
                            vm.addReaction(System.currentTimeMillis() - shownAt)
                            hits++
                            if (hits >= step.hitsNeeded) {
                                if (vm.reflexTooSlow()) vm.fail("Refleksler normalinden çok yavaş (${vm.averageReaction}ms)") else vm.pass()
                            } else {
                                dot = Offset(Random.nextFloat(), Random.nextFloat())
                                shownAt = System.currentTimeMillis()
                                moveTick++
                            }
                        } else {
                            misses++
                            if (misses > step.maxMisses) vm.fail("Refleks testinde çok fazla ıska")
                        }
                    }
                }
        )
        Box(
            Modifier
                .offset { IntOffset((center.x - dotPx / 2).toInt(), (center.y - dotPx / 2).toInt()) }
                .size(dotSize)
                .glow(Ak.Primary, 1.1f, 0.5f)
                .clip(CircleShape)
                .background(Ak.Primary)
                .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )
    }
}

@Composable
private fun BalanceStepView(step: TestStep.Balance, onPass: () -> Unit) {
    val context = LocalContext.current
    var roll by remember { mutableFloatStateOf(0f) }
    var jitter by remember { mutableFloatStateOf(0f) }
    var steadyMs by remember { mutableLongStateOf(0L) }
    var noSensor by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    DisposableEffect(step) {
        val sm = context.getSystemService(SensorManager::class.java)
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            noSensor = true
            onDispose { }
        } else {
            var smooth = 0f
            var last = 0L
            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
                    val raw = Math.toDegrees(atan2(e.values[0].toDouble(), e.values[1].toDouble())).toFloat()
                    smooth = if (last == 0L) raw else smooth * 0.85f + raw * 0.15f
                    jitter = jitter * 0.9f + abs(raw - smooth) * 0.1f
                    roll = smooth
                    val now = System.currentTimeMillis()
                    val dt = if (last == 0L) 0 else now - last
                    last = now
                    val steady = abs(smooth) <= step.maxTiltDeg && jitter < 2.2f
                    steadyMs = if (steady) steadyMs + dt else (steadyMs - dt * 2).coerceAtLeast(0)
                    if (!done && steadyMs >= step.holdSec * 1000L) {
                        done = true
                        onPass()
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            onDispose { sm.unregisterListener(listener) }
        }
    }
    LaunchedEffect(noSensor) {
        if (noSensor) {
            delay(1_000)
            onPass()
        }
    }

    val stable = abs(roll) <= step.maxTiltDeg && jitter < 2.2f
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("EL TİTREME & DENGE ÖLÇÜMÜ", style = MaterialTheme.typography.labelMedium, color = Ak.OnSurface, modifier = Modifier.weight(1f))
        Tag(if (noSensor) "Sensör yok" else if (stable) "● Normal" else "● Dengesiz", if (stable) Ak.Primary else Ak.Secondary)
    }
    Spacer(Modifier.height(12.dp))
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Ak.Lowest)
    ) {
        val zone = size.width * (step.maxTiltDeg / 60f)
        drawRoundRect(
            Ak.Primary.copy(alpha = 0.12f),
            topLeft = Offset(size.width / 2 - zone, 8f),
            size = androidx.compose.ui.geometry.Size(zone * 2, size.height - 16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
        )
        val x = size.width / 2 + (roll.coerceIn(-30f, 30f) / 30f) * (size.width / 2 - size.height / 2)
        drawCircle(if (stable) Ak.Primary else Ak.Secondary, size.height / 2 - 10f, Offset(x, size.height / 2))
        drawCircle(Color.White, 8f, Offset(x, size.height / 2))
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Sol eğilim (${"%.0f".format(roll.coerceAtMost(0f))}°)", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
        Text("Titreme: ${"%.1f".format(jitter)}", style = MaterialTheme.typography.labelSmall, color = Ak.Primary)
        Text("Sağ eğilim", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
    }
    Spacer(Modifier.height(14.dp))
    LinearProgressIndicator(
        progress = { (steadyMs / (step.holdSec * 1000f)).coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape),
        color = Ak.Primary,
        trackColor = Ak.High,
        drawStopIndicator = {},
    )
    Spacer(Modifier.height(6.dp))
    Text("Telefonu dik tut ve noktayı yeşil alanda ${step.holdSec} saniye sabit tut.", style = MaterialTheme.typography.bodySmall, color = Ak.Muted)
}

@Composable
private fun MemoryStepView(step: TestStep.Memory, onPass: () -> Unit, onFail: (String) -> Unit) {
    var showing by remember(step) { mutableStateOf(true) }
    var input by remember(step) { mutableStateOf("") }
    LaunchedEffect(step) {
        delay(step.showMs)
        showing = false
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Ak.Lowest)
            .padding(vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(if (showing) "BU SAYIYI AKLINDA TUT" else "SAYIYI YAZ", style = MaterialTheme.typography.labelMedium, color = Ak.Muted)
        Spacer(Modifier.height(8.dp))
        Text(
            if (showing) step.digits.chunked(3).joinToString(" ") else "• • •  • • •",
            style = MaterialTheme.typography.displayMedium.copy(letterSpacing = 4.sp),
            color = if (showing) Ak.Secondary else Ak.Highest,
            fontWeight = FontWeight.ExtraBold,
        )
    }
    if (!showing) {
        Spacer(Modifier.height(12.dp))
        Field(input, { input = it.filter(Char::isDigit).take(step.digits.length) }, "Sayı", KeyboardType.NumberPassword)
        Spacer(Modifier.height(12.dp))
        BigButton("Onayla", Icons.Rounded.CheckCircle, {
            if (input == step.digits) onPass() else onFail("Hafıza: ${step.digits} yerine $input yazdın")
        }, enabled = input.length == step.digits.length, height = 56.dp)
    }
}
