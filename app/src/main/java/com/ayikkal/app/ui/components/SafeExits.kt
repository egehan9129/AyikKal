package com.ayikkal.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ayikkal.app.data.SafeContact
import com.ayikkal.app.system.SafeActions
import com.ayikkal.app.ui.theme.Ak
import kotlinx.coroutines.launch

/** Kilitliyken bile açık kalan çıkışlar: güvenli kişi, konum, eve dönüş ve 112. */
@Composable
fun SafeExits(safe: SafeContact?, homeAddress: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    SectionHeader(Icons.Rounded.Favorite, "İzin Verilen Güvenli Çıkışlar", Ak.Primary) {
        Text("Her zaman açık", style = MaterialTheme.typography.labelMedium, color = Ak.Primary)
    }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconBadge(Icons.Rounded.Person, Ak.Primary, size = 60.dp, shape = RoundedCornerShape(50))
                Box(
                    Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .offset((-2).dp, (-2).dp)
                        .clip(CircleShape)
                        .background(Ak.Primary)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("GÜVENLİ KİŞİ", style = MaterialTheme.typography.labelSmall, color = Ak.Primary)
                Text(safe?.name ?: "Seçilmedi", style = MaterialTheme.typography.headlineSmall, color = Ak.OnSurface, maxLines = 1)
                Text("Acil durum gardiyanı", style = MaterialTheme.typography.bodySmall, color = Ak.Muted)
            }
            PillButton("Hemen Ara", Icons.Rounded.Call, Ak.Primary, { SafeActions.callSafeContact(context) }, filled = true)
        }
        Spacer(Modifier.height(12.dp))
        GhostButton("Tek Tuşla Canlı Konum Gönder", Icons.Rounded.Navigation, { scope.launch { SafeActions.sendLocation(context) } }, enabled = safe != null)
    }

    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.LocalTaxi, Ak.Secondary, size = 44.dp, shape = RoundedCornerShape(50))
            Spacer(Modifier.width(12.dp))
            Text("Güvenli Eve Dönüş", style = MaterialTheme.typography.headlineSmall, color = Ak.OnSurface, modifier = Modifier.weight(1f))
            Tag(if (homeAddress.isBlank()) "Adres yok" else "Kayıtlı ev adresi", if (homeAddress.isBlank()) Ak.Muted else Ak.OnVariant)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RideButton("BiTaksi Çağır", "Adres panoya", Icons.Rounded.LocalTaxi, Ak.Amber, Ak.OnSecondary, Modifier.weight(1f)) { SafeActions.openBiTaksi(context) }
            RideButton("Uber ile Git", "Otomatik rota", Icons.Rounded.DirectionsCar, Ak.Lowest, Ak.OnSurface, Modifier.weight(1f)) { SafeActions.openUber(context) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RideButton("Toplu Taşıma", "Haritada eve rota", Icons.Rounded.Map, Ak.High, Ak.OnSurface, Modifier.weight(1f)) { SafeActions.openRouteHome(context) }
            RideButton("112 Acil", "Her zaman açık", Icons.Rounded.LocalHospital, Ak.Rose.copy(alpha = 0.18f), Ak.Tertiary, Modifier.weight(1f)) { SafeActions.callEmergency(context) }
        }
    }
}

@Composable
private fun RideButton(title: String, sub: String, icon: ImageVector, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(72.dp), shape = RoundedCornerShape(16.dp), color = bg, contentColor = fg) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.titleSmall)
            }
            Text(sub, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}
