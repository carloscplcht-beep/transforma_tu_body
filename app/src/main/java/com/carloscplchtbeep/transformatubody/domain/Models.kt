package com.carloscplchtbeep.transformatubody.domain

data class Phase(
    val id: String,
    val title: String,
    val dayRange: IntRange,
    val description: String
)

data class ProgramDay(
    val day: Int,
    val phaseId: String,
    val title: String,
    val sessionRefs: List<String>,
    val prescription: String,
    val nutritionNote: String = "Prioriza proteina suficiente, verduras, agua y raciones medidas segun el documento.",
    val isRecovery: Boolean = false
)

data class Session(
    val id: String,
    val title: String,
    val description: String,
    val blocks: List<TrainingBlock>,
    val defaultRest: String? = null,
    val defaultIntensity: String? = null
)

data class TrainingBlock(
    val title: String,
    val instructions: String,
    val items: List<ExercisePrescription>
)

data class ExercisePrescription(
    val exerciseId: String,
    val prescription: String,
    val side: String? = null,
    val timedSeconds: Int? = null
)

data class Exercise(
    val id: String,
    val programName: String,
    val clearName: String,
    val material: List<String>,
    val bodyGroups: List<String>,
    val startPosition: String,
    val steps: List<String>,
    val breathing: String,
    val commonErrors: List<String>,
    val adaptation: String,
    val painWarning: String = "Detente si aparece dolor, mareo, opresion toracica o dificultad respiratoria intensa.",
    val appearsIn: List<String>
)

data class Menu(
    val id: String,
    val title: String,
    val meals: List<Meal>
)

data class Meal(
    val name: String,
    val items: List<String>
)

data class NutritionContent(
    val proteinGoal: List<String>,
    val dailyStructure: List<String>,
    val carbDistribution: List<String>,
    val fats: List<String>,
    val menus: List<Menu>,
    val priorityFoods: List<String>,
    val eliminatedFoods: List<String>,
    val celebrationAlternative: String,
    val adjustments: List<String>
)

data class StaticGuide(
    val objective: List<String>,
    val rules: List<String>,
    val effortScale: List<String>,
    val warmUp: List<String>,
    val coolDown: List<String>,
    val dumbbellProgression: List<String>,
    val resultControl: List<String>,
    val safety: List<String>,
    val finalFactors: String
)

data class ProgramContent(
    val phases: List<Phase>,
    val days: List<ProgramDay>,
    val sessions: List<Session>,
    val exercises: List<Exercise>,
    val nutrition: NutritionContent,
    val guide: StaticGuide,
    val measurementDays: List<Int>
)

data class ContentValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

object ProgramCalculations {
    fun currentProgramDay(startEpochDay: Long, todayEpochDay: Long): Int {
        val raw = (todayEpochDay - startEpochDay + 1).toInt()
        return raw.coerceIn(1, 30)
    }

    fun progressPercent(completedDays: Int): Int = ((completedDays.coerceIn(0, 30) / 30.0) * 100).toInt()

    fun averageWeight(values: List<Double?>): Double? {
        val valid = values.filterNotNull()
        return if (valid.isEmpty()) null else valid.average()
    }

    fun variation(first: Double?, last: Double?): Double? {
        return if (first == null || last == null) null else last - first
    }

    fun isValidMeasurement(weightKg: Double?, waistCm: Double?): Boolean {
        val weightOk = weightKg == null || weightKg in 30.0..250.0
        val waistOk = waistCm == null || waistCm in 40.0..200.0
        return weightOk && waistOk
    }
}

