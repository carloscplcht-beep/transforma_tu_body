package com.carloscplchtbeep.transformatubody.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

enum class ThemeChoice { SYSTEM, LIGHT, DARK }

data class UserPreferences(
    val onboardingDone: Boolean = false,
    val safetyAccepted: Boolean = false,
    val startEpochDay: Long = LocalDate.now().toEpochDay(),
    val themeChoice: ThemeChoice = ThemeChoice.SYSTEM,
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
)

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val safetyAccepted = booleanPreferencesKey("safety_accepted")
        val startEpochDay = longPreferencesKey("start_epoch_day")
        val themeChoice = stringPreferencesKey("theme_choice")
        val remindersEnabled = booleanPreferencesKey("reminders_enabled")
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { data ->
        UserPreferences(
            onboardingDone = data[Keys.onboardingDone] ?: false,
            safetyAccepted = data[Keys.safetyAccepted] ?: false,
            startEpochDay = data[Keys.startEpochDay] ?: LocalDate.now().toEpochDay(),
            themeChoice = runCatching { ThemeChoice.valueOf(data[Keys.themeChoice] ?: ThemeChoice.SYSTEM.name) }.getOrDefault(ThemeChoice.SYSTEM),
            remindersEnabled = data[Keys.remindersEnabled] ?: false,
            reminderHour = data[Keys.reminderHour] ?: 8,
            reminderMinute = data[Keys.reminderMinute] ?: 0
        )
    }

    suspend fun completeOnboarding(startEpochDay: Long, theme: ThemeChoice, reminders: Boolean) {
        context.dataStore.edit { data ->
            data[Keys.onboardingDone] = true
            data[Keys.safetyAccepted] = true
            data[Keys.startEpochDay] = startEpochDay
            data[Keys.themeChoice] = theme.name
            data[Keys.remindersEnabled] = reminders
        }
    }

    suspend fun setTheme(theme: ThemeChoice) {
        context.dataStore.edit { it[Keys.themeChoice] = theme.name }
    }

    suspend fun setReminders(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.remindersEnabled] = enabled
            it[Keys.reminderHour] = hour
            it[Keys.reminderMinute] = minute
        }
    }

    suspend fun resetProgram() {
        context.dataStore.edit {
            it[Keys.startEpochDay] = LocalDate.now().toEpochDay()
            it[Keys.onboardingDone] = false
            it[Keys.safetyAccepted] = false
        }
    }
}

