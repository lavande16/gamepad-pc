# GameFold PC × NS 2.9 Visual Parity Loop

## Goal

Starting from the immutable GameFold PC 0.6.6 NS-UI R15 artifact (versionCode 10072), make the PC foreground UI behave and render like the installed GameFold NS 2.9 R2 product. The loop is: identify a scene from source, capture the same scene on both products, compare geometry/style/interaction, aggregate the differences, implement one coherent change set, build a separately identified PC candidate, reinstall without clearing data, recapture, and repeat until the required scenes pass the parity gates.

Only the GameFold PC frontend is in scope. The GameFold FC/NES product and its runtime remain untouched and are checked by a path-level guard.

## Frozen inputs

- PC baseline APK: `releases/baselines/V0.6.6-NS-UI-R15-10072/GameFold-PC-V0.6.6-NS-UI-R15-10072.apk`
- PC baseline SHA-256: `dc79f8c3d3e21a9bba841c449c4871a5de5ab5790b2672d49c15e48141ad1e39`
- PC baseline identity: `com.gamefold.pc.v066nsrc`, `0.6.6-gf-ns-r15`, `10072`
- NS reference APK: installed `com.switch.fold.baseline.lite`, GameFold 2.9, SHA-256 `156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292`
- Device: vivo X Fold5, display `4630946685006800003`, 2480×2200 landscape capture

The PC keeps click-time Proton/ImageFS/resource loading and all PC launch/container semantics. No startup provisioning rewrite is part of this visual pass.

## Scene matrix

| ID | Scene | NS source anchor | PC source anchor | Required evidence |
|---|---|---|---|---|
| S0 | Permission and processor gate | `GameFoldAssemblyActivity`, pixel dialog sizing | `GameFoldPcRuntimeProvisionGate` | source comparison; existing permission capture; no destructive permission reset |
| S1 | Home, cold and focused | `GamesFragment`, `GameAdapter`, `card_game_grid.xml`, `GameFoldShellLayout` | `HomeScreen`, `OfflineLibraryScreen`, `GameFoldShellLayout` | home screenshot, focus/DPAD trace, bounds/colors |
| S2 | Global settings landing | `HomeSettingsFragment`, pixel cards/styles | `SettingsScreen` | same-shell screenshot and focus trace |
| S3 | Advanced settings | NS settings sub-screens/styles | `SettingsScreen`/settings groups | screenshot and scroll/focus trace |
| S4 | Add-game dialog | `GameFoldAssemblyActivity` dialog layouts | `OfflineLibraryScreen`, `GameFoldPcPixelDialog` | screenshot, copy/path/button action trace |
| S5 | Per-game properties | `GamePropertiesFragment` | PC game detail route | screenshot, long-press and DPAD trace |
| S6 | Container/game settings | NS property/settings fragments | `ContainerConfigDialog` | screenshot confined to display aperture; tab/focus trace |
| S7 | Loading early/late | NS emulation/loading card and progress | `BootingSplash`, `GameFoldGameLoadingOverlayView`, `LoadingDialog` | two timed screenshots, selected square cover, dynamic status |
| S8 | Runtime and FPS | NS `PixelNumberView`, `EmulationActivity` | `XServerScreen`, `PerformanceHudView`, `FrameRating` | 15/30/55s runtime screenshots and nonzero FPS evidence |
| S9 | Pause/back/exit warning | `dialog_emulation_exit_pixel.xml` | PC back-warning/quick-menu path | screenshot and two-back behavior trace |
| S10 | Error/retry/black bootstrap | NS error/empty dialogs | PC loading/error/container paths | screenshot; no silent or full-screen loading |
| S11 | Controller, orientation, dual-screen | NS `GameFoldShellLayout`, rear-touch lifecycle | PC shell/input/display adapters | input trace and landscape/portrait/display evidence |

## Pixel comparison contract

Every capture records package/version, scene ID, display ID, orientation, timestamp, and SHA-256. The comparison is performed on the display aperture and the full shell separately so game artwork differences do not hide layout defects.

Required thresholds for a scene to pass:

1. The upper display aperture and lower controller geometry match the NS shell within 4 px at the normalized 2480×2200 reference size.
2. Cartridge/dialog/card bounds and inter-item gaps match within 6 px; square PC artwork remains square and has no rounded crop.
3. NS palette tokens are used for surfaces, ink, border, focus, action, and shadows; sampled stable pixels differ by no more than 8 per RGB channel except for PC artwork and dynamic game frames.
4. Text/icon baseline and scale are within 1 px after density normalization, or use the exact NS bitmap asset/renderer.
5. All foreground state changes are clipped to the upper display aperture. The controller remains visible and interactive in every S0–S11 state.
6. DPAD/confirm/back can reach every primary home/settings/game-property action; touch remains supported.
7. Loading has one NS-style card, the selected game square cover, dynamic status/progress, and no modern full-screen splash. Runtime FPS is a nonzero integer when frames are presented and uses the NS 3×5 pixel glyph with no panel or arrow.

Scene status is `PASS`, `FAIL`, or `BLOCKED` with a concrete source/evidence reason. A candidate is promotion-ready only when S1–S9 pass, S0/S10/S11 have no P0/P1 defect, the focused automated suite is green, and the FC path guard is clean.

## Aggregated change set (not point fixes)

1. Centralize PC NS tokens and one shell/aperture overlay host; remove full-screen or nested loading/dialog windows.
2. Port NS home geometry: logo/wordmark, three pixel actions, four-column cartridge rhythm, focus frame, title wrapping, add-game-last ordering, and controller focus traversal. Keep PC square cover data and paths.
3. Port the NS settings landing, advanced settings, add-game dialog, and per-game properties surfaces, including pixel dialogs/buttons and the existing PC actions.
4. Fit container settings into the same display aperture and visual language without changing container semantics.
5. Make loading use the selected title/cover, Chinese NS copy, alternating NS progress segments, and dynamic state text.
6. Fix FPS at the presentation boundary, then render through an NS-equivalent `PixelNumberView` contract; do not synthesize refresh-rate values.
7. Port pause/back/exit warning and error/retry states to NS pixel cards.
8. Complete DPAD/focus/controller dispatch and verify orientation/dual-screen shell behavior.

## Non-goals and safety

- No edits under `gamefold-fc`.
- No changes to PC Wine/Proton/container layout, game launch IDs, resource paths, or click-time loading order.
- No clearing app data, permissions, game directories, or user containers during acceptance.
- Never overwrite the 10072 baseline; every build gets a new version identity and a retained artifact/hash manifest.
