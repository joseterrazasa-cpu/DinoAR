package com.example.dinoar.rendering

import android.opengl.Matrix
import android.util.Log
import android.view.Choreographer
import android.view.Surface
import android.view.TextureView
import com.google.android.filament.*
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.*
import java.nio.ByteBuffer
import com.example.dinoar.model.ModelFraming
import com.example.dinoar.model.ModelPresentation

/** Image-space card tracking with independent user transforms; no calibrated 3D pose. */
class DinosaurRenderer(private val texture: TextureView, private val onError: (Throwable) -> Unit) : AutoCloseable {
    private val engine: Engine
    private val renderer: Renderer
    private val scene: Scene
    private val view: View
    private val camera: Camera
    private val entities: EntityManager
    private val cameraEntity: Int
    private val lights = mutableListOf<Int>()
    private val materials: UbershaderProvider
    private val loader: AssetLoader
    private val resources: ResourceLoader
    private val helper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private var swapChain: SwapChain? = null
    private var asset: FilamentAsset? = null
    private var placementEntity = 0
    private var normalization = FloatArray(16)
    private var presentation = ModelPresentation()
    private var targetPlacement: CardPlacement? = null
    private var smoothPlacement: CardPlacement? = null
    private var userScale = 1f
    private var yaw = 0f
    private var pitch = 0f
    private var animationIndex = 0
    private var animationSeconds = 0f
    private var animationPlaying = true
    private var animationFrameAt = 0L
    private var transformFrameAt = 0L

    val animationNames: List<String>
        get() = asset?.instance?.animator?.let { animator ->
            List(animator.animationCount) { animator.getAnimationName(it).ifBlank { "Animación ${it + 1}" } }
        } ?: emptyList()

    fun selectAnimation(index: Int) {
        if (index !in animationNames.indices) return
        animationIndex = index
        animationSeconds = 0f
        animationFrameAt = 0L
    }
    fun setAnimationPlaying(playing: Boolean) { animationPlaying = playing; animationFrameAt = 0L }
    fun resetInteraction() { userScale = 1f; yaw = 0f; pitch = 0f }
    fun scaleBy(factor: Float) {
        if (factor.isFinite() && factor > 0) userScale = (userScale * factor).coerceIn(0.35f, 4f)
    }
    fun rotateBy(horizontal: Float, vertical: Float) {
        yaw = (yaw + horizontal * 0.35f) % 360f
        pitch = (pitch + vertical * 0.25f).coerceIn(-40f, 40f)
    }
    fun track(placement: CardPlacement) {
        targetPlacement = placement
        if (smoothPlacement == null) smoothPlacement = placement
    }
    private val readyEntities = IntArray(128)
    private var onReady: (() -> Unit)? = null
    private var loadStartedAt = 0L
    private var running = false
    private var closed = false
    private var lastRender = 0L
    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running || closed) return
            try {
                if (frameTimeNanos - lastRender >= 33_000_000L && helper.isReadyToRender) {
                    lastRender = frameTimeNanos
                    renderFrame(frameTimeNanos)
                }
                if (running) choreographer.postFrameCallback(this)
            } catch (error: Exception) {
                pause()
                onError(error)
            }
        }
    }

    init {
        Filament.init()
        Gltfio.init()
        entities = EntityManager.get()
        engine = Engine.create(Engine.Backend.OPENGL)
        renderer = engine.createRenderer()
        scene = engine.createScene()
        view = engine.createView()
        cameraEntity = entities.create()
        camera = engine.createCamera(cameraEntity)
        camera.lookAt(0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
        camera.setExposure(16f, 1f / 125f, 100f)
        view.scene = scene
        view.camera = camera
        view.blendMode = View.BlendMode.TRANSLUCENT
        renderer.clearOptions = Renderer.ClearOptions().apply {
            clear = true
            clearColor = floatArrayOf(0f, 0f, 0f, 0f)
        }
        materials = UbershaderProvider(engine)
        loader = AssetLoader(engine, materials, entities)
        resources = ResourceLoader(engine, true)
        addLight(-0.6f, -1f, -0.8f, 100_000f)
        addLight(0.8f, 0.1f, 0.5f, 35_000f, LightManager.Type.POINT)
        texture.isOpaque = false
        helper.isOpaque = false
        helper.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface, helper.swapChainFlags)
            }
            override fun onDetachedFromSurface() {
                swapChain?.let { engine.destroySwapChain(it) }
                engine.flushAndWait()
                swapChain = null
            }
            override fun onResized(width: Int, height: Int) {
                if (width <= 0 || height <= 0) return
                view.viewport = Viewport(0, 0, width, height)
                camera.setProjection(45.0, width.toDouble() / height, 0.1, 100.0, Camera.Fov.HORIZONTAL)
                engine.flushAndWait()
            }
        }
        helper.attachTo(texture)
        Log.i("DinoAR.Renderer", "Filament OpenGL: capa transparente inicializada")
    }

    private fun addLight(x: Float, y: Float, z: Float, intensity: Float,
                         type: LightManager.Type = LightManager.Type.DIRECTIONAL) {
        val entity = entities.create()
        LightManager.Builder(type).color(1f, 0.97f, 0.9f)
            .intensity(intensity).direction(x, y, z).position(-x * 3f, -y * 3f, -z * 3f)
            .falloff(10f).castShadows(false).build(engine, entity)
        lights += entity
        scene.addEntity(entity)
    }

    fun load(buffer: ByteBuffer, framing: ModelFraming? = null,
             presentation: ModelPresentation = ModelPresentation(), ready: () -> Unit) {
        check(!closed)
        clear()
        this.presentation = presentation
        try {
            val model = requireNotNull(loader.createAsset(buffer)) { "Filament no pudo leer el GLB" }
            asset = model
            require(model.resourceUris.isEmpty()) { "El GLB debe incluir todas las texturas y buffers" }
            val center = framing?.let { floatArrayOf(it.centerX, it.centerY, it.centerZ) } ?: model.boundingBox.center
            val extent = framing?.let { floatArrayOf(it.halfX, it.halfY, it.halfZ) } ?: model.boundingBox.halfExtent
            val longest = extent.max() * 2f
            require(longest.isFinite() && longest > 0f) { "Modelo sin geometrÃ­a vÃ¡lida" }
            // Parent owns tracking/user transforms. Animated glTF nodes keep their own hierarchy.
            placementEntity = entities.create()
            engine.transformManager.create(placementEntity)
            engine.transformManager.setParent(engine.transformManager.getInstance(model.root),
                engine.transformManager.getInstance(placementEntity))
            normalization = FloatArray(16)
            Matrix.setIdentityM(normalization, 0)
            Matrix.scaleM(normalization, 0, 1f / longest, 1f / longest, 1f / longest)
            Matrix.translateM(normalization, 0, -center[0], -center[1] + extent[1], -center[2])
            resetInteraction()
            animationIndex = 0
            animationSeconds = 0f
            animationFrameAt = 0L
            animationPlaying = true
            check(resources.asyncBeginLoad(model)) { "No se pudieron cargar los recursos del GLB" }
            model.releaseSourceData()
            onReady = ready
            loadStartedAt = android.os.SystemClock.elapsedRealtime()
        } catch (error: Exception) { clear(); throw error }
    }

    private fun renderFrame(time: Long) {
        resources.asyncUpdateLoad()
        asset?.let { model ->
            updateTransform(time)
            val animator = model.instance.animator
            if (animator.animationCount > 0 && onReady == null) {
                val delta = if (animationFrameAt == 0L) 0f else ((time - animationFrameAt) / 1e9).toFloat().coerceIn(0f, 0.1f)
                animationFrameAt = time
                if (animationPlaying) animationSeconds += delta
                val duration = animator.getAnimationDuration(animationIndex)
                animator.applyAnimation(animationIndex, if (duration > 0f) animationSeconds % duration else 0f)
                animator.updateBoneMatrices()
            } else animator.updateBoneMatrices()
            var count = model.popRenderables(readyEntities)
            while (count > 0) {
                scene.addEntities(readyEntities.copyOf(count))
                count = model.popRenderables(readyEntities)
            }
            if (onReady != null && resources.asyncGetLoadProgress() >= 1f) {
                val callback = onReady
                onReady = null
                Log.i("DinoAR.Renderer", "GLB cargado: ${model.entities.size} entidades")
                callback?.invoke()
            } else if (onReady != null && android.os.SystemClock.elapsedRealtime() - loadStartedAt > 15_000) {
                clear()
                onError(IllegalStateException("Tiempo agotado al cargar GLB"))
            }
        }
        val chain = swapChain ?: return
        if (renderer.beginFrame(chain, time)) { renderer.render(view); renderer.endFrame() }
    }

    private fun updateTransform(time: Long) {
        if (placementEntity == 0 || texture.width == 0 || texture.height == 0) return
        val target = targetPlacement
        val matrix = FloatArray(16)
        Matrix.setIdentityM(matrix, 0)
        if (target != null) {
            val previous = smoothPlacement ?: target
            val dt = if (transformFrameAt == 0L) 0.033f else ((time - transformFrameAt) / 1e9).toFloat().coerceIn(0f, 0.1f)
            val weight = 1f - kotlin.math.exp(-dt / 0.09f)
            val current = CardPlacement(
                previous.x + (target.x - previous.x) * weight,
                previous.y + (target.y - previous.y) * weight,
                previous.width + (target.width - previous.width) * weight,
                previous.roll + CardTracking.angleDelta(previous.roll, target.roll) * weight)
            smoothPlacement = current
            val worldWidth = (10.0 * kotlin.math.tan(Math.toRadians(22.5))).toFloat()
            val unitsPerPixel = worldWidth / texture.width
            Matrix.translateM(matrix, 0, (current.x - texture.width / 2f) * unitsPerPixel,
                (texture.height / 2f - current.y) * unitsPerPixel, 0f)
            Matrix.rotateM(matrix, 0, current.roll, 0f, 0f, 1f)
            val size = current.width * unitsPerPixel * 1.6f * userScale * presentation.scaleMultiplier
            Matrix.scaleM(matrix, 0, size, size, size)
        } else {
            // Preview/test host without a physical card: larger than the original 1.8-unit model.
            Matrix.translateM(matrix, 0, 0f, -0.8f, 0f)
            val size = 2.4f * userScale * presentation.scaleMultiplier
            Matrix.scaleM(matrix, 0, size, size, size)
        }
        transformFrameAt = time
        Matrix.rotateM(matrix, 0, yaw + presentation.yawDegrees, 0f, 1f, 0f)
        Matrix.rotateM(matrix, 0, pitch + presentation.pitchDegrees, 1f, 0f, 0f)
        val composed = FloatArray(16)
        Matrix.multiplyMM(composed, 0, matrix, 0, normalization, 0)
        engine.transformManager.setTransform(engine.transformManager.getInstance(placementEntity), composed)
    }

    fun clear() {
        if (closed) return
        onReady = null
        resources.asyncCancelLoad()
        resources.evictResourceData()
        asset?.let { scene.removeEntities(it.entities); loader.destroyAsset(it) }
        asset = null
        if (placementEntity != 0) {
            engine.destroyEntity(placementEntity)
            entities.destroy(placementEntity)
            placementEntity = 0
        }
        targetPlacement = null
        smoothPlacement = null
        transformFrameAt = 0L
        animationFrameAt = 0L
    }
    fun resume() {
        if (running || closed) return
        running = true
        lastRender = 0L
        choreographer.postFrameCallback(frameCallback)
    }
    fun pause() { animationFrameAt = 0L; transformFrameAt = 0L; running = false; choreographer.removeFrameCallback(frameCallback) }

    override fun close() {
        if (closed) return
        pause()
        clear()
        helper.detach() // The surface must be detached before destroying the engine.
        resources.destroy()
        loader.destroy()
        materials.destroyMaterials()
        materials.destroy()
        lights.forEach { scene.removeEntity(it); engine.destroyEntity(it); entities.destroy(it) }
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyCameraComponent(cameraEntity)
        entities.destroy(cameraEntity)
        engine.destroyRenderer(renderer)
        engine.destroy()
        closed = true
    }
}
