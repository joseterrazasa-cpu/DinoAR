package com.example.dinoar

import android.Manifest
import android.os.ParcelFileDescriptor
import android.widget.TextView
import androidx.camera.view.PreviewView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.dinoar.camera.CameraActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CameraInstrumentedTest {
    @Test fun opensCameraAndDetectorWithSharedViewportAndRecreates() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.executeShellCommand("pm grant ${context.packageName} ${Manifest.permission.CAMERA}").use {
            ParcelFileDescriptor.AutoCloseInputStream(it).use { stream -> stream.readBytes() }
        }
        ActivityScenario.launch(CameraActivity::class.java).use { scenario ->
            fun awaitCamera() {
                val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(25)
                var streaming = false
                var status = ""
                while (!streaming && System.nanoTime() < deadline) {
                    scenario.onActivity {
                        val preview = it.findViewById<PreviewView>(R.id.preview)
                        status = it.findViewById<TextView>(R.id.status).text.toString()
                        streaming = preview.previewStreamState.value == PreviewView.StreamState.STREAMING &&
                            status != it.getString(R.string.initializing)
                        assertNotEquals(it.getString(R.string.camera_error), status)
                        assertNotEquals(it.getString(R.string.detector_error), status)
                    }
                    if (!streaming) Thread.sleep(100)
                }
                assertTrue("La cámara no inició: $status", streaming)
            }
            awaitCamera()
            scenario.recreate()
            awaitCamera()
        }
    }
}
