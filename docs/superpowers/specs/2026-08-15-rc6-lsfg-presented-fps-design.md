# GameFold PC V0.8.2 RC6 LSFG Presented FPS Design

## Problem

The RC5 HUD samples `FrameRating.currentFPS`, which prefers fresh XServer window-update callbacks, and then multiplies that value by the configured LSFG multiplier. In the captured vivo session the renderer presented about 60 frames per second, while window updates reached about 125 per second and the HUD multiplied that by two to show about 250 FPS.

## Considered approaches

1. **Use native presented FPS while LSFG is active (selected).** This measures frames after LSFG at the renderer presentation boundary and must not be multiplied again.
2. Clamp the existing HUD value to the display refresh rate. This hides impossible numbers but does not repair the counter source.
3. Divide the legacy window-update rate by a guessed factor. Window callbacks are not one-to-one with frames, so the divisor is not stable.

## Design

- Expose the fresh native presentation rate separately from `FrameRateSampler`; do not change the established non-LSFG source priority.
- When LSFG is armed and its multiplier is at least two, select the native presented rate as the HUD value.
- Do not multiply an LSFG-active rate by the LSFG multiplier.
- During the first native sample or after a stalled presentation counter, show zero rather than a fabricated legacy-derived LSFG output rate.
- When LSFG is inactive, keep the existing guest/window-update FPS behavior unchanged.
- Log guest FPS, presented FPS, selected source, multiplier, and shown FPS so device acceptance can distinguish all three layers.

## Acceptance

- A 125 FPS guest/window callback sample plus a 60 FPS native presented sample and LSFG multiplier two displays 60, not 250.
- With LSFG inactive, a 30 FPS guest sample still displays 30.
- Native counter resets and stalls never retain a stale positive LSFG output value.
- The complete `legacyRelease` unit suite and release build pass.
- The installed RC6 identity and pulled APK hash match the archived candidate.

