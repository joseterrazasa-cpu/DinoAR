package com.example.dinoar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dinoar.camera.CameraActivity
import com.example.dinoar.databinding.ActivityMainBinding
import com.example.dinoar.utils.applySafeInsets
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySafeInsets()
        binding.start.setOnClickListener { startActivity(Intent(this, CameraActivity::class.java)) }
        binding.help.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.how_it_works)
                .setMessage(R.string.help_text)
                .setPositiveButton(R.string.understood, null).show()
        }
    }
}
