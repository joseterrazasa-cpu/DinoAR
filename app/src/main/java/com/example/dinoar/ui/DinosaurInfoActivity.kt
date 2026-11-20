package com.example.dinoar.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dinoar.databinding.ActivityDinosaurInfoBinding
import com.example.dinoar.model.DinosaurRepository
import com.example.dinoar.utils.applySafeInsets

class DinosaurInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dinosaur = DinosaurRepository.fromName(intent.getStringExtra(EXTRA_DINOSAUR))
        if (dinosaur == null) { finish(); return }
        val binding = ActivityDinosaurInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySafeInsets()
        binding.name.text = dinosaur.name
        binding.collection.text = dinosaur.group
        binding.scientificName.text = dinosaur.scientificName
        binding.features.text = dinosaur.features
        binding.source.text = dinosaur.sourceUrl
        binding.modelCredit.text = dinosaur.modelCredit
        binding.period.text = dinosaur.period
        binding.diet.text = dinosaur.diet
        binding.length.text = dinosaur.length
        binding.description.text = dinosaur.description
        binding.back.setOnClickListener { finish() }
    }
    companion object { const val EXTRA_DINOSAUR = "dinosaur_id" }
}
