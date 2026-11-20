package com.example.dinoar.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.Camera
import androidx.camera.core.CameraState
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor

/** Owns only the camera use cases created by this screen. All calls are on main. */
class CameraManager(private val context: Context, private val owner: LifecycleOwner) {
    private var provider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var analysis: ImageAnalysis? = null
    private var generation = 0
    private var camera: Camera? = null

    @SuppressLint("MissingPermission") // Permission is checked by CameraActivity before every start.
    fun start(view: PreviewView, executor: Executor, analyzer: ImageAnalysis.Analyzer, onError: (Throwable) -> Unit) {
        stop()
        val request = generation
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (request != generation) return@addListener
            try {
                val cameraProvider = future.get()
                provider = cameraProvider
                val rotation = view.display?.rotation ?: android.view.Surface.ROTATION_0
                val cameraPreview = Preview.Builder().setTargetRotation(rotation).build()
                val cameraAnalysis = ImageAnalysis.Builder()
                    .setTargetRotation(rotation)
                    .setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(
                        ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                    ).build())
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                preview = cameraPreview
                analysis = cameraAnalysis
                cameraPreview.setSurfaceProvider(view.surfaceProvider)
                cameraAnalysis.setAnalyzer(executor, analyzer)
                // A shared viewport gives analysis and preview the same sensor crop.
                val viewport = requireNotNull(view.viewPort) { "La vista de cámara aún no tiene dimensiones" }
                val group = UseCaseGroup.Builder().addUseCase(cameraPreview).addUseCase(cameraAnalysis)
                    .setViewPort(viewport).build()
                val boundCamera = cameraProvider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, group)
                camera = boundCamera
                boundCamera.cameraInfo.cameraState.observe(owner) { state ->
                    if (request == generation && state.error != null) {
                        onError(IllegalStateException("CameraX error ${state.error?.code}"))
                    }
                    if (state.type == CameraState.Type.OPEN) Log.i(TAG, "CameraX: cámara abierta")
                }
                Log.i(TAG, "Preview + ImageAnalysis vinculados; KEEP_ONLY_LATEST")
            } catch (error: Exception) {
                Log.e(TAG, "No se pudo iniciar CameraX", error)
                onError(error)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stop() {
        generation++
        camera?.cameraInfo?.cameraState?.removeObservers(owner)
        camera = null
        analysis?.clearAnalyzer()
        val cases = listOfNotNull(preview, analysis)
        if (cases.isNotEmpty()) provider?.unbind(*cases.toTypedArray())
        preview = null
        analysis = null
    }
    companion object { private const val TAG = "DinoAR.Camera" }
}
