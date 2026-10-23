package com.ayikkal.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayikkal.app.domain.BacLevel
import com.ayikkal.app.ui.theme.Ak

enum class ShieldStatus(val label: String, val color: Color) {
    READY("Koruma Devrede", Ak.Primary),
    SETUP("Kurulum Eksik", Ak.Secondary),
    DRUNK("Sarhoş Modu", Ak.Crimson),
}

fun levelColor(level: BacLevel): Color = when (level) {
    BacLevel.SOBER -> Ak.Primary
    BacLevel.LIGHT -> Color(0xFFA3E635)
    BacLevel.TIPSY -> Ak.Secondary
    BacLevel.DRUNK -> Ak.Orange
    BacLevel.VERY_DRUNK -> Ak.Crimson
    BacLevel.DANGER -> Ak.Rose
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    container: Color = Ak.Container.copy(alpha = 0.85f),
    border: Color = Ak.Hairline,
    corner: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .border(1.dp, border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

/** Kartın arkasına renkli, yumuşak bir ışıma çizer. */
fun Modifier.glow(color: Color, radiusFraction: Float = 0.8f, alpha: Float = 0.22f): Modifier = drawBehind {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = alpha), Color.Transparent),
            center = Offset(size.width / 2, size.height * 0.35f),
            radius = size.maxDimension * radiusFraction,
        ),
        radius = size.maxDimension * radiusFraction,
        center = Offset(size.width / 2, size.height * 0.35f),
    )
}

@Composable
fun AyikTopBar(subtitle: String, status: ShieldStatus, onProfile: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Ak.Container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Shield, null, tint = Ak.Primary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("AyıkKal", style = MaterialTheme.typography.titleLarge, color = Ak.OnSurface)
            Text(subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, color = Ak.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        StatusPill(status.label, status.color, pulsing = status == ShieldStatus.DRUNK)
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(status.color)
                .clickable(onClick = onProfile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Person, "Ayarlar", tint = Ak.Bg, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color, pulsing: Boolean = false, modifier: Modifier = Modifier) {
    val alpha = if (pulsing) {
        val t = rememberInfiniteTransition(label = "pulse")
        t.animateFloat(0.35f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a").value
    } else 1f
    Row(
        modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.25f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .alpha(alpha)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

@Composable
fun Tag(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        maxLines = 1,
    )
}

@Composable
fun SectionHeader(icon: ImageVector, title: String, tint: Color = Ak.Primary, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Ak.OnSurface, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color, size: Dp = 52.dp, background: Color = tint.copy(alpha = 0.14f), shape: RoundedCornerShape = RoundedCornerShape(14.dp)) {
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.48f))
    }
}

@Composable
fun BigButton(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Ak.Primary,
    contentColor: Color = Ak.OnPrimary,
    enabled: Boolean = true,
    height: Dp = 64.dp,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor,
            disabledContainerColor = Ak.High,
            disabledContentColor = Ak.Muted,
        ),
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(text, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun GhostButton(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Ak.OnSurface,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Ak.Hairline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Ak.Container, contentColor = color),
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun PillButton(text: String, icon: ImageVector?, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier, filled: Boolean = false) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (filled) color else color.copy(alpha = 0.14f),
        contentColor = if (filled) Ak.Bg else color,
    ) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        }
    }
}

@Composable
fun CounterControls(count: Int, onMinus: () -> Unit, onPlus: () -> Unit, enabled: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (count > 0) {
            SquareIconButton(Icons.Rounded.Remove, Ak.High, Ak.OnSurface, onMinus, enabled)
            Text(
                "$count",
                Modifier.width(40.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = Ak.OnSurface,
                textAlign = TextAlign.Center,
            )
        }
        SquareIconButton(Icons.Rounded.Add, Ak.Primary, Ak.OnPrimary, onPlus, enabled)
    }
}

@Composable
fun SquareIconButton(icon: ImageVector, bg: Color, fg: Color, onClick: () -> Unit, enabled: Boolean = true, size: Dp = 48.dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) bg else Ak.High)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (enabled) fg else Ak.Muted)
    }
}

@Composable
fun ToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Ak.High.copy(alpha = if (checked) 1f else 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon, iconTint, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (checked) Ak.OnSurface else Ak.Muted)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = if (checked) Ak.Primary else Ak.Muted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Ak.Bg,
                checkedTrackColor = Ak.Primary,
                uncheckedThumbColor = Ak.Muted,
                uncheckedTrackColor = Ak.Lowest,
                uncheckedBorderColor = Ak.Highest,
            ),
        )
    }
}

/** Promil halkası: 0–2‰ aralığını gösteren, ucu parlayan gradyan yay. */
@Composable
fun BacRing(bac: Double, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val fraction by animateFloatAsState((bac / 2.0).toFloat().coerceIn(0f, 1f), tween(900), label = "bac")
    val color = levelColor(BacLevel.of(bac))
    Box(modifier.size(230.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(230.dp)) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2 + 6.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            drawArc(Ak.High, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            rotate(-90f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to Ak.Primary,
                        0.45f to Ak.Secondary,
                        0.8f to Ak.Orange,
                        1f to Ak.Crimson,
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f * fraction.coerceAtLeast(0.005f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
            // Ucun parıltısı
            val angle = Math.toRadians((360.0 * fraction) - 90.0)
            val r = arcSize.width / 2
            val c = Offset(size.width / 2 + (r * kotlin.math.cos(angle)).toFloat(), size.height / 2 + (r * kotlin.math.sin(angle)).toFloat())
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), c, stroke * 1.6f), stroke * 1.6f, c)
        }
        content()
    }
}


@Composable
fun KeyValue(label: String, value: String, valueColor: Color = Ak.OnSurface, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Ak.Muted)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoBanner(text: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color, fontSize = 13.sp)
    }
}
