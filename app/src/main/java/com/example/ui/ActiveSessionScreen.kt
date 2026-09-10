package com.example.ui

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExercisePhase
import com.example.model.ExerciseType
import com.example.model.Protocol
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BiologicalTeal
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis
import com.example.ui.theme.ZincSurfaceElevated
import com.example.util.SensoryFeedbackManager
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import com.example.util.SafetyGovernance

@Composable
fun ActiveSessionScreen(
    protocol: Protocol,
    onClose: () -> Unit,
    onSessionCompleted: (protocolId: String, duration: Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sensoryManager = remember { SensoryFeedbackManager(context) }

    var currentPhaseIndex by remember { mutableIntStateOf(0) }
    val currentPhase: ExercisePhase = protocol.phases[currentPhaseIndex]
    var phaseSecondsRemaining by remember { mutableIntStateOf(currentPhase.durationSeconds) }
    var isPaused by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var showSafetyStopDialog by remember { mutableStateOf(false) }

    sensoryManager.soundEnabled = soundEnabled
    sensoryManager.hapticsEnabled = hapticsEnabled

    // Stage 13 & 14: Safety Stop Alert Modal
    if (showSafetyStopDialog) {
        AlertDialog(
            onDismissRequest = {
                showSafetyStopDialog = false
                isPaused = false
            },
            containerColor = CharcoalSurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Safety Stop & Rest Protocol",
                    color = TextHighEmphasis,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Stage 13 Clinical Mandate: If you experience eye discomfort, headache, or visual strain, pause immediately.",
                        color = TextMediumEmphasis,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Red-Flag Symptoms: Sudden vision drop, persistent double vision, deep ocular pain, or bright light flashes require immediate professional ophthalmologic evaluation.",
                        color = AmberPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSafetyStopDialog = false
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Exit Session Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSafetyStopDialog = false
                        isPaused = false
                    }
                ) {
                    Text("Resume (Feeling Fine)", color = TextHighEmphasis)
                }
            }
        )
    }

    // Session Timer Loop
    LaunchedEffect(currentPhaseIndex, isPaused) {
        if (!isPaused) {
            sensoryManager.triggerPhaseHaptic()
            if (soundEnabled) {
                sensoryManager.playChimeTone(432.0, 300)
            }
            while (phaseSecondsRemaining > 0) {
                delay(1000)
                if (!isPaused) {
                    phaseSecondsRemaining--
                }
            }

            // Phase complete
            if (currentPhaseIndex < protocol.phases.size - 1) {
                currentPhaseIndex++
                phaseSecondsRemaining = protocol.phases[currentPhaseIndex].durationSeconds
            } else {
                sensoryManager.triggerCompletionHaptic()
                if (soundEnabled) {
                    sensoryManager.playCompletionChime()
                }
                onSessionCompleted(protocol.id, protocol.totalSeconds)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // TOP CONTROL BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("session_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Session",
                    tint = TextMediumEmphasis
                )
            }

            // Phase indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PHASE ${currentPhaseIndex + 1} OF ${protocol.phases.size}",
                    color = AmberPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = currentPhase.title,
                    color = TextHighEmphasis,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = { soundEnabled = !soundEnabled },
                modifier = Modifier.testTag("session_sound_toggle")
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Toggle Chime Audio",
                    tint = if (soundEnabled) BiologicalTeal else TextMediumEmphasis
                )
            }
        }

        // CENTER TRAINING CANVAS (The Exercise IS the Interface)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 180.dp),
            contentAlignment = Alignment.Center
        ) {
            when (currentPhase.type) {
                ExerciseType.SMOOTH_PURSUIT -> {
                    SmoothPursuitCanvas(isPaused = isPaused)
                }
                ExerciseType.SACCADE_JUMP -> {
                    SaccadeJumpCanvas(
                        isPaused = isPaused,
                        onJump = { sensoryManager.triggerTickHaptic() },
                        onTargetHit = { latency ->
                            sensoryManager.triggerTargetHitHaptic()
                            if (soundEnabled) {
                                coroutineScope.launch {
                                    sensoryManager.playHitChime()
                                }
                            }
                        }
                    )
                }
                ExerciseType.RAPID_BLINK -> {
                    BlinkPacingCanvas(isPaused = isPaused)
                }
                ExerciseType.PALMING_BREATH -> {
                    PalmingBreathCanvas(isPaused = isPaused)
                }
                ExerciseType.ACCOMMODATION_SHIFT -> {
                    AccommodationShiftCanvas(isPaused = isPaused)
                }
            }
        }

        // BOTTOM INSTRUCTION & TELEMETRY PANEL
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Linear Progress of current phase
            val progress = (currentPhase.durationSeconds - phaseSecondsRemaining).toFloat() / currentPhase.durationSeconds.coerceAtLeast(1)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AmberPrimary,
                trackColor = ZincSurfaceElevated
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Instruction Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentPhase.instruction,
                        color = TextHighEmphasis,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentPhase.physiologicalBenefit,
                        color = BiologicalTeal,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pause / Resume, Countdown Timer & Emergency Safety Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d:%02d", phaseSecondsRemaining / 60, phaseSecondsRemaining % 60),
                    color = TextHighEmphasis,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Stage 13 Safety Stop Button
                    OutlinedButton(
                        onClick = {
                            isPaused = true
                            showSafetyStopDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("session_safety_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Safety Stop",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(text = "Discomfort?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) AmberPrimary else ZincSurfaceElevated,
                            contentColor = if (isPaused) ObsidianBg else TextHighEmphasis
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("session_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = if (isPaused) "Resume" else "Pause", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun SmoothPursuitCanvas(isPaused: Boolean) {
    // Stage 9: Deterministic Lemniscate (Figure-8) trajectory at constant angular velocity
    val infiniteTransition = rememberInfiniteTransition(label = "smooth_pursuit")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_progress"
    )

    Canvas(modifier = Modifier.size(300.dp)) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val scaleA = width * 0.40f

        // Parametric Lemniscate of Bernoulli: x = a*cos(t)/(1+sin^2(t)), y = a*sin(t)*cos(t)/(1+sin^2(t))
        val t = progress * 2 * Math.PI
        val denom = (1 + sin(t) * sin(t)).toFloat().coerceAtLeast(0.001f)
        val targetX = centerX + (scaleA * cos(t)).toFloat() / denom
        val targetY = centerY + (scaleA * 0.65f * sin(t) * cos(t)).toFloat() / denom

        // Ultra-low contrast guide path to avoid distraction while offering spatial anchor
        drawCircle(
            color = Color(0x12F59E0B),
            radius = scaleA * 0.75f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.0f)
        )

        // Peripheral anti-aliased focus ring
        drawCircle(
            color = Color(0x28F59E0B),
            radius = 30f,
            center = Offset(targetX, targetY)
        )

        // High-contrast 590nm Amber focal core (Stage 9 token: 14dp core)
        drawCircle(
            color = AmberPrimary,
            radius = 12f,
            center = Offset(targetX, targetY)
        )

        // Micro inner foveal fixation dot
        drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = Offset(targetX, targetY)
        )
    }
}

@Composable
fun SaccadeJumpCanvas(
    isPaused: Boolean,
    onJump: () -> Unit,
    onTargetHit: (latencyMs: Long) -> Unit = {}
) {
    var step by remember { mutableIntStateOf(0) }
    var lastJumpTimeNanos by remember { mutableStateOf(SystemClock.elapsedRealtimeNanos()) }
    var lastHitLatency by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            while (true) {
                delay(950)
                if (!isPaused) {
                    step = (step + 1) % 4
                    lastJumpTimeNanos = SystemClock.elapsedRealtimeNanos()
                    onJump()
                }
            }
        }
    }

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    val latency = (SystemClock.elapsedRealtimeNanos() - lastJumpTimeNanos) / 1_000_000
                    if (latency in 120..1500) {
                        lastHitLatency = latency
                        onTargetHit(latency)
                        onJump()
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val margin = 40f

            val targets = listOf(
                Offset(margin, margin),
                Offset(width - margin, margin),
                Offset(width - margin, height - margin),
                Offset(margin, height - margin)
            )

            // Draw stationary crosshairs
            for (i in targets.indices) {
                val point = targets[i]
                val isCurrent = (i == step)
                drawCircle(
                    color = if (isCurrent) AmberPrimary else Color(0x33EDEDF0),
                    radius = if (isCurrent) 20f else 6f,
                    center = point
                )
                if (isCurrent) {
                    drawCircle(
                        color = Color(0x44F59E0B),
                        radius = 36f,
                        center = point
                    )
                }
            }
        }

        if (lastHitLatency != null) {
            Text(
                text = "${lastHitLatency}ms",
                color = BiologicalTeal,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun BlinkPacingCanvas(isPaused: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink_transition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(220.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    color = Color(0x1914B8A6),
                    radius = 90f * scale,
                    center = center
                )
                drawCircle(
                    color = BiologicalTeal,
                    radius = 80f * scale,
                    center = center,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Text(
                text = if (scale > 0.75f) "CLOSE &\nSQUEEZE" else "OPEN &\nRELAX",
                color = TextHighEmphasis,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun PalmingBreathCanvas(isPaused: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "palming_breath")
    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    color = Color(0x15F59E0B),
                    radius = 100f * breathProgress,
                    center = center
                )
                drawCircle(
                    color = AmberPrimary.copy(alpha = 0.6f),
                    radius = 70f * breathProgress,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PALMS OVER EYES",
                    color = AmberPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (breathProgress > 0.75f) "Inhale Slowly" else "Exhale Completely",
                    color = TextHighEmphasis,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AccommodationShiftCanvas(isPaused: Boolean) {
    // Stage 9: Continuous harmonic accommodative shift pacing
    var isNearFocus by remember { mutableStateOf(true) }
    val transition = updateTransition(targetState = isNearFocus, label = "accommodation_transition")
    val focusRingRadius by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) },
        label = "ring_radius"
    ) { near -> if (near) 32f else 96f }

    val focusRingAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = LinearEasing) },
        label = "ring_alpha"
    ) { near -> if (near) 1.0f else 0.5f }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            while (true) {
                delay(3500)
                if (!isPaused) {
                    isNearFocus = !isNearFocus
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(250.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                
                // Outer expansion ring
                drawCircle(
                    color = (if (isNearFocus) AmberPrimary else BiologicalTeal).copy(alpha = focusRingAlpha * 0.25f),
                    radius = focusRingRadius * 1.35f,
                    center = center
                )

                // Central high-contrast focal stimulus
                drawCircle(
                    color = if (isNearFocus) AmberPrimary else BiologicalTeal,
                    radius = focusRingRadius,
                    center = center,
                    style = if (isNearFocus) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = 3.dp.toPx())
                )

                // Foveal anchor cross
                drawLine(
                    color = if (isNearFocus) Color.Black else Color.White,
                    start = Offset(center.x - 8f, center.y),
                    end = Offset(center.x + 8f, center.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = if (isNearFocus) Color.Black else Color.White,
                    start = Offset(center.x, center.y - 8f),
                    end = Offset(center.x, center.y + 8f),
                    strokeWidth = 2f
                )
            }

            Text(
                text = if (isNearFocus) "FOCUS NEAR\n(Thumb / Screen)" else "LOOK FAR\n(20ft Horizon)",
                color = TextHighEmphasis,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 180.dp)
            )
        }
    }
}
