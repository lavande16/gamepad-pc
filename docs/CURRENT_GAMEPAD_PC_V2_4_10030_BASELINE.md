# Current GamePad PC V2.4 / 10030 baseline

The approved personal baseline is **V2.4-2304X1296-10030**.

- APK: `releases/baselines/GamePad-PC-V2.4-10030/GamePad-PC-V2.4-2304X1296-10030.apk`
- Package/version: `com.gamepad.pc`, `2.4 / 10030`
- SHA-256: `51aff7b3d4619dbc9ecc62f7f2007000e59b90e0a542cc9cdb289bc955209600`
- Source commit: `3ae3b687039b77a0aa96ffa2dd5d89ebb9a4f59d`
- Git tag: `gamepad-pc-v2.4-10030-baseline`

This revision keeps the V2.4 `2304×1296` display profile and adds that landscape size to the GamePad PC immersive-window whitelist. The resize icon now uses a stable offset policy: `-2dp` for the unfocused shadowed button face and `0dp` when the focus frame is shown, so the glyph stays centered after the resolution switch.

Device verification passed on `403G8G00BM611V0 / iPA2693`: the APK was installed in place with existing data preserved, and launching the original NFS11 at `2304×1296` reached the runtime surface without the old resolution gate.
