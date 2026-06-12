package com.carloscplchtbeep.transformatubody.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_progress")
data class SessionProgressEntity(
    @PrimaryKey val day: Int,
    val completed: Boolean,
    val inProgressStep: Int,
    val perceivedEffort: Int?,
    val durationMinutes: Int?,
    val note: String?,
    val completedAtEpochMillis: Long?
)

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey val checkpointDay: Int,
    val weight1Kg: Double?,
    val weight2Kg: Double?,
    val weight3Kg: Double?,
    val waistCm: Double?,
    val beltHole: String?,
    val frontPhotoUri: String?,
    val sidePhotoUri: String?,
    val notes: String?,
    val updatedAtEpochMillis: Long
)

