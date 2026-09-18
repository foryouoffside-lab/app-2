package com.example.ui.drill

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExercisePhase
import com.example.model.Protocol
import com.example.model.StudioDrill
import com.example.model.DrillGuidance
import com.example.model.StudioDrillRepository
import com.example.model.StudioStimulus
import com.example.model.guidance
import com.example.model.TargetFunction
import com.example.model.prefersLandscape
import com.example.model.targetFunction
import com.example.ui.components.TargetSpeedBar
import com.example.ui.theme.AppTheme
import com.example.util.SensoryFeedbackManager
import com.example.util.DEFAULT_TARGET_SPEED
import com.example.util.TargetColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Seconds of tutorial before the drill itself starts. */
private const val INTRO_SECONDS = 6

/**
 * One close-squeeze-open cycle, per the blink trial protocol.
 *
 * Only a fallback now: every drill carries its own cycle length on its evidence record,
 * and this is what a protocol without one runs at.
 */
const val BLINK_CYCLE_SECONDS = 6

/** Bounds on a user-set drill length: enough to be useful, not enough to strain. */
const val MIN_REPS = 5
const val MAX_REPS = 30

/**
 * The stepper range for a drill whose own dose is [doseReps].
 *
 * The floor exists so nobody sets a uselessly short blink set, but it must never push a
 * drill above its own prescribed dose: one 20-second look-away is the whole 20-20-20
 * rule, and clamping that up to five reps would run 100 seconds of a drill the trial
 * defined as 20.
 */
fun repRange(doseReps: Int): IntRange =
    minOf(MIN_REPS, doseReps)..maxOf(MAX_REPS, doseReps)

private enum class Stage { INTRO, PREP, ACTIVE, DONE }

/**
 * How long the session chrome stays up before a full-screen drill clears itself.
 *
 * Long enough to read the timer, short enough that the user is not tracking a moving
 * target across a panel. One tap anywhere brings it all back.
 */
private const val CONTROLS_IDLE_MS = 4_000L

/**
 * How often a non-visual cue repeats itself while nothing about it has changed.
 *
 * A 20-second look-away has one instruction and no phases, so without this the user hears
 * it once and then has no idea how far through they are.
 */
private const val CUE_REPEAT_MS = 5_000L

/**
 * How long a drill is, said the way a workout app says it: "15 Reps" when there are reps
 * to count, a clock when the whole drill is one held interval.
 */
fun repLabel(reps: Int, cycleSeconds: Int): String =
    if (reps > 1) "$reps Reps"
    else (reps * cycleSeconds).let { String.format("%02d:%02d", it / 60, it % 60) }

/** "90s" under a minute, "2m 30s" above it. */
fun formatLength(seconds: Int): String =
    if (seconds < 60) "${seconds}s"
    else "${seconds / 60}m ${seconds % 60}s".removeSuffix(" 0s")

/**
 * The drill environment: a tutorial that explains and demonstrates, then the drill.
 *
 * Layout is an outer session frame holding an inner drill box, so the drill's own
 * visual and cue stay visually separate from the session chrome around them.
 */
@Composable
fun DrillScreen(
    protocol: Protocol,
    voiceDefault: Boolean = true,
    hapticsEnabled: Boolean = true,
    targetColor: TargetColor = TargetColor.AMBER,
    targetSpeed: Float = DEFAULT_TARGET_SPEED,
    onTargetSpeedChange: (Float) -> Unit = {},
    onClose: () -> Unit,
    onCompleted: (protocolId: String, duration: Int) -> Unit
) {
    val context = LocalContext.current
    val targetStyle = remember(targetColor, targetSpeed) { TargetStyle.from(targetColor, targetSpeed) }

    // A drill is the whole point of the screen, so the system bars come off for it. The
    // insets are restored on the way out, including when the drill is abandoned.
    val view = LocalView.current
    val activity = context as? Activity
    DisposableEffect(Unit) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            // Only ever set by the rotate gate, but restored unconditionally: leaving a
            // drill must never leave the rest of the app pinned sideways.
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        }
    }
    val coach = remember { SensoryFeedbackManager(context) }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    var stage by remember { mutableStateOf(Stage.INTRO) }
    // True once the user has stepped through every how-to page and there is nothing left
    // to read: the rotate ask is the last page, not a wall in front of the first one.
    var awaitingRotate by remember { mutableStateOf(false) }

    // The how-to (with its demo video) is written and shot for portrait, so it stays put
    // however the phone is held while it's on screen. Rotation only opens up once there is
    // nothing left to read -- either the drill is already running, or we're on the rotate
    // ask itself, which needs the sensor free to react to the turn.
    LaunchedEffect(stage, awaitingRotate) {
        activity?.requestedOrientation = if (awaitingRotate || stage == Stage.ACTIVE || stage == Stage.DONE) {
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    var phaseIndex by remember { mutableIntStateOf(0) }
    val phase: ExercisePhase = protocol.phases[phaseIndex]
    var secondsLeft by remember(phaseIndex) { mutableIntStateOf(phase.durationSeconds) }
    // One clock for the running drill. The animation and the spoken cue are both derived
    // from it, so the voice cannot drift out of step with the lid.
    var elapsedMs by remember(phaseIndex) { mutableFloatStateOf(0f) }
    var introLeft by remember { mutableIntStateOf(INTRO_SECONDS) }
    // Setup the app cannot cue frame by frame -- what to fetch, how to heat it, which
    // finger. Walked one step at a time before the timer starts, because reading it off
    // an evidence sheet afterwards is no use to someone holding a warm flannel.
    var prepIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var voiceOn by remember { mutableStateOf(voiceDefault) }
    // Null for a drill with no evidence record; the evidence button hides itself then.
    val evidence = remember(protocol.evidenceId) { protocol.evidenceId?.let(StudioDrillRepository::byId) }
    var showEvidence by remember { mutableStateOf(false) }
    // Each drill paces itself. A 20-second look-away and a 4-second saccade switch are
    // both one rep, so the cycle has to come off the drill rather than a shared constant.
    val cycleSeconds = evidence?.dose?.cycleSeconds ?: BLINK_CYCLE_SECONDS
    val stimulus = evidence?.stimulus ?: StudioStimulus.BLINK
    // Decides whether the drill is guided by the screen or by sound, voice and vibration.
    val guidance = evidence?.guidance ?: DrillGuidance.NON_VISUAL
    // Length the user picked on the intro. Prebuilt sets keep the dose they shipped with.
    val repBounds = repRange(evidence?.dose?.reps ?: MIN_REPS)
    var reps by remember(cycleSeconds) {
        mutableIntStateOf((phase.durationSeconds / cycleSeconds).coerceIn(repBounds))
    }
    val plannedSeconds =
        if (protocol.userAdjustable) reps * cycleSeconds else phase.durationSeconds
    val prepSteps = evidence?.howTo.orEmpty()
    // What to have in your hand before the timer runs. Drills that need nothing say
    // "None", and a "You'll need: None" line is worse than no line at all.
    val needsKit = evidence?.equipment?.takeUnless { it.startsWith("None", ignoreCase = true) }
    // A drill whose target has only the short edge to travel is worth turning the phone
    // for, and asking is the whole of it -- the app cannot turn the phone for the user.
    val mustRotate = !landscape && stimulus.prefersLandscape

    // One switch for everything audible. The tones are cues, not decoration, so muting
    // the coach has to mute them too.
    coach.voiceEnabled = voiceOn
    coach.soundEnabled = voiceOn
    coach.hapticsEnabled = hapticsEnabled

    DisposableEffect(Unit) {
        coach.initVoice()
        onDispose { coach.release() }
    }

    BackHandler {
        coach.stopVoice()
        when {
            showEvidence -> showEvidence = false
            awaitingRotate -> awaitingRotate = false
            stage != Stage.PREP -> onClose()
            prepIndex > 0 -> prepIndex--
            else -> stage = Stage.INTRO
        }
    }

    /**
     * Reached from the last how-to page (or straight from Start when there is no how-to).
     * A drill that needs the long edge gets one more page -- the rotate ask -- instead of
     * starting; everything else runs immediately.
     */
    fun run() {
        if (mustRotate && !landscape) {
            awaitingRotate = true
            return
        }
        coach.stopVoice()
        secondsLeft = plannedSeconds
        elapsedMs = 0f
        stage = Stage.ACTIVE
    }

    // The turn itself is the confirmation: once the sensor reports landscape there is
    // nothing left to ask for, so the drill starts on its own rather than making the user
    // find a Start button while holding the phone sideways.
    LaunchedEffect(landscape, awaitingRotate) {
        if (awaitingRotate && landscape) {
            awaitingRotate = false
            run()
        }
    }

    /** Start pressed: walk the setup first where there is one, otherwise straight in. */
    fun begin() {
        coach.stopVoice()
        if (prepSteps.isEmpty()) run() else { prepIndex = 0; stage = Stage.PREP }
    }

    // Tutorial: explain once, and every line waits for the one before it to finish.
    // An adjustable drill then waits for Start so the length can actually be set; a fixed
    // set counts itself in, but only once there is silence to count into.
    LaunchedEffect(stage) {
        if (stage != Stage.INTRO) return@LaunchedEffect
        delay(250)
        // The engine is still warming up on a cold start; speaking before it is ready
        // drops the line silently.
        coach.awaitVoiceReady()
        // One script, so pressing Start part-way through abandons the rest of it rather
        // than reading the next line over the drill.
        coach.speakSequence(
            phase.title,
            if (mustRotate) "This one needs the long edge. I will ask you to turn once the how-to is done." else "",
            if (needsKit != null) "You will need $needsKit." else "",
            if (guidance == DrillGuidance.NON_VISUAL) {
                "Your eyes will be off the screen, so I will call every step out loud."
            } else {
                "Watch the screen and follow the target. I will stay quiet while you do."
            },
            when {
                prepSteps.isNotEmpty() -> "Press start and I will talk you through the setup."
                protocol.userAdjustable -> "Set your reps, then press start."
                else -> ""
            }
        )
        if (protocol.userAdjustable) return@LaunchedEffect
        while (introLeft > 0 && stage == Stage.INTRO) {
            if (introLeft <= 3) coach.speak(introLeft.toString(), interrupt = true)
            delay(1000)
            introLeft--
        }
        coach.speak("Begin", interrupt = true)
        begin()
    }

    // Read aloud, because the whole point of the walkthrough is that your hands are busy
    // with a flannel or a pencil and your eyes are not on the phone.
    LaunchedEffect(stage, prepIndex) {
        if (stage != Stage.PREP) return@LaunchedEffect
        coach.awaitVoiceReady()
        prepSteps.getOrNull(prepIndex)?.let { coach.speak(it, interrupt = true) }
    }

    // Frame-driven so the lid position and the countdown come from the same elapsed time.
    LaunchedEffect(stage, phaseIndex, isPaused) {
        if (stage != Stage.ACTIVE || isPaused) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (elapsedMs < plannedSeconds * 1000f) {
            withFrameNanos { now ->
                elapsedMs += (now - last) / 1_000_000f
                last = now
            }
            secondsLeft = (plannedSeconds - (elapsedMs / 1000f).toInt()).coerceAtLeast(0)
        }
        if (phaseIndex < protocol.phases.lastIndex) {
            phaseIndex++
        } else {
            coach.speak("Done.", interrupt = true)
            coach.triggerCompletionHaptic()
            stage = Stage.DONE
            onCompleted(protocol.id, protocol.totalSeconds)
        }
    }

    val cycleProgress = (elapsedMs / (cycleSeconds * 1000f)) % 1f
    // The blink drill names its three lid phases; every other stimulus has one standing
    // instruction, so it is spoken once a cycle rather than on every frame.
    val liveCue = when {
        stage != Stage.ACTIVE || isPaused -> null
        stimulus == StudioStimulus.BLINK -> blinkStageFor(cycleProgress).label
        else -> stimulusCue(stimulus, cycleProgress)
    }
    val repIndex = (elapsedMs / (cycleSeconds * 1000f)).toInt()

    // A drill the user is watching gets no voice at all: the animation is the cue, and
    // talking over it competes with the thing they are meant to be looking at. A drill
    // done with the eyes shut or turned away gets the opposite treatment, because nothing
    // on the display can reach them -- voice on every change, a tone and a pulse to mark
    // it, and the line repeated while it still stands.
    LaunchedEffect(liveCue, guidance) {
        if (guidance != DrillGuidance.NON_VISUAL) return@LaunchedEffect
        val cue = liveCue ?: return@LaunchedEffect
        while (true) {
            coach.speak(cue, interrupt = true)
            coach.triggerPhaseHaptic()
            launch { coach.playChimeTone(frequency = cueTone(cue), durationMs = 160) }
            delay(CUE_REPEAT_MS)
        }
    }

    // A watched drill runs edge to edge: the target wants the whole display, and the
    // session chrome is what goes over it rather than the frame it sits inside.
    val immersive = guidance == DrillGuidance.VISUAL && stage == Stage.ACTIVE
    var controlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(immersive, controlsVisible, landscape, isPaused) {
        if (immersive && controlsVisible && !isPaused) {
            delay(CONTROLS_IDLE_MS)
            controlsVisible = false
        }
    }

    // Turning the phone is deliberate -- it was done to give the target room -- so the
    // screen clears to the stimulus and the instruction is spoken rather than read. The
    // user's eyes are on a moving target at this point; they cannot also be on a panel.
    var wasLandscape by remember { mutableStateOf(landscape) }
    LaunchedEffect(landscape) {
        if (landscape == wasLandscape) return@LaunchedEffect
        wasLandscape = landscape
        // Rotating while the how-to is still up isn't "deliberate" in that sense -- it's
        // the rotate ask itself, or an accidental turn while portrait is locked -- so only
        // the running drill gets this announcement.
        if (stage != Stage.ACTIVE) return@LaunchedEffect
        controlsVisible = false
        coach.speak(
            "${phase.instruction}. Tap the screen for your controls.",
            interrupt = true
        )
    }

    val elapsed = phase.durationSeconds - secondsLeft
    val running = stage == Stage.ACTIVE && !isPaused
    // The speed bar only belongs to a drill that actually moves something.
    val movingTarget = stimulus.targetFunction != TargetFunction.NONE

    // Everything that is not the stimulus. Held as one block so portrait can stack it
    // under the drill, landscape can put it beside, and a full-screen drill can float it
    // over the top, without duplicating any of it.
    val panel: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = phase.title,
                color = AppTheme.colors.textHigh,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            val subtitle =
                if (stage == Stage.INTRO) needsKit?.let { "You'll need: $it" }
                else "Rep ${(elapsed / cycleSeconds) + 1} of ${plannedSeconds / cycleSeconds}"
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = AppTheme.colors.textMedium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (stage == Stage.INTRO) {
                IntroFooter(
                    adjustable = protocol.userAdjustable,
                    cycleSeconds = cycleSeconds,
                    bounds = repBounds,
                    reps = reps,
                    secondsLeft = introLeft,
                    hasSetup = prepSteps.isNotEmpty(),
                    onReps = { reps = it.coerceIn(repBounds) },
                    onStart = ::begin
                )
            } else {
                Text(
                    text = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60),
                    color = AppTheme.colors.textHigh,
                    fontSize = if (landscape) 40.sp else 52.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                TransportControls(
                    isPaused = isPaused,
                    progress = elapsed.toFloat() / phase.durationSeconds.coerceAtLeast(1),
                    hasPrevious = phaseIndex > 0,
                    onPrevious = { if (phaseIndex > 0) phaseIndex-- },
                    onToggle = {
                        if (!isPaused) coach.stopVoice()
                        isPaused = !isPaused
                    },
                    onNext = {
                        coach.stopVoice()
                        if (phaseIndex < protocol.phases.lastIndex) phaseIndex++ else onClose()
                    }
                )
            }

            // Tuned here rather than only in Settings: how fast is too fast is only
            // answerable while the target is actually moving.
            if (movingTarget) {
                Spacer(modifier = Modifier.height(14.dp))
                TargetSpeedBar(
                    speed = targetSpeed,
                    onSpeedChange = onTargetSpeedChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
    }

    val box: @Composable (Modifier) -> Unit = { boxModifier ->
        DrillBox(
            isIntro = stage == Stage.INTRO,
            isRunning = running || stage == Stage.INTRO,
            drivenProgress = if (stage == Stage.ACTIVE) cycleProgress else null,
            stimulus = stimulus,
            cycleSeconds = cycleSeconds,
            style = targetStyle,
            compact = landscape,
            modifier = boxModifier
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { controlsVisible = !controlsVisible }
    ) {
        // The drill itself, with nothing between it and the edges of the display.
        if (immersive) {
            StimulusView(
                stimulus = stimulus,
                cycle = cycleProgress,
                isRunning = running,
                style = targetStyle,
                modifier = Modifier.fillMaxSize().padding(10.dp)
            )
        }

        // Hidden entirely while a full-screen drill runs untouched; one tap restores it.
        if (!immersive || controlsVisible) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = if (landscape) 24.dp else 20.dp, vertical = 10.dp)
            ) {
                SessionTopBar(
                    onEvidence = evidence?.let { { showEvidence = true } },
                    voiceOn = voiceOn,
                    onToggleVoice = {
                        voiceOn = !voiceOn
                        if (!voiceOn) coach.stopVoice()
                    },
                    onClose = {
                        coach.stopVoice()
                        onClose()
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (awaitingRotate) {
                    // The last page: everything else has been read, this is the one thing
                    // left standing between here and the drill.
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        RotateGate {
                            activity?.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }
                    }
                } else if (stage == Stage.PREP) {
                    PrepSteps(
                        steps = prepSteps,
                        images = evidence?.howToImages.orEmpty(),
                        videos = evidence?.howToVideos.orEmpty(),
                        index = prepIndex,
                        onBack = { if (prepIndex > 0) prepIndex-- else stage = Stage.INTRO },
                        onSkip = { coach.stopVoice(); run() },
                        onNext = { if (prepIndex < prepSteps.lastIndex) prepIndex++ else run() },
                        modifier = Modifier.weight(1f)
                    )
                } else if (immersive) {
                    // The stimulus is already behind this, so the panel is all that needs room:
                    // it floats at the bottom on its own ground rather than taking space away.
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = liveCue ?: stimulusCue(stimulus, cycleProgress),
                        color = AppTheme.colors.textHigh,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppTheme.colors.surface.copy(alpha = 0.94f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("drill_cue")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = AppTheme.colors.surface.copy(alpha = 0.94f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) { panel() }
                    }
                } else if (landscape) {
                    // Turning the phone is what gives a tracking drill room to actually move, so
                    // landscape hands the stimulus the whole left side rather than letterboxing it.
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        box(Modifier.weight(1.45f).fillMaxHeight())
                        Spacer(modifier = Modifier.width(20.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) { panel() }
                        }
                    }
                } else {
                    box(Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(18.dp))
                    panel()
                }
            }
        }
    }

    if (showEvidence && evidence != null) {
        DrillSheet(evidence) { showEvidence = false }
    }
}

/**
 * A pitch per cue, so the three blink steps are told apart by ear alone.
 *
 * Rising through the cycle matches what the lid is doing, which is the only feedback
 * available with the eyes shut.
 */
private fun cueTone(cue: String): Double = when (cue) {
    "Close" -> 392.0
    "Squeeze" -> 330.0
    "Open" -> 523.0
    else -> 432.0
}

/**
 * The drill box. Everything the drill itself owns lives inside this surface; session
 * chrome stays outside it.
 */
@Composable
private fun DrillBox(
    isIntro: Boolean,
    isRunning: Boolean,
    drivenProgress: Float?,
    stimulus: StudioStimulus,
    cycleSeconds: Int,
    style: TargetStyle,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val cycleMillis = cycleSeconds * 1000
    // During the drill the cycle comes from the session clock; the intro demo runs free.
    val demo = pausableFloat(
        isPaused = !isRunning,
        from = 0f,
        to = 1f,
        periodMillis = cycleMillis,
        reverse = false,
        easing = LinearEasing
    )
    val cycle = drivenProgress ?: demo
    val cueWord =
        if (stimulus == StudioStimulus.BLINK) blinkStageFor(cycle).label
        else stimulusCue(stimulus, cycle)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = AppTheme.colors.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isIntro && !compact) {
                Text(
                    text = "HOW IT WORKS",
                    color = AppTheme.colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // The stimulus takes every pixel the box can spare. It used to be pinned to a
            // 240dp strip, which is why the drill sat in a sea of empty surface.
            StimulusView(
                stimulus = stimulus,
                cycle = cycle,
                isRunning = isRunning,
                style = style,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = cueWord,
                color = AppTheme.colors.textHigh,
                fontSize = if (compact) 18.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("drill_cue")
            )
        }
    }
}

/**
 * The drill's stimulus and nothing else, drawn to fill whatever it is handed.
 *
 * Shared by the boxed layout and the full-screen one, so a drill looks the same in both
 * and there is only one place a stimulus can be got wrong.
 */
@Composable
private fun StimulusView(
    stimulus: StudioStimulus,
    cycle: Float,
    isRunning: Boolean,
    style: TargetStyle,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (stimulus == StudioStimulus.BLINK) {
            val (openness, squeeze) = blinkCycleState(cycle)
            BoxWithConstraints {
                BlinkingEye(
                    openness = openness,
                    squeeze = squeeze,
                    irisColor = style.color,
                    lidColor = AppTheme.colors.textHigh,
                    size = minOf(maxWidth, maxHeight) * 0.92f
                )
            }
        } else {
            StimulusCanvas(stimulus, cycle, isRunning, style)
        }
    }
}

@Composable
private fun IntroFooter(
    adjustable: Boolean,
    cycleSeconds: Int,
    bounds: IntRange,
    reps: Int,
    secondsLeft: Int,
    hasSetup: Boolean,
    onReps: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (adjustable) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StepperButton("−", enabled = reps > bounds.first) { onReps(reps - 1) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$reps reps",
                        color = AppTheme.colors.textHigh,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("reps_value")
                    )
                    Text(
                        text = formatLength(reps * cycleSeconds),
                        color = AppTheme.colors.textMuted,
                        fontSize = 13.sp
                    )
                }
                StepperButton("+", enabled = reps < bounds.last) { onReps(reps + 1) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text(
                text = "Starting in $secondsLeft",
                color = AppTheme.colors.amber,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        Text(
            text = if (adjustable) (if (hasSetup) "Start — how to do it" else "Start") else "Skip intro",
            color = if (adjustable) AppTheme.colors.bg else AppTheme.colors.textMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (adjustable) AppTheme.colors.amber else AppTheme.colors.surface)
                .clickable(onClick = onStart)
                .padding(horizontal = 40.dp, vertical = 14.dp)
                .testTag("start_drill")
        )
    }
}

/**
 * The ask to turn the phone, for a drill that has no room to move in portrait.
 *
 * The escape hatch matters as much as the ask. With the system rotation lock on, turning
 * the phone does precisely nothing, and a gate with no way through would leave those
 * users unable to start the drill at all.
 */
@Composable
private fun RotateGate(onRotate: () -> Unit) {
    val angle = pausableFloat(
        isPaused = false,
        from = 0f,
        to = -90f,
        periodMillis = 1500,
        easing = FastOutSlowInEasing
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().testTag("rotate_hint")
    ) {
        Box(
            modifier = Modifier
                .size(width = 54.dp, height = 90.dp)
                .rotate(angle)
                .border(2.dp, AppTheme.colors.amber, RoundedCornerShape(11.dp))
        )
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = "Turn your phone sideways",
            color = AppTheme.colors.textHigh,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "This drill needs the long edge to move across.",
            color = AppTheme.colors.textMedium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Rotation locked? Turn it for me",
            color = AppTheme.colors.amber,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.surface)
                .clickable(onClick = onRotate)
                .padding(horizontal = 22.dp, vertical = 12.dp)
                .testTag("force_landscape")
        )
    }
}

/**
 * The setup, one step at a time, spoken as it appears.
 *
 * One step per screen rather than a numbered list: this is read with a warm flannel in
 * one hand, and a wall of text is read by nobody in that position. The voice carries it,
 * so the screen only has to hold the place.
 */
@Composable
private fun PrepSteps(
    steps: List<String>,
    images: List<Int?>,
    videos: List<Int?>,
    index: Int,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val last = index >= steps.lastIndex
    val image = images.getOrNull(index)
    val video = videos.getOrNull(index)
    Column(
        modifier = modifier.fillMaxWidth().testTag("prep_steps"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HOW TO DO IT",
            color = AppTheme.colors.teal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            steps.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (i <= index) AppTheme.colors.teal else AppTheme.colors.border
                        )
                )
            }
        }

        // Scrollable on its own: the video is sized by its real aspect ratio now rather
        // than however much weighted space happened to be left over, and a portrait clip
        // at full width can run tall enough that the instruction text needs to be able to
        // scroll into view below it instead of getting squeezed against the buttons.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (video != null) {
                LoopingStepVideo(
                    resId = video,
                    modifier = Modifier
                        .fillMaxWidth()
                        // VideoView has no equivalent of ContentScale.Fit -- it stretches
                        // to whatever box it's given, so the box has to be pinned to the
                        // clip's own 4:5 shape itself rather than left to a weight share.
                        .aspectRatio(4f / 5f)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("prep_step_video")
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else if (image != null) {
                Image(
                    painter = painterResource(image),
                    contentDescription = steps.getOrElse(index) { "" },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("prep_step_image")
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Text(
                    text = "${index + 1}",
                    color = AppTheme.colors.teal.copy(alpha = .30f),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            Text(
                text = steps.getOrElse(index) { "" },
                color = AppTheme.colors.textHigh,
                fontSize = 19.sp,
                lineHeight = 29.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("prep_step_text")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Back",
                color = AppTheme.colors.textHigh,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.surface)
                    .clickable(onClick = onBack)
                    .padding(vertical = 16.dp)
                    .testTag("prep_back")
            )
            Text(
                text = "Skip",
                color = AppTheme.colors.textMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.surface)
                    .clickable(onClick = onSkip)
                    .padding(vertical = 16.dp)
                    .testTag("prep_skip")
            )
            Text(
                text = if (last) "Start" else "Next",
                color = if (last) AppTheme.colors.bg else AppTheme.colors.textHigh,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (last) AppTheme.colors.amber else AppTheme.colors.surface)
                    .clickable(onClick = onNext)
                    .padding(vertical = 16.dp)
                    .testTag("prep_next")
            )
        }
    }
}

/**
 * A muted, looping demo clip for one prep step. Keyed on [resId] so moving between
 * steps tears down the old player instead of re-pointing it mid-playback.
 */
@Composable
private fun LoopingStepVideo(resId: Int, modifier: Modifier = Modifier) {
    key(resId) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(Uri.parse("android.resource://${context.packageName}/$resId"))
                    setOnPreparedListener { player ->
                        player.isLooping = true
                        player.setVolume(0f, 0f)
                        // Starting here, once the player has actually confirmed it's ready,
                        // instead of firing start() right after setVideoURI(): calling it
                        // before preparation completes is a race that can silently leave
                        // the view parked on frame one, no error, no visible failure.
                        start()
                    }
                    setOnErrorListener { _, _, _ -> true }
                }
            },
            onRelease = { it.stopPlayback() }
        )
    }
}

@Composable
private fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (enabled) AppTheme.colors.textHigh else AppTheme.colors.textMuted,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(top = 9.dp)
    )
}

@Composable
private fun SessionTopBar(
    onEvidence: (() -> Unit)?,
    voiceOn: Boolean,
    onToggleVoice: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(42.dp).clip(CircleShape).background(AppTheme.colors.surface)
        ) {
            Icon(Icons.Default.Close, "Close drill", tint = AppTheme.colors.textMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (onEvidence != null) IconButton(
            onClick = onEvidence,
            modifier = Modifier.size(42.dp).clip(CircleShape).background(AppTheme.colors.surface)
                .testTag("evidence_button")
        ) {
            Icon(Icons.Default.Science, "Show the evidence for this drill", tint = AppTheme.colors.amber)
        }
        IconButton(
            onClick = onToggleVoice,
            modifier = Modifier.size(42.dp).clip(CircleShape).background(AppTheme.colors.surface)
        ) {
            Icon(
                imageVector = if (voiceOn) Icons.Default.RecordVoiceOver else Icons.Default.VoiceOverOff,
                contentDescription = "Toggle voice",
                tint = if (voiceOn) AppTheme.colors.amber else AppTheme.colors.textMuted
            )
        }
        }
    }
}

@Composable
private fun TransportControls(
    isPaused: Boolean,
    progress: Float,
    hasPrevious: Boolean,
    onPrevious: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit
) {
    val fill by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = LinearEasing),
        label = "drill_fill"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            enabled = hasPrevious,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(AppTheme.colors.surface)
        ) {
            Icon(
                Icons.Default.SkipPrevious, "Previous",
                tint = if (hasPrevious) AppTheme.colors.textMedium else AppTheme.colors.textMuted
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AppTheme.colors.surface)
                .clickable(onClick = onToggle)
                .testTag("transport_toggle"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fill)
                    .fillMaxHeight()
                    .background(AppTheme.colors.amber.copy(alpha = 0.30f))
            )
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Resume" else "Pause",
                tint = AppTheme.colors.amberGlow,
                modifier = Modifier.size(30.dp)
            )
        }
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(AppTheme.colors.surface)
        ) {
            Icon(Icons.Default.SkipNext, "Next", tint = AppTheme.colors.textMedium)
        }
    }
}

/**
 * A value that ramps between [from] and [to] and actually stops when [isPaused].
 * Compose's infiniteRepeatable cannot be paused.
 */
@Composable
fun pausableFloat(
    isPaused: Boolean,
    from: Float,
    to: Float,
    periodMillis: Int,
    reverse: Boolean = true,
    easing: Easing = FastOutSlowInEasing
): Float {
    var phase by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPaused, periodMillis) {
        if (isPaused) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                phase = (phase + (now - last) / 1_000_000f / periodMillis) % 1f
                last = now
            }
        }
    }
    val t = if (reverse) (phase * 2f).let { if (it <= 1f) it else 2f - it } else phase
    return from + (to - from) * easing.transform(t.coerceIn(0f, 1f))
}
