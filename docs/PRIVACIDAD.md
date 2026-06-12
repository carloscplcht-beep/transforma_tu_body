# Privacidad

## Datos guardados

- Fecha de inicio.
- Preferencia de tema.
- Activacion y hora de recordatorios.
- Dias completados y avance de sesion.
- Medicion voluntaria: peso, cintura, agujero de cinturon, notas y URI de fotos seleccionadas.

## Dónde se guardan

Los datos se guardan en el almacenamiento privado de la app mediante DataStore y Room. Las fotos se seleccionan con el selector del sistema y no se copian ni suben a servidores.

## Qué no hace la app

- No crea cuenta.
- No usa backend.
- No envia datos personales a internet.
- No incorpora anuncios ni analitica externa.
- No registra mediciones en logs de produccion.

## Permisos

- `POST_NOTIFICATIONS`: solo para recordatorios locales, solicitado al activar la funcion.
- `SCHEDULE_EXACT_ALARM`: declarado para compatibilidad de recordatorios locales.
- `VIBRATE`: avisos locales de temporizador cuando el dispositivo lo permita.

## Borrado

La pantalla `Mas` permite reiniciar el programa y borrar progreso y mediciones con confirmacion.

