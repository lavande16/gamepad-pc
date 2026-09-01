package com.winlator.widget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.winlator.xenvironment.ImageFs;

import app.gamenative.R;
import timber.log.Timber;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrameRating extends FrameLayout implements Runnable {
    private long lastTime = 0;
    private int frameCount = 0;
    private float lastFPS = 0;
    private final TextView textView;

    // FPS reading tracking
    private static final int READING_INTERVAL_MS = 1000; // Take reading every 1 second
    private int readingCount = 0;
    private long sessionStartTime = 0;
    private int maxFPS = 0;
    private int minFPS = Integer.MAX_VALUE;
    private long lastReadingTime = 0;
    private long fpsSum = 0; // Sum of all FPS readings for average calculation
    private final FrameRateSampler sampler = new FrameRateSampler();
    private FrameRateSampler.Source loggedSource = FrameRateSampler.Source.NONE;

    public FrameRating(Context context) {
        this(context, null);
    }

    public FrameRating(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameRating(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = LayoutInflater.from(context).inflate(R.layout.frame_rating, this, false);
        textView = view.findViewById(R.id.TVFPS);
        addView(view);
    }

    public void update() {
        if (lastTime == 0) {
            lastTime = SystemClock.elapsedRealtime();
            sessionStartTime = SystemClock.elapsedRealtime();
        }
        long time = SystemClock.elapsedRealtime();
        if (time >= lastTime + 500) {
            lastFPS = ((float)(frameCount * 1000) / (time - lastTime));
            sampler.updateLegacy(lastFPS, time);

            // Take reading at specified interval
            if (lastReadingTime == 0 || time >= lastReadingTime + READING_INTERVAL_MS) {
                int currentFPS = Math.round(lastFPS);
                readingCount++;
                fpsSum += currentFPS;

                // Track max and min FPS (min must be > 1)
                if (currentFPS > maxFPS) {
                    maxFPS = currentFPS;
                }
                if (currentFPS > 1 && currentFPS < minFPS) {
                    minFPS = currentFPS;
                }

                lastReadingTime = time;
            }

            post(this);
            lastTime = time;
            frameCount = 0;
        }

        frameCount++;
    }

    public void reset() {
        lastTime = 0;
        frameCount = 0;
        lastFPS = 0;
        readingCount = 0;
        sessionStartTime = 0;
        maxFPS = 0;
        minFPS = Integer.MAX_VALUE;
        lastReadingTime = 0;
        fpsSum = 0;
        sampler.reset();
        loggedSource = FrameRateSampler.Source.NONE;
        post(() -> textView.setText(String.format(Locale.ENGLISH, "%.1f", 0f)));
    }

    /** Returns the most recent measured FPS value for the active session. */
    public float getCurrentFPS() {
        long now = Math.max(SystemClock.elapsedRealtime(), sampler.latestSampleTimeMs());
        FrameRateSampler.Source source = sampler.source(now);
        if (source != loggedSource) {
            Timber.d("FrameRating source changed: %s -> %s", loggedSource, source);
            loggedSource = source;
        }
        return sampler.currentFps(now);
    }

    /** Returns the post-renderer presentation rate without guest-window fallback. */
    public float getPresentedFPS() {
        long now = Math.max(SystemClock.elapsedRealtime(), sampler.latestSampleTimeMs());
        return sampler.presentedFps(now);
    }

    /** Stable source label used by GameFold's on-device FPS acceptance telemetry. */
    public String getCurrentSourceName() {
        long now = Math.max(SystemClock.elapsedRealtime(), sampler.latestSampleTimeMs());
        return sampler.source(now).name();
    }

    /**
     * A renderer may expose its presentation counter before the first positive-rate sample.
     * Keep the established window-update reading visible during that warm-up instead of
     * replacing it with a permanent zero.
     */
    public static float preferPresentedFps(float presented, float legacy) {
        return presented > 0f ? presented : legacy;
    }

    /** Updates the rating from frames that reached the presentation boundary. */
    public void updatePresentedFrames(long totalPresentedFrames, long sampleTimeMs) {
        updatePresentedFrames(totalPresentedFrames, sampleTimeMs, 0L);
    }

    public void updatePresentedFrames(long totalPresentedFrames, long sampleTimeMs, long rendererGeneration) {
        sampler.updatePresented(totalPresentedFrames, sampleTimeMs, rendererGeneration);
    }

    /** Pure helper kept public for deterministic renderer/HUD contract tests. */
    public static float calculatePresentedFps(
            long previousFrames, long previousTimeMs, long currentFrames, long currentTimeMs) {
        long frameDelta = currentFrames - previousFrames;
        long timeDelta = currentTimeMs - previousTimeMs;
        if (frameDelta < 0 || timeDelta <= 0) return 0;
        return (float) frameDelta * 1000f / timeDelta;
    }

    public float getAvgFPS() {
        if (readingCount == 0) return 0;
        return (float) fpsSum / readingCount;
    }

    public float getSessionLengthSec() {
        if (sessionStartTime == 0) return 0;
        return (SystemClock.elapsedRealtime() - sessionStartTime) / 1000.0f;
    }

    public void writeSessionSummary() {
        if (readingCount == 0) return;

        final long sessionLengthMs = sessionStartTime > 0 ?
            SystemClock.elapsedRealtime() - sessionStartTime : 0;
        final float sessionLengthSec = sessionLengthMs / 1000.0f;
        final int max = maxFPS;
        final int min = minFPS == Integer.MAX_VALUE ? 0 : minFPS;
        final float avgFPS = (float) fpsSum / readingCount;

        Context context = getContext();
        ImageFs imageFs = ImageFs.find(context);

        // Generate unique filename with timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
        String timestamp = dateFormat.format(new Date());
        File fpsLogFile = new File(imageFs.getTmpDir(), "fps_session" + ".json");
        ExecutorService fileWriteExecutor = Executors.newSingleThreadExecutor();

        fileWriteExecutor.execute(() -> {
            try {
                // Create file if it doesn't exist, or overwrite if it does
                if (!fpsLogFile.exists()) {
                    fpsLogFile.createNewFile();
                }

                // Write JSON format for easy parsing
                String json = String.format(Locale.ENGLISH,
                    "{\n" +
                    "  \"length_sec\": %.2f,\n" +
                    "  \"avg_fps\": %.1f,\n" +
                    "  \"max_fps\": %d,\n" +
                    "  \"min_fps\": %d,\n" +
                    "  \"readings\": %d\n" +
                    "}\n",
                    sessionLengthSec, avgFPS, max, min, readingCount);
                try (FileWriter fw = new FileWriter(fpsLogFile, false)) {
                    fw.write(json);
                    fw.flush();
                }
                Timber.d("Session summary written to: %s", fpsLogFile.getAbsolutePath());
            } catch (IOException e) {
                Timber.e(e, "Failed to write session summary");
            } finally {
                fileWriteExecutor.shutdown();
            }
        });
    }

    @Override
    public void run() {
        if (getVisibility() == GONE) setVisibility(View.VISIBLE);
        textView.setText(String.format(Locale.ENGLISH, "%.1f", getCurrentFPS()));
    }
}
