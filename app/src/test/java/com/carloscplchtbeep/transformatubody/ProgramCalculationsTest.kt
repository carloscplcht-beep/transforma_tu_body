package com.carloscplchtbeep.transformatubody

import com.carloscplchtbeep.transformatubody.domain.ProgramCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramCalculationsTest {
    @Test
    fun currentDayIsClampedToProgramRange() {
        assertEquals(1, ProgramCalculations.currentProgramDay(100, 99))
        assertEquals(1, ProgramCalculations.currentProgramDay(100, 100))
        assertEquals(30, ProgramCalculations.currentProgramDay(100, 160))
    }

    @Test
    fun progressPercentUsesThirtyDays() {
        assertEquals(0, ProgramCalculations.progressPercent(0))
        assertEquals(50, ProgramCalculations.progressPercent(15))
        assertEquals(100, ProgramCalculations.progressPercent(30))
    }

    @Test
    fun averageWeightIgnoresMissingValues() {
        assertEquals(76.0, ProgramCalculations.averageWeight(listOf(75.5, 76.5, null))!!, 0.01)
        assertNull(ProgramCalculations.averageWeight(listOf(null, null)))
    }

    @Test
    fun variationAndValidationWork() {
        assertEquals(-1.2, ProgramCalculations.variation(76.2, 75.0)!!, 0.01)
        assertTrue(ProgramCalculations.isValidMeasurement(76.0, 92.0))
        assertFalse(ProgramCalculations.isValidMeasurement(10.0, 92.0))
        assertFalse(ProgramCalculations.isValidMeasurement(76.0, 300.0))
    }
}

