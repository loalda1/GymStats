# Configuración y compatibilidad con vuestro Firebase

## Identificador nuevo: opción para el portfolio

1. Conserva vuestro proyecto Firebase si queréis mantener las mismas cuentas y datos.
2. En Configuración del proyecto → Tus apps, añade Android con **org.gymstats.android**.
3. Ejecuta `./gradlew signingReport` y registra SHA-1/SHA-256 de la clave que utilices realmente. La clave debug de otro ordenador tiene otras huellas.
4. Activa Email/Password y Google en Authentication. Completa los ajustes de OAuth solicitados por Firebase.
5. Descarga el JSON de la app nueva y guárdalo en `app/google-services.json`. Sincroniza Gradle.
6. Ejecuta las pruebas de reglas y revisa `firestore.rules` antes de desplegarlo a tu proyecto elegido.
7. Prueba las cuentas y el CRUD real siguiendo `MANUAL_QA.md`.

El identificador OAuth se extrae del JSON local durante la configuración de Gradle; no hay un client ID falso ni credenciales de administrador en el código.

## Reutilizar temporalmente la app antigua

El JSON original está conservado en `private-config/google-services.json`. Copia ese archivo a `app/google-services.json` y ejecuta:

```sh
./gradlew -Pgymstats.applicationId=com.example.gymstats assembleDebug
```

O añade esto a tu `~/.gradle/gradle.properties` local:

```properties
gymstats.applicationId=com.example.gymstats
```

El namespace del código seguirá siendo `org.gymstats.android`; el applicationId con el que Firebase identifica la instalación será el original. Registra también las huellas de la clave de este ordenador. No se ha renombrado el paquete dentro del JSON para intentar engañar a Firebase.

Antes de publicar, registra la app nueva, usa su JSON y elimina la propiedad legacy. `.gitignore` excluye tanto el JSON activo como `private-config/`. No incluyas esa carpeta al compartir un ZIP público.

## Datos previos

Los campos `id`, `name`, `description`, `dayOfWeek` y `createdAt` siguen presentes. El ID del documento Firestore es el que usa el repositorio aunque el campo guardado fuera distinto. Una rutina antigua sin ejercicios carga con una lista vacía; puedes editarla y añadirlos conservando su fecha de creación.

No se ejecuta una migración destructiva, no se borran colecciones y no se inventan ejercicios para tus rutinas anteriores.

## Backend nuevo

Las sesiones usan metadatos y bloques de hasta cinco series. Las reglas exigen escritura atómica, propietario correcto, cantidades coherentes y valores válidos. Una sesión completada no puede modificarse; se permite repetir exactamente la misma escritura. Al borrar una rutina permanece el historial; al borrar un entrenamiento se borran también sus bloques mediante batch.

El acceso cliente no sustituye a las reglas. Despliega las reglas suministradas al proyecto correcto y prueba con dos usuarios distintos. No hay permisos amplios para cualquier usuario autenticado.

## Configuración que no se ha cambiado desde aquí

No se han modificado proveedores, huellas, usuarios, OAuth, App Check, billing, reglas desplegadas ni datos de vuestra cuenta Firebase. Las pruebas locales tampoco demuestran que esa configuración externa esté correcta.
