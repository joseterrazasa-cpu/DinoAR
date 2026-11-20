package com.example.dinoar.model

enum class DinosaurId {
    TREX, TRICERATOPS, VELOCIRAPTOR, PACHYCEPHALOSAURUS,
    MOSASAURUS, DIMORPHODON, SPINOSAURUS, ANKYLOSAURUS
}

data class ModelPresentation(val yawDegrees: Float = -25f, val pitchDegrees: Float = 0f,
                             val scaleMultiplier: Float = 1f)

/** Bind-pose bounds when an exporter supplies mesh bounds before skin deformation. */
data class ModelFraming(val centerX: Float, val centerY: Float, val centerZ: Float,
                        val halfX: Float, val halfY: Float, val halfZ: Float)

data class Dinosaur(
    val id: DinosaurId,
    val name: String,
    val period: String,
    val diet: String,
    val length: String,
    val description: String,
    val markerPath: String,
    val modelPath: String,
    val framing: ModelFraming? = null,
    val scientificName: String = name,
    val group: String = "Dinosaurio",
    val features: String = description,
    val sourceUrl: String = "",
    val modelCredit: String = "",
    val expectedAnimations: Int? = null,
    val presentation: ModelPresentation = ModelPresentation(),
    val fallbackModelPath: String = "models/demo_cube.glb"
)
