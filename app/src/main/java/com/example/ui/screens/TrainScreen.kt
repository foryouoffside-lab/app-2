package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomWorkoutEntity
import com.example.data.SessionLogDao
import com.example.model.ExercisePhase
import com.example.model.ExerciseType
import com.example.model.EyeIssue
import com.example.model.Practice
import com.example.model.Protocol
import com.example.model.StudioDrill
import com.example.model.StudioDrillRepository
import com.example.model.toProtocol
import com.example.ui.CustomRoutineBuilderDialog
import com.example.ui.drill.DrillSheet
import androidx.compose.animation.core.LinearEasing
import com.example.ui.drill.StimulusCanvas
import com.example.ui.drill.TargetStyle
import com.example.util.DEFAULT_TARGET_SPEED
import com.example.util.TargetColor
import com.example.ui.drill.pausableFloat
import com.example.ui.drill.repLabel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BiologicalTeal
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IrisLavender
import com.example.ui.theme.MintBreeze
import com.example.ui.theme.MutedBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RoseCritical
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmApricot
import com.example.ui.theme.ZincSurfaceElevated
import kotlinx.coroutines.launch

@Composable
fun TrainScreen(
    onStartProtocol: (Protocol) -> Unit,
    sessionLogDao: SessionLogDao,
    targetColor: TargetColor = TargetColor.AMBER,
    targetSpeed: Float = DEFAULT_TARGET_SPEED,
    modifier: Modifier = Modifier
) {
    // The row thumbnails show the target exactly as the drill will draw it, so a colour or
    // speed change is visible before the drill is opened.
    val targetStyle = remember(targetColor, targetSpeed) { TargetStyle.from(targetColor, targetSpeed) }
    // Hoisted so the non-composable LazyListScope below can still colour its items.
    val appColors = AppTheme.colors
    var showBuilderDialog by remember { mutableStateOf(false) }
    // The exercise whose instructions are open. A habit has no timer to start, so tapping
    // it opens what to do instead of the player.
    var openHowTo by remember { mutableStateOf<StudioDrill?>(null) }
    val habitDrills = remember { StudioDrillRepository.drills.filter { it.practice == Practice.HABIT } }
    val customWorkouts by sessionLogDao.getAllCustomWorkouts().collectAsStateWithLifecycle(initialValue = emptyList())
    // Drives every row thumbnail. A fixed loop, not each drill's real cycle: a 20-second
    // one would read as a frozen image at this size.
    val thumbnailPhase = pausableFloat(
        isPaused = false, from = 0f, to = 1f,
        periodMillis = THUMBNAIL_CYCLE_MILLIS, reverse = false, easing = LinearEasing
    )
    val coroutineScope = rememberCoroutineScope()

    if (showBuilderDialog) {
        CustomRoutineBuilderDialog(
            onDismiss = { showBuilderDialog = false },
            onSaveAndLaunch = { protocol, entity ->
                coroutineScope.launch {
                    sessionLogDao.insertCustomWorkout(entity)
                }
                showBuilderDialog = false
                onStartProtocol(protocol)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Training",
                            color = AppTheme.colors.textHigh,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Pick one, or build a routine",
                            color = AppTheme.colors.textMedium,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = AppTheme.colors.iris,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Custom Studio Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, AppTheme.colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AppTheme.colors.iris.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = AppTheme.colors.iris,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CUSTOM STUDIO",
                                    color = AppTheme.colors.iris,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Text(
                            text = "${customWorkouts.size} saved",
                            color = AppTheme.colors.textMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your own routine",
                        color = AppTheme.colors.textHigh,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Pick the drills, set the order.",
                        color = AppTheme.colors.textMedium,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showBuilderDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.iris),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = AppTheme.colors.bg,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "New routine",
                                color = AppTheme.colors.bg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Custom Routines List (if any)
        if (customWorkouts.isNotEmpty()) {
            item {
                Text(
                    text = "My Custom Routines",
                    color = AppTheme.colors.textHigh,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(customWorkouts, key = { it.workoutId }) { workout ->
                val phases = workout.serializedDrills.split(",").mapNotNull { part ->
                    val tokens = part.split(":")
                    if (tokens.size == 2) {
                        val type = try { ExerciseType.valueOf(tokens[0]) } catch (_: Exception) { ExerciseType.RAPID_BLINK }
                        val duration = tokens[1].toIntOrNull() ?: 20
                        ExercisePhase(
                            title = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            instruction = "Follow the guided eye exercise pacing smoothly.",
                            durationSeconds = duration,
                            type = type,
                            physiologicalBenefit = "Customized ocular exercise phase."
                        )
                    } else null
                }.ifEmpty {
                    listOf(
                        ExercisePhase(
                            title = "Custom Drill",
                            instruction = "Relax your eyes.",
                            durationSeconds = workout.estimatedTotalSeconds,
                            type = ExerciseType.RAPID_BLINK,
                            physiologicalBenefit = "Custom relaxation."
                        )
                    )
                }

                val protocol = Protocol(
                    id = workout.workoutId,
                    title = workout.title,
                    tag = "Custom",
                    totalSeconds = workout.estimatedTotalSeconds,
                    description = "Personalized routine with ${phases.size} phases.",
                    phases = phases,
                    targetSymptom = "Custom Eye Care"
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = BorderStroke(1.dp, AppTheme.colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.iris.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = AppTheme.colors.iris,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = workout.title,
                                color = AppTheme.colors.textHigh,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${protocol.totalSeconds}s • ${phases.size} phases",
                                color = AppTheme.colors.textMedium,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    sessionLogDao.deleteCustomWorkout(workout.workoutId)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AppTheme.colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.surfaceElevated)
                                .clickable { onStartProtocol(protocol) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = AppTheme.colors.amber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Grouped by the complaint each one is offered for, because that is the question
        // somebody arrives with. A flat list of 25 made you already know which drill you
        // wanted before you could find it. No library-wide heading: each drill states its
        // own evidence in its how-to sheet, and saying it twice only added a banner.
        EyeIssue.entries.forEach { issue ->
            val group = StudioDrillRepository.drills.filter {
                it.issue == issue && it.practice == Practice.GUIDED
            }
            if (group.isEmpty()) return@forEach
            item(key = "issue_${issue.name}") { IssueHeader(issue, group.size) }
            items(group, key = { it.id }) { drill ->
                DrillListCard(drill, thumbnailPhase, targetStyle) {
                    if (drill.practice == Practice.HABIT) {
                        openHowTo = drill
                    } else {
                        onStartProtocol(drill.toProtocol())
                    }
                }
            }
        }

        // Last, under the exercises. These are things to read and change about your day,
        // not something to run, so they would otherwise push the actual drills off the
        // first screen.
        if (habitDrills.isNotEmpty()) {
            item(key = "daily_habits") {
                LibrarySectionHeader(
                    title = "Daily habits & prevention",
                    blurb = "The useful parts that happen away from an exercise animation",
                    count = habitDrills.size
                )
            }
            items(habitDrills, key = { "habit_${it.id}" }) { drill ->
                DrillListCard(drill, thumbnailPhase, targetStyle) { openHowTo = drill }
            }
        }
    }

    openHowTo?.let { drill -> DrillSheet(drill) { openHowTo = null } }
}


@Composable
private fun LibrarySectionHeader(title: String, blurb: String, count: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = AppTheme.colors.textHigh,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(text = "$count", color = AppTheme.colors.textMuted, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = blurb, color = AppTheme.colors.textMedium, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

/** The heading over one group: the complaint, and what the group does about it. */
@Composable
private fun IssueHeader(issue: EyeIssue, count: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = issue.label,
                color = AppTheme.colors.textHigh,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(text = "$count", color = AppTheme.colors.textMuted, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = issue.blurb,
            color = AppTheme.colors.textMedium,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

/**
 * A drill name on exactly one line.
 *
 * "Post-Concussion Vergence / Accommodation Demo" does not fit a phone row at 16sp, and
 * the two ways out are both bad: wrapping makes rows different heights so the list stops
 * scanning cleanly, and an ellipsis clips the word that identifies the drill. So the
 * long ones shrink to fit instead, down to a 12sp floor. Most names never move.
 */
@Composable
private fun DrillName(name: String) {
    var sizeSp by remember(name) { mutableStateOf(16f) }
    Text(
        text = name,
        color = AppTheme.colors.textHigh,
        fontSize = sizeSp.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false,
        onTextLayout = { if (it.hasVisualOverflow && sizeSp > 12f) sizeSp -= 0.5f }
    )
}

/**
 * One exercise in the Train list.
 *
 * Two kinds go through here. A guided drill shows how long it runs and starts on tap; a
 * habit has nothing to run, so it says so and opens its instructions instead. The row
 * has to make that difference visible before the tap, not after it.
 */
@Composable
private fun DrillListCard(drill: StudioDrill, phase: Float, style: TargetStyle, onStart: () -> Unit) {
    val habit = drill.practice == Practice.HABIT
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onStart)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            // The drill's own stimulus, running. Same canvas the player uses, so the row
            // shows the actual movement rather than a picture of it.
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.surfaceElevated)
            ) {
                StimulusCanvas(drill.stimulus, phase, active = true, style = style)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                DrillName(drill.name)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (habit) "How to \u00b7 nothing to run" else repLabel(drill.dose.reps, drill.dose.cycleSeconds),
                    color = AppTheme.colors.textMedium,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceElevated)
            ) {
                Icon(
                    imageVector = if (habit) Icons.Default.MenuBook else Icons.Default.PlayArrow,
                    contentDescription = if (habit) "How to do it" else "Start",
                    tint = if (habit) AppTheme.colors.teal else AppTheme.colors.textHigh,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** One loop of a row thumbnail. Slow enough to read, quick enough to look alive. */
private const val THUMBNAIL_CYCLE_MILLIS = 3000
