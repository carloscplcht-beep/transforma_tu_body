package com.carloscplchtbeep.transformatubody.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM session_progress ORDER BY day")
    fun observeSessions(): Flow<List<SessionProgressEntity>>

    @Query("SELECT * FROM session_progress WHERE day = :day")
    fun observeSession(day: Int): Flow<SessionProgressEntity?>

    @Upsert
    suspend fun upsertSession(entity: SessionProgressEntity)

    @Query("DELETE FROM session_progress")
    suspend fun clearSessions()

    @Query("SELECT * FROM measurements ORDER BY checkpointDay")
    fun observeMeasurements(): Flow<List<MeasurementEntity>>

    @Upsert
    suspend fun upsertMeasurement(entity: MeasurementEntity)

    @Query("DELETE FROM measurements WHERE checkpointDay = :day")
    suspend fun deleteMeasurement(day: Int)

    @Query("DELETE FROM measurements")
    suspend fun clearMeasurements()
}

