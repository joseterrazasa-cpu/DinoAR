package com.example.dinoar.rendering

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.dinoar.model.Dinosaur
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

data class ModelData(val buffer: ByteBuffer, val isDemo: Boolean)

/** Disk reads off main; generation prevents a slow old load replacing the next card. */
class ModelManager(context: Context) : AutoCloseable {
    private val assets = context.applicationContext.assets
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var generation = 0
    private var closed = false

    fun load(dinosaur: Dinosaur, onLoaded: (ModelData) -> Unit, onError: (Throwable) -> Unit) {
        check(!closed)
        val token = ++generation
        executor.execute {
            try {
                val expected = dinosaur.modelPath.substringAfterLast('/')
                val exists = assets.list("models")?.contains(expected) == true
                val path = if (exists) dinosaur.modelPath else dinosaur.fallbackModelPath
                val bytes = assets.open(path).use { stream ->
                    val output = ByteArrayOutputStream()
                    val chunk = ByteArray(8192)
                    var count = stream.read(chunk)
                    while (count != -1) {
                        require(output.size() + count <= MAX_BYTES) { "GLB supera 36 MiB: $path" }
                        output.write(chunk, 0, count)
                        count = stream.read(chunk)
                    }
                    output.toByteArray()
                }
                val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.LITTLE_ENDIAN)
                buffer.put(bytes).rewind()
                require(bytes.size >= 20 && buffer.getInt(0) == 0x46546C67 &&
                    buffer.getInt(4) == 2 && buffer.getInt(8) == bytes.size) { "GLB inválido: $path" }
                main.post { if (!closed && token == generation) onLoaded(ModelData(buffer, !exists)) }
            } catch (error: Exception) {
                main.post { if (!closed && token == generation) onError(error) }
            }
        }
    }

    fun cancel() { generation++ }
    override fun close() { closed = true; cancel(); executor.shutdownNow() }
    companion object { private const val MAX_BYTES = 36 * 1024 * 1024 }
}
