# Comprobaciones de producto

## Demo y frontend

1. Instala y abre la app sin JSON activo. Debe abrir login sin fallar; pulsa Explorar demo.
2. Debe verse claramente el aviso de datos de ejemplo. Revisa resumen, rutinas, historial y estadísticas.
3. Crea una rutina con nombre y ejercicios. Prueba campos vacíos, 0 repeticiones, valores excesivos y más de 20 series.
4. Edita y borra una rutina. Debe pedir confirmación; cancelar no modifica datos.
5. Inicia una sesión. Cambia pesos y repeticiones; gira el dispositivo y comprueba la conservación del borrador. Prueba el temporizador.
6. Finaliza y guarda. El historial y las métricas deben reflejar los valores reales introducidos.
7. Modifica o borra la rutina original. El entrenamiento histórico debe conservarse.
8. Cambia idioma y tema; comprueba navegación, teclado, tamaños de fuente y contenido en pantallas pequeñas.
9. Cierra sesión. Debe volver al login; la demo se reinicia en la siguiente entrada.
10. Prueba TalkBack en inputs, FAB, navegación y gráficos. Revisa contraste y recortes en claro/oscuro.

## Backend conectado

1. Configura tu Firebase siguiendo `FIREBASE_SETUP.md`. Registra cuentas A y B.
2. A crea/edita/elimina sus rutinas. Comprueba la ruta `users/{uid}/routines`.
3. A completa una sesión. Verifica metadatos y todos los chunks. Los nombres y valores deben ser correctos.
4. B no debe ver datos de A. Ejecuta además las pruebas de reglas para accesos directos.
5. Desconecta la red, intenta guardar y comprueba el estado pendiente. Reconecta y espera confirmación real.
6. Sal de la cuenta A y entra en B. No deben permanecer en pantalla datos de A.
7. Prueba Google, cancelación del selector y recuperación de contraseña con la configuración OAuth real.

## Recordatorios

1. Selecciona al menos un día y una hora próxima, activa el aviso y concede permiso.
2. Verifica que no llega antes de la hora y que Android puede retrasarlo.
3. Desactívalo y comprueba que no llega. Cierra sesión y verifica que no llega un aviso de la cuenta anterior.
4. Revisa comportamiento tras reinicio, cambio de zona horaria y permiso denegado. No prometas precisión de una alarma exacta.

## Capturas para README/LinkedIn

Captura login, resumen, rutinas, detalle, sesión y estadísticas. Utiliza datos de demostración, sin correos personales. Con cada pantalla visible:

```sh
sh tools/capture_screenshot.sh dashboard
```

Añade las capturas reales a `docs/screenshots/` y enlázalas en el README. No se han generado capturas ficticias en esta entrega.
