package com.carloscplchtbeep.transformatubody

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.carloscplchtbeep.transformatubody.data.ThemeChoice
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
}

