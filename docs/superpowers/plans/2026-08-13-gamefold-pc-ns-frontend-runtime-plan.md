# GameFold PC NS Frontend and Runtime-Surface Plan

> For agentic workers: execute this plan task by task and keep the verification evidence with the candidate build.

**Goal:** Based on the frozen PC `0.6.6` / NS UI R15 / `versionCode 10072` baseline, make GameFold PC use the GameFold NS visual language and interaction shell while leaving the PC runtime, click-time resource loading, and GameFold FC completely untouched.

**Baseline:** `releases/baselines/V0.6.6-NS-UI-R15-10072/` with APK SHA-256 `dc79f8c3d3e21a9bba841c449c4871a5de5ab5790b2672d49c15e48141ad1e39`; source commit `d0cffbd` records the freeze.

**Architecture:** Keep the existing PC Compose/Nav/Hilt/Winlator launch pipeline. Add a reusable PC `GameFoldShellLayout` host around each PC front-end surface that needs the NS two-stage geometry. The host owns the upper display aperture and persistent lower controller; existing XServer runtime hosting remains the only runtime surface owner. Home/settings/loading use the same host without importing NS emulator classes or changing PC launch APIs.

**Tech Stack:** Kotlin, Jetpack Compose, existing Android `GameFoldShellLayout`, `ComposeView`/`AndroidView` bridge, existing `PerformanceHudView` and `FrameRating`, JUnit/Robolectric unit tests, Gradle offline release build.

## Global constraints

1. Only edit files under `gamefold-pc-ns-ui-integration`. Do not read, edit, build, install, or test `gamefold-fc`.
2. Do not change PC runtime behavior: permission and processor checks remain first-launch checks; Proton/ImageFS and deferred archives continue loading when a game is opened, not at app start.
3. Do not overwrite the frozen APK, its metadata, or its source commit. Every candidate uses a distinct package/version identity.
4. Reuse NS source-of-truth assets and geometry where available; do not invent a second controller or font system.
5. Keep all state transitions inside the upper display aperture. The lower controller remains visible from the first app frame through home, loading, settings, and game runtime.

## Task 1: Lock the frontend contract before production UI changes

**Files:**

- Add `app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicy.kt`.
- Add `app/src/test/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicyTest.kt`.
- Add/update `docs/gamefold/GAMEFOLD_PC_V0_6_6_NS_UI_R15_ACCEPTANCE.md`.

**Contract:**

- local-shell PC screens use the NS shell (`display aperture + persistent controller`);
- loading content is display-aperture-only and uses the NS retro card/progress language;
- FPS is an integer, no-panel, 3x5 pixel glyph with NS light ink and hard shadow;
- non-local-shell/legacy behavior remains unchanged;
- Home uses NS cartridge layout but retains the PC square-cover source and PC path/link semantics;
- add-game copy says GameFold PC paths; download URL remains unchanged;
- FC is outside the contract.

**TDD:** Write the policy tests first. The tests must cover shell selection, overlay confinement, integer FPS rounding/clamping, and preservation of the legacy path. Run the focused test and record the red/green transition.

## Task 2: Build one reusable PC shell host

**Files:**

- Add `app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcShellHost.kt`.
- Add/update host tests under `app/src/test/java/app/gamenative/gamefold/ui/`.
- Keep `app/src/main/java/app/gamenative/gamefold/ui/GameFoldShellLayout.kt` as the geometry/input source of truth.

**Implementation:**

- Bridge a nested `ComposeView` into `GameFoldShellLayout` with `AndroidView`.
- Measure the child to the upper display aperture only.
- Keep the controller layer drawn and interactive for the full host lifetime.
- Use `rememberUpdatedState`/stable callbacks so recomposition does not recreate the shell or leak a `ComposeView`.
- Do not nest a second shell around the existing `XServerScreen` shell.

**Acceptance:** A host-level test proves the child is constrained to the display stage and the controller layer remains present; a static source check proves `XServerScreen` remains the sole runtime shell owner.

## Task 3: Port the home and settings surfaces to the NS design system

**Files:**

- Update `app/src/main/java/app/gamenative/ui/screen/HomeScreen.kt`.
- Update `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt`.
- Update `app/src/main/java/app/gamenative/ui/screen/settings/SettingsScreen.kt` only for the PC shell entry/layout; keep settings semantics intact.
- Update related `AddGameCartridge`, `PixelCartridge`, details, and string resources only where required for PC copy/geometry.

**Implementation:**

- Wrap PC home/settings content in `GameFoldPcShellHost` when `GAMEFOLD_PC_LOCAL_SHELL` is enabled.
- Reproduce NS cream display background, pixel logo/header treatment, icon-button geometry, cartridge frame, four-column display-grid rhythm, centered pixel title wrapping, and clipped scrolling inside the upper display.
- Use the existing PC square cover bitmap as the cartridge image; do not replace the data source.
- Keep long-press game details and add-game flow; restyle their content/dialogs to the NS card language.
- Change only PC add-game path wording; keep download link behavior unchanged.
- Preserve PC navigation destinations and game IDs/directories.

**Acceptance:** Screenshot evidence at the same aspect ratio shows the controller unchanged below the display, no content drawn behind it, four-column NS-like cartridge rhythm, and working add/long-press flows.

## Task 4: Port loading states into the upper display aperture

**Files:**

- Update `app/src/main/java/app/gamenative/ui/components/BootingSplash.kt`.
- Update `app/src/main/java/app/gamenative/ui/component/dialog/LoadingDialog.kt`.
- Update `app/src/main/java/app/gamenative/PluviaMain.kt` only at the overlay composition sites.

**Implementation:**

- Replace the full-screen modern gradient/particle splash for local-shell PC with an upper-display NS-style retro loading card: square cover, title, `加载中...`/dynamic state text, segmented pixel progress, and dynamic status/progress retained.
- Keep the lower controller visible while loading.
- Constrain every loading overlay to the display aperture; no full-screen `Dialog` window for local-shell loading.
- Keep click-time runtime provisioning and existing launch callbacks unchanged.
- Legacy/non-local-shell splash behavior remains as-is.

**Acceptance:** Launching a game never hides the controller; only the upper display changes; progress/status updates continue; failed launch still returns to the same home shell with actionable error/retry.

## Task 5: Port the runtime FPS HUD to the NS pixel treatment

**Files:**

- Update `app/src/main/java/app/gamenative/ui/widget/PerformanceHudView.kt`.
- Update only the style hookup in `app/src/main/java/app/gamenative/ui/screen/XServerScreen.kt` if necessary.
- Add `app/src/test/java/app/gamenative/ui/widget/PerformanceHudViewContractTest.kt` or a pure formatter test.

**Implementation:**

- Preserve `FrameRating` as the value provider and preserve existing drag/quick-menu/input behavior.
- Render rounded integer FPS using the NS `PixelNumberView` glyph geometry (3x5 bitmap, light pixel ink, hard dark shadow, no dark rectangular panel).
- Keep placement at the display upper-left with NS pixel margins; never render over the lower controller.
- Avoid importing NS emulator runtime classes; copy only the small visual contract or share a PC-local equivalent.

**Acceptance:** A running game HUD shows `0`, `27`, `66`, etc. as integer pixel glyphs; no decimal/panel remains; FPS updates continue without changing the PC frame pipeline.

## Task 6: Regression and artifact gates

**Automated checks:**

1. Focused policy/host/loading/FPS tests pass.
2. Existing PC unit tests pass with no new failures beyond the frozen baseline ledger.
3. `git diff --check` passes.
4. Static scope guard confirms no path under `gamefold-fc` changed.
5. APK metadata guard confirms candidate package, version, signer, and SHA differ from but are traceable to the frozen `10072` baseline.

**Build:** Use the PC legacy release task offline with an explicitly distinct RC application ID/version code. Never build with the baseline package identity.

**Device/screenshot acceptance:**

- install the distinct RC on the connected test phone only after the APK metadata/hash guard passes;
- cold start: permission/processor checks first, then home shell;
- home: persistent controller, display-only content, NS cartridge/header/colour/spacing;
- add game, long-press settings, settings return;
- launch: upper-display retro card, dynamic status/progress, controller visible;
- runtime: PC game surface in upper display, integer pixel FPS at upper-left, controller input unchanged;
- back/quick menu, orientation, dual-screen/rear-touch, 1067x600/GTA/LSFG/input smoke checks;
- second launch remains click-time resource loading and does not introduce startup provisioning;
- capture screenshots and log package/version/SHA/install time.

**Rollback:** If any acceptance gate fails, uninstall only the distinct RC, restore the source to the frozen baseline commit if needed, and use the immutable APK under `releases/baselines/V0.6.6-NS-UI-R15-10072/`. Do not delete or modify the baseline artifact.

