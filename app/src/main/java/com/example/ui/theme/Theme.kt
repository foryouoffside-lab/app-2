package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.util.ThemeMode

private val ErgonomicDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = ObsidianBg,
    primaryContainer = AmberContainer,
    onPrimaryContainer = OnAmberContainer,
    secondary = BiologicalTeal,
    onSecondary = ObsidianBg,
    secondaryContainer = BiologicalTealContainer,
    onSecondaryContainer = TextHighEmphasis,
    tertiary = BiologicalTeal,
    background = ObsidianBg,
    onBackground = TextHighEmphasis,
    surface = CharcoalSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = ZincSurfaceElevated,
    onSurfaceVariant = TextMediumEmphasis,
    outline = MutedBorder,
    error = RoseCritical,
    onError = TextHighEmphasis
)

private val DaylightColorScheme = lightColorScheme(
    primary = AmberInk,
    onPrimary = PaperSurface,
    primaryContainer = AmberInkContainer,
    onPrimaryContainer = AmberInk,
    secondary = TealInk,
    onSecondary = PaperSurface,
    secondaryContainer = TealInkContainer,
    onSecondaryContainer = InkHighEmphasis,
    tertiary = TealInk,
    background = PaperBg,
    onBackground = InkHighEmphasis,
    surface = PaperSurface,
    onSurface = InkHighEmphasis,
    surfaceVariant = PaperSurfaceElevated,
    onSurfaceVariant = InkMediumEmphasis,
    outline = PaperBorder,
    error = RoseInk,
    onError = PaperSurface
)

/**
 * Resolves the user's choice, falling back to the system setting.
 *
 * Dark remains the default elsewhere in the app: a low-luminance ground is the right
 * default for an eye-strain tool, but forcing it on someone working in daylight is worse
 * than letting them choose.
 */
@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun EyeRestTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val dark = isDarkTheme(themeMode)
    CompositionLocalProvider(LocalAppPalette provides if (dark) DarkPalette else LightPalette) {
        MaterialTheme(
            colorScheme = if (dark) ErgonomicDarkColorScheme else DaylightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
