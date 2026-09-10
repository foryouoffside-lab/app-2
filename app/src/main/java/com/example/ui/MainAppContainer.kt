package com.example.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.launch
import com.example.data.AnchorBaselineEntity
import com.example.data.CategoryProgressionEntity
import com.example.data.CustomWorkoutEntity
import com.example.data.SessionLog
import com.example.data.SessionLogDao
import com.example.model.ExercisePhase
import com.example.model.ExerciseType
import com.example.model.Protocol
import com.example.model.ProtocolsRepository
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BiologicalTeal
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MutedBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis
import com.example.ui.theme.TextMuted
import com.example.ui.theme.ZincSurfaceElevated

enum class AppPillarTab {
    TODAY,
    TRAIN,
    PROGRESS
}

@Composable
fun MainAppContainer(
    sessionLogDao: SessionLogDao,
    onStartProtocol: (Protocol) -> Unit
) {
    var activeTab by remember { mutableStateOf(AppPillarTab.TODAY) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        bottomBar = {
            NavigationBar(
                containerColor = CharcoalSurface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = activeTab == AppPillarTab.TODAY,
                    onClick = { activeTab = AppPillarTab.TODAY },
                    icon = { Icon(Icons.Default.Visibility, contentDescription = "Today") },
                    label = { Text("Today") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberPrimary,
                        selectedTextColor = AmberPrimary,
                        indicatorColor = AmberPrimary.copy(alpha = 0.15f),
                        unselectedIconColor = TextMediumEmphasis,
                        unselectedTextColor = TextMediumEmphasis
                    ),
                    modifier = Modifier.testTag("nav_tab_today")
                )

                NavigationBarItem(
                    selected = activeTab == AppPillarTab.TRAIN,
                    onClick = { activeTab = AppPillarTab.TRAIN },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Train") },
                    label = { Text("Train") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberPrimary,
                        selectedTextColor = AmberPrimary,
                        indicatorColor = AmberPrimary.copy(alpha = 0.15f),
                        unselectedIconColor = TextMediumEmphasis,
                        unselectedTextColor = TextMediumEmphasis
                    ),
                    modifier = Modifier.testTag("nav_tab_train")
                )

                NavigationBarItem(
                    selected = activeTab == AppPillarTab.PROGRESS,
                    onClick = { activeTab = AppPillarTab.PROGRESS },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberPrimary,
                        selectedTextColor = AmberPrimary,
                        indicatorColor = AmberPrimary.copy(alpha = 0.15f),
                        unselectedIconColor = TextMediumEmphasis,
                        unselectedTextColor = TextMediumEmphasis
                    ),
                    modifier = Modifier.testTag("nav_tab_progress")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                AppPillarTab.TODAY -> TodayScreen(
                    onStartProtocol = onStartProtocol,
                    sessionLogDao = sessionLogDao
                )
                AppPillarTab.TRAIN -> TrainScreen(
                    onStartProtocol = onStartProtocol,
                    sessionLogDao = sessionLogDao
                )
                AppPillarTab.PROGRESS -> ProgressScreen(
                    sessionLogDao = sessionLogDao
                )
            }
        }
    }
}

@Composable
fun TodayScreen(
    onStartProtocol: (Protocol) -> Unit,
    sessionLogDao: SessionLogDao
) {
    val totalSessions by sessionLogDao.getTotalSessionsCount().collectAsState(initial = 0)
    val totalSeconds by sessionLogDao.getTotalRestSeconds().collectAsState(initial = 0)
    val instantProtocol = ProtocolsRepository.defaultProtocols.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EYE REST",
                        color = AmberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Desk Recovery",
                        color = TextHighEmphasis,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = ZincSurfaceElevated,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "WELLNESS ONLY",
                        color = BiologicalTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stage 13 Regulatory Intended-Use Ribbon
            Surface(
                color = ZincSurfaceElevated.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Non-Medical Wellness Software • Not intended to diagnose or treat eye disease or refractive errors. In case of acute pain or visual drop, seek urgent medical care.",
                    color = TextMediumEmphasis,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // PRIMARY ACTION CARD: "Instant Screen Reset"
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AmberPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "QUICK BREAK • 60 SEC",
                                color = AmberPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "3 Phases",
                            color = TextMediumEmphasis,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = instantProtocol.title,
                        color = TextHighEmphasis,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = instantProtocol.description,
                        color = TextMediumEmphasis,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onStartProtocol(instantProtocol) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberPrimary,
                            contentColor = ObsidianBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_instant_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Start 60s Reset",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // SYMPTOM-TARGETED PRESETS
        item {
            Text(
                text = "Targeted Recovery",
                color = TextHighEmphasis,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(ProtocolsRepository.defaultProtocols.drop(1)) { protocol ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartProtocol(protocol) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = protocol.targetSymptom.uppercase(),
                            color = BiologicalTeal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = protocol.title,
                            color = TextHighEmphasis,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${protocol.totalSeconds}s • ${protocol.phases.size} phases",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        color = ZincSurfaceElevated,
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Launch",
                                tint = AmberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // DAY SUMMARY STATS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ZincSurfaceElevated.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalSessions",
                            color = TextHighEmphasis,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Sessions Logged",
                            color = TextMediumEmphasis,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 1.dp, height = 30.dp)
                            .background(MutedBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val minutes = totalSeconds / 60
                        Text(
                            text = "${minutes}m",
                            color = AmberPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Total Ocular Rest",
                            color = TextMediumEmphasis,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrainScreen(
    onStartProtocol: (Protocol) -> Unit,
    sessionLogDao: SessionLogDao
) {
    var showBuilderDialog by remember { mutableStateOf(false) }
    val customWorkouts by sessionLogDao.getAllCustomWorkouts().collectAsStateWithLifecycle(initialValue = emptyList())
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "TRAINING PROTOCOLS",
                color = AmberPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Studio & Protocols",
                color = TextHighEmphasis,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Custom-compose personalized sessions or train with validated clinical presets.",
                color = TextMediumEmphasis,
                fontSize = 13.sp
            )
        }

        // STAGE 11: PERFORMANCE CHALLENGE ENGINE CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ZincSurfaceElevated.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AmberPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "STAGE 11 CHALLENGE",
                                color = AmberPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "30s SPRINT",
                            color = BiologicalTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Saccadic Reflex Benchmark",
                        color = TextHighEmphasis,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Standardized 4-quadrant motor acquisition test. Measures median reaction latency without medical diagnostic claims.",
                        color = TextMediumEmphasis,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val challengeProtocol = Protocol(
                                id = "CHAL_SAC_01",
                                title = "Saccadic Reflex Benchmark",
                                tag = "Benchmark",
                                totalSeconds = 30,
                                description = "30s standardized 4-quadrant latency benchmark",
                                targetSymptom = "Motor reaction latency and foveal acquisition calibration",
                                phases = listOf(
                                    ExercisePhase(
                                        title = "Saccade Step Jump",
                                        instruction = "Acquire peripheral targets as quickly as possible. Keep head fixed.",
                                        durationSeconds = 30,
                                        type = ExerciseType.SACCADE_JUMP,
                                        physiologicalBenefit = "Quantifies saccadic reaction time and foveal motor speed."
                                    )
                                )
                            )
                            onStartProtocol(challengeProtocol)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiologicalTeal,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Start 30s Benchmark", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // CUSTOM ROUTINE STUDIO HERO ACTION CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AmberPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "CUSTOM STUDIO",
                                color = AmberPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Safety Enforced",
                            color = BiologicalTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Build Personalized Routine",
                        color = TextHighEmphasis,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Select, order, and tune exercise modules (15s to 360s). Safe velocity and size clamps active.",
                        color = TextMediumEmphasis,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showBuilderDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Compose New Routine", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // USER SAVED ROUTINES SECTION (IF ANY)
        if (customWorkouts.isNotEmpty()) {
            item {
                Text(
                    text = "MY SAVED ROUTINES",
                    color = AmberPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(customWorkouts) { workout ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ZincSurfaceElevated.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = workout.title,
                                color = TextHighEmphasis,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${workout.estimatedTotalSeconds}s",
                                color = BiologicalTeal,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Configured modules: ${workout.serializedDrills}",
                            color = TextMediumEmphasis,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Parse and launch
                                    val phases = workout.serializedDrills.split(",").mapNotNull { entry ->
                                        val parts = entry.split(":")
                                        if (parts.size == 2) {
                                            val drillName = parts[0]
                                            val duration = parts[1].toIntOrNull() ?: 20
                                            val exType = try { ExerciseType.valueOf(drillName) } catch (e: Exception) { ExerciseType.RAPID_BLINK }
                                            ExercisePhase(
                                                title = workout.title,
                                                instruction = "Follow the guided visual target smoothly.",
                                                durationSeconds = duration,
                                                type = exType,
                                                physiologicalBenefit = "Visual recovery cycle"
                                            )
                                        } else null
                                    }
                                    if (phases.isNotEmpty()) {
                                        onStartProtocol(
                                            Protocol(
                                                id = workout.workoutId,
                                                title = workout.title,
                                                description = "Saved custom session.",
                                                tag = "Custom",
                                                totalSeconds = workout.estimatedTotalSeconds,
                                                targetSymptom = "Personalized Recovery",
                                                phases = phases
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZincSurfaceElevated,
                                    contentColor = TextHighEmphasis
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Launch", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        sessionLogDao.deleteCustomWorkout(workout.workoutId)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CharcoalSurface,
                                    contentColor = Color(0xFFEF4444)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "Delete", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "STANDARDIZED CLINICAL PROTOCOLS",
                color = AmberPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        items(ProtocolsRepository.defaultProtocols) { protocol ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = BiologicalTeal.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = protocol.tag.uppercase(),
                                color = BiologicalTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "${protocol.totalSeconds}s",
                            color = AmberPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = protocol.title,
                        color = TextHighEmphasis,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = protocol.description,
                        color = TextMediumEmphasis,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phase breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        protocol.phases.forEachIndexed { index, phase ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${index + 1}.",
                                    color = AmberPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${phase.title} (${phase.durationSeconds}s)",
                                    color = TextHighEmphasis,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onStartProtocol(protocol) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZincSurfaceElevated,
                            contentColor = TextHighEmphasis
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Launch Protocol", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(
    sessionLogDao: SessionLogDao
) {
    val logs by sessionLogDao.getAllLogs().collectAsState(initial = emptyList())
    val totalSessions by sessionLogDao.getTotalSessionsCount().collectAsState(initial = 0)
    val totalSeconds by sessionLogDao.getTotalRestSeconds().collectAsState(initial = 0)
    val baselines by sessionLogDao.getAllAnchorBaselines().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showWipeConfirmDialog by remember { mutableStateOf(false) }

    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            containerColor = CharcoalSurface,
            title = {
                Text(
                    text = "Wipe All Training Telemetry?",
                    color = TextHighEmphasis,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Stage 12 Privacy Mandate: This will permanently delete all local session logs and progression events from this device. Baseline records will remain intact.",
                    color = TextMediumEmphasis,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            sessionLogDao.clearAllSessionLogs()
                            sessionLogDao.clearAllProgressionEvents()
                        }
                        showWipeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showWipeConfirmDialog = false }) {
                    Text("Cancel", color = TextMediumEmphasis)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "STAGE 12 ANALYTICS & TELEMETRY",
                color = AmberPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Progress & Health Signals",
                color = TextHighEmphasis,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "All training data stored strictly on-device. Zero cloud transmission of raw oculomotor events.",
                color = TextMediumEmphasis,
                fontSize = 13.sp
            )
        }

        // Aggregate Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalSessions",
                            color = AmberPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Total Sessions",
                            color = TextMediumEmphasis,
                            fontSize = 12.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val minutes = totalSeconds / 60
                        Text(
                            text = "${minutes}m",
                            color = BiologicalTeal,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Eye Rest Time",
                            color = TextMediumEmphasis,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Adaptive Oculomotor Mastery Levels
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ADAPTIVE PROGRESSION",
                            color = AmberPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Hysteresis Active",
                            color = BiologicalTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Smooth Pursuit Speed",
                                color = TextHighEmphasis,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "14.0°/sec (Level 1 • Safe Floor)",
                                color = TextMediumEmphasis,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "LEARNING",
                            color = AmberPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Saccade Reaction Latency",
                                color = TextHighEmphasis,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Standardized Anchor: 4-Quadrant",
                                color = TextMediumEmphasis,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "CALIBRATED",
                            color = BiologicalTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 30-Day Cycle Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ZincSurfaceElevated.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "30-Day Visual Recovery Cycle",
                            color = TextHighEmphasis,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Day ${(totalSessions % 30).coerceAtLeast(1)} of 30 • Phase 1: Habit Foundation",
                            color = TextMediumEmphasis,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "CYCLE 1",
                        color = AmberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Text(
                text = "Session History",
                color = TextHighEmphasis,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ZincSurfaceElevated.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No sessions logged yet",
                            color = TextMediumEmphasis,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start your first 60-second screen reset from the Today tab.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column {
                                Text(
                                    text = log.protocolTitle,
                                    color = TextHighEmphasis,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${log.durationSeconds}s duration",
                                    color = TextMediumEmphasis,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "COMPLETED",
                            color = EmeraldSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Stage 12: Data Sovereignty & Privacy Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ZincSurfaceElevated.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DATA PRIVACY & SOVEREIGNTY",
                        color = AmberPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your oculomotor performance records never leave this hardware. You have total sovereign authority to wipe your telemetry at any moment.",
                        color = TextMediumEmphasis,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showWipeConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Wipe All Local Telemetry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
