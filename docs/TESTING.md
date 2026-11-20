# Verificación de DinoAR

**Catálogo vigente de ocho animales:** 23 pruebas JVM, 14 instrumentadas
aprobadas y Lint sin errores. Resultados, recursos y pendientes en
[expansion/README.md](expansion/README.md).

**Catálogo de tres dinosaurios:** 23 pruebas JVM y 9 instrumentadas aprobadas,
incluido Velociraptor; ver [VELOCIRAPTOR.md](VELOCIRAPTOR.md).

**Versión interactiva actual:** 23 pruebas JVM y 8 instrumentadas aprobadas;
Lint sin errores. Ver [INTERACTION.md](INTERACTION.md) para seguimiento y gestos.

Los resultados siguientes describen la versión inicial.
La sustitución de modelos y las pruebas ya ejecutadas en el teléfono están en
[MODEL_UPDATE.md](MODEL_UPDATE.md).

Fecha de trabajo: 16 de noviembre de 2026 (America/La_Paz).

## Evidencia disponible

La base XML + CameraX y la integración posterior de OpenCV tuvieron compilaciones
`assembleDebug` exitosas. La ejecución final del código completo terminó con
**BUILD SUCCESSFUL en 3 min 56 s**, 86 tareas (29 ejecutadas y 57 actualizadas).
Se ejecutaron `assembleDebug`, `testDebugUnitTest`, `lintDebug` y
`assembleDebugAndroidTest`. Las 18 pruebas JVM pasaron sin errores ni omisiones.
Lint terminó con **0 errores y 9 avisos**, exclusivamente por versiones de
dependencias más nuevas. No se desactivó Lint ni se añadió un baseline para ocultar
errores. Se mantienen las versiones verificadas y fijadas descritas en README.

APK final: `app/build/outputs/apk/debug/app-debug.apk`, **172 761 673 bytes**
(aproximadamente 165 MiB). APK instrumentado: 596 175 bytes.
Firma APK v2 verificada con `apksigner verify --verbose`.
La comprobación `zipalign -c -P 16 4` terminó con código 0.

SHA-256 del APK de aplicación:

```text
83f3d4345ed8ebed77ebc3d99fdad8564a027f7669c9313841573fef84efb1ea
```

`aapt dump badging` confirmó minSdk 26, targetSdk 37, OpenGL ES 3.0 y bibliotecas
arm64-v8a, armeabi-v7a, x86 y x86_64. Los únicos permisos listados fueron CAMERA y
el permiso interno de receptores de AndroidX; no figura INTERNET. Esto es evidencia
estática, no sustituye la prueba de modo avión.

También se inspeccionaron las cabeceras PNG (720 × 1000 en ambas referencias) y
los chunks JSON/BIN, límites de buffer y geometría de los dos GLB de prueba. Son
archivos glTF 2.0 binarios con 24 vértices (cubo) y 16 (pirámide). Esta comprobación
estructural no equivale a renderizarlos en el teléfono.

ADB detectó inicialmente un dispositivo `unauthorized`; después dejó de aparecer.
El usuario indicó continuar sin el teléfono. **No se ejecutaron pruebas físicas,
no se instaló el APK en el HONOR y no se verificaron su Android, ABI ni GPU.**

## Automatizadas

| Prueba | Cobertura | Ejecución |
| --- | --- | --- |
| RecognitionStabilizerTest (8) | Confirmación, ruido, pérdidas, cambio, recuperación | 8 aprobadas |
| QuadGeometryTest (7) | Perspectiva, reflejo, cruce, degeneración, límites | 7 aprobadas |
| LumaPlaneTest (3) | Stride, padding, recorte y posición del buffer | 3 aprobadas |
| Android Lint | APIs, recursos y análisis estático Android | 0 errores; 9 avisos de versiones |
| MarkerDetectorInstrumentedTest (5) | Ambas referencias, perspectiva sintética, blanco, ruido y cambio | Compiladas; no ejecutadas |
| RendererInstrumentedTest (1) | Carga de ambos sólidos, píxel central, transparencia y recreación | Compilada; no ejecutada |

Las pruebas instrumentadas usan la biblioteca nativa real. Las de OpenCV generan
escenarios sintéticos y comprueban las esquinas reproyectadas: no miden iluminación,
enfoque, movimiento ni calidad de impresión. La actividad `RenderTestActivity`
solo existe en la variante debug, no está exportada ni tiene entrada en el launcher.

Comandos reproducibles:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
# Solo con un dispositivo autorizado:
.\gradlew.bat :app:connectedDebugAndroidTest
```

Reportes locales: `app/build/reports/tests/testDebugUnitTest/index.html` y
`app/build/reports/lint-results-debug.html`. Las pruebas instrumentadas generan
sus informes únicamente cuando se ejecutan; compilar su APK no equivale a pasarlas.

## Lista de pruebas manuales para la demostración

Todas pendientes de realizar en el teléfono. Anotar versión Android, ABI, GPU,
fecha, dimensiones impresas, luz y resultado observado.

| Nº | Acción | Resultado esperado |
| --- | --- | --- |
| 1 | Abrir DinoAR desde el launcher | Inicio visible, controles legibles, sin crash |
| 2 | Abrir «Cómo funciona» y cerrarlo | Explicación completa y regreso al inicio |
| 3 | Iniciar cámara en instalación nueva | Solicitud de CAMERA; no solicita otros permisos |
| 4 | Denegar permiso y reabrir | Mensaje explicativo y botón hacia Ajustes; no crash |
| 5 | Conceder permiso desde Ajustes | Se inicia cámara al volver a la pantalla |
| 6 | Iniciar exploración | Preview real; Logcat confirma CameraX y OpenCV |
| 7 | Mostrar TREX completa y enfocada | Confirmación estable, nombre y cubo/modelo TREX |
| 8 | Mostrar TRICERATOPS | Confirmación estable, nombre y pirámide/modelo correspondiente |
| 9 | Mostrar mesa, mano, libro y otras imágenes | No activar un dinosaurio; registrar cualquier falso positivo |
| 10 | Alternar ambas tarjetas lentamente | Cambiar nombre, ficha y objeto sin mezclar resultados |
| 11 | Tapar tarjeta menos de 900 ms | Mantener resultado brevemente sin parpadeo |
| 12 | Retirar tarjeta más de 900 ms | Ocultar modelo, desactivar Información, aviso de pérdida |
| 13 | Volver a mostrar la tarjeta | Confirmar de nuevo y recuperar el objeto |
| 14 | Pulsar Información de cada dinosaurio | Período, alimentación, longitud y descripción correctos |
| 15 | Volver de la ficha y volver al inicio | Cámara se reanuda; al salir se libera el acceso a cámara |
| 16 | Cambiar orientación, bloquear y desbloquear | Sin fugas, crashes ni resultados antiguos |
| 17 | Ejecutar con modo avión | Mismo flujo local; no descarga recursos |
| 18 | Sustituir por los dos GLB artísticos y recompilar | Mostrar dinosaurios y retirar aviso de objeto de prueba |
| 19 | Incorporar un GLB inválido en una copia de prueba | Error visible; cámara e información siguen disponibles |
| 20 | Usar cámara 5 minutos y alternar fichas | Comprobar temperatura, latencia, memoria y estabilidad |

Para inspección gráfica, comprobar que la cámara se ve alrededor del sólido y que
no aparece un rectángulo opaco. En paisaje y con tamaño de fuente aumentado,
comprobar que los botones permanecen accesibles.

## Protocolo de reconocimiento

Usar las tarjetas suministradas, una a la vez, sin plastificado brillante.
Empezar con la tarjeta ocupando aproximadamente entre un tercio y dos tercios de
la altura del encuadre y ajustar distancia según enfoque. Repetir con luz uniforme,
luz reducida, inclinación moderada y movimiento. Registrar al menos diez intentos
por tarjeta y condición: acierto, falso positivo, tiempo de aparición y pérdida.
No hay porcentajes medidos todavía; no atribuir al algoritmo una precisión no probada.

## Errores conocidos del entorno

- El Java de PATH era 8: usar JDK de Android Studio.
- La caché Gradle está fuera del workspace y requirió autorización de escritura.
- Hubo un fallo intermedio por `CameraActivity` aún no creada; la siguiente
  compilación con la actividad presente terminó correctamente.
- La integración 3D detectó una llamada incompleta a `Camera.lookAt`; se corrigió
  incluyendo el vector vertical. Lint detectó un atributo de API 27 en recursos
  generales; se movió a `values-v27` conservando compatibilidad con API 26.
- Gradle avisa que empaqueta algunas bibliotecas nativas sin eliminar símbolos;
  no es un error de compilación, pero aumenta el tamaño del APK debug.
- Sin teléfono autorizado no se puede asegurar el funcionamiento gráfico, los
  permisos ni el reconocimiento de tarjetas físicas en una variante concreta.
