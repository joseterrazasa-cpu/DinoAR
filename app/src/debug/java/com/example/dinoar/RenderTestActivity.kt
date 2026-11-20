package com.example.dinoar

import android.app.Activity
import android.os.Bundle
import android.view.TextureView
import com.example.dinoar.rendering.DinosaurRenderer

/** Instrumentation host only: absent from release builds and not exported. */
class RenderTestActivity : Activity() {
    lateinit var texture: TextureView
    lateinit var renderer: DinosaurRenderer
    var renderError: Throwable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        texture = TextureView(this)
        setContentView(texture)
        renderer = DinosaurRenderer(texture) { renderError = it }
    }
    override fun onResume() { super.onResume(); renderer.resume() }
    override fun onPause() { renderer.pause(); super.onPause() }
    override fun onDestroy() { renderer.close(); super.onDestroy() }
}
