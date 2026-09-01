# GameFold PC GTA5 Gyro Wine Mouse RC6 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce RC6 with the proven RC5 gyro calculation routed through Wine's game-relative mouse event channel.

**Architecture:** Add a testable nonzero-delta dispatcher and bind it to `WinHandler.mouseEvent(MouseEventFlags.MOVE, dx, dy, 0)` in `XServerScreen`. Preserve every upstream-derived motion/calibration behavior, add throttled runtime diagnostics, then assign `0.9.2 / 10150`, build, archive and install.

**Tech Stack:** Kotlin, Android sensors, Winlator `WinHandler`, JUnit 4, Gradle legacy release, adb/vivo installer.

## Global Constraints

- Work only on `codex/gamefold-pc-v0.9.2-gta5-gyro-wine-mouse-rc6` from archived RC5 commit `0ec4178`.
- Do not modify RC5 archives or formal baselines.
- Do not change gyro direction, sensitivity, calibration, executable scope or ZL behavior.
- Package/version is `com.gamefold.pc`, `0.9.2`, `10150`.
- GTA5 gameplay remains the final acceptance gate.

---

### Task 1: Protect the Wine relative-mouse transport

**Files:**
- Create: `app/src/main/java/app/gamenative/gamefold/gyro/GyroMouseOutputDispatcher.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/gyro/GyroMouseOutputDispatcherTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt:2786`

**Interfaces:**
- Consumes: integer gyro mouse deltas.
- Produces: one `sendRelativeMove(dx, dy)` call for a nonzero delta and no call for `(0, 0)`.

- [ ] Write tests for zero suppression and unchanged nonzero forwarding.
- [ ] Run only `GyroMouseOutputDispatcherTest` and require compilation failure because the dispatcher does not exist.
- [ ] Add the minimal dispatcher.
- [ ] Bind its sender to `WinHandler.mouseEvent(MouseEventFlags.MOVE, dx, dy, 0)` and remove the RC5 XServer callback.
- [ ] Run dispatcher and all gyro tests; require pass.

### Task 2: Add bounded diagnostics and RC6 identity

**Files:**
- Modify: `app/src/main/java/app/gamenative/gamefold/gyro/AndroidGyroMouseController.kt`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`

- [ ] Add `Log.i` on ZL gate transitions and at most one gyro-output summary per second while held.
- [ ] Change the identity test to `0.9.2 / 10150` and require it to fail against RC5.
- [ ] Change the Gradle fallback identity and require the identity test to pass.

### Task 3: Verify, archive and install RC6

**Files:**
- Create: `releases/candidates/V0.9.2-GTA5-GYRO-WINE-MOUSE-RC6/GameFold-PC-V0.9.2-GTA5-GYRO-WINE-MOUSE-RC6-10150.apk`
- Create: `releases/candidates/V0.9.2-GTA5-GYRO-WINE-MOUSE-RC6/README.md`
- Create: `releases/candidates/V0.9.2-GTA5-GYRO-WINE-MOUSE-RC6/EVIDENCE.md`

- [ ] Run `git diff --check` and the full `testLegacyReleaseUnitTest assembleLegacyRelease --rerun-tasks` command.
- [ ] Verify package, version, signer, size and SHA-256 before archiving.
- [ ] Install with the vivo installer helper without clearing app data.
- [ ] Pull installed `base.apk`; require identity and SHA-256 equality.
- [ ] Record evidence and explicitly leave GTA5 camera behavior pending user acceptance.
