package com.example.dinoar.detection

import kotlin.math.abs
import kotlin.math.hypot

object QuadGeometry {
    fun isReasonable(points: List<Point2>, width: Int, height: Int, config: DetectionConfig): Boolean {
        if (points.size != 4 || width <= 0 || height <= 0) return false
        if (points.any { !it.x.isFinite() || !it.y.isFinite() ||
                    it.x < -width * 0.15 || it.x > width * 1.15 ||
                    it.y < -height * 0.15 || it.y > height * 1.15 }) return false
        val cross = points.indices.map { i ->
            val a = points[i]; val b = points[(i + 1) % 4]; val c = points[(i + 2) % 4]
            (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x)
        }
        // Reference order is clockwise in image coordinates. Reject reflections and concavity.
        if (cross.any { it <= 1.0 }) return false
        val area = abs(points.indices.sumOf { i ->
            val a = points[i]; val b = points[(i + 1) % 4]
            a.x * b.y - a.y * b.x
        }) / 2.0
        val fraction = area / (width.toDouble() * height)
        if (fraction !in config.minFrameArea..config.maxFrameArea) return false
        val lengths = points.indices.map { i ->
            val a = points[i]; val b = points[(i + 1) % 4]
            hypot(b.x - a.x, b.y - a.y)
        }
        return lengths.min() >= 25.0 && lengths.max() / lengths.min() <= 7.0
    }
}
