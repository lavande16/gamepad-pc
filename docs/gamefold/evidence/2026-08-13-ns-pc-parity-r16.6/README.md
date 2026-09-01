# NS/PC parity capture set — 2026-08-13

Device: vivo X Fold5 (`10AF3P0SDY001M6`), main display `4630946685006800003`, landscape 2480×2200.

The files in this directory are the first read-only comparison set before the aggregated frontend port. `ns-*` are captures from the installed GameFold NS 2.9 R2 product; `pc-*` are captures from the installed GameFold PC R16.6 candidate derived from the frozen 10072 line. The PC permission capture predates this set and is retained as the non-destructive permission reference; NS permission was not forced because the acceptance policy forbids clearing user authorization.

Scene mapping:

- S1: `ns-home.png`, `pc-home.png`
- S2: `ns-settings.png`, `pc-settings.png`
- S3: `pc-advanced.png` (NS source/style reference)
- S4: `pc-add-game.png`
- S5: `ns-game-settings.png`, `pc-game-settings.png`
- S6: `pc-container-settings.png`
- S7: `ns-loading.png`, `pc-loading.png`
- S8: `pc-runtime.png`
- S9: `pc-back-warning.png`
- S0: `pc-permission.png` plus source-level NS permission comparison

These are evidence artifacts, not a promotion claim. The post-port loop must recapture all affected scenes with the same display ID and timing.
