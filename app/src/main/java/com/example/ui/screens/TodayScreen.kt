package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SessionLogDao
import com.example.model.DailyPlanRepository
import com.example.model.DailyRecommendation
import com.example.model.Protocol
import com.example.model.StudioDrill
import com.example.model.WellnessProfile
import com.example.model.localDayKey
import com.example.ui.components.HeroRoutineCard
import com.example.ui.components.RoutineCard
import com.example.ui.drill.DrillSheet
import com.example.ui.theme.AppTheme

@Composable
fun TodayScreen(
    sessionLogDao: SessionLogDao,
    profile: WellnessProfile,
    onStartProtocol: (Protocol) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSessions by sessionLogDao.getTotalSessionsCount().collectAsStateWithLifecycle(initialValue = 0)
    val totalRestSeconds by sessionLogDao.getTotalRestSeconds().collectAsStateWithLifecycle(initialValue = 0)
    val dayKey = localDayKey()
    val plan = remember(profile, dayKey) { DailyPlanRepository.forDay(profile, dayKey) }
    var openHabit by remember { mutableStateOf<StudioDrill?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Today", color = AppTheme.colors.textHigh, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = planSummary(plan.guided.size, plan.guided.sumOf { it.protocol.totalSeconds }, plan.isPausedForSafety),
                        color = AppTheme.colors.textMedium,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.width(16.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(AppTheme.colors.teal.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Default.Spa, contentDescription = null, tint = AppTheme.colors.teal, modifier = Modifier.size(20.dp))
                }
            }
        }

        plan.safetyMessage?.let { message ->
            item { SafetyPlanCard(message, plan.isPausedForSafety) }
        }

        if (!plan.isPausedForSafety) {
            val first = plan.guided.firstOrNull()
            if (first != null) {
                item {
                    HeroRoutineCard(
                        protocol = first.protocol,
                        onStart = { onStartProtocol(first.protocol) },
                        modifier = Modifier.testTag("daily_primary_drill")
                    )
                }
                if (plan.guided.size > 1) {
                    item { SectionTitle("Up next") }
                }
                items(plan.guided.drop(1), key = { it.drill.id }) { recommendation ->
                    RoutineCard(
                        protocol = recommendation.protocol,
                        icon = Icons.Default.Visibility,
                        iconTint = if (recommendation.isCore) AppTheme.colors.teal else AppTheme.colors.iris,
                        onStart = { onStartProtocol(recommendation.protocol) }
                    )
                }
            } else {
                item { RestDayCard() }
            }

            if (plan.habits.isNotEmpty()) {
                item {
                    SectionTitle("Daily habits")
                }
                items(plan.habits, key = { it.drill.id }) { recommendation ->
                    HabitPlanCard(recommendation) { openHabit = recommendation.drill }
                }
            }

            item {
                ProgressSummary(
                    sessions = totalSessions,
                    practiceMinutes = totalRestSeconds / 60
                )
            }

            item {
                Surface(
                    color = AppTheme.colors.bg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = AppTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "For comfort and healthy habits—not vision correction or medical treatment.",
                            color = AppTheme.colors.textMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    openHabit?.let { drill -> DrillSheet(drill) { openHabit = null } }
}

private fun planSummary(drillCount: Int, totalSeconds: Int, paused: Boolean): String {
    if (paused) return "Your plan needs attention"
    if (drillCount == 0) return "A lighter day for your eyes"
    val duration = if (totalSeconds < 60) "under 1 min" else "${(totalSeconds + 59) / 60} min"
    return "$drillCount ${if (drillCount == 1) "drill" else "drills"} · $duration"
}

@Composable
private fun SafetyPlanCard(message: String, paused: Boolean) {
    Surface(
        color = AppTheme.colors.amber.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppTheme.colors.amber.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth().testTag(if (paused) "daily_plan_paused" else "daily_plan_notice")
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (paused) "PLAN PAUSED FOR SAFETY" else "IMPORTANT FOR YOUR PLAN",
                color = AppTheme.colors.amber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
            Text(message, color = AppTheme.colors.textHigh, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = AppTheme.colors.textHigh, fontSize = 17.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun HabitPlanCard(recommendation: DailyRecommendation, onOpen: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(AppTheme.colors.teal.copy(alpha = 0.14f))
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = AppTheme.colors.teal, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(recommendation.drill.name, color = AppTheme.colors.textHigh, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(
                    recommendation.reason,
                    color = AppTheme.colors.textMedium,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = AppTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProgressSummary(sessions: Int, practiceMinutes: Int) {
    Surface(
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactMetric(
                value = "$sessions",
                label = if (sessions == 1) "session" else "sessions",
                icon = { Icon(Icons.Default.CheckCircle, null, tint = AppTheme.colors.emerald, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(32.dp).background(AppTheme.colors.border))
            CompactMetric(
                value = "$practiceMinutes min",
                label = "practice",
                icon = { Icon(Icons.Default.Schedule, null, tint = AppTheme.colors.iris, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun CompactMetric(
    value: String,
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        icon()
        Column {
            Text(value, color = AppTheme.colors.textHigh, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(label, color = AppTheme.colors.textMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RestDayCard() {
    Surface(
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("No drill needed today", color = AppTheme.colors.textHigh, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                "For a symptom-free person with little screen exposure, adding eye exercises has no proven preventive benefit. Use the real-world actions below when relevant.",
                color = AppTheme.colors.textMedium,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}
