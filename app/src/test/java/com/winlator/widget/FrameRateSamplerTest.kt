package com.winlator.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class FrameRateSamplerTest {
    @Test
    fun presentationCounterIsSampledAtNsResponsiveCadence() {
        assertEquals(800L, FrameRateSampler.recommendedSampleIntervalMs())
    }

    @Test
    fun nativeCounterStall_fallsBackAfterTwoWindowsInsteadOfFreezingLastValue() {
        val sampler = FrameRateSampler()
        sampler.updatePresented(0L, 1_000L, 1L)
        sampler.updatePresented(60L, 2_000L, 1L)
        assertEquals(60f, sampler.currentFps(2_000L), 0.01f)

        sampler.updatePresented(60L, 3_000L, 1L)
        assertEquals(60f, sampler.currentFps(3_000L), 0.01f)

        sampler.updateLegacy(28f, 4_000L)
        sampler.updatePresented(60L, 4_000L, 1L)
        assertEquals(28f, sampler.currentFps(4_000L), 0.01f)
        assertEquals(FrameRateSampler.Source.LEGACY, sampler.source(4_000L))
    }

    @Test
    fun freshGuestWindowRate_winsOverFixedCompositorPresentationCadence() {
        val sampler = FrameRateSampler()
        sampler.updatePresented(0L, 1_000L, 1L)
        sampler.updatePresented(15L, 1_800L, 1L)
        sampler.updateLegacy(27f, 1_800L)

        assertEquals(FrameRateSampler.Source.LEGACY, sampler.source(1_800L))
        assertEquals(27f, sampler.currentFps(1_800L), 0.01f)
    }

    @Test
    fun presentedRate_remainsIndependentFromInflatedGuestWindowCallbacks() {
        val sampler = FrameRateSampler()
        sampler.updatePresented(0L, 1_000L, 1L)
        sampler.updatePresented(60L, 2_000L, 1L)
        sampler.updateLegacy(125f, 2_000L)

        assertEquals(125f, sampler.currentFps(2_000L), 0.01f)
        assertEquals(60f, sampler.presentedFps(2_000L), 0.01f)
    }

    @Test
    fun presentedRate_returnsZeroAfterTwoStalledSamplingWindows() {
        val sampler = FrameRateSampler()
        sampler.updatePresented(0L, 1_000L, 1L)
        sampler.updatePresented(60L, 2_000L, 1L)
        assertEquals(60f, sampler.presentedFps(2_000L), 0.01f)

        sampler.updatePresented(60L, 3_000L, 1L)
        assertEquals(60f, sampler.presentedFps(3_000L), 0.01f)

        sampler.updatePresented(60L, 4_000L, 1L)
        assertEquals(0f, sampler.presentedFps(4_000L), 0.01f)
    }

    @Test
    fun bothSourcesStale_returnsZero() {
        val sampler = FrameRateSampler()
        sampler.updatePresented(0L, 1_000L, 1L)
        sampler.updatePresented(60L, 2_000L, 1L)
        sampler.updatePresented(60L, 3_000L, 1L)
        sampler.updatePresented(60L, 4_000L, 1L)

        assertEquals(0f, sampler.currentFps(4_000L), 0.01f)
        assertEquals(FrameRateSampler.Source.NONE, sampler.source(4_000L))
    }

    @Test
    fun rendererGenerationChange_rebaselinesWithoutLeakingOldFps() {
        val sampler = FrameRateSampler()
        sampler.updatePresented(100L, 1_000L, 1L)
        sampler.updatePresented(160L, 2_000L, 1L)
        assertEquals(60f, sampler.currentFps(2_000L), 0.01f)

        sampler.updatePresented(5L, 3_000L, 2L)
        assertEquals(0f, sampler.currentFps(3_000L), 0.01f)
        sampler.updatePresented(35L, 3_500L, 2L)
        assertEquals(60f, sampler.currentFps(3_500L), 0.01f)
    }

    @Test
    fun sessionReset_clearsAllSourcesAndCounters() {
        val sampler = FrameRateSampler()
        sampler.updateLegacy(45f, 1_000L)
        sampler.updatePresented(0L, 1_000L, 1L)
        sampler.updatePresented(60L, 2_000L, 1L)

        sampler.reset()

        assertEquals(0f, sampler.currentFps(2_000L), 0.01f)
        assertEquals(FrameRateSampler.Source.NONE, sampler.source(2_000L))
    }
}
