package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colours for app chrome.
 *
 * The screens used to name palette constants directly, which meant a theme switch
 * changed nothing. They now read these roles instead.
 */
@Immutable
data class AppPalette(
    val bg: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textHigh: Color,
    val textMedium: Color,
    val textMuted: Color,
    val amber: Color,
    val amberGlow: Color,
    val teal: Color,
    val tealContainer: Color,
    val iris: Color,
    val mint: Color,
    val apricot: Color,
    val emerald: Color,
    val rose: Color
)

val DarkPalette = AppPalette(
    bg = ObsidianBg,
    surface = CharcoalSurface,
    surfaceElevated = ZincSurfaceElevated,
    border = MutedBorder,
    textHigh = TextHighEmphasis,
    textMedium = TextMediumEmphasis,
    textMuted = TextMuted,
    amber = AmberPrimary,
    amberGlow = AmberGlow,
    teal = BiologicalTeal,
    tealContainer = BiologicalTealContainer,
    iris = IrisLavender,
    mint = MintBreeze,
    apricot = WarmApricot,
    emerald = EmeraldSuccess,
    rose = RoseCritical
)

/** Same roles as [DarkPalette], with a neutral true-black ground for the Eye Comfort setting. */
val TrueBlackPalette = DarkPalette.copy(
    bg = TrueBlackBg,
    surface = TrueBlackSurface,
    surfaceElevated = TrueBlackSurfaceElevated,
    border = TrueBlackBorder
)

val LightPalette = AppPalette(
    bg = PaperBg,
    surface = PaperSurface,
    surfaceElevated = PaperSurfaceElevated,
    border = PaperBorder,
    textHigh = InkHighEmphasis,
    textMedium = InkMediumEmphasis,
    textMuted = InkMuted,
    amber = AmberInk,
    amberGlow = AmberInk,
    teal = TealInk,
    tealContainer = TealInkContainer,
    iris = Color(0xFF4F46E5),
    mint = Color(0xFF059669),
    apricot = Color(0xFFC2410C),
    emerald = EmeraldInk,
    rose = RoseInk
)

val LocalAppPalette = staticCompositionLocalOf { DarkPalette }

object AppTheme {
    val colors: AppPalette
        @Composable get() = LocalAppPalette.current
}
