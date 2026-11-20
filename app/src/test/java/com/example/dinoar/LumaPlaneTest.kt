package com.example.dinoar

import com.example.dinoar.utils.LumaPlane
import java.nio.ByteBuffer
import org.junit.Assert.*
import org.junit.Test

class LumaPlaneTest {
    @Test fun skipsRowPadding() {
        val b = ByteBuffer.wrap(byteArrayOf(1,2,3,99,99,4,5,6))
        assertArrayEquals(byteArrayOf(1,2,3,4,5,6), LumaPlane.copy(b,5,1,0,0,3,2))
        assertEquals(0, b.position())
    }
    @Test fun handlesCropAndPixelStride() {
        val b = ByteBuffer.wrap(byteArrayOf(0,0,0,0,0,0, 10,99,11,99,12,99, 20,99,21,99,22))
        assertArrayEquals(byteArrayOf(11,12,21,22), LumaPlane.copy(b,6,2,1,1,2,2))
    }
    @Test fun honorsInitialBufferPosition() {
        val b = ByteBuffer.wrap(byteArrayOf(99,99,1,2,3,4)).apply { position(2) }
        assertArrayEquals(byteArrayOf(1,2,3,4), LumaPlane.copy(b,2,1,0,0,2,2))
        assertEquals(2, b.position())
    }
}
