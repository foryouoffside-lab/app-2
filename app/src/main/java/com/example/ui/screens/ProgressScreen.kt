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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SessionLog
import com.example.data.SessionLogDao
import com.example.model.WEEKLY_CARE_GOAL
import com.example.model.calculateHabitStats
import com.example.ui.components.StatMetricCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    sessionLogDao: SessionLogDao,
    modifier: Modifier = Modifier
) {
    val totalSessions by sessionLogDao.getTotalSessionsCount().collectAsStateWithLifecycle(initialValue = 0)
    val totalRestSeconds by sessionLogDao.getTotalRestSeconds().collectAsStateWithLifecycle(initialValue = 0)
    val allLogs by sessionLogDao.getAllLogs().collectAsStateWithLifecycle(initialValue = emptyList())
    val habitStats = remember(allLogs) { calculateHabitStats(allLogs) }

    val coroutineScope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = AppTheme.colors.surface,
            title = {
                Text(
                    text = "Reset Session History?",
                    color = AppTheme.colors.textHigh,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will clear all logged eye rest sessions from your device. This cannot be undone.",
                    color = AppTheme.colors.textMedium,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            sessionLogDao.clearAllSessionLogs()
                        }
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.rose)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = AppTheme.colors.textHigh)
                }
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
                            text = "Insights & Habits",
                            color = AppTheme.colors.textHigh,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Your daily screen break journey & visual health",
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
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = AppTheme.colors.amberGlow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Stats Overview Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val minutesRested = totalRestSeconds / 60
                StatMetricCard(
                    title = "Total Sessions",
                    value = "$totalSessions",
                    subtitle = "Lifetime",
                    icon = Icons.Default.CheckCircle,
                    accentColor = AppTheme.colors.emerald,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Minutes Rested",
                    value = "${minutesRested}m",
                    subtitle = "Rest Time",
                    icon = Icons.Default.Schedule,
                    accentColor = AppTheme.colors.iris,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Consistency Chart
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Activity",
                            color = AppTheme.colors.textHigh,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Last 7 Days",
                            color = AppTheme.colors.textMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    val days = recentDayLabels()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.zip(habitStats.lastSevenDays).forEach { (day, careDay) ->
                            val isToday = careDay.isToday
                            val hasActivity = careDay.completed

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(if (hasActivity) 54.dp else 24.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(
                                            if (isToday) {
                                                Modifier.background(Brush.verticalGradient(listOf(AppTheme.colors.teal, AppTheme.colors.iris)))
                                            } else {
                                                Modifier.background(if (hasActivity) AppTheme.colors.surfaceElevated else AppTheme.colors.surfaceElevated.copy(alpha = 0.4f))
                                            }
                                        )
                                )

                                Text(
                                    text = day,
                                    color = if (isToday) AppTheme.colors.teal else AppTheme.colors.textMedium,
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Care rhythm: one meaningful completion per day, never per-session grinding.
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppTheme.colors.amberGlow.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = AppTheme.colors.amberGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Care Rhythm",
                                color = AppTheme.colors.textHigh,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${habitStats.activeDaysLastSeven}/$WEEKLY_CARE_GOAL this week",
                            color = AppTheme.colors.teal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val target = habitStats.nextMilestone
                    val progressFraction = if (target == null) 1f else
                        (habitStats.longestStreak.toFloat() / target).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = AppTheme.colors.teal,
                        trackColor = AppTheme.colors.surfaceElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = buildString {
                            append("Current ${habitStats.currentStreak} days · best ${habitStats.longestStreak} · ${habitStats.totalCareDays} total care days. ")
                            if (target != null) append("Next badge at $target days. ")
                            append("Only one session counts each day; rest is part of the plan.")
                        },
                        color = AppTheme.colors.textMedium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Recent Sessions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sessions",
                    color = AppTheme.colors.textHigh,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (allLogs.isNotEmpty()) {
                    Text(
                        text = "Clear",
                        color = AppTheme.colors.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showResetDialog = true }
                    )
                }
            }
        }

        if (allLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = AppTheme.colors.textMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No sessions completed yet",
                            color = AppTheme.colors.textHigh,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start your first 60s eye reset from the Today tab.",
                            color = AppTheme.colors.textMedium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(allLogs.take(15), key = { it.id }) { log ->
                val formatter = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }
                val dateString = remember(log.timestamp) { formatter.format(Date(log.timestamp)) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = BorderStroke(1.dp, AppTheme.colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppTheme.colors.teal.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = AppTheme.colors.teal,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.protocolTitle,
                                color = AppTheme.colors.textHigh,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateString,
                                color = AppTheme.colors.textMuted,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            color = AppTheme.colors.surfaceElevated,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${log.durationSeconds}s",
                                color = AppTheme.colors.amberGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun recentDayLabels(): List<String> {
    val formatter = SimpleDateFormat("EEEEE", Locale.getDefault())
    val calendar = java.util.Calendar.getInstance()
    return (6 downTo 0).map { daysAgo ->
        (calendar.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, -daysAgo) }
            .let { formatter.format(it.time).take(1) }
    }
}
