package com.example.util

/**
 * Decides when uninterrupted content consumption has earned a distance break.
 *
 * Kept as pure data so the rule can be tested without a device, a service or a clock:
 * everything platform-shaped (who is in the foreground, whether the screen is on) is
 * resolved by the caller and handed in as [foregroundPackage].
 */

/** How often [ScreenUseWatchService] samples the foreground app. */
const val WATCH_POLL_MILLIS = 15_000L

/**
 * A sample gap longer than this means the service was dozed, killed or the phone was
 * asleep -- time the person was demonstrably not scrolling. Counting it would fire a
 * nudge the moment they picked the phone up, which reads as a bug, not a reminder.
 */
private const val MAX_CREDITED_GAP_MILLIS = WATCH_POLL_MILLIS * 3

data class WatchState(
    val continuousMillis: Long = 0,
    val lastSampleAt: Long = 0
)

data class WatchStep(val state: WatchState, val shouldNudge: Boolean)

/**
 * Advances the accumulator by one sample.
 *
 * Switching Instagram -> YouTube does not reset the count: it is the same near-focus
 * screen work the 20-20-20 interval is about, and resetting would let app-hopping
 * dodge every break. Leaving watched apps entirely -- or the screen going off -- does.
 */
fun advanceWatch(
    state: WatchState,
    foregroundPackage: String?,
    nowMillis: Long,
    watched: Set<String>,
    thresholdMillis: Long
): WatchStep {
    if (foregroundPackage == null || foregroundPackage !in watched) {
        return WatchStep(WatchState(lastSampleAt = nowMillis), shouldNudge = false)
    }

    val gap = (nowMillis - state.lastSampleAt).coerceAtLeast(0)
    val credited = if (state.lastSampleAt == 0L || gap > MAX_CREDITED_GAP_MILLIS) 0 else gap
    val total = state.continuousMillis + credited

    return if (total >= thresholdMillis) {
        // Reset on nudge so the next break is a full interval away, not one sample later.
        WatchStep(WatchState(lastSampleAt = nowMillis), shouldNudge = true)
    } else {
        WatchStep(WatchState(continuousMillis = total, lastSampleAt = nowMillis), shouldNudge = false)
    }
}

/**
 * Apps whose use is sustained near-focus content consumption.
 *
 * Hard-coded rather than "every installed app" on purpose: reading the full app list needs
 * QUERY_ALL_PACKAGES, which Play treats as sensitive and which this feature does not need.
 * Each entry here is also declared in <queries> in the manifest, or usage events for it
 * would be filtered out on Android 11+.
 *
 * ponytail: a fixed list means a new app needs a release to be covered. If that becomes a
 * real complaint, the upgrade is a user-facing picker backed by <queries> intent filters.
 */
val WATCHED_SOCIAL_PACKAGES: Set<String> = setOf(
    "com.instagram.android",
    "com.zhiliaoapp.musically", // TikTok
    "com.google.android.youtube",
    "com.facebook.katana",
    "com.twitter.android",
    "com.x.android",
    "com.snapchat.android",
    "com.reddit.frontpage",
    "com.pinterest",
    "in.mohalla.sharechat",
    "com.next.innovation.takatak",
    "com.netflix.mediaclient",
    "com.whatsapp"
)
