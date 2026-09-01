# GameFold PC V0.8.2 Dual-Screen Focus Backing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render the dual-screen header action's focus frame behind and outside its 48dp button face without changing the other header actions.

**Architecture:** Add a pure frontend policy that selects `NONE`, `BACKING`, or `FOREGROUND` focus placement. `HeaderPixelAction` consumes that decision: the dual-screen `ownsFrame` branch draws a centered 56dp backing frame before its image, while regular actions retain their existing foreground frame.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit 4, Android Gradle legacy release.

## Global Constraints

- Dual-screen button layout and touch target remain exactly 48dp.
- Backing focus frame is centered at 56dp and may overflow the unclipped 48dp parent.
- Quick Start and Settings keep foreground focus behavior.
- Do not change dual-screen lifecycle, FC/NS, runtime, container, or input logic.

---

### Task 1: Focus Placement Policy and Header Rendering

**Files:**
- Modify: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicy.kt`
- Modify: `app/src/test/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt:738-816`

**Interfaces:**
- Produces: `HeaderFocusLayer { NONE, BACKING, FOREGROUND }`
- Produces: `headerFocusLayer(ownsFrame: Boolean, focused: Boolean): HeaderFocusLayer`
- Consumes: existing `ownsFrame` and `visual.showForegroundFocus` values.

- [ ] **Step 1: Write the failing policy test**

```kotlin
@Test
fun headerFocus_placesOwnedDualScreenFrameBehindButKeepsRegularFramesInFront() {
    assertEquals(HeaderFocusLayer.BACKING, headerFocusLayer(true, true))
    assertEquals(HeaderFocusLayer.FOREGROUND, headerFocusLayer(false, true))
    assertEquals(HeaderFocusLayer.NONE, headerFocusLayer(true, false))
}
```

- [ ] **Step 2: Run the focused test to verify RED**

Run `./gradlew :app:testLegacyReleaseUnitTest --tests 'app.gamenative.gamefold.ui.GameFoldPcFrontendPolicyTest' --offline --no-daemon --console=plain` and require an unresolved `HeaderFocusLayer`/`headerFocusLayer` failure.

- [ ] **Step 3: Implement the minimal policy**

```kotlin
enum class HeaderFocusLayer { NONE, BACKING, FOREGROUND }

fun headerFocusLayer(ownsFrame: Boolean, focused: Boolean): HeaderFocusLayer = when {
    !focused -> HeaderFocusLayer.NONE
    ownsFrame -> HeaderFocusLayer.BACKING
    else -> HeaderFocusLayer.FOREGROUND
}
```

- [ ] **Step 4: Consume the policy in `HeaderPixelAction`**

Before the button image, draw `Modifier.requiredSize(56.dp).gameFoldNsFocusFrame(true)` only for `BACKING`. After the image, retain the existing 48dp frame only for `FOREGROUND`.

- [ ] **Step 5: Run focused tests and verify GREEN**

Run the same focused test class and require zero failures.

- [ ] **Step 6: Commit**

Commit the policy, test, and single Compose rendering change.

### Task 2: RC3 Build and vivo Verification

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC3/manifest.txt`

**Interfaces:**
- Produces: `com.gamefold.pc`, versionName `0.8.2-rc3`, versionCode `10133`.

- [ ] **Step 1: Set RC3 identity**

Change default versionName/versionCode and their existing test to `0.8.2-rc3` / `10133`.

- [ ] **Step 2: Run full verification and build**

Run `:app:testLegacyReleaseUnitTest :app:assembleLegacyRelease`, `git diff --check`, then verify package/version/signer/SHA-256.

- [ ] **Step 3: Archive and install RC3**

Copy the APK to `releases/candidates/V0.8.2-DUAL-SCREEN-RC3/GameFold-PC-V0.8.2-DUAL-SCREEN-RC3-10133.apk`, record its manifest, and install in place with the vivo workflow without clearing data or permissions.

- [ ] **Step 4: Verify on vivo**

Confirm installed version/hash, focus the dual-screen action with DPAD, capture the front display, and verify the 56dp focus frame is behind the 48dp button while Quick Start/Settings are unchanged.
