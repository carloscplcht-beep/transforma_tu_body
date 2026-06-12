package com.carloscplchtbeep.transformatubody

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carloscplchtbeep.transformatubody.content.DailyPlanBuilder
import com.carloscplchtbeep.transformatubody.data.ThemeChoice
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BasicComposeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun onboardingShowsProgramName() {
        compose.setContent {
            TransformaTheme(ThemeChoice.LIGHT) {
                OnboardingScreen { _, _ -> }
            }
        }
        compose.onNodeWithText("Transforma tu Body").assertIsDisplayed()
    }

    @Test
    fun nutritionShowsProteinGoal() {
        compose.setContent {
            TransformaTheme(ThemeChoice.LIGHT) {
                NutritionScreen()
            }
        }
        compose.onNodeWithText("Objetivo de proteina").assertIsDisplayed()
    }

    @Test
    fun dayOneShowsExpandedF1WithoutIndependentExerciseSection() {
        compose.setContent {
            TransformaTheme(ThemeChoice.LIGHT) {
                DayPlanForTest(1)
            }
        }
        compose.onNodeWithText("F1 · Fuerza general").assertIsDisplayed()
        compose.onNodeWithText("Sentadilla goblet").assertIsDisplayed()
        compose.onNodeWithText("Press de pecho con las dos mancuernas sobre el banco").assertIsDisplayed()
        compose.onNodeWithText("Remo con una mancuerna apoyado en el banco").assertIsDisplayed()
        compose.onNodeWithText("Peso muerto rumano con dos mancuernas").assertIsDisplayed()
        compose.onNodeWithText("Plancha frontal").assertIsDisplayed()
        compose.onNodeWithText("Marcha del granjero, de pie y con las mancuernas").assertIsDisplayed()
        assertEquals(0, compose.onAllNodesWithText("Ejercicios").fetchSemanticsNodes().size)
    }

    @Test
    fun exerciseNameOpensAndClosesTechniqueSheet() {
        compose.setContent {
            TransformaTheme(ThemeChoice.LIGHT) {
                var selected by remember { mutableStateOf<String?>(null) }
                PlanBlockCard(DailyPlanBuilder.build(1).mainBlocks.first(), openExercise = { selected = it })
                selected?.let { ExerciseTechniqueSheet(it) { selected = null } }
            }
        }
        compose.onNodeWithText("Sentadilla goblet").performClick()
        compose.onNodeWithText("Ficha tecnica").assertIsDisplayed()
        compose.onNodeWithText("X").performClick()
        compose.waitUntil { compose.onAllNodesWithText("Ficha tecnica").fetchSemanticsNodes().isEmpty() }
        assertEquals(0, compose.onAllNodesWithText("Ficha tecnica").fetchSemanticsNodes().size)
    }

    @Test
    fun representativeDaysShowTheirSpecificPlans() {
        compose.setContent {
            TransformaTheme(ThemeChoice.LIGHT) {
                androidx.compose.foundation.lazy.LazyColumn {
                    item { PlanBlockCard(DailyPlanBuilder.build(12).mainBlocks.first(), openExercise = {}) }
                    item { PlanBlockCard(DailyPlanBuilder.build(15).mainBlocks.first(), openExercise = {}) }
                    item { PlanBlockCard(DailyPlanBuilder.build(22).mainBlocks.first(), openExercise = {}) }
                    item { PlanBlockCard(DailyPlanBuilder.build(30).mainBlocks.first(), openExercise = {}) }
                }
            }
        }
        compose.onNodeWithText("C · Circuito metabolico").assertIsDisplayed()
        compose.onNodeWithText("Realizar 4 vueltas").assertIsDisplayed()
        compose.onNodeWithText("Eliptica continua 45 minutos a RPE 5-6. Los ultimos 8 minutos a RPE 7.").assertIsDisplayed()
        compose.onNodeWithText("F3 · Hipertrofia y definicion").assertIsDisplayed()
        compose.onNodeWithText("Sentadilla bulgara solo si la tecnica es estable.").assertIsDisplayed()
        compose.onNodeWithText("Repetir exactamente la sesion F1 del dia 1.").assertIsDisplayed()
    }
}

@androidx.compose.runtime.Composable
private fun DayPlanForTest(day: Int) {
    val plan = DailyPlanBuilder.build(day)
    androidx.compose.foundation.lazy.LazyColumn {
        item { DayPlanSummaryCard(plan, completed = false) }
        items(plan.mainBlocks.size) { index ->
            PlanBlockCard(plan.mainBlocks[index], openExercise = {})
        }
    }
}
