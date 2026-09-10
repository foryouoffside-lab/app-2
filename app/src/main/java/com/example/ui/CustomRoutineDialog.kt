package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CustomWorkoutEntity
import com.example.model.ExercisePhase
import com.example.model.ExerciseType
import com.example.model.Protocol
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BiologicalTeal
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.MutedBorder
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis
import com.example.ui.theme.ZincSurfaceElevated
import java.util.UUID

data class BuilderPhaseItem(
    val id: String = UUID.randomUUID().toString(),
    val exerciseType: ExerciseType,
    val title: String,
    val instruction: String,
    var durationSeconds: Int = 20,
    val benefit: String = "Ergonomic visual-motor reset"
)

val AVAILABLE_DRILL_TEMPLATES = listOf(
    BuilderPhaseItem(
        exerciseType = ExerciseType.RAPID_BLINK,
        title = "Conscious Blink Reset",
        instruction = "Soft close, hold 2s, open. Replenish the tear film.",
        durationSeconds = 20,
        benefit = "Replenishes corneal tear film."
    ),
    BuilderPhaseItem(
        exerciseType = ExerciseType.ACCOMMODATION_SHIFT,
        title = "20-20-20 Horizon Focus",
        instruction = "Look at an object at least 20 feet away. Release ciliary tension.",
        durationSeconds = 20,
        benefit = "Breaks ciliary muscle spasm."
    ),
    BuilderPhaseItem(
        exerciseType = ExerciseType.SMOOTH_PURSUIT,
        title = "Smooth Pursuit Sweep",
        instruction = "Track the continuous amber target without moving your head.",
        durationSeconds = 30,
        benefit = "Stimulates lateral extraocular muscles."
    ),
    BuilderPhaseItem(
        exerciseType = ExerciseType.SACCADE_JUMP,
        title = "4-Quadrant Rapid Saccades",
        instruction = "Jump your visual focus instantaneously between target anchors.",
        durationSeconds = 25,
        benefit = "Sharpens ballistic saccadic re-fixation."
    ),
    BuilderPhaseItem(
        exerciseType = ExerciseType.PALMING_BREATH,
        title = "Orbital Cup Palming",
        instruction = "Warm hands, cup lightly over closed orbits. Breathe steadily in darkout.",
        durationSeconds = 35,
        benefit = "Relaxes photoreceptor metabolic demand."
    )
)

@Composable
fun CustomRoutineBuilderDialog(
    onDismiss: () -> Unit,
    onSaveAndLaunch: (Protocol, CustomWorkoutEntity) -> Unit
) {
    var routineName by remember { mutableStateOf("My Custom De-Strain") }
    val selectedPhases = remember {
        mutableStateListOf(
            AVAILABLE_DRILL_TEMPLATES[0].copy(id = UUID.randomUUID().toString(), durationSeconds = 15),
            AVAILABLE_DRILL_TEMPLATES[1].copy(id = UUID.randomUUID().toString(), durationSeconds = 20),
            AVAILABLE_DRILL_TEMPLATES[2].copy(id = UUID.randomUUID().toString(), durationSeconds = 25)
        )
    }
    var showDrillPicker by remember { mutableStateOf(false) }

    val totalTime = selectedPhases.sumOf { it.durationSeconds }
    val isOverMaxLimit = totalTime > 360 // Stage 7 safety ceiling: max 6 minutes

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CharcoalSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CUSTOM STUDIO",
                            color = AmberPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Routine Composer",
                            color = TextHighEmphasis,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMediumEmphasis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("Routine Name", color = TextMediumEmphasis) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis,
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = MutedBorder,
                        focusedLabelColor = AmberPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Time Budget & Safety Lock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ZincSurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOverMaxLimit) Color(0xFFEF4444) else BiologicalTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TIME BUDGET: ${totalTime}s",
                            color = if (isOverMaxLimit) Color(0xFFEF4444) else TextHighEmphasis,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = if (isOverMaxLimit) "EXCEEDS 360s LIMIT" else "SAFE LIMIT (≤ 360s)",
                        color = if (isOverMaxLimit) Color(0xFFEF4444) else TextMediumEmphasis,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selected Phases List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(selectedPhases) { index, phase ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ZincSurfaceElevated.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${index + 1}",
                                        color = AmberPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = phase.title,
                                            color = TextHighEmphasis,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${phase.durationSeconds} seconds",
                                            color = TextMediumEmphasis,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Quick Duration adjust
                                    Surface(
                                        color = CharcoalSurface,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable {
                                            val newDuration = when (phase.durationSeconds) {
                                                15 -> 20
                                                20 -> 30
                                                30 -> 45
                                                45 -> 60
                                                else -> 15
                                            }
                                            selectedPhases[index] = phase.copy(durationSeconds = newDuration)
                                        }
                                    ) {
                                        Text(
                                            text = "+ time",
                                            color = BiologicalTeal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = {
                                            if (selectedPhases.size > 1) {
                                                selectedPhases.removeAt(index)
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Phase",
                                            tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Drill Button
                Button(
                    onClick = { showDrillPicker = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZincSurfaceElevated,
                        contentColor = TextHighEmphasis
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Exercise Phase", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Launch Custom Session Button
                Button(
                    onClick = {
                        val workoutId = "custom_${System.currentTimeMillis()}"
                        val compiledProtocol = Protocol(
                            id = workoutId,
                            title = routineName.ifBlank { "Custom Routine" },
                            description = "User-composed sequence with $totalTime seconds total ocular relaxation.",
                            tag = "Custom",
                            totalSeconds = totalTime,
                            targetSymptom = "Personalized Recovery",
                            phases = selectedPhases.map {
                                ExercisePhase(
                                    title = it.title,
                                    instruction = it.instruction,
                                    durationSeconds = it.durationSeconds,
                                    type = it.exerciseType,
                                    physiologicalBenefit = it.benefit
                                )
                            }
                        )

                        val entity = CustomWorkoutEntity(
                            workoutId = workoutId,
                            title = routineName.ifBlank { "Custom Routine" },
                            estimatedTotalSeconds = totalTime,
                            isFavorite = true,
                            serializedDrills = selectedPhases.joinToString(",") { "${it.exerciseType.name}:${it.durationSeconds}" }
                        )

                        onSaveAndLaunch(compiledProtocol, entity)
                    },
                    enabled = !isOverMaxLimit && selectedPhases.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save & Launch ($totalTime s)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Drill Picker Sub-Dialog
    if (showDrillPicker) {
        Dialog(onDismissRequest = { showDrillPicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CharcoalSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Select Exercise Module",
                        color = TextHighEmphasis,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AVAILABLE_DRILL_TEMPLATES.forEach { template ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ZincSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedPhases.add(template.copy(id = UUID.randomUUID().toString()))
                                    showDrillPicker = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = template.title,
                                        color = TextHighEmphasis,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${template.durationSeconds}s",
                                        color = AmberPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = template.instruction,
                                    color = TextMediumEmphasis,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
