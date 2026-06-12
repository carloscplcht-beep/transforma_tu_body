# Transforma tu Body

Aplicacion Android nativa para acompanar un programa de entrenamiento y nutricion de 30 dias. Funciona sin servidor, sin cuenta de usuario, sin anuncios, sin analitica externa y con los datos personales guardados localmente en el dispositivo.

## Funciones principales

- Pantalla **Hoy** con dia actual, progreso, calentamiento, entrenamiento, vuelta a la calma y acceso a sesion guiada.
- Programa completo de 30 dias agrupado en cuatro fases.
- Biblioteca de ejercicios con nombre original, explicacion comprensible, material, ejecucion, respiracion, errores, adaptacion y advertencia.
- Area de nutricion con objetivo de proteina, estructura diaria, hidratos, grasas, cuatro menus, alimentos prioritarios y eliminados.
- Progreso con controles en dias 1, 8, 15, 22 y 30, hasta tres pesos por control, cintura, cinturon, fotos locales y notas.
- Recordatorios locales opcionales.
- Tema claro, oscuro o segun el sistema.

## Arquitectura

- Kotlin, Jetpack Compose, Material 3 y Navigation Compose.
- Estado unidireccional con `AppViewModel`, `StateFlow` y repositorios locales.
- DataStore para onboarding, tema, fecha de inicio y recordatorios.
- Room para sesiones completadas y mediciones.
- Contenido editorial estructurado en modelos de dominio, separado de estado de usuario y UI.

## Requisitos

- Android Studio o Android SDK con API 35.
- JDK 17.
- Gradle Wrapper incluido.

## Compilar

```bash
./gradlew testDebugUnitTest assembleDebug
```

En Windows:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest assembleDebug
```

APK debug instalable:

`app/build/outputs/apk/debug/app-debug.apk`

Nombre de entrega recomendado:

`TransformaTuBody-v1.0.0-debug.apk`

## Pruebas

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

Las pruebas unitarias validan la estructura esencial del programa: 30 dias, sesiones F1/F2/F3/C, ejercicios referenciados, menus, controles de medicion y reglas de seguridad/ajuste.

## Privacidad

Los datos se guardan en el dispositivo. La app no envia peso, cintura, notas, fotos ni preferencias a servidores. Consulta `docs/PRIVACIDAD.md`.

## Limitaciones

- No es una aplicacion clinica.
- No garantiza resultados.
- El APK release final requiere firma propia; el workflow puede generar un APK release sin firmar, pero no debe presentarse como release instalable definitiva.

## Licencia

Se conserva la licencia existente del repositorio. El contenido del programa procede del documento fuente aportado por el usuario y se incorpora para el funcionamiento offline de la app.

## Aviso de salud

Interrumpe el ejercicio ante dolor u opresion toracica, mareo, desmayo, palpitaciones anormales o dificultad respiratoria intensa. Consulta con un profesional cuando proceda.

