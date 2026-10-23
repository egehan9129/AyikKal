package com.ayikkal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsBar
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ayikkal.app.ui.components.BigButton
import com.ayikkal.app.ui.components.GlassCard
import com.ayikkal.app.ui.components.IconBadge
import com.ayikkal.app.ui.components.glow
import com.ayikkal.app.ui.theme.Ak
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pager = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    var adult by remember { mutableStateOf(false) }
    var understood by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        HorizontalPager(pager, Modifier.weight(1f), userScrollEnabled = true) { page ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (page) {
                    0 -> Welcome()
                    1 -> HowItWorks()
                    else -> Disclaimer(adult, { adult = it }, understood, { understood = it })
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.Center) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(if (pager.currentPage == i) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(if (pager.currentPage == i) Ak.Primary else Ak.Highest)
                )
            }
        }
        Box(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            if (pager.currentPage < 2) {
                BigButton("Devam", null, { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } })
            } else {
                BigButton("Güvenlik Duvarını Kur", Icons.Rounded.Shield, onDone, enabled = adult && understood)
            }
        }
    }
}

@Composable
private fun Welcome() {
    Box(
        Modifier
            .size(160.dp)
            .glow(Ak.Primary, 1.2f, 0.35f)
            .clip(CircleShape)
            .background(Ak.Container)
            .border(2.dp, Ak.Primary.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Rounded.Shield, null, tint = Ak.Primary, modifier = Modifier.size(84.dp)) }
    Spacer(Modifier.height(32.dp))
    Text("AyıkKal", style = MaterialTheme.typography.displayLarge, color = Ak.OnSurface)
    Spacer(Modifier.height(12.dp))
    Text(
        "Sarhoş halin, ayık halinin kararlarını bozamasın.",
        style = MaterialTheme.typography.headlineSmall,
        color = Ak.Primary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    Text(
        "Eski sevgiliyi aramak, gece 3'te hikâye atmak, patrona \"aslında…\" diye başlayan mesajlar. Hepsini sabaha erteliyoruz.",
        style = MaterialTheme.typography.bodyLarge,
        color = Ak.OnVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun HowItWorks() {
    Text("Nasıl çalışır?", style = MaterialTheme.typography.displaySmall, color = Ak.OnSurface)
    Spacer(Modifier.height(24.dp))
    Step(Icons.Rounded.Shield, Ak.Primary, "1. Ayıkken kur", "Kara liste, kilitlenecek uygulamalar ve güvenli kişini seç.")
    Step(Icons.Rounded.SportsBar, Ak.Secondary, "2. İçmeye başla", "Her kadehi ekle; promilini canlı tahmin edelim.")
    Step(Icons.Rounded.Psychology, Ak.Orange, "3. Testleri geç", "Belirli aralıklarla matematik, yazım, refleks ve denge testi gelir.")
    Step(Icons.Rounded.Lock, Ak.Crimson, "4. Kalkan devreye girer", "Testi geçemezsen kilitli uygulamalar ve yasaklı aramalar sabaha kadar kapanır.")
    Step(Icons.Rounded.WbSunny, Ak.Secondary, "5. Sabah raporu", "Dün gece ne içtin, neyi engelledik; hepsi kara kutuda.")
}

@Composable
private fun Step(icon: ImageVector, color: androidx.compose.ui.graphics.Color, title: String, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon, color)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = Ak.OnVariant)
        }
    }
}

@Composable
private fun Disclaimer(adult: Boolean, onAdult: (Boolean) -> Unit, understood: Boolean, onUnderstood: (Boolean) -> Unit) {
    Text("Önce dürüst olalım", style = MaterialTheme.typography.displaySmall, color = Ak.OnSurface, textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))
    GlassCard {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.DirectionsCar, null, tint = Ak.Secondary)
            Spacer(Modifier.width(12.dp))
            Text(
                "Promil değeri boy, kilo ve içtiklerine dayalı bir TAHMİNDİR; alkolmetre değildir. Araç kullanıp kullanmamaya asla bu uygulamaya bakarak karar verme. İçtiysen direksiyona geçme.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ak.OnSurface,
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.LocalHospital, null, tint = Ak.Crimson)
            Spacer(Modifier.width(12.dp))
            Text(
                "AyıkKal tıbbi bir cihaz değildir. Bilinç bulanıklığı, kusma veya yavaş nefes alkol zehirlenmesi belirtisi olabilir: 112'yi ara. 112 her zaman aranabilir.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ak.OnSurface,
            )
        }
    }
    Spacer(Modifier.height(20.dp))
    CheckRow("18 yaşından büyüğüm", adult, onAdult)
    CheckRow("Okudum, anladım", understood, onUnderstood)
}

@Composable
private fun CheckRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (checked) Ak.Primary else Ak.Lowest)
                .border(3.dp, if (checked) Ak.Primary else Ak.Highest, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) { if (checked) Icon(Icons.Rounded.Check, null, tint = Ak.OnPrimary, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Ak.OnSurface)
    }
}
