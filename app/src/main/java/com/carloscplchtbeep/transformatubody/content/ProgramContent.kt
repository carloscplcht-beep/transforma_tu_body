package com.carloscplchtbeep.transformatubody.content

import com.carloscplchtbeep.transformatubody.domain.*

object TransformaProgram {
    val content: ProgramContent = ProgramContent(
        phases = listOf(
            Phase("p1", "Dias 1-7: adaptacion exigente", 1..7, "Entrada progresiva con fuerza, eliptica, movilidad y control tecnico."),
            Phase("p2", "Dias 8-14: aumento del volumen", 8..14, "Sube el numero de series, intervalos y densidad sin superar la tecnica."),
            Phase("p3", "Dias 15-21: fase de maxima carga", 15..21, "Semana de mayor carga combinando fuerza, circuito y cardio continuo."),
            Phase("p4", "Dias 22-30: definicion y aumento de densidad", 22..30, "Cierre con descansos mas cortos, comparacion final y medicion.")
        ),
        days = programDays,
        sessions = sessions,
        exercises = exercises,
        nutrition = nutrition,
        guide = guide,
        measurementDays = listOf(1, 8, 15, 22, 30)
    )

    fun validate(): ContentValidationResult {
        val errors = mutableListOf<String>()
        val days = content.days.map { it.day }
        if (content.days.size != 30) errors += "El programa debe tener exactamente 30 dias."
        val missing = (1..30).filterNot { it in days }
        if (missing.isNotEmpty()) errors += "Faltan dias: $missing."
        val duplicated = days.groupBy { it }.filterValues { it.size > 1 }.keys
        if (duplicated.isNotEmpty()) errors += "Hay dias duplicados: $duplicated."
        listOf("F1", "F2", "F3", "C").forEach { id ->
            if (content.sessions.none { it.id == id }) errors += "Falta la sesion $id."
        }
        val exerciseIds = content.exercises.map { it.id }.toSet()
        content.sessions.flatMap { it.blocks }.flatMap { it.items }.forEach { item ->
            if (item.exerciseId !in exerciseIds) errors += "Ejercicio inexistente referenciado: ${item.exerciseId}."
        }
        if (content.nutrition.menus.size != 4) errors += "Deben existir cuatro menus completos."
        if (content.measurementDays != listOf(1, 8, 15, 22, 30)) errors += "Los controles deben ser dias 1, 8, 15, 22 y 30."
        if (content.guide.safety.size < 2) errors += "Faltan advertencias de seguridad."
        if (content.nutrition.adjustments.size < 2) errors += "Faltan reglas de ajuste."
        return ContentValidationResult(errors.isEmpty(), errors)
    }
}

private val sessions = listOf(
    Session(
        id = "F1",
        title = "F1. Fuerza general",
        description = "Realizar los ejercicios por parejas.",
        defaultRest = "60-90 segundos despues de cada pareja.",
        blocks = listOf(
            TrainingBlock("Pareja A", "Completa los dos ejercicios y descansa segun la prescripcion.", listOf(
                ExercisePrescription("sentadilla-goblet", "10-15 repeticiones"),
                ExercisePrescription("press-pecho-banco", "8-12 repeticiones")
            )),
            TrainingBlock("Pareja B", "Completa los dos ejercicios y descansa segun la prescripcion.", listOf(
                ExercisePrescription("remo-banco", "10-12 repeticiones por lado", "por lado"),
                ExercisePrescription("peso-muerto-rumano", "10-15 repeticiones")
            )),
            TrainingBlock("Final", "Termina con control postural y agarre.", listOf(
                ExercisePrescription("plancha-frontal", "30-45 segundos", timedSeconds = 30),
                ExercisePrescription("marcha-granjero", "40-60 segundos", timedSeconds = 40)
            ))
        )
    ),
    Session(
        id = "F2",
        title = "F2. Pierna unilateral, gluteo y hombro",
        description = "Trabajo unilateral, empuje, traccion y musculatura central.",
        blocks = listOf(TrainingBlock("Bloque principal", "Respeta el rango prescrito y la tecnica.", listOf(
            ExercisePrescription("zancada-atras", "8-12 repeticiones por pierna", "por pierna"),
            ExercisePrescription("hip-thrust", "12-20 repeticiones"),
            ExercisePrescription("flexiones-banco", "8-15 repeticiones"),
            ExercisePrescription("remo-unilateral", "10-15 repeticiones por lado", "por lado"),
            ExercisePrescription("pike-push-up", "6-12 repeticiones"),
            ExercisePrescription("dead-bug", "10 repeticiones por lado", "por lado"),
            ExercisePrescription("plancha-lateral", "20-40 segundos por lado", "por lado", 20)
        )))
    ),
    Session(
        id = "F3",
        title = "F3. Hipertrofia y definicion",
        description = "Ritmo controlado, pausas y accesorios de brazos.",
        blocks = listOf(TrainingBlock("Bloque principal", "En tercera y cuarta semana la sentadilla dividida puede ser bulgara si no hay molestia ni perdida de equilibrio.", listOf(
            ExercisePrescription("sentadilla-dividida", "10-12 repeticiones por pierna", "por pierna"),
            ExercisePrescription("press-pecho-3s", "8-12 repeticiones, bajando durante 3 segundos"),
            ExercisePrescription("remo-pausa", "10-12 repeticiones por lado, pausa de 1 segundo arriba", "por lado"),
            ExercisePrescription("peso-muerto-escalonado", "10-12 repeticiones por lado", "por lado"),
            ExercisePrescription("curl-martillo", "8-12 repeticiones"),
            ExercisePrescription("extension-triceps", "10-15 repeticiones"),
            ExercisePrescription("bird-dog-controlado", "10 repeticiones por lado", "por lado")
        )))
    ),
    Session(
        id = "C",
        title = "C. Circuito metabolico",
        description = "Una vuelta contiene todos los ejercicios listados. Descansar entre 60 y 90 segundos despues de cada vuelta.",
        blocks = listOf(TrainingBlock("Vuelta de circuito", "Mantener tecnica y no superar la intensidad indicada del dia.", listOf(
            ExercisePrescription("sentadilla-goblet", "12 repeticiones"),
            ExercisePrescription("peso-muerto-rumano", "12 repeticiones"),
            ExercisePrescription("flexiones-banco", "10 repeticiones"),
            ExercisePrescription("remo", "10 repeticiones por lado", "por lado"),
            ExercisePrescription("zancada-atras", "8 repeticiones por pierna", "por pierna"),
            ExercisePrescription("mountain-climber", "30 segundos", timedSeconds = 30),
            ExercisePrescription("eliptica-intensa", "2 minutos", timedSeconds = 120)
        )))
    ),
    Session(
        id = "CORE",
        title = "Core: musculatura central",
        description = "Musculatura central del abdomen, espalda y pelvis.",
        blocks = listOf(TrainingBlock("Core", "Vueltas de control, sin prisa.", listOf(
            ExercisePrescription("dead-bug", "Segun dia"),
            ExercisePrescription("plancha-lateral", "Segun dia", timedSeconds = 20),
            ExercisePrescription("bird-dog", "Segun dia")
        )))
    ),
    Session(
        id = "ELIPTICA",
        title = "Eliptica",
        description = "Trabajo cardiovascular en bicicleta eliptica segun duracion e intensidad del dia.",
        blocks = listOf(TrainingBlock("Eliptica", "Sigue exactamente la duracion, intervalos e intensidad del dia.", listOf(
            ExercisePrescription("eliptica", "Segun prescripcion del dia")
        )))
    ),
    Session(
        id = "MOVILIDAD",
        title = "Movilidad y estiramientos",
        description = "Movilidad de cadera, espalda y hombros; estiramientos suaves cuando corresponda.",
        blocks = listOf(TrainingBlock("Movilidad", "Sin dolor y con respiracion tranquila.", listOf(
            ExercisePrescription("movilidad-cadera-espalda-hombros", "Segun dia")
        )))
    )
)

private val programDays = listOf(
    ProgramDay(1, "p1", "Medicion y F1", listOf("F1"), "Medir peso y cintura. F1: 3 series, RPE 6. Anotar repeticiones y tiempo total."),
    ProgramDay(2, "p1", "Eliptica y core", listOf("ELIPTICA", "CORE"), "Eliptica: 35 min a RPE 5-6. Despues, 2 vueltas de dead bug, plancha lateral y bird-dog."),
    ProgramDay(3, "p1", "F2", listOf("F2"), "F2: 3 series, RPE 6-7. Descanso de 90 s."),
    ProgramDay(4, "p1", "Recuperacion", listOf("ELIPTICA", "MOVILIDAD"), "Recuperacion: eliptica 25 min a RPE 3-4. Movilidad de cadera, espalda y hombros durante 10 min.", isRecovery = true),
    ProgramDay(5, "p1", "F3 e intervalos", listOf("F3", "ELIPTICA"), "F3: 3 series, RPE 7. Despues, 5 intervalos de 30 s rapidos y 60 s suaves en la eliptica."),
    ProgramDay(6, "p1", "Intervalos y core", listOf("ELIPTICA", "CORE"), "Eliptica: 6 min suaves + 8 intervalos de 45 s fuertes y 75 s suaves + 5 min suaves. Core: 3 vueltas."),
    ProgramDay(7, "p1", "Suave y movilidad", listOf("ELIPTICA", "MOVILIDAD"), "Eliptica suave 30 min. Estiramientos y movilidad durante 10 min.", isRecovery = true),
    ProgramDay(8, "p2", "F1 volumen", listOf("F1"), "F1: 4 series, RPE 7. Descanso de 75 s."),
    ProgramDay(9, "p2", "Bloques en eliptica", listOf("ELIPTICA"), "Eliptica: 6 min suaves + 4 bloques de 4 min a RPE 7 y 2 min suaves + 5 min de vuelta a la calma."),
    ProgramDay(10, "p2", "F2 volumen", listOf("F2"), "F2: 4 series, RPE 7-8. Plancha lateral de 30-40 s."),
    ProgramDay(11, "p2", "Recuperacion", listOf("ELIPTICA", "MOVILIDAD"), "Recuperacion: eliptica 30 min a RPE 4. Movilidad 10 min.", isRecovery = true),
    ProgramDay(12, "p2", "Circuito C", listOf("C"), "Circuito C: 4 vueltas. Descanso de 90 s. No superar RPE 8."),
    ProgramDay(13, "p2", "Intervalos largos", listOf("ELIPTICA", "CORE"), "Eliptica: 6 min suaves + 10 intervalos de 60 s fuertes y 60 s suaves + 5 min suaves. Core: 3 vueltas."),
    ProgramDay(14, "p2", "F3 volumen", listOf("F3", "ELIPTICA"), "F3: 4 series. Usar 12-15 repeticiones cuando sea posible. Finalizar con 10 min suaves de eliptica."),
    ProgramDay(15, "p3", "Eliptica continua", listOf("ELIPTICA"), "Eliptica continua 45 min a RPE 5-6. Los ultimos 8 min a RPE 7."),
    ProgramDay(16, "p3", "F1 maxima carga", listOf("F1"), "F1: 4 series, RPE 8. Descansos de 60-75 s. Bajada de 3 s en sentadilla y press."),
    ProgramDay(17, "p3", "Intervalos y core", listOf("ELIPTICA", "CORE"), "Eliptica: 6 min suaves + 12 intervalos de 60 s fuertes y 60 s suaves + 5 min suaves. Core: 3 vueltas."),
    ProgramDay(18, "p3", "F2 pausa", listOf("F2"), "F2: 4 series, RPE 8. Zancadas y hip thrust con pausa de un segundo."),
    ProgramDay(19, "p3", "Recuperacion", listOf("ELIPTICA", "MOVILIDAD"), "Recuperacion: eliptica 30-35 min a RPE 3-4. Movilidad 12 min.", isRecovery = true),
    ProgramDay(20, "p3", "Circuito C denso", listOf("C"), "Circuito C: 5 vueltas. Descanso de 75 s. Mantener tecnica perfecta."),
    ProgramDay(21, "p3", "Eliptica 50", listOf("ELIPTICA", "MOVILIDAD"), "Eliptica continua 50 min a RPE 5-6. Movilidad de cadera y espalda al finalizar."),
    ProgramDay(22, "p4", "F3 densidad", listOf("F3"), "F3: 4 series, RPE 8. Sentadilla bulgara solo si la tecnica es estable."),
    ProgramDay(23, "p4", "Piramides", listOf("ELIPTICA"), "Eliptica: dos piramides de 30-45-60-75-60-45-30 s intensos, recuperando el mismo tiempo suave."),
    ProgramDay(24, "p4", "F1 intensa", listOf("F1"), "F1: 4 series, RPE 8-9. Descansos de 60 s. Sin llegar al fallo."),
    ProgramDay(25, "p4", "Recuperacion", listOf("ELIPTICA", "MOVILIDAD"), "Recuperacion: eliptica 30 min a RPE 4 + 12 min de movilidad.", isRecovery = true),
    ProgramDay(26, "p4", "F2 e intervalos", listOf("F2", "ELIPTICA"), "F2: 4 series. Despues, 6 intervalos de 30 s rapidos y 45 s suaves."),
    ProgramDay(27, "p4", "Eliptica variable", listOf("ELIPTICA", "CORE"), "Eliptica 45 min: 10 min suaves + 20 min a RPE 7 + 10 min moderados + 5 min suaves. Core: 2 vueltas."),
    ProgramDay(28, "p4", "Circuito C maximo", listOf("C"), "Circuito C: 5 vueltas. Descanso de 60 s. RPE maximo 9."),
    ProgramDay(29, "p4", "Descarga", listOf("ELIPTICA", "MOVILIDAD"), "Eliptica muy suave 25 min. Movilidad y estiramientos 15 min. Acostarse temprano.", isRecovery = true),
    ProgramDay(30, "p4", "Medicion y repeticion F1", listOf("F1", "ELIPTICA"), "Medicion matinal. Repetir exactamente la sesion F1 del dia 1: 3 series. Comparar repeticiones, tiempo y percepcion de esfuerzo. Terminar con 20 min de eliptica moderada.")
)

private fun exercise(
    id: String,
    name: String,
    clear: String,
    material: List<String>,
    groups: List<String>,
    appears: List<String>,
    adaptation: String = "Usa menor rango, menor numero de repeticiones o peso corporal cuando el documento lo contemple."
) = Exercise(
    id = id,
    programName = name,
    clearName = clear,
    material = material,
    bodyGroups = groups,
    startPosition = "Colocate estable, con abdomen activo y respiracion tranquila antes de iniciar.",
    steps = listOf(
        "Prepara la postura y confirma que no hay dolor.",
        "Ejecuta el movimiento de forma controlada dentro del rango prescrito.",
        "Mantente lejos del fallo muscular y conserva la tecnica.",
        "Termina la repeticion o el tiempo sin rebotes ni prisas."
    ),
    breathing = "Inspira en la fase facil o de bajada y expulsa el aire en la fase de esfuerzo.",
    commonErrors = listOf("Perder la alineacion", "Acelerar por encima de la tecnica", "Contener la respiracion", "Forzar si aparece dolor"),
    adaptation = adaptation,
    appearsIn = appears
)

private val exercises = listOf(
    exercise("sentadilla-goblet", "Sentadilla goblet", "Sentadilla sujetando una mancuerna delante del pecho", listOf("Mancuerna de 10 kg"), listOf("Pierna", "Gluteo"), listOf("F1", "C")),
    exercise("press-pecho-banco", "Press de pecho con las dos mancuernas sobre el banco", "Empuje de pecho tumbado en banco", listOf("Banco", "Dos mancuernas de 10 kg"), listOf("Pecho", "Hombro", "Triceps"), listOf("F1")),
    exercise("press-pecho-3s", "Press de pecho bajando durante 3 segundos", "Press de pecho con bajada lenta", listOf("Banco", "Dos mancuernas de 10 kg"), listOf("Pecho", "Hombro", "Triceps"), listOf("F3")),
    exercise("remo-banco", "Remo con una mancuerna apoyado en el banco", "Tirón de espalda con apoyo", listOf("Banco", "Mancuerna de 10 kg"), listOf("Espalda", "Brazo"), listOf("F1")),
    exercise("remo-unilateral", "Remo unilateral", "Remo con un brazo", listOf("Mancuerna de 10 kg", "Banco opcional"), listOf("Espalda", "Brazo"), listOf("F2")),
    exercise("remo-pausa", "Remo unilateral con pausa de un segundo arriba", "Remo con pausa al final", listOf("Mancuerna de 10 kg"), listOf("Espalda", "Brazo"), listOf("F3")),
    exercise("remo", "Remo", "Remo con mancuerna", listOf("Mancuerna de 10 kg"), listOf("Espalda", "Brazo"), listOf("C")),
    exercise("peso-muerto-rumano", "Peso muerto rumano con dos mancuernas", "Bisagra de cadera con mancuernas", listOf("Dos mancuernas de 10 kg"), listOf("Isquios", "Gluteo", "Espalda"), listOf("F1", "C")),
    exercise("peso-muerto-escalonado", "Peso muerto rumano en posicion escalonada", "Bisagra de cadera con una pierna retrasada", listOf("Mancuernas de 10 kg"), listOf("Isquios", "Gluteo"), listOf("F3")),
    exercise("plancha-frontal", "Plancha frontal", "Plancha abdominal boca abajo", listOf("Esterilla"), listOf("Core"), listOf("F1")),
    exercise("marcha-granjero", "Marcha del granjero, de pie y con las mancuernas", "Caminar o mantenerse erguido con peso en las manos", listOf("Dos mancuernas de 10 kg"), listOf("Agarre", "Core", "Pierna"), listOf("F1")),
    exercise("zancada-atras", "Zancada hacia atras", "Paso atras y flexion de ambas piernas", listOf("Mancuernas opcionales"), listOf("Pierna", "Gluteo"), listOf("F2", "C")),
    exercise("hip-thrust", "Hip thrust con espalda apoyada en el banco", "Elevacion de cadera con la espalda en banco", listOf("Banco", "Mancuerna opcional"), listOf("Gluteo"), listOf("F2")),
    exercise("flexiones-banco", "Flexiones con manos sobre el banco", "Flexiones inclinadas", listOf("Banco"), listOf("Pecho", "Hombro", "Triceps"), listOf("F2", "C")),
    exercise("pike-push-up", "Pike push-up con manos apoyadas en el banco", "Flexion en pica para hombros", listOf("Banco"), listOf("Hombro", "Triceps"), listOf("F2")),
    exercise("dead-bug", "Dead bug", "Ejercicio de abdomen tumbado alternando brazo y pierna", listOf("Esterilla"), listOf("Core"), listOf("F2", "CORE")),
    exercise("plancha-lateral", "Plancha lateral", "Plancha apoyado sobre un lado", listOf("Esterilla"), listOf("Core"), listOf("F2", "CORE")),
    exercise("sentadilla-dividida", "Sentadilla dividida", "Sentadilla con una pierna adelantada", listOf("Mancuernas opcionales"), listOf("Pierna", "Gluteo"), listOf("F3")),
    exercise("curl-martillo", "Curl martillo", "Flexion de codo con agarre neutro", listOf("Mancuernas de 10 kg"), listOf("Biceps", "Antebrazo"), listOf("F3")),
    exercise("extension-triceps", "Extension de triceps tumbado con una mancuerna", "Extension de codo tumbado", listOf("Banco", "Mancuerna de 10 kg"), listOf("Triceps"), listOf("F3")),
    exercise("bird-dog", "Bird-dog", "Extender brazo y pierna contraria a cuatro apoyos", listOf("Esterilla"), listOf("Core", "Espalda"), listOf("Calentamiento", "CORE")),
    exercise("bird-dog-controlado", "Bird-dog controlado", "Bird-dog realizado de forma lenta", listOf("Esterilla"), listOf("Core", "Espalda"), listOf("F3")),
    exercise("mountain-climber", "Mountain climber con manos en el banco", "Llevar rodillas hacia delante con manos apoyadas", listOf("Banco"), listOf("Core", "Cardio"), listOf("C")),
    exercise("eliptica", "Eliptica", "Bicicleta eliptica", listOf("Bicicleta eliptica"), listOf("Cardio"), listOf("Dias de cardio")),
    exercise("eliptica-intensa", "Eliptica intensa", "Tramo intenso en bicicleta eliptica", listOf("Bicicleta eliptica"), listOf("Cardio"), listOf("C")),
    exercise("movilidad-cadera-espalda-hombros", "Movilidad de cadera, espalda y hombros", "Movilidad articular suave", listOf("Esterilla opcional"), listOf("Movilidad"), listOf("Recuperacion")),
    exercise("sentadilla-sin-peso", "Sentadilla sin peso", "Sentadilla con peso corporal", listOf("Peso corporal"), listOf("Pierna"), listOf("Calentamiento")),
    exercise("bisagra-cadera-sin-peso", "Bisagra de cadera sin peso", "Inclinar cadera atras sin cargar peso", listOf("Peso corporal"), listOf("Cadera", "Espalda"), listOf("Calentamiento")),
    exercise("circulos-hombros", "Circulos de hombros", "Movilidad circular de hombros", listOf("Peso corporal"), listOf("Hombros"), listOf("Calentamiento"))
)

private val nutrition = NutritionContent(
    proteinGoal = listOf(
        "Para 76 kg: objetivo de 115-125 gramos diarios de proteina.",
        "Repartirla en 3 comidas principales y, cuando sea necesario, una colacion.",
        "Intentar alcanzar aproximadamente 30-40 g en cada comida principal."
    ),
    dailyStructure = listOf(
        "Desayuno: fuente importante de proteina, fruta, avena o pan integral en cantidad moderada; sin zumos, azucar ni bolleria.",
        "Comida y cena: medio plato verduras, un cuarto proteina, un cuarto patata, arroz, pasta integral, legumbre o pan integral.",
        "Aceite de oliva medido, no vertido libremente."
    ),
    carbDistribution = listOf(
        "Dos raciones diarias los dias de fuerza o intervalos.",
        "Una o dos raciones pequenas en recuperacion, segun hambre.",
        "Colocar preferentemente una de las raciones antes o despues de entrenar."
    ),
    fats = listOf(
        "Aceite de oliva: aproximadamente una cucharada en comida y otra en cena.",
        "Frutos secos: 15-25 g, no comer directamente de la bolsa.",
        "Aguacate: aproximadamente medio pequeno."
    ),
    menus = listOf(
        Menu("m1", "Menu 1", listOf(
            Meal("Desayuno", listOf("250 g de skyr o yogur griego natural alto en proteina.", "40 g de avena.", "Fresas, arandanos o una manzana.", "Cafe sin azucar.")),
            Meal("Comida", listOf("180 g de pechuga de pollo.", "Ensalada grande.", "150 g de arroz cocido.", "Una cucharada de aceite de oliva.")),
            Meal("Colacion", listOf("Queso fresco batido o yogur natural.", "Una pieza de fruta.")),
            Meal("Cena", listOf("200 g de merluza.", "Verduras al horno o salteadas.", "200-250 g de patata cocida o asada."))
        )),
        Menu("m2", "Menu 2", listOf(
            Meal("Desayuno", listOf("Tres huevos.", "Una rebanada de pan integral con tomate.", "Una fruta.", "Yogur natural.")),
            Meal("Comida", listOf("Ensalada de lentejas: 250 g de lentejas cocidas, verduras y una lata grande de atun al natural.", "Aceite de oliva medido.")),
            Meal("Colacion", listOf("200 g de queso fresco batido.", "Canela o fruta.")),
            Meal("Cena", listOf("180-200 g de pavo o pollo.", "Pisto, calabacin, berenjena o verduras.", "Una pequena porcion de pan integral."))
        )),
        Menu("m3", "Menu 3", listOf(
            Meal("Desayuno", listOf("250 g de yogur natural alto en proteina.", "Avena.", "Platano pequeno.", "15 g de nueces.")),
            Meal("Comida", listOf("180-200 g de salmon.", "Verdura abundante.", "Patata cocida o asada.")),
            Meal("Colacion", listOf("Dos huevos cocidos y una fruta.")),
            Meal("Cena", listOf("Tortilla de tres huevos.", "120-150 g de gambas, langostinos o atun.", "Ensalada grande."))
        )),
        Menu("m4", "Menu 4", listOf(
            Meal("Desayuno", listOf("Dos huevos.", "200 g de queso fresco batido.", "Fruta.", "Una rebanada de pan integral.")),
            Meal("Comida", listOf("180 g de ternera magra.", "Verduras.", "150 g de arroz cocido o una patata mediana.")),
            Meal("Colacion", listOf("Yogur natural.", "15-20 g de almendras.")),
            Meal("Cena", listOf("200 g de pescado blanco.", "Crema de verduras sin nata.", "Ensalada o verduras salteadas."))
        ))
    ),
    priorityFoods = listOf("Pescado blanco y azul.", "Pollo, pavo y carnes magras.", "Huevos.", "Yogur natural, skyr, queso fresco y queso fresco batido.", "Lentejas, garbanzos y alubias.", "Verduras de todo tipo.", "Fruta entera.", "Patata cocida o asada.", "Arroz, avena y cereales integrales.", "Aceite de oliva y frutos secos en cantidades medidas.", "Agua, cafe y te sin azucar."),
    eliminatedFoods = listOf("Alcohol, incluida cerveza y vino.", "Refrescos azucarados y bebidas energeticas.", "Zumos, aunque sean naturales.", "Bolleria, galletas, cereales azucarados y barritas.", "Patatas fritas y aperitivos salados.", "Comida rapida y frituras.", "Embutidos grasos y carnes procesadas.", "Salsas comerciales, mayonesa y alinos muy caloricos.", "Helados, chocolates, postres y golosinas.", "Picoteo entre comidas.", "Comidas libres que terminan convirtiendose en un dia libre completo."),
    celebrationAlternative = "En una celebracion, la alternativa seria un unico plato flexible, sin alcohol, sin repetir y sin encadenar aperitivo, postre y picoteo posterior.",
    adjustments = listOf(
        "Si al terminar el dia 10 el peso medio no ha bajado al menos 300 g y la cintura tampoco ha disminuido, eliminar diariamente una racion de pan, arroz o pasta, una cucharada de aceite o los frutos secos de la colacion.",
        "No eliminar proteina ni verduras.",
        "Si se pierde mas de 1 kg semanal y aparece cansancio, anadir una pequena racion de patata, arroz, avena o fruta alrededor del entrenamiento."
    )
)

private val guide = StaticGuide(
    objective = listOf(
        "Resultado exigente pero razonable en un mes: perder aproximadamente 2-4 kg.",
        "Reducir claramente el perimetro abdominal.",
        "Mantener, y posiblemente aumentar ligeramente, la masa muscular.",
        "Mejorar la definicion de pecho, hombros, brazos y piernas.",
        "Aumentar notablemente la capacidad cardiovascular.",
        "Los resultados dependen del cumplimiento y de las circunstancias individuales."
    ),
    rules = listOf("No se entrenara al fallo muscular.", "No anadir entrenamientos improvisados por encima del programa.", "Completar fuerza, cardio, recuperaciones y alimentacion segun el documento."),
    effortScale = listOf("Escala de esfuerzo percibido (RPE) 5-6: moderado; se puede hablar.", "RPE 7: exigente, quedan unas 3 repeticiones posibles.", "RPE 8: quedan aproximadamente 2 repeticiones.", "RPE 9: quedaria una repeticion.", "No se entrenara al fallo muscular."),
    warmUp = listOf("Eliptica suave: 4 minutos.", "Sentadilla sin peso: 10 repeticiones.", "Bisagra de cadera sin peso: 10.", "Flexiones apoyado en el banco: 8.", "Bird-dog: 6 por lado.", "Circulos de hombros y movilidad de cadera: 1 minuto."),
    coolDown = listOf("Eliptica muy suave: 3 minutos.", "Estiramiento suave de cuadriceps, gluteos, pectoral y espalda: 3-5 minutos."),
    dumbbellProgression = listOf("Aumentar repeticiones hasta 15-20.", "Bajar durante 3 segundos.", "Hacer una pausa de un segundo abajo.", "Emplear ejercicios unilaterales.", "Reducir progresivamente los descansos.", "Cuando 10 kg sean excesivos para un ejercicio unilateral, usar una mancuerna con ambas manos, menos repeticiones o variante con peso corporal."),
    resultControl = listOf("Dias 1, 8, 15, 22 y 30.", "Medir en ayunas, despues de ir al bano: peso, cintura a la altura del ombligo, fotografia frontal, fotografia lateral y numero de agujero del cinturon.", "El peso debe valorarse mediante la media de tres mananas."),
    safety = listOf(
        "No iniciar con intensidad alta sin valoracion profesional si existen antecedentes cardiovasculares, enfermedad renal o metabolica, hipertension no controlada, dolor toracico, falta de aire desproporcionada, mareos o palpitaciones.",
        "Interrumpir inmediatamente el ejercicio ante dolor u opresion toracica, mareo, desmayo, palpitaciones anormales o dificultad respiratoria intensa.",
        "Esta app no sustituye la valoracion profesional."
    ),
    finalFactors = "La transformacion dependera principalmente de cumplir la alimentacion, completar las sesiones de fuerza, respetar las recuperaciones y dormir suficientemente."
)

