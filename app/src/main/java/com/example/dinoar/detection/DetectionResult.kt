package com.example.dinoar.detection

import com.example.dinoar.model.DinosaurId

data class Point2(val x: Double, val y: Double)
data class DetectionResult(
    val dinosaur: DinosaurId,
    val corners: List<Point2>,
    val inliers: Int,
    val matches: Int,
    val frameWidth: Int,
    val frameHeight: Int
)
enum class RecognitionStatus { SEARCHING, DETECTED, LOST }
data class RecognitionState(val dinosaur: DinosaurId?, val status: RecognitionStatus)
