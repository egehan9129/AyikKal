package com.ayikkal.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockPerson
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.PhoneDisabled
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayikkal.app.data.SafeContact
import com.ayikkal.app.domain.BacCalculator
import com.ayikkal.app.domain.PhoneNumbers
import com.ayikkal.app.domain.UserProfile
import com.ayikkal.app.system.AppCatalog
import com.ayikkal.app.system.InstalledApp
import com.ayikkal.app.system.Permissions
import com.ayikkal.app.ui.AppViewModel
import com.ayikkal.app.ui.components.BigButton
import com.ayikkal.app.ui.components.GhostButton
import com.ayikkal.app.ui.components.GlassCard
import com.ayikkal.app.ui.components.IconBadge
import com.ayikkal.app.ui.components.InfoBanner
import com.ayikkal.app.ui.components.SectionHeader
import com.ayikkal.app.ui.components.SquareIconButton
import com.ayikkal.app.ui.components.Tag
import com.ayikkal.app.ui.components.ToggleRow
import com.ayikkal.app.ui.components.glow
import com.ayikkal.app.ui.rememberResumeKey
import com.ayikkal.app.ui.theme.Ak
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(vm: AppViewModel, onOpenPermissions: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    val contacts by vm.blockedContacts.collectAsStateWithLifecycle()
    val apps by vm.lockedApps.collectAsStateWithLifecycle()
    val locked = state.drunkActive
    var showAppPicker by remember { mutableStateOf(false) }
    var showManualContact by remember { mutableStateOf(false) }
    val resume = rememberResumeKey()
    val perms = remember(resume) { Permissions.all(context) }

    val pickBlocked = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) readPickedPhone(context, res.data?.data)?.let { (n, num) -> vm.addBlockedContact(n, num) }
    }
    val pickSafe = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) readPickedPhone(context, res.data?.data)?.let { (n, num) -> vm.setSafeContact(SafeContact(n, num)) }
    }
    val phonePicker = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GlassCard(Modifier.glow(Ak.Primary, 0.9f, 0.14f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.Shield, Ak.Primary, shape = RoundedCornerShape(50))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Güvenlik Duvarı & Ayarlar", style = MaterialTheme.typography.headlineSmall, color = Ak.OnSurface)
                    Text("Dışarı çıkıp içmeye başlamadan önce savunma hattını kur.", style = MaterialTheme.typography.bodyMedium, color = Ak.OnVariant)
                }
            }
        }
        if (locked) {
            InfoBanner("Sarhoş Modu açık: kısıtlama ekleyebilirsin ama kaldıramaz, gevşetemezsin. Sabah her şey açılır.", Ak.Crimson, Icons.Rounded.Lock)
        }

        // ------------------------------------------------------------ Kara liste
        SectionHeader(Icons.Rounded.PersonOff, "Kara Liste (Yasaklı Kişiler)", Ak.Secondary) {
            Tag("${contacts.size} Aktif", Ak.Secondary)
        }
        GlassCard {
            Text("Sarhoş Modu'nda bu kişilere giden aramalar anında kesilir, gelen aramaları sessizce reddedilir.", style = MaterialTheme.typography.bodyMedium, color = Ak.OnVariant)
            Spacer(Modifier.height(12.dp))
            contacts.forEach { c ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Ak.High)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Block, null, tint = Ak.Muted)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ak.OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(PhoneNumbers.mask(c.number), style = MaterialTheme.typography.labelMedium, color = Ak.Muted)
                    }
                    if (!locked) {
                        TextButton(onClick = { vm.removeBlockedContact(c) }) {
                            Text("Kaldır", color = Ak.OnVariant)
                            Icon(Icons.Rounded.Close, null, tint = Ak.OnVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton("Rehberden Ekle", Icons.Rounded.PersonAdd, { pickBlocked.launch(phonePicker) }, Modifier.weight(1f), color = Ak.Primary)
                GhostButton("Elle Gir", Icons.Rounded.Add, { showManualContact = true }, Modifier.weight(0.7f))
            }
        }

        // ------------------------------------------------------------ Uygulamalar
        SectionHeader(Icons.Rounded.LockPerson, "Kilitlenecek Uygulamalar", Ak.Primary) {
            Tag("${apps.count { it.enabled }} Devrede", Ak.Primary)
        }
        GlassCard {
            Text("Sarhoş Modu'nda bu uygulamalar açıldığı an engel ekranı çıkar. Telefon uygulamasını eklersen yalnızca güvenli kişi ve 112 aranabilir.", style = MaterialTheme.typography.bodyMedium, color = Ak.OnVariant)
            Spacer(Modifier.height(12.dp))
            apps.forEach { app ->
                val icon = remember(app.packageName) { AppCatalog.icon(context, app.packageName)?.toBitmap(72, 72)?.asImageBitmap() }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Ak.High.copy(alpha = if (app.enabled) 1f else 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) Image(icon, null, Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
                    else IconBadge(Icons.Rounded.Apps, Ak.OnVariant, size = 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.label, style = MaterialTheme.typography.titleMedium, color = if (app.enabled) Ak.OnSurface else Ak.Muted, maxLines = 1)
                        Text(if (app.enabled) "Aktif / Korumalı" else "Kapalı", style = MaterialTheme.typography.labelMedium, color = if (app.enabled) Ak.Primary else Ak.Muted)
                    }
                    androidx.compose.material3.Switch(
                        checked = app.enabled,
                        onCheckedChange = { vm.setLockedApp(app.packageName, app.label, it) },
                        enabled = !locked || !app.enabled,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Ak.Bg,
                            checkedTrackColor = Ak.Primary,
                            uncheckedTrackColor = Ak.Lowest,
                        ),
                    )
                    if (!locked) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Rounded.Close, "Listeden çıkar", tint = Ak.Muted, modifier = Modifier.size(20.dp).clickable { vm.removeLockedApp(app) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostButton("Ekle", Icons.Rounded.AddCircleOutline, { showAppPicker = true }, Modifier.weight(1f))
                GhostButton("Önerilenler", Icons.Rounded.AutoAwesome, { vm.addSuggestedApps() }, Modifier.weight(1f), color = Ak.Secondary)
            }
        }

        // ------------------------------------------------------------ Güvenli kişi
        SectionHeader(Icons.Rounded.SupportAgent, "Güvenli Kişi", Ak.Primary) {
            Text("Acil durum kurtarıcısı", style = MaterialTheme.typography.labelMedium, color = Ak.Primary)
        }
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val safe = state.safeContact
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Ak.Emerald.copy(alpha = 0.25f))
                        .border(2.dp, Ak.Primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(safe?.name?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercase() }?.take(2)?.joinToString("") ?: "?", style = MaterialTheme.typography.titleMedium, color = Ak.Primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(safe?.name ?: "Henüz seçilmedi", style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(safe?.let { PhoneNumbers.mask(it.number) } ?: "Sarhoşken arayabileceğin tek kişi", style = MaterialTheme.typography.labelMedium, color = Ak.Muted)
                }
                TextButton(onClick = { pickSafe.launch(phonePicker) }, enabled = !locked) { Text(if (safe == null) "Seç" else "Değiştir") }
            }
        }

        // ------------------------------------------------------------ Promil metrikleri
        SectionHeader(Icons.Rounded.Scale, "Promil (BAC) Metrikleri", Ak.Secondary) {
            Text("Widmark + Watson", style = MaterialTheme.typography.labelMedium, color = Ak.Secondary)
        }
        ProfileEditor(state.profile, enabled = !locked, onChange = vm::saveProfile)

        // ------------------------------------------------------------ Kurallar
        SectionHeader(Icons.Rounded.Gavel, "Koruma Kuralları", Ak.Primary)
        GlassCard {
            Text("KİLİDİN AÇILACAĞI SABAH SAATİ", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("%02d:%02d".format(state.unlockHour, state.unlockMinute), style = MaterialTheme.typography.displaySmall, color = Ak.OnSurface, modifier = Modifier.weight(1f))
                SquareIconButton(Icons.Rounded.Remove, Ak.High, Ak.OnSurface, { vm.setUnlockTime((state.unlockHour + 23) % 24, state.unlockMinute) }, enabled = !locked)
                Spacer(Modifier.width(8.dp))
                SquareIconButton(Icons.Rounded.Add, Ak.High, Ak.OnSurface, { vm.setUnlockTime((state.unlockHour + 1) % 24, state.unlockMinute) }, enabled = !locked)
                Spacer(Modifier.width(8.dp))
                ChoiceChip(if (state.unlockMinute == 0) ":30" else ":00", false, !locked) { vm.setUnlockTime(state.unlockHour, if (state.unlockMinute == 0) 30 else 0) }
            }
            Text("Kilit her durumda en az 2 saat sürer.", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)

            Spacer(Modifier.height(16.dp))
            Text("TEST ARALIĞI (promil yükseldikçe kendiliğinden sıklaşır)", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 45, 60, 90).forEach { m ->
                    ChoiceChip("$m dk", state.testIntervalMin == m, !locked || m < state.testIntervalMin, Modifier.weight(1f)) { vm.setTestInterval(m) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("OTOMATİK KİLİT EŞİĞİ (test beklenmeden)", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.0 to "Kapalı", 1.0 to "1.0‰", 1.5 to "1.5‰", 2.0 to "2.0‰").forEach { (v, l) ->
                    ChoiceChip(l, state.autoLockBac == v, !locked, Modifier.weight(1f)) { vm.setAutoLock(v) }
                }
            }
            Spacer(Modifier.height(16.dp))
            ToggleRow(Icons.Rounded.PhoneDisabled, Ak.Crimson, "Sadece güvenli kişi aranabilsin", "Sarhoşken diğer tüm giden aramalar kesilir", state.callBlockAll, vm::setCallBlockAll, enabled = !locked || !state.callBlockAll)
            Spacer(Modifier.height(8.dp))
            ToggleRow(Icons.Rounded.WaterDrop, Ak.Primary, "Su hatırlatıcısı", "Her 2 kadehte bir bildirim", state.waterReminder, vm::setWaterReminder)
        }

        // ------------------------------------------------------------ Ev adresi
        SectionHeader(Icons.Rounded.Home, "Ev Adresi", Ak.Secondary)
        var address by remember(state.homeAddress) { mutableStateOf(state.homeAddress) }
        Field(address, { address = it; vm.setHomeAddress(it) }, "Taksi ve rota için ev adresin", KeyboardType.Text)

        // ------------------------------------------------------------ İzinler
        val missing = perms.count { !it.granted }
        val criticalMissing = perms.any { it.critical && !it.granted }
        GlassCard(onClick = onOpenPermissions, container = (if (criticalMissing) Ak.Secondary else Ak.Primary).copy(alpha = 0.10f), border = (if (criticalMissing) Ak.Secondary else Ak.Primary).copy(alpha = 0.3f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(if (criticalMissing) Icons.Rounded.Warning else Icons.Rounded.CheckCircle, if (criticalMissing) Ak.Secondary else Ak.Primary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Sistem İzinleri", style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface)
                    Text(
                        if (missing == 0) "Hepsi tamam" else "${perms.size - missing}/${perms.size} tamam" + if (criticalMissing) " · kritik izin eksik" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (criticalMissing) Ak.Secondary else Ak.Muted,
                    )
                }
            }
        }

        InfoBanner("Tüm veriler yalnızca bu telefonda saklanır. AyıkKal'ın internet izni yok; hiçbir veri dışarı gönderilmez.", Ak.Primary, Icons.Rounded.Policy)

        BigButton("Ayarları Kaydet & Hazırım", Icons.Rounded.CheckCircle, onDone)
        Spacer(Modifier.height(8.dp))
    }

    if (showAppPicker) AppPickerDialog(existing = apps.map { it.packageName }.toSet(), onDismiss = { showAppPicker = false }) { app ->
        vm.setLockedApp(app.packageName, app.label, true)
    }
    if (showManualContact) ManualContactDialog(onDismiss = { showManualContact = false }) { n, num -> vm.addBlockedContact(n, num) }
}

@Composable
private fun ProfileEditor(p: UserProfile, enabled: Boolean, onChange: (UserProfile) -> Unit) {
    GlassCard {
        InfoBanner("Doğru promil ve ayılma süresi tahmini için gereklidir.", Ak.Secondary, Icons.Rounded.Info)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("CİNSİYET", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                Spacer(Modifier.height(6.dp))
                Segmented(
                    listOf("Erkek" to Icons.Rounded.Male, "Kadın" to Icons.Rounded.Female),
                    if (p.male) 0 else 1,
                    enabled,
                ) { onChange(p.copy(male = it == 0)) }
            }
            Column(Modifier.weight(1f)) {
                Text("MİDE DURUMU", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                Spacer(Modifier.height(6.dp))
                Segmented(listOf("Aç" to null, "Tok" to null), if (p.fullStomach) 1 else 0, enabled) { onChange(p.copy(fullStomach = it == 1)) }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stepper("VÜCUT AĞIRLIĞI", p.weightKg, "kg", 35..200, enabled, Modifier.weight(1f)) { onChange(p.copy(weightKg = it)) }
            Stepper("BOY", p.heightCm, "cm", 130..220, enabled, Modifier.weight(1f)) { onChange(p.copy(heightCm = it)) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
            Stepper("YAŞ", p.age, "", 18..99, enabled, Modifier.weight(1f)) { onChange(p.copy(age = it)) }
            Column(Modifier.weight(1f)) {
                Text("DAĞILIM KATSAYISI", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
                Spacer(Modifier.height(6.dp))
                Text("r = ${"%.2f".format(BacCalculator.distributionFactor(p))}", style = MaterialTheme.typography.titleLarge, color = Ak.Secondary)
                Text("Vücut saatte ~${BacCalculator.ELIMINATION_PER_HOUR}‰ atar", style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
            }
        }
    }
}

@Composable
private fun Segmented(options: List<Pair<String, ImageVector?>>, selected: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Ak.Lowest)
            .padding(4.dp),
    ) {
        options.forEachIndexed { i, (label, icon) ->
            val sel = i == selected
            Row(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (sel) Ak.Primary else Color.Transparent)
                    .clickable(enabled = enabled) { onSelect(i) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(icon, null, tint = if (sel) Ak.OnPrimary else Ak.Muted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(label, style = MaterialTheme.typography.labelLarge, color = if (sel) Ak.OnPrimary else Ak.OnVariant)
            }
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, unit: String, range: IntRange, enabled: Boolean, modifier: Modifier, onChange: (Int) -> Unit) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Ak.Lowest)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f).padding(start = 6.dp), verticalAlignment = Alignment.Bottom) {
                Text("$value", style = MaterialTheme.typography.headlineMedium, color = Ak.OnSurface)
                if (unit.isNotEmpty()) Text(" $unit".uppercase(), style = MaterialTheme.typography.labelSmall, color = Ak.Muted, modifier = Modifier.padding(bottom = 4.dp))
            }
            SquareIconButton(Icons.Rounded.Remove, Ak.High, Ak.OnSurface, { onChange((value - 1).coerceIn(range)) }, enabled, size = 36.dp)
            Spacer(Modifier.width(6.dp))
            SquareIconButton(Icons.Rounded.Add, Ak.High, Ak.OnSurface, { onChange((value + 1).coerceIn(range)) }, enabled, size = 36.dp)
        }
    }
}

@Composable
private fun ChoiceChip(text: String, selected: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Ak.Primary else Ak.High)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (selected) Ak.OnPrimary else if (enabled) Ak.OnSurface else Ak.Muted)
    }
}

@Composable
private fun AppPickerDialog(existing: Set<String>, onDismiss: () -> Unit, onPick: (InstalledApp) -> Unit) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { apps = withContext(Dispatchers.IO) { AppCatalog.launchableApps(context) } }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Ak.Low,
        title = { Text("Uygulama seç") },
        text = {
            Column {
                Field(query, { query = it }, "Ara", KeyboardType.Text)
                Spacer(Modifier.height(10.dp))
                val list = apps
                if (list == null) {
                    Text("Yükleniyor…", color = Ak.Muted)
                } else {
                    LazyColumn(Modifier.heightIn(max = 380.dp)) {
                        items(list.filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }, key = { it.packageName }) { app ->
                            val added = app.packageName in existing
                            val bmp = remember(app.packageName) { app.icon?.toBitmap(64, 64)?.asImageBitmap() }
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !added) { onPick(app); onDismiss() }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (bmp != null) Image(bmp, null, Modifier.size(36.dp)) else Icon(Icons.Rounded.Apps, null, tint = Ak.Muted)
                                Spacer(Modifier.width(12.dp))
                                Text(app.label, style = MaterialTheme.typography.titleSmall, color = if (added) Ak.Muted else Ak.OnSurface, modifier = Modifier.weight(1f))
                                if (added) Icon(Icons.Rounded.CheckCircle, null, tint = Ak.Primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } },
    )
}

@Composable
private fun ManualContactDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Ak.Low,
        title = { Text("Yasaklı kişi ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(name, { name = it }, "İsim (ör. Eski Sevgili)", KeyboardType.Text)
                Field(number, { number = it }, "Telefon numarası", KeyboardType.Phone)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, number); onDismiss() },
                enabled = PhoneNumbers.normalize(number).length >= 7,
            ) { Text("Ekle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}

/** Rehber seçicisinden dönen URI'den isim ve numara okur; READ_CONTACTS izni gerekmez. */
private fun readPickedPhone(context: Context, uri: Uri?): Pair<String, String>? {
    uri ?: return null
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
    )
    return context.contentResolver.query(uri, projection, null, null, null)?.use { c ->
        if (!c.moveToFirst()) null else (c.getString(0) ?: "") to (c.getString(1) ?: "")
    }?.takeIf { it.second.isNotBlank() }
}
