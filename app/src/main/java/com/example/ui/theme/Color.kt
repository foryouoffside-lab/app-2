package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Warm charcoal night palette.
//
// Chosen for eye comfort rather than punch. Pure black is the worst ground for reading:
// the contrast against bright text causes halation, and on OLED it smears. A warm,
// non-black ground with softened off-white text keeps the contrast ratio comfortable
// and carries less blue than the navy this replaced.
val ObsidianBg = Color(0xFF121110)
val CharcoalSurface = Color(0xFF1D1B1A)
val ZincSurfaceElevated = Color(0xFF282524)
val MutedBorder = Color(0xFF35312F)

// True black option for the Eye Comfort setting: same ladder as above but neutral
// (R=G=B), for anyone who reads the warm tint above as an off yellow rather than a comfort choice.
val TrueBlackBg = Color(0xFF000000)
val TrueBlackSurface = Color(0xFF161616)
val TrueBlackSurfaceElevated = Color(0xFF232323)
val TrueBlackBorder = Color(0xFF343434)

// Calming Accents & Glows
val AmberPrimary = Color(0xFFF59E0B)
val AmberGlow = Color(0xFFFBBF24)
val AmberContainer = Color(0xFF352511)
val OnAmberContainer = Color(0xFFFEF3C7)

val BiologicalTeal = Color(0xFF2DD4BF)
val BiologicalTealContainer = Color(0xFF113D38)

val IrisLavender = Color(0xFF818CF8)
val CalmIndigo = Color(0xFF6366F1)
val WarmApricot = Color(0xFFFB923C)
val MintBreeze = Color(0xFF34D399)

// Text. Deliberately not #FFFFFF: a warm off-white sits around 14:1 on the ground above,
// which is legible without the glare that maxed-out contrast produces at night.
val TextHighEmphasis = Color(0xFFEDE9E6)
val TextMediumEmphasis = Color(0xFFA8A29E)
// Was #78716C (~3.9:1 on ObsidianBg) -- fails WCAG AA (4.5:1) for the small captions and
// disclaimers this role is used on. Lightened to clear AA with margin (~5.4:1).
val TextMuted = Color(0xFF8F8880)

val EmeraldSuccess = Color(0xFF10B981)
val RoseCritical = Color(0xFFF43F5E)

// Daylight palette. Deliberately a soft paper white rather than #FFFFFF, and the
// accents are darkened so they still pass contrast on a light ground.
val PaperBg = Color(0xFFF7F8FA)
val PaperSurface = Color(0xFFFFFFFF)
val PaperSurfaceElevated = Color(0xFFEFF2F7)
val PaperBorder = Color(0xFFDFE4EC)

val AmberInk = Color(0xFFB45309)
val AmberInkContainer = Color(0xFFFDF0DC)
val TealInk = Color(0xFF0D9488)
val TealInkContainer = Color(0xFFD5F2EE)

val InkHighEmphasis = Color(0xFF141A26)
val InkMediumEmphasis = Color(0xFF556070)
// Was #8A94A5 (~2.9:1 on PaperBg) -- well below WCAG AA (4.5:1) for small text. Darkened
// to clear AA with margin (~5.5:1), same slate-blue hue.
val InkMuted = Color(0xFF5A6578)

val EmeraldInk = Color(0xFF047857)
val RoseInk = Color(0xFFBE123C)
