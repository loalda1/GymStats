# GymStats · Organiza tu entrenamiento 💪📱

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Authentication_%26_Firestore-DD2C00?logo=firebase&logoColor=white)
![Arquitectura](https://img.shields.io/badge/Arquitectura-MVVM-1565C0)
![Versión](https://img.shields.io/badge/Versión-1.0-2563EB)

**Tus rutinas, tu cuenta y tu planificación en una aplicación Android.**

GymStats es una aplicación nativa desarrollada en **Kotlin** para organizar rutinas de gimnasio. Permite crear una cuenta, guardar entrenamientos asociados a un día de la semana y gestionar las rutinas desde una interfaz móvil en español.

Desarrollada de forma individual como proyecto académico de **Programación Móvil**, combina autenticación con Firebase, persistencia en Cloud Firestore y una estructura basada en Fragments, ViewModel y Repository.

Esta documentación corresponde a la **versión inicial 1.0**, el punto de partida del proyecto en GitHub.

## ✨ Características

- 🔐 **Registro e inicio de sesión** — Acceso mediante correo y contraseña con Firebase Authentication.
- 🌐 **Acceso con Google** — Inicio de sesión con una cuenta de Google integrada con Firebase.
- 📝 **Creación de rutinas** — Nombre, descripción opcional y día de la semana.
- 📋 **Listado personal** — Consulta de las rutinas del usuario mediante RecyclerView.
- ✏️ **Edición de rutinas** — Carga de los datos existentes y actualización desde el formulario de gestión.
- 🗑️ **Eliminación de rutinas** — Borrado desde el listado y actualización de los datos mostrados.
- ☁️ **Persistencia en Firestore** — Almacenamiento bajo la cuenta del usuario autenticado.
- 📊 **Contador de rutinas** — Visualización del número de rutinas cargadas en la pantalla principal.
- 🔔 **Recordatorio de prueba** — Activación manual de una notificación mediante WorkManager, con una demora inicial de 10 segundos.
- ✅ **Validación de formularios** — Comprobación de campos obligatorios y longitud mínima de la contraseña al registrarse.
- 💬 **Mensajes de resultado** — Avisos sobre operaciones completadas y errores mediante Toast.
- 👤 **Identificación y cierre de sesión** — Visualización del correo de la cuenta y salida mediante Firebase Authentication.

## 🛠️ Tecnologías

| Tecnología | Uso en el proyecto |
| --- | --- |
| **Kotlin** | Lógica de la aplicación Android. |
| **XML y componentes Material** | Diseño de pantallas, formularios y controles. |
| **Fragments y Navigation Component** | Navegación entre acceso, registro, inicio y gestión de rutinas. |
| **ViewModel y LiveData** | Gestión y observación de los datos de las rutinas. |
| **RecyclerView** | Presentación del listado y acciones sobre cada rutina. |
| **Firebase Authentication** | Registro, acceso con correo, acceso con Google y cierre de sesión. |
| **Cloud Firestore** | Creación, consulta, actualización y eliminación de rutinas. |
| **WorkManager** | Ejecución diferida del recordatorio de entrenamiento. |
| **Gradle Kotlin DSL** | Configuración de compilación y dependencias. |

## 🏗️ Estructura del proyecto

Las rutas parten de la carpeta que contiene `settings.gradle.kts`. Los archivos Kotlin se encuentran dentro de `app/src/main/java/`.

| Ruta | Descripción |
| --- | --- |
| `app/build.gradle.kts` | Configuración Android, versión y dependencias. |
| `app/src/main/AndroidManifest.xml` | Actividad principal y permisos de Internet y notificaciones. |
| `app/src/main/java/com/example/gymstats/MainActivity.kt` | Toolbar, navegación y solicitud del permiso de notificaciones. |
| `app/src/main/java/model/Routine.kt` | Modelo de datos de una rutina. |
| `app/src/main/java/repository/RoutineRepository.kt` | Acceso a Firestore y operaciones sobre rutinas. |
| `app/src/main/java/viewmodel/RoutineViewModel.kt` | Datos observables y coordinación con el repositorio. |
| `app/src/main/java/ui/auth/` | Pantallas de inicio de sesión y registro. |
| `app/src/main/java/ui/home/HomeFragment.kt` | Listado, contador, cierre de sesión y activación del recordatorio. |
| `app/src/main/java/ui/routine/` | Formulario de creación y edición, y adaptador del listado. |
| `app/src/main/java/com/example/gymstats/worker/WorkoutReminderWorker.kt` | Canal y notificación del recordatorio. |
| `app/src/main/res/layout/` | Diseños XML de pantallas y elementos del listado. |
| `app/src/main/res/navigation/nav_graph.xml` | Destinos y acciones de navegación. |
| `gradle/libs.versions.toml` | Catálogo de versiones, bibliotecas y plugins. |
| `docs/FIREBASE_SETUP.md` | Guía de configuración de Firebase. |
| `.gitignore` | Exclusión de archivos locales, configuración Firebase y archivos de firma. |

### Organización del código

La gestión de rutinas sigue una estructura **MVVM con Repository**:

- **Vista:** los Fragments recogen las acciones del usuario y observan los cambios mediante LiveData.
- **ViewModel:** `RoutineViewModel` expone las rutinas, la rutina seleccionada y los mensajes de resultado.
- **Repositorio:** `RoutineRepository` obtiene el usuario autenticado y realiza las operaciones en Firestore.
- **Modelo:** `Routine` representa los datos de cada entrenamiento.

Las pantallas de autenticación utilizan Firebase Authentication directamente. El recordatorio se ejecuta en un Worker independiente.

## 🚀 Inicio rápido

### Requisitos

- Android Studio compatible con **Android Gradle Plugin 9.2.1**.
- SDK de compilación **Android 36.1**, tal como está configurado en el proyecto.
- JDK compatible con Gradle y el plugin Android; la configuración incluida del daemon indica **Java 21**.
- Dispositivo o emulador con **Android 7.0 / API 24 o superior**.
- Un proyecto Firebase con Authentication y Cloud Firestore configurados.

El repositorio incluye el wrapper de **Gradle 9.4.1**. El objetivo Java del código es **11**, distinto del JDK utilizado para ejecutar Gradle.

### Instalación

1. **Clona o descarga el repositorio.** Abre en Android Studio la carpeta que contiene `settings.gradle.kts`, `build.gradle.kts` y `app/`.
2. **Registra la aplicación en Firebase** con el identificador `com.example.gymstats`.
3. **Añade la configuración de Firebase** descargada para tu proyecto en `app/google-services.json`.
4. **Habilita Correo/contraseña y Google** en Firebase Authentication. Para Google, registra las huellas del certificado de desarrollo y descarga de nuevo el JSON actualizado.
5. **Crea Cloud Firestore y configura sus reglas de acceso** para que cada usuario autenticado pueda acceder únicamente a sus propias rutinas.
6. **Sincroniza Gradle**, instala los componentes de SDK solicitados y selecciona un dispositivo compatible.
7. **Ejecuta el módulo `app`** desde Android Studio.

Puedes consultar las huellas del certificado desde la raíz del proyecto:

```sh
# Linux / macOS
sh gradlew signingReport
```

```powershell
# Windows PowerShell
.\gradlew.bat signingReport
```

Consulta [la guía de configuración de Firebase](docs/FIREBASE_SETUP.md) para ampliar estos pasos. Esta versión necesita la configuración de Firebase para compilar y utilizar sus servicios.

## 📖 Cómo funciona

### Flujo de uso

1. **Crea una cuenta o inicia sesión** con correo y contraseña, o accede con Google.
2. **Consulta la pantalla principal**, donde aparecen tu correo, las rutinas guardadas y su contador.
3. **Añade una rutina** indicando nombre y día; puedes incluir una descripción.
4. **Edita o elimina una rutina** desde las acciones de su tarjeta en el listado.
5. **Activa el recordatorio** para probar la notificación de entrenamiento.
6. **Cierra sesión** desde la pantalla principal.

### Modelo de datos

Cada rutina se almacena en la siguiente ruta de Cloud Firestore:

```text
users/{uid}/routines/{routineId}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `id` | `String` | Identificador del documento de la rutina. |
| `name` | `String` | Nombre de la rutina. Obligatorio en el formulario. |
| `description` | `String` | Descripción opcional del entrenamiento. |
| `dayOfWeek` | `String` | Día indicado por el usuario como texto libre. Obligatorio en el formulario. |
| `createdAt` | `Long` | Marca temporal de creación, en milisegundos. |

El repositorio utiliza el UID de Firebase Authentication para seleccionar la colección del usuario. Las reglas de Firestore deben aplicar la restricción de acceso también en el servidor.

### Recordatorio de entrenamiento

El botón de recordatorio encola un trabajo no periódico de WorkManager con una demora inicial de **10 segundos**. El Worker muestra una notificación con el mensaje «Recuerda revisar tus rutinas de entrenamiento».

La ejecución depende de la planificación del sistema y puede producirse más tarde que la demora indicada. En Android 13 o superior se solicita el permiso de notificaciones; si se rechaza, el Worker no muestra el aviso.

## 📱 Pantallas principales

| Pantalla | Función |
| --- | --- |
| **Inicio de sesión** | Acceso con correo y contraseña o Google, y enlace al registro. |
| **Registro** | Creación de una cuenta con correo y contraseña de al menos seis caracteres. |
| **Inicio** | Listado de rutinas, contador, correo del usuario, recordatorio y cierre de sesión. |
| **Añadir / editar rutina** | Formulario compartido para guardar nuevas rutinas o actualizar las existentes. |

## 🎯 Alcance de la versión 1.0

La primera versión se centra en la **autenticación y gestión personal de rutinas**. El contador resume cuántas rutinas hay guardadas y el recordatorio permite demostrar el uso de tareas diferidas y notificaciones.

Esta versión no incorpora seguimiento de ejercicios, series, repeticiones o pesos, gráficos de progreso ni programación de recordatorios recurrentes. El día de entrenamiento se introduce como texto libre. Las mejoras del proyecto se documentarán en sus correspondientes versiones.

## 🧪 Desarrollo y pruebas

El proyecto incluye las pruebas de ejemplo de JUnit y Android instrumentado generadas con la estructura inicial. No incluyen una suite específica para validar la autenticación o la gestión de rutinas.

Con Firebase y el entorno Android configurados, puedes compilar y ejecutar las pruebas locales desde la raíz:

```sh
# Linux / macOS
sh gradlew assembleDebug testDebugUnitTest
```

```powershell
# Windows PowerShell
.\gradlew.bat assembleDebug testDebugUnitTest
```

Para las pruebas instrumentadas, conecta un dispositivo o inicia un emulador y ejecuta `connectedDebugAndroidTest` con el wrapper de Gradle.

## 🐛 Resolución de problemas

### Falta `google-services.json`

Coloca el archivo de tu aplicación Firebase en `app/google-services.json` y vuelve a sincronizar Gradle. El archivo no se distribuye en el repositorio.

### El acceso con Google falla

Comprueba el identificador de aplicación, el proveedor Google y las huellas del certificado de desarrollo. Utiliza el JSON actualizado: el plugin Google Services genera `default_web_client_id` a partir del cliente OAuth web de esa configuración.

### No se cargan o guardan las rutinas

Comprueba la sesión, la conectividad y la configuración de Cloud Firestore. Las reglas deben permitir las operaciones del usuario autenticado sobre `users/{uid}/routines`.

### No aparece la notificación

Comprueba los permisos de notificaciones y la configuración del canal de GymStats. WorkManager puede ejecutar el trabajo después de los 10 segundos de demora inicial según las condiciones del sistema.

## 🔐 Configuración local

El archivo `.gitignore` excluye la configuración de Firebase, `local.properties`, directorios de compilación, configuraciones locales y archivos de firma. Configura tu propio proyecto Firebase y mantén fuera del repositorio las claves privadas y credenciales de firma.

## 👤 Autoría

Proyecto diseñado y desarrollado **de forma individual por el autor de este repositorio**, en el contexto de la asignatura **Programación Móvil**.

GymStats reúne el trabajo de interfaz, navegación, autenticación, persistencia y tareas en segundo plano en una primera aplicación Android de gestión de rutinas.

## 📄 Licencia

Esta versión no incluye un archivo `LICENSE` ni declara una licencia de uso.

---

**Organiza tus rutinas. Construye tu constancia. 💪**
