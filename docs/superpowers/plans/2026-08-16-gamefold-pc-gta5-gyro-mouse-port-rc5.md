# GameFold PC GTA5 Gyro Mouse Port RC5 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce an installable RC5 candidate that ports JoyShockMapper/GamepadMotionHelpers-style calibrated gyro mouse aiming into GTA5 while preserving strict ZL gating and a pure manual right stick.

**Architecture:** Start from RC3 commit `b16d418`. Replace gyro-to-virtual-right-stick output with calibrated angular velocity integrated into Wine/XServer relative mouse deltas, with GamepadMotionHelpers-style stillness bias estimation and JoyShockMapper-style fractional mouse accumulation. Keep the feature GTA5-local-shell scoped and preserve all frozen baselines and rejected RC archives.

**Tech Stack:** Kotlin, Android `SensorManager`, Winlator `XServer.injectPointerMoveDelta`, JUnit 4, Gradle Android release build, adb/vivo installer helper.

## Global Constraints

- Work only on branch `codex/gamefold-pc-v0.9.1-gta5-gyro-mouse-rc5` created from RC3 verification commit `b16d418`.
- Do not change or overwrite formal V0.8.6, RC1, RC2, RC3 or rejected RC4 archives.
- Package remains `com.gamefold.pc`.
- RC5 identity is `versionName=0.9.1`, `versionCode=10149`.
- Gyro output is allowed only for local-shell `GTA5.exe` and `GTAVLauncher.exe` while ZL is held.
- ZL remains the virtual Xbox left trigger; gyro never modifies virtual right-stick axes.
- Default X/Y gyro sensitivity is equal.
- Ship the upstream MIT license and record source commit `f1a76007ec058122a4bb6f19c816c6c0c7b7f12a`.
- Automatic tests do not count as GTA5 gameplay acceptance.

---

### Task 1: Port the motion-to-relative-mouse control law

**Files:**
- Create: `app/src/main/java/app/gamenative/gamefold/gyro/GyroMouseProcessor.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/gyro/GamepadMotionStillnessCalibrator.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/gyro/GyroMouseProcessorTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/gyro/GamepadMotionStillnessCalibratorTest.kt`
- Delete after replacement: `app/src/main/java/app/gamenative/gamefold/gyro/GyroAimProcessor.kt`
- Delete after replacement: `app/src/test/java/app/gamenative/gamefold/gyro/GyroAimProcessorTest.kt`

**Interfaces:**
- Consumes: gyro angular velocity in radians/second, optional acceleration in g, Android display quarter-turn and sensor timestamp.
- Produces: `GyroMouseDelta(dxCounts: Float, dyCounts: Float)`, `FractionalMouseDeltaAccumulator.add(delta): IntegerMouseDelta`, and `GamepadMotionStillnessCalibrator.bias`.

- [ ] **Step 1: Write failing processor tests**

Add tests proving: ZL-off samples produce zero; first aimed sample only establishes timing; 20 degrees/second for 50 ms at `40 counts/degree` produces 40 counts; equal opposite rotations sum to zero; display rotations preserve expected signs; equal X/Y physical angles use equal counts; a gap over 50 ms emits no jump; release clears timing.

- [ ] **Step 2: Run the processor tests and verify RED**

Run:

```bash
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:testLegacyReleaseUnitTest --offline --no-daemon --console=plain \
  --tests 'app.gamenative.gamefold.gyro.GyroMouseProcessorTest'
```

Expected: compilation/test failure because `GyroMouseProcessor` and its output types do not yet exist.

- [ ] **Step 3: Implement the minimal processor and fractional accumulator**

Implement these exact public-in-module concepts:

```kotlin
internal data class GyroMouseSample(
    val xRadPerSec: Float,
    val yRadPerSec: Float,
    val zRadPerSec: Float,
    val timestampNs: Long,
    val rotation: DisplayQuarterTurn,
)

internal data class GyroMouseDelta(val dxCounts: Float, val dyCounts: Float)
internal data class IntegerMouseDelta(val dx: Int, val dy: Int)

internal class GyroMouseProcessor {
    fun setAimHeld(held: Boolean)
    fun setBias(bias: GyroBias)
    fun onGyroSample(sample: GyroMouseSample): GyroMouseDelta
    fun reset()
}

internal class FractionalMouseDeltaAccumulator {
    fun add(delta: GyroMouseDelta): IntegerMouseDelta
    fun reset()
}
```

Use `mouseX = yawDegrees * 40` and `mouseY = -pitchDegrees * 40`. Do not add deadzone, smoothing, acceleration or right-stick conversion. Preserve sub-count remainders by truncating the accumulated float toward zero exactly as JoyShockMapper does.

- [ ] **Step 4: Run processor tests and verify GREEN**

Run the command from Step 2. Expected: all `GyroMouseProcessorTest` tests pass.

- [ ] **Step 5: Write failing stillness-calibration tests**

Add tests proving: fewer than 0.5 seconds cannot establish stability; unstable gyro or acceleration resets the candidate window; two seconds of stable samples learns the three-axis mean bias; calibration is not updated when the caller freezes it for an aim session.

- [ ] **Step 6: Run calibrator tests and verify RED**

Run:

```bash
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:testLegacyReleaseUnitTest --offline --no-daemon --console=plain \
  --tests 'app.gamenative.gamefold.gyro.GamepadMotionStillnessCalibratorTest'
```

Expected: compilation/test failure because the stillness calibrator does not exist.

- [ ] **Step 7: Port the GamepadMotionHelpers stillness subset**

Port the upstream `SensorMinMaxWindow` and stillness calibration behavior needed for gyro bias: min/max/mean tracking for three gyro and three acceleration axes, minimum 10 samples, 0.5-second collection gate, 2-second correction gate, and eased bias replacement. Keep upstream attribution in the file header and expose only:

```kotlin
internal class GamepadMotionStillnessCalibrator {
    val bias: GyroBias
    fun addSample(gyroDps: Vector3f, accelG: Vector3f, deltaSeconds: Float)
    fun setFrozen(frozen: Boolean)
    fun resetCandidateWindow()
    fun reset()
}
```

- [ ] **Step 8: Run all gyro math tests and commit**

Run both test classes, then:

```bash
git add app/src/main/java/app/gamenative/gamefold/gyro \
  app/src/test/java/app/gamenative/gamefold/gyro
git commit -m "feat: port calibrated gyro mouse control law"
```

### Task 2: Wire Android sensors, ZL gate and XServer relative mouse injection

**Files:**
- Create: `app/src/main/java/app/gamenative/gamefold/gyro/AndroidGyroMouseController.kt`
- Delete after replacement: `app/src/main/java/app/gamenative/gamefold/gyro/AndroidGyroAimController.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/gyro/GameFoldRuntimeGamepadBridge.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Modify: `app/src/test/java/app/gamenative/gamefold/gyro/GameFoldRuntimeGamepadBridgeTest.kt`

**Interfaces:**
- Consumes: `GameFoldRuntimeGamepadBridge.onAimHeldChanged(Boolean)`, Android gyro/accelerometer events and the active `XServer`.
- Produces: integer `(dx, dy)` relative mouse deltas sent through `XServer.injectPointerMoveDelta(dx, dy)`.

- [ ] **Step 1: Change bridge tests first**

Replace assertions about mixed gyro/right-stick values with assertions that: ZL still drives `triggerL`; ZL gate callback fires on press/release; right-stick values remain exactly the manual values through gate changes; stale gyro callbacks are impossible because the bridge no longer accepts gyro vectors; lifecycle release clears ZL without altering the manual stick.

- [ ] **Step 2: Run bridge tests and verify RED**

Run:

```bash
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:testLegacyReleaseUnitTest --offline --no-daemon --console=plain \
  --tests 'app.gamenative.gamefold.gyro.GameFoldRuntimeGamepadBridgeTest'
```

Expected: failures against the RC3 bridge's gyro/right-stick mixing API.

- [ ] **Step 3: Remove gyro/right-stick mixing from the bridge**

Keep `manualRightX` and `manualRightY` as the sole sources of `state.thumbRX` and `state.thumbRY`. Keep `onAimHeldChanged` and left-trigger publication. Remove `gyroAim`, `onGyroAimChanged` and mixed-stick arithmetic.

- [ ] **Step 4: Run bridge tests and verify GREEN**

Run the command from Step 2. Expected: all bridge tests pass.

- [ ] **Step 5: Implement the Android controller and XServer wiring**

Register `TYPE_GYROSCOPE` at 10,000 microseconds and `TYPE_ACCELEROMETER` as the stillness reference on the main looper. Convert gyro radians/second to the processor input unchanged and accelerometer m/s² to g. Feed the calibrator only with ZL released; freeze bias while aiming. Send nonzero integer deltas through the active XServer. On ZL release, lifecycle pause, session teardown or display rotation discontinuity, reset processor timing and fractional remainder.

- [ ] **Step 6: Run scoped gyro/session tests and commit**

Run:

```bash
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:testLegacyReleaseUnitTest --offline --no-daemon --console=plain \
  --tests 'app.gamenative.gamefold.gyro.*'
```

Then:

```bash
git add app/src/main/java/app/gamenative/gamefold/gyro \
  app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt \
  app/src/test/java/app/gamenative/gamefold/gyro
git commit -m "feat: inject ZL-gated gyro as relative mouse"
```

### Task 3: Add source attribution and RC5 identity

**Files:**
- Create: `app/src/main/assets/licenses/GamepadMotionHelpers-LICENSE.txt`
- Create: `docs/third-party/GamepadMotionHelpers-port.md`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`

**Interfaces:**
- Consumes: upstream MIT license and source commit.
- Produces: APK identity `0.9.1` / `10149` and auditable source provenance.

- [ ] **Step 1: Change the identity test and verify RED**

Assert `BuildConfig.VERSION_NAME == "0.9.1"` and `BuildConfig.VERSION_CODE == 10149`, then run only `SettingsGeometryTest`. Expected: failure showing RC3 identity `0.8.9` / `10147`.

- [ ] **Step 2: Update identity and add attribution**

Set fallback version name/code to `0.9.1` / `10149`. Copy the complete upstream MIT license into the APK assets. Document upstream repository, commit, version, ported classes, local modifications and the fact that no upstream trademark is used.

- [ ] **Step 3: Verify identity test, license packaging path and commit**

Run the identity test, confirm the source/license files exist with `rg`, then commit with:

```bash
git add app/build.gradle.kts app/src/main/assets/licenses \
  app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt \
  docs/third-party docs/superpowers/specs docs/superpowers/plans
git commit -m "release: identify and attribute GTA5 gyro mouse RC5"
```

### Task 4: Verify, build, archive and install RC5

**Files:**
- Create: `releases/candidates/V0.9.1-GTA5-GYRO-MOUSE-RC5/GameFold-PC-V0.9.1-GTA5-GYRO-MOUSE-RC5-10149.apk`
- Create: `releases/candidates/V0.9.1-GTA5-GYRO-MOUSE-RC5/README.md`
- Create: `releases/candidates/V0.9.1-GTA5-GYRO-MOUSE-RC5/EVIDENCE.md`

**Interfaces:**
- Consumes: verified source tree and release APK.
- Produces: archived candidate plus vivo-installed byte-identical artifact evidence.

- [ ] **Step 1: Run source and full test/build verification**

Run `git diff --check`, then:

```bash
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:testLegacyReleaseUnitTest :app:assembleLegacyRelease \
  --offline --no-daemon --console=plain --rerun-tasks
```

Require exit code 0 and zero failed tests.

- [ ] **Step 2: Verify and archive APK identity**

Use Android `aapt2 dump badging`, `apksigner verify --print-certs`, `stat` and `shasum -a 256` on `app/build/outputs/apk/legacy/release/app-legacy-release.apk`. Copy it to the exact RC5 archive filename only after badging reports package `com.gamefold.pc`, version `0.9.1`, code `10149`.

- [ ] **Step 3: Install with the vivo skill**

Run:

```bash
PATH='/Users/11085429/Library/Android/sdk/platform-tools':$PATH \
python3 /Users/11085429/.codex/skills/install-apk-vivo/scripts/install_apk_vivo.py \
  '/Users/11085429/Documents/Game Fold/gamefold-pc-v082-dual-screen-candidate/releases/candidates/V0.9.1-GTA5-GYRO-MOUSE-RC5/GameFold-PC-V0.9.1-GTA5-GYRO-MOUSE-RC5-10149.apk'
```

Preserve app data and require installer success.

- [ ] **Step 4: Verify installed identity and byte equality**

Query `dumpsys package com.gamefold.pc` for version and install time, pull the installed base APK, and require its SHA-256 to equal the archived candidate SHA-256.

- [ ] **Step 5: Write evidence and commit the candidate**

Record source/base commits, upstream source/license, tests, build, APK metadata, hashes, signer, device identity, installed version/time and the remaining GTA5 gameplay acceptance gate. Commit with:

```bash
git add releases/candidates/V0.9.1-GTA5-GYRO-MOUSE-RC5
git commit -m "archive: store GTA5 gyro mouse RC5 candidate"
```
