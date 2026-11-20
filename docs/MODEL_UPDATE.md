# Sustitución de modelos — 16/11/2026

Se sustituyeron los modelos anteriores de Quaternius por los GLB suministrados
por el usuario, sin modificar sus bytes:

| Asset | Original | Tamaño | SHA-256 |
| --- | --- | --- | --- |
| trex.glb | Rampaging T-Rex.glb | 29 468 076 bytes | 16be5030cfebfb7f9364f6f7c693687d5c6beef346c700b98966b59da679fab2 |
| triceratops.glb | triceratops.glb | 3 787 568 bytes | 17806cf9ec82bf4ad5526172b277fea8a1a9dbb2576caaf3de023b8cba3714f5 |

Ambos son GLB 2.0 con recursos integrados. El T-Rex requiere
KHR_materials_pbrSpecularGlossiness y contiene cinco animaciones; el MVP conserva
su presentación estática. El límite de lectura se aumentó de 24 a 36 MiB para
admitir el archivo. No se cambió la correspondencia tarjeta/modelo ni el modo
de superposición fija.

Triceratops incluye autor Gigan_FK y licencia CC BY 4.0 en sus metadatos. Se
conservaron sus créditos en assets y en «Cómo funciona». El T-Rex no incluye
autor ni licencia en el archivo o carpeta suministrados; quedan pendientes de
documentar. Ver THIRD_PARTY.md y assets/models/CREDITS.txt.

## Verificaciones ejecutadas

Comando: `:app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest`.
Resultado: **BUILD SUCCESSFUL en 1 min 25 s**.

- 18 pruebas unitarias aprobadas.
- 6 pruebas instrumentadas aprobadas, ninguna omitida: cinco de reconocimiento
  sintético OpenCV y una de renderizado de cuatro assets, incluidos ambos nuevos
  dinosaurios, transparencia y recreación de actividad.
- La prueba gráfica admite alfa de fondo de hasta 1/255 por cuantización del
  swapchain del teléfono; exige geometría opaca visible en la región central.
- Dispositivo: HONOR CLK-LX3, Android 14/API 34, ARM64, Adreno 610, OpenGL ES 3.2.
- Tras las pruebas, Gradle retiró las aplicaciones temporales. Se reinstaló el
  APK final mediante `adb install -r` (Success) y se abrió MainActivity.
- Las capturas temporales no se recuperaron tras la limpieza de Gradle; no se
  entregan imágenes de renderizado como evidencia visual.
- No se ejecutó de nuevo Lint en esta actualización; el informe de la versión
  inicial no equivale a un análisis de estos cambios.
- Pendientes: detección con tarjetas impresas frente a cámara, modo avión y
  medición prolongada de memoria/rendimiento con el T-Rex de mayor tamaño.

APK: `app/build/outputs/apk/debug/app-debug.apk`, 204 978 494 bytes (~195,5 MiB).
SHA-256: `63796d129883161e04f667f1cff686c13ac11c1a2f4157270f20a258c1ef4aec`.
Informe Android: `app/build/reports/androidTests/connected/debug/index.html`.
