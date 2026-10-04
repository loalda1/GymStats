# Cómo revisar la entrega

Abre la carpeta **GymStats** en Android Studio. Lee primero **docs/VERIFICATION.md**: el código de esta reconstrucción no tiene todavía una compilación ni pruebas de ejecución confirmadas.

Para la interfaz: sincroniza Gradle, ejecuta `app` y pulsa **Explorar demo**. No requiere Firebase activo.

Para vuestro backend: lee **docs/FIREBASE_SETUP.md**. El JSON original se conserva en **private-config/google-services.json**. Puedes usar temporalmente el applicationId original con la propiedad indicada; para el identificador nuevo debes registrar otra app Android en Firebase.

Para comprobar las mejoras: **docs/CHANGELOG_ES.md** y **docs/MANUAL_QA.md**.

El proyecto incluye código, tests, reglas y CI. No se entrega una APK final ni capturas de una app en ejecución porque no se ha podido completar esa validación en este entorno. No publiques la carpeta private-config.
