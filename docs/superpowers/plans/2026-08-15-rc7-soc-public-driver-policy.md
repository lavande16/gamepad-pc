# GameFold PC V0.8.2 RC7 SoC Public Driver Policy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Select V30 for SM8750/SM8850 and V25 for SM8650 from the GameFold PC public graphics directory without packaging either driver in the APK.

**Architecture:** A pure `GameFoldPcDriverPolicy` owns SoC-to-driver mapping. `ContainerUtils` applies it only to automatic defaults, while `GameFoldPcResourceRoot` and `AdrenotoolsManager` expose exact public archives to settings and runtime extraction.

**Tech Stack:** Kotlin, Java, Android, JUnit/Robolectric, Gradle, AdrenoTools.

## Global Constraints

- Preserve the V0.8.1 baseline and RC1-RC6 candidates.
- Package remains `com.gamefold.pc`; RC7 is `0.8.2-rc7` / `10137`.
- Store V25/V30 only under `/storage/emulated/0/GameFoldPC/graphics/`; package neither archive in the APK.
- Never overwrite an explicit per-game driver selection.
- Never substitute a different Turnip build when an exact SoC-selected archive is missing.
- Install in place without clearing app data or permissions.

---

### Task 1: SoC driver selection policy

**Files:**
- Create: `app/src/main/java/app/gamenative/product/GameFoldPcDriverPolicy.kt`
- Create: `app/src/test/java/app/gamenative/product/GameFoldPcDriverPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/utils/ContainerUtils.kt`

**Interfaces:**
- Produces: `GameFoldPcDriverPolicy.defaultDriverForSoc(socModel: String?, fallback: String): String`
- Produces: `GameFoldPcDriverPolicy.shouldReplaceAutomaticDriver(current: String, previousAutomatic: String): Boolean`

- [ ] **Step 1:** Add tests asserting SM8750 and SM8850 return `Turnip Gen8 V30`, SM8650 returns `Turnip_Gen8_V25`, unknown/null use the fallback, and explicit T26/V30 choices are preserved.
- [ ] **Step 2:** Run `:app:testLegacyReleaseUnitTest --tests app.gamenative.product.GameFoldPcDriverPolicyTest` and verify compilation fails because the policy is absent.
- [ ] **Step 3:** Implement the pure policy and use `HardwareUtils.getSOCName()` when creating or migrating GameFold PC containers.
- [ ] **Step 4:** Bump the GameFold PC runtime migration marker and replace only blank, `System`, or the prior automatic default.
- [ ] **Step 5:** Re-run the focused test and require PASS.

### Task 2: Exact public driver availability and extraction

**Files:**
- Modify: `app/src/main/java/app/gamenative/utils/GameFoldPcResourceRoot.kt`
- Modify: `app/src/main/java/com/winlator/contents/AdrenotoolsManager.java`
- Modify: `app/src/main/java/app/gamenative/utils/ManifestComponentHelper.kt`
- Modify: `app/src/test/java/app/gamenative/utils/GameFoldPcResourceRootTest.kt`

**Interfaces:**
- Produces: `GameFoldPcResourceRoot.resolveGraphicsDriverExact(vararg logicalNames: String?): File?`
- Produces: `AdrenotoolsManager.enumarateAvailableDrivers(): ArrayList<String>`

- [ ] **Step 1:** Add temp-root tests proving exact V25/V30 matches and proving a directory containing only V30 does not satisfy a V25 request.
- [ ] **Step 2:** Run the focused resource-root test and verify it fails because exact-root resolution is absent.
- [ ] **Step 3:** Refactor graphics resolution so the public API can disable Turnip fallback; make AdrenoTools use strict resolution.
- [ ] **Step 4:** Include strict public V25/V30 archives in the locally available settings list without installing them into the APK.
- [ ] **Step 5:** Re-run focused policy/resource tests and require PASS.

### Task 3: Build and verify RC7

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC7/GameFold-PC-V0.8.2-DUAL-SCREEN-RC7-10137.apk`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC7/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC7/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC7/manifest.txt`

**Interfaces:**
- Produces: signed `legacyRelease` APK `0.8.2-rc7` / `10137` plus a separate public-resource payload.

- [ ] **Step 1:** Change the version identity test to RC7 and verify RED against RC6.
- [ ] **Step 2:** Update build identity to `10137` / `0.8.2-rc7` and verify the identity test passes.
- [ ] **Step 3:** Run `:app:testLegacyReleaseUnitTest :app:assembleLegacyRelease --offline` and require zero failures.
- [ ] **Step 4:** Archive APK and public V25/V30 resources, record SHA-256 and sizes, and inspect APK entries to prove no graphics-driver payload is embedded.
- [ ] **Step 5:** Push the two `.tzst` files to `/storage/emulated/0/GameFoldPC/graphics/`, install RC7 in place, and verify package/version/install time/pulled APK hash.
- [ ] **Step 6:** Launch GTA5 on SM8850 and verify logs name V30, the exact public path resolves, and the 3D scene has no checkerboard corruption.
