# GameFold PC NS UI R15 acceptance ledger

This ledger belongs to the post-freeze PC frontend work. The immutable rollback point is
`releases/baselines/V0.6.6-NS-UI-R15-10072/` and must remain unchanged.

## Product boundary

- Scope: GameFold PC frontend and its display/controller presentation only.
- Out of scope: `gamefold-fc`, the PC Winlator/Proton runtime implementation, and startup resource
  provisioning. PC resources remain click-time lazy-loaded; first launch still performs permission
  and processor checks before entering Home.
- Reference design: GameFold NS 2.9 R2 shell, cartridge, loading card, pixel typography, and FPS
  treatment.

## Required evidence

| Area | Required result | Evidence |
| --- | --- | --- |
| Baseline | 10072 APK can be restored byte-for-byte | baseline APK, `BASELINE.json`, freeze commit |
| Scope | no FC path changes | `git diff --name-only` guard |
| Home | cream NS display, pixel header/actions, cartridge grid, square PC covers, titles | device screenshot |
| Shell | controller visible from first frame; changing content stays in upper display | home/loading/settings/runtime screenshots |
| Loading | retro card in display aperture, dynamic status/progress retained | launch screenshot + log |
| FPS | integer NS 3x5 glyph at NS 4dp-per-pixel scale, upper-left, no panel/decimal | runtime screenshot + HUD test |
| Interactions | add game, tap launch, long press details, settings return, back/quick menu | device checklist |
| Runtime | PC game/input/1067x600 smoke path unchanged | device log/checklist |
| Artifact | distinct RC package/version/signer/hash, baseline untouched | APK metadata report |

## Automated acceptance

1. `GameFoldPcFrontendPolicyTest` passes.
2. Shell host, loading-state, and FPS contract tests pass.
3. Existing PC test suite has no new failures relative to the recorded baseline ledger.
4. `git diff --check` passes.
5. APK build is offline and uses an explicitly distinct RC identity.

## Current execution evidence

- Kotlin compile: `:app:compileLegacyDebugKotlin` passed after the final shell-overlay/HUD fixes.
- Focused PC product/rear/frontend suite: `45` tests, `0` failures, `0` errors.
- Full PC legacy suite: `959` tests, `71` failures; this matches the frozen baseline ledger and the
  failures remain the existing downloader/fixture/resource failures, not frontend regressions.
- Final candidate: `releases/candidates/V0.6.6-NS-UI-R16/GameFold-PC-V0.6.6-NS-UI-R16-10073.apk`,
  package `com.gamefold.pc.v066nsrc.r16`, version `0.6.6-gf-ns-r16`, SHA-256
  `d4ea26076813e039f2d31cd948019120608fc99919b64c99d63f53701153d5cf`, v2 signature.
- Device install verified on vivo `V2436A` (`10AF3P0SDY001M6`): pulled device APK hash matches the
  candidate SHA-256. Cold-start Home shows the NS shell and persistent controller; permission denial
  shows the in-aperture NS-style authorization card; Settings uses the light NS palette; tapping a PC
  game shows the retro loading card and changing segmented progress only in the upper display.
- Runtime game surface and FPS glyph were not claimed: the device smoke was stopped during the
  existing click-time Bionic installation path, before a game surface became available.

## R16.1 follow-up fixes

- `864792e` removes the shell cursor from the display aperture, makes FPS collection independent of
  the hidden legacy widget, counts Vulkan native-scanout updates, and matches NS `PixelNumberView`
  sizing/spacing.
- `8b91663` replaces the local-shell add-game and generic message dialogs with in-aperture NS pixel
  cards, and moves manual pause to an NS square pixel action card inside the display aperture.
- `2f5b21d` keeps a dynamic segmented GameFold loading card visible in the display aperture from the
  XServer route until the first application window content update, covering the previously observed
  black bootstrap interval without covering the lower controller.
- R16.1 candidate: `releases/candidates/V0.6.6-NS-UI-R16.1/GameFold-PC-V0.6.6-NS-UI-R16.1-10074.apk`,
  SHA-256 `31fbeb3116b257559d013dd5c50a6e4c63203252fec2d60d250b573063d314d4`, package
  `com.gamefold.pc.v066nsrc.r16`, version `0.6.6-gf-ns-r16.1`, versionCode `10074`.

## R16.5 device review

- Candidate: `releases/candidates/V0.6.6-NS-UI-R16.5/GameFold-PC-V0.6.6-NS-UI-R16.5-10078.apk`,
  SHA-256 `ae0717040fc219e991f993effd6f1b7888bd6e93a2f85faff0a6246cd0a1e6f6`, package
  `com.gamefold.pc.v066nsrc.r16`, version `0.6.6-gf-ns-r16.5`.
- The installed APK was pulled from vivo `V2436A` and matched the candidate hash byte-for-byte.
- Reviewed evidence: `/private/tmp/gf-r16.5-home.png` (NS wordmark, three pixel actions, square
  unrounded covers, cartridge order), `/private/tmp/gf-r16.3-add2.png` (pixel add-game dialog),
  `/private/tmp/gf-r16.3-container.png` (per-game settings clipped to display aperture), and
  `/private/tmp/gf-r16.5-loading.png` (selected GTA cover and segmented loading state).
- Runtime is not promoted as passed: one launch returned to the system desktop after the loading
  card and produced no attributable runtime log, so non-zero FPS and stable GTA runtime remain a
  next-pass device gate rather than a hidden success claim.
- The 10072 baseline artifact and SHA remain unchanged. No `gamefold-fc` path is in the diff.

## Device acceptance order

1. Install only the distinct RC; do not replace the frozen baseline package.
2. Cold start and reject/grant permission paths; verify processor check precedes Home.
3. Verify Home, settings, add-game dialog, and long-press details.
4. Launch a PC game; verify upper-display loading card and persistent lower controller.
5. Verify game surface and integer pixel FPS at upper-left.
6. Exercise Back, quick menu, orientation, dual-screen/rear-touch, and input smoke paths.
7. Repeat launch to confirm resource loading remains click-time and Home startup stays fast.

## R16.6 parity-loop baseline capture (2026-08-13)

The first scene-by-scene comparison set is archived at
`docs/gamefold/evidence/2026-08-13-ns-pc-parity-r16.6/` with a SHA-256 manifest. It uses the
same vivo X Fold5 main display (`4630946685006800003`, 2480×2200 landscape) for NS 2.9 and PC.

The source/evidence review marks these as the aggregated next change set rather than isolated
fixes: NS home geometry/focus, settings and per-game cards, container aperture styling, add-game
and permission dialogs, selected-cover loading copy/progress, presentation-boundary FPS, and the
NS pause/exit/error surfaces. The PC runtime remains click-time and the FC path remains outside
the diff. Current R16.6 runtime evidence still shows an FPS glyph of `0`, so runtime/FPS is a
failing gate until the next candidate is captured with a presented-frame measurement.

## Rollback

If any gate fails, stop promoting the RC. The restore artifact is the baseline APK identified by
SHA-256 `dc79f8c3d3e21a9bba841c449c4871a5de5ab5790b2672d49c15e48141ad1e39`; do not delete, rewrite,
or re-sign it.
