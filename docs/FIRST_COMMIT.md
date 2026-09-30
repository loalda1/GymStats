# Publicar el primer commit

Trabaja dentro de la carpeta `GymStats`, donde está `settings.gradle.kts`. Crea en GitHub un repositorio vacío, sin README, licencia ni `.gitignore` generados, y ejecuta:

```sh
git init
git branch -M main
git add .
git status
git diff --cached --stat
```

Antes del commit, verifica que no aparecen `google-services.json`, archivos de firma o configuraciones locales. Revisa también la atribución de autores del README.

```sh
git commit -m "Initial academic version of GymStats"
git tag v1.0-original
git remote add origin https://github.com/TU_USUARIO/GymStats.git
git push -u origin main
git push origin v1.0-original
```

Sustituye `TU_USUARIO` y el nombre del repositorio por los tuyos. El ZIP preparado no contiene un repositorio Git ni se ha publicado automáticamente.

Para incorporar después la versión mejorada, conserva `.git` y sustituye únicamente los archivos del proyecto. Revisa eliminaciones de archivos antiguos y cambios antes de crear el siguiente commit. No copies archivos privados ni `.git` de otro proyecto.
