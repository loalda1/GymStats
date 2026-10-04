# Qué se ha cambiado

| Punto | Implementación |
|---|---|
| Interfaz | Material 3, inputs, cards, FAB, paleta propia e icono adaptive |
| Producto | Ejercicios, series/repeticiones/pesos, sesiones, historial y gráficos |
| Arquitectura | domain/data/ui/worker dentro de su ruta de paquete |
| Identificador | org.gymstats.android y opción legacy explícita |
| README | Instalación, arquitectura, datos, métricas y limitaciones |
| Pruebas | 23 unitarias, 17 de reglas y 2 de navegación escritas |
| Estados | StateFlow, loading/busy/error y eventos tipados |
| Coroutines | await y listeners encapsulados en el repositorio |
| Binding | ViewBinding y limpieza al destruir la vista |
| Lista | ListAdapter/DiffUtil |
| Recordatorios | Días/hora por usuario, WorkManager, permisos y cancelación |
| Seguridad | Reglas versionadas, validación anidada y escrituras atómicas |
| Limpieza | JSON duplicado eliminado, configuración original aislada e ignorada |
| Idiomas | Inglés/español con selector |
| CI | GitHub Actions para build, lint, tests y reglas |

Falta ejecutar build/tests de esta reconstrucción, revisar la interfaz en dispositivo, capturar pantallas reales y verificar vuestro Firebase. Las funcionalidades anteriores son código implementado, no resultados de ejecución certificados. Lee `VERIFICATION.md` antes de publicarlo.

No se ha asignado una licencia unilateralmente a un trabajo de tres autores ni se ha publicado a GitHub/LinkedIn desde aquí.
