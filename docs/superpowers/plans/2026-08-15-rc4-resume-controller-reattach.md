# RC4 Resume Controller Reattachment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Preserve and reattach the running WinHandler when a GameFold PC game returns from Home so the on-screen controller continues controlling the guest.

**Architecture:** Extend the pure session policy with an explicit runtime-input attachment decision. On a reused XServer, keep the started WinHandler and rebind its renderer view/context plus neutral virtual-pad state; create a handler only for a cold XServer.

**Tech Stack:** Kotlin, Java, Jetpack Compose AndroidView, JUnit 4, Gradle Android legacyRelease.

## Global Constraints

- Base the candidate on the committed RC3 branch state.
- Produce versionName `0.8.2-rc4` and versionCode `10134`.
- Do not change game pause/resume, replacement-game handling, rear-display lifecycle, or RC3 focus rendering.
- Preserve app data and permissions during vivo installation.
- Do not promote V0.8.1 or RC4 to a formal baseline without explicit user approval.

---

### Task 1: Runtime Input Attachment Policy

**Files:**
- Modify: `app/src/main/java/app/gamenative/gamefold/session/GameFoldSessionPolicy.kt`
- Test: `app/src/test/java/app/gamenative/gamefold/session/GameFoldSessionPolicyTest.kt`

**Interfaces:**
- Produces: `GameFoldRuntimeInputAttachmentAction` and `GameFoldSessionPolicy.runtimeInputAttachmentAction(reusedXServer: Boolean, existingHandlerPresent: Boolean)`.

- [ ] **Step 1: Write the failing policy test**

```kotlin
@Test
fun reusedXServerKeepsItsStartedInputHandler() {
    assertEquals(
        GameFoldRuntimeInputAttachmentAction.REATTACH_EXISTING,
        GameFoldSessionPolicy.runtimeInputAttachmentAction(
            reusedXServer = true,
            existingHandlerPresent = true,
        ),
    )
}

@Test
fun coldOrIncompleteXServerCreatesAnInputHandler() {
    assertEquals(
        GameFoldRuntimeInputAttachmentAction.CREATE_NEW,
        GameFoldSessionPolicy.runtimeInputAttachmentAction(false, false),
    )
    assertEquals(
        GameFoldRuntimeInputAttachmentAction.CREATE_NEW,
        GameFoldSessionPolicy.runtimeInputAttachmentAction(true, false),
    )
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.gamefold.session.GameFoldSessionPolicyTest --offline --no-daemon --console=plain`

Expected: compilation fails because `GameFoldRuntimeInputAttachmentAction` and `runtimeInputAttachmentAction` do not exist.

- [ ] **Step 3: Implement the pure policy**

```kotlin
enum class GameFoldRuntimeInputAttachmentAction {
    CREATE_NEW,
    REATTACH_EXISTING,
}

fun runtimeInputAttachmentAction(
    reusedXServer: Boolean,
    existingHandlerPresent: Boolean,
): GameFoldRuntimeInputAttachmentAction = if (reusedXServer && existingHandlerPresent) {
    GameFoldRuntimeInputAttachmentAction.REATTACH_EXISTING
} else {
    GameFoldRuntimeInputAttachmentAction.CREATE_NEW
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Expected: all `GameFoldSessionPolicyTest` tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/gamenative/gamefold/session/GameFoldSessionPolicy.kt app/src/test/java/app/gamenative/gamefold/session/GameFoldSessionPolicyTest.kt
git commit -m "test: define preserved input handler ownership"
```

### Task 2: Reattach the Started WinHandler

**Files:**
- Modify: `app/src/main/java/com/winlator/winhandler/WinHandler.java`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`

**Interfaces:**
- Consumes: `GameFoldSessionPolicy.runtimeInputAttachmentAction(...)`.
- Produces: `WinHandler.reattachRendererView(XServerRendererView)` and `WinHandler.restoreGameFoldVirtualGamepad()`.

- [ ] **Step 1: Make the WinHandler view reference reattachable**

Change `xServerView` from final to mutable and add:

```java
public void reattachRendererView(XServerRendererView view) {
    if (view == null) return;
    this.xServerView = view;
    this.activity = view.getContext();
    Log.i(TAG, "Reattached existing WinHandler to renderer view");
}

public void restoreGameFoldVirtualGamepad() {
    GamepadState neutral = new GamepadState();
    setGameFoldVirtualGamepadState(neutral);
    sendVirtualGamepadState(neutral);
    Log.i(TAG, "Restored GameFold virtual gamepad after renderer reattachment");
}
```

- [ ] **Step 2: Replace unconditional handler creation in XServerScreen**

Use the policy at the existing handler assignment:

```kotlin
val existingHandler = getxServer().winHandler
when (GameFoldSessionPolicy.runtimeInputAttachmentAction(
    reusedXServer = existingXServer != null,
    existingHandlerPresent = existingHandler != null,
)) {
    GameFoldRuntimeInputAttachmentAction.CREATE_NEW -> {
        getxServer().winHandler = WinHandler(getxServer(), this)
    }
    GameFoldRuntimeInputAttachmentAction.REATTACH_EXISTING -> {
        existingHandler!!.reattachRendererView(this)
        existingHandler.restoreGameFoldVirtualGamepad()
        Timber.i("[GameFoldSession] Reattached existing WinHandler")
    }
}
```

- [ ] **Step 3: Compile and run the focused policy suite**

Run the Task 1 focused command. Expected: PASS and production sources compile.

- [ ] **Step 4: Run all legacy release unit tests**

Run: `./gradlew :app:testLegacyReleaseUnitTest --offline --no-daemon --console=plain`

Expected: zero failures/errors.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/winlator/winhandler/WinHandler.java app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt
git commit -m "fix: reattach preserved game input handler"
```

### Task 3: RC4 Candidate and Device Verification

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/gamefold/ui/SettingsGeometryTest.kt`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC4/manifest.txt`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC4/GameFold-PC-V0.8.2-DUAL-SCREEN-RC4-10134.apk`
- Create: `docs/gamefold/evidence/2026-08-15-v082-dual-screen-rc4/`

**Interfaces:**
- Produces: independently installable `com.gamefold.pc` RC4 candidate.

- [ ] **Step 1: Update candidate identity**

Set `versionCode = 10134` and `versionName = "0.8.2-rc4"`; update the version identity test.

- [ ] **Step 2: Run full tests and release build**

Run: `./gradlew :app:testLegacyReleaseUnitTest :app:assembleLegacyRelease --offline --no-daemon --console=plain`

Expected: BUILD SUCCESSFUL, zero test failures.

- [ ] **Step 3: Verify and archive APK identity**

Check package/version with `aapt`, signing certificate with `apksigner`, and SHA-256 with `shasum`; copy the APK and record exact evidence in `manifest.txt`.

- [ ] **Step 4: Install in place on vivo**

Run the vivo installer helper against the archived RC4 APK. Do not uninstall or clear app state.

- [ ] **Step 5: Verify the original scenario twice**

Cold-start one game, verify controller response, press GameFold HOME, reopen the same game, and verify controller response. Repeat Home/reopen once more. Capture logs proving `Reattached existing WinHandler`, no new Java evshim mapping during resume, and continued frame presentation.

- [ ] **Step 6: Verify device/package and lifecycle evidence**

Confirm installed version `0.8.2-rc4 / 10134`, pulled APK hash equality, front display ON, rear display OFF when dual screen is disabled, and no crash/ANR.

- [ ] **Step 7: Commit candidate evidence**

```bash
git add app/build.gradle.kts app/src/test/java/app/gamenative/gamefold/ui/SettingsGeometryTest.kt releases/candidates/V0.8.2-DUAL-SCREEN-RC4 docs/gamefold/evidence/2026-08-15-v082-dual-screen-rc4
git commit -m "release: archive v0.8.2 rc4 candidate"
```
