package com.winlator.widget;

/**
 * Chooses a fresh FPS source without retaining a renderer value after its counter stops.
 * All timestamps use the caller's monotonic clock.
 */
public final class FrameRateSampler {
    public enum Source { NATIVE, LEGACY, NONE }

    private static final long RECOMMENDED_SAMPLE_INTERVAL_MS = 800L;
    private static final int NATIVE_STALE_WINDOWS_BEFORE_FALLBACK = 2;
    private static final long LEGACY_FRESHNESS_MS = 2500L;

    private long rendererGeneration = Long.MIN_VALUE;
    private long nativeFrameCount = 0L;
    private long nativeSampleTimeMs = 0L;
    private float nativeFps = 0f;
    private int nativeStaleWindows = 0;
    private float legacyFps = 0f;
    private long legacySampleTimeMs = 0L;

    /** Matches the native GameFold NS performance-stat refresh cadence. */
    public static long recommendedSampleIntervalMs() {
        return RECOMMENDED_SAMPLE_INTERVAL_MS;
    }

    public synchronized void updatePresented(
            long totalPresentedFrames,
            long sampleTimeMs,
            long generation) {
        if (totalPresentedFrames < 0L || sampleTimeMs <= 0L) return;
        if (generation != rendererGeneration || nativeSampleTimeMs == 0L ||
                totalPresentedFrames < nativeFrameCount || sampleTimeMs <= nativeSampleTimeMs) {
            rendererGeneration = generation;
            nativeFrameCount = totalPresentedFrames;
            nativeSampleTimeMs = sampleTimeMs;
            nativeFps = 0f;
            nativeStaleWindows = 0;
            return;
        }

        long frameDelta = totalPresentedFrames - nativeFrameCount;
        long timeDelta = sampleTimeMs - nativeSampleTimeMs;
        if (frameDelta > 0L) {
            nativeFps = (float) frameDelta * 1000f / (float) timeDelta;
            nativeStaleWindows = 0;
        } else {
            nativeStaleWindows++;
        }
        nativeFrameCount = totalPresentedFrames;
        nativeSampleTimeMs = sampleTimeMs;
    }

    public synchronized void updateLegacy(float fps, long sampleTimeMs) {
        if (sampleTimeMs <= 0L) return;
        legacyFps = Math.max(0f, fps);
        legacySampleTimeMs = sampleTimeMs;
    }

    public synchronized float currentFps(long nowMs) {
        Source source = source(nowMs);
        if (source == Source.NATIVE) return nativeFps;
        if (source == Source.LEGACY) return legacyFps;
        return 0f;
    }

    /** Returns only the renderer presentation rate, never the guest window-update fallback. */
    public synchronized float presentedFps(long nowMs) {
        if (nowMs < nativeSampleTimeMs || nativeFps <= 0f ||
                nativeStaleWindows >= NATIVE_STALE_WINDOWS_BEFORE_FALLBACK) {
            return 0f;
        }
        return nativeFps;
    }

    public synchronized long latestSampleTimeMs() {
        return Math.max(nativeSampleTimeMs, legacySampleTimeMs);
    }

    public synchronized Source source(long nowMs) {
        // Window-content updates are the PC equivalent of NS's guest performance stats: they
        // move only when the game submits new content. The compositor presentation counter can
        // keep advancing at a fixed cadence while showing the same guest frame, so it is a
        // fallback rather than the primary source.
        if (legacyFps > 0f && legacySampleTimeMs > 0L &&
                nowMs >= legacySampleTimeMs && nowMs - legacySampleTimeMs <= LEGACY_FRESHNESS_MS) {
            return Source.LEGACY;
        }
        if (nativeFps > 0f && nativeStaleWindows < NATIVE_STALE_WINDOWS_BEFORE_FALLBACK) {
            return Source.NATIVE;
        }
        return Source.NONE;
    }

    public synchronized void reset() {
        rendererGeneration = Long.MIN_VALUE;
        nativeFrameCount = 0L;
        nativeSampleTimeMs = 0L;
        nativeFps = 0f;
        nativeStaleWindows = 0;
        legacyFps = 0f;
        legacySampleTimeMs = 0L;
    }
}
