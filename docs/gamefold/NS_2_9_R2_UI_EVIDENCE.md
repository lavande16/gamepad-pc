# GameFold NS 2.9 R2 UI evidence

## PC integration input

The runtime base is the frozen GameFold PC V0.6.6 tag
`gamefold-pc-v0.6.6-baseline` (source `c0320448b6256ada0d6e175385d4c8ca43df64d9`, tag commit
`139d0d460038c9175f1ef27c4ade5e2189c01729`) and APK
`releases/baselines/V0.6.6/GameFold-PC-V0.6.6-Baseline-10057.apk`, SHA-256
`3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad`.
Its 227-test/39-class supported Legacy inventory is the pre-integration gate.

## Authoritative identity

| Field | Frozen UI truth |
| --- | --- |
| APK | `/Users/11085429/Documents/Game Fold/baselines/GameFold-2.9-Mainline-Baseline-R2/GameFold-2.9-Baseline-R2.apk` |
| Package | `com.switch.fold.baseline.lite` |
| versionName / versionCode | `2.9` / `33466084` |
| SHA-256 | `156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292` |
| Baseline revision | `R2`, accepted 2026-08-09 |

The frozen APK and accepted device behaviour are the authority. The current NS
working tree at `/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/src/android`
is a migration reference only: its Git HEAD predates later uncommitted UI work.
When the reference source, a screenshot, and the frozen APK disagree, do not
infer a new visual rule from source; preserve the APK/device result and record
the discrepancy in the acceptance ledger.

## Evidence locations

- Frozen APK: `/Users/11085429/Documents/Game Fold/baselines/GameFold-2.9-Mainline-Baseline-R2/GameFold-2.9-Baseline-R2.apk`
- Baseline identity and device acceptance: `/Users/11085429/Documents/Game Fold/baselines/GameFold-2.9-Mainline-Baseline-R2/README.md`
- Current baseline pointer: `/Users/11085429/Documents/Game Fold/CURRENT_GAMEFOLD_2_9_BASELINE.md`
- Existing dual-screen references:
  - `/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/design-qa-evidence/dual-screen-off-reference.webp`
  - `/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/design-qa-evidence/dual-screen-on-reference.webp`
  - `/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/design-qa-evidence/gamefold-home-dual-toggle-on-device.png`
- Migration-reference implementation:
  - `/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/src/android/app/src/main/java/org/yuzu/yuzu_emu/views/GameFoldShellLayout.kt`
  - `/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/src/android/app/src/main/java/org/yuzu/yuzu_emu/views/GameFoldViewportLayout.kt`
  - `/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/src/android/app/src/main/java/org/yuzu/yuzu_emu/views/GameFoldControllerInput.kt`
  - `/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/src/android/app/src/main/res/values/pixel_colors.xml`

## Frozen visual and interaction rules

| Area | Rule to preserve |
| --- | --- |
| Shell | Landscape controller artwork reference is `2480 x 2200`; the display aperture is 16:9 and has a 7 px reference border. Portrait uses a 16:9 upper aperture and bottom-anchors the controller composition. |
| Library | A cartridge outer card is `343:392`; the PC cover is square with crop behaviour; title is constrained to two lines below. Preserve frame, focus and add-card treatment. |
| Controller | Preserve D-pad, ABXY, sticks, L/R/ZL/ZR, minus/plus and HOME hit regions; pressed layers, haptics/sounds, multi-touch, cancel/focus release, and L3/R3 long press at 600 ms are semantic controller behaviour. |
| Pixel palette | Primary shell values: deep `#252429`, portrait deep `#252529`, display frame `#050607`, display mask `#474951`, light surface/text `#FBFAF7`, moss `#C4EA72`, ink border `#787370`, focus `#6D8F9C`, action `#B65454`. Preserve the named NS pixel palette rather than substituting generic Material colors. |
| Orientation | Portrait and landscape are an explicit persisted choice, not a sensor decision; startup, library, settings and runtime share it. Portrait top display corners derive from `WindowInsets.RoundedCorner`, while hinge-adjacent bottom corners remain fixed. |
| Unsupported windows | Multi-window, PiP or undersized layouts show the official unsupported state, hide child content, consume shell touches, release controller input, and restore the existing session when supported geometry returns. |
| vivo rear screen | Unsupported manufacturers hide the control. On supported vivo devices retain rear ZL/ZR touch, front-edge feedback, display/activity lifecycle, and committed blank frame before rear-display disable; physical no-flash/tactile confirmation remains device acceptance. |

## Required comparison captures

Each candidate capture is compared to the frozen APK/device evidence at the
same orientation and geometry. The Task 1 ledger records no candidate capture
as accepted yet.

| Required state | Reference / required evidence |
| --- | --- |
| Startup processor, permission and public-payload states | Frozen APK plus screenshot capture at each transition |
| Home library with games, empty library and add-game dialog | Frozen APK; existing home dual-toggle capture above; candidate screenshots |
| Global advanced settings and per-game settings | Frozen APK screenshots and UI-state assertions |
| Loading, progress, error and retry | Frozen APK screenshots, UI-state assertions and no previous-frame leakage |
| Landscape shell and controller | `2480 x 2200` reference geometry, 16:9 aperture and controller input tests |
| Portrait shell and controller | `2200 x 2480` reference geometry, 16:9 aperture, dynamic top corners and fixed bottom corners |
| Unsupported window and recovery | Screenshot plus test proving child concealment, touch swallowing and input release |
| vivo rear screen off/on, rear ZL/ZR and shutdown | Existing off/on references plus physical-device capture of feedback and blank-frame-before-disable ordering |

## Asset provenance

Task 1 intentionally copies no UI asset. `packaging/gamefold-ns-ui-assets.sha256`
is initialized as the future byte ledger; Task 5 may add only enumerated
GameFold shell/controller assets, with one SHA-256 row per copied binary.
