# GamePad PC V1 Implementation Plan

> 2026-08-21 approved amendment: mount the controller groups and GAMEPAD HOME only on the game/XServer surface. All non-game pages are touch-only. Scale Home cartridges and their touch regions to `70%` while retaining `12sp` title typography, and align both Header groups to the scaled first/last cartridge outer edges.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce a signed, non-debuggable GamePad PC 1.0 Release candidate for the connected 2304 × 1440 vivo tablet, preserving GameFold PC 1.1 runtime/input behavior while introducing an independent product identity, storage root, full-screen shell, aspect-ratio policy, and approved controller geometry.

**Architecture:** Branch from the frozen `gamefold-pc-v1.1-baseline` commit and keep inherited runtime code intact. Introduce pure GamePad policy/geometry objects for window support, runtime viewport fitting, and controller transforms; make the existing shell host consume those explicit contracts instead of the foldable global transform. Keep baseline controller classification/feedback semantics as the source of truth, but inverse-transform left/right screen touches before classification and handle the GAMEPAD HOME rectangle directly in screen coordinates.

**Tech Stack:** Android/Kotlin/Java, Jetpack Compose hosted in Android Views, Gradle Android plugin, JUnit/Robolectric, Android SDK 37 build tools, ADB, vivo installer automation.

**Spec:** `docs/superpowers/specs/2026-08-21-gamepad-pc-v1-design.md`

## Global Constraints

- Base all product code on tag `gamefold-pc-v1.1-baseline` at commit `fee253141f67373744893712d6b3a38114c7e4f2`; never move or rewrite that tag.
- Develop in an isolated worktree and branch. Do not modify or replace the existing GameFold PC installation, package, public data, release archive, or frozen pointers.
- The only supported product identity is `com.gamepad.pc`, `versionName=1.0`, `versionCode=10001`, external root `/storage/emulated/0/GamePadPC/`.
- Every candidate is a signed Release build with `android:debuggable=false`; `run-as com.gamepad.pc` must fail.
- Do not rename inherited technical classes or flags merely for cosmetic consistency. Rename/change only product-facing identity, paths, entry points, policies, and tests required by the approved spec.
- Preserve GameFold PC 1.1 input semantics, runtime behavior, launcher artwork, splash artwork, signing identity, graphics/runtime provisioning, gyro behavior, and save import unless this plan explicitly changes a boundary.
- Treat build/package/hash evidence separately from runtime UI/touch/game acceptance.
- Run the scoped failing test before each implementation step, then rerun it after the minimum change. Keep each task independently reviewable and commit it before starting the next task.

## File Map

### New production files

- `app/src/main/java/app/gamenative/gamepad/GamePadProductContract.kt` — one source of truth for target pixels, package-facing storage name, release identity constants used by tests, and disabled-product-feature flags.
- `app/src/main/java/app/gamenative/gamepad/GamePadWindowPolicy.kt` — fail-closed support decision for exact 2304 × 1440 immersive landscape, 576dp target, and rejection of portrait/multi-window/PiP.
- `app/src/main/java/app/gamenative/gamepad/GamePadViewportPolicy.kt` — pure integer viewport fitting for 16:10 fill and 16:9 centered letterbox without crop/stretch.
- `app/src/main/java/app/gamenative/gamepad/GamePadControllerGeometry.kt` — left/right 72% transforms, inverse transforms, visual/touch mappings, exact HOME rectangle, and cutout intersection helpers.

### Modified production files

- `app/build.gradle.kts` — product identity/version defaults, GamePad public path, Release run-as contract, test allowlist, and variant metadata.
- `app/src/main/AndroidManifest.xml` — GamePad wording, fixed landscape/non-resizable/no-PiP contract, release debuggable placeholder, and removal of rear-activity reachability.
- `app/src/main/res/values*/strings.xml` — app title and user-visible GameFold PC/public-path strings changed to GamePad PC/GamePadPC in every locale where they occur.
- `app/src/main/res/drawable-nodpi/gamepad_home_logo.webp` — supplied 270 × 77 transparent HOME asset copied byte-for-byte.
- `app/src/main/java/app/gamenative/utils/GameFoldPcResourceRoot.kt` — inherited resolver now roots only at `GamePadPC`; no fallback to `GameFoldPC`.
- `app/src/main/java/com/winlator/container/Container.java`, `app/src/main/java/com/winlator/core/WineUtils.java`, `app/src/main/java/com/winlator/core/DXVKHelper.java` — package-private data paths changed to `com.gamepad.pc`.
- Runtime/provisioning/downloader/UI call sites found by the bounded product-string scan — product-visible `GameFoldPC` paths/messages/log labels changed to GamePad equivalents without changing resolver semantics.
- `app/src/main/java/app/gamenative/gamefold/ui/GameFoldControllerInput.kt` — expose/reuse group-specific baseline classification instead of coupling HOME and both groups to one global coordinate space.
- `app/src/main/java/app/gamenative/gamefold/ui/GameFoldShellLayout.kt` — render a full-window GamePad shell, explicit content/overlay roles, independent controller transforms, exact HOME bounds, and pass-through touch outside transformed hits.
- `app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcShellHost.kt` — add owned-page content through the explicit full-window role and update product-visible labels.
- `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt` — provide runtime source size to the viewport policy, use full-window overlays, remove rear-touch routing, and keep gyro/gamepad bridge behavior.
- `app/src/main/java/app/gamenative/MainActivity.kt`, `app/src/main/java/app/gamenative/PluviaApp.kt`, `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt` — force landscape and make fold/rear display controls/entry points unreachable.

### New/modified tests

- `app/src/test/java/app/gamenative/product/GamePadProductContractTest.kt`
- `app/src/test/java/app/gamenative/gamepad/GamePadWindowPolicyTest.kt`
- `app/src/test/java/app/gamenative/gamepad/GamePadViewportPolicyTest.kt`
- `app/src/test/java/app/gamenative/gamepad/GamePadControllerGeometryTest.kt`
- `app/src/test/java/app/gamenative/gamepad/GamePadShellSourceContractTest.kt`
- `app/src/test/java/app/gamenative/gamepad/GamePadRuntimeIntegrationContractTest.kt`
- Existing `GameFoldControllerInputTest.kt`, `GameFoldPcRunAsBuildContractTest.kt`, `GameFoldPcProtectedRuntimeContractTest.kt`, `GameFoldPcFrontendPolicyTest.kt`, and `SettingsGeometryTest.kt` — update only assertions invalidated by the approved independent product contract; preserve semantic regression assertions.

---

## Task 1: Create the Isolated GamePad Product Worktree

**Files:**

- Create worktree directory: `/Users/11085429/Documents/Game Fold/gamepad-pc-v1-candidate`
- Preserve source repo: `/Users/11085429/Documents/Game Fold/gamefold-pc-v082-dual-screen-candidate`

- [ ] **Step 1: Verify the frozen baseline and clean source state**

Run:

```bash
git status --short
git rev-parse gamefold-pc-v1.1-baseline
git show -s --format='%H %s' gamefold-pc-v1.1-baseline
```

Expected: clean state; tag resolves exactly to `fee253141f67373744893712d6b3a38114c7e4f2`.

- [ ] **Step 2: Create an isolated worktree from the tag**

Invoke the required `superpowers:using-git-worktrees` skill, then create branch `codex/gamepad-pc-v1.0` at the frozen tag in `/Users/11085429/Documents/Game Fold/gamepad-pc-v1-candidate`.

Run:

```bash
git worktree add -b codex/gamepad-pc-v1.0 "/Users/11085429/Documents/Game Fold/gamepad-pc-v1-candidate" gamefold-pc-v1.1-baseline
```

Expected: the new worktree HEAD is `fee2531`; the source worktree remains unchanged.

- [ ] **Step 3: Bring the approved documentation into the product branch**

Cherry-pick the approved design-doc commit, then resolve the plan commit from the source worktree by its unique file path and cherry-pick that resolved hash. Verify the resolved commit contains only this plan before applying it.

Run:

```bash
git cherry-pick 2a3e42e
GAMEPAD_PLAN_COMMIT="$(git -C "/Users/11085429/Documents/Game Fold/gamefold-pc-v082-dual-screen-candidate" log -1 --format='%H' -- docs/superpowers/plans/2026-08-21-gamepad-pc-v1.md)"
git cherry-pick "$GAMEPAD_PLAN_COMMIT"
```

Expected: product history has the frozen tag as its ancestor and contains both approved docs.

- [ ] **Step 4: Record pre-change evidence**

Run:

```bash
git status --short
git merge-base --is-ancestor gamefold-pc-v1.1-baseline HEAD
git diff --stat gamefold-pc-v1.1-baseline..HEAD
```

Expected: only documentation differs; the ancestor check exits 0.

## Task 2: Lock Independent Product Identity, Storage, and Release Contract

**Files:**

- Create: `app/src/main/java/app/gamenative/gamepad/GamePadProductContract.kt`
- Create: `app/src/test/java/app/gamenative/product/GamePadProductContractTest.kt`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/app/gamenative/utils/GameFoldPcResourceRoot.kt`
- Modify: `app/src/main/java/com/winlator/container/Container.java`
- Modify: `app/src/main/java/com/winlator/core/WineUtils.java`
- Modify: `app/src/main/java/com/winlator/core/DXVKHelper.java`
- Modify: product-path callers returned by the final bounded scan
- Modify: `app/src/test/java/app/gamenative/product/GameFoldPcRunAsBuildContractTest.kt`
- Modify: `app/src/test/java/app/gamenative/product/GameFoldPcProtectedRuntimeContractTest.kt`

- [ ] **Step 1: Write the failing identity/release/path contract test**

Add assertions that read the Gradle script, merged-source manifest, resource-root source, Java private-path sources, and localized resources. The test must assert:

```kotlin
assertEquals("com.gamepad.pc", GamePadProductContract.APPLICATION_ID)
assertEquals("GamePad PC", GamePadProductContract.APP_NAME)
assertEquals("GamePadPC", GamePadProductContract.PUBLIC_ROOT_NAME)
assertEquals("1.0", GamePadProductContract.VERSION_NAME)
assertEquals(10001, GamePadProductContract.VERSION_CODE)
assertFalse(GamePadProductContract.RUN_AS_ENABLED)
```

It must also reject any active product path containing `/storage/emulated/0/GameFoldPC` or `/data/data/com.gamefold.pc` and verify the manifest still binds `android:debuggable` to a placeholder whose Release value is false.

- [ ] **Step 2: Run the scoped test and confirm it fails**

Run:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/11085429/Library/Android/sdk' GRADLE_USER_HOME='/Users/11085429/.gradle' ./gradlew :app:testLegacyReleaseUnitTest --tests 'app.gamenative.product.GamePadProductContractTest' --offline --no-daemon --console=plain
```

Expected: FAIL because GamePad contract/source values do not exist yet.

- [ ] **Step 3: Add the minimum product contract and Gradle defaults**

Implement:

```kotlin
object GamePadProductContract {
    const val APPLICATION_ID = "com.gamepad.pc"
    const val APP_NAME = "GamePad PC"
    const val PUBLIC_ROOT_NAME = "GamePadPC"
    const val VERSION_NAME = "1.0"
    const val VERSION_CODE = 10001
    const val RUN_AS_ENABLED = false
    const val REAR_DISPLAY_ENABLED = false
    const val REAR_TOUCH_ENABLED = false
    const val PORTRAIT_ENABLED = false
}
```

In `app/build.gradle.kts`, make these the non-overridable GamePad defaults for normal builds, keep the existing Release build type, force its `gameFoldPcRunAsEnabled` placeholder to `false`, and add the new test classes to `gameFoldPcSupportedLegacyTestClasses`. Do not enable the debug signing/run-as candidate path.

- [ ] **Step 4: Redirect all owned paths without adding fallback behavior**

Change `GameFoldPcResourceRoot.ROOT_NAME` to `GamePadPC`, LSFG paths to `/storage/emulated/0/GamePadPC/...`, and package-private paths to `/data/data/com.gamepad.pc/...`. Use this audit command after editing:

```bash
rg -n '/storage/emulated/0/GameFoldPC|/data/data/com\.gamefold\.pc|applicationId\s*=\s*"com\.gamefold\.pc"' app/src/main app/build.gradle.kts
```

Expected: no active product path matches. Comments/logs may keep historical class names, but no path resolver or user instruction may point to GameFoldPC.

- [ ] **Step 5: Update launcher title/product wording while preserving icon/splash bytes**

Change all `app_name` resource variants to `GamePad PC`, update visible resource/provisioning messages to `GamePad PC` and `GamePadPC`, and leave every launcher/splash bitmap and XML asset unchanged. Extend `SettingsGeometryTest` to compare the existing icon SHA-256 before and after the port.

- [ ] **Step 6: Make rear/fold entry points absent from the manifest**

Remove the rear Activity declaration from the packaged manifest and set the main Activity to fixed landscape, `resizeableActivity=false`, and `supportsPictureInPicture=false`. Do not delete inherited rear implementation files in this task.

- [ ] **Step 7: Run the identity and inherited release-contract tests**

Run:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/11085429/Library/Android/sdk' GRADLE_USER_HOME='/Users/11085429/.gradle' ./gradlew :app:testLegacyReleaseUnitTest --tests 'app.gamenative.product.GamePadProductContractTest' --tests 'app.gamenative.product.GameFoldPcRunAsBuildContractTest' --tests 'app.gamenative.product.GameFoldPcProtectedRuntimeContractTest' --tests 'app.gamenative.ui.screen.settings.SettingsGeometryTest' --offline --no-daemon --console=plain
```

Expected: PASS.

- [ ] **Step 8: Commit the independent product contract**

```bash
git add app/build.gradle.kts app/src/main app/src/test
git commit -m "feat: establish independent GamePad PC product contract"
```

## Task 3: Implement the Fail-Closed 2304 × 1440 Window Policy

**Files:**

- Create: `app/src/main/java/app/gamenative/gamepad/GamePadWindowPolicy.kt`
- Create: `app/src/test/java/app/gamenative/gamepad/GamePadWindowPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/MainActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Write failing pure policy tests**

Cover exact support and rejection:

```kotlin
assertTrue(GamePadWindowPolicy.isSupported(state(2304, 1440, 576)))
assertFalse(GamePadWindowPolicy.isSupported(state(2303, 1440, 576)))
assertFalse(GamePadWindowPolicy.isSupported(state(2304, 1439, 576)))
assertFalse(GamePadWindowPolicy.isSupported(state(1440, 2304, 576)))
assertFalse(GamePadWindowPolicy.isSupported(state(2304, 1440, 575)))
assertFalse(GamePadWindowPolicy.isSupported(state(2304, 1440, 576, multiWindow = true)))
assertFalse(GamePadWindowPolicy.isSupported(state(2304, 1440, 576, pictureInPicture = true)))
```

Also test that background bounds are `0,0,2304,1440`, logical corner radius is 0, and logical border width is 0.

- [ ] **Step 2: Run the scoped test and confirm failure**

Run the `GamePadWindowPolicyTest` with `:app:testLegacyReleaseUnitTest`; expect unresolved policy symbols.

- [ ] **Step 3: Implement the pure window contract**

Implement:

```kotlin
data class GamePadWindowState(
    val widthPx: Int,
    val heightPx: Int,
    val smallestWidthDp: Int,
    val inMultiWindow: Boolean,
    val inPictureInPicture: Boolean,
)

object GamePadWindowPolicy {
    const val WIDTH_PX = 2304
    const val HEIGHT_PX = 1440
    const val MIN_SMALLEST_WIDTH_DP = 576

    fun isSupported(state: GamePadWindowState): Boolean =
        state.widthPx == WIDTH_PX &&
            state.heightPx == HEIGHT_PX &&
            state.smallestWidthDp >= MIN_SMALLEST_WIDTH_DP &&
            !state.inMultiWindow &&
            !state.inPictureInPicture
}
```

- [ ] **Step 4: Apply immersive landscape in MainActivity**

Remove calls that toggle/apply `GameFoldOrientation` and rear-session window focus behavior. Keep the accepted edge-to-edge implementation, explicitly hide status/navigation bars with transient swipe reveal, request landscape, and route unsupported measured states to one deterministic unsupported-layout Compose page. Do not reinterpret approved pixel constants as dp.

- [ ] **Step 5: Add a source contract for the Activity boundary**

Extend the policy test (or add a narrowly scoped source test) to assert that MainActivity no longer calls `GameFoldOrientation.toggle/apply` or `VivoRearDisplayController`, and that manifest orientation/multi-window/PiP attributes match the product policy.

- [ ] **Step 6: Run tests and commit**

Run `GamePadWindowPolicyTest` and the affected `GameFoldPcFrontendPolicyTest`; update only old assertions that contradict the approved GamePad behavior. Expected: PASS.

```bash
git add app/src/main app/src/test
git commit -m "feat: enforce GamePad tablet window policy"
```

## Task 4: Implement 16:10 Fill and 16:9 Letterbox Geometry

**Files:**

- Create: `app/src/main/java/app/gamenative/gamepad/GamePadViewportPolicy.kt`
- Create: `app/src/test/java/app/gamenative/gamepad/GamePadViewportPolicyTest.kt`

- [ ] **Step 1: Write failing viewport tests**

Test exact approved outputs:

```kotlin
assertEquals(GamePadViewport(0, 0, 2304, 1440), fit(sourceWidth = 1280, sourceHeight = 800))
assertEquals(GamePadViewport(0, 72, 2304, 1296), fit(sourceWidth = 1920, sourceHeight = 1080))
```

Add invariants: the viewport is centered, remains within the full surface, reduces the source ratio with integer cross-products, and never returns a viewport larger than the window. Test invalid/non-positive source sizes fail closed instead of guessing.

- [ ] **Step 2: Run the scoped test and confirm failure**

Run `GamePadViewportPolicyTest`; expect unresolved policy symbols.

- [ ] **Step 3: Implement integer-rational fitting**

Implement a pure fit function using `Long` cross-products to avoid floating-point drift:

```kotlin
data class GamePadViewport(val left: Int, val top: Int, val width: Int, val height: Int)

fun fit(windowWidth: Int, windowHeight: Int, sourceWidth: Int, sourceHeight: Int): GamePadViewport {
    require(windowWidth > 0 && windowHeight > 0 && sourceWidth > 0 && sourceHeight > 0)
    // Width-limited or height-limited contain fit; center the unused dimension.
}
```

Use a contain fit only: no center-crop branch and no stretch branch.

- [ ] **Step 4: Run tests and commit**

Expected: exact 16:10 and 16:9 tests PASS.

```bash
git add app/src/main/java/app/gamenative/gamepad/GamePadViewportPolicy.kt app/src/test/java/app/gamenative/gamepad/GamePadViewportPolicyTest.kt
git commit -m "feat: add GamePad runtime viewport policy"
```

## Task 5: Lock Controller and HOME Geometry with Inverse Touch Mapping

**Files:**

- Create: `app/src/main/java/app/gamenative/gamepad/GamePadControllerGeometry.kt`
- Create: `app/src/test/java/app/gamenative/gamepad/GamePadControllerGeometryTest.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldControllerInput.kt`
- Modify: `app/src/test/java/app/gamenative/gamefold/ui/GameFoldControllerInputTest.kt`

- [ ] **Step 1: Write failing transform and rectangle tests**

Encode baseline group visual bounds and approved anchors. With source visual bounds `left: x=93..872, y=1277..2050` and `right: x=1608..2387, y=1277..2050`, assert:

```kotlin
assertEquals(0.72f, LEFT_TRANSFORM.scale)
assertPointClose(GamePadPoint(86f, 1361f), LEFT_TRANSFORM.toScreen(GamePadPoint(93f, 2050f)))
assertPointClose(GamePadPoint(2218f, 1361f), RIGHT_TRANSFORM.toScreen(GamePadPoint(2387f, 2050f)))
assertEquals(GamePadRect(1978f, 56f, 2248f, 133f), HOME_BOUNDS)
```

Use a small floating tolerance only for transform arithmetic; round visible draw rectangles and hit decisions through one documented function so visuals and hits cannot choose different pixels.

- [ ] **Step 2: Add failing hit-parity tests for every control family**

For D-pad, ABXY, ZL/L/R/ZR, plus/minus, both sticks, and L3/R3 dwell centers:

1. choose a baseline logical point that classifies as the intended control;
2. map it to GamePad screen space;
3. inverse-map it through the matching group transform;
4. assert the reused baseline classifier returns the same semantic control.

Add boundary tests one pixel inside/outside transformed enlarged hit regions. Assert a point outside both transformed groups and HOME is not consumed.

- [ ] **Step 3: Add the target cutout two-dimensional nonintersection test**

Represent the measured landscape cutout as `GamePadRect(0f, 694f, 82f, 746f)`. Assert every transformed controller effective hit region has no 2D intersection with it. Do not replace this with a simple `x >= 82` assertion: the approved lower controls may extend left of the inset x-coordinate while remaining vertically disjoint from the cutout.

- [ ] **Step 4: Run scoped tests and confirm failure**

Run `GamePadControllerGeometryTest` and `GameFoldControllerInputTest`; the new geometry tests must fail before implementation.

- [ ] **Step 5: Implement pure transforms and group-specific classification**

Implement:

```kotlin
enum class GamePadControlGroup { LEFT, RIGHT }

data class GamePadTransform(val scale: Float, val translateX: Float, val translateY: Float) {
    fun toScreen(point: GamePadPoint): GamePadPoint
    fun toSource(point: GamePadPoint): GamePadPoint
    fun mapRect(rect: GamePadRect): GamePadRect
}
```

Use the approved constants:

```kotlin
const val GROUP_SCALE = 0.72f
val LEFT_TRANSFORM = GamePadTransform(0.72f, 19.04f, -115f)
val RIGHT_TRANSFORM = GamePadTransform(0.72f, 499.36f, -115f)
val HOME_BOUNDS = GamePadRect(1978f, 56f, 2248f, 133f)
```

Refactor `GameFoldControllerInput.kt` so HOME classification is separable and the baseline classifier can be called for only the left or right control set. Do not change dead zones, enlarged baseline hit radii, button ordering, or dwell semantics.

- [ ] **Step 6: Run transform, hit-parity, and baseline semantic tests**

Expected: all new geometry tests and the unchanged semantic regression suite PASS.

- [ ] **Step 7: Commit geometry**

```bash
git add app/src/main/java/app/gamenative/gamepad app/src/main/java/app/gamenative/gamefold/ui/GameFoldControllerInput.kt app/src/test/java/app/gamenative/gamepad app/src/test/java/app/gamenative/gamefold/ui/GameFoldControllerInputTest.kt
git commit -m "feat: define GamePad controller and HOME geometry"
```

## Task 6: Add and Verify the Supplied GAMEPAD HOME Asset

**Files:**

- Source attachment: `/var/folders/31/fs4zzxdx3kq33jmgmnz8v_q0c5v4lz/T/codex-clipboard-25fc1f32-867c-410e-becc-9dd78c5b9332.webp`
- Create: `app/src/main/res/drawable-nodpi/gamepad_home_logo.webp`
- Modify: `app/src/test/java/app/gamenative/gamepad/GamePadControllerGeometryTest.kt`

- [ ] **Step 1: Add a failing asset contract test**

Read the committed resource and assert:

```kotlin
assertEquals(270, bitmap.width)
assertEquals(77, bitmap.height)
assertEquals("d2606470655c545f07e401c3297eea73c3c9ab6e0fc95677e43d11cf220069e2", sha256(file))
```

Also assert `HOME_BOUNDS.width == 270` and `HOME_BOUNDS.height == 77`.

- [ ] **Step 2: Run the test and confirm it fails because the committed asset is absent**

- [ ] **Step 3: Copy the supplied WebP byte-for-byte**

Use a direct file copy, not image generation, recompression, raster editing, or Android density scaling. Store it under `drawable-nodpi`.

- [ ] **Step 4: Verify source and committed hashes match**

Run:

```bash
shasum -a 256 "/var/folders/31/fs4zzxdx3kq33jmgmnz8v_q0c5v4lz/T/codex-clipboard-25fc1f32-867c-410e-becc-9dd78c5b9332.webp" app/src/main/res/drawable-nodpi/gamepad_home_logo.webp
```

Expected: both lines report the approved SHA-256.

- [ ] **Step 5: Run tests and commit**

```bash
git add app/src/main/res/drawable-nodpi/gamepad_home_logo.webp app/src/test/java/app/gamenative/gamepad/GamePadControllerGeometryTest.kt
git commit -m "feat: add approved GamePad HOME artwork"
```

## Task 7: Replace the Foldable Shell with the Full-Window GamePad Shell

**Files:**

- Modify: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldShellLayout.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcShellHost.kt`
- Create: `app/src/test/java/app/gamenative/gamepad/GamePadShellSourceContractTest.kt`
- Modify/add Robolectric shell tests if the existing test harness supports drawing and touch dispatch

- [ ] **Step 1: Write failing shell-structure and routing tests**

Require explicit APIs:

```kotlin
fun addContentView(view: View)
fun addOverlayView(view: View)
fun setContentSourceSize(width: Int, height: Int)
fun clearContentSourceSize()
```

Test these invariants:

- owned-page content lays out at `0,0,2304,1440`;
- runtime content uses `GamePadViewportPolicy` and the shell background remains black outside the viewport;
- overlay-role children lay out full-window;
- drawing order is content, overlay children, transformed left controls, transformed right controls, HOME;
- no foldable aperture/background/hinge/portrait branch is invoked;
- only controller/HOME hits are consumed; unrelated points reach the content child.

- [ ] **Step 2: Run the shell tests and confirm failure**

- [ ] **Step 3: Refactor measurement/layout around explicit child roles**

Keep the existing class name to avoid unrelated churn, but remove its 2480 × 2200 hardware-shell transform as the product layout authority. Store one content child and zero or more overlay children explicitly. For content with no source size, fill the window; for runtime content, apply the pure viewport. Lay overlay children full-window.

- [ ] **Step 4: Draw independent control groups and HOME**

After children draw, use `Canvas.save()`, `translate`, and `scale` separately for the left and right groups. Draw only each group's inherited PC 1.1 assets. Draw `gamepad_home_logo.webp` directly into `HOME_BOUNDS`; do not apply the group scale or density scaling.

- [ ] **Step 5: Route touch through the exact same transforms**

For every pointer:

1. test the exact HOME rectangle;
2. inverse-map through the left transform and call the left baseline classifier;
3. inverse-map through the right transform and call the right baseline classifier;
4. if no control is classified, leave the pointer unclaimed and pass it to normal ViewGroup dispatch.

Preserve existing pointer ownership, cancel/release behavior, haptic/audio calls, stick normalization, and L3/R3 timers. Do not create a second semantic controller implementation.

- [ ] **Step 6: Update the page host**

Keep `GameFoldPcShellHost.kt` available for inherited code, but bypass it for Home, settings, about, dialogs, and runtime-provision pages. These non-game surfaces must be directly touchable and must not draw or hit-test controller/HOME controls. XServer remains the only shell owner.

- [ ] **Step 7: Run shell, geometry, and input regression tests**

Run:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/11085429/Library/Android/sdk' GRADLE_USER_HOME='/Users/11085429/.gradle' ./gradlew :app:testLegacyReleaseUnitTest --tests 'app.gamenative.gamepad.*' --tests 'app.gamenative.gamefold.ui.GameFoldControllerInputTest' --tests 'app.gamenative.gamefold.ui.GameFoldPcFrontendPolicyTest' --offline --no-daemon --console=plain
```

Expected: PASS.

- [ ] **Step 8: Commit shell behavior**

```bash
git add app/src/main/java/app/gamenative/gamefold/ui app/src/test/java/app/gamenative/gamepad app/src/test/java/app/gamenative/gamefold/ui
git commit -m "feat: render full-window GamePad controller shell"
```

## Task 8: Integrate Runtime Viewport and Disable Rear/Fold Runtime Paths

**Files:**

- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Modify: `app/src/main/java/app/gamenative/PluviaApp.kt`
- Modify: `app/src/main/java/app/gamenative/MainActivity.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt`
- Create: `app/src/test/java/app/gamenative/gamepad/GamePadRuntimeIntegrationContractTest.kt`
- Modify: `app/src/test/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicyTest.kt`

- [ ] **Step 1: Write failing integration source-contract tests**

Assert that:

- XServer passes parsed `container.screenSize` width/height into `setContentSourceSize`;
- the XServer root is the content-role child and pause/loading/exit overlays are overlay-role children;
- `RearTouchInputRouter.register/unregister` is absent from the GamePad runtime path;
- `GameFoldOrientation.isPortrait` does not select runtime/gamepad/gyro behavior;
- `PluviaApp` does not install the vivo rear-display controller;
- library UI does not expose rear-display or portrait-toggle controls;
- the inherited `GameFoldRuntimeGamepadBridge` and HOME semantic event remain wired.

- [ ] **Step 2: Run the scoped contract test and confirm failure**

- [ ] **Step 3: Integrate runtime source-size parsing and explicit child roles**

Parse the existing `container.screenSize` value into positive integer width/height. Pass it to the shell before layout. If parsing fails, show the existing explicit runtime error flow; do not silently assume 16:9 or 16:10.

Add the game root using `addContentView`; add loading, pause, confirmation, and performance overlays using `addOverlayView` so they remain full-window while the game surface letterboxes.

- [ ] **Step 4: Remove product entry points to disabled features**

Remove rear router registration from XServer, rear controller installation from `PluviaApp`, rear session calls from MainActivity, and the rear/portrait actions from `OfflineLibraryScreen`. Preserve source files if deletion would broaden risk, but make them unreachable from the packaged product.

- [ ] **Step 5: Preserve gyro/input semantics in landscape**

Feed a fixed landscape product orientation into the existing gyro bridge/controller and retain the GameFold PC 1.1 sensitivity, ZL gate, axes, HOME behavior, and physical gamepad key mapping. Run existing gyro/runtime bridge tests without weakening their semantic assertions.

- [ ] **Step 6: Run integration and regression tests**

Run `GamePadRuntimeIntegrationContractTest`, `GameFoldPcFrontendPolicyTest`, `GameFoldRuntimeGamepadBridgeTest`, and the gyro processor/calibrator suites. Expected: PASS.

- [ ] **Step 7: Commit runtime integration**

```bash
git add app/src/main/java/app/gamenative app/src/test/java/app/gamenative
git commit -m "feat: integrate GamePad runtime surface and disable rear paths"
```

## Task 9: Run the Complete Test and Static Contract Gates

**Files:**

- Modify only files required by demonstrated failures; do not broaden scope.

- [ ] **Step 1: Scan for forbidden active identity/path references**

Run:

```bash
rg -n '/storage/emulated/0/GameFoldPC|/data/data/com\.gamefold\.pc|applicationId\s*=\s*"com\.gamefold\.pc"|android:label="GameFold PC"' app/src/main app/build.gradle.kts
rg -n 'GameFoldOrientation\.(toggle|apply)|RearTouchInputRouter\.(register|unregister)|VivoRearDisplayController\.(install|setSessionEnabled|onFrontWindowFocusChanged)' app/src/main
```

Expected: no active GamePad entry-point matches. Any allowed historical/internal match must be documented in the candidate evidence with a reason and a test proving it is unreachable.

- [ ] **Step 2: Run all applicable Legacy Release unit tests**

Run:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/11085429/Library/Android/sdk' GRADLE_USER_HOME='/Users/11085429/.gradle' ./gradlew :app:testLegacyReleaseUnitTest --offline --no-daemon --console=plain
```

Expected: all applicable tests pass; do not merely run the four new test classes.

- [ ] **Step 3: Self-review against the approved spec**

Check every section of the design doc against code/tests. In particular, verify exact HOME hit replacement, visual/touch transform parity, game-only overlay/non-game touch routing, 70% Home cartridges with unchanged title typography and aligned Header edges, 576dp acceptance, 16:9 letterbox, GamePadPC-only resource root, and no rear/portrait entry points.

- [ ] **Step 4: Scan for unfinished implementation markers and type drift**

Run:

```bash
rg -n 'TODO|TBD|FIXME|PLACEHOLDER|throw NotImplementedError|error\("not implemented' app/src/main app/src/test
./gradlew :app:compileLegacyReleaseKotlin :app:compileLegacyReleaseJavaWithJavac --offline --no-daemon --console=plain
```

Expected: no newly introduced placeholder; Kotlin and Java compile against consistent APIs.

- [ ] **Step 5: Commit any narrowly required test-alignment fixes**

```bash
git add app/src/main app/src/test
git commit -m "test: lock GamePad PC release contracts"
```

Skip the commit if the worktree is already clean.

## Task 10: Build and Inspect the Signed Release APK

**Files:**

- Build output: `app/build/outputs/apk/legacy/release/app-legacy-release.apk`
- Create candidate evidence: `releases/candidates/GamePad-PC-V1.0-RC1/README.md`
- Create archived APK: `releases/candidates/GamePad-PC-V1.0-RC1/GamePad-PC-V1.0-RC1-10001.apk`

- [ ] **Step 1: Build the Release variant**

Run:

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/11085429/Library/Android/sdk' GRADLE_USER_HOME='/Users/11085429/.gradle' ./gradlew :app:assembleLegacyRelease --offline --no-daemon --console=plain
```

Expected: BUILD SUCCESSFUL and the APK exists at the declared output path.

- [ ] **Step 2: Inspect package/version/label/manifest**

Run with Android SDK 37 tools:

```bash
/Users/11085429/Library/Android/sdk/build-tools/37.0.0/aapt2 dump badging app/build/outputs/apk/legacy/release/app-legacy-release.apk
/Users/11085429/Library/Android/sdk/build-tools/37.0.0/apkanalyzer manifest print app/build/outputs/apk/legacy/release/app-legacy-release.apk
```

Expected: `com.gamepad.pc`, versionCode `10001`, versionName `1.0`, application label `GamePad PC`, fixed landscape, no rear Activity, and `android:debuggable` absent or false.

- [ ] **Step 3: Verify signing and record hashes**

Run:

```bash
/Users/11085429/Library/Android/sdk/build-tools/37.0.0/apksigner verify --verbose --print-certs app/build/outputs/apk/legacy/release/app-legacy-release.apk
shasum -a 256 app/build/outputs/apk/legacy/release/app-legacy-release.apk app/src/main/res/drawable-nodpi/gamepad_home_logo.webp
```

Expected: APK signature verifies; the HOME hash is the approved value. Compare the signing certificate digest with the GameFold PC 1.1 baseline certificate and require equality.

- [ ] **Step 4: Archive RC1 without promoting a baseline**

Create `releases/candidates/GamePad-PC-V1.0-RC1/`, copy the exact APK into it, and write `README.md` containing source commit, branch, build command, package/version, signature digest, APK SHA-256, HOME SHA-256, test result, target serial/model, and explicit remaining runtime acceptance gates.

- [ ] **Step 5: Verify archived/build bytes and commit evidence**

Run:

```bash
cmp app/build/outputs/apk/legacy/release/app-legacy-release.apk releases/candidates/GamePad-PC-V1.0-RC1/GamePad-PC-V1.0-RC1-10001.apk
shasum -a 256 releases/candidates/GamePad-PC-V1.0-RC1/GamePad-PC-V1.0-RC1-10001.apk
git add releases/candidates/GamePad-PC-V1.0-RC1
git commit -m "release: archive GamePad PC 1.0 RC1"
```

Expected: `cmp` exits 0. Do not create a frozen baseline tag or update a GameFold PC current-baseline pointer.

## Task 11: Install and Verify on the Connected vivo Tablet

**Files:**

- Candidate APK: `releases/candidates/GamePad-PC-V1.0-RC1/GamePad-PC-V1.0-RC1-10001.apk`
- Device evidence/screenshots: `releases/candidates/GamePad-PC-V1.0-RC1/device-evidence/`

- [ ] **Step 1: Reconfirm the exact connected target and coexistence precondition**

Run:

```bash
/Users/11085429/Library/Android/sdk/platform-tools/adb devices -l
/Users/11085429/Library/Android/sdk/platform-tools/adb shell getprop ro.product.model
/Users/11085429/Library/Android/sdk/platform-tools/adb shell pm list packages | rg 'com\.gamefold\.pc|com\.gamepad\.pc'
```

Expected: serial `403G8G00BM611V0`, model `iPA2693`; existing GameFold PC state is recorded and not changed.

- [ ] **Step 2: Install with the vivo confirmation workflow**

Invoke the required `install-apk-vivo` skill and install the archived candidate with:

```bash
PATH='/Users/11085429/Library/Android/sdk/platform-tools':$PATH python3 /Users/11085429/.codex/skills/install-apk-vivo/scripts/install_apk_vivo.py '/Users/11085429/Documents/Game Fold/gamepad-pc-v1-candidate/releases/candidates/GamePad-PC-V1.0-RC1/GamePad-PC-V1.0-RC1-10001.apk'
```

Do not uninstall or clear `com.gamefold.pc` or `com.gamepad.pc`.

- [ ] **Step 3: Verify installed identity, version, install time, and run-as rejection**

Run `dumpsys package com.gamepad.pc`, record versionName/versionCode/firstInstallTime/lastUpdateTime, verify both packages coexist, and run:

```bash
/Users/11085429/Library/Android/sdk/platform-tools/adb shell run-as com.gamepad.pc id
```

Expected: `run-as` rejects the package as non-debuggable.

- [ ] **Step 4: Verify independent external storage behavior**

Launch GamePad PC and exercise its existing provisioning flow. Verify any created/read public root is `/storage/emulated/0/GamePadPC/`; inspect logs for absence of GameFoldPC fallback. Do not copy or migrate the GameFold PC directory unless separately authorized.

- [ ] **Step 5: Capture full-window page/runtime screenshots**

Capture display 0 screenshots for Home, settings, about, runtime provisioning, a 16:10 game surface, and a 16:9 game surface. Record `wm size`, current rotation, window bounds, and system-bar visibility alongside them.

For image-based geometry checks, measure:

- full application surface `0,0..2304,1440`;
- left/right outer anchors 86px and bottom anchor 79px;
- GAMEPAD bounds `1978,56..2248,133`;
- 16:9 content `0,72..2304,1368` with black 72px bands;
- no visible foldable border/aperture/hinge/edge light.

- [ ] **Step 6: Exercise real touch and controller acceptance**

On device, verify every D-pad and ABXY direction/button, ZL/L/R/ZR, plus/minus, both sticks, L3/R3 dwell, multi-touch combinations, haptic, sound, release/cancel, game-surface pass-through, physical controller routing, gyro, and HOME inside the game runtime. Confirm Home/settings contain no overlay and use touch, and confirm the former 400 × 155 HOME area outside the new 270 × 77 rect no longer triggers HOME.

- [ ] **Step 7: Verify unsupported product paths are unavailable**

Attempt portrait, multi-window, PiP, and rear-display/rear-touch entry paths. Expected: portrait/rear paths are unavailable; multi-window/PiP cannot enter a partially transformed controller shell; unsupported size handling is deterministic.

- [ ] **Step 8: Pull the installed APK and compare bytes**

Resolve the installed base path, pull it to `/private/tmp/gamepad-pc-installed-base.apk`, then run `cmp` and SHA-256 against the archived RC1.

Expected: byte-for-byte equality and matching hash.

- [ ] **Step 9: Record acceptance evidence without prematurely promoting**

Append device commands/results, screenshot filenames, touch checklist, installed APK comparison, and any remaining manual game-specific acceptance to the RC1 evidence. Commit evidence files only; keep RC1 labeled a candidate until the user confirms visible/touch/game acceptance.

```bash
git add releases/candidates/GamePad-PC-V1.0-RC1/device-evidence releases/candidates/GamePad-PC-V1.0-RC1/README.md
git commit -m "test: record GamePad PC RC1 device acceptance"
```

## Task 12: Final Review and Handoff

**Files:**

- Review all changes from `gamefold-pc-v1.1-baseline..HEAD`
- Review `releases/candidates/GamePad-PC-V1.0-RC1/README.md`

- [ ] **Step 1: Invoke `superpowers:verification-before-completion`**

Rerun fresh tests, build/package/signature checks, archive/build/device byte comparisons, and the clean-worktree check. Do not rely on earlier terminal output for completion claims.

- [ ] **Step 2: Review scope and source preservation**

Run:

```bash
git diff --stat gamefold-pc-v1.1-baseline..HEAD
git diff --check gamefold-pc-v1.1-baseline..HEAD
git status --short
git rev-parse gamefold-pc-v1.1-baseline
```

Expected: no whitespace errors, clean product worktree, frozen tag unchanged.

- [ ] **Step 3: Report evidence at the correct confidence level**

The handoff must include:

- exact source commit/branch/worktree;
- clickable archived APK and evidence paths;
- package/version/signature/APK hash/HOME hash;
- automated test count/result;
- connected-device identity/install time/coexistence/run-as result;
- build-to-archive-to-installed byte comparison;
- page/runtime screenshot list and measured geometry;
- completed real-touch/game checklist;
- any remaining user-visible acceptance gate.

Do not call the candidate a frozen baseline until the user explicitly approves it after device-visible/touch acceptance.
