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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Spa
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SessionLogDao
import com.example.model.DailyPlanRepository
import com.example.model.HabitStats
import com.example.model.calculateHabitStats
import com.example.model.DailyRecommendation
import com.example.model.Protocol
import com.example.model.StudioDrill
import com.example.model.WellnessProfile
import com.example.model.localDayKey
import com.example.ui.components.HeroRoutineCard
import com.example.ui.components.RoutineCard
import com.example.ui.drill.DrillSheet
import com.example.ui.theme.AppTheme
import java.util.Calendar

/**
 * Home is deliberately one decision: where today stands, today's drill, then the
 * habits behind it. All-time totals and the rest of the guided queue live on
 * Progress and Train so this screen never becomes a dashboard to read instead of
 * a session to start.
 */
@Composable
fun TodayScreen(
    sessionLogDao: SessionLogDao,
    profile: WellnessProfile,
    onStartProtocol: (Protocol) -> Unit,
    modifier: Modifier = Modifier
) {
    val allLogs by sessionLogDao.getAllLogs().collectAsStateWithLifecycle(initialValue = emptyList())
    val dayKey = localDayKey()
    val plan = remember(profile) { DailyPlanRepository.forDay(profile) }
    val stats = remember(allLogs, dayKey) { calculateHabitStats(allLogs) }
    var openHabit by remember { mutableStateOf<StudioDrill?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(greeting(), color = AppTheme.colors.textHigh, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Let's take care of your eyes today.",
                    color = AppTheme.colors.textMuted,
                    fontSize = 14.sp
                )
            }
        }

        plan.safetyMessage?.let { message ->
            item { SafetyPlanCard(message, plan.isPausedForSafety) }
        }

        if (!plan.isPausedForSafety) {
            item { DailyProgressStrip(stats) }

            val first = plan.guided.firstOrNull()
            if (first != null) {
                item { SectionTitle("Today's guided set") }
                item {
                    HeroRoutineCard(
                        protocol = first.protocol,
                        onStart = { onStartProtocol(first.protocol) },
                        modifier = Modifier.testTag("daily_primary_drill")
                    )
                }
                items(plan.guided.drop(1), key = { it.drill.id }) { recommendation ->
                    RoutineCard(
                        protocol = recommendation.protocol,
                        icon = Icons.Default.Spa,
                        iconTint = AppTheme.colors.textHigh,
                        onStart = { onStartProtocol(recommendation.protocol) },
                        modifier = Modifier.testTag("daily_drill_${recommendation.drill.id}")
                    )
                }
            } else {
                item { RestDayCard() }
            }

            if (plan.habits.isNotEmpty()) {
                item { SectionTitle("Daily habits") }
                items(plan.habits, key = { it.drill.id }) { recommendation ->
                    HabitPlanCard(recommendation) { openHabit = recommendation.drill }
                }
            }

            item {
                Text(
                    text = "For comfort and healthy habits—not medical treatment.",
                    color = AppTheme.colors.textMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    openHabit?.let { drill -> DrillSheet(drill) { openHabit = null } }
}

/** A warmer opener than a bare "Today", the same low-cost personalization every wellness app uses. */
private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 0..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

/**
 * The one number worth coming back for: consecutive days, with the last seven as dots.
 * Totals ("all-time minutes") reward nothing a person can act on today, so they stay
 * on Progress; a streak and a visibly incomplete today do.
 */
@Composable
private fun DailyProgressStrip(stats: HabitStats) {
    val colors = AppTheme.colors
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (stats.todayComplete) colors.amber.copy(alpha = .55f) else colors.border),
        modifier = Modifier.fillMaxWidth().testTag("daily_progress_strip")
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        stats.currentStreak > 0 -> "${stats.currentStreak}-day streak"
                        else -> "Start your streak"
                    },
                    color = colors.textHigh,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (stats.todayComplete) "Today is done" else "One session counts today",
                    color = if (stats.todayComplete) colors.amber else colors.textMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                stats.lastSevenDays.forEach { day ->
                    Box(
                        Modifier.size(if (day.isToday) 13.dp else 9.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    day.completed -> colors.amber
                                    day.isToday -> colors.textMuted
                                    else -> colors.surfaceElevated
                                }
                            )
                    )
                }
            }
        }
    }
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
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(AppTheme.colors.surfaceElevated)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = AppTheme.colors.textHigh, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                recommendation.drill.name,
                color = AppTheme.colors.textHigh,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
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
                "Use the daily habits below when they apply.",
                color = AppTheme.colors.textMedium,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}
