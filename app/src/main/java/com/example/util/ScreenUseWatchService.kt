package com.example.util

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.Process
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val WATCH_CHANNEL_ID = "screen_use_watch"
private const val WATCH_NOTIFICATION_ID = 2021

/**
 * Polls the foreground app so a break can be offered after sustained content consumption
 * rather than on a clock that fires at an idle phone.
 *
 * A foreground service is the only supported shape for this: WorkManager's floor is 15
 * minutes, which cannot see a 20-minute scroll start, and AccessibilityService is the
 * other way to read the foreground app but Play restricts it to accessibility use.
 */
class ScreenUseWatchService : Service() {

    private var loop: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()
        if (loop?.isActive != true) loop = scope.launch { watch() }
        return START_STICKY
    }

    override fun onDestroy() {
        loop?.cancel()
        super.onDestroy()
    }

    private suspend fun watch() {
        val power = getSystemService(PowerManager::class.java)
        val usage = getSystemService(UsageStatsManager::class.java)
        var state = WatchState()

        while (scope.isActive) {
            delay(WATCH_POLL_MILLIS)
            val settings = UserPrefs(this).breakReminderSettings
            if (!settings.smartEnabled) {
                stopSelf()
                return
            }

            // A dark screen is not screen time, and off-hours nudges are the fastest way
            // to get notifications from this app turned off for good.
            val awake = power?.isInteractive == true
            val inWindow = isBreakReminderActiveAt(System.currentTimeMillis(), java.util.TimeZone.getDefault(), settings)
            val foreground = if (awake && inWindow) usage?.foregroundPackage() else null

            val step = advanceWatch(
                state = state,
                foregroundPackage = foreground,
                nowMillis = System.currentTimeMillis(),
                watched = WATCHED_SOCIAL_PACKAGES,
                thresholdMillis = settings.sanitized().intervalMinutes * 60_000L
            )
            state = step.state
            if (step.shouldNudge) BreakReminderScheduler.notifyBreakDue(this)
        }
    }

    private fun startForegroundCompat() {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(WATCH_CHANNEL_ID, "Smart break watch", NotificationManager.IMPORTANCE_MIN).apply {
                    description = "Kept quiet: this only says the smart break timer is running."
                }
            )
        }
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, WATCH_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Smart breaks are on")
            .setContentText("Watching for long stretches of scrolling.")
            .setContentIntent(open)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(WATCH_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(WATCH_NOTIFICATION_ID, notification)
        }
    }

    companion object {
        /**
         * Starts or stops the watcher to match settings. Safe to call on every settings
         * change, app start and boot: starting an already-running service just re-enters
         * onStartCommand, and the loop guard keeps that from stacking.
         */
        fun sync(context: Context) {
            val settings = UserPrefs(context).breakReminderSettings
            val intent = Intent(context, ScreenUseWatchService::class.java)
            if (settings.smartEnabled && hasUsageAccess(context)) {
                // startForegroundService needs API 26; minSdk is 24, so plain startService
                // is the only option below that -- onCreate still promotes itself with
                // startForeground() either way.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }

        /**
         * Usage Access cannot be requested with a runtime dialog; the user has to grant it
         * in system Settings, so this is a check, never a request.
         */
        fun hasUsageAccess(context: Context): Boolean {
            val ops = context.getSystemService(AppOpsManager::class.java) ?: return false
            // unsafeCheckOpNoThrow needs API 29; minSdk is 24, so older platforms fall back
            // to the deprecated equivalent rather than crashing on the usage-access check.
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            } else {
                @Suppress("DEPRECATION")
                ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }
    }
}

/**
 * The most recently resumed app, from the last two minutes of events.
 *
 * queryEvents is used rather than queryUsageStats because only the event stream says which
 * app is in front *now*; aggregated stats only say who used how much today.
 */
private fun UsageStatsManager.foregroundPackage(): String? {
    val now = System.currentTimeMillis()
    val events = queryEvents(now - 120_000, now)
    val event = UsageEvents.Event()
    var latest: String? = null
    while (events.hasNextEvent()) {
        events.getNextEvent(event)
        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) latest = event.packageName
    }
    return latest
}
