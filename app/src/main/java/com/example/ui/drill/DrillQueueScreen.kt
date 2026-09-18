package com.example.ui.drill

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Protocol
import com.example.model.StudioDrillRepository
import com.example.ui.theme.AppTheme
import com.example.util.DEFAULT_TARGET_SPEED
import com.example.util.SensoryFeedbackManager
import com.example.util.TargetColor
import kotlinx.coroutines.delay

/**
 * How long the coach gets to explain the next drill before it starts on its own.
 *
 * Long enough to say the drill's name, what to do and what to have ready; short enough
 * that it never feels like the wait is the point.
 */
const val NEXT_UP_SECONDS = 28

/**
 * A set of drills, run back to back.
 *
 * A single-drill queue behaves exactly like handing [DrillScreen] its one [Protocol]
 * directly -- no "next up" pause ever shows, because there is never a next drill to
 * explain. A multi-drill queue (today's personalized set, or a saved custom routine)
 * inserts a spoken explanation of what's coming between one drill's finish and the next
 * one's start, so nobody has to guess what a drill they haven't seen yet is asking for.
 */
@Composable
fun DrillQueueScreen(
    queue: List<Protocol>,
    voiceDefault: Boolean = true,
    hapticsEnabled: Boolean = true,
    skipInstructions: Boolean = false,
    targetColor: TargetColor = TargetColor.AMBER,
    targetSpeed: Float = DEFAULT_TARGET_SPEED,
    onTargetSpeedChange: (Float) -> Unit = {},
    onClose: () -> Unit,
    onDrillCompleted: (protocolId: String, duration: Int) -> Unit,
    onQueueFinished: () -> Unit
) {
    var index by remember(queue) { mutableIntStateOf(0) }
    var showingNextUp by remember(queue) { mutableStateOf(false) }
    val current = queue.getOrNull(index)
    if (current == null) {
        onQueueFinished()
        return
    }

    if (showingNextUp) {
        val next = queue.getOrNull(index + 1)
        if (next == null) {
            onQueueFinished()
            return
        }
        NextUpScreen(
            next = next,
            voiceEnabled = voiceDefault,
            onContinue = { showingNextUp = false; index++ }
        )
    } else {
        // Keyed on the queue position so stepping to a sibling drill -- whether via the
        // "next up" hand-off above or a direct manual Previous/Next tap below -- always
        // gets a fresh DrillScreen instance. Without the key, Compose would just recompose
        // the existing one in place and its remembered stage/phase/timer state (none of it
        // keyed to the protocol) would carry over from the drill that just finished.
        key(index) {
            DrillScreen(
                protocol = current,
                voiceDefault = voiceDefault,
                hapticsEnabled = hapticsEnabled,
                skipInstructions = skipInstructions,
                targetColor = targetColor,
                targetSpeed = targetSpeed,
                onTargetSpeedChange = onTargetSpeedChange,
                hasPreviousDrill = index > 0,
                hasNextDrill = index < queue.lastIndex,
                onPreviousDrill = { index-- },
                onNextDrill = { index++ },
                onClose = onClose,
                onCompleted = { protocolId, duration ->
                    onDrillCompleted(protocolId, duration)
                    if (index < queue.lastIndex) showingNextUp = true else onQueueFinished()
                }
            )
        }
    }
}

/**
 * The coach's word between two drills: what's coming, what to do, what to have ready.
 *
 * Read off the same fields the drill itself uses -- the first phase's instruction and
 * benefit, and the evidence record's kit -- so this can never say something the drill
 * that follows it doesn't back up.
 */
@Composable
private fun NextUpScreen(
    next: Protocol,
    voiceEnabled: Boolean,
    onContinue: () -> Unit
) {
    HideSystemBarsWhileMounted()
    val context = LocalContext.current
    val coach = remember { SensoryFeedbackManager(context) }
    coach.voiceEnabled = voiceEnabled
    coach.soundEnabled = voiceEnabled
    DisposableEffect(Unit) {
        coach.initVoice()
        onDispose { coach.release() }
    }

    val evidence = remember(next.evidenceId) { next.evidenceId?.let(StudioDrillRepository::byId) }
    val phase = next.phases.firstOrNull()
    val kit = evidence?.equipment?.takeUnless { it.startsWith("None", ignoreCase = true) }

    var secondsLeft by remember(next.id) { mutableIntStateOf(NEXT_UP_SECONDS) }

    LaunchedEffect(next.id) {
        secondsLeft = NEXT_UP_SECONDS
        coach.awaitVoiceReady()
        coach.speakSequence(
            "Next up: ${next.title}.",
            phase?.instruction.orEmpty(),
            if (kit != null) "You will need $kit." else ""
        )
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.bg)
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "NEXT UP",
                color = AppTheme.colors.amber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = next.title,
                color = AppTheme.colors.textHigh,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (phase != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = phase.instruction,
                    color = AppTheme.colors.textHigh,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = phase.physiologicalBenefit,
                    color = AppTheme.colors.textMedium,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            }
            if (kit != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "You'll need: $kit",
                    color = AppTheme.colors.textMedium,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Starting in $secondsLeft",
                color = AppTheme.colors.textMuted,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Skip",
                color = AppTheme.colors.bg,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.amber)
                    .clickable { coach.stopVoice(); onContinue() }
                    .padding(horizontal = 40.dp, vertical = 14.dp)
                    .testTag("next_up_skip")
            )
        }
    }
}
