@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carloscplchtbeep.transformatubody

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carloscplchtbeep.transformatubody.content.TransformaProgram
import com.carloscplchtbeep.transformatubody.data.MeasurementEntity
import com.carloscplchtbeep.transformatubody.data.SessionProgressEntity
import com.carloscplchtbeep.transformatubody.data.ThemeChoice
import com.carloscplchtbeep.transformatubody.data.UserPreferences
import com.carloscplchtbeep.transformatubody.domain.*
import com.carloscplchtbeep.transformatubody.notifications.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val requestNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = application as TransformaTuBodyApp
            val vm: AppViewModel = viewModel(factory = AppViewModel.factory(app))
            val state by vm.state.collectAsState()
            TransformaTheme(state.preferences.themeChoice) {
                if (!state.preferences.onboardingDone) {
                    OnboardingScreen(
                        onFinish = { theme, reminders ->
                            if (reminders && Build.VERSION.SDK_INT >= 33) {
                                val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_GRANTED
                                if (!granted) requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            vm.completeOnboarding(theme, reminders)
                        }
                    )
                } else {
                    AppScaffold(state, vm)
                }
            }
        }
    }
}

data class AppUiState(
    val preferences: UserPreferences = UserPreferences(),
    val sessions: List<SessionProgressEntity> = emptyList(),
    val measurements: List<MeasurementEntity> = emptyList(),
    val currentDay: Int = 1
)

class AppViewModel(private val app: TransformaTuBodyApp) : AndroidViewModel(app) {
    private val container = app.container
    val state: StateFlow<AppUiState> = combine(
        container.preferences.preferences,
        container.progress.sessions,
        container.progress.measurements
    ) { prefs, sessions, measurements ->
        AppUiState(
            preferences = prefs,
            sessions = sessions,
            measurements = measurements,
            currentDay = ProgramCalculations.currentProgramDay(prefs.startEpochDay, LocalDate.now().toEpochDay())
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    fun completeOnboarding(theme: ThemeChoice, reminders: Boolean) = viewModelScope.launch {
        container.preferences.completeOnboarding(LocalDate.now().toEpochDay(), theme, reminders)
        if (reminders) ReminderScheduler(app).scheduleDaily(8, 0)
    }

    fun markComplete(day: Int, effort: Int? = null, duration: Int? = null, note: String? = null) = viewModelScope.launch {
        container.progress.markDayComplete(day, effort, duration, note)
    }

    fun saveSessionStep(day: Int, step: Int) = viewModelScope.launch {
        container.progress.saveSessionStep(day, step)
    }

    fun saveMeasurement(entity: MeasurementEntity) = viewModelScope.launch {
        if (ProgramCalculations.isValidMeasurement(entity.weight1Kg, entity.waistCm)) container.progress.saveMeasurement(entity)
    }

    fun deleteMeasurement(day: Int) = viewModelScope.launch { container.progress.deleteMeasurement(day) }

    fun resetAll() = viewModelScope.launch {
        container.progress.clearAll()
        container.preferences.resetProgram()
        ReminderScheduler(app).cancel()
    }

    companion object {
        fun factory(app: TransformaTuBodyApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(app) as T
        }
    }
}

@Composable
fun TransformaTheme(themeChoice: ThemeChoice, content: @Composable () -> Unit) {
    val dark = when (themeChoice) {
        ThemeChoice.DARK -> true
        ThemeChoice.LIGHT -> false
        ThemeChoice.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val colors = if (dark) {
        darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFFE17855), secondary = androidx.compose.ui.graphics.Color(0xFF9CC8BA), background = androidx.compose.ui.graphics.Color(0xFF101415), surface = androidx.compose.ui.graphics.Color(0xFF182021))
    } else {
        lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF9E3E28), secondary = androidx.compose.ui.graphics.Color(0xFF426B61), background = androidx.compose.ui.graphics.Color(0xFFF8F5EF), surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF))
    }
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}

@Composable
fun OnboardingScreen(onFinish: (ThemeChoice, Boolean) -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    var theme by remember { mutableStateOf(ThemeChoice.SYSTEM) }
    var reminders by remember { mutableStateOf(false) }
    val pages = listOf(
        "Transforma tu Body" to "Programa de 30 dias con esterilla, bicicleta eliptica, dos mancuernas de 10 kg y banco plano.",
        "Objetivo realista" to "Objetivo exigente pero razonable. Los resultados dependen del cumplimiento y de las circunstancias individuales.",
        "Seguridad" to "Lee las advertencias: detente ante dolor toracico, mareo, desmayo, palpitaciones anormales o dificultad respiratoria intensa.",
        "Configuracion" to "Elige tema y recordatorios locales. No se pide nombre, correo, peso inicial ni registro."
    )
    Scaffold { padding ->
        Column(Modifier.padding(padding).padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Transforma tu Body", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { (page + 1) / 4f }, modifier = Modifier.fillMaxWidth())
                Text(pages[page].first, style = MaterialTheme.typography.headlineSmall)
                Text(pages[page].second, style = MaterialTheme.typography.bodyLarge)
                if (page == 3) {
                    Text("Tema", fontWeight = FontWeight.Bold)
                    ThemeChoice.entries.forEach { choice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = theme == choice, onClick = { theme = choice })
                            Text(choice.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = reminders, onCheckedChange = { reminders = it })
                        Spacer(Modifier.width(12.dp))
                        Text("Activar recordatorios locales")
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { page = (page - 1).coerceAtLeast(0) }, enabled = page > 0, modifier = Modifier.weight(1f)) { Text("Atras") }
                Button(onClick = { if (page == 3) onFinish(theme, reminders) else page++ }, modifier = Modifier.weight(1f)) { Text(if (page == 3) "Aceptar y empezar" else "Siguiente") }
            }
        }
    }
}

@Composable
fun AppScaffold(state: AppUiState, vm: AppViewModel) {
    val nav = rememberNavController()
    val tabs = listOf("hoy" to "Hoy", "programa" to "Programa", "nutricion" to "Nutricion", "progreso" to "Progreso", "mas" to "Mas")
    Scaffold(
        bottomBar = {
            NavigationBar {
                val current by nav.currentBackStackEntryAsState()
                val route = current?.destination?.route
                tabs.forEach { (target, label) ->
                    NavigationBarItem(
                        selected = route == target,
                        onClick = { nav.navigate(target) { popUpTo("hoy"); launchSingleTop = true } },
                        icon = { Text(label.take(1)) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = "hoy", modifier = Modifier.padding(padding)) {
            composable("hoy") { TodayScreen(state, vm, { nav.navigate("session/${it}") }, { nav.navigate("exercise/$it") }) }
            composable("programa") { ProgramScreen(state, { nav.navigate("day/$it") }) }
            composable("nutricion") { NutritionScreen() }
            composable("progreso") { ProgressScreen(state, vm) }
            composable("mas") { MoreScreen(vm) }
            composable("day/{day}", arguments = listOf(navArgument("day") { type = NavType.IntType })) {
                val day = it.arguments?.getInt("day") ?: state.currentDay
                DayDetailScreen(day, state, vm, { nav.popBackStack() }, { nav.navigate("session/$day") }, { nav.navigate("exercise/$it") })
            }
            composable("session/{day}", arguments = listOf(navArgument("day") { type = NavType.IntType })) {
                SessionModeScreen(it.arguments?.getInt("day") ?: state.currentDay, state, vm, { nav.popBackStack() }, { nav.navigate("exercise/$it") })
            }
            composable("exercise/{id}") {
                ExerciseDetailScreen(it.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
fun TodayScreen(state: AppUiState, vm: AppViewModel, start: (Int) -> Unit, exercise: (String) -> Unit) {
    val day = TransformaProgram.content.days.first { it.day == state.currentDay }
    val completed = state.sessions.count { it.completed }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Hoy: dia ${day.day}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(TransformaProgram.content.phases.first { it.id == day.phaseId }.title)
            LinearProgressIndicator(progress = { ProgramCalculations.progressPercent(completed) / 100f }, modifier = Modifier.fillMaxWidth())
            Text("$completed dias completados (${ProgramCalculations.progressPercent(completed)}%)")
        }
        item { DayCard(day, state.sessions.any { it.day == day.day && it.completed }, onClick = {}, compact = false) }
        item { Text("Calentamiento", style = MaterialTheme.typography.titleMedium); BulletList(TransformaProgram.content.guide.warmUp) }
        item { Text("Entrenamiento", style = MaterialTheme.typography.titleMedium); Text(day.prescription) }
        item { Text("Vuelta a la calma", style = MaterialTheme.typography.titleMedium); BulletList(TransformaProgram.content.guide.coolDown) }
        item { Text("Nutricion del dia", style = MaterialTheme.typography.titleMedium); Text(day.nutritionNote) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { start(day.day) }, modifier = Modifier.weight(1f)) { Text("Comenzar sesion") }
                OutlinedButton(onClick = { vm.markComplete(day.day) }, modifier = Modifier.weight(1f)) { Text("Marcar completada") }
            }
        }
        item { Text("Ejercicios", style = MaterialTheme.typography.titleMedium) }
        items(day.sessionRefs.flatMap { ref -> TransformaProgram.content.sessions.firstOrNull { it.id == ref }?.blocks.orEmpty() }.flatMap { it.items }.distinctBy { it.exerciseId }) { item ->
            TextButton(onClick = { exercise(item.exerciseId) }) { Text(item.exerciseId.replace("-", " ")) }
        }
    }
}

@Composable
fun ProgramScreen(state: AppUiState, openDay: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TransformaProgram.content.phases.forEach { phase ->
            item { Text(phase.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(TransformaProgram.content.days.filter { it.phaseId == phase.id }) { day ->
                DayCard(day, state.sessions.any { it.day == day.day && it.completed }, { openDay(day.day) })
            }
        }
    }
}

@Composable
fun DayDetailScreen(dayNumber: Int, state: AppUiState, vm: AppViewModel, onBack: () -> Unit, start: () -> Unit, exercise: (String) -> Unit) {
    val day = TransformaProgram.content.days.first { it.day == dayNumber }
    val refs = day.sessionRefs.mapNotNull { ref -> TransformaProgram.content.sessions.firstOrNull { it.id == ref } }
    Scaffold(topBar = { TopAppBar(title = { Text("Dia ${day.day}") }, navigationIcon = { TextButton(onClick = onBack) { Text("Atras") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text(day.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(day.prescription) }
            items(refs) { session ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(session.title, fontWeight = FontWeight.Bold)
                        Text(session.description)
                        session.blocks.forEach { block ->
                            Text(block.title, fontWeight = FontWeight.SemiBold)
                            block.items.forEach { TextButton(onClick = { exercise(it.exerciseId) }) { Text("${it.exerciseId.replace("-", " ")}: ${it.prescription}") } }
                        }
                    }
                }
            }
            item { Button(onClick = start, modifier = Modifier.fillMaxWidth()) { Text("Comenzar sesion") } }
            item { OutlinedButton(onClick = { vm.markComplete(day.day) }, modifier = Modifier.fillMaxWidth()) { Text("Marcar como completada") } }
        }
    }
}

@Composable
fun SessionModeScreen(dayNumber: Int, state: AppUiState, vm: AppViewModel, onBack: () -> Unit, exercise: (String) -> Unit) {
    val day = TransformaProgram.content.days.first { it.day == dayNumber }
    val steps = day.sessionRefs.mapNotNull { ref -> TransformaProgram.content.sessions.firstOrNull { it.id == ref } }
        .flatMap { session -> session.blocks.flatMap { block -> block.items.map { session.title to it } } }
    var index by remember(dayNumber) { mutableIntStateOf(state.sessions.firstOrNull { it.day == dayNumber }?.inProgressStep ?: 0) }
    var paused by remember { mutableStateOf(false) }
    val current = steps.getOrNull(index)
    Scaffold(topBar = { TopAppBar(title = { Text("Sesion dia $dayNumber") }, navigationIcon = { TextButton(onClick = onBack) { Text("Salir") } }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LinearProgressIndicator(progress = { if (steps.isEmpty()) 1f else (index + 1) / steps.size.toFloat() }, modifier = Modifier.fillMaxWidth())
            if (current == null) {
                Text("Sesion lista para finalizar.")
            } else {
                Text(current.first, style = MaterialTheme.typography.titleMedium)
                Text(current.second.exerciseId.replace("-", " "), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(current.second.prescription)
                current.second.timedSeconds?.let { TimerBox(it, paused) }
                TextButton(onClick = { exercise(current.second.exerciseId) }) { Text("Ver tecnica") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { index = (index - 1).coerceAtLeast(0); vm.saveSessionStep(dayNumber, index) }, modifier = Modifier.weight(1f)) { Text("Anterior") }
                OutlinedButton(onClick = { paused = !paused }, modifier = Modifier.weight(1f)) { Text(if (paused) "Reanudar" else "Pausar") }
                Button(onClick = { index = (index + 1).coerceAtMost(steps.size); vm.saveSessionStep(dayNumber, index) }, modifier = Modifier.weight(1f)) { Text("Siguiente") }
            }
            Button(onClick = { vm.markComplete(dayNumber); onBack() }, modifier = Modifier.fillMaxWidth()) { Text("Finalizar y guardar") }
        }
    }
}

@Composable
fun TimerBox(seconds: Int, paused: Boolean) {
    var remaining by remember(seconds) { mutableIntStateOf(seconds) }
    LaunchedEffect(seconds, paused, remaining) {
        if (!paused && remaining > 0) {
            kotlinx.coroutines.delay(1000)
            remaining -= 1
        }
    }
    AssistChip(onClick = {}, label = { Text("Temporizador: ${remaining}s") })
}

@Composable
fun ExerciseDetailScreen(id: String, onBack: () -> Unit) {
    val ex = TransformaProgram.content.exercises.firstOrNull { it.id == id }
    Scaffold(topBar = { TopAppBar(title = { Text(ex?.programName ?: "Ejercicio") }, navigationIcon = { TextButton(onClick = onBack) { Text("Atras") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (ex == null) item { Text("Ficha no encontrada.") } else {
                item { Text(ex.programName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(ex.clearName) }
                item { Text("Material", fontWeight = FontWeight.Bold); BulletList(ex.material) }
                item { Text("Posicion inicial", fontWeight = FontWeight.Bold); Text(ex.startPosition) }
                item { Text("Ejecucion paso a paso", fontWeight = FontWeight.Bold); BulletList(ex.steps) }
                item { Text("Respiracion", fontWeight = FontWeight.Bold); Text(ex.breathing) }
                item { Text("Errores frecuentes", fontWeight = FontWeight.Bold); BulletList(ex.commonErrors) }
                item { Text("Adaptacion", fontWeight = FontWeight.Bold); Text(ex.adaptation) }
                item { Text("Advertencia", fontWeight = FontWeight.Bold); Text(ex.painWarning) }
                item { Text("Aparece en", fontWeight = FontWeight.Bold); BulletList(ex.appearsIn) }
            }
        }
    }
}

@Composable
fun NutritionScreen() {
    val n = TransformaProgram.content.nutrition
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Nutricion", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Section("Objetivo de proteina", n.proteinGoal) }
        item { Section("Estructura diaria", n.dailyStructure) }
        item { Section("Distribucion de hidratos", n.carbDistribution) }
        item { Section("Grasas", n.fats) }
        items(n.menus) { menu -> MenuCard(menu) }
        item { Section("Alimentos prioritarios", n.priorityFoods) }
        item { Section("Alimentos eliminados durante 30 dias", n.eliminatedFoods) }
        item { Text("Celebraciones", fontWeight = FontWeight.Bold); Text(n.celebrationAlternative) }
        item { Section("Ajustes desde el dia 10", n.adjustments) }
    }
}

@Composable
fun ProgressScreen(state: AppUiState, vm: AppViewModel) {
    var checkpoint by remember { mutableIntStateOf(TransformaProgram.content.measurementDays.first()) }
    val current = state.measurements.firstOrNull { it.checkpointDay == checkpoint }
    var w1 by remember(current) { mutableStateOf(current?.weight1Kg?.toString().orEmpty()) }
    var w2 by remember(current) { mutableStateOf(current?.weight2Kg?.toString().orEmpty()) }
    var w3 by remember(current) { mutableStateOf(current?.weight3Kg?.toString().orEmpty()) }
    var waist by remember(current) { mutableStateOf(current?.waistCm?.toString().orEmpty()) }
    var belt by remember(current) { mutableStateOf(current?.beltHole.orEmpty()) }
    var notes by remember(current) { mutableStateOf(current?.notes.orEmpty()) }
    var frontUri by remember(current) { mutableStateOf(current?.frontPhotoUri) }
    var sideUri by remember(current) { mutableStateOf(current?.sidePhotoUri) }
    val frontPicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { frontUri = it?.toString() ?: frontUri }
    val sidePicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { sideUri = it?.toString() ?: sideUri }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Progreso", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransformaProgram.content.measurementDays.forEach { day ->
                    FilterChip(selected = checkpoint == day, onClick = { checkpoint = day }, label = { Text("Dia $day") })
                }
            }
        }
        item { Text("Peso medio: ${ProgramCalculations.averageWeight(listOf(w1.toDoubleOrNull(), w2.toDoubleOrNull(), w3.toDoubleOrNull()))?.let { "%.1f kg".format(it) } ?: "Sin datos"}") }
        item { NumberField("Peso manana 1 (kg)", w1) { w1 = it } }
        item { NumberField("Peso manana 2 (kg)", w2) { w2 = it } }
        item { NumberField("Peso manana 3 (kg)", w3) { w3 = it } }
        item { NumberField("Cintura a la altura del ombligo (cm)", waist) { waist = it } }
        item { OutlinedTextField(value = belt, onValueChange = { belt = it }, label = { Text("Agujero del cinturon") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { frontPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f)) { Text("Foto frontal") }
                OutlinedButton(onClick = { sidePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f)) { Text("Foto lateral") }
            }
        }
        item { OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    vm.saveMeasurement(MeasurementEntity(checkpoint, w1.toDoubleOrNull(), w2.toDoubleOrNull(), w3.toDoubleOrNull(), waist.toDoubleOrNull(), belt.ifBlank { null }, frontUri, sideUri, notes.ifBlank { null }, System.currentTimeMillis()))
                }, modifier = Modifier.weight(1f)) { Text("Guardar") }
                OutlinedButton(onClick = { vm.deleteMeasurement(checkpoint) }, modifier = Modifier.weight(1f)) { Text("Eliminar") }
            }
        }
        item { Text("Los datos y fotos se guardan localmente. No se suben a servidores.") }
    }
}

@Composable
fun MoreScreen(vm: AppViewModel) {
    var confirm by remember { mutableStateOf(false) }
    val g = TransformaProgram.content.guide
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Mas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Section("Objetivos realistas", g.objective) }
        item { Section("Reglas del programa", g.rules) }
        item { Section("Escala de esfuerzo percibido", g.effortScale) }
        item { Section("Calentamiento", g.warmUp) }
        item { Section("Vuelta a la calma", g.coolDown) }
        item { Section("Progresion con mancuernas", g.dumbbellProgression) }
        item { Section("Control de resultados", g.resultControl) }
        item { Section("Seguridad", g.safety) }
        item { Text("Privacidad", fontWeight = FontWeight.Bold); Text("Todos los datos personales permanecen en el dispositivo. No hay cuenta, anuncios, analitica externa ni backend.") }
        item { Text("Acerca de", fontWeight = FontWeight.Bold); Text("Version 1.0.0. App nativa Android para acompanar un programa de 30 dias.") }
        item { OutlinedButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) { Text("Reiniciar programa y borrar datos") } }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            confirmButton = { Button(onClick = { vm.resetAll(); confirm = false }) { Text("Borrar") } },
            dismissButton = { OutlinedButton(onClick = { confirm = false }) { Text("Cancelar") } },
            title = { Text("Confirmar borrado") },
            text = { Text("Esta accion elimina progreso y mediciones locales.") }
        )
    }
}

@Composable
fun DayCard(day: ProgramDay, completed: Boolean, onClick: () -> Unit, compact: Boolean = true) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Dia ${day.day}: ${day.title}", fontWeight = FontWeight.Bold)
            Text(if (completed) "Completado" else if (day.isRecovery) "Recuperacion" else "Pendiente")
            if (!compact) Text(day.prescription)
        }
    }
}

@Composable
fun Section(title: String, bullets: List<String>) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            BulletList(bullets)
        }
    }
}

@Composable
fun MenuCard(menu: Menu) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(menu.title, fontWeight = FontWeight.Bold)
            menu.meals.forEach { meal ->
                Text(meal.name, fontWeight = FontWeight.SemiBold)
                BulletList(meal.items)
            }
        }
    }
}

@Composable
fun BulletList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { Text("- $it") }
    }
}

@Composable
fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}
