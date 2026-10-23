package com.ayikkal.app.ui.screens

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ayikkal.app.system.PermissionKey
import com.ayikkal.app.system.Permissions
import com.ayikkal.app.ui.components.BigButton
import com.ayikkal.app.ui.components.GlassCard
import com.ayikkal.app.ui.components.InfoBanner
import com.ayikkal.app.ui.components.PillButton
import com.ayikkal.app.ui.components.Tag
import com.ayikkal.app.ui.rememberResumeKey
import com.ayikkal.app.ui.theme.Ak

@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val resume = rememberResumeKey()
    val items = remember(resume, refresh) { Permissions.all(context) }

    val single = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh++ }
    val multi = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refresh++ }
    val role = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { refresh++ }

    fun open(intent: Intent) = runCatching { context.startActivity(intent) }.onFailure {
        context.startActivity(Permissions.appDetails(context))
    }

    fun request(key: PermissionKey) {
        when (key) {
            PermissionKey.NOTIFICATIONS ->
                if (Build.VERSION.SDK_INT >= 33) single.launch(Manifest.permission.POST_NOTIFICATIONS) else open(Permissions.notificationSettings(context))
            PermissionKey.ACCESSIBILITY -> {
                Toast.makeText(context, "Yüklü uygulamalar > AyıkKal Kalkanı > Aç", Toast.LENGTH_LONG).show()
                open(Permissions.accessibilitySettings())
            }
            PermissionKey.EXACT_ALARM -> open(Permissions.exactAlarmSettings(context))
            PermissionKey.FULL_SCREEN -> open(Permissions.fullScreenSettings(context))
            PermissionKey.CALL_REDIRECTION -> requestRole(context, RoleManager.ROLE_CALL_REDIRECTION) { role.launch(it) }
            PermissionKey.CALL_SCREENING -> requestRole(context, RoleManager.ROLE_CALL_SCREENING) { role.launch(it) }
            PermissionKey.BATTERY -> open(Permissions.batterySettings(context))
            PermissionKey.CALL_PHONE -> single.launch(Manifest.permission.CALL_PHONE)
            PermissionKey.SMS -> single.launch(Manifest.permission.SEND_SMS)
            PermissionKey.LOCATION -> multi.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Geri", tint = Ak.OnSurface) }
            Text("Sistem İzinleri", style = MaterialTheme.typography.headlineMedium, color = Ak.OnSurface)
        }
        Text(
            "Android başka uygulamaları kilitlemeye ancak bu izinlerle izin veriyor. Kritik olanlar olmadan kalkan çalışmaz.",
            style = MaterialTheme.typography.bodyMedium,
            color = Ak.OnVariant,
        )
        InfoBanner(
            "Erişilebilirlik ayarı gri ve açılmıyorsa: Ayarlar > Uygulamalar > AyıkKal > sağ üst ⋮ > \"Kısıtlanmış ayarlara izin ver\". (Play Store dışından yüklenen uygulamalarda Android 13+ kuralı.)",
            Ak.Secondary,
            Icons.Rounded.Info,
        )
        items.sortedByDescending { it.critical }.forEach { p ->
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (p.granted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        null,
                        tint = if (p.granted) Ak.Primary else if (p.critical) Ak.Secondary else Ak.Muted,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(p.title, style = MaterialTheme.typography.titleMedium, color = Ak.OnSurface)
                            if (p.critical) {
                                Spacer(Modifier.width(8.dp))
                                Tag("Kritik", Ak.Secondary)
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(p.why, style = MaterialTheme.typography.bodySmall, color = Ak.Muted)
                    }
                    if (!p.granted) {
                        Spacer(Modifier.width(8.dp))
                        PillButton("Ver", null, Ak.Primary, { request(p.key) })
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        BigButton("Tamam", Icons.Rounded.CheckCircle, onBack, height = 56.dp)
        Spacer(Modifier.height(8.dp))
    }
}

private fun requestRole(context: Context, roleName: String, launch: (Intent) -> Unit) {
    val intent = Permissions.roleIntent(context, roleName)
    if (intent == null) {
        Toast.makeText(context, "Bu cihaz bu özelliği desteklemiyor", Toast.LENGTH_SHORT).show()
    } else {
        launch(intent)
    }
}
