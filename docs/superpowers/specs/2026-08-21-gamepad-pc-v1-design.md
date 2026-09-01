# GamePad PC V1 Product Design

**Date:** 2026-08-21

**Status:** Written specification confirmed by the user on 2026-08-21; frozen implementation input

**Source baseline:** `gamefold-pc-v1.1-baseline` / `fee253141f67373744893712d6b3a38114c7e4f2`

## 1. Objective

Create a new Android product named **GamePad PC** from the frozen GameFold PC 1.1 baseline. GamePad PC targets a small landscape tablet with a physical `2304 x 1440` display and replaces the GameFold hardware-shell presentation with a full-screen 16:10 product shell and two compact overlaid controller groups.

GamePad PC is an independent product. It must not modify the frozen GameFold PC 1.1 tag, package, data, external resource directory, or release artifact.

## 2. Scope

### Included

- Independent source worktree and Git branch derived from `gamefold-pc-v1.1-baseline`.
- Independent package identity, internal data, external public data, version identity, and release APK.
- Full-screen `2304 x 1440` layout on every GamePad PC-owned page.
- Independently positioned left controller group, right controller group, and GAMEPAD HOME logo.
- Exact visual and touch geometry defined in this document.
- Game content aspect-ratio handling for 16:10 and 16:9 output.
- Preservation of the accepted GameFold PC 1.1 runtime and controller behavior except where this document explicitly changes product-shell behavior.
- Release-only candidate, device installation, and target-tablet verification.

### Excluded

- Changes to GameFold PC 1.1 or its frozen release/tag.
- Data migration or automatic sharing between GameFold PC and GamePad PC.
- Portrait, split-screen, freeform/multi-window, picture-in-picture, fold-state, rear-display, or rear-touch support.
- New controller artwork other than the supplied GAMEPAD HOME logo.
- Unrelated runtime, gyro, save-import, container, graphics-driver, or game-compatibility changes.

## 3. Target Device Evidence

ADB read-only inspection identified the connected target as:

- Manufacturer/brand: vivo
- Model/device: `iPA2693` / `DPD2653`
- Android: 17, SDK 37
- SoC: Qualcomm `SM8850`
- ABI: `arm64-v8a`
- Physical display: `1440 x 2304`; landscape application window: `2304 x 1440`
- Density: 400 dpi (`2.5 px/dp`), approximately `576dp` smallest width
- Navigation: gesture navigation (`navigation_mode=2`)
- Landscape rotation: `ROTATION_90`
- Landscape left display cutout inset: `82px`
- Physical rounded-corner radius reported by WindowManager: `117px`

The existing PC shell's `smallestScreenWidthDp >= 600` gate would reject this device. GamePad PC must own a separate support policy that accepts the verified `576dp`, `2304 x 1440` target instead of reusing the PC foldable-device gate.

## 4. Product Identity and Storage

- User-visible application name: `GamePad PC`
- Android application ID: `com.gamepad.pc`
- Initial `versionName`: `1.0`
- Initial `versionCode`: `10001`
- Internal application data: Android-owned data for `com.gamepad.pc`
- External public resource root: `/storage/emulated/0/GamePadPC/`
- Signing: reuse the established GameFold PC signing certificate
- Launcher icon: reuse GameFold PC artwork without visual modification
- Android/system splash artwork: reuse GameFold PC artwork without visual modification
- Launcher title and visible product text: `GamePad PC`

All hard-coded or configured `/storage/emulated/0/GameFoldPC/` product paths that belong to application-owned resources must be redirected to `/storage/emulated/0/GamePadPC/`. GamePad PC must not silently fall back to the GameFold PC directory.

## 5. Release Contract

The first candidate and every subsequent GamePad PC build are signed release builds.

- `android:debuggable=false`
- `run-as com.gamepad.pc` must be rejected
- No internal run-as/debuggable candidate is produced
- GameFold PC's optional run-as override must not become enabled through the GamePad PC build defaults
- The final APK must report package `com.gamepad.pc`, version `1.0 / 10001`, and the expected signing certificate

Release verification must distinguish artifact identity from runtime acceptance. A successful build, signature, or hash does not by itself prove layout, touch, game, or device behavior.

## 6. Window and Page Shell

Every GamePad PC-owned page uses the entire landscape application window:

- Bounds: `left=0`, `top=0`, `right=2304`, `bottom=1440`
- Application display radius: `0`
- Application display border: `0`
- No GameFold PC 16:9 aperture, cover, hinge, or hardware-shell background
- Edge-to-edge immersive presentation with status and navigation bars hidden
- Backgrounds and game surfaces may draw behind the physical cutout and physical rounded corners
- Important page content and interactive UI must respect physical insets even though the background fills the full window

GamePad PC is landscape-only. Portrait, multi-window, picture-in-picture, and unsupported window sizes must fail closed into a clear unsupported-layout state rather than fall back to the GameFold PC shell.

The controller overlay is game-runtime-only. It is mounted on the XServer/game surface and is absent from Home, settings, about, provisioning, dialogs, and every other non-game page; those pages use direct touch interaction. System permission pages, package installers, and other external Android activities also do not receive the overlay.

## 7. Content Aspect Ratio

The application surface always occupies the full `2304 x 1440` window. Content inside that surface follows these rules:

- 16:10 content fills `2304 x 1440`.
- 16:9 content preserves its full aspect ratio with no stretch and no crop.
- At the target resolution, 16:9 content is `2304 x 1296`, centered vertically at `y=72..1368`.
- The remaining `72px` at the top and `72px` at the bottom are black letterbox regions.

The black regions are content letterboxing, not a return of the removed GameFold display border or aperture.

## 8. Controller Groups

The current PC implementation uses one `2480 x 2200` transform for the display shell and all controls. GamePad PC must decouple the display surface, left control group, right control group, and HOME logo into independent transforms.

Both controller groups are rendered and hit-tested only while a game runtime surface is active.

### Left group

Members: `ZL`, `L`, minus, left stick, and D-pad.

- Visual geometry: GameFold PC 1.1 geometry scaled uniformly to `72%`
- Touch geometry: the already-enlarged GameFold PC 1.1 touch geometry scaled uniformly to `72%`
- Group outer anchor: `86px` from the left edge
- Group outer anchor: `79px` from the bottom edge

The target's left cutout consumes `82px`, leaving a `4px` gap between the cutout inset and the specified `86px` group anchor. Integer rounding must never move the group or its effective touch targets into the cutout region.

### Right group

Members: `R`, `ZR`, plus, right stick, and ABXY.

- Visual geometry: GameFold PC 1.1 geometry scaled uniformly to `72%`
- Touch geometry: the already-enlarged GameFold PC 1.1 touch geometry scaled uniformly to `72%`
- Group outer anchor: `86px` from the right edge
- Group outer anchor: `79px` from the bottom edge

### Preserved controller behavior

- ABXY semantic order and pressed assets from PC 1.1
- D-pad classification
- Stick travel and normalization
- Stick dead zone
- L3/R3 dwell behavior
- Multi-touch occupancy rules
- Haptic and sound feedback
- Press/release cancellation behavior
- Controller-to-Compose navigation and confirm routing

Only transformed controller hit regions intercept touches. Other points continue to the underlying page or game surface.

## 9. GAMEPAD HOME Control

Use the supplied transparent WebP as the top-right HOME control.

- Source dimensions: `270 x 77px`
- SHA-256: `d2606470655c545f07e401c3297eea73c3c9ab6e0fc95677e43d11cf220069e2`
- Visual bounds: `left=1978`, `top=56`, `right=2248`, `bottom=133`
- Right margin: `56px`
- Top margin: `56px`
- HOME hit bounds: exactly the same `270 x 77px` rectangle
- The former PC `400 x 155px` HOME hit target is not retained
- HOME button semantics, sound, and haptic feedback remain unchanged

GAMEPAD HOME is rendered and hit-tested only inside the game runtime. Returning to Home removes it together with both controller groups.

The asset must be copied byte-for-byte into the GamePad PC source resources during implementation, and its committed hash must match this specification.

## 10. Retained and Disabled Product Behavior

### Retained from GameFold PC 1.1

- Container creation and editing
- Local runtime/resource provisioning
- Graphics driver behavior
- Game launch and runtime behavior
- GTA5 ZL-gated gyro behavior and accepted sensitivity
- Save auto-import behavior
- Settings and about flows, with product text changed to GamePad PC
- Existing controller semantic input, audio, and haptic behavior

### Disabled for GamePad PC

- vivo rear-display session and rear Activity
- Rear-touch input routing
- Front edge lights associated with rear controls
- Foldable orientation toggles
- Portrait controller layout and hinge rendering
- GameFold foldable-device support gate

The source may preserve disabled code where removing it would create unrelated risk, but GamePad PC packaging and runtime entry points must make the features unreachable and tests must enforce that boundary.

## 11. Component Boundaries

Implementation should keep the following responsibilities independently testable:

1. **GamePad product contract** — package, version, label, paths, release/debug contract, and disabled features.
2. **GamePad window policy** — exact target window, immersive system bars, orientation, supported-layout decision, and physical-inset handling.
3. **GamePad viewport policy** — 16:10 fill and 16:9 centered letterbox geometry.
4. **GamePad controller geometry** — fixed reference dimensions, group transforms, inverse touch transforms, and HOME bounds.
5. **GamePad shell rendering** — game-runtime child surface followed by left/right controls and HOME overlay; non-game pages bypass the shell.
6. **Baseline input semantics** — reused PC 1.1 classification and feedback behavior without product-layout constants leaking into semantic input.

Do not implement the new form as scattered special cases inside the existing PC global shell transform. The display and each control group need explicit boundaries so visible and actual touch geometry cannot drift apart.

## 12. Failure and Fallback Behavior

- If the application window is not the supported landscape form, do not draw a partially transformed controller; show a deterministic unsupported-layout page.
- If immersive mode has not produced a `2304 x 1440` content window, do not silently reinterpret the approved pixel coordinates as dp.
- If the GAMEPAD asset dimensions or hash differ, fail the asset/geometry test rather than rescale it implicitly.
- If a 16:9 surface is requested, letterbox; never stretch or crop as a hidden fallback.
- If GamePad external resources are absent, use the existing explicit provisioning/error flow pointed at `GamePadPC`; never read `GameFoldPC` as fallback.
- If controller transform inversion cannot classify a touch deterministically, treat it as non-controller input and pass it through.

## 13. Verification and Acceptance

### Automated tests

- Product identity and external-root contract tests
- Release/run-as/debuggable contract tests
- Exact full-window and supported-device policy tests, including `576dp`
- Exact left/right group visual transforms and anchors
- Exact transformed touch geometry for D-pad, ABXY, shoulders, plus/minus, sticks, and L3/R3
- Exact GAMEPAD bounds and `270 x 77px` HOME hit target
- Game-only overlay policy and non-game touch-only page contract
- Home card scale of `70%`, unchanged `12sp` title size, and Header edges aligned to the scaled cartridge outer edges
- Tests that visible geometry and touch geometry use the same transform
- 16:10 viewport fill test
- 16:9 `2304 x 1296 @ y=72` letterbox test
- No portrait/rear-display/multi-window product-entry test
- Existing PC 1.1 semantic controller regression suite

### Build and package checks

- Full applicable unit test suite passes
- Signed release APK builds successfully
- `aapt` reports `com.gamepad.pc`, `1.0 / 10001`
- Manifest reports `android:debuggable=false`
- APK signature verification passes
- APK and supplied GAMEPAD asset hashes are recorded

### Connected-device checks

- Install to connected vivo `iPA2693` without touching GameFold PC
- Confirm GameFold PC and GamePad PC can coexist
- Confirm independent internal package and `/storage/emulated/0/GamePadPC/` usage
- Confirm all owned pages occupy `2304 x 1440` in immersive landscape
- Capture and inspect screenshots for Home, settings, about, game/runtime, 16:10, and 16:9 cases
- Confirm Home/settings/about contain no controller or GAMEPAD HOME overlay and remain directly touchable
- Confirm Home cartridges are `70%` of the previous size, title typography remains `12sp`, and Header groups align to the first/last cartridge outer edges
- Verify game-runtime left/right `86px`, bottom `79px`, and GAMEPAD top/right `56px` geometry
- Verify the left group remains outside the `82px` cutout inset
- Exercise every controller control, enlarged transformed hit zones, multi-touch, sticks, L3/R3, sounds, haptics, and HOME
- Confirm 16:9 content has `72px` top/bottom letterbox regions with no stretch or crop
- Confirm portrait, rear-display, and multi-window paths are unavailable
- Confirm `run-as com.gamepad.pc` is rejected
- Pull the installed APK and compare it byte-for-byte with the archived candidate

### Acceptance boundary

Build, archive, hash, and installation prove artifact identity. Final product acceptance additionally requires user-visible layout and real touch/game verification on the target tablet. The candidate must not be promoted as a frozen GamePad PC baseline until those checks pass.
