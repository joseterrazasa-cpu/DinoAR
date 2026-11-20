# Modelos para DinoAR

Se incluyen los modelos proporcionados por el usuario: `trex.glb` (Rampaging T-Rex, 29 468 076 bytes) y `triceratops.glb` (3 787 568 bytes). Consulta `CREDITS.txt` para su procedencia y atribución.

Los archivos `demo_cube.glb` y `demo_pyramid.glb` son sólidos 3D reales de prueba,
generados con `tools/generate-demo-models.py`; no representan dinosaurios.
La aplicación indica explícitamente cuándo los está utilizando.

Requisitos: glTF 2.0 binario (GLB), geometría y texturas dentro del archivo,
eje Y hacia arriba, unidades en metros, origen cerca del centro y materiales PBR.
Exportar una pose estática con normales. No usar recursos externos ni compresión
Draco para este MVP. Recomendado: menos de 50 000 triángulos, texturas de hasta
1024 px y menos de 10 MB. El cargador limita cada archivo a 36 MiB.
Se normaliza la caja envolvente para la vista fija: no conserva escala física.
La orientación debe ajustarse en el programa de modelado antes de exportar.
La primera animación se reproduce automáticamente si existe; la cámara incluye selector y pausa.
Se sigue la tarjeta en 2D y la escala aparente; no hay pose 3D calibrada.
Se conservan los nodos del GLB para combinar animación, seguimiento y gestos.

Usar únicamente modelos propios o con licencia que permita incluirlos y
redistribuirlos en el APK. Registrar autor, URL y licencia al incorporarlos.
