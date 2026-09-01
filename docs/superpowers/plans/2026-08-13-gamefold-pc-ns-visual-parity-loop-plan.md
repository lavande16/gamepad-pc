# GameFold PC NS visual parity loop plan

> Execute this plan in order. Keep every screenshot, hash, test result, and candidate identity in the evidence ledger. The user has authorized autonomous execution; do not pause for per-scene approval.

**Goal:** Make the GameFold PC 0.6.6 frontend visually and interactively consistent with GameFold NS 2.9 R2 across the scene matrix in the design spec, while preserving PC runtime behavior and leaving GameFold FC untouched.

**Baseline:** `releases/baselines/V0.6.6-NS-UI-R15-10072/`, APK SHA-256 `dc79f8c3d3e21a9bba841c449c4871a5de5ab5790b2672d49c15e48141ad1e39`, versionCode `10072`.

## Phase 0 — Freeze and evidence ledger

1. Verify the baseline artifact, source commit, signer, and the installed NS 2.9 APK.
2. Confirm the worktree has no FC changes and record the current PC candidate identity.
3. Store same-device screenshots and a manifest under `docs/gamefold/evidence/2026-08-13-ns-pc-parity-r16.6/`.
4. Do not reset permissions or app data. Treat S0 permission UI as a source-plus-existing-capture comparison until a non-destructive test path exists.

**Gate:** all scene captures have metadata/hashes; baseline remains byte-identical.

## Phase 1 — Code-to-scene mapping and red contracts

1. Add parity contracts for scene IDs, NS tokens, display-aperture confinement, title/cover loading, integer FPS presentation samples, and FC path exclusion.
2. Add focused tests before production changes for home ordering/focus, loading title/progress, FPS counter behavior, overlay ownership, and container settings confinement.
3. Run the focused tests and record the expected red/green transitions.

**Gate:** every planned production change maps to an S-scene and a test/evidence method.

## Phase 2 — One coherent frontend port

1. Establish a single PC NS token/card/dialog layer based on NS `pixel_colors.xml`, `pixel_styles.xml`, `card_game_grid.xml`, `PixelNumberView`, and `dialog_emulation_exit_pixel.xml`.
2. Refactor the PC shell so every PC foreground route uses one aperture overlay slot and a non-null controller/focus dispatcher.
3. Port home, settings, advanced settings, add-game, per-game properties, and container settings in one coherent pass. Preserve callbacks and PC data models.
4. Port loading, error/retry, pause/exit, and FPS presentation without touching the PC emulator/runtime ABI.

**Gate:** focused tests green, `git diff --check` clean, no files under FC, and the source diff contains no runtime provisioning changes.

## Phase 3 — Candidate build and real-device loop

1. Build a distinct legacy release identity (versionCode greater than 10072, unique versionName/package alias) offline.
2. Verify APK package/version/signer/SHA and archive it with a manifest before installation.
3. Install on the connected vivo device without clearing data or permissions; record install time and `dumpsys package` identity.
4. Reproduce S1–S11 in a fixed order and capture the same timed frames as the baseline.
5. Compare aperture geometry, card bounds/gaps, sampled palette, text/icon scale, focus/input, controller persistence, loading status, and runtime FPS.

## Phase 4 — Loop rule

1. For every FAIL, trace the rendered component to its PC source and the matching NS source anchor.
2. Aggregate all failures into one change list; do not ship point fixes between scenes.
3. Implement the next coherent batch, add/adjust a regression test, rebuild, reinstall, and recapture all affected scenes plus S1/S8 smoke scenes.
4. Repeat until the promotion gates pass or a concrete external blocker is recorded. A transient black frame is classified as a loading state only when it has a visible dynamic NS card/status and does not hide the controller.

## Final acceptance

- S1–S9 PASS; S0/S10/S11 have no P0/P1 defect.
- No uncontrolled full-screen modern dialog/splash in local-shell PC.
- Home/settings/per-game/container flows are reachable by touch and controller DPAD/confirm/back.
- Loading card uses selected square cover/title, NS pixel palette/copy/progress, and controller remains visible.
- Runtime shows presented-frame FPS as a nonzero integer when the game is rendering, with NS 3×5 pixel glyph and no arrow/panel.
- GTA 5 and one second PC game still launch, retain 1067×600 defaults, and preserve PC input/LSFG behavior.
- Focused tests pass; no new failures beyond the frozen historical baseline ledger.
- FC scope guard passes; baseline artifact is unchanged; candidate artifact/hash/install metadata are archived.
