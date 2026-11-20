# Seguimiento e interacción — 16/11/2026

## Inspección y alcance

Antes, FrameAnalyzer solo comunicaba cambios de RecognitionState y descartaba
las esquinas de cada detección. DinosaurRenderer normalizaba el modelo y mantenía
una cámara virtual fija. No había animación ni controles táctiles.

Ahora se conserva la arquitectura y las versiones de dependencias. Los GLB no
se modifican. Esta etapa implementa registro visual 2D: centro, escala aparente
y giro en el plano de la tarjeta. No implementa inclinación espacial ni distancia
métrica. No hay calibración de cámara en el proyecto; no se ejecuta solvePnP con
intrínsecos inventados ni se interpreta una homografía como pose 3D completa.

## Flujo de seguimiento

1. ORB y RANSAC siguen produciendo cuatro esquinas validadas en el frame gris,
   recortado, rotado y reducido.
2. FrameAnalyzer envía cada detección de la tarjeta confirmada, además de los
   cambios de estado. No activa la pose de otra tarjeta aún no confirmada.
3. CameraManager vincula Preview e ImageAnalysis con un UseCaseGroup y el mismo
   ViewPort de PreviewView. Esto alinea su región del sensor. Se conserva cámara
   trasera, rotación de pantalla y FILL_CENTER.
4. CardTracking transforma el frame vertical/horizontal a la vista usando escala
   de llenado y desplazamientos de recorte centrados. La TextureView ocupa toda
   la superficie, también en paisaje. El promedio de esquinas determina el centro;
   el promedio de los bordes superior/inferior determina tamaño y giro 2D.
5. El renderizador usa una cámara frontal virtual de proyección conocida y coloca
   el objeto en un plano de profundidad constante. Las coordenadas de pantalla
   se convierten a unidades de ese plano: no representan profundidad física.
6. Se filtran posición, tamaño y ángulo con interpolación exponencial de 90 ms,
   usando tiempo por frame y el camino angular corto. No hay extrapolación durante
   la pérdida: se conserva el último resultado hasta la tolerancia de 900 ms y
   después se retira el modelo. Cambiar de tarjeta descarta la carga anterior.

La escala acompaña el tamaño aparente y la rotación acompaña el giro en el plano.
Una tarjeta muy inclinada puede producir deformación de perspectiva que este
método 2D no reproduce. Para orientación 6-DoF hacen falta intrínsecos y distorsión
calibrados para la configuración usada, tamaño físico de la referencia, ajuste
de intrínsecos tras recorte/rotación y solvePnP con validación de reproyección.

## Transformaciones y gestos

Se crea una entidad padre del root glTF para conservar las transformaciones
originales y los nodos animados del archivo. Su composición es:

`traslación de tarjeta × giro 2D × escala aparente × escala del usuario × yaw base/usuario × pitch usuario × normalización`.

La normalización centra X/Z y coloca el punto inferior del volumen en el centro
de la tarjeta en pantalla. El tamaño inicial equivale a 1,6 veces su ancho
aparente, con multiplicador táctil entre 0,35 y 4. En el host de pruebas sin tarjeta
se muestra una vista previa de 2,4 unidades frente a las 1,8 anteriores.

Pellizcar modifica solo el multiplicador del modelo. Arrastrar horizontalmente
cambia yaw; verticalmente cambia pitch, limitado a ±40°. Los controles de los
paneles conservan sus eventos normales; los gestos comienzan en el área de cámara
libre. Dos dedos no aplican a la vez rotación por arrastre. «Restablecer» devuelve
escala y orientación manual a su valor inicial sin cambiar la tarjeta.

La selección manual se mantiene durante la misma detección y pérdidas breves;
se restablece al cambiar de tarjeta, tras perderla definitivamente o al abandonar
la pantalla. La recreación por orientación vuelve a iniciar reconocimiento y
controles, igual que el ciclo de vida original.

## Animaciones

Las APIs se comprobaron contra las clases de Filament/gltfio 1.69.5 instaladas:
FilamentInstance.getAnimator, getAnimationCount, getAnimationName,
getAnimationDuration, applyAnimation y updateBoneMatrices.

Al terminar la carga se anuncia la lista de pistas. Se reproduce la primera en
bucle, módulo su duración. El T-Rex suministrado contiene cinco; Triceratops no
contiene pistas. La lista solo aparece con más de una. Seleccionar otra reinicia
su tiempo, manteniendo el estado reproducir/pausar. La duración cero no genera
una división por cero. El tiempo se toma de Choreographer, se limita el salto por
frame a 100 ms y se reinicia la referencia temporal al pausar el renderizado.

No se aplica seguimiento sobre los nodos internos animados: la entidad padre lo
separa de huesos y animación. Se conserva la liberación del modelo, recursos,
superficies, nueva entidad padre y motor. Los clips con movimiento propio de raíz
pueden desplazar al animal respecto del punto de apoyo; no se eliminan esos datos
del GLB ni se añade blending entre pistas en esta etapa.

## Rendimiento y validación

Se mantienen análisis en executor, KEEP_ONLY_LATEST y máximo aproximado de 8
análisis/s. La vista interpola entre resultados a unos 30 renders/s; no se promete
ese ritmo en todo dispositivo. Solo se publica una actualización por frame
procesado de la tarjeta activa. Las matrices y animaciones cambian sin leer el
GLB nuevamente. Los resultados de cargas y sesiones antiguas se descartan.

No se han medido aún latencia, temperatura, uso de memoria ni FPS sostenidos con
el T-Rex pesado. Validar en el teléfono moviendo ambas tarjetas en horizontal,
vertical, acercamiento y giro en el plano; probar pellizco, arrastre, reset,
pausa, todas las animaciones, pérdida y recuperación, orientación del teléfono,
regreso de Información y modo avión. La detección física fue confirmada por el
usuario en la versión anterior; eso no prueba el nuevo seguimiento.

## Resultados ejecutados

- Compilación final: BUILD SUCCESSFUL; tareas assembleDebug, testDebugUnitTest,
  lintDebug y connectedDebugAndroidTest con dependencias locales (--offline).
- 23 pruebas JVM aprobadas: 5 de mapeo/ángulos, 8 de confirmación temporal,
  7 de geometría y 3 de luminancia.
- 8 instrumentadas aprobadas en HONOR CLK-LX3/Android 14, sin omisiones:
  5 de reconocimiento OpenCV sintético, 2 gráficas y 1 de cámara real.
- La prueba gráfica mide desplazamiento y escala en píxeles de un cubo renderizado.
  La otra carga los cuatro modelos, verifica transparencia, cinco pistas del
  T-Rex y ninguna del Triceratops, ejercita selección/pausa/reset y recrea el host.
- La prueba de cámara concede CAMERA, espera Preview STREAMING tras inicializar
  reconocimiento y recrea CameraActivity. Pasó con el ViewPort compartido.
- Lint: 0 errores, 10 avisos; nueve de versiones y uno de posible sobrepintado
  del fondo del ítem del selector, usado para asegurar contraste en el desplegable.
- Se corrigió el salto angular tras varias vueltas completas. Hubo un intento
  de compilación superpuesto que bloqueó R.jar; se repitió en serie con éxito.
- El APK final se reinstaló con adb (Success) y se abrió MainActivity.
- APK: app/build/outputs/apk/debug/app-debug.apk (206 129 537 bytes).
  SHA-256: dca23e70a7b37c2b1606587328e99e52aeb78d2e6e43009a15665444ff4a06ff.

No se afirma haber realizado movimientos de tarjetas impresas ni pellizcos
manuales durante esta sesión: esas pruebas y el rendimiento sostenido siguen
pendientes. La prueba sintética del detector no sustituye ese ensayo físico.

## Archivos de esta etapa

Modificados: camera/CameraManager.kt, camera/FrameAnalyzer.kt,
camera/CameraActivity.kt, rendering/DinosaurRenderer.kt, los dos layouts de
cámara, values/strings.xml y RendererInstrumentedTest.kt.

Creados: rendering/CardTracking.kt, layout/item_animation.xml,
CardTrackingTest.kt, CameraInstrumentedTest.kt y este documento.
También se actualizaron README, el índice de pruebas y el registro por fases.
No se cambiaron dependencias, modelos GLB ni el algoritmo de reconocimiento.
