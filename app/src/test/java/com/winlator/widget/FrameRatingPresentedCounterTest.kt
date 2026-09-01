package com.winlator.widget

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FrameRatingPresentedCounterTest {
    @Test
    fun calculatesPresentedFramesPerSecondFromMonotonicSamples() {
        assertEquals(60f, FrameRating.calculatePresentedFps(10L, 1_000L, 40L, 1_500L), 0.01f)
    }

    @Test
    fun rejectsCounterResetsAndNonMonotonicTime() {
        assertEquals(0f, FrameRating.calculatePresentedFps(40L, 1_500L, 4L, 2_000L), 0.01f)
        assertEquals(0f, FrameRating.calculatePresentedFps(10L, 2_000L, 20L, 2_000L), 0.01f)
    }

    @Test
    fun fallsBackToLegacyReadingWhilePresentedCounterHasNoPositiveRate() {
        assertEquals(30f, FrameRating.preferPresentedFps(0f, 30f), 0.01f)
        assertEquals(30f, FrameRating.preferPresentedFps(-1f, 30f), 0.01f)
        assertEquals(28f, FrameRating.preferPresentedFps(28f, 30f), 0.01f)
    }

    @Test
    fun liveRatingUsesPresentedCounterAfterTheFirstSample() {
        val rating = FrameRating(ApplicationProvider.getApplicationContext())

        rating.updatePresentedFrames(0L, 1_000L)
        assertEquals(0f, rating.currentFPS, 0.01f)

        rating.updatePresentedFrames(60L, 2_000L)
        assertEquals(60f, rating.currentFPS, 0.01f)
        assertEquals("NATIVE", rating.currentSourceName)
    }

    @Test
    fun liveRatingExposesPresentedRateWithoutLsfgMultiplierArithmetic() {
        val rating = FrameRating(ApplicationProvider.getApplicationContext())

        rating.updatePresentedFrames(10L, 1_000L)
        rating.updatePresentedFrames(58L, 1_800L)

        assertEquals(60f, rating.presentedFPS, 0.01f)
    }

    @Test
    fun liveRatingRebaselinesAfterRendererCounterReset() {
        val rating = FrameRating(ApplicationProvider.getApplicationContext())

        rating.updatePresentedFrames(60L, 1_000L)
        rating.updatePresentedFrames(120L, 2_000L)
        assertEquals(60f, rating.currentFPS, 0.01f)

        rating.updatePresentedFrames(0L, 3_000L)
        assertEquals(0f, rating.currentFPS, 0.01f)

        rating.updatePresentedFrames(30L, 4_000L)
        assertEquals(30f, rating.currentFPS, 0.01f)
    }
}
