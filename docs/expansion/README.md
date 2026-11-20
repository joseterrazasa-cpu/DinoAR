# Ampliación del catálogo — 20 de noviembre de 2026

## Resultado

Se incorporaron cinco animales a los tres existentes. El catálogo conserva
identificadores, fichas, referencias, encuadre, orientación y pistas esperadas.
El mismo detector, seguimiento visual 2D, gestos y animador se reutilizan para
todos. Los modelos se cargan individualmente cuando se confirma su tarjeta.

| Identificador | Modelo | Referencia PNG | Animaciones |
| --- | --- | --- | --- |
| PACHYCEPHALOSAURUS | pachycephalosaurus.glb | pachycephalosaurus_marker.png | 21 |
| MOSASAURUS | mosasaurus.glb | mosasaurus_marker.png | 1 |
| DIMORPHODON | dimorphodon.glb | dimorphodon_marker.png | 1 |
| SPINOSAURUS | spinosaurus.glb | spinosaurus_marker.png | 1 |
| ANKYLOSAURUS | ankylosaurus.glb | ankylosaurus_marker.png | 1 |

Los nombres se identificaron por archivos, carpetas, metadatos y revisión de los
renders. No se atribuye una especie concreta a los modelos: las fichas usan el
género seguido de «sp.». Mosasaurus se presenta como reptil marino y Dimorphodon
como pterosaurio, sin clasificarlos como dinosaurios.

## Fase 1 — Inspección

Se validaron las cabeceras GLB 2.0 y los tamaños de chunks. Los cinco archivos
incluyen recursos, pieles y animaciones, sin URI externas. Pachycephalosaurus
usa KHR_materials_pbrSpecularGlossiness, ya compatible con el gltfio actual y
verificada en ejecución. No se añadieron dependencias Android ni se modificaron
los archivos GLB. `assets.json` registra hashes, tamaños geométricos, pistas,
atribución original y límites calculados.

## Fase 2 — Tarjetas

Se conservaron formato cuadrado 1024 × 1024, papel claro, marco negro grueso,
esquina recortada y símbolos periféricos. Las nuevas ilustraciones son renders
monocromáticos de las propias mallas proporcionadas, no imágenes genéricas.
Se añade el nombre bajo el marco y una firma periférica diferente por tarjeta.
Las originales no fueron alteradas. El detector carga los PNG finales exactos.

`tools/glb_geometry.py` decodifica accesores, jerarquía de nodos, pesos y matrices
inversas para obtener geometría en pose de reposo. `generate-expansion-cards.py`
crea los renders y tarjetas mediante proyección ortográfica, sombreado facetado
y ordenación de triángulos. Son ilustraciones de identificación, no imágenes
fotográficas ni reconstrucciones científicas certificadas. No se procesan los
materiales PBR en este render offline; el renderizador Android sí carga el GLB.

Se utilizó una vista más elevada para Dimorphodon y Mosasaurus, mostrando alas
y aletas. Los renders independientes están en `renders/`. Los scripts requieren
NumPy y Pillow; la exportación PDF también ReportLab. No son dependencias del APK.

## Fase 3 — Catálogo y fichas

Se ampliaron DinosaurId y DinosaurRepository. Dinosaur incorpora nombre científico,
clasificación, características, fuente, créditos, animaciones esperadas y
ModelPresentation. Las fichas mantienen el layout existente y agregan esos campos.
El rótulo de clasificación ya no presupone que todos pertenecen al Cretácico.

ModelManager obtiene el fallback desde el catálogo: no hace falta agregar una
rama de carga por cada dinosaurio nuevo. El fallback tampoco reutiliza el
encuadre específico del modelo ausente.

### Fuentes educativas

Las fichas son resúmenes originales, con tamaños aproximados y sin identificar
especies a partir de una apariencia artística.

- [Pachycephalosaurus — Natural History Museum](https://www.nhm.ac.uk/discover/dino-directory/pachycephalosaurus.html): período, tamaño orientativo, dieta principalmente vegetal y debate sobre la cúpula craneal.
- [Mosasaurus y sus parientes — Natural History Museum](https://www.nhm.ac.uk/discover/what-is-a-mosasaur.html): reptil marino, dieta y variación/incertidumbre del tamaño entre especies.
- [Dimorphodon — Natural History Museum](https://www.nhm.ac.uk/discover/mary-anning-fossils.html): pterosaurio, dentición, longitud, envergadura y alimentación probable.
- [Dimorphodon — Phoenix Zoo](https://www.phoenixzoo.org/ditd-educational-resources/dino-factsheets/Dimorphodon/): Jurásico temprano.
- [Spinosaurus — Natural History Museum](https://www.nhm.ac.uk/discover/dino-directory/spinosaurus.html): período, longitud estimada, anatomía y alimentación; se conserva la incertidumbre sobre su modo de vida acuático.
- [Ankylosaurus — Natural History Museum](https://www.nhm.ac.uk/discover/dino-directory/ankylosaurus.html): período, tamaño, herbivoría, armadura y cola.
- [Clasificación de dinosaurios — Natural History Museum](https://www.nhm.ac.uk/discover/what-are-dinosaurs.html): separación de pterosaurios y mosasaurios respecto de Dinosauria.

## Fase 4 — Modelos y animaciones

Los encuadres se calcularon a partir de geometría con skinning en pose de reposo,
evitando normalizar con límites de malla que no representen el animal visible.
La orientación inicial es configurable desde ModelPresentation y se combina con
seguimiento y rotación del usuario. Los nodos originales del GLB no se reescriben.
Cada modelo conserva sus pistas y utiliza reproducción, pausa y selección comunes.

## Fases 5 y 6 — Reconocimiento e integración

MarkerDatabase recorre los ocho registros y precalcula ORB una vez al crear el
detector. No se modificaron umbrales ni la lógica de ORB/Hamming/RANSAC.
La asociación tarjeta/modelo/ficha es la del repositorio. Las pruebas gráficas
ahora recorren el catálogo completo y aplican su encuadre y presentación.

## Fase 7 — Verificación

Comando ejecutado:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest --offline --console=plain
```

- **BUILD SUCCESSFUL**, 2 min 8 s.
- **23 pruebas JVM aprobadas**.
- **14 instrumentadas aprobadas**, sin fallos ni omisiones, en Android físico.
- Incluyen ocho referencias con perspectiva sintética, rechazo de ruido y blanco,
  alternancia entre todas, cámara/recreación, geometría visible, transparencia y
  pistas esperadas de los diez assets probados (ocho animales y dos sólidos).
- Lint: **0 errores, 4 avisos**; tres sobre versiones y uno de posible sobrepintado.
- GLB originales intactos; hashes registrados en `assets.json`.
- APK final instalado mediante adb (Success) y MainActivity abierta.

APK: `app/build/outputs/apk/debug/app-debug.apk`, 279 712 569 bytes.
SHA-256: `0924fe333e89632e4d4ebd3fc5deddb9735de1d56e417af64537762f1e126ccf`.

La prueba sintética no equivale a reconocer impresiones reales. Pendientes:
iluminación, enfoque, distancias, movimiento físico de las nuevas tarjetas,
gestos manuales con esos animales, inspección completa de cada clip y una medición
prolongada de FPS, memoria y temperatura. No se promete rendimiento constante.

## Impresión

`output/pdf/DinoAR-tarjetas.pdf` contiene dos páginas A4 de tarjetas y una de
créditos. Las ocho imágenes completas se colocan a **88 × 88 mm**, cuatro por hoja.
Imprimir al 100 %, sin «ajustar a página». La regla de control debe medir 50 mm.
No recortar dentro de la imagen; el contorno y sus detalles pertenecen al marcador.

El PDF se exportó con ReportLab y se revisó renderizando las tres páginas con
PDFium. Las referencias embebidas proceden de los mismos assets del detector.
Los PNG nuevos también se distribuyen en `output/cards/DinoAR-nuevas-tarjetas.zip`.

### Reproducción de recursos

```text
python tools/generate-expansion-cards.py
python tools/export-card-pdf.py
```

Después de regenerar una tarjeta, recompilar y reimprimir. No cambiar las imágenes
tras validar reconocimiento sin repetir las pruebas.

## Archivos creados y modificados

**Creados:** cinco GLB en assets/models, cinco PNG en assets/markers,
tools/glb_geometry.py, tools/generate-expansion-cards.py, tools/export-card-pdf.py,
renders y assets.json en docs/expansion, PDF y ZIP de tarjetas.

**Modificados:** Dinosaur.kt, DinosaurRepository.kt, ModelManager.kt,
DinosaurRenderer.kt, CameraActivity.kt, DinosaurInfoActivity.kt,
activity_dinosaur_info.xml, strings.xml, pruebas instrumentadas de detector y
renderizador, printable/tarjetas.html y documentación del proyecto.

Los créditos conocidos de Pachycephalosaurus y Mosasaurus están en las fichas,
THIRD_PARTY.md y el PDF. Para Dimorphodon, Spinosaurus y Ankylosaurus no se
incluyó autor/licencia en los archivos suministrados: se registra esa ausencia,
sin asignarles una licencia inventada.
