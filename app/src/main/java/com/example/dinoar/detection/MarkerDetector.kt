package com.example.dinoar.detection

import android.content.Context
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.features2d.BFMatcher
import org.opencv.features2d.ORB

/** Single-thread confined: construct, detect and close on the analysis executor. */
class MarkerDetector(context: Context, private val config: DetectionConfig = DetectionConfig()) : AutoCloseable {
    private val orb = ORB.create(config.maxFeatures)
    private val matcher = BFMatcher.create(Core.NORM_HAMMING, false)
    private val database = MarkerDatabase(context, orb)

    fun detect(gray: Mat): DetectionResult? {
        val keys = MatOfKeyPoint(); val descriptors = Mat(); val mask = Mat()
        try {
            orb.detectAndCompute(gray, mask, keys, descriptors)
            if (descriptors.rows() < 2) return null
            val points = keys.toArray()
            val results = database.references.mapNotNull { match(it, points, descriptors, gray.cols(), gray.rows()) }
                .sortedByDescending { it.inliers }
            // Two equally convincing cards: wait until one is dominant.
            if (results.size > 1 && results[0].inliers < results[1].inliers * 1.2) return null
            return results.firstOrNull()
        } finally { keys.release(); descriptors.release(); mask.release() }
    }

    private fun match(reference: MarkerReference, framePoints: Array<KeyPoint>, descriptors: Mat,
                      width: Int, height: Int): DetectionResult? {
        val pairs = mutableListOf<MatOfDMatch>()
        val source = MatOfPoint2f(); val destination = MatOfPoint2f(); val inlierMask = Mat()
        val referenceCorners = MatOfPoint2f(); val projectedCorners = MatOfPoint2f()
        var homography: Mat? = null
        try {
            matcher.knnMatch(reference.descriptors, descriptors, pairs, 2)
            val good = pairs.mapNotNull {
                val pair = it.toArray()
                if (pair.size == 2 && pair[0].distance < config.ratioThreshold * pair[1].distance &&
                    pair[0].distance <= config.maxHammingDistance) pair[0] else null
            }.sortedBy { it.distance }.distinctBy { it.trainIdx }
            if (good.size < config.minMatches) return null
            val src = good.map { reference.points[it.queryIdx].pt }
            val dst = good.map { framePoints[it.trainIdx].pt }
            source.fromList(src); destination.fromList(dst)
            homography = Calib3d.findHomography(source, destination, Calib3d.RANSAC, config.ransacPixels,
                inlierMask, 2000, 0.995)
            if (homography.empty() || inlierMask.empty()) return null
            val inliers = Core.countNonZero(inlierMask)
            if (inliers < config.minInliers || inliers.toDouble() / good.size < config.minInlierRatio) return null
            val supported = src.filterIndexed { index, _ -> inlierMask.get(index, 0)[0] != 0.0 }
            val coverage = ((supported.maxOf { it.x } - supported.minOf { it.x }) *
                (supported.maxOf { it.y } - supported.minOf { it.y })) / (reference.width.toDouble() * reference.height)
            if (coverage < config.minReferenceCoverage) return null
            referenceCorners.fromArray(Point(0.0, 0.0), Point(reference.width - 1.0, 0.0),
                Point(reference.width - 1.0, reference.height - 1.0), Point(0.0, reference.height - 1.0))
            Core.perspectiveTransform(referenceCorners, projectedCorners, homography)
            val corners = projectedCorners.toArray().map { Point2(it.x, it.y) }
            if (!QuadGeometry.isReasonable(corners, width, height, config)) return null
            return DetectionResult(reference.dinosaur.id, corners, inliers, good.size, width, height)
        } finally {
            pairs.forEach { it.release() }; source.release(); destination.release(); inlierMask.release()
            referenceCorners.release(); projectedCorners.release(); homography?.release()
        }
    }
    override fun close() { database.close(); matcher.clear(); orb.clear() }
}
