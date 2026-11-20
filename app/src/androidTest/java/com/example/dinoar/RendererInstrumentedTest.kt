package com.example.dinoar

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RendererInstrumentedTest {
    @Test fun followsPlacementAndUserScaleInRenderedPixels() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch(RenderTestActivity::class.java).use { scenario ->
            val ready = CountDownLatch(1)
            val bytes = context.assets.open("models/demo_cube.glb").use { it.readBytes() }
            scenario.onActivity {
                it.renderer.load(ByteBuffer.allocateDirect(bytes.size).apply { put(bytes); flip() }) { ready.countDown() }
            }
            assertTrue(ready.await(15, TimeUnit.SECONDS))
            fun place(fraction: Float) {
                scenario.onActivity {
                    it.renderer.track(com.example.dinoar.rendering.CardPlacement(
                        it.texture.width * fraction, it.texture.height * 0.65f,
                        it.texture.width * 0.14f, 0f))
                }
            }
            fun bounds(): Pair<Float, Float> {
                var result = 0f to 0f
                scenario.onActivity {
                    assertNull(it.renderError)
                    val bitmap = requireNotNull(it.texture.bitmap)
                    var minX = bitmap.width; var maxX = 0
                    for (y in 0 until bitmap.height step 4) for (x in 0 until bitmap.width step 4) {
                        if (android.graphics.Color.alpha(bitmap.getPixel(x,y)) > 200) {
                            minX = minOf(minX,x); maxX = maxOf(maxX,x)
                        }
                    }
                    assertTrue("Debe haber geometría visible", maxX > minX)
                    result = ((maxX + minX) / 2f / bitmap.width) to ((maxX-minX).toFloat() / bitmap.width)
                    bitmap.recycle()
                }
                return result
            }
            place(0.25f)
            Thread.sleep(600) // Allow the render thread and the 90-ms smoothing filter to settle.
            val left = bounds()
            place(0.75f)
            Thread.sleep(600)
            val right = bounds()
            assertTrue("El modelo debe desplazarse con la tarjeta", right.first-left.first > 0.4f)
            scenario.onActivity { it.renderer.scaleBy(1.5f) }
            Thread.sleep(200)
            assertTrue("El gesto debe escalar la geometría", bounds().second > right.second * 1.3f)
        }
    }

    @Test fun rendersDinosaursAndSolidsWithTransparentBackgroundAndRecreates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch(RenderTestActivity::class.java).use { scenario ->
            val catalog = com.example.dinoar.model.DinosaurRepository.dinosaurs
            (listOf("demo_cube.glb", "demo_pyramid.glb") + catalog.map { it.modelPath.substringAfterLast('/') }).forEach { name ->
                val bytes = context.assets.open("models/$name").use { it.readBytes() }
                val ready = CountDownLatch(1)
                scenario.onActivity {
                    val dinosaur = catalog.firstOrNull { it.modelPath == "models/$name" }
                    it.renderer.load(ByteBuffer.allocateDirect(bytes.size).apply { put(bytes); flip() }, dinosaur?.framing,
                        dinosaur?.presentation ?: com.example.dinoar.model.ModelPresentation()) { ready.countDown() }
                }
                assertTrue("Carga GLB agotó tiempo", ready.await(15, TimeUnit.SECONDS))
                scenario.onActivity {
                    val names = it.renderer.animationNames
                    if (name == "trex.glb") assertEquals(5, names.size)
                    if (name == "triceratops.glb") assertTrue(names.isEmpty())
                    if (name == "velociraptor.glb") assertEquals(26, names.size)
                    catalog.firstOrNull { it.modelPath == "models/$name" }?.expectedAnimations?.let { expected ->
                        assertEquals("Pistas de $name", expected, names.size)
                    }
                    names.indices.forEach { index -> it.renderer.selectAnimation(index) }
                    it.renderer.selectAnimation(0)
                    it.renderer.scaleBy(1.2f)
                    it.renderer.rotateBy(30f, 15f)
                    it.renderer.setAnimationPlaying(false)
                    it.renderer.setAnimationPlaying(true)
                    it.renderer.resetInteraction()
                }
                // Wait for a rendered surface, checking pixels rather than only the load callback.
                var visible = false
                val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
                while (!visible && System.nanoTime() < deadline) {
                    scenario.onActivity {
                        assertNull(it.renderError)
                        it.texture.bitmap?.let { bitmap ->
                            // A dinosaur's center can lie between legs or outside its silhouette.
                            var opaqueSamples = 0
                            for (y in bitmap.height / 4 until bitmap.height * 3 / 4 step 8) {
                                for (x in bitmap.width / 8 until bitmap.width * 7 / 8 step 8) {
                                    if (android.graphics.Color.alpha(bitmap.getPixel(x, y)) > 200) opaqueSamples++
                                }
                            }
                            visible = opaqueSamples > 50
                            // Allow 1/255 quantization on the device's transparent swapchain.
                            assertTrue("El fondo debe ser transparente",
                                android.graphics.Color.alpha(bitmap.getPixel(1, 1)) <= 1)
                            if (visible) {
                                val output = java.io.File(context.getExternalFilesDir(null), "render-$name.png")
                                output.outputStream().use { stream -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream) }
                            }
                            bitmap.recycle()
                        }
                    }
                    if (!visible) Thread.sleep(50)
                }
                assertTrue("No hay geometría visible para $name", visible)
            }
            scenario.recreate()
            scenario.onActivity { assertNull(it.renderError) }
        }
    }
}
