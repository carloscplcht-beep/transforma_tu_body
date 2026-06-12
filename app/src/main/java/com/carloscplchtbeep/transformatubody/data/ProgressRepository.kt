package com.carloscplchtbeep.transformatubody.data

class ProgressRepository(private val dao: ProgressDao) {
    val sessions = dao.observeSessions()
    val measurements = dao.observeMeasurements()

    fun session(day: Int) = dao.observeSession(day)

    suspend fun markDayComplete(day: Int, effort: Int? = null, durationMinutes: Int? = null, note: String? = null) {
        dao.upsertSession(
            SessionProgressEntity(
                day = day,
                completed = true,
                inProgressStep = 0,
                perceivedEffort = effort,
                durationMinutes = durationMinutes,
                note = note,
                completedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveSessionStep(day: Int, step: Int) {
        dao.upsertSession(
            SessionProgressEntity(
                day = day,
                completed = false,
                inProgressStep = step,
                perceivedEffort = null,
                durationMinutes = null,
                note = null,
                completedAtEpochMillis = null
            )
        )
    }

    suspend fun saveMeasurement(entity: MeasurementEntity) = dao.upsertMeasurement(entity)
    suspend fun deleteMeasurement(day: Int) = dao.deleteMeasurement(day)
    suspend fun clearAll() {
        dao.clearSessions()
        dao.clearMeasurements()
    }
}

