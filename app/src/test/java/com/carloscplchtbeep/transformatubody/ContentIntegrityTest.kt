package com.carloscplchtbeep.transformatubody

import com.carloscplchtbeep.transformatubody.content.TransformaProgram
import com.carloscplchtbeep.transformatubody.content.DailyPlanBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun baseSessionsHaveExpectedVisibleExercises() {
        assertEquals(
            listOf(
                "Sentadilla goblet",
                "Press de pecho con las dos mancuernas sobre el banco",
                "Remo con una mancuerna apoyado en el banco",
                "Peso muerto rumano con dos mancuernas",
                "Plancha frontal",
                "Marcha del granjero, de pie y con las mancuernas"
            ),
            DailyPlanBuilder.visibleExerciseNamesForSession("F1")
        )
        assertEquals(7, DailyPlanBuilder.visibleExerciseNamesForSession("F2").size)
        assertEquals(7, DailyPlanBuilder.visibleExerciseNamesForSession("F3").size)
        assertEquals(7, DailyPlanBuilder.visibleExerciseNamesForSession("C").size)
    }

    @Test
    fun allDaysGenerateDevelopedPlanWithResolvableTechniqueLinks() {
        val exerciseIds = TransformaProgram.content.exercises.map { it.id }.toSet()
        DailyPlanBuilder.developedPlansForAllDays().forEach { plan ->
            assertTrue("Dia ${plan.day.day} sin bloques principales", plan.mainBlocks.isNotEmpty())
            val items = listOf(plan.warmUp).plus(plan.mainBlocks).plus(plan.coolDown)
                .flatMap { it.groups }
                .flatMap { it.items }
            assertTrue("Dia ${plan.day.day} sin items", items.isNotEmpty())
            items.mapNotNull { it.exerciseId }.forEach { assertTrue("Ficha inexistente: $it", it in exerciseIds) }
        }
    }

    @Test
    fun visibleNamesDoNotUseInternalIdentifiers() {
        val exercises = TransformaProgram.content.exercises
        exercises.forEach { exercise ->
            assertFalse(exercise.programName == exercise.id)
        }
        DailyPlanBuilder.developedPlansForAllDays().flatMap { plan ->
            listOf(plan.warmUp).plus(plan.mainBlocks).plus(plan.coolDown)
        }.flatMap { it.groups }.flatMap { it.items }.forEach { item ->
            item.exerciseId?.let { assertFalse("Nombre visible tecnico en ${item.visibleName}", item.visibleName == it) }
        }
    }
}
