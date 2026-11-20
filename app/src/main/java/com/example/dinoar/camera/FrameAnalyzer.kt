package com.example.dinoar.camera

import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.dinoar.detection.*
import com.example.dinoar.utils.ImageUtils

class FrameAnalyzer(
    private val detector: MarkerDetector,
    private val config: DetectionConfig,
    private val onState: (RecognitionState) -> Unit,
    private val onError: (Throwable) -> Unit,
    private val onDetection: (DetectionResult) -> Unit = {}
) : ImageAnalysis.Analyzer {
    private val stabilizer = RecognitionStabilizer(config)
    private var lastFrameAt = -config.frameIntervalMs
    private var previous: RecognitionState? = null
    private var consecutiveErrors = 0
    private var failed = false

    override fun analyze(image: ImageProxy) {
        try {
            val now = SystemClock.elapsedRealtime()
            if (failed || now - lastFrameAt < config.frameIntervalMs) return
            lastFrameAt = now
            val gray = ImageUtils.grayUpright(image)
            val result = try { detector.detect(gray) } finally { gray.release() }
            consecutiveErrors = 0
            val state = stabilizer.update(result?.dinosaur, now)
            if (state != previous) {
                previous = state
                Log.i("DinoAR.Detector", "${state.status} ${state.dinosaur}; inliers=${result?.inliers ?: 0}")
                onState(state)
            }
            if (result != null && result.dinosaur == state.dinosaur) onDetection(result)
        } catch (error: Exception) {
            Log.e("DinoAR.Detector", "Error al analizar fotograma", error)
            if (++consecutiveErrors >= 3) { failed = true; onError(error) }
        } finally {
            // Also executes for skipped frames, conversion errors and early returns.
            image.close()
        }
    }
}
