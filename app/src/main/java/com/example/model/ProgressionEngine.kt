package com.example.model

import com.example.data.CategoryProgressionEntity
import com.example.data.ProgressionEventEntity
import com.example.data.SessionLogDao

object ProgressionEngine {
    const val MAX_VELOCITY_DEG = 22.0f
    const val MIN_SCALE_DP = 24.0f
    const val DEFAULT_VELOCITY_DEG = 14.0f
    const val DEFAULT_SCALE_DP = 36.0f

    suspend fun recordDrillCompletion(
        categoryId: String,
        accuracy: Float, // 0.0 to 1.0
        sessionLogDao: SessionLogDao
    ): String {
        val current = sessionLogDao.getCategoryProgression(categoryId) ?: CategoryProgressionEntity(
            categoryId = categoryId,
            currentLevel = 1,
            currentVelocityDeg = DEFAULT_VELOCITY_DEG,
            currentScaleDp = DEFAULT_SCALE_DP
        )

        var newSuccess = current.consecutiveSuccessCount
        var newFailure = current.consecutiveFailureCount
        var newVelocity = current.currentVelocityDeg
        var newScale = current.currentScaleDp
        var newLevel = current.currentLevel
        var eventDesc = ""

        if (accuracy >= 0.85f) {
            newSuccess++
            newFailure = 0
            if (newSuccess >= 3 && newVelocity < MAX_VELOCITY_DEG) {
                newVelocity = (newVelocity + 1.5f).coerceAtMost(MAX_VELOCITY_DEG)
                newScale = (newScale - 2.0f).coerceAtLeast(MIN_SCALE_DP)
                newLevel = (newLevel + 1).coerceAtMost(5)
                newSuccess = 0
                eventDesc = "Promoted to Level $newLevel (+1.5°/s velocity) after 3 steady sessions."
                sessionLogDao.insertProgressionEvent(
                    ProgressionEventEntity(
                        eventType = "PROMOTED",
                        categoryId = categoryId,
                        description = eventDesc
                    )
                )
            } else {
                eventDesc = "Solid tracking performance recorded ($accuracy)."
            }
        } else if (accuracy <= 0.60f) {
            newFailure++
            newSuccess = 0
            if (newFailure >= 2 && newVelocity > 10.0f) {
                newVelocity = (newVelocity - 1.5f).coerceAtLeast(10.0f)
                newScale = (newScale + 3.0f).coerceAtMost(44.0f)
                newLevel = (newLevel - 1).coerceAtLeast(1)
                newFailure = 0
                eventDesc = "Reduced speed by 1.5°/s to relieve visual strain."
                sessionLogDao.insertProgressionEvent(
                    ProgressionEventEntity(
                        eventType = "REGRESSED",
                        categoryId = categoryId,
                        description = eventDesc
                    )
                )
            } else {
                eventDesc = "Maintenance session completed."
            }
        } else {
            eventDesc = "Steady performance maintained."
        }

        val updatedMastery = when (newLevel) {
            1 -> "LEARNING"
            2 -> "DEVELOPING"
            3 -> "STABLE"
            4 -> "ADVANCED"
            else -> "MASTERED"
        }

        sessionLogDao.upsertCategoryProgression(
            current.copy(
                currentLevel = newLevel,
                currentVelocityDeg = newVelocity,
                currentScaleDp = newScale,
                consecutiveSuccessCount = newSuccess,
                consecutiveFailureCount = newFailure,
                masteryState = updatedMastery,
                lastTrainedTimestamp = System.currentTimeMillis()
            )
        )

        return eventDesc
    }
}
