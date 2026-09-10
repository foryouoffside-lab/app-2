package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val protocolId: String,
    val protocolTitle: String,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "category_progression")
data class CategoryProgressionEntity(
    @PrimaryKey val categoryId: String,
    val currentLevel: Int = 1,
    val currentVelocityDeg: Float = 14.0f,
    val currentScaleDp: Float = 36.0f,
    val consecutiveSuccessCount: Int = 0,
    val consecutiveFailureCount: Int = 0,
    val masteryState: String = "LEARNING",
    val lastTrainedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "anchor_baselines")
data class AnchorBaselineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val protocolId: String,
    val dayNumber: Int,
    val medianLatencyMs: Float,
    val accuracyPercentage: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "progression_events")
data class ProgressionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val categoryId: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_workouts")
data class CustomWorkoutEntity(
    @PrimaryKey val workoutId: String,
    val title: String,
    val estimatedTotalSeconds: Int,
    val isFavorite: Boolean = false,
    val serializedDrills: String, // Comma-separated or short format: e.g. "ACC_01:20,REC_01:15,TRK_01:25"
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
