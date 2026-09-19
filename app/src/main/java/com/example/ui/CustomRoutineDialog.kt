package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CustomWorkoutEntity
import com.example.model.Practice
import com.example.model.Protocol
import com.example.model.StudioDrill
import com.example.model.StudioDrillRepository
import com.example.model.toProtocol
import com.example.ui.drill.formatLength
import com.example.ui.theme.AppTheme

/**
 * A routine built from real Studio drills, in the order picked.
 *
 * Each selection becomes its own [Protocol] via [toProtocol] rather than a hand-authored
 * phase: that is what carries the drill's real stimulus, evidence id and how-to pages
 * into the player, instead of every custom-routine phase silently rendering as the blink
 * animation the way a freeform phase list used to.
 */
@Composable
fun CustomRoutineBuilderDialog(
    onDismiss: () -> Unit,
    onSaveAndLaunch: (List<Protocol>, CustomWorkoutEntity, skipInstructions: Boolean) -> Unit
) {
    var routineName by remember { mutableStateOf("My Custom Routine") }
    val availableDrills = remember {
        StudioDrillRepository.drills.filter { it.practice == Practice.GUIDED }
    }
    val selectedDrills = remember { mutableStateListOf<StudioDrill>() }
    var skipInstructions by remember { mutableStateOf(false) }
    var showDrillPicker by remember { mutableStateOf(false) }

    val totalTime = selectedDrills.sumOf { it.dose.totalSeconds }
    val isOverMaxLimit = totalTime > 360 // Safety ceiling: max 6 minutes for a self-composed set

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppTheme.colors.surface,
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
                            color = AppTheme.colors.amber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Routine Composer",
                            color = AppTheme.colors.textHigh,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppTheme.colors.textMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("Routine Name", color = AppTheme.colors.textMedium) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTheme.colors.textHigh,
                        unfocusedTextColor = AppTheme.colors.textHigh,
                        focusedBorderColor = AppTheme.colors.amber,
                        unfocusedBorderColor = AppTheme.colors.border,
                        focusedLabelColor = AppTheme.colors.amber
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Time Budget & Safety Lock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.colors.surfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOverMaxLimit) Color(0xFFEF4444) else AppTheme.colors.amber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TIME BUDGET: ${formatLength(totalTime)}",
                            color = if (isOverMaxLimit) Color(0xFFEF4444) else AppTheme.colors.textHigh,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = if (isOverMaxLimit) "EXCEEDS 6 MIN LIMIT" else "SAFE LIMIT (≤ 6 min)",
                        color = if (isOverMaxLimit) Color(0xFFEF4444) else AppTheme.colors.textMedium,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Skip-instructions toggle: an experienced user can run straight through
                // without the how-to walkthrough this same routine would otherwise show.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.colors.surfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Skip instructions",
                            color = AppTheme.colors.textHigh,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Go straight into each drill, no how-to pages.",
                            color = AppTheme.colors.textMedium,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = skipInstructions,
                        onCheckedChange = { skipInstructions = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppTheme.colors.bg,
                            checkedTrackColor = AppTheme.colors.amber,
                            // The row behind this switch is surfaceElevated, so the
                            // unchecked track has to read against that, not against it --
                            // surface (not surfaceElevated) is what actually contrasts here.
                            uncheckedThumbColor = AppTheme.colors.textMuted,
                            uncheckedTrackColor = AppTheme.colors.surface,
                            uncheckedBorderColor = AppTheme.colors.border
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selected Drills List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(selectedDrills, key = { _, drill -> drill.id }) { index, drill ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated.copy(alpha = 0.6f)),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = AppTheme.colors.amber,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = drill.name,
                                            color = AppTheme.colors.textHigh,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatLength(drill.dose.totalSeconds),
                                            color = AppTheme.colors.textMedium,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { selectedDrills.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove drill",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
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
                        containerColor = AppTheme.colors.surfaceElevated,
                        contentColor = AppTheme.colors.textHigh
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Exercise", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Launch Custom Session Button
                Button(
                    onClick = {
                        val workoutId = "custom_${System.currentTimeMillis()}"
                        val protocols = selectedDrills.map { it.toProtocol() }
                        val entity = CustomWorkoutEntity(
                            workoutId = workoutId,
                            title = routineName.ifBlank { "Custom Routine" },
                            estimatedTotalSeconds = totalTime,
                            isFavorite = true,
                            serializedDrills = selectedDrills.joinToString(",") { it.id }
                        )
                        onSaveAndLaunch(protocols, entity, skipInstructions)
                    },
                    enabled = !isOverMaxLimit && selectedDrills.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.amber,
                        contentColor = AppTheme.colors.bg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save & Launch (${formatLength(totalTime)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Drill Picker Sub-Dialog -- every available drill stays listed the whole time, a tap
    // toggles it in or out of the routine, and nothing closes the picker until "Done" is
    // pressed, so several exercises can be added in one visit instead of reopening this
    // dialog once per drill.
    if (showDrillPicker) {
        Dialog(onDismissRequest = { showDrillPicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppTheme.colors.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Select Exercises",
                                color = AppTheme.colors.textHigh,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedDrills.size} selected",
                                color = AppTheme.colors.textMedium,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { showDrillPicker = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = AppTheme.colors.textMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp).weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(availableDrills, key = { _, drill -> drill.id }) { _, template ->
                            val isSelected = selectedDrills.any { it.id == template.id }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        AppTheme.colors.amber.copy(alpha = 0.15f)
                                    } else {
                                        AppTheme.colors.surfaceElevated
                                    }
                                ),
                                border = if (isSelected) BorderStroke(1.dp, AppTheme.colors.amber) else null,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (isSelected) {
                                            selectedDrills.removeAll { it.id == template.id }
                                        } else {
                                            selectedDrills.add(template)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) AppTheme.colors.amber else AppTheme.colors.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = AppTheme.colors.bg,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = template.name,
                                                color = AppTheme.colors.textHigh,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = formatLength(template.dose.totalSeconds),
                                                color = AppTheme.colors.amber,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                modifier = Modifier.padding(start = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showDrillPicker = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.amber,
                            contentColor = AppTheme.colors.bg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text(text = "Done", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
