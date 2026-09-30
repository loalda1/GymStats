# Configurar Firebase para la versión original

Los archivos de configuración del proyecto Firebase original no se distribuyen en este repositorio. Conserva tu copia privada si quieres seguir utilizando ese proyecto.

1. Crea un proyecto Firebase propio, o utiliza uno al que tengas acceso.
2. Registra una aplicación Android con el paquete **`com.example.gymstats`**, que coincide con el `applicationId` original.
3. Descarga su archivo `google-services.json` y colócalo en **`app/google-services.json`**. Está excluido por `.gitignore`.
4. Habilita los proveedores **Correo/contraseña** y **Google** en Firebase Authentication.
5. Para Google, registra las huellas SHA requeridas de tu certificado de desarrollo. Puedes consultarlas desde la raíz con `gradlew.bat signingReport` en Windows o `./gradlew signingReport` en macOS/Linux. Descarga de nuevo la configuración tras habilitar el proveedor y registrar las huellas.
6. Crea la base de datos Cloud Firestore. La aplicación accede a `users/{uid}/routines`.
7. Revisa y configura sus reglas para que cada usuario autenticado acceda únicamente a sus datos. El ZIP original no incluye un archivo de reglas: esta preparación tampoco afirma que el backend esté protegido ni añade reglas nuevas.
8. Sincroniza Gradle y ejecuta la aplicación.

El plugin Google Services genera `default_web_client_id` a partir del cliente OAuth web presente en tu configuración. Se ha eliminado el valor fijo del proyecto anterior de `strings.xml`. Si el recurso no se genera, revisa que Google esté habilitado y que el JSON actualizado contenga el cliente OAuth web correspondiente.

Esta versión conserva la lógica y dependencias originales. No incluye modo demo: sin `app/google-services.json`, el plugin Google Services impedirá completar la compilación.

No subas certificados de firma, contraseñas ni claves privadas de cuentas de servicio. El archivo de configuración del cliente Firebase no sustituye unas reglas de acceso correctas.
