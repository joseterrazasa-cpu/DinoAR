package com.example.dinoar.utils

import androidx.camera.core.ImageProxy
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object ImageUtils {
    fun grayUpright(image: ImageProxy, maxDimension: Int = 800): Mat {
        val crop = image.cropRect
        val plane = image.planes[0]
        val bytes = LumaPlane.copy(plane.buffer, plane.rowStride, plane.pixelStride,
            crop.left, crop.top, crop.width(), crop.height())
        val raw = Mat(crop.height(), crop.width(), CvType.CV_8UC1)
        val rotated = Mat()
        try {
            raw.put(0, 0, bytes)
            when (image.imageInfo.rotationDegrees) {
                0 -> raw.copyTo(rotated)
                90 -> Core.rotate(raw, rotated, Core.ROTATE_90_CLOCKWISE)
                180 -> Core.rotate(raw, rotated, Core.ROTATE_180)
                270 -> Core.rotate(raw, rotated, Core.ROTATE_90_COUNTERCLOCKWISE)
                else -> error("Rotación no soportada")
            }
            val largest = maxOf(rotated.cols(), rotated.rows())
            if (largest > maxDimension) {
                val scale = maxDimension.toDouble() / largest
                Imgproc.resize(rotated, rotated, Size(), scale, scale, Imgproc.INTER_AREA)
            }
            return rotated
        } catch (error: Throwable) { rotated.release(); throw error }
        finally { raw.release() }
    }
}
