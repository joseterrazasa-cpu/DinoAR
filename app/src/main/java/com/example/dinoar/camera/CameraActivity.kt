package com.example.dinoar.camera

import android.Manifest
import android.graphics.Rect
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.view.View
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.example.dinoar.R
import com.example.dinoar.databinding.ActivityCameraBinding
import com.example.dinoar.detection.*
import com.example.dinoar.model.DinosaurId
import com.example.dinoar.model.DinosaurRepository
import com.example.dinoar.rendering.DinosaurRenderer
import com.example.dinoar.rendering.ModelManager
import com.example.dinoar.rendering.CardTracking
import com.example.dinoar.ui.DinosaurInfoActivity
import com.example.dinoar.utils.applySafeInsets
import org.opencv.android.OpenCVLoader
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraManager: CameraManager
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var detector: MarkerDetector? = null // Access only on analysisExecutor.
    private var permissionRequested = false
    private var session = 0
    private var started = false
    private var selected: DinosaurId? = null
    private lateinit var modelManager: ModelManager
    private var dinosaurRenderer: DinosaurRenderer? = null
    private val config = DetectionConfig()
    private var lastDetection: DetectionResult? = null
    private var animationPlaying = true
    private var manipulating = false
    private var previousX = 0f
    private var previousY = 0f
    private lateinit var scaleGesture: ScaleGestureDetector
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && started) startCamera() else if (!granted) showPermissionError()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.controls.applySafeInsets()
        binding.modelView.isOpaque = false
        cameraManager = CameraManager(this, this)
        modelManager = ModelManager(this)
        try {
            dinosaurRenderer = DinosaurRenderer(binding.modelView, ::showModelError)
        } catch (error: Exception) { showModelError(error) }
        catch (error: LinkageError) { showModelError(error) }
        scaleGesture = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                dinosaurRenderer?.scaleBy(detector.scaleFactor)
                return true
            }
        })
        binding.resetModel.setOnClickListener { dinosaurRenderer?.resetInteraction() }
        binding.playAnimation.setOnClickListener {
            animationPlaying = !animationPlaying
            dinosaurRenderer?.setAnimationPlaying(animationPlaying)
            binding.playAnimation.setText(if (animationPlaying) R.string.pause_animation else R.string.play_animation)
        }
        binding.animationSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                dinosaurRenderer?.selectAnimation(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.back.setOnClickListener { finish() }
        binding.information.setOnClickListener {
            selected?.let {
                startActivity(Intent(this, DinosaurInfoActivity::class.java)
                    .putExtra(DinosaurInfoActivity.EXTRA_DINOSAUR, it.name))
            }
        }
        binding.retry.setOnClickListener {
            if (hasPermission()) startCamera()
            else startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
        }
    }

    override fun onStart() {
        super.onStart()
        started = true
        dinosaurRenderer?.resume()
        if (hasPermission()) startCamera()
        else if (!permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else showPermissionError()
    }

    private fun hasPermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    private fun startCamera() {
        val token = ++session
        cameraManager.stop()
        resetRecognition()
        binding.retry.isVisible = false
        binding.retry.setText(R.string.retry)
        binding.status.setText(R.string.initializing)
        analysisExecutor.execute {
            try {
                if (detector == null) {
                    check(OpenCVLoader.initLocal()) { "OpenCV no pudo cargar las bibliotecas nativas" }
                    Log.i("DinoAR.Detector", "OpenCV inicializado: ${org.opencv.core.Core.VERSION}")
                    detector = MarkerDetector(applicationContext, config)
                }
                val analyzer = FrameAnalyzer(detector!!, config, { state ->
                    runOnUiThread { if (started && token == session) displayState(state) }
                }, { error -> runOnUiThread { if (started && token == session) showError(R.string.detector_error, error) } },
                    { detection -> runOnUiThread {
                        if (started && token == session && selected == detection.dinosaur) {
                            lastDetection = detection
                            updateTracking()
                        }
                    } })
                runOnUiThread {
                    if (started && token == session) {
                        binding.status.setText(R.string.searching)
                        cameraManager.start(binding.preview, analysisExecutor, analyzer) { error ->
                            if (started && token == session) showError(R.string.camera_error, error)
                        }
                    }
                }
            } catch (error: Exception) {
                runOnUiThread { if (started && token == session) showError(R.string.detector_error, error) }
            } catch (error: LinkageError) {
                runOnUiThread { if (started && token == session) showError(R.string.detector_error, error) }
            }
        }
    }

    private fun displayState(state: RecognitionState) {
        val changed = selected != state.dinosaur
        selected = state.dinosaur
        val dinosaur = state.dinosaur?.let(DinosaurRepository::get)
        binding.information.isEnabled = dinosaur != null
        binding.resetModel.isEnabled = dinosaur != null
        binding.dinosaurName.text = dinosaur?.name ?: getString(R.string.point_card)
        binding.status.text = when (state.status) {
            RecognitionStatus.DETECTED -> getString(R.string.detected, dinosaur?.name.orEmpty())
            RecognitionStatus.LOST -> getString(R.string.lost)
            RecognitionStatus.SEARCHING -> getString(R.string.searching)
        }
        if (changed) {
            lastDetection = null
            hideAnimationControls()
            modelManager.cancel()
            dinosaurRenderer?.clear()
            if (dinosaur == null) binding.modelStatus.setText(R.string.fixed_overlay)
            else if (dinosaurRenderer == null) binding.modelStatus.setText(R.string.model_error)
            else {
                binding.modelStatus.setText(R.string.model_loading)
                modelManager.load(dinosaur, { data ->
                    try {
                        dinosaurRenderer?.load(data.buffer,
                            if (data.isDemo) null else dinosaur.framing,
                            if (data.isDemo) com.example.dinoar.model.ModelPresentation() else dinosaur.presentation) {
                            configureAnimations()
                            binding.modelStatus.text = if (data.isDemo)
                                getString(R.string.demo_model, dinosaur.modelPath.substringAfterLast('/'))
                            else getString(R.string.model_ready)
                        }
                        updateTracking()
                    } catch (error: Exception) { showModelError(error) }
                }, ::showModelError)
            }
        }
    }

    private fun updateTracking() {
        lastDetection?.let { detection ->
            CardTracking.map(detection, binding.modelView.width, binding.modelView.height)?.let {
                dinosaurRenderer?.track(it)
            }
        }
    }

    private fun hideAnimationControls() {
        binding.playAnimation.isVisible = false
        binding.animationSelector.isVisible = false
    }
    private fun configureAnimations() {
        val names = dinosaurRenderer?.animationNames.orEmpty()
        animationPlaying = true
        binding.playAnimation.setText(R.string.pause_animation)
        binding.playAnimation.isVisible = names.isNotEmpty()
        binding.animationSelector.isVisible = names.size > 1
        binding.animationSelector.adapter = ArrayAdapter(this, R.layout.item_animation, names)
        Log.i("DinoAR.Renderer", "Animaciones disponibles: $names")
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (::binding.isInitialized && ::scaleGesture.isInitialized) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                fun inside(view: View): Boolean {
                    val bounds = Rect()
                    return view.getGlobalVisibleRect(bounds) && bounds.contains(event.rawX.toInt(), event.rawY.toInt())
                }
                manipulating = selected != null && !inside(binding.topPanel) && !inside(binding.bottomPanel)
                previousX = event.x
                previousY = event.y
            }
            if (manipulating) {
                scaleGesture.onTouchEvent(event)
                if (event.actionMasked == MotionEvent.ACTION_MOVE && event.pointerCount == 1 && !scaleGesture.isInProgress) {
                    dinosaurRenderer?.rotateBy(event.x - previousX, event.y - previousY)
                }
                previousX = event.x
                previousY = event.y
                if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) manipulating = false
                return true
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun showModelError(error: Throwable) {
        Log.e("DinoAR.Renderer", "No se pudo mostrar el modelo", error)
        binding.modelStatus.setText(R.string.model_error)
    }

    private fun resetRecognition() {
        lastDetection = null
        manipulating = false
        hideAnimationControls()
        binding.resetModel.isEnabled = false
        modelManager.cancel()
        dinosaurRenderer?.clear()
        binding.modelStatus.setText(R.string.fixed_overlay)
        selected = null
        binding.information.isEnabled = false
        binding.dinosaurName.setText(R.string.point_card)
    }
    private fun showError(message: Int, error: Throwable) {
        Log.e("DinoAR.Camera", getString(message), error)
        ++session
        cameraManager.stop()
        resetRecognition()
        binding.status.setText(message)
        binding.retry.setText(R.string.retry)
        binding.retry.isVisible = true
    }
    private fun showPermissionError() {
        resetRecognition()
        binding.status.setText(R.string.camera_permission)
        binding.retry.setText(R.string.open_settings)
        binding.retry.isVisible = true
    }
    override fun onStop() {
        started = false
        ++session
        cameraManager.stop()
        resetRecognition()
        dinosaurRenderer?.pause()
        super.onStop()
    }
    override fun onDestroy() {
        modelManager.close()
        dinosaurRenderer?.close()
        // Queued after any in-flight analyze call; never free native Mats from the UI thread.
        analysisExecutor.execute { detector?.close(); detector = null }
        analysisExecutor.shutdown()
        super.onDestroy()
    }
}
