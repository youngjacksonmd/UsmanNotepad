package com.usman.notepad.v3.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.notepad.v3.data.AppearancePrefs
import com.usman.notepad.v3.data.V3Accent
import com.usman.notepad.v3.data.V3Density
import com.usman.notepad.v3.data.V3ThemeMode

@Immutable
data class UsmanMetrics(
    val pagePadding: Dp,
    val itemGap: Dp,
    val sectionGap: Dp,
    val cardRadius: Dp,
    val chipRadius: Dp,
    val titleScale: Float,
    val bodyScale: Float
)

val LocalUsmanMetrics = compositionLocalOf { UsmanMetrics(20.dp, 12.dp, 28.dp, 22.dp, 14.dp, 1f, 1f) }

object UsmanMotion {
    const val Micro = 150
    const val Small = 220
    const val Sheet = 300
}

private fun accentColor(a: V3Accent): Color = when (a) {
    V3Accent.INDIGO -> Color(0xFF5A5BD6)
    V3Accent.VIOLET -> Color(0xFF7656C9)
    V3Accent.SAGE -> Color(0xFF5E7764)
    V3Accent.ROSE -> Color(0xFFA85F6E)
    V3Accent.SKY -> Color(0xFF4D7395)
    V3Accent.GRAPHITE -> Color(0xFF5D6269)
}

private fun lightScheme(accent: Color): ColorScheme = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = .13f).compositeOver(Color(0xFFF6F1E8)),
    onPrimaryContainer = Color(0xFF28284A),
    background = Color(0xFFF7F2E9),
    onBackground = Color(0xFF242321),
    surface = Color(0xFFFFFBF4),
    onSurface = Color(0xFF242321),
    surfaceVariant = Color(0xFFEDE6DB),
    onSurfaceVariant = Color(0xFF6B665F),
    outline = Color(0xFFD4CCC0),
    error = Color(0xFFB04D4D)
)

private fun darkScheme(accent: Color, oled: Boolean): ColorScheme {
    val bg = if (oled) Color.Black else Color(0xFF171717)
    val surface = if (oled) Color(0xFF0B0B0B) else Color(0xFF202020)
    return darkColorScheme(
        primary = accent.copy(red = (accent.red + .14f).coerceAtMost(1f), blue = (accent.blue + .08f).coerceAtMost(1f)),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF2D2C49),
        onPrimaryContainer = Color(0xFFE8E6FF),
        background = bg,
        onBackground = Color(0xFFF0EEE9),
        surface = surface,
        onSurface = Color(0xFFF0EEE9),
        surfaceVariant = if (oled) Color(0xFF161616) else Color(0xFF292929),
        onSurfaceVariant = Color(0xFFB8B4AE),
        outline = Color(0xFF454545),
        error = Color(0xFFFFB4AB)
    )
}

private fun Color.compositeOver(background: Color): Color {
    val a = alpha
    if (a >= 1f) return this
    val outA = a + background.alpha * (1f - a)
    return Color(
        red = (red * a + background.red * background.alpha * (1f - a)) / outA,
        green = (green * a + background.green * background.alpha * (1f - a)) / outA,
        blue = (blue * a + background.blue * background.alpha * (1f - a)) / outA,
        alpha = outA
    )
}

@Composable
fun UsmanNotepadTheme(prefs: AppearancePrefs, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (prefs.themeMode) {
        V3ThemeMode.SYSTEM -> systemDark
        V3ThemeMode.LIGHT -> false
        V3ThemeMode.DARK, V3ThemeMode.OLED -> true
    }
    val accent = accentColor(prefs.accent)
    val scheme = if (dark) darkScheme(accent, prefs.themeMode == V3ThemeMode.OLED) else lightScheme(accent)
    val densityFactor = when (prefs.density) {
        V3Density.CALM -> 1.12f
        V3Density.MODERN -> 1f
        V3Density.COMPACT -> .88f
    }
    val metrics = UsmanMetrics(
        pagePadding = (20 * densityFactor).dp,
        itemGap = (12 * densityFactor).dp,
        sectionGap = (28 * densityFactor).dp,
        cardRadius = 22.dp,
        chipRadius = 14.dp,
        titleScale = prefs.textScale,
        bodyScale = prefs.textScale
    )
    val typography = Typography(
        displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = (34 * prefs.textScale).sp, lineHeight = (40 * prefs.textScale).sp, letterSpacing = (-.4).sp),
        headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = (27 * prefs.textScale).sp, lineHeight = (33 * prefs.textScale).sp),
        titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = (20 * prefs.textScale).sp, lineHeight = (26 * prefs.textScale).sp),
        titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = (17 * prefs.textScale).sp, lineHeight = (23 * prefs.textScale).sp),
        bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = (17 * prefs.textScale).sp, lineHeight = (27 * prefs.textScale).sp),
        bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = (15 * prefs.textScale).sp, lineHeight = (22 * prefs.textScale).sp),
        labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = (13 * prefs.textScale).sp)
    )
    androidx.compose.runtime.CompositionLocalProvider(LocalUsmanMetrics provides metrics) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}