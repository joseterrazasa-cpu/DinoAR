# Velociraptor — ampliación del catálogo

Se añadió VELOCIRAPTOR como tercer DinosaurId. DinosaurRepository incorpora su
ficha (Cretácico tardío, carnívoro, aproximadamente 2 m), referencia y modelo.
La base ORB recorre el catálogo y precalcula automáticamente la nueva referencia.
No se modificaron umbrales, versiones de dependencias ni el algoritmo del detector.

## Recursos proporcionados por el usuario

- Tarjeta: `app/src/main/assets/markers/velociraptor_marker.jpg`, copia de
  `image_18249627.jpg`. SHA-256:
  `3c452a6a0356121a000610d0a32ab2690db78fe44c9a6332cb081d1cf3210c67`.
- Modelo: `app/src/main/assets/models/velociraptor.glb`, copia sin modificaciones
  de `pbr_velociraptor_animated.glb`. Tamaño: 14 996 912 bytes. SHA-256:
  `5f7c8887d7ffdd46cce2988a5b3cc54497bc0c738819bc9be325630512e6d8e8`.
- GLB 2.0 con recursos integrados, sin extensiones obligatorias adicionales.
  Contiene 26 animaciones: Sniff, Eat Prey, Drink, Call_Alert, Go to Sleep,
  Sleep_Loop, Wake From Sleep, Idle_01, Idle_02, Roar_01, Roar_02, Roar_03,
  Bite_01, Bite_02, Leap_01, Leap_02, Tackle, Hurt_01, Hurt_02, Knocked Down,
  Death_01, Death_02, Walk, Jog, Sprint y Action_Pose.

Se reproduce inicialmente Sniff. Las demás se seleccionan con los controles
existentes. Se conserva seguimiento visual 2D, pellizco, rotación, reset y pausa.
Al cambiar de tarjeta se retira el modelo anterior y se confirma el nuevo.
La hoja `printable/tarjetas.html` incluye ahora las tres tarjetas en varias filas.

Créditos: PBR Velociraptor (Animated), Ferocious Industries, CC BY 4.0,
según asset.extras del archivo. Fuente y licencia están en THIRD_PARTY.md y
en «Cómo funciona» dentro de la aplicación. La imagen de la tarjeta fue
proporcionada por el usuario.

## Verificación

Las pruebas instrumentadas incluyen perspectiva sintética del velociraptor,
cambio entre las tres referencias y carga/renderizado del nuevo modelo,
comprobando sus 26 pistas disponibles. Esto no demuestra que se hayan inspeccionado
visualmente las 26 animaciones completas ni que se haya probado la tarjeta impresa.
Resultado final: **BUILD SUCCESSFUL en 1 min 10 s**. Se ejecutaron assembleDebug,
testDebugUnitTest, lintDebug y connectedDebugAndroidTest: 23 pruebas JVM y 9
instrumentadas aprobadas en el HONOR CLK-LX3/Android 14, sin omisiones.
Lint terminó con 0 errores y 10 avisos (versiones y fondo del selector).
La prueba gráfica confirmó geometría visible y transparente tras corregir el
encuadre. Las pruebas de detector incluyeron la nueva referencia con perspectiva
y alternancia entre las tres tarjetas.

APK instalado con adb (Success) y MainActivity abierta. Tamaño: 221 126 921 bytes.
SHA-256: `8cab183666cf3df791bd8b8cb8bc30383d672b5a07c7b4fb5fe5c794ad07c02f`.
Permanece pendiente el ensayo de la tarjeta impresa frente a la cámara y el
rendimiento sostenido con las distintas animaciones.

## Encuadre de la malla animada

La primera prueba gráfica detectó que la normalización automática ocultaba el
modelo: los límites previos al skinning abarcaban aproximadamente 97 unidades,
mientras que la geometría deformada por el esqueleto abarca unas 3,63.
Se calculó la pose de reposo con matrices globales de nodos, inverseBindMatrices,
JOINTS_0 y WEIGHTS_0 sobre POSITION. Sus mínimos son
(-0,615964; -0,009737; -2,259996) y máximos (0,557339; 1,909905; 1,367143).

ModelFraming almacena centro y semiextensiones de esa caja para este modelo.
CameraActivity lo pasa al renderizador; los otros modelos siguen usando sus
límites automáticos. El GLB original permanece idéntico. El encuadre no se
recalcula en cada animación para evitar cambios de tamaño al mover las patas.
