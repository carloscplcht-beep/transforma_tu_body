# AGENTS.md

## Proyecto

App Android nativa Kotlin/Compose para `Transforma tu Body`.

## Reglas importantes

- No cambiar dias, sesiones, ejercicios, series, repeticiones, descansos, intensidades, menus, cantidades, ajustes ni advertencias sin cotejar el documento fuente.
- Mantener separado el contenido editorial de los datos de usuario.
- No anadir backend, login, anuncios, analitica externa ni permisos innecesarios.
- No registrar en logs peso, cintura, notas, rutas de fotos ni preferencias personales.
- No commitear `local.properties`, APKs, claves privadas ni datos personales.

## Comandos

```bash
./gradlew testDebugUnitTest assembleDebug
./gradlew lintDebug
```

## Entrega

El APK debug instalable se genera en `app/build/outputs/apk/debug/app-debug.apk`.

