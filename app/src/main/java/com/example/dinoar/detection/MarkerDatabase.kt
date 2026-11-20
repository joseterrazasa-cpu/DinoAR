package com.example.dinoar.detection

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.dinoar.model.Dinosaur
import com.example.dinoar.model.DinosaurRepository
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.ORB
import org.opencv.imgproc.Imgproc

class MarkerReference(val dinosaur: Dinosaur, val width: Int, val height: Int,
                      val points: Array<KeyPoint>, val descriptors: Mat)

class MarkerDatabase(context: Context, orb: ORB) : AutoCloseable {
    val references = mutableListOf<MarkerReference>()
    init {
        try {
            for (dinosaur in DinosaurRepository.dinosaurs) {
                val rgba = Mat(); val gray = Mat(); val keys = MatOfKeyPoint()
                val descriptors = Mat(); val mask = Mat()
                try {
                    val bitmap = context.assets.open(dinosaur.markerPath).use {
                        requireNotNull(BitmapFactory.decodeStream(it)) { "Imagen inválida: ${dinosaur.markerPath}" }
                    }
                    try { Utils.bitmapToMat(bitmap, rgba) } finally { bitmap.recycle() }
                    Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
                    orb.detectAndCompute(gray, mask, keys, descriptors)
                    check(keys.rows() >= 50 && !descriptors.empty()) { "Tarjeta sin suficientes características: ${dinosaur.id}" }
                    references += MarkerReference(dinosaur, gray.cols(), gray.rows(), keys.toArray(), descriptors)
                    Log.i("DinoAR.Detector", "${dinosaur.id}: ${keys.rows()} características precalculadas")
                } catch (error: Throwable) {
                    descriptors.release()
                    throw error
                } finally { rgba.release(); gray.release(); keys.release(); mask.release() }
            }
        } catch (error: Throwable) { close(); throw error }
    }
    override fun close() { references.forEach { it.descriptors.release() }; references.clear() }
}
