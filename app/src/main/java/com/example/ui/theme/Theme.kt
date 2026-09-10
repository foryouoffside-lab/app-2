package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun EyeRestTheme(
    content: @Composable () -> Unit
) {
    // For an eye-strain relief instrument, we default to the eye-safe, low-luminance palette
    MaterialTheme(
        colorScheme = ErgonomicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
