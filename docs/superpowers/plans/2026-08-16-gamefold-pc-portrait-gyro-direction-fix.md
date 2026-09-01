# GameFold PC Portrait Gyro Direction Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce GameFold PC `1.0.1 / 10164` Release RC1 with correct gyro camera directions in portrait while preserving the accepted landscape behavior.

**Architecture:** Add a pure output-orientation policy between `GyroMouseProcessor` and `FractionalMouseDeltaAccumulator`. Capture the authoritative `GameFoldOrientation.isPortrait(context)` value when the GTA5 gyro controller is created; do not branch on device brand.

**Tech Stack:** Kotlin, Android Sensor API, JUnit 4, Gradle `legacyRelease`, ADB.

## Global Constraints

- Portrait output is exactly `(-dx, -dy)` from the existing floating-point delta.
- Landscape output is byte-for-byte equivalent at the policy boundary.
- Keep calibration at `19.6 counts/degree` and both sensitivities at `1x`.
- Preserve ZL gating, bias calibration, fractional accumulation, Wine mouse transport, branding, and About layout.
- Release identity is `com.gamefold.pc`, `1.0.1 / 10164`.

---

### Task 1: Orientation-aware gyro output and Release RC1

**Files:**
- Create: `app/src/main/java/app/gamenative/gamefold/gyro/GameFoldGyroOrientationPolicy.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/gyro/GameFoldGyroOrientationPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/gyro/AndroidGyroMouseController.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`

**Interfaces:**
- Consumes: `GyroMouseDelta`, `GameFoldOrientation.isPortrait(context)`.
- Produces: `GameFoldGyroOrientationPolicy.apply(delta: GyroMouseDelta, portraitMode: Boolean): GyroMouseDelta`.

- [ ] **Step 1: Write the failing direction-policy tests**

```kotlin
@Test fun portraitInvertsBothMouseAxes() {
    assertEquals(
        GyroMouseDelta(-12.5f, 7.25f),
        GameFoldGyroOrientationPolicy.apply(GyroMouseDelta(12.5f, -7.25f), true),
    )
}

@Test fun landscapePreservesBothMouseAxes() {
    val delta = GyroMouseDelta(12.5f, -7.25f)
    assertEquals(delta, GameFoldGyroOrientationPolicy.apply(delta, false))
}
```

- [ ] **Step 2: Run the targeted test and verify RED**

Run: `./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.gamefold.gyro.GameFoldGyroOrientationPolicyTest --offline --no-daemon --console=plain`

Expected: compilation failure because `GameFoldGyroOrientationPolicy` does not yet exist.

- [ ] **Step 3: Implement and connect the minimal policy**

```kotlin
internal object GameFoldGyroOrientationPolicy {
    fun apply(delta: GyroMouseDelta, portraitMode: Boolean): GyroMouseDelta =
        if (portraitMode) GyroMouseDelta(-delta.dxCounts, -delta.dyCounts) else delta
}
```

Add `portraitMode: Boolean` to `AndroidGyroMouseController`, apply the policy before `accumulator.add`, and pass `GameFoldOrientation.isPortrait(context)` from `XServerScreen`.

- [ ] **Step 4: Run the targeted direction tests and verify GREEN**

Run the Step 2 command again.

Expected: all policy tests pass.

- [ ] **Step 5: Update and test Release identity**

Set Gradle defaults to `versionName=1.0.1` and `versionCode=10164`; update `SettingsGeometryTest` to assert both values. Run that test and the complete gyro test package.

- [ ] **Step 6: Run full regression and build Release**

Run: `./gradlew :app:testLegacyReleaseUnitTest :app:assembleLegacyRelease --rerun-tasks --offline --no-daemon --console=plain`

Expected: all tests pass and `app-legacy-release.apk` is produced.

- [ ] **Step 7: Archive, install, and verify**

Archive as `releases/candidates/V1.0.1-PORTRAIT-GYRO-DIRECTION-RC1/GameFold-PC-V1.0.1-PORTRAIT-GYRO-DIRECTION-RC1-10164.apk`. Verify badging, signature, SHA-256, install in place on the connected OPPO, then pull the installed APK and compare its SHA-256 with the archive.
