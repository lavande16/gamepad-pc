# GameFold PC NS UI per-version acceptance

Every PC NS-UI release candidate is evaluated against the immutable 10072 baseline and the same
GameFold NS 2.9 R2 page matrix. This is a frontend acceptance gate; GameFold FC is never included.

## Procedure

1. Build a distinct candidate identity and verify package, `versionName`, `versionCode`, signer, and
   SHA-256. Confirm the 10072 APK and hash are unchanged.
2. Install the candidate in place on the test phone without clearing app data, permissions, or the
   PC game library. Record install time and pull the installed APK hash.
3. Cold-start the candidate and capture the seven pages below. Use the same display orientation and
   device scale for every version. The lower controller must be present in every PC local-shell
   capture; only the upper display aperture may change.
4. Compare each capture with the same-page NS 2.9 R2 reference. Use the evidence script to record
   metadata, device package state, screenshot names, and optional ImageMagick difference metrics.
5. Record differences as `keep`, `fix next`, or `blocked by runtime/device`. Only after the complete
   matrix is reviewed may a fix be carried into the next candidate.

## Fixed page matrix

| Capture | NS parity gate |
| --- | --- |
| `home` | pixel logo/title, three top-right actions, NS cartridge dimensions/spacing, add game last, square unrounded cover |
| `add_game` | pixel dialog, PC path, “下载游戏” action, no legacy download-sentence text |
| `settings` | NS light palette, advanced settings route, D-pad/A selection and open, B return |
| `container_settings` | full per-game settings page is clipped to the upper display; controller remains visible and interactive |
| `loading` | one wide NS loading card, square game cover, dynamic state/progress, no full-screen phase |
| `runtime_fps` | game surface, large 8-bit upper-left FPS, no arrow/panel; zero is allowed only before the first frame and must update afterward |
| `pause` | NS pause card/action inside the upper display; controller remains visible |

The evidence bundle is produced with:

```bash
packaging/verify_gamefold_pc_ns_ui_rc.sh <candidate.apk> <evidence-dir> [device-serial]
```

Set `GAMEFOLD_NS_REFERENCE_DIR` to a directory containing the same seven NS reference filenames to
enable optional pixel-difference metrics. Metrics are diagnostic only; the final decision remains a
page-by-page review of geometry, palette, typography, clipping, and controller behavior.

## Carry-forward rule

The next candidate inherits every `fix next` item from the previous ledger, then repeats the full
matrix. A runtime failure (for example, a black interval before the first frame or FPS stuck at 0)
is recorded separately from visual parity, and is not hidden by a passing build or APK hash.
