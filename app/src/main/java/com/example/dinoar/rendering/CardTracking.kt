package com.example.dinoar.rendering

import com.example.dinoar.detection.DetectionResult
import kotlin.math.*

/** 2D image registration, deliberately not a 6-DoF pose or a metric distance. */
data class CardPlacement(val x: Float, val y: Float, val width: Float, val roll: Float)

object CardTracking {
    fun map(result: DetectionResult, viewWidth: Int, viewHeight: Int): CardPlacement? {
        if (viewWidth <= 0 || viewHeight <= 0 || result.frameWidth <= 0 || result.frameHeight <= 0 || result.corners.size != 4) return null
        val scale = max(viewWidth.toDouble() / result.frameWidth, viewHeight.toDouble() / result.frameHeight)
        val dx = (viewWidth - result.frameWidth * scale) / 2
        val dy = (viewHeight - result.frameHeight * scale) / 2
        val p = result.corners
        val cx = p.sumOf { it.x } / 4 * scale + dx
        val cy = p.sumOf { it.y } / 4 * scale + dy
        // Average opposite edges: size follows perspective without inventing depth/tilt.
        val width = (hypot(p[1].x-p[0].x,p[1].y-p[0].y) + hypot(p[2].x-p[3].x,p[2].y-p[3].y)) / 2 * scale
        val angle = atan2((p[1].y-p[0].y)+(p[2].y-p[3].y), (p[1].x-p[0].x)+(p[2].x-p[3].x))
        return CardPlacement(cx.toFloat(), cy.toFloat(), width.toFloat(), (-angle * 180 / PI).toFloat())
    }

    fun angleDelta(from: Float, to: Float): Float = (((to - from) % 360f + 540f) % 360f) - 180f
}
