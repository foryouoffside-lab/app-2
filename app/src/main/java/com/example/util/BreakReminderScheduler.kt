package com.example.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.BreakActivity
import com.example.R
import java.util.TimeZone

private const val BREAK_CHANNEL_ID = "screen_breaks"
private const val BREAK_ALARM_REQUEST = 202020
private const val BREAK_NOTIFICATION_ID = 2020

object BreakReminderScheduler {
    fun update(context: Context, settings: BreakReminderSettings) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val alarm = alarmIntent(context)
        alarmManager.cancel(alarm)

        val next = nextBreakReminderAt(System.currentTimeMillis(), TimeZone.getDefault(), settings) ?: return
        // A wellness nudge is not an alarm-clock emergency. Android recommends an inexact
        // alarm here so the system can protect battery life and avoid special access.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, alarm)
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            BREAK_CHANNEL_ID,
            "Screen break reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Gentle reminders to look away from a near screen"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** The one nudge, raised either by the clock alarm or by the foreground-app watcher. */
    fun notifyBreakDue(context: Context) {
        createChannel(context)
        val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!canNotify) return

        // Straight into the 20 seconds. Making the user find a Start button inside the
        // app is how a two-second nudge turns into a browsing session.
        val startBreak = PendingIntent.getActivity(
            context,
            0,
            Intent(context, BreakActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, BREAK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Give your eyes a distance break")
            .setContentText("Tap for a 20-second look-away, then straight back.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Look at a real object about 6 metres (20 feet) away for 20 seconds. " +
                        "Blink normally; this is a comfort break, not an eyesight treatment."
                )
            )
            .setContentIntent(startBreak)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(BREAK_NOTIFICATION_ID, notification)
    }

    private fun alarmIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        BREAK_ALARM_REQUEST,
        Intent(context, BreakReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

class BreakReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settings = UserPrefs(context).breakReminderSettings
        if (!settings.enabled) return

        if (isBreakReminderActiveAt(System.currentTimeMillis(), TimeZone.getDefault(), settings)) {
            BreakReminderScheduler.notifyBreakDue(context)
        }
        BreakReminderScheduler.update(context, settings)
    }
}

/** Rebuilds the one pending alarm after reboot, app replacement or a wall-clock change. */
class BreakReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        BreakReminderScheduler.update(context, UserPrefs(context).breakReminderSettings)
        ScreenUseWatchService.sync(context)
    }
}
