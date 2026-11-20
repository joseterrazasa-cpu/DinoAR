package com.example.dinoar

import com.example.dinoar.detection.*
import com.example.dinoar.model.DinosaurId
import com.example.dinoar.rendering.CardTracking
import org.junit.Assert.*
import org.junit.Test

class CardTrackingTest {
    private fun detection(points: List<Point2>) = DetectionResult(DinosaurId.TREX, points, 30, 40, 640, 480)
    private val card = listOf(Point2(220.0,140.0),Point2(420.0,140.0),Point2(420.0,340.0),Point2(220.0,340.0))
    @Test fun mapsCenterAndSizeWithCenterCrop() {
        val mapped = CardTracking.map(detection(card), 1080,1920)!!
        assertEquals(540f,mapped.x,0.01f)
        assertEquals(960f,mapped.y,0.01f)
        assertEquals(800f,mapped.width,0.01f)
        assertEquals(0f,mapped.roll,0.01f)
    }
    @Test fun followsTranslationAndDistance() {
        val a = CardTracking.map(detection(card),640,480)!!
        val b = CardTracking.map(detection(card.map { Point2(it.x+40,it.y+20) }),640,480)!!
        assertEquals(40f,b.x-a.x,0.01f)
        assertEquals(20f,b.y-a.y,0.01f)
        val c = CardTracking.map(detection(card.map { Point2((it.x-320)*2+320,(it.y-240)*2+240) }),640,480)!!
        assertEquals(a.width*2,c.width,0.01f)
    }
    @Test fun imageClockwiseRotationBecomesGlCounterclockwise() {
        val rotated = card.map { Point2(320-(it.y-240),240+(it.x-320)) }
        assertEquals(-90f,CardTracking.map(detection(rotated),640,480)!!.roll,0.01f)
    }
    @Test fun smoothsAcrossAngleWrap() {
        assertEquals(2f,CardTracking.angleDelta(179f,-179f),0.01f)
        assertEquals(-2f,CardTracking.angleDelta(-179f,179f),0.01f)
        assertEquals(2f,CardTracking.angleDelta(899f,-179f),0.01f)
    }
    @Test fun ignoresUnmeasuredView() { assertNull(CardTracking.map(detection(card),0,480)) }
}
