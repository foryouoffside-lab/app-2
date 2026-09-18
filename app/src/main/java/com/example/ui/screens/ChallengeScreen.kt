package com.example.ui.screens

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChallengeKind
import com.example.model.ChallengeSection
import com.example.model.VisionChallenge
import com.example.model.VisionChallengeRepository
import com.example.model.accuracyPercent
import com.example.model.acuityEyesDiffer
import com.example.model.medianMillis
import com.example.model.nearClarityLabel
import com.example.model.nearClarityRecommendation
import com.example.model.optotypeMillimetresAt40Cm
import com.example.ui.components.ChallengePreview
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random


private data class GridAnswer(
    val wavy: Boolean = false,
    val missing: Boolean = false,
    val dark: Boolean = false
) {
    val hasChange get() = wavy || missing || dark
}

private data class PlateDot(val x: Float, val y: Float, val radius: Float, val inDigit: Boolean)

/** Evidence-aware home checks and performance games. None diagnose disease. */
@Composable
fun ChallengeScreen(age: Int = 30) {
    var selected by remember { mutableStateOf<VisionChallenge?>(null) }
    BackHandler(enabled = selected != null) { selected = null }
    selected?.let { ChallengeRunner(it, age) { selected = null } }
        ?: ChallengeLibrary { selected = it }
}

@Composable
private fun ChallengeLibrary(onSelect: (VisionChallenge) -> Unit) {
    val colors = AppTheme.colors
    val checks = VisionChallengeRepository.challenges.filter { it.section == ChallengeSection.VISION_CHECK }
    val games = VisionChallengeRepository.challenges.filter { it.section == ChallengeSection.PERFORMANCE_CHALLENGE }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.bg).testTag("challenges_and_tests"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Challenges & Tests", color = colors.textHigh, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Structured self-checks and measurable visual challenges.", color = colors.textMedium, fontSize = 13.sp, lineHeight = 18.sp)
        }
        item { SafetyBanner() }
        item { SectionTitle("VISION CHECKS", "Run each eye separately. Repeat under the same conditions.") }
        items(checks, key = { it.id }) { ChallengeCard(it, onSelect) }
        item { SectionTitle("PERFORMANCE CHALLENGES", "Scores measure this task on this device—not eye health.") }
        items(games, key = { it.id }) { ChallengeCard(it, onSelect) }
        item {
            Surface(color = colors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, colors.border), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, null, tint = colors.teal, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("These checks cannot examine the retina, optic nerve, eye pressure or prescription. A normal result does not rule out eye disease.", color = colors.textMedium, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun SafetyBanner() {
    val colors = AppTheme.colors
    Surface(color = colors.rose.copy(alpha = 0.10f), border = androidx.compose.foundation.BorderStroke(1.dp, colors.rose.copy(alpha = 0.45f)), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.WarningAmber, null, tint = colors.rose, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Know the urgent signs", color = colors.textHigh, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("Sudden vision loss, severe eye pain, many new floaters or flashes, or a curtain/shadow in vision needs urgent medical care. Do not run a test first.", color = colors.textMedium, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    val colors = AppTheme.colors
    Column(Modifier.padding(top = 8.dp)) {
        Text(title, color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, color = colors.textMuted, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

/**
 * One row in the library: what the task looks like, what it is called, what you do.
 *
 * The row carried two coloured pills as well -- a duration and an evidence caveat -- on
 * every one of thirteen cards. Both are answers to questions you only ask after picking
 * a test, so both moved to the setup screen, where there is room to say them properly
 * and where they are read instead of scanned past.
 */
@Composable
private fun ChallengeCard(item: VisionChallenge, onSelect: (VisionChallenge) -> Unit) {
    val colors = AppTheme.colors
    Surface(
        color = colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().clickable { onSelect(item) }.testTag("challenge_${item.id}")
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ChallengePreview(
                item.kind,
                Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(colors.surfaceElevated)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, color = colors.textHigh, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(item.description, color = colors.textMedium, fontSize = 13.sp, lineHeight = 17.sp)
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(34.dp).clip(CircleShape).background(colors.surfaceElevated), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, "Start ${item.title}", tint = colors.textHigh, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.11f)).padding(horizontal = 7.dp, vertical = 3.dp))
}

@Composable
private fun ChallengeRunner(challenge: VisionChallenge, age: Int, onClose: () -> Unit) {
    when (challenge.kind) {
        ChallengeKind.NEAR_CLARITY -> NearClarityCheck(challenge, age, onClose)
        ChallengeKind.ASTIGMATISM_FAN -> AstigmatismFanCheck(challenge, onClose)
        ChallengeKind.READING_CLARITY -> ReadingClarityCheck(challenge, onClose)
        ChallengeKind.RED_GREEN_BALANCE -> RedGreenBalanceCheck(challenge, onClose)
        ChallengeKind.CENTRAL_GRID -> CentralGridCheck(challenge, onClose)
        ChallengeKind.ISHIHARA_STYLE_PLATES -> IshiharaStyleCheck(challenge, onClose)
        ChallengeKind.COVER_ALIGNMENT -> CoverAlignmentCheck(challenge, onClose)
        ChallengeKind.NEAR_POINT_CONVERGENCE -> NearPointConvergenceLog(challenge, onClose)
        ChallengeKind.CONTRAST_SPOTTING -> ContrastChallenge(challenge, onClose)
        ChallengeKind.COLOR_DISCRIMINATION -> ColorDiscriminationChallenge(challenge, onClose)
        ChallengeKind.AMBLYOPIA_PLAY -> AmblyopiaPlayChallenge(challenge, age, onClose)
        ChallengeKind.PERIPHERAL_AWARENESS -> PeripheralChallenge(challenge, onClose)
        ChallengeKind.VISUAL_REACTION -> ReactionChallenge(challenge, onClose)
    }
}

@Composable
private fun TestHeader(title: String, onClose: () -> Unit, step: String? = null) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(colors.surface).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to challenges and tests", tint = colors.textHigh)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        step?.let { Pill(it, colors.teal) }
    }
}

@Composable
private fun IntroScreen(challenge: VisionChallenge, bullets: List<String>, warning: String, onClose: () -> Unit, onStart: () -> Unit, extra: (@Composable () -> Unit)? = null) {
    val colors = AppTheme.colors
    if (challenge.howTo.isNotEmpty()) {
        // A check with a walkthrough covers its own setup one step at a time, so this
        // screen's only job is to say what it measures and hand off to that -- the same
        // shape as a training drill's intro, title and needs-kit line, then "Start — how
        // to do it". The bullets, the eye diagram and the calibration slider all moved
        // into the walkthrough itself; stacking them here too was reading everything twice.
        Column(Modifier.fillMaxSize().background(colors.bg)) {
            TestHeader(challenge.title, onClose)
            Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp), verticalArrangement = Arrangement.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Pill("About ${challenge.durationMinutes} min", colors.amber)
                    Pill(challenge.evidenceLabel, colors.teal)
                }
                Spacer(Modifier.height(14.dp))
                Text(challenge.measures, color = colors.textHigh, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(challenge.description, color = colors.textMedium, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(22.dp))
                extra?.invoke()
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 92.dp)) {
                PrimaryButton("Begin — how to do it", onStart)
                Spacer(Modifier.height(10.dp))
                Text(warning, color = colors.textMuted, fontSize = 11.sp, lineHeight = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().background(colors.bg), contentPadding = PaddingValues(bottom = 110.dp)) {
        item { TestHeader(challenge.title, onClose) }
        item {
            Column(Modifier.padding(horizontal = 22.dp)) {
                // The two things the row no longer says. Here they are read by somebody
                // deciding whether to start, which is the only moment they matter.
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Pill("About ${challenge.durationMinutes} min", colors.amber)
                    Pill(challenge.evidenceLabel, colors.teal)
                }
                Spacer(Modifier.height(14.dp))
                Text(challenge.measures, color = colors.textHigh, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(challenge.description, color = colors.textMedium, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(22.dp))
                Text("SETUP", color = colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(10.dp))
                bullets.forEachIndexed { index, text ->
                    Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(colors.teal.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = colors.teal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(11.dp))
                        Text(text, color = colors.textMedium, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                    }
                }
                extra?.invoke()
                Spacer(Modifier.height(18.dp))
                Surface(color = colors.amber.copy(alpha = 0.10f), shape = RoundedCornerShape(14.dp)) {
                    Text(warning, color = colors.textMedium, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(13.dp))
                }
                Spacer(Modifier.height(20.dp))
                PrimaryButton("Begin", onStart)
            }
        }
    }
}

/**
 * The intro's demo, same card Train's drill intro shows before its own Start button: a
 * bordered surface labelled HOW IT WORKS holding the one thing that actually explains the
 * task, with a line underneath saying what it's showing.
 */
@Composable
private fun HowItWorksDemo(caption: String, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    Surface(shape = RoundedCornerShape(24.dp), color = colors.surface, modifier = Modifier.fillMaxWidth().testTag("how_it_works_demo")) {
        Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HOW IT WORKS", color = colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(16.dp))
            content()
            Spacer(Modifier.height(14.dp))
            Text(caption, color = colors.textMedium, fontSize = 12.sp, lineHeight = 17.sp, textAlign = TextAlign.Center)
        }
    }
}

/**
 * The setup walked one step at a time, same idea as a training drill's how-to: a wall of
 * numbered bullets gets skimmed once and forgotten, but "cover your left eye" read alone,
 * full width, with the same eye diagram the test itself uses, stays read. [visual] is the
 * demo for the step that has one -- the eye to cover, the ring to expect, the pad to tap --
 * and falls back to a plain step number for a step that is only setup.
 */
@Composable
private fun TestWalkthrough(
    steps: List<String>,
    index: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    visual: (@Composable () -> Unit)? = null
) {
    val colors = AppTheme.colors
    val last = index >= steps.lastIndex
    Column(
        Modifier.fillMaxSize().background(colors.bg).testTag("test_walkthrough"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)) {
            Text("HOW TO DO IT", color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                steps.indices.forEach { i ->
                    Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (i <= index) colors.teal else colors.border))
                }
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 26.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (visual != null) visual() else Text("${index + 1}", color = colors.teal.copy(alpha = .30f), fontSize = 60.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Text(
                steps[index],
                color = colors.textHigh,
                fontSize = 19.sp,
                lineHeight = 27.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("walkthrough_step_text")
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp).padding(bottom = 92.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            PrimaryButton(if (last) "Start" else "Next", onNext, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = colors.amber, contentColor = colors.bg), shape = RoundedCornerShape(15.dp), modifier = modifier.fillMaxWidth().height(52.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun ResultScreen(title: String, headline: String, body: String, onClose: () -> Unit, accent: Color = AppTheme.colors.teal, details: (@Composable () -> Unit)? = null) {
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg)) {
        TestHeader(title, onClose, "COMPLETE")
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = accent, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text(headline, color = colors.textHigh, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(9.dp))
            Text(body, color = colors.textMedium, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
            details?.invoke()
            Spacer(Modifier.weight(1f))
            PrimaryButton("Back to challenges & tests", onClose)
            Spacer(Modifier.height(92.dp))
        }
    }
}

@Composable
private fun CentralGridCheck(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var complete by remember { mutableStateOf(false) }
    var eye by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateListOf(GridAnswer(), GridAnswer()) }
    var current by remember(eye) { mutableStateOf(GridAnswer()) }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = when (howToIndex) {
                1 -> { { EyeCoverDiagram(coverLeft = true) } }
                2 -> { { AmslerGrid() } }
                3 -> { { EyeCoverDiagram(coverLeft = false) } }
                else -> null
            }
        )
        return
    }
    if (!started) {
        IntroScreen(challenge, emptyList(), "This phone-sized pattern is a qualitative Amsler-style observation, not a full-size clinical chart. A new change should be discussed promptly with an eye-care professional.", onClose, onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }) {
            HowItWorksDemo("Cover one eye, watch the centre dot, then switch and do the other.") {
                EyeCoverDiagram(coverLeft = true)
            }
        }
        return
    }
    if (complete) {
        val changedEyes = answers.count { it.hasChange }
        ResultScreen(challenge.title, if (changedEyes == 0) "No change reported" else "Change reported in $changedEyes eye${if (changedEyes == 1) "" else "s"}", if (changedEyes == 0) "This is a monitoring note, not an all-clear. Keep your regular eye exams and compare again under the same conditions." else "If this is new or worse, contact an eye-care professional promptly. The grid cannot identify the cause.", onClose, if (changedEyes == 0) AppTheme.colors.teal else AppTheme.colors.rose)
        return
    }
    val colors = AppTheme.colors
    LazyColumn(Modifier.fillMaxSize().background(colors.bg), contentPadding = PaddingValues(bottom = 110.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item { TestHeader(challenge.title, onClose, if (eye == 0) "RIGHT EYE" else "LEFT EYE") }
        item {
            Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (eye == 0) "Cover your left eye" else "Cover your right eye", color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Keep looking at the centre dot.", color = colors.textMedium, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp)); EyeCoverDiagram(coverLeft = eye == 0)
                Spacer(Modifier.height(16.dp)); AmslerGrid(); Spacer(Modifier.height(18.dp))
                Text("What do you notice?", color = colors.textHigh, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                ToggleAnswer("Lines look wavy or bent", current.wavy) { current = current.copy(wavy = !current.wavy) }
                ToggleAnswer("An area looks missing or blank", current.missing) { current = current.copy(missing = !current.missing) }
                ToggleAnswer("An area looks dark or blurred", current.dark) { current = current.copy(dark = !current.dark) }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(if (eye == 0) "Save & check left eye" else "See result", onClick = { answers[eye] = current; if (eye == 0) eye = 1 else complete = true })
            }
        }
    }
}

@Composable
private fun AmslerGrid() {
    val colors = AppTheme.colors
    Canvas(Modifier.fillMaxWidth().widthIn(max = 320.dp).aspectRatio(1f).background(Color.White).testTag("central_vision_grid")) {
        val step = size.width / 20f
        for (i in 0..20) {
            val p = i * step
            drawLine(Color(0xFF616161), androidx.compose.ui.geometry.Offset(p, 0f), androidx.compose.ui.geometry.Offset(p, size.height), 1f)
            drawLine(Color(0xFF616161), androidx.compose.ui.geometry.Offset(0f, p), androidx.compose.ui.geometry.Offset(size.width, p), 1f)
        }
        drawCircle(colors.rose, radius = step * 0.18f, center = center)
    }
}

/**
 * A closed lid beside an open, seeing eye -- the one instruction every self-administered
 * per-eye check depends on, shown rather than only told. On the intro it previews what
 * "one eye at a time" actually means before the first round; on the round itself it turns
 * the header text into something read at a glance instead of parsed.
 */
/** The intro's "how you'll test" block: what the round below actually looks like. */
@Composable
private fun EyeCoverPreview() {
    val colors = AppTheme.colors
    Spacer(Modifier.height(18.dp))
    Text("HOW YOU'LL TEST", color = colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    Spacer(Modifier.height(12.dp))
    EyeCoverDiagram(coverLeft = true)
    Spacer(Modifier.height(10.dp))
    Text("You'll alternate: cover one eye, then the other, so each is scored on its own.", color = colors.textMedium, fontSize = 12.sp, lineHeight = 17.sp)
}

@Composable
private fun EyeCoverDiagram(coverLeft: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        EyeIcon(covered = coverLeft, label = "LEFT")
        EyeIcon(covered = !coverLeft, label = "RIGHT")
    }
}

@Composable
private fun EyeIcon(covered: Boolean, label: String) {
    val colors = AppTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(60.dp)) {
            val r = size.minDimension * 0.44f
            if (covered) {
                drawCircle(colors.surfaceElevated, r, center)
                drawLine(colors.textMuted, Offset(center.x - r * 0.62f, center.y), Offset(center.x + r * 0.62f, center.y), 4f, StrokeCap.Round)
            } else {
                drawCircle(Color.White, r, center)
                drawCircle(colors.iris, r * 0.52f, center)
                drawCircle(Color.Black, r * 0.20f, center)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = colors.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(if (covered) "Cover" else "Keep open", color = if (covered) colors.rose else colors.teal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ToggleAnswer(label: String, checked: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(color = if (checked) colors.iris.copy(alpha = 0.14f) else colors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) colors.iris else colors.border), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(if (checked) colors.iris else colors.surfaceElevated), contentAlignment = Alignment.Center) { if (checked) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(15.dp)) }
            Spacer(Modifier.width(11.dp)); Text(label, color = colors.textHigh, fontSize = 13.sp)
        }
    }
}

@Composable
private fun NearClarityCheck(challenge: VisionChallenge, age: Int, onClose: () -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val xdpi = context.resources.displayMetrics.xdpi
    fun mmToDp(mm: Double): Dp = (((mm / 25.4) * xdpi) / density.density).dp
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var scale by remember { mutableStateOf(1f) }
    var eye by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(0) }
    var attempt by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var complete by remember { mutableStateOf(false) }
    val results = remember { mutableStateListOf<Double?>(null, null) }
    val levels = remember { listOf(0.7, 0.5, 0.4, 0.3, 0.2, 0.1, 0.0) }
    val direction = (eye * 5 + level * 3 + attempt * 7) % 4
    fun finishEye(passed: Double?) { results[eye] = passed; if (eye == 0) { eye = 1; level = 0; attempt = 0; correct = 0 } else complete = true }
    fun answer(selected: Int?) {
        val newCorrect = correct + if (selected == direction) 1 else 0
        if (attempt < 2) { attempt++; correct = newCorrect; return }
        if (newCorrect >= 2) { if (level == levels.lastIndex) finishEye(levels[level]) else { level++; attempt = 0; correct = 0 } }
        else finishEye(levels.getOrNull(level - 1))
    }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            // The demo for each step is the exact diagram, ring, pad and slider the round
            // below uses -- so what "cover your left eye" means here is what it means there.
            visual = when (howToIndex) {
                1 -> { {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.width(mmToDp(53.98) * scale).height(4.dp).clip(CircleShape).background(AppTheme.colors.iris))
                        Spacer(Modifier.height(18.dp))
                        Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.75f..1.25f, modifier = Modifier.width(240.dp))
                    }
                } }
                2 -> { { EyeCoverDiagram(coverLeft = true) } }
                3 -> { { LandoltC(60.dp, 0) } }
                4 -> { { DirectionPad {} } }
                5 -> { { EyeCoverDiagram(coverLeft = false) } }
                else -> null
            }
        )
        return
    }
    if (!started) {
        IntroScreen(challenge, emptyList(), if (age < 18) "This adult-oriented self-check is not validated for children. A child's vision should be screened by a trained professional." else "Device density and viewing distance affect the result. The output is an approximate repeatable threshold, not a prescription or diagnosis.", onClose, onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }) {
            HowItWorksDemo("You'll cover one eye, read the gap in the ring, then switch and do the other.") {
                EyeCoverDiagram(coverLeft = true)
            }
        }
        return
    }
    if (complete) {
        val mismatch = acuityEyesDiffer(results[0], results[1])
        val noThreshold = results.all { it == null }
        ResultScreen(challenge.title, if (noThreshold) "No threshold recorded" else if (mismatch) "The eyes differed today" else "Eye results were similar", nearClarityRecommendation(results[0], results[1]), onClose, if (mismatch || noThreshold) AppTheme.colors.amber else AppTheme.colors.teal) {
            Spacer(Modifier.height(22.dp)); ResultMetric("Right eye", nearClarityLabel(results[0])); ResultMetric("Left eye", nearClarityLabel(results[1]))
        }
        return
    }
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, if (eye == 0) "RIGHT EYE" else "LEFT EYE")
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (eye == 0) "Cover left eye" else "Cover right eye", color = colors.textHigh, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("40 cm · ${level + 1}/${levels.size}", color = colors.textMuted, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp)); EyeCoverDiagram(coverLeft = eye == 0)
            Spacer(Modifier.weight(0.7f)); LandoltC((mmToDp(optotypeMillimetresAt40Cm(levels[level])) * scale).coerceAtLeast(4.dp), direction); Spacer(Modifier.weight(0.7f))
            Text("Where is the gap?", color = colors.textMedium, fontSize = 13.sp); Spacer(Modifier.height(12.dp))
            DirectionPad { answer(it) }
            OutlinedButton(onClick = { answer(null) }, modifier = Modifier.padding(top = 8.dp)) { Text("Not sure") }
            Spacer(Modifier.height(98.dp))
        }
    }
}

@Composable
private fun LandoltC(diameter: Dp, direction: Int) {
    val colors = AppTheme.colors
    Canvas(Modifier.size(diameter * 2.4f).background(Color.White).testTag("near_clarity_optotype")) {
        val d = diameter.toPx(); val stroke = d / 5f
        rotate(direction * 90f, center) {
            drawArc(Color.Black, 36f, 288f, false, androidx.compose.ui.geometry.Offset(center.x - d / 2, center.y - d / 2), androidx.compose.ui.geometry.Size(d, d), style = Stroke(stroke, cap = StrokeCap.Butt))
        }
        drawRect(colors.border.copy(alpha = 0.35f), style = Stroke(1f))
    }
}

@Composable
private fun DirectionPad(onAnswer: (Int) -> Unit) {
    val colors = AppTheme.colors
    val icons = listOf(Icons.Default.ArrowForward, Icons.Default.ArrowDownward, Icons.AutoMirrored.Filled.ArrowBack, Icons.Default.ArrowUpward)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        icons.forEachIndexed { index, icon ->
            Surface(color = colors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, colors.border), shape = RoundedCornerShape(15.dp), modifier = Modifier.size(60.dp).clickable { onAnswer(index) }) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, "Gap or cue direction", tint = colors.textHigh) }
            }
        }
    }
}

@Composable
private fun ResultMetric(label: String, value: String) {
    val colors = AppTheme.colors
    Surface(color = colors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, colors.border), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = colors.textMedium, fontSize = 13.sp); Text(value, color = colors.textHigh, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
    }
}

// ---------------------------------------------------------------------------
// Additional Essilor-inspired observations (independent graphics and wording)
// ---------------------------------------------------------------------------

@Composable
private fun AstigmatismFanCheck(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var eye by remember { mutableIntStateOf(0) }
    var complete by remember { mutableStateOf(false) }
    val uneven = remember { mutableStateListOf(false, false) }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = when (howToIndex) {
                1 -> { { EyeCoverDiagram(coverLeft = true) } }
                2 -> { { AstigmatismFan() } }
                3 -> { { EyeCoverDiagram(coverLeft = false) } }
                else -> null
            }
        )
        return
    }
    if (!started) {
        IntroScreen(
            challenge,
            emptyList(),
            "All spokes are drawn with identical width and darkness. Uneven appearance can have several causes and this pattern cannot diagnose astigmatism.",
            onClose,
            onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }
        ) {
            HowItWorksDemo("Cover one eye, compare every spoke, then switch and do the other.") {
                EyeCoverDiagram(coverLeft = true)
            }
        }
        return
    }
    if (complete) {
        val count = uneven.count { it }
        ResultScreen(
            challenge.title,
            if (count == 0) "Lines looked even" else "Uneven lines reported",
            if (count == 0) "Record this only as today's observation. It does not rule out astigmatism or another vision problem."
            else "If this repeats, or vision is blurred or distorted, arrange a comprehensive eye examination. The fan cannot determine a prescription.",
            onClose,
            if (count == 0) AppTheme.colors.teal else AppTheme.colors.amber,
            details = {
                Spacer(Modifier.height(20.dp))
                ResultMetric("Right eye", if (uneven[0]) "Some lines darker" else "Lines looked equal")
                ResultMetric("Left eye", if (uneven[1]) "Some lines darker" else "Lines looked equal")
            }
        )
        return
    }
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, if (eye == 0) "RIGHT EYE" else "LEFT EYE")
        Text(if (eye == 0) "Cover your left eye" else "Cover your right eye", color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Keep looking at the centre", color = colors.textMedium, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        EyeCoverDiagram(coverLeft = eye == 0)
        Spacer(Modifier.height(18.dp))
        AstigmatismFan()
        Spacer(Modifier.height(18.dp))
        Text("Do any spokes look darker or sharper?", color = colors.textHigh, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.padding(horizontal = 24.dp)) {
            PrimaryButton("All lines look equal", onClick = {
                uneven[eye] = false
                if (eye == 0) eye = 1 else complete = true
            })
            OutlinedButton(
                onClick = {
                    uneven[eye] = true
                    if (eye == 0) eye = 1 else complete = true
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Some lines look darker") }
        }
    }
}

@Composable
private fun AstigmatismFan() {
    Canvas(Modifier.size(284.dp).background(Color.White).testTag("astigmatism_fan")) {
        val radius = size.minDimension * 0.42f
        repeat(12) { index ->
            val angle = Math.toRadians((index * 15.0) - 90.0)
            val dx = kotlin.math.cos(angle).toFloat() * radius
            val dy = kotlin.math.sin(angle).toFloat() * radius
            drawLine(Color.Black, center - androidx.compose.ui.geometry.Offset(dx, dy), center + androidx.compose.ui.geometry.Offset(dx, dy), strokeWidth = 3f)
        }
        drawCircle(Color.White, radius = 14f, center = center)
        drawCircle(Color.Black, radius = 4f, center = center)
    }
}

@Composable
private fun ReadingClarityCheck(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var selectedLine by remember { mutableStateOf<String?>(null) }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = if (howToIndex == 2) { {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Aa", color = AppTheme.colors.textHigh, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Aa", color = AppTheme.colors.textMedium, fontSize = 24.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Aa", color = AppTheme.colors.textMuted, fontSize = 14.sp)
                }
            } } else null
        )
        return
    }
    if (!started) {
        IntroScreen(
            challenge,
            emptyList(),
            "Text size follows this phone's display and accessibility settings. This is a comfort baseline, not a Jaeger or near-acuity result.",
            onClose,
            onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }
        ) {
            HowItWorksDemo("You'll read from the largest line down and tap the smallest one that's still comfortable.") {
                Text("Aa", color = AppTheme.colors.textHigh, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }
        }
        return
    }
    selectedLine?.let { result ->
        ResultScreen(challenge.title, result, "Repeat with the same correction, distance, lighting and text-size settings. A recent or persistent reading change deserves an eye examination.", onClose)
        return
    }
    val colors = AppTheme.colors
    val lines = listOf(
        Triple("Line 1", "Clear vision supports comfortable reading.", 23.sp),
        Triple("Line 2", "Keep a steady forty centimetre distance.", 19.sp),
        Triple("Line 3", "Relax your shoulders and blink normally.", 16.sp),
        Triple("Line 4", "Small details should stay clear without squinting.", 13.sp),
        Triple("Line 5", "Stop if reading causes strain, headache or double vision.", 10.sp)
    )
    LazyColumn(Modifier.fillMaxSize().background(colors.bg), contentPadding = PaddingValues(bottom = 110.dp)) {
        item { TestHeader(challenge.title, onClose, "40 CM") }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("Tap the smallest line you can read comfortably", color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                lines.forEach { (label, text, size) ->
                    Surface(color = colors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, colors.border), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { selectedLine = "$label was comfortable" }) {
                        Column(Modifier.padding(14.dp)) {
                            Text(label.uppercase(), color = colors.teal, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
                            Spacer(Modifier.height(5.dp)); Text(text, color = colors.textHigh, fontSize = size, lineHeight = size * 1.25f)
                        }
                    }
                }
                OutlinedButton(onClick = { selectedLine = "No line was comfortable" }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("None are comfortable") }
            }
        }
    }
}

@Composable
private fun RedGreenBalanceCheck(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var answer by remember { mutableStateOf<String?>(null) }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = if (howToIndex == 1) { {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BalancePanel(Color(0xFFD94A4A), "RED", Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)))
                    BalancePanel(Color(0xFF48A868), "GREEN", Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)))
                }
            } } else null
        )
        return
    }
    if (!started) {
        IntroScreen(
            challenge,
            emptyList(),
            "Clinicians use duochrome as one step during subjective refraction. A phone display and this observation cannot determine whether a lens prescription should change.",
            onClose,
            onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }
        ) {
            HowItWorksDemo("You'll compare the identical dark rings on the red and green halves.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BalancePanel(Color(0xFFD94A4A), "RED", Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)))
                    BalancePanel(Color(0xFF48A868), "GREEN", Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)))
                }
            }
        }
        return
    }
    answer?.let { choice ->
        ResultScreen(challenge.title, choice, "This records only what looked clearer today. Do not change glasses or contact lenses based on this screen.", onClose, AppTheme.colors.iris)
        return
    }
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, "40 CM")
        Spacer(Modifier.height(24.dp))
        Text("Where do the rings look darker?", color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 20.dp)) {
            BalancePanel(Color(0xFFD94A4A), "RED", Modifier.weight(1f))
            BalancePanel(Color(0xFF48A868), "GREEN", Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton("Red side", onClick = { answer = "Red looked darker" })
            PrimaryButton("Green side", onClick = { answer = "Green looked darker" })
            OutlinedButton(onClick = { answer = "Both sides looked similar" }, modifier = Modifier.fillMaxWidth()) { Text("They look the same") }
        }
    }
}

@Composable
private fun BalancePanel(color: Color, label: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(color), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { repeat(3) { Box(Modifier.size(26.dp).clip(CircleShape).background(Color(0xFF202020))) } }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { repeat(3) { Box(Modifier.size(26.dp).clip(CircleShape).background(Color(0xFF202020))) } }
            Spacer(Modifier.height(22.dp)); Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun ColorDiscriminationChallenge(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    val hues = remember { listOf(8f, 48f, 112f, 176f, 224f, 292f) }
    val deltas = remember { listOf(28f, 22f, 17f, 13f, 10f, 8f) }
    val swatchDemo: @Composable () -> Unit = {
        val base = Color.hsv(hues[0], .58f, .86f)
        val different = Color.hsv((hues[0] + deltas[0]) % 360f, .58f, .86f)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(2) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(2) { column ->
                        val isTarget = row == 0 && column == 1
                        Box(Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(AppTheme.colors.surface), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(30.dp).clip(CircleShape).background(if (isTarget) different else base))
                        }
                    }
                }
            }
        }
    }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = if (howToIndex == 2) swatchDemo else null
        )
        return
    }
    if (!started) {
        IntroScreen(
            challenge,
            emptyList(),
            "This original hue game is not an Ishihara or validated colour-vision test. Display calibration, viewing angle and ambient light alter the score.",
            onClose,
            onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }
        ) {
            HowItWorksDemo("One tile's hue is slightly different from the other three — find and tap it.", swatchDemo)
        }
        return
    }
    if (round >= hues.size) {
        ResultScreen(challenge.title, "${accuracyPercent(correct, hues.size)}% accurate", "Use this only as a personal game score on this device. Occupational and clinical colour-vision decisions require validated plates under specified lighting.", onClose, details = { Spacer(Modifier.height(20.dp)); ResultMetric("Different hues found", "$correct / ${hues.size}") })
        return
    }
    val colors = AppTheme.colors
    val target = (round * 3 + 2) % 4
    val base = Color.hsv(hues[round], .58f, .86f)
    val different = Color.hsv((hues[round] + deltas[round]) % 360f, .58f, .86f)
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, "${round + 1}/${hues.size}")
        Spacer(Modifier.height(28.dp)); Text("Which hue is different?", color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(2) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(2) { column ->
                        val index = row * 2 + column
                        Box(Modifier.size(112.dp).clip(RoundedCornerShape(22.dp)).background(colors.surface).clickable { if (index == target) correct++; round++ }, contentAlignment = Alignment.Center) {
                            Box(Modifier.size(66.dp).clip(CircleShape).background(if (index == target) different else base))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IshiharaStyleCheck(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    val digits = remember { listOf(4, 7, 2, 9, 3, 6) }
    val choices = remember {
        listOf(
            listOf("4", "8", "1", "No number"),
            listOf("2", "7", "5", "No number"),
            listOf("2", "6", "3", "No number"),
            listOf("5", "9", "8", "No number"),
            listOf("8", "3", "5", "No number"),
            listOf("9", "5", "6", "No number")
        )
    }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = if (howToIndex == 2) { { PseudoisochromaticPlate(digit = 8, seed = 41) } } else null
        )
        return
    }
    if (!started) {
        IntroScreen(
            challenge,
            emptyList(),
            "These are newly generated Ishihara-style plates, not official Ishihara plates. Only a validated colour-plate test administered under specified lighting can assess colour vision.",
            onClose,
            onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }
        ) {
            HowItWorksDemo("Look for the number formed by the coloured dots — or choose \"No number\".") {
                PseudoisochromaticPlate(digit = 8, seed = 41)
            }
        }
        return
    }
    if (round >= digits.size) {
        ResultScreen(
            challenge.title,
            "$correct of ${digits.size} plates identified",
            if (correct == digits.size) "All generated plates were identified on this display. This does not rule out colour-vision deficiency."
            else "One or more generated plates were missed. Screen colour can cause this; request a validated colour-plate test if colour decisions matter or this concerns you.",
            onClose,
            if (correct == digits.size) AppTheme.colors.teal else AppTheme.colors.amber
        )
        return
    }
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, "${round + 1}/${digits.size}")
        Spacer(Modifier.height(12.dp))
        PseudoisochromaticPlate(digit = digits[round], seed = round + 41)
        Spacer(Modifier.height(18.dp))
        Text("What number do you see?", color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(2) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(2) { column ->
                        val choice = choices[round][row * 2 + column]
                        OutlinedButton(
                            onClick = { if (choice == digits[round].toString()) correct++; round++ },
                            modifier = Modifier.width(138.dp).height(50.dp)
                        ) { Text(choice, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PseudoisochromaticPlate(digit: Int, seed: Int) {
    val pairs = listOf(
        Color(0xFFE56F4A) to Color(0xFF74A85B),
        Color(0xFFC65D5B) to Color(0xFF8BA45A),
        Color(0xFFDC8250) to Color(0xFF64A07B),
        Color(0xFFD06A62) to Color(0xFF8D9E57),
        Color(0xFFE08B55) to Color(0xFF6A9A69),
        Color(0xFFC96B55) to Color(0xFF82A45F)
    )
    val dots = remember(digit, seed) {
        val random = Random(seed)
        buildList {
            while (size < 270) {
                val x = random.nextFloat() * .9f + .05f
                val y = random.nextFloat() * .9f + .05f
                val dx = x - .5f
                val dy = y - .5f
                if (dx * dx + dy * dy <= .225f) add(PlateDot(x, y, random.nextFloat() * .012f + .011f, digitContains(digit, x, y)))
            }
        }
    }
    val foreground = pairs[(seed - 41).coerceIn(0, pairs.lastIndex)].first
    val background = pairs[(seed - 41).coerceIn(0, pairs.lastIndex)].second
    Canvas(Modifier.size(292.dp).clip(CircleShape).background(Color(0xFFF2E7C9)).testTag("ishihara_style_plate")) {
        dots.forEachIndexed { index, dot ->
            val base = if (dot.inDigit) foreground else background
            val varied = if (index % 3 == 0) base.copy(alpha = .78f) else base
            drawCircle(varied, radius = dot.radius * size.width, center = androidx.compose.ui.geometry.Offset(dot.x * size.width, dot.y * size.height))
        }
    }
}

private fun digitContains(digit: Int, x: Float, y: Float): Boolean {
    val segments = when (digit) {
        2 -> setOf('a', 'b', 'g', 'e', 'd')
        3 -> setOf('a', 'b', 'g', 'c', 'd')
        4 -> setOf('f', 'g', 'b', 'c')
        6 -> setOf('a', 'f', 'g', 'e', 'c', 'd')
        7 -> setOf('a', 'b', 'c')
        9 -> setOf('a', 'f', 'b', 'g', 'c', 'd')
        else -> emptySet()
    }
    fun horizontal(yCenter: Float) = x in .32f..68f && y in (yCenter - .045f)..(yCenter + .045f)
    fun vertical(xCenter: Float, top: Float, bottom: Float) = x in (xCenter - .045f)..(xCenter + .045f) && y in top..bottom
    return ('a' in segments && horizontal(.20f)) ||
        ('g' in segments && horizontal(.50f)) ||
        ('d' in segments && horizontal(.80f)) ||
        ('f' in segments && vertical(.30f, .22f, .48f)) ||
        ('b' in segments && vertical(.70f, .22f, .48f)) ||
        ('e' in segments && vertical(.30f, .52f, .78f)) ||
        ('c' in segments && vertical(.70f, .52f, .78f))
}

@Composable
private fun CoverAlignmentCheck(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var eye by remember { mutableIntStateOf(0) }
    var complete by remember { mutableStateOf(false) }
    val movement = remember { mutableStateListOf(false, false) }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = when (howToIndex) {
                2 -> { { EyeCoverDiagram(coverLeft = true) } }
                3 -> { { EyeCoverDiagram(coverLeft = false) } }
                else -> null
            }
        )
        return
    }
    if (!started) {
        IntroScreen(
            challenge,
            emptyList(),
            "Clinical cover testing requires trained observation and may use prisms to measure movement. This helper check cannot rule out or quantify eye misalignment.",
            onClose,
            onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }
        ) {
            HowItWorksDemo("A helper covers one eye and watches the other for movement, then repeats on the other side.") {
                EyeCoverDiagram(coverLeft = true)
            }
        }
        return
    }
    if (complete) {
        val count = movement.count { it }
        ResultScreen(
            challenge.title,
            if (count == 0) "No movement reported" else "Movement reported",
            if (count == 0) "A helper did not notice movement today. Subtle or intermittent alignment differences can still be missed."
            else "An eye-care professional should repeat the cover test and interpret the movement. This observation cannot identify the type or amount of misalignment.",
            onClose,
            if (count == 0) AppTheme.colors.teal else AppTheme.colors.amber
        )
        return
    }
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, if (eye == 0) "COVER LEFT" else "COVER RIGHT")
        Spacer(Modifier.height(36.dp))
        Text("Keep both eyes aimed at", color = colors.textMedium, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        Box(Modifier.size(150.dp).clip(CircleShape).background(colors.surface), contentAlignment = Alignment.Center) {
            Text("★", color = colors.amber, fontSize = 68.sp)
        }
        Spacer(Modifier.height(32.dp))
        Text("Helper: did the uncovered eye move?", color = colors.textHigh, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Column(Modifier.padding(horizontal = 24.dp)) {
            PrimaryButton("It stayed still", onClick = { movement[eye] = false; if (eye == 0) eye = 1 else complete = true })
            OutlinedButton(onClick = { movement[eye] = true; if (eye == 0) eye = 1 else complete = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("It moved to the target") }
        }
    }
}

@Composable
private fun NearPointConvergenceLog(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var trial by remember { mutableIntStateOf(0) }
    var distance by remember { mutableStateOf(8f) }
    val readings = remember { mutableStateListOf<Int>() }
    val penTargetDemo: @Composable () -> Unit = { Text("✚", color = AppTheme.colors.amber, fontSize = 56.sp, fontWeight = FontWeight.Bold) }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = if (howToIndex == 1) penTargetDemo else null
        )
        return
    }
    if (!started) {
        IntroScreen(
            challenge,
            emptyList(),
            "Clinical NPC records both the subjective double point and objective eye movement, then the recovery point. Stop for pain, nausea or persistent double vision.",
            onClose,
            onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }
        ) {
            HowItWorksDemo("Move a pen target from arm's length toward your nose until your helper sees it double.", penTargetDemo)
        }
        return
    }
    if (trial >= 3) {
        val median = readings.sorted()[1]
        ResultScreen(
            challenge.title,
            "Median break point: $median cm",
            "Keep this as a technique-dependent baseline and share repeated or concerning results with an eye-care professional. It is not a convergence-insufficiency diagnosis.",
            onClose,
            if (median > 10) AppTheme.colors.amber else AppTheme.colors.teal,
            details = { Spacer(Modifier.height(20.dp)); ResultMetric("Three trials", readings.joinToString(" · ") { "$it cm" }) }
        )
        return
    }
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg)) {
        TestHeader(challenge.title, onClose, "TRIAL ${trial + 1}/3")
        Column(Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(35.dp))
            Text("✚", color = colors.amber, fontSize = 70.sp, fontWeight = FontWeight.Bold)
            Text("Enter the measured break distance", color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("${distance.roundToInt()} cm", color = colors.iris, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Slider(value = distance, onValueChange = { distance = it }, valueRange = 2f..30f, steps = 27, modifier = Modifier.fillMaxWidth())
            Text("Bridge of nose → target", color = colors.textMuted, fontSize = 12.sp)
            Spacer(Modifier.height(26.dp))
            PrimaryButton("Record trial", onClick = { readings.add(distance.roundToInt()); trial++; distance = 8f })
        }
    }
}

@Composable
private fun AmblyopiaPlayChallenge(challenge: VisionChallenge, age: Int, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var showingHowTo by remember { mutableStateOf(false) }
    var howToIndex by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    val symbols = remember { listOf("●", "▲", "■", "◆", "★") }
    val targetDemo: @Composable () -> Unit = { Text("★", color = AppTheme.colors.amber, fontSize = 48.sp) }
    val gridDemo: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(2) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(2) { column ->
                        val symbol = if (row == 0 && column == 1) "★" else symbols[(row * 2 + column) % symbols.size]
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(AppTheme.colors.surface), contentAlignment = Alignment.Center) {
                            Text(symbol, color = AppTheme.colors.textHigh, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = when (howToIndex) {
                1 -> targetDemo
                2 -> gridDemo
                else -> null
            }
        )
        return
    }
    if (!started) {
        IntroScreen(
            challenge,
            emptyList(),
            if (age < 18) "Amblyopia needs an eye examination and early professional treatment. This game does not decide which eye to patch or for how long; follow the child's existing treatment plan exactly."
            else "Amblyopia begins in childhood and treatment is usually less effective in adults. This is a visual-search game, not amblyopia therapy or a substitute for assessment.",
            onClose,
            onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }
        ) {
            HowItWorksDemo("Match the target symbol to the same symbol in the grid.", gridDemo)
        }
        return
    }
    if (round >= challenge.steps) {
        ResultScreen(
            challenge.title,
            "${accuracyPercent(correct, challenge.steps)}% accurate",
            "This score measures symbol-search accuracy only. It does not measure improvement in amblyopia or change an existing treatment plan.",
            onClose,
            AppTheme.colors.apricot,
            details = { Spacer(Modifier.height(20.dp)); ResultMetric("Matches found", "$correct / ${challenge.steps}") }
        )
        return
    }
    val colors = AppTheme.colors
    val targetIndex = (round * 2 + 1) % symbols.size
    val targetPosition = (round * 7 + 3) % 9
    val distractors = symbols.filterIndexed { index, _ -> index != targetIndex }
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, "${round + 1}/${challenge.steps}")
        Text("FIND", color = colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(symbols[targetIndex], color = colors.amber, fontSize = 64.sp)
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { column ->
                        val index = row * 3 + column
                        val symbol = if (index == targetPosition) symbols[targetIndex] else distractors[(index + round) % distractors.size]
                        Surface(color = colors.surface, border = androidx.compose.foundation.BorderStroke(1.dp, colors.border), shape = RoundedCornerShape(18.dp), modifier = Modifier.size(88.dp).clickable { if (index == targetPosition) correct++; round++ }) {
                            Box(contentAlignment = Alignment.Center) { Text(symbol, color = if (index == targetPosition) colors.textHigh else colors.textMedium, fontSize = 36.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContrastChallenge(challenge: VisionChallenge, onClose: () -> Unit) {
    var started by remember { mutableStateOf(false) }; var round by remember { mutableIntStateOf(0) }; var correct by remember { mutableIntStateOf(0) }
    var showingHowTo by remember { mutableStateOf(false) }; var howToIndex by remember { mutableIntStateOf(0) }
    val contrasts = remember { listOf(.72f, .62f, .54f, .46f, .39f, .33f, .27f, .22f, .18f, .14f) }
    val dotsDemo: @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(.30f, .55f, .30f, .30f).forEach { alpha -> Box(Modifier.size(46.dp).clip(CircleShape).background(AppTheme.colors.textHigh.copy(alpha = alpha))) }
        }
    }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; started = true } },
            visual = if (howToIndex == 2) dotsDemo else null
        )
        return
    }
    if (!started) {
        IntroScreen(challenge, emptyList(), "Screens differ in brightness, gamma, glare and colour. Compare scores only on the same device under the same conditions.", onClose, onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else started = true }) { HowItWorksDemo("One circle looks slightly stronger — more solid — than the other three.", dotsDemo) }; return
    }
    if (round >= contrasts.size) {
        val score = accuracyPercent(correct, contrasts.size)
        ResultScreen(challenge.title, "$score% spotted", "This is an on-device performance score, not clinical contrast sensitivity.", onClose, details = { Spacer(Modifier.height(20.dp)); ResultMetric("Correct", "$correct / ${contrasts.size}") }); return
    }
    val colors = AppTheme.colors; val target = (round * 3 + 1) % 4
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, "${round + 1}/${contrasts.size}"); Spacer(Modifier.height(24.dp))
        Text("Which circle is stronger?", color = colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(30.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(2) { row -> Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(2) { column ->
                    val index = row * 2 + column
                    Box(Modifier.size(112.dp).clip(RoundedCornerShape(22.dp)).background(colors.surface).clickable { if (index == target) correct++; round++ }, contentAlignment = Alignment.Center) {
                        Box(Modifier.size(58.dp).clip(CircleShape).background(colors.textHigh.copy(alpha = if (index == target) contrasts[round] else (contrasts[round] - .08f).coerceAtLeast(.04f))))
                    }
                }
            } }
        }
    }
}

@Composable
private fun PeripheralChallenge(challenge: VisionChallenge, onClose: () -> Unit) {
    var phase by remember { mutableIntStateOf(-1) }; var round by remember { mutableIntStateOf(0) }; var correct by remember { mutableIntStateOf(0) }
    var showingHowTo by remember { mutableStateOf(false) }; var howToIndex by remember { mutableIntStateOf(0) }
    val cue = (round * 3 + 1) % 4
    LaunchedEffect(round, phase) { if (phase == 0) { delay(700L + (round % 3) * 220L); phase = 1; delay(420L); phase = 2 } }
    val crossDemo: @Composable () -> Unit = { Text("+", color = AppTheme.colors.textHigh, fontSize = 34.sp, fontWeight = FontWeight.Light) }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; phase = 0 } },
            visual = when (howToIndex) {
                1 -> crossDemo
                2 -> { { DirectionPad {} } }
                else -> null
            }
        )
        return
    }
    if (phase == -1) {
        IntroScreen(challenge, emptyList(), "This small-screen attention task cannot test your visual field. Formal perimetry is needed to assess field loss.", onClose, onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else phase = 0 }) { HowItWorksDemo("Keep watching the centre cross; when a dot flashes near an edge, tap that direction.", crossDemo) }; return
    }
    if (phase == 3) {
        val score = accuracyPercent(correct, challenge.steps)
        ResultScreen(challenge.title, "$score% accurate", "Your score combines fixation, attention and response memory. It does not map the visual field.", onClose, details = { Spacer(Modifier.height(20.dp)); ResultMetric("Cues located", "$correct / ${challenge.steps}") }); return
    }
    val colors = AppTheme.colors; val alignments = listOf(Alignment.CenterEnd, Alignment.BottomCenter, Alignment.CenterStart, Alignment.TopCenter)
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, "${round + 1}/${challenge.steps}")
        Box(Modifier.fillMaxWidth().height(390.dp).padding(28.dp)) {
            Text("+", color = colors.textHigh, fontSize = 34.sp, fontWeight = FontWeight.Light, modifier = Modifier.align(Alignment.Center))
            if (phase == 1) Box(Modifier.size(24.dp).clip(CircleShape).background(colors.amber).align(alignments[cue]))
        }
        if (phase == 2) {
            Text("Where was the cue?", color = colors.textMedium, fontSize = 13.sp); Spacer(Modifier.height(10.dp))
            DirectionPad { selected -> if (selected == cue) correct++; if (round == challenge.steps - 1) phase = 3 else { round++; phase = 0 } }
        } else Text("Keep looking at +", color = colors.textMuted, fontSize = 13.sp)
    }
}

@Composable
private fun ReactionChallenge(challenge: VisionChallenge, onClose: () -> Unit) {
    var phase by remember { mutableIntStateOf(-1) }; var trial by remember { mutableIntStateOf(0) }; var onset by remember { mutableStateOf(0L) }
    var showingHowTo by remember { mutableStateOf(false) }; var howToIndex by remember { mutableIntStateOf(0) }
    val samples = remember { mutableStateListOf<Long>() }
    val alignments = listOf(Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd, Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd, Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd)
    LaunchedEffect(trial, phase) { if (phase == 0) { delay(650L + (trial * 173L) % 750L); onset = SystemClock.elapsedRealtime(); phase = 1 } }
    val targetDemo: @Composable () -> Unit = {
        Box(Modifier.size(56.dp).clip(CircleShape).background(AppTheme.colors.amber), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Bolt, null, tint = AppTheme.colors.bg, modifier = Modifier.size(24.dp))
        }
    }
    if (showingHowTo) {
        TestWalkthrough(
            steps = challenge.howTo,
            index = howToIndex,
            onBack = { if (howToIndex > 0) howToIndex-- else showingHowTo = false },
            onNext = { if (howToIndex < challenge.howTo.lastIndex) howToIndex++ else { showingHowTo = false; phase = 0 } },
            visual = if (howToIndex == 2) targetDemo else null
        )
        return
    }
    if (phase == -1) {
        IntroScreen(challenge, emptyList(), "Tap time includes attention, decision and finger movement. It is not a measure of retinal or neurological health.", onClose, onStart = { if (challenge.howTo.isNotEmpty()) { howToIndex = 0; showingHowTo = true } else phase = 0 }) { HowItWorksDemo("Tap the amber target the instant it appears, anywhere on the screen.", targetDemo) }; return
    }
    if (phase == 2) {
        val median = medianMillis(samples)
        ResultScreen(challenge.title, "${median ?: 0} ms median", "Use the median to compare your own sessions; device latency and hand position affect it.", onClose, details = { Spacer(Modifier.height(20.dp)); ResultMetric("Completed", "${samples.size} / ${challenge.steps}") }); return
    }
    val colors = AppTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg), horizontalAlignment = Alignment.CenterHorizontally) {
        TestHeader(challenge.title, onClose, "${trial + 1}/${challenge.steps}")
        Box(Modifier.fillMaxWidth().height(470.dp).padding(30.dp).testTag("reaction_field")) {
            if (phase == 1) {
                Box(Modifier.size(70.dp).clip(CircleShape).background(colors.amber).align(alignments[(trial * 7 + 2) % alignments.size]).clickable { samples.add(SystemClock.elapsedRealtime() - onset); if (trial == challenge.steps - 1) phase = 2 else { trial++; phase = 0 } }.testTag("reaction_target"), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Bolt, "Reaction target", tint = colors.bg, modifier = Modifier.size(28.dp))
                }
            } else Text("Wait…", color = colors.textMuted, fontSize = 14.sp, modifier = Modifier.align(Alignment.Center))
        }
    }
}
