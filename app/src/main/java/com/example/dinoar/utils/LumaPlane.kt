package com.example.dinoar.utils

import java.nio.ByteBuffer

/** Y in YUV_420_888 already is luminance. Handles padding, crop and pixel stride. */
object LumaPlane {
    fun copy(buffer: ByteBuffer, rowStride: Int, pixelStride: Int,
             left: Int, top: Int, width: Int, height: Int): ByteArray {
        val input = buffer.duplicate()
        val base = input.position()
        val output = ByteArray(width * height)
        for (row in 0 until height) {
            val start = base + (top + row) * rowStride + left * pixelStride
            if (pixelStride == 1) {
                input.position(start)
                input.get(output, row * width, width)
            } else {
                for (col in 0 until width) output[row * width + col] = input.get(start + col * pixelStride)
            }
        }
        return output
    }
}
