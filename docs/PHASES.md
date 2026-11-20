# Registro por fases

Ampliación vigente (20/11/2026): cinco animales nuevos, tarjetas PNG derivadas de
sus GLB y PDF A4 de ocho tarjetas. Compilación, 23 pruebas JVM y 14 instrumentadas
aprobadas. [Detalles y archivos](expansion/README.md).

Última ampliación: [Velociraptor](VELOCIRAPTOR.md), tercera tarjeta y modelo con
26 animaciones. Compilación, 23 pruebas JVM y 9 instrumentadas aprobadas;
APK instalado en el HONOR. Incluye encuadre específico del esqueleto sin alterar
el GLB original.

Etapa actual: seguimiento visual 2D, gestos y animaciones implementados;
23 pruebas JVM y 8 instrumentadas aprobadas. Alcance, archivos y limitaciones:
[INTERACTION.md](INTERACTION.md).

Actualización posterior: modelos del usuario incorporados y seis pruebas
instrumentadas aprobadas en el teléfono. Ver [MODEL_UPDATE.md](MODEL_UPDATE.md).

## 1. Inspección y configuración

Encontrada una plantilla de Android Studio basada en Compose, sin lógica de DinoAR,
sin assets y sin repositorio Git inicializado. SDK 36 y 37 instalados; terminal con
Java 8 y Android Studio con JBR 25.0.2. Se conservaron AGP 9.3.3, Gradle 9.5.0,
minSdk 26 y SDK 37. Se verificaron publicaciones y documentación oficiales antes
de fijar dependencias. La resolución Gradle confirmó disponibilidad de artefactos.

Modificados: `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`,
`gradle.properties`. Eliminadas las tres clases de `ui/theme` de la plantilla
Compose. Activados ViewBinding, CameraX, OpenCV, AppCompat, Material y Filament.
Limitación: Gradle usa caché externa y primera descarga por Internet.

## 2. Inicio y navegación

Creados: layouts `activity_main`, `activity_camera`, `activity_dinosaur_info`,
dibujables `fossil` y `panel_background`, `DinosaurInfoActivity`, modelos
`Dinosaur`/`DinosaurRepository` y utilidad `WindowInsets`.
Modificados: `MainActivity`, `strings.xml`, `colors.xml`, `themes.xml` y manifiesto.
Implementados inicio, instrucciones, fichas, navegación y textos en español.
Verificación: recursos y Kotlin incluidos en una compilación base exitosa.
Pendiente: inspección visual en el teléfono.

## 3. CameraX

Creados: `CameraActivity.kt`, `CameraManager.kt`.
Modificados: manifiesto (CAMERA y ES 3.0), layout de cámara y recursos.
Implementados permiso, recuperación vía Ajustes, Preview, análisis con executor,
KEEP_ONLY_LATEST, mensajes de error y liberación vinculada al ciclo de vida.
Prueba: `assembleDebug` exitoso (base, 18 s). No se probó apertura física.
El dispositivo apareció sin autorización y posteriormente desconectado.

## 4. OpenCV y referencias

Creados: `MarkerDatabase`, PNG de ambas tarjetas, `tools/generate-markers.ps1`,
`printable/tarjetas.html`. Implementados carga local de OpenCV, extracción única
de descriptores y liberación de matrices de referencia. Las tarjetas son originales
y contienen patrones diferentes que no requieren clasificación por color.
Prueba: generador ejecutado, PNG inspeccionado visualmente y assets empaquetados.
Pendiente: validación de características y detección en runtime Android.

## 5. Reconocimiento

Creados: `DetectionConfig`, `DetectionResult`, `MarkerDetector`, `QuadGeometry`,
`RecognitionStabilizer`, `FrameAnalyzer`, `LumaPlane` e `ImageUtils`.
Implementados ORB/Hamming, ratio test, correspondencias únicas, RANSAC, cobertura,
geometría, resolución de ambigüedad, confirmación temporal y tolerancia de pérdida.
La conversión respeta stride, recorte y rotación, y cierra ImageProxy en finally.
Prueba: compilación OpenCV integrada exitosa (27 s).
Limitación: umbrales iniciales sin calibración empírica con impresiones.

## 6. Identidad e información

Modificada `CameraActivity`: vincula identificación estable con el nombre,
estados visibles y `DinosaurInfoActivity`. Repositorio con ambas fichas solicitadas.
Se invalidan callbacks de sesiones anteriores al salir o reintentar.
Prueba: incluida en la compilación exitosa de reconocimiento.
Pendiente: navegación e identificación verificadas en dispositivo.

## 7. Renderizado

Creados: `DinosaurRenderer`, `ModelManager`, `tools/generate-demo-models.py`,
`demo_cube.glb`, `demo_pyramid.glb` y `assets/models/README.md`.
Modificada `CameraActivity` para cargar, sustituir y retirar modelos; eliminada
la dependencia innecesaria filament-utils al usar directamente el motor y gltfio.
Implementados backend OpenGL, transparencia, luces, normalización, carga asíncrona,
cancelación de resultados antiguos, límite de archivo y destrucción de recursos.
Se usa un objeto básico distinto por tarjeta mientras faltan los dinosaurios.
Pruebas: generador de sólidos ejecutado; ver resultado final en TESTING.md.
Pendiente: confirmación visual de Filament en el HONOR y modelos artísticos.

## 8. Seguimiento espacial

No implementado. No se ejecutaron pruebas físicas del MVP ni hay calibración de
cámara. Las esquinas 2D se calculan para validar reconocimiento, pero no se usan
como una pose 3D ficticia. README explica calibración, tamaño físico, solvePnP y
conversión de ejes necesarios para una futura etapa.

## 9. Verificaciones y APK

Creadas pruebas JVM de estabilización, geometría y plano Y; pruebas instrumentadas
de reconocimiento sintético y renderizado. Sustituidas las pruebas de ejemplo
que solo verificaban una suma o el nombre del paquete. Creada una actividad de
pruebas en `src/debug`, no exportada, excluida de release.
Creado `tools/build-debug.ps1` para reproducir las tareas de verificación.
Los resultados finales y limitaciones están en `docs/TESTING.md`.

Resultado final: `BUILD SUCCESSFUL`, 18 pruebas JVM aprobadas, Lint sin errores
y con 9 avisos de versiones. APK de aplicación y APK instrumentado generados;
firma y alineación del APK de aplicación verificadas. No se ejecutaron pruebas
instrumentadas ni físicas por falta de dispositivo autorizado, según la decisión
del usuario de continuar sin el teléfono.

Durante esta fase se corrigieron la firma de `Camera.lookAt`, los recursos de API
27, sobrepintado de layouts y recursos sin usar. Se añadieron
`res/values-v27/themes.xml` y `res/layout-land/activity_camera.xml`; se actualizaron
los iconos del launcher y las reglas de extracción de datos, y se eliminó
`res/xml/backup_rules.xml`. El detalle de archivos y funcionalidades de las fases
previas sigue siendo aplicable al código final.

## 10. Documentación académica

Creados `README.md`, `THIRD_PARTY.md`, `docs/TESTING.md` y este registro.
Incluyen objetivo, arquitectura, algoritmo, diferencias con ARCore, dependencias,
instalación, comprobación Android/ABI/GPU, impresión, pruebas pendientes y exposición
de 10 minutos. Los modelos faltantes y la ausencia de anclaje espacial están
identificados explícitamente.
