package com.example.dinoar.detection

import com.example.dinoar.model.DinosaurId

/** Pure Kotlin, independent of OpenCV. Use a monotonic clock (elapsedRealtime). */
class RecognitionStabilizer(private val config: DetectionConfig = DetectionConfig()) {
    private var active: DinosaurId? = null
    private var candidate: DinosaurId? = null
    private var hits = 0
    private var lastCandidateAt = 0L
    private var lastSeenAt = 0L
    private var hasDetected = false

    fun update(detected: DinosaurId?, nowMs: Long): RecognitionState {
        if (detected == null) {
            candidate = null
            hits = 0
        } else if (detected == active) {
            lastSeenAt = nowMs
            candidate = null
            hits = 0
        } else {
            hits = if (candidate == detected && nowMs - lastCandidateAt <= config.candidateGapMs) hits + 1 else 1
            candidate = detected
            lastCandidateAt = nowMs
            if (hits >= config.confirmationFrames) {
                active = detected
                lastSeenAt = nowMs
                hasDetected = true
                candidate = null
                hits = 0
            }
        }
        if (active != null && nowMs - lastSeenAt > config.holdMs) active = null
        return RecognitionState(active, when {
            active != null -> RecognitionStatus.DETECTED
            hasDetected -> RecognitionStatus.LOST
            else -> RecognitionStatus.SEARCHING
        })
    }
}
