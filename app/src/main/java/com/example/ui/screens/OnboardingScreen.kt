package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClinicalContext
import com.example.model.ScreenTimeBand
import com.example.model.VisionCorrection
import com.example.model.WellnessProfile
import com.example.model.WellnessSymptom
import com.example.ui.theme.AppTheme
import com.example.util.SUPPORTED_AGES
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Questions are asked one per screen; the step after the last one is the review. */
const val ONBOARDING_QUESTION_COUNT = 6
const val ONBOARDING_REVIEW_STEP = ONBOARDING_QUESTION_COUNT

/** Lets the radio fill paint before the card slides away. */
private const val AUTO_ADVANCE_MS = 220L

/**
 * A short intake for choosing conservative daily comfort work.
 *
 * These are selectors and safety gates, not a diagnostic questionnaire. In particular,
 * a symptom answer never unlocks the condition-specific drills in Training Studio.
 */
@Composable
fun OnboardingScreen(
    initialProfile: WellnessProfile? = null,
    initialAge: Int = initialProfile?.age ?: 30,
    onCompleted: (WellnessProfile) -> Unit
) {
    var age by remember { mutableIntStateOf(initialProfile?.age ?: initialAge) }
    var screenTime by remember { mutableStateOf(initialProfile?.screenTime ?: ScreenTimeBand.TWO_TO_FOUR) }
    var correction by remember { mutableStateOf(initialProfile?.correction ?: VisionCorrection.NONE) }
    var symptoms by remember { mutableStateOf(initialProfile?.symptoms.orEmpty()) }
    var clinicalContexts by remember { mutableStateOf(initialProfile?.clinicalContexts.orEmpty()) }
    var urgentSymptoms by remember { mutableStateOf(initialProfile?.urgentSymptoms ?: false) }

    var step by remember { mutableIntStateOf(0) }
    var advancing by remember { mutableStateOf(false) }
    val next = { step = (step + 1).coerceAtMost(ONBOARDING_REVIEW_STEP) }

    // Picking a single-choice answer is the answer, so it moves on by itself. Slider and
    // multi-select steps have no such moment and wait for Continue.
    LaunchedEffect(advancing) {
        if (advancing) {
            delay(AUTO_ADVANCE_MS)
            next()
            advancing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.bg)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = if (initialProfile == null) "Build your daily plan" else "Update your daily plan",
            color = AppTheme.colors.textHigh,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { (step + 1) / (ONBOARDING_REVIEW_STEP + 1).toFloat() },
            color = AppTheme.colors.amber,
            trackColor = AppTheme.colors.surfaceElevated,
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
        Spacer(Modifier.height(20.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val forward = targetState > initialState
                val enter = slideInHorizontally { width -> if (forward) width / 3 else -width / 3 } + fadeIn()
                val exit = slideOutHorizontally { width -> if (forward) -width / 3 else width / 3 } + fadeOut()
                enter togetherWith exit
            },
            label = "onboarding_step",
            modifier = Modifier.weight(1f)
        ) { current ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                when (current) {
                    0 -> QuestionCard(
                        number = 1,
                        title = "What is your age?",
                        detail = "Age changes near-focus ability and child prevention guidance. It is not an 'eye age'."
                    ) {
                        Text(
                            text = "$age",
                            color = AppTheme.colors.amber,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_age")
                        )
                        Slider(
                            value = age.toFloat(),
                            onValueChange = { age = it.roundToInt() },
                            valueRange = SUPPORTED_AGES.first.toFloat()..SUPPORTED_AGES.last.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = AppTheme.colors.amber,
                                activeTrackColor = AppTheme.colors.amber,
                                inactiveTrackColor = AppTheme.colors.surfaceElevated
                            )
                        )
                    }

                    1 -> QuestionCard(
                        number = 2,
                        title = "How much screen time on most days?",
                        detail = "Screen exposure changes break and blink priority, not eye strength."
                    ) {
                        ScreenTimeBand.entries.forEach { option ->
                            SingleChoiceRow(option.label, option == screenTime) {
                                screenTime = option
                                advancing = true
                            }
                        }
                    }

                    2 -> QuestionCard(
                        number = 3,
                        title = "What vision correction do you use?",
                        detail = "Glasses do not require a different exercise. They can signal that working-distance correction should be checked."
                    ) {
                        VisionCorrection.entries.forEach { option ->
                            SingleChoiceRow(option.label, option == correction) {
                                correction = option
                                advancing = true
                            }
                        }
                    }

                    3 -> QuestionCard(
                        number = 4,
                        title = "What regularly bothers you?",
                        detail = "Choose all that apply, then continue. Leave everything clear if you have no regular symptoms."
                    ) {
                        WellnessSymptom.entries.forEach { option ->
                            MultipleChoiceRow(option.label, option in symptoms) {
                                symptoms = symptoms.toggle(option)
                            }
                        }
                    }

                    4 -> QuestionCard(
                        number = 5,
                        title = "Has a clinician diagnosed or treated any of these?",
                        detail = "Choose only diagnosed conditions. These answers add safeguards; they do not create a treatment prescription."
                    ) {
                        ClinicalContext.entries.forEach { option ->
                            MultipleChoiceRow(option.label, option in clinicalContexts) {
                                clinicalContexts = clinicalContexts.toggle(option)
                            }
                        }
                    }

                    5 -> QuestionCard(
                        number = 6,
                        title = "Do you have an urgent warning sign now?",
                        detail = "Sudden vision change or loss, new flashes or a curtain, severe eye pain, a painful red eye, or sudden double vision."
                    ) {
                        SingleChoiceRow("No, none of these", !urgentSymptoms) {
                            urgentSymptoms = false
                            advancing = true
                        }
                        SingleChoiceRow("Yes - pause drills and show care guidance", urgentSymptoms) {
                            urgentSymptoms = true
                            advancing = true
                        }
                    }

                    else -> ReviewCard(
                        age = age,
                        screenTime = screenTime,
                        correction = correction,
                        symptoms = symptoms,
                        clinicalContexts = clinicalContexts,
                        urgentSymptoms = urgentSymptoms
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { step-- },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.textMedium),
                    modifier = Modifier.height(56.dp).testTag("onboarding_back")
                ) {
                    Text("Back", fontSize = 15.sp)
                }
            }
            Button(
                onClick = {
                    if (step < ONBOARDING_REVIEW_STEP) {
                        next()
                    } else {
                        onCompleted(
                            WellnessProfile(
                                age = age,
                                screenTime = screenTime,
                                correction = correction,
                                symptoms = symptoms,
                                clinicalContexts = clinicalContexts,
                                urgentSymptoms = urgentSymptoms
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.amber,
                    contentColor = AppTheme.colors.bg
                ),
                modifier = Modifier.weight(1f).height(56.dp).testTag("onboarding_continue")
            ) {
                Text(
                    text = when {
                        step < ONBOARDING_REVIEW_STEP -> "Continue"
                        urgentSymptoms -> "Save and show safety guidance"
                        else -> "Create my daily plan"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Answers are one per screen, so the last step shows them together before they are saved. */
@Composable
private fun ReviewCard(
    age: Int,
    screenTime: ScreenTimeBand,
    correction: VisionCorrection,
    symptoms: Set<WellnessSymptom>,
    clinicalContexts: Set<ClinicalContext>,
    urgentSymptoms: Boolean
) {
    val answers = listOf(
        "Age" to "$age",
        "Screen time" to screenTime.label,
        "Correction" to correction.label,
        "Symptoms" to symptoms.joinToString { it.label }.ifEmpty { "None" },
        "Diagnosed" to clinicalContexts.joinToString { it.label }.ifEmpty { "None" },
        "Urgent signs" to if (urgentSymptoms) "Yes" else "No"
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            color = AppTheme.colors.surface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "YOUR ANSWERS",
                    color = AppTheme.colors.teal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                answers.forEach { (label, value) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(label, color = AppTheme.colors.textMedium, fontSize = 13.sp, modifier = Modifier.weight(0.4f))
                        Text(value, color = AppTheme.colors.textHigh, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.weight(0.6f))
                    }
                }
            }
        }
        Surface(
            color = AppTheme.colors.amber.copy(alpha = 0.10f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "The plan supports comfort and healthy viewing habits. It cannot diagnose an eye condition, improve a glasses prescription, or replace an eye examination.",
                color = AppTheme.colors.textHigh,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun QuestionCard(
    number: Int,
    title: String,
    detail: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "QUESTION $number OF $ONBOARDING_QUESTION_COUNT",
                color = AppTheme.colors.teal,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(title, color = AppTheme.colors.textHigh, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = AppTheme.colors.textMedium, fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
private fun SingleChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AppTheme.colors.amber,
                unselectedColor = AppTheme.colors.textMuted
            )
        )
        Text(label, color = AppTheme.colors.textHigh, fontSize = 15.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun MultipleChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = AppTheme.colors.amber,
                checkmarkColor = AppTheme.colors.bg,
                uncheckedColor = AppTheme.colors.textMuted
            )
        )
        Text(label, color = AppTheme.colors.textHigh, fontSize = 15.sp, lineHeight = 20.sp)
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> =
    if (value in this) this - value else this + value
