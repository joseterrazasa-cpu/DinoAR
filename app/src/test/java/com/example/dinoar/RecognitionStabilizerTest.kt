package com.example.dinoar

import com.example.dinoar.detection.*
import com.example.dinoar.model.DinosaurId.*
import org.junit.Assert.*
import org.junit.Test

class RecognitionStabilizerTest {
    private fun confirmed(s: RecognitionStabilizer) {
        s.update(TREX, 0); s.update(TREX, 120); s.update(TREX, 240)
    }
    @Test fun requiresThreeConsecutiveFrames() {
        val s = RecognitionStabilizer()
        assertNull(s.update(TREX, 0).dinosaur)
        assertNull(s.update(TREX, 120).dinosaur)
        assertEquals(TREX, s.update(TREX, 240).dinosaur)
    }
    @Test fun anUnknownFrameBreaksConfirmation() {
        val s = RecognitionStabilizer()
        s.update(TREX, 0); s.update(null, 120)
        assertNull(s.update(TREX, 240).dinosaur)
        assertNull(s.update(TREX, 360).dinosaur)
    }
    @Test fun staleCandidateCannotConfirm() {
        val s = RecognitionStabilizer()
        s.update(TREX, 0); s.update(TREX, 120)
        assertNull(s.update(TREX, 1200).dinosaur)
    }
    @Test fun holdsBriefLossThenClearsTheModel() {
        val s = RecognitionStabilizer(); confirmed(s)
        assertEquals(TREX, s.update(null, 800).dinosaur)
        val state = s.update(null, 1200)
        assertNull(state.dinosaur)
        assertEquals(RecognitionStatus.LOST, state.status)
    }
    @Test fun switchesOnlyAfterConfirmation() {
        val s = RecognitionStabilizer(); confirmed(s)
        assertEquals(TREX, s.update(TRICERATOPS, 360).dinosaur)
        assertEquals(TREX, s.update(TRICERATOPS, 480).dinosaur)
        assertEquals(TRICERATOPS, s.update(TRICERATOPS, 600).dinosaur)
    }
    @Test fun unknownImagesNeverActivateADinosaur() {
        val s = RecognitionStabilizer()
        repeat(20) { assertEquals(RecognitionStatus.SEARCHING, s.update(null, it * 200L).status) }
    }
    @Test fun recoveryNeedsConfirmationAgain() {
        val s = RecognitionStabilizer(); confirmed(s)
        s.update(null, 2000)
        assertNull(s.update(TREX, 2120).dinosaur)
        s.update(TREX, 2240)
        assertEquals(TREX, s.update(TREX, 2360).dinosaur)
    }
    @Test fun alternatingCardsDoNotFlicker() {
        val s = RecognitionStabilizer()
        repeat(20) { assertNull(s.update(if (it % 2 == 0) TREX else TRICERATOPS, it * 120L).dinosaur) }
    }
}
