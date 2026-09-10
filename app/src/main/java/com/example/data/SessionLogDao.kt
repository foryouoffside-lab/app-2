package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionLogDao {
    @Query("SELECT * FROM session_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SessionLog>>

    @Query("SELECT COUNT(*) FROM session_logs")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM session_logs")
    fun getTotalRestSeconds(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SessionLog)

    // Progression Queries
    @Query("SELECT * FROM category_progression WHERE categoryId = :categoryId")
    suspend fun getCategoryProgression(categoryId: String): CategoryProgressionEntity?

    @Query("SELECT * FROM category_progression")
    fun getAllCategoryProgressions(): Flow<List<CategoryProgressionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryProgression(progression: CategoryProgressionEntity)

    // Anchor Baselines
    @Query("SELECT * FROM anchor_baselines ORDER BY dayNumber ASC")
    fun getAllAnchorBaselines(): Flow<List<AnchorBaselineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchorBaseline(baseline: AnchorBaselineEntity)

    // Progression Audit Events
    @Query("SELECT * FROM progression_events ORDER BY timestamp DESC LIMIT 20")
    fun getRecentProgressionEvents(): Flow<List<ProgressionEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressionEvent(event: ProgressionEventEntity)

    // Custom Workouts (Stage 7 & Stage 8)
    @Query("SELECT * FROM custom_workouts ORDER BY createdAtTimestamp DESC")
    fun getAllCustomWorkouts(): Flow<List<CustomWorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomWorkout(workout: CustomWorkoutEntity)

    @Query("DELETE FROM custom_workouts WHERE workoutId = :workoutId")
    suspend fun deleteCustomWorkout(workoutId: String)

    // Privacy & Data Sovereignty (Stage 12)
    @Query("DELETE FROM session_logs")
    suspend fun clearAllSessionLogs()

    @Query("DELETE FROM progression_events")
    suspend fun clearAllProgressionEvents()
}
