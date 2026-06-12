package com.carloscplchtbeep.transformatubody

import com.carloscplchtbeep.transformatubody.content.TransformaProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentIntegrityTest {
    @Test
    fun programHasExactlyThirtyDays() {
        val days = TransformaProgram.content.days.map { it.day }
        assertEquals(30, days.size)
        assertEquals((1..30).toList(), days.sorted())
    }

    @Test
    fun requiredSessionsMenusAndCheckpointsExist() {
        val sessions = TransformaProgram.content.sessions.map { it.id }.toSet()
        assertTrue(listOf("F1", "F2", "F3", "C").all { it in sessions })
        assertEquals(4, TransformaProgram.content.nutrition.menus.size)
        assertEquals(listOf(1, 8, 15, 22, 30), TransformaProgram.content.measurementDays)
    }

    @Test
    fun allReferencedExercisesExist() {
        val exercises = TransformaProgram.content.exercises.map { it.id }.toSet()
        val referenced = TransformaProgram.content.sessions.flatMap { it.blocks }.flatMap { it.items }.map { it.exerciseId }
        assertTrue(referenced.all { it in exercises })
    }

    @Test
    fun validatorPassesEssentialAudit() {
        val result = TransformaProgram.validate()
        assertTrue(result.errors.joinToString(), result.isValid)
    }
}

