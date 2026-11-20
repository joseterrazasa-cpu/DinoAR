# DinoAR — Museo de Dinosaurios

Aplicación Android nativa para una exposición de Programación de Dispositivos
Móviles. Una tarjeta física selecciona el contenido virtual mediante reconocimiento
local de imágenes. La versión actual sigue la posición, el tamaño aparente y el giro 2D de la tarjeta
sobre la cámara; añade gestos y animaciones GLB. **No depende de ARCore**.
Consulta [la implementación interactiva y sus límites](docs/INTERACTION.md).

## Estado y alcance

Ampliación vigente: [cinco animales nuevos y ocho tarjetas](docs/expansion/README.md).
[PDF A4 imprimible](output/pdf/DinoAR-tarjetas.pdf) · [PNG nuevos](output/cards/DinoAR-nuevas-tarjetas.zip).

La ampliación a tres dinosaurios se describe en [VELOCIRAPTOR.md](docs/VELOCIRAPTOR.md).

- **Compilación final verificada:** APK debug generado y firmado; 23 pruebas JVM
  y 14 instrumentadas aprobadas en el HONOR; Android Lint con 0 errores y 4 avisos.
  Ver [INTERACTION.md](docs/INTERACTION.md) para el alcance de esta verificación.
- Código Kotlin, interfaces XML, ViewBinding y Material Components.
- CameraX: vista previa, permiso en ejecución y análisis en segundo plano.
- OpenCV: ORB, Hamming, ratio test, homografía RANSAC y validación geométrica.
- Ocho tarjetas suministradas por el usuario incluidas y listas para imprimir.
- Confirmación temporal, cambio de tarjeta, pérdida y recuperación de detección.
- Nombre y ficha del Tiranosaurio Rex y del Triceratops.
- Filament con OpenGL ES: carga GLB local y superficie transparente sobre CameraX.
- Modelos activos: Rampaging T-Rex, Triceratops y PBR Velociraptor (Animated), proporcionados por el usuario.
  Se incluyen completos en assets, con geometría y texturas integradas.
  Créditos en [THIRD_PARTY.md](THIRD_PARTY.md) y en «Cómo funciona».
- La verificación de los modelos y la etapa de interacción se registran en
  [docs/MODEL_UPDATE.md](docs/MODEL_UPDATE.md) y [docs/INTERACTION.md](docs/INTERACTION.md). Los resultados anteriores de
  TESTING.md corresponden a la versión inicial.


## Objetivo general

Construir un museo interactivo portátil que relacione tarjetas impresas con
información y objetos virtuales, sin servicios en la nube ni dispositivos
certificados para ARCore.

## Objetivos específicos

1. Capturar fotogramas sin bloquear la interfaz.
2. Distinguir ocho tarjetas mediante características visuales y geometría.
3. Reducir falsos positivos y cambios bruscos mediante confirmación temporal.
4. Asociar cada identificación con información educativa y un recurso GLB.
5. Separar captura, visión artificial, renderizado y presentación.
6. Documentar pruebas, límites y una posible evolución hacia pose 3D calibrada.

## Tecnologías y versiones

| Componente | Versión | Decisión |
| --- | --- | --- |
| Android Gradle Plugin | 9.3.3 | Conservado del proyecto; compatible con SDK 37 y Gradle 9.5 |
| Gradle Wrapper | 9.5.0 | Wrapper original con SHA-256 fijado |
| JDK de este entorno | JetBrains 25.0.2 | El criterio del daemon original pide Java 25; Java 8 no sirve |
| Kotlin | Integrado en AGP | Sin plugin Compose ni plugin Kotlin Android duplicado |
| SDK mínimo / compilación / destino | 26 / 37 / 37 | Android 8.0 o superior; no presupone variante de HONOR |
| AndroidX Core / AppCompat | 1.19.1 / 1.8.0 | Core original; AppCompat para actividades XML |
| Material Components | 1.13.0 | Componentes Views, no Compose |
| CameraX | 1.5.3 | Publicación estable; misma versión para core, camera2, lifecycle y view |
| OpenCV Android | 4.12.0 | AAR oficial de Maven Central con bibliotecas nativas incluidas |
| Filament / gltfio | 1.69.5 | Misma versión de motor y cargador; backend OpenGL explícito |
| JUnit | 4.13.2 | Pruebas JVM sin dispositivo |
| AndroidX Test JUnit / Espresso | 1.3.0 / 3.7.0 | Pruebas instrumentadas |

Se fijan publicaciones verificadas, no versiones dinámicas `+`. No se pretende
usar siempre la publicación más reciente. Las versiones seleccionadas ofrecen
las APIs necesarias para el MVP; la resolución de Gradle y `checkDebugAarMetadata`
comprueban compatibilidad con el proyecto. No hay dependencias ARCore, Unity,
Vuforia ni Compose. El manifiesto principal no solicita `INTERNET`.

Fuentes oficiales consultadas al implementar:

- [Compatibilidad de AGP 9.3](https://developer.android.com/build/releases/agp-9-3-0-release-notes).
- [Kotlin integrado en AGP](https://developer.android.com/build/migrate-to-built-in-kotlin).
- [Publicaciones de CameraX](https://developer.android.com/jetpack/androidx/releases/camera)
  y [ImageAnalysis](https://developer.android.com/media/camera/camerax/analyze).
- [AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat)
  y [Material Components 1.13.0](https://github.com/material-components/material-components-android/releases/tag/1.13.0).
- [OpenCV Android 4.12](https://docs.opencv.org/4.12.0/d5/df8/tutorial_dev_with_OCV_on_Android.html),
  [ORB](https://docs.opencv.org/4.12.0/d1/d89/tutorial_py_orb.html) y
  [homografía](https://docs.opencv.org/4.12.0/d1/de0/tutorial_py_feature_homography.html).
- [Filament 1.69.5](https://github.com/google/filament/tree/v1.69.5),
  [UiHelper](https://github.com/google/filament/blob/v1.69.5/android/filament-android/src/main/java/com/google/android/filament/android/UiHelper.java)
  y [especificación GLB/glTF 2.0](https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html).

## Arquitectura

```text
app/src/main/java/com/example/dinoar/
  MainActivity.kt                Inicio y explicación
  camera/
    CameraActivity.kt            Permisos, ciclo de vida y coordinación de la UI
    CameraManager.kt             Preview + ImageAnalysis vinculados a Lifecycle
    FrameAnalyzer.kt             Frecuencia de análisis, cierre de ImageProxy
  detection/
    DetectionConfig.kt           Umbrales configurables
    MarkerDatabase.kt            Referencias y descriptores precalculados
    MarkerDetector.kt            ORB + Hamming + RANSAC + validación
    DetectionResult.kt           Resultado 2D y estados de reconocimiento
    QuadGeometry.kt              Validación geométrica sin dependencias Android
    RecognitionStabilizer.kt     Confirmación, persistencia breve y cambio
  rendering/
    ModelManager.kt              Lectura asíncrona de GLB y selección de recurso
    DinosaurRenderer.kt          Motor, superficie, escena y recursos Filament
  model/
    Dinosaur.kt                  Identificadores y entidad educativa
    DinosaurRepository.kt        Dos fichas y rutas a assets
  ui/DinosaurInfoActivity.kt     Pantalla de información
  utils/
    LumaPlane.kt                 Lectura del plano Y con strides y recorte
    ImageUtils.kt                Rotación y reducción de la imagen gris
    WindowInsets.kt              Espacio para barras y recortes de pantalla
```

Flujo: `CameraX → FrameAnalyzer → MarkerDetector → RecognitionStabilizer → UI`.
Una identificación estable selecciona la ficha y solicita un GLB a `ModelManager`.
Filament recibe ese recurso en el hilo principal, que también posee su motor.
El detector y sus matrices nativas pertenecen a un único executor de análisis.
La lectura de modelos usa otro executor. Los callbacks obsoletos se descartan
mediante contadores de sesión para evitar cambios al abandonar la pantalla.

## CameraX y conversión de imagen

`PreviewView` muestra la cámara trasera. `ImageAnalysis` solicita una resolución
cercana a 640 × 480 y usa `STRATEGY_KEEP_ONLY_LATEST`: si el análisis tarda, conserva
el fotograma más reciente sin acumular una cola. La resolución finalmente elegida
depende de la cámara; no se presupone que sea exactamente la solicitada.

El analizador recibe `YUV_420_888`. Para ORB solo se necesita luminancia, por lo que
se copia el plano Y respetando `rowStride`, `pixelStride`, posición del buffer y
`cropRect`. No se interpreta el buffer como JPEG ni se mezclan indebidamente los
planos U/V. Después se aplica `imageInfo.rotationDegrees` y se limita el lado
mayor a 800 píxeles. Cada `ImageProxy` se cierra en `finally`, incluso al omitir
un frame o producirse un error. Se analizan como máximo unos 8 frames/s; el ritmo
real depende del hardware. El renderizado intenta mantenerse en torno a 30 fps,
sin prometer ese rendimiento en todas las variantes del dispositivo.

## OpenCV, ORB y reconocimiento

OpenCV se carga localmente con `OpenCVLoader.initLocal()`. No requiere instalar
OpenCV Manager. Al abrir el detector se decodifican las dos imágenes JPEG, se convierten a
gris y se calculan sus características una sola vez.

ORB combina detección de esquinas orientadas con descriptores binarios. Un
descriptor resume la apariencia local mediante bits. BFMatcher compara esos bits
con distancia Hamming; no es adecuado usar distancia Euclídea como si fueran
descriptores SIFT.

Por cada fotograma:

1. Extraer hasta 1500 características ORB de la imagen gris.
2. Buscar los dos vecinos más próximos de cada descriptor de referencia.
3. Aplicar ratio test: la mejor distancia debe ser menor que 0,75 veces la segunda;
   además se limita la distancia Hamming a 65 y se evitan destinos duplicados.
4. Exigir al menos 18 coincidencias antes de estimar geometría.
5. Estimar una homografía mediante RANSAC con umbral de reproyección de 4 píxeles.
6. Exigir al menos 14 inliers, proporción mínima 0,5 y cobertura suficiente de la
   referencia. Son valores iniciales ajustables, no una certificación de precisión.
7. Proyectar las cuatro esquinas. Rechazar valores no finitos, reflejos,
   cuadriláteros cruzados, cóncavos, diminutos, demasiado deformados o muy fuera
   del encuadre.
8. Elegir la tarjeta con más inliers. Si dos resultados tienen fuerza parecida,
   esperar a una observación menos ambigua. La demostración usa una tarjeta a la vez.
9. Confirmar tres resultados consecutivos. Conservar la identificación durante
   pérdidas menores o iguales a 900 ms; después ocultar el objeto y mostrar
   «Tarjeta fuera de cámara». Un cambio de tarjeta también necesita confirmación.

Los parámetros están en `DetectionConfig.kt`. Cambiar umbrales requiere volver a
probar tarjetas e imágenes desconocidas: relajar solo el mínimo de coincidencias
puede aumentar falsos positivos.

### Homografía y RANSAC

La homografía es una matriz 3 × 3 que relaciona puntos de dos vistas de un plano.
RANSAC propone transformaciones a partir de subconjuntos de coincidencias y mide
cuántas observaciones las apoyan. Los puntos compatibles se llaman *inliers*.
Esto permite descartar una parte de las coincidencias incorrectas.

Las esquinas resultantes están en coordenadas del frame de análisis ya rotado.
No se dibujan directamente sobre `PreviewView`, porque su recorte y escala pueden
ser distintos. **Una homografía 2D no es una pose 3D completa.** Este proyecto
la usa para validar la identificación, no para fingir un anclaje del modelo.

## Renderizado 3D

Filament crea un motor OpenGL, una escena, una cámara virtual y dos luces.
`gltfio` carga el GLB; su caja envolvente se normaliza para encajarlo en la vista.
Una `TextureView` no opaca y un swapchain con transparencia permiten componer el
objeto sobre CameraX. No hay fondo 3D que tape la cámara.

La posición y el giro 2D siguen la tarjeta. La escala es visual, no métrica.
Se reproduce animación esquelética cuando está disponible, con selección y pausa.
La cámara virtual no está calibrada con la cámara física: no hay inclinación 3D
de la tarjeta. La composición de gestos y seguimiento se explica en INTERACTION.md. Al cambiar de tarjeta se cancela la carga
anterior, se libera el asset previo y se carga el nuevo. Al detener la actividad
se pausa el bucle y al destruirla se liberan superficies, materiales y motor.

Para incorporar los dinosaurios reales, coloca:

```text
app/src/main/assets/models/trex.glb
app/src/main/assets/models/triceratops.glb
app/src/main/assets/models/velociraptor.glb
```

Recompila e instala. No es necesario cambiar Kotlin. Los requisitos de formato,
orientación, escala, tamaño y licencia están en
[models/README.md](app/src/main/assets/models/README.md). Un archivo existente pero
inválido produce un error visible; no se sustituye silenciosamente por otro modelo.

## Tarjetas imprimibles

Abre [printable/tarjetas.html](printable/tarjetas.html) en un navegador e imprime
en A4 al 100 %. El archivo usa los mismos JPEG que la aplicación; conserva la
carpeta del proyecto para que sus rutas relativas funcionen. También puedes
imprimir directamente los JPEG de `app/src/main/assets/markers/`.

Cada tarjeta se imprime aproximadamente a 88 × 88 mm en la hoja preparada.
Utiliza papel mate, impresión nítida y luz uniforme. La geometría funciona en
blanco y negro; el algoritmo no clasifica por colores. No recortes los patrones
periféricos ni estires las imágenes. Las tarjetas activas son las imágenes JPEG del usuario; el generador
`tools/generate-markers.ps1` conserva las tarjetas antiguas. Si lo modificas, debes regenerar, reimprimir y
recompilar para mantener idénticas la referencia y la tarjeta física.

## Instalación y generación del APK

1. Abre esta carpeta en Android Studio. Instala Android SDK Platform 37 y las
   herramientas que solicite Gradle. Acepta las licencias del SDK.
2. Usa el JDK de Android Studio en la configuración de Gradle. Este proyecto
   conserva `gradle/gradle-daemon-jvm.properties`, que solicita Java 25. El Java 8
   encontrado originalmente en la terminal no puede ejecutar esta compilación.
3. Pulsa **File → Sync Project with Gradle Files**. La primera sincronización
   requiere Internet para descargar herramientas y dependencias.
4. Compila desde Android Studio o ejecuta en PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

También está disponible `./tools/build-debug.ps1`, que incluye la compilación
del APK de pruebas instrumentadas. El APK de la aplicación queda en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

La firma debug es adecuada para esta demostración, no para distribuir una
versión final en una tienda. Gradle genera el certificado local de desarrollo.
El APK universal verificado ocupa aproximadamente 165 MiB porque incluye las
bibliotecas nativas para arm64-v8a, armeabi-v7a, x86 y x86_64. Tras verificar la ABI
del teléfono se puede preparar una variante específica para reducir el paquete.

### Conectar el HONOR X7b

Activa Opciones de desarrollador y Depuración USB en el teléfono. Usa un cable
que permita datos, desbloquéalo y acepta la clave RSA de este equipo. Comprueba:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb shell getprop ro.product.model
& $adb shell getprop ro.build.version.release
& $adb shell getprop ro.build.version.sdk
& $adb shell getprop ro.product.cpu.abilist
& $adb shell getprop ro.opengles.version
```

El estado debe ser `device`; `unauthorized` significa que falta autorización.
Si no aparece, revisa cable, modo USB y controladores. Para OpenGL ES, 196608
corresponde a 3.0 y 196610 a 3.2. Se requiere ES 3.0 o superior y Android API 26
o superior. La ABI debe coincidir con las bibliotecas nativas del APK: no se fija
un filtro ARM que impida probar en otras ABI soportadas por las dependencias.
No se asumen procesador, memoria, versión Android ni extensiones gráficas de
todas las variantes comerciales del HONOR X7b.

Instala con el botón **Run** de Android Studio o:

```powershell
& $adb install -r app/build/outputs/apk/debug/app-debug.apk
& $adb shell am start -n com.example.dinoar/.MainActivity
```

Concede el permiso de cámara al iniciar la exploración. Si fue denegado, el botón
«Abrir ajustes» permite concederlo manualmente. Encuadra una tarjeta completa,
espera la confirmación y abre su ficha. Retírala y prueba las otras dos. Repite con
modo avión para verificar funcionamiento sin conexión. La instalación y uso de
la aplicación no requieren descargar modelos ni datos.

## Pruebas y depuración

Consulta [docs/TESTING.md](docs/TESTING.md): diferencia compilación, pruebas JVM,
pruebas instrumentadas y ensayos físicos. Para ejecutar las instrumentadas,
conecta un dispositivo autorizado y ejecuta:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Los tags de Logcat son `DinoAR.Camera`, `DinoAR.Detector` y `DinoAR.Renderer`.
Registran apertura de cámara, inicialización OpenCV, características de referencia,
cambios de reconocimiento, inliers y carga de GLB. No guardan imágenes de cámara.

```powershell
& $adb logcat -s DinoAR.Camera DinoAR.Detector DinoAR.Renderer AndroidRuntime
```

## Diferencias respecto de ARCore

| DinoAR MVP | Seguimiento espacial con ARCore |
| --- | --- |
| Reconoce patrones visuales locales de ocho tarjetas | Ofrece seguimiento de cámara y funciones espaciales |
| Homografía para validar el plano de la tarjeta | Integra estimación del movimiento y otras capacidades del entorno |
| Modelo que sigue posición, tamaño aparente y giro 2D de la tarjeta | Puede situar contenido mediante poses y anclajes |
| Sin servicio de AR ni certificación ARCore | Depende de los requisitos y compatibilidad de ARCore |

La demostración explica la relación entre un marcador físico y contenido virtual.
No debe presentarse como una reconstrucción 3D del entorno ni como equivalente a
un sistema de seguimiento espacial calibrado.

## Limitaciones y mejoras futuras

- El T-Rex ocupa 28,1 MiB y usa texturas más pesadas que los modelos iniciales.
  Revisar memoria y fluidez en el teléfono antes de una demostración prolongada.
- Desenfoque, movimiento rápido, reflejos, sombras, baja resolución, impresión
  defectuosa, oclusiones y perspectiva extrema perjudican ORB. No hay detección
  perfecta ni una tasa de acierto medida todavía.
- Se selecciona una tarjeta dominante; no se muestran dos dinosaurios a la vez.
- El seguimiento es 2D, sin inclinación espacial calibrada, sombras reales
  ni oclusión con objetos del mundo.
- La frecuencia real y el consumo deben medirse en cada variante del teléfono.
- Una mejora posterior puede calibrar intrínsecos y distorsión, medir las tarjetas
  y usar `solvePnP`. Debe ajustar los intrínsecos al recorte, resolución y rotación
  del análisis; convertir explícitamente los ejes OpenCV/Filament y comprobar el
  error de reproyección. Después puede añadirse filtrado de pose y seguimiento
  óptico. **Esta fase no está implementada.**
- Otras mejoras: animación, recursos 3D optimizados, más fichas y mediciones
  sistemáticas de falsos positivos, latencia y recuperación.

## Exposición de 10 minutos

1. **0:00–1:00 — Problema y objetivo.** Presentar las ocho tarjetas y explicar la
   necesidad de una solución sin ARCore para el dispositivo disponible.
2. **1:00–2:00 — Demostración.** Abrir cámara, mostrar cada tarjeta y su ficha.
   Si aún hay sólidos de prueba, identificarlo expresamente.
3. **2:00–3:30 — CameraX.** Explicar Preview, ImageAnalysis, segundo plano y por
   qué se descartan fotogramas atrasados.
4. **3:30–5:30 — OpenCV.** Mostrar una tarjeta con sus detalles; explicar ORB,
   Hamming, ratio test, RANSAC e inliers sin presentar cada línea de código.
5. **5:30–6:30 — Estabilidad.** Retirar y recuperar una tarjeta; justificar tres
   confirmaciones y la tolerancia breve a pérdidas.
6. **6:30–7:30 — Filament.** Explicar assets GLB, capa transparente y selección
   del modelo. Mover la tarjeta para evidenciar la superposición fija.
7. **7:30–8:30 — Arquitectura.** Mostrar el flujo de módulos y dónde se liberan
   ImageProxy, matrices y recursos 3D.
8. **8:30–9:30 — Verificación.** Mostrar resultados reales de compilación y
   pruebas; demostrar modo avión solo si se realizó la prueba.
9. **9:30–10:00 — Límites y evolución.** Distinguir homografía de pose y proponer
   calibración + solvePnP como siguiente etapa.

Antes de exponer: probar en el mismo teléfono, imprimir tarjetas limpias, tener
batería suficiente, verificar luz/enfoque y conservar el APK junto al proyecto.

## Registro incremental y recursos

[docs/PHASES.md](docs/PHASES.md) registra los archivos y resultados de cada fase.
Los patrones de las tarjetas y los sólidos de prueba son recursos originales
generados en este proyecto. Sus scripts permiten reproducirlos. Las bibliotecas
mantienen sus respectivas licencias; ver [THIRD_PARTY.md](THIRD_PARTY.md).
