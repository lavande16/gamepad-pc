# GameFold PC x NS integration acceptance ledger

## Status vocabulary

| Status | Meaning |
| --- | --- |
| `NOT_RUN` | No evidence has been collected for this cell. |
| `PASS` | The listed command or device observation completed with the recorded evidence. |
| `FAIL` | The listed evidence disproves the required behaviour. |
| `HOLD_FOR_DEVICE` | Automation cannot establish a required physical, tactile, fold or security fact. |
| `NOT_APPLICABLE` | The cell does not apply to the candidate/device; record why. |

## Frozen inputs

| Input | Identity |
| --- | --- |
| PC runtime | `gamefold-pc-v0.6.6-baseline` at `139d0d460038c9175f1ef27c4ade5e2189c01729` (source `c0320448b6256ada0d6e175385d4c8ca43df64d9`); `GameFold-PC-V0.6.6-Baseline-10057.apk`; SHA-256 `3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad` |
| NS UI truth | `GameFold-2.9-Baseline-R2.apk`; `com.switch.fold.baseline.lite`; versionCode `33466084`; SHA-256 `156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292` |

## Initial acceptance rows

Only the frozen supported 0.6.6 baseline inventory is prefilled as `PASS`. All
candidate UI, artifact, device, fold and rear-screen cells remain unrecorded
until their own task has fresh evidence.

| Scope | Status | Evidence |
| --- | --- | --- |
| Frozen PC supported Legacy baseline: 227 tests across 39 classes | `PASS` | Frozen V0.6.6 evidence `docs/CURRENT_GAMEFOLD_PC_V0_6_6_BASELINE.md`; tag `gamefold-pc-v0.6.6-baseline`; package `com.gamefold.pc.v04localshell`; version `0.6.6` / `10057`; APK SHA `3460a103...5268ad`; 0 failures, 0 errors, 0 skipped. |

## R14 candidate evidence

| Scope | Status | Evidence |
| --- | --- | --- |
| Independent R15 artifact identity | `PASS` | `GameFold-PC-V0.6.6-NS-UI-R15-10072.apk`; package `com.gamefold.pc.v066nsrc`; version `0.6.6-gf-ns-r15`; SHA `dc79f8c3...ad1e39`; built with `GAMEFOLD_PC_LOCAL_SHELL=true`, installed SHA matched. |
| R15 focused JVM tests | `PASS` | 37 tests, 0 failures, 0 errors, 0 skipped; defaults, migration, startup gate, controller geometry, rear input router and rear policy. |
| First-open flow | `PASS` | vivo cold start enters homepage after processor/permission gate; no ImageFS/Proton preload overlay. Click-time flow shows GameFold dynamic `Launching game...` state. |
| GameFold front shell | `PASS` | Device UI tree and screenshots show cartridge library, square PC cover, GameFold branding, FPS HUD, official controller shell and long-press settings page. |
| vivo rear display lifecycle | `PASS` | V2436A creates Display 3 (`local:secondary`, 2748×1172) and hosts `GameFoldRearTouchActivity` on `d3`; R15 transient focus fix keeps it alive during game startup, and new-container creation reads the global `1067x600` default. |
| Physical rear touch / tactile feel | `HOLD_FOR_DEVICE` | ADB can establish the Activity, input bridge and rendering; physical finger/tactile acceptance still requires manual device confirmation. |

## Future evidence rule

Every later task appends the exact command or device observation, exit code or
capture path, candidate identity, and one status from the vocabulary above.
`HOLD_FOR_DEVICE` is mandatory rather than a claimed pass whenever a physical
fold, rear-display no-flash observation, tactile feedback, or security dialog
cannot be established safely by automation.
