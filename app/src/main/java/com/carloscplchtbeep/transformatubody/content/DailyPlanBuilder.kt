package com.carloscplchtbeep.transformatubody.content

import com.carloscplchtbeep.transformatubody.domain.DailyTrainingPlan
import com.carloscplchtbeep.transformatubody.domain.Exercise
import com.carloscplchtbeep.transformatubody.domain.ExercisePrescription
import com.carloscplchtbeep.transformatubody.domain.PlanBlock
import com.carloscplchtbeep.transformatubody.domain.PlanGroup
import com.carloscplchtbeep.transformatubody.domain.PlanItem
import com.carloscplchtbeep.transformatubody.domain.ProgramDay
import com.carloscplchtbeep.transformatubody.domain.Session

object DailyPlanBuilder {
    fun build(dayNumber: Int): DailyTrainingPlan {
        val content = TransformaProgram.content
        val day = content.days.first { it.day == dayNumber }
        val exerciseById = content.exercises.associateBy { it.id }
        val sessions = day.sessionRefs.mapNotNull { ref -> content.sessions.firstOrNull { it.id == ref } }
        val spec = DaySpecifics.forDay(day)
        return DailyTrainingPlan(
            day = day,
            headline = headline(day, sessions),
            statusLabels = spec.labels,
            beforeStart = beforeStart(day),
            warmUp = warmUp(exerciseById),
            mainBlocks = sessions.map { expandSession(day, it, spec, exerciseById) },
            coolDown = coolDown()
        )
    }

    fun developedPlansForAllDays(): List<DailyTrainingPlan> = (1..30).map { build(it) }

    fun visibleExerciseNamesForSession(sessionId: String): List<String> {
        val content = TransformaProgram.content
        val exerciseById = content.exercises.associateBy { it.id }
        val session = content.sessions.first { it.id == sessionId }
        return session.blocks.flatMap { it.items }.mapNotNull { exerciseById[it.exerciseId]?.programName }
    }

    private fun headline(day: ProgramDay, sessions: List<Session>): String {
        val readable = sessions.joinToString(" + ") { readableSessionTitle(it.id) }
        return "Dia ${day.day} · ${day.title}${if (readable.isNotBlank()) " · $readable" else ""}"
    }

    private fun beforeStart(day: ProgramDay): List<PlanItem> {
        return when (day.day) {
            1 -> listOf(
                PlanItem(visibleName = "Medir el peso", detail = "En ayunas, despues de ir al bano."),
                PlanItem(visibleName = "Medir la cintura", detail = "A la altura del ombligo."),
                PlanItem(visibleName = "Anotar repeticiones", detail = "Registra las repeticiones realizadas."),
                PlanItem(visibleName = "Anotar tiempo total", detail = "Sirve para comparar con el dia 30.")
            )
            30 -> listOf(
                PlanItem(visibleName = "Medicion matinal", detail = "Peso, cintura, fotografias y agujero del cinturon."),
                PlanItem(visibleName = "Comparacion final", detail = "Compara repeticiones, tiempo y percepcion de esfuerzo con el dia 1.")
            )
            in listOf(8, 15, 22) -> listOf(
                PlanItem(visibleName = "Control de resultados", detail = "Peso medio de tres mananas, cintura, fotos y cinturon.")
            )
            else -> emptyList()
        }
    }

    private fun warmUp(exerciseById: Map<String, Exercise>) = PlanBlock(
        title = "Calentamiento",
        subtitle = "Antes de cada sesion, en este orden.",
        groups = listOf(
            PlanGroup(
                title = "Secuencia",
                items = listOf(
                    planItem(1, "eliptica", "Eliptica suave: 4 minutos.", 240, exerciseById),
                    planItem(2, "sentadilla-sin-peso", "10 repeticiones.", null, exerciseById),
                    planItem(3, "bisagra-cadera-sin-peso", "10 repeticiones.", null, exerciseById),
                    planItem(4, "flexiones-banco", "8 repeticiones.", null, exerciseById),
                    planItem(5, "bird-dog", "6 repeticiones por lado.", null, exerciseById),
                    planItem(6, "circulos-hombros", "Circulos de hombros y movilidad de cadera: 1 minuto.", 60, exerciseById)
                )
            )
        )
    )

    private fun coolDown() = PlanBlock(
        title = "Vuelta a la calma",
        subtitle = "Despues del entrenamiento principal.",
        groups = listOf(
            PlanGroup(
                title = "Secuencia",
                items = listOf(
                    PlanItem(1, "eliptica", "Eliptica muy suave", "3 minutos.", true, 180),
                    PlanItem(2, null, "Estiramiento suave", "Cuadriceps, gluteos, pectoral y espalda: 3-5 minutos.")
                )
            )
        )
    )

    private fun expandSession(
        day: ProgramDay,
        session: Session,
        spec: DaySpecifics,
        exerciseById: Map<String, Exercise>
    ): PlanBlock {
        return when (session.id) {
            "F1", "F2", "F3" -> strengthBlock(session, spec, exerciseById)
            "C" -> circuitBlock(session, spec, exerciseById)
            "ELIPTICA" -> cardioBlock(day)
            "CORE" -> coreBlock(day, exerciseById)
            "MOVILIDAD" -> mobilityBlock(day)
            else -> genericBlock(session, exerciseById)
        }
    }

    private fun strengthBlock(session: Session, spec: DaySpecifics, exerciseById: Map<String, Exercise>): PlanBlock {
        var order = 1
        return PlanBlock(
            title = readableSessionTitle(session.id),
            subtitle = spec.series?.let { "Realizar $it series" },
            labels = spec.labels,
            groups = session.blocks.map { block ->
                PlanGroup(
                    title = block.title,
                    instruction = restForGroup(block.title, spec),
                    items = block.items.map { prescription ->
                        prescription.toPlanItem(order++, exerciseById)
                    }
                )
            },
            note = spec.note
        )
    }

    private fun circuitBlock(session: Session, spec: DaySpecifics, exerciseById: Map<String, Exercise>): PlanBlock {
        var order = 1
        return PlanBlock(
            title = readableSessionTitle(session.id),
            subtitle = "Realizar ${spec.rounds ?: 1} vueltas",
            labels = spec.labels,
            groups = listOf(
                PlanGroup(
                    title = "Estaciones de cada vuelta",
                    instruction = "Descanso: ${spec.rest ?: "60-90 segundos despues de cada vuelta."}",
                    items = session.blocks.flatMap { it.items }.map { it.toPlanItem(order++, exerciseById) }
                )
            ),
            note = spec.note
        )
    }

    private fun cardioBlock(day: ProgramDay): PlanBlock {
        return PlanBlock(
            title = "Eliptica",
            labels = DaySpecifics.forDay(day).labels.filter { it.contains("RPE") || it.contains("min") || it.contains("intervalos") },
            groups = listOf(
                PlanGroup(
                    title = "Trabajo cardiovascular",
                    items = listOf(PlanItem(1, "eliptica", "Eliptica", cardioDetail(day), true, cardioSeconds(day)))
                )
            )
        )
    }

    private fun coreBlock(day: ProgramDay, exerciseById: Map<String, Exercise>): PlanBlock {
        val rounds = when (day.day) {
            2 -> "2 vueltas"
            27 -> "2 vueltas"
            else -> "3 vueltas"
        }
        return PlanBlock(
            title = "Musculatura central",
            subtitle = rounds,
            groups = listOf(
                PlanGroup(
                    title = "Ejercicios previstos",
                    items = listOf(
                        planItem(1, "dead-bug", "Segun el dia, con control.", null, exerciseById),
                        planItem(2, "plancha-lateral", "Segun el dia, por lado.", null, exerciseById),
                        planItem(3, "bird-dog", "Segun el dia, por lado.", null, exerciseById)
                    )
                )
            )
        )
    }

    private fun mobilityBlock(day: ProgramDay): PlanBlock {
        return PlanBlock(
            title = if (day.isRecovery) "Recuperacion y movilidad" else "Movilidad y estiramientos",
            labels = listOfNotNull(mobilityDuration(day)),
            groups = listOf(
                PlanGroup(
                    title = "Movilidad",
                    items = listOf(
                        PlanItem(1, "movilidad-cadera-espalda-hombros", "Movilidad de cadera, espalda y hombros", mobilityDetail(day), true)
                    )
                )
            )
        )
    }

    private fun genericBlock(session: Session, exerciseById: Map<String, Exercise>): PlanBlock {
        var order = 1
        return PlanBlock(
            title = session.title,
            subtitle = session.description,
            groups = session.blocks.map { block ->
                PlanGroup(block.title, block.instructions, block.items.map { it.toPlanItem(order++, exerciseById) })
            }
        )
    }

    private fun ExercisePrescription.toPlanItem(order: Int, exerciseById: Map<String, Exercise>): PlanItem {
        val exercise = exerciseById[exerciseId]
        return PlanItem(
            order = order,
            exerciseId = exerciseId,
            visibleName = exercise?.programName ?: exerciseId,
            detail = prescription,
            isTechniqueAvailable = exercise != null,
            timedSeconds = timedSeconds
        )
    }

    private fun planItem(order: Int, exerciseId: String, detail: String, timedSeconds: Int?, exerciseById: Map<String, Exercise>): PlanItem {
        val exercise = exerciseById[exerciseId]
        return PlanItem(order, exerciseId, exercise?.programName ?: exerciseId, detail, exercise != null, timedSeconds)
    }

    private fun restForGroup(groupTitle: String, spec: DaySpecifics): String? {
        if (groupTitle == "Final") return null
        return spec.rest?.let { "Descanso: $it" }
    }

    private fun readableSessionTitle(id: String): String = when (id) {
        "F1" -> "F1 · Fuerza general"
        "F2" -> "F2 · Pierna unilateral, gluteo y hombro"
        "F3" -> "F3 · Hipertrofia y definicion"
        "C" -> "C · Circuito metabolico"
        "CORE" -> "Musculatura central"
        "ELIPTICA" -> "Eliptica"
        "MOVILIDAD" -> "Movilidad"
        else -> id
    }

    private fun cardioDetail(day: ProgramDay): String {
        return when (day.day) {
            2 -> "35 minutos a RPE 5-6."
            4 -> "25 minutos a RPE 3-4."
            5 -> "Despues de F3: 5 intervalos de 30 segundos rapidos y 60 segundos suaves."
            6 -> "6 minutos suaves + 8 intervalos de 45 segundos fuertes y 75 segundos suaves + 5 minutos suaves."
            7 -> "Eliptica suave 30 minutos."
            9 -> "6 minutos suaves + 4 bloques de 4 minutos a RPE 7 y 2 minutos suaves + 5 minutos de vuelta a la calma."
            11 -> "30 minutos a RPE 4."
            13 -> "6 minutos suaves + 10 intervalos de 60 segundos fuertes y 60 segundos suaves + 5 minutos suaves."
            14 -> "Finalizar con 10 minutos suaves de eliptica."
            15 -> "Eliptica continua 45 minutos a RPE 5-6. Los ultimos 8 minutos a RPE 7."
            17 -> "6 minutos suaves + 12 intervalos de 60 segundos fuertes y 60 segundos suaves + 5 minutos suaves."
            19 -> "30-35 minutos a RPE 3-4."
            21 -> "Eliptica continua 50 minutos a RPE 5-6."
            23 -> "Dos piramides de 30-45-60-75-60-45-30 segundos intensos, recuperando el mismo tiempo suave."
            25 -> "30 minutos a RPE 4."
            26 -> "Despues de F2: 6 intervalos de 30 segundos rapidos y 45 segundos suaves."
            27 -> "45 minutos: 10 suaves + 20 a RPE 7 + 10 moderados + 5 suaves."
            29 -> "Eliptica muy suave 25 minutos."
            30 -> "Terminar con 20 minutos de eliptica moderada."
            else -> day.prescription
        }
    }

    private fun cardioSeconds(day: ProgramDay): Int? = when (day.day) {
        2 -> 35 * 60
        4 -> 25 * 60
        7 -> 30 * 60
        11 -> 30 * 60
        15 -> 45 * 60
        19 -> 30 * 60
        21 -> 50 * 60
        25 -> 30 * 60
        27 -> 45 * 60
        29 -> 25 * 60
        30 -> 20 * 60
        else -> null
    }

    private fun mobilityDetail(day: ProgramDay): String = when (day.day) {
        4, 7, 11 -> "Movilidad durante 10 minutos."
        19, 25 -> "Movilidad durante 12 minutos."
        21 -> "Movilidad de cadera y espalda al finalizar."
        29 -> "Movilidad y estiramientos durante 15 minutos. Acostarse temprano."
        else -> "Movilidad y estiramientos segun la prescripcion del dia."
    }

    private fun mobilityDuration(day: ProgramDay): String? = when (day.day) {
        4, 7, 11 -> "10 min"
        19, 25 -> "12 min"
        29 -> "15 min"
        else -> null
    }
}

private data class DaySpecifics(
    val series: Int? = null,
    val rounds: Int? = null,
    val rest: String? = null,
    val labels: List<String> = emptyList(),
    val note: String? = null
) {
    companion object {
        fun forDay(day: ProgramDay): DaySpecifics {
            return when (day.day) {
                1 -> DaySpecifics(series = 3, rest = "60-90 segundos despues de cada pareja.", labels = listOf("Series: 3", "RPE 6", "Medicion inicial"))
                3 -> DaySpecifics(series = 3, rest = "90 segundos.", labels = listOf("Series: 3", "RPE 6-7", "Descanso: 90 s"))
                5 -> DaySpecifics(series = 3, labels = listOf("Series: 3", "RPE 7", "5 intervalos"))
                8 -> DaySpecifics(series = 4, rest = "75 segundos.", labels = listOf("Series: 4", "RPE 7", "Descanso: 75 s"))
                10 -> DaySpecifics(series = 4, labels = listOf("Series: 4", "RPE 7-8"), note = "Plancha lateral de 30-40 segundos.")
                12 -> DaySpecifics(rounds = 4, rest = "90 segundos despues de cada vuelta.", labels = listOf("Vueltas: 4", "Descanso: 90 s", "RPE maximo 8"))
                14 -> DaySpecifics(series = 4, labels = listOf("Series: 4", "12-15 repeticiones cuando sea posible"))
                16 -> DaySpecifics(series = 4, rest = "60-75 segundos.", labels = listOf("Series: 4", "RPE 8", "Descanso: 60-75 s"), note = "Bajada de 3 segundos en sentadilla y press.")
                18 -> DaySpecifics(series = 4, labels = listOf("Series: 4", "RPE 8"), note = "Zancadas y hip thrust con pausa de un segundo.")
                20 -> DaySpecifics(rounds = 5, rest = "75 segundos despues de cada vuelta.", labels = listOf("Vueltas: 5", "Descanso: 75 s"), note = "Mantener tecnica perfecta.")
                22 -> DaySpecifics(series = 4, labels = listOf("Series: 4", "RPE 8"), note = "Sentadilla bulgara solo si la tecnica es estable.")
                24 -> DaySpecifics(series = 4, rest = "60 segundos.", labels = listOf("Series: 4", "RPE 8-9", "Descanso: 60 s"), note = "Sin llegar al fallo.")
                26 -> DaySpecifics(series = 4, labels = listOf("Series: 4", "6 intervalos"))
                28 -> DaySpecifics(rounds = 5, rest = "60 segundos despues de cada vuelta.", labels = listOf("Vueltas: 5", "Descanso: 60 s", "RPE maximo 9"))
                30 -> DaySpecifics(series = 3, rest = "60-90 segundos despues de cada pareja.", labels = listOf("Series: 3", "Comparacion final"), note = "Repetir exactamente la sesion F1 del dia 1.")
                else -> DaySpecifics(labels = extractLabels(day.prescription))
            }
        }

        private fun extractLabels(text: String): List<String> {
            val labels = mutableListOf<String>()
            Regex("RPE ?[0-9]-?[0-9]?").find(text)?.value?.let { labels += it }
            Regex("[0-9]+ ?min").find(text)?.value?.let { labels += it }
            if (text.contains("intervalos", ignoreCase = true)) labels += "Intervalos"
            return labels
        }
    }
}

