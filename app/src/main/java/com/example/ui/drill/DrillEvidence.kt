package com.example.ui.drill

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DigitalReproducibility
import com.example.model.EvidenceGrade
import com.example.model.Practice
import com.example.model.StudioDrill
import com.example.model.StudioStimulus
import com.example.ui.theme.AppTheme

/**
 * Everything the library holds on one drill: how to do it, then what the case for it is.
 *
 * One sheet for both ways in. The player opens it from the flask mid-drill, and Train
 * opens it for an exercise that has no timer to start, so the steps and the evidence
 * cannot end up saying different things in two places.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillSheet(drill: StudioDrill, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppTheme.colors.bg
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(drill.name, color = AppTheme.colors.textHigh, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (drill.howTo.isNotEmpty()) HowToCard(drill.howTo)
            DrillEvidenceBody(drill)
            Spacer(Modifier.size(24.dp))
        }
    }
}

/** The steps, numbered, for an exercise the app cannot cue frame by frame. */
@Composable
private fun HowToCard(steps: List<String>) {
    Surface(
        color = AppTheme.colors.surface, shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.teal.copy(alpha = .5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "HOW TO DO IT", color = AppTheme.colors.teal, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp
            )
            steps.forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(24.dp).background(AppTheme.colors.teal.copy(alpha = .16f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}", color = AppTheme.colors.teal,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Text(step, color = AppTheme.colors.textHigh, fontSize = 14.sp, lineHeight = 21.sp)
                }
            }
        }
    }
}

@Composable
fun DrillEvidenceBody(drill: StudioDrill, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            GradeMark(drill.evidenceGrade)
            StatusPill(drill.safetyLevel)
        }
        if (drill.aliases.isNotEmpty()) {
            InfoSection("Also known as", drill.aliases.joinToString(" · "))
        }
        DoseCard(drill)
        EvidenceCard(drill)
        InfoSection("Digital reproduction", "${drill.digitalReproducibility.label()}. ${drill.digitalValidity}\n\nHardware: ${drill.equipment}")
        InfoSection("What it does NOT prove", drill.unprovenClaims.joinToString("\n• ", prefix = "• "))
        SafetyBanner("STOP for persistent double vision, significant dizziness, nausea, pain, severe headache, or a new visual disturbance. ${drill.contraindications}")
        References(drill)
        Text("No diagnosis · No treatment prescription · Research verified 15 September 2026", color = AppTheme.colors.textMuted, fontSize = 11.sp)
    }
}

/**
 * What this drill is about to make you do, and where that number came from.
 *
 * Kept separate from the evidence card on purpose: a graded trial does not imply a
 * graded dose, and most of these drills have the first without the second.
 */
@Composable
private fun DoseCard(drill: StudioDrill) {
    val dose = drill.dose
    val habit = drill.practice == Practice.HABIT
    val accent = if (dose.fromEvidence) AppTheme.colors.teal else AppTheme.colors.amber
    Surface(
        color = AppTheme.colors.surface, shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = .5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                when {
                    habit -> "HOW OFTEN"
                    dose.fromEvidence -> "DOSE \u00b7 FROM THE TRIAL"
                    else -> "DOSE \u00b7 APP DEFAULT, NOT FROM A TRIAL"
                },
                color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp
            )
            // A habit is not run on a timer here, so quoting it reps-by-seconds would be
            // an invented precision. Its basis line carries the real answer.
            if (!habit) Text(
                "${dose.reps} \u00d7 ${dose.cycleSeconds}s  \u00b7  ${dose.totalSeconds / 60}m ${dose.totalSeconds % 60}s total",
                color = AppTheme.colors.textHigh, fontSize = 18.sp, fontWeight = FontWeight.Bold
            )
            Text(dose.basis, color = AppTheme.colors.textMedium, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun EvidenceCard(drill: StudioDrill) {
    Surface(
        color = AppTheme.colors.surface, shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border), modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("EVIDENCE", color = AppTheme.colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            EvidenceLine("Grade", drill.evidenceGrade.name)
            EvidenceLine("Clinical status", if (drill.clinicalUse) "Clinically used" else "Research only")
            EvidenceLine("Evidence type", drill.evidenceType)
            EvidenceLine("Population", drill.studiedPopulation)
            EvidenceLine("Condition", drill.condition)
            InfoSection("Evidence for", drill.evidenceFor, compact = true)
            InfoSection("Limitation", drill.evidenceLimitation, compact = true)
            InfoSection("Measured outcomes", drill.provenOutcomes.joinToString(" · "), compact = true)
            EvidenceLine("Review status", drill.reviewStatus.name.replace('_', ' '))
        }
    }
}

@Composable
private fun References(drill: StudioDrill) {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("REFERENCES", color = AppTheme.colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        drill.sources.forEach { source ->
            Column(
                Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                    .clickable { uriHandler.openUri(source.url) }.padding(14.dp)
            ) {
                Text(source.title, color = AppTheme.colors.teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
                Spacer(Modifier.size(4.dp))
                Text(source.citation, color = AppTheme.colors.textMedium, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
        if (drill.sources.isEmpty()) Text("No drill-specific outcome study identified; clinical/mechanistic demonstration only.", color = AppTheme.colors.textMedium, fontSize = 12.sp)
    }
}

@Composable
private fun InfoSection(title: String, body: String, compact: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title.uppercase(), color = AppTheme.colors.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
        Spacer(Modifier.size(1.dp))
        Text(body, color = AppTheme.colors.textHigh, fontSize = if (compact) 13.sp else 15.sp, lineHeight = if (compact) 20.sp else 23.sp)
    }
}

@Composable
private fun EvidenceLine(label: String, value: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label.uppercase(), color = AppTheme.colors.textMuted, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
        )
        Text(value, color = AppTheme.colors.textHigh, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun GradeMark(grade: EvidenceGrade) {
    val color = when (grade) {
        EvidenceGrade.A -> Color(0xFF28B781)
        EvidenceGrade.B -> AppTheme.colors.teal
        EvidenceGrade.C -> AppTheme.colors.amber
        EvidenceGrade.D -> Color(0xFFE18A52)
        EvidenceGrade.E -> Color(0xFFE05D68)
    }
    Box(Modifier.size(38.dp).background(color.copy(alpha = .14f), CircleShape).border(1.dp, color, CircleShape), contentAlignment = Alignment.Center) {
        Text(grade.name, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun StatusPill(text: String) {
    AssistChip(onClick = {}, label = { Text(text, maxLines = 1) }, colors = AssistChipDefaults.assistChipColors(containerColor = AppTheme.colors.surface, labelColor = AppTheme.colors.textMedium))
}

@Composable
private fun SafetyBanner(text: String) {
    Surface(color = AppTheme.colors.amber.copy(alpha = .10f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = AppTheme.colors.textHigh, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(14.dp))
    }
}

private fun DigitalReproducibility.label() = when (this) {
    DigitalReproducibility.FULL -> "Digital"
    DigitalReproducibility.PARTIAL -> "Partial demo"
    DigitalReproducibility.EQUIPMENT_REQUIRED -> "Equipment"
}
