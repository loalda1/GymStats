# Verificación de esta entrega reconstruida — 30/09/2026

**Esta versión no dispone de una compilación Android confirmada.** Durante el intento de ejecutar Gradle, el wrapper no pudo descargar Gradle 9.4.1: `java.net.SocketException: Network is unreachable`. No hay SDK Android ni dependencias Gradle suficientes instaladas en esta ejecución. El registro está en `docs/gradle-attempt.log`.

| Comprobación | Resultado de este ZIP |
|---|---|
| XML de recursos y manifest | 32 archivos analizados; bien formados |
| Recursos ES/EN | 92 claves con paridad |
| Referencias locales de strings, IDs, layouts y drawables | Sin referencias faltantes en el chequeo estático |
| Rutas/paquetes Kotlin | Coherentes |
| Navigation y clases de Fragment | Destinos con clases presentes |
| ViewBinding y layouts | Nombres coherentes |
| JSON/TOML | Sintaxis válida; aliases Gradle presentes |
| Workflow YAML | Analizado sin error sintáctico |
| Script de seguridad JavaScript | `node --check` pasa |
| Unit tests | 23 escritos; ejecución pendiente |
| Firestore Rules tests | 17 escritos; ejecución pendiente |
| Espresso | 2 escritos; ejecución pendiente |
| Build/lint/APK | Intento bloqueado al descargar Gradle; no APK final |
| Ejecución y revisión visual | Pendiente; sin capturas reales |
| Firebase de vuestra cuenta | No conectado ni modificado |

Los chequeos estáticos no son una compilación: no verifican tipos Kotlin, inflación Android, APIs de dependencias ni interpretación de reglas Firestore. El informe de detalle está en `static-checks.json`; se puede regenerar con `python3 tools/verify_static.py`.

En la ejecución anterior, antes de perderse los archivos, se habían obtenido resultados positivos en pruebas. Esos resultados no se reutilizan como validación de esta reconstrucción, que contiene diferencias y pruebas adicionales.

## Comandos pendientes

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
./gradlew connectedDebugAndroidTest
npm install --ignore-scripts --no-audit --no-fund
npm run test:rules
```

Después, completa `MANUAL_QA.md`, captura las pantallas, comprueba Firebase real y revisa los informes de CI. Hasta entonces, el estado adecuado del proyecto es **mejoras implementadas en código, validación de ejecución pendiente**, no una app ya publicada o certificada como lista para producción.
