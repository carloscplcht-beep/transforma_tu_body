# Pruebas

## Ejecutadas localmente

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
cmd /c gradlew.bat testDebugUnitTest assembleDebug
```

Resultado observado: superado.

## Cobertura

- Integridad de contenido: 30 dias, sesiones, ejercicios, menus, checkpoints, reglas.
- Calculos: dia actual, porcentaje de progreso, media de tres pesos, variacion y validacion.
- UI Compose basica: onboarding y nutricion mediante pruebas instrumentadas preparadas.

## Pendiente segun entorno

`connectedDebugAndroidTest` requiere emulador o dispositivo disponible con `adb`.

