# Arquitectura

La app usa un modulo Android unico para mantener una primera version compacta y mantenible.

## Capas

- `content`: programa estructurado offline.
- `domain`: modelos y calculos puros.
- `data`: Room, DataStore y repositorios.
- `ui`: pantallas Compose y navegacion.
- `notifications`: recordatorios locales.

## Datos

DataStore guarda preferencias ligeras: onboarding, fecha de inicio, tema y recordatorios. Room guarda sesiones completadas y mediciones. Las fotos se referencian mediante URI local del selector del sistema.

## Estado

`AppViewModel` combina preferencias, progreso y mediciones con `StateFlow`. Las pantallas reciben estado inmutable y emiten acciones.

