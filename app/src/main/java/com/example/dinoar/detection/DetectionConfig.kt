package com.example.dinoar.detection

/** Tune against real printed cards; these are conservative starting values, not guarantees. */
data class DetectionConfig(
    val maxFeatures: Int = 1500,
    val ratioThreshold: Float = 0.75f,
    val maxHammingDistance: Float = 65f,
    val minMatches: Int = 18,
    val minInliers: Int = 14,
    val minInlierRatio: Double = 0.5,
    val ransacPixels: Double = 4.0,
    val minReferenceCoverage: Double = 0.12,
    val minFrameArea: Double = 0.025,
    val maxFrameArea: Double = 1.15,
    val confirmationFrames: Int = 3,
    val candidateGapMs: Long = 650,
    val holdMs: Long = 900,
    val frameIntervalMs: Long = 120
)
