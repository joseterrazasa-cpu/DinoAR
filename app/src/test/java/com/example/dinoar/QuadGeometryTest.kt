package com.example.dinoar

import com.example.dinoar.detection.*
import org.junit.Assert.*
import org.junit.Test

class QuadGeometryTest {
    private val quad = listOf(Point2(100.0, 80.0), Point2(450.0, 100.0), Point2(420.0, 420.0), Point2(80.0, 400.0))
    private fun valid(points: List<Point2>) = QuadGeometry.isReasonable(points, 640, 480, DetectionConfig())
    @Test fun acceptsAPerspectiveCard() { assertTrue(valid(quad)) }
    @Test fun rejectsCrossedCorners() { assertFalse(valid(listOf(quad[0], quad[2], quad[1], quad[3]))) }
    @Test fun rejectsReflectedCard() { assertFalse(valid(quad.reversed())) }
    @Test fun rejectsTinyCard() { assertFalse(valid(quad.map { Point2(it.x / 20, it.y / 20) })) }
    @Test fun rejectsOffscreenCard() { assertFalse(valid(quad.map { Point2(it.x + 2000, it.y) })) }
    @Test fun rejectsNonFiniteCoordinates() { assertFalse(valid(quad.toMutableList().apply { this[0] = Point2(Double.NaN, 0.0) })) }
    @Test fun rejectsDegenerateCard() { assertFalse(valid(List(4) { Point2(it * 50.0, it * 50.0) })) }
}
