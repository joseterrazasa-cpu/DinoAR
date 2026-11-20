package com.example.dinoar

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.dinoar.detection.MarkerDetector
import com.example.dinoar.model.DinosaurId
import com.example.dinoar.model.DinosaurRepository
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/** Synthetic images on the Android OpenCV runtime; NOT evidence of printed-card accuracy. */
@RunWith(AndroidJUnit4::class)
class MarkerDetectorInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var detector: MarkerDetector

    @Before fun setUp() {
        assertTrue(OpenCVLoader.initLocal())
        detector = MarkerDetector(context)
    }
    @After fun tearDown() { detector.close() }

    private fun reference(id: DinosaurId): Mat {
        val bitmap = context.assets.open(DinosaurRepository.get(id).markerPath).use { BitmapFactory.decodeStream(it) }
        val rgba = Mat(); val gray = Mat()
        try { Utils.bitmapToMat(bitmap, rgba); Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY) }
        finally { bitmap.recycle(); rgba.release() }
        return gray
    }

    private fun checkPerspective(id: DinosaurId) {
        val ref = reference(id)
        val frame = Mat()
        val src = MatOfPoint2f(Point(0.0,0.0),Point(ref.cols()-1.0,0.0),
            Point(ref.cols()-1.0,ref.rows()-1.0),Point(0.0,ref.rows()-1.0))
        val dst = MatOfPoint2f(Point(100.0,100.0),Point(510.0,130.0),Point(530.0,710.0),Point(70.0,680.0))
        val h = Imgproc.getPerspectiveTransform(src, dst)
        try {
            Imgproc.warpPerspective(ref, frame, h, Size(640.0,800.0))
            val result = detector.detect(frame)
            assertNotNull("No reconoce $id con perspectiva sintética", result)
            assertEquals(id, result!!.dinosaur)
            assertTrue(result.inliers >= 14)
            result.corners.zip(dst.toArray()).forEach { (actual, expected) ->
                assertEquals(expected.x, actual.x, 18.0)
                assertEquals(expected.y, actual.y, 18.0)
            }
        } finally { ref.release(); frame.release(); src.release(); dst.release(); h.release() }
    }
    @Test fun trexPerspective() = checkPerspective(DinosaurId.TREX)
    @Test fun triceratopsPerspective() = checkPerspective(DinosaurId.TRICERATOPS)
    @Test fun velociraptorPerspective() = checkPerspective(DinosaurId.VELOCIRAPTOR)
    @Test fun pachycephalosaurusPerspective() = checkPerspective(DinosaurId.PACHYCEPHALOSAURUS)
    @Test fun mosasaurusPerspective() = checkPerspective(DinosaurId.MOSASAURUS)
    @Test fun dimorphodonPerspective() = checkPerspective(DinosaurId.DIMORPHODON)
    @Test fun spinosaurusPerspective() = checkPerspective(DinosaurId.SPINOSAURUS)
    @Test fun ankylosaurusPerspective() = checkPerspective(DinosaurId.ANKYLOSAURUS)
    @Test fun rejectsBlankFrame() {
        val blank = Mat(480,640,CvType.CV_8UC1,Scalar(190.0))
        try { assertNull(detector.detect(blank)) } finally { blank.release() }
    }
    @Test fun rejectsUnknownNoise() {
        val noise = Mat(480,640,CvType.CV_8UC1)
        try { Core.setRNGSeed(42); Core.randu(noise,0.0,255.0); assertNull(detector.detect(noise)) }
        finally { noise.release() }
    }
    @Test fun switchesReferencesWithoutReinitializing() {
        (DinosaurId.entries.toList() + DinosaurId.TREX).forEach { id ->
            val frame = reference(id)
            try { assertEquals(id, detector.detect(frame)?.dinosaur) } finally { frame.release() }
        }
    }
}
