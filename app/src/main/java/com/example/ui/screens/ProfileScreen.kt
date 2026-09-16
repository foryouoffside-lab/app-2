package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ageBandFor
import com.example.model.AgeBand
import com.example.model.nearTargetCm
import com.example.model.WellnessProfile
import com.example.ui.components.TargetSpeedBar
import com.example.ui.theme.AppTheme
import com.example.util.TargetColor
import com.example.util.TARGET_SPEED_RANGE
import com.example.util.targetSpeedLabel
import com.example.util.SUPPORTED_AGES
import com.example.util.ThemeMode
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    age: Int,
    wellnessProfile: WellnessProfile,
    themeMode: ThemeMode,
    voiceEnabled: Boolean,
    hapticsEnabled: Boolean,
    targetColor: TargetColor,
    targetSpeed: Float,
    onAgeChange: (Int) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onVoiceChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onTargetColorChange: (TargetColor) -> Unit,
    onTargetSpeedChange: (Float) -> Unit,
    onRetakeAssessment: () -> Unit
) {
    val colors = AppTheme.colors

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Settings",
                color = colors.textHigh,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingsCard(title = "Appearance") {
                SegmentedThemePicker(themeMode = themeMode, onThemeChange = onThemeChange)
            }
        }

        item {
            var draft by remember(age) { mutableIntStateOf(age) }
            SettingsCard(title = "Age") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$draft",
                        color = colors.textHigh,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = nearTargetCm(draft)?.let { "near target ${it}cm" }
                            ?: "no near drills",
                        color = colors.teal,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Slider(
                    value = draft.toFloat(),
                    onValueChange = { draft = it.roundToInt() },
                    onValueChangeFinished = { onAgeChange(draft) },
                    valueRange = SUPPORTED_AGES.first.toFloat()..SUPPORTED_AGES.last.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.amber,
                        activeTrackColor = colors.amber,
                        inactiveTrackColor = colors.surfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("settings_age")
                )
                Text(
                    text = ageNote(draft),
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
        }

        item {
            SettingsCard(title = "Daily plan") {
                Text(
                    text = wellnessProfile.screenTime.label + " screen time  |  " +
                        wellnessProfile.correction.label + "  |  " +
                        if (wellnessProfile.symptoms.isEmpty()) "No regular symptoms" else "${wellnessProfile.symptoms.size} symptom areas",
                    color = colors.textHigh,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRetakeAssessment,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.teal,
                        contentColor = colors.bg
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("update_daily_plan")
                ) {
                    Text("Update plan answers", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            SettingsCard(title = "Guidance") {
                ToggleRow(
                    label = "Voice coach",
                    detail = "Speaks each step during a drill.",
                    checked = voiceEnabled,
                    onChange = onVoiceChange,
                    tag = "toggle_voice"
                )
                Spacer(modifier = Modifier.height(4.dp))
                ToggleRow(
                    label = "Vibration",
                    detail = "A pulse at each stage change.",
                    checked = hapticsEnabled,
                    onChange = onHapticsChange,
                    tag = "toggle_haptics"
                )
            }
        }

        item {
            SettingsCard(title = "Target") {
                Text(
                    text = "TARGET COLOUR",
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TargetColor.entries.forEach { option ->
                        val selected = option == targetColor
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(option.argb).copy(alpha = if (selected) 0.30f else 0.12f))
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) Color(option.argb) else colors.border,
                                    shape = CircleShape
                                )
                                .clickable { onTargetColorChange(option) }
                                .testTag("target_color_${option.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(option.argb))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Applies to drills whose target is a plain mark. Drills where the colour " +
                        "is the exercise \u2014 the red and cyan fusion pair, the three-bead string \u2014 keep theirs.",
                    color = colors.textMedium,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                TargetSpeedBar(speed = targetSpeed, onSpeedChange = onTargetSpeedChange)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Changes how far a moving target travels, not how long the drill runs. " +
                        "The dose stays the dose.",
                    color = colors.textMedium,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

            }
        }

        item {
            Text(
                text = "Comfort practice, not treatment. See an optometrist for eye problems.",
                color = colors.textMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Why this age changes the drill, in one line. */
private fun ageNote(age: Int): String = when (ageBandFor(age)) {
    AgeBand.YOUTH -> "Breaks and comfort only at this age."
    AgeBand.ADULT -> "Sets how far your focus drills reach."
    AgeBand.EARLY_PRESBYOPIC -> "Near targets move further out from here."
    AgeBand.PRESBYOPIC -> "Blink and distance work replace near drills."
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppTheme.colors.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title.uppercase(),
                color = AppTheme.colors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/** One control, three equal segments, so it reads as a single choice rather than chips. */
@Composable
private fun SegmentedThemePicker(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceElevated)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            val selected = mode == themeMode
            Text(
                text = when (mode) {
                    ThemeMode.SYSTEM -> "Auto"
                    ThemeMode.LIGHT -> "Day"
                    ThemeMode.DARK -> "Night"
                },
                color = if (selected) colors.bg else colors.textMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (selected) colors.amber else colors.surfaceElevated)
                    .clickable { onThemeChange(mode) }
                    .padding(vertical = 11.dp)
                    .testTag("theme_${mode.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    detail: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    tag: String
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = colors.textHigh, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = detail, color = colors.textMuted, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.bg,
                checkedTrackColor = colors.amber,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceElevated,
                uncheckedBorderColor = colors.border
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
