@file:OptIn(ExperimentalTextApi::class)

package com.ayikkal.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ayikkal.app.R

/** "Nocturnal Clarity" tasarım sisteminin renkleri (designs/.../DESIGN.md). */
object Ak {
    val Bg = Color(0xFF0F131D)
    val Lowest = Color(0xFF0A0E18)
    val Low = Color(0xFF171B26)
    val Container = Color(0xFF1C1F2A)
    val High = Color(0xFF262A35)
    val Highest = Color(0xFF313540)
    val OnSurface = Color(0xFFDFE2F1)
    val OnVariant = Color(0xFFBBCABF)
    val Muted = Color(0xFF9CA3AF)
    val Outline = Color(0xFF86948A)
    val OutlineVariant = Color(0xFF3C4A42)
    val Hairline = Color(0x14FFFFFF)

    val Primary = Color(0xFF4EDEA3)
    val OnPrimary = Color(0xFF003824)
    val Emerald = Color(0xFF10B981)

    val Amber = Color(0xFFF59E0B)
    val Secondary = Color(0xFFFFB95F)
    val Orange = Color(0xFFFB923C)
    val OnSecondary = Color(0xFF472A00)

    val Crimson = Color(0xFFEF4444)
    val Rose = Color(0xFFF43F5E)
    val Tertiary = Color(0xFFFFB3AD)
    val TertiaryContainer = Color(0xFFFF7A73)
    val ErrorContainer = Color(0xFF93000A)
}

private fun jakarta(weight: Int) = Font(
    R.font.plus_jakarta_sans,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Jakarta = FontFamily(
    jakarta(400), jakarta(500), jakarta(600), jakarta(700), jakarta(800),
)

private fun style(size: Int, weight: Int, line: Int, spacing: Double = 0.0) = TextStyle(
    fontFamily = Jakarta,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.em,
)

val AkTypography = Typography(
    displayLarge = style(40, 800, 48, -0.02),
    displayMedium = style(32, 800, 40, -0.02),
    displaySmall = style(28, 800, 34, -0.01),
    headlineLarge = style(28, 700, 36, -0.01),
    headlineMedium = style(22, 700, 28),
    headlineSmall = style(18, 600, 24),
    titleLarge = style(20, 700, 26),
    titleMedium = style(16, 700, 22),
    titleSmall = style(14, 700, 18),
    bodyLarge = style(16, 500, 24),
    bodyMedium = style(14, 400, 20),
    bodySmall = style(12, 400, 16),
    labelLarge = style(14, 700, 18, 0.04),
    labelMedium = style(12, 600, 16, 0.02),
    labelSmall = style(11, 600, 14, 0.05),
)

private val scheme = darkColorScheme(
    primary = Ak.Primary,
    onPrimary = Ak.OnPrimary,
    primaryContainer = Ak.Emerald,
    onPrimaryContainer = Color(0xFF00422B),
    secondary = Ak.Secondary,
    onSecondary = Ak.OnSecondary,
    secondaryContainer = Color(0xFFEE9800),
    tertiary = Ak.Tertiary,
    tertiaryContainer = Ak.TertiaryContainer,
    error = Color(0xFFFFB4AB),
    errorContainer = Ak.ErrorContainer,
    background = Ak.Bg,
    onBackground = Ak.OnSurface,
    surface = Ak.Bg,
    onSurface = Ak.OnSurface,
    surfaceVariant = Ak.Highest,
    onSurfaceVariant = Ak.OnVariant,
    surfaceContainerLowest = Ak.Lowest,
    surfaceContainerLow = Ak.Low,
    surfaceContainer = Ak.Container,
    surfaceContainerHigh = Ak.High,
    surfaceContainerHighest = Ak.Highest,
    outline = Ak.Outline,
    outlineVariant = Ak.OutlineVariant,
)

/** Bar ortamında göz almasın diye uygulama her zaman karanlık temada çalışır. */
@Composable
fun AyikKalTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = AkTypography, content = content)
}
