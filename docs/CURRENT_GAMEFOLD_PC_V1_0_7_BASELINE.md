# Current GameFold PC V1.0.7 baseline

The approved GameFold PC baseline is **V1.0.7-GTA5-FIRST-LAUNCH-RESTART-10171**. It promotes the user-selected `GameFold-PC-V1.0.4-GTA5-FIRST-LAUNCH-RESTART-10168.apk` behavior with a formal `1.0.7 / 10171` Android identity.

- APK: `releases/baselines/V1.0.7/GameFold-PC-V1.0.7-GTA5-FIRST-LAUNCH-RESTART-10171.apk`
- SHA-256: `bc5d035936a53ebf9c41e22b1f5df0c21ae60cc690c5a1045d6f3c514b20afc0`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `1.0.7` / `10171`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `fe2e66b5a540c3532fd01faca69cec4436e93cd5`
- Selected candidate commit: `7584904b941eef28f368dfc72051985f56f2b3cd`
- Selected candidate SHA-256: `001a0edf11aa9d2ca580c0c4d12db0b74400b392cddab4328dfb5a1ceddbed2a`
- Git tag: `gamefold-pc-v1.0.7-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,411,799` bytes

## Verification evidence

- TDD RED reported five expected failures for ZR gyro gating, 150% sensitivity, and the old Android identity; the focused suite passed after restoration.
- Full unit regression: `433 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully with the formal PC shell flags.
- APK badging reports `com.gamefold.pc`, `1.0.7 / 10171`, minSdk `26`, and targetSdk `28`.
- APK Signature Scheme v2 verification passed with the established certificate.
- The production gyro bridge and sensitivity files have a zero Git diff from selected commit `7584904b941e`.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 remains `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`, with the required JNI symbol exported.
- Device `43925730` (vivo V2436A) reports `1.0.7 / 10171` after an in-place update at `2026-08-19 14:58:16`; first-install time `2026-08-19 01:15:52` and `MANAGE_EXTERNAL_STORAGE: allow` were preserved.
- The installed `base.apk` was pulled read-only; its size, SHA-256, and bytes match the frozen artifact exactly.
- Codex did not launch the application after installing the formal build.

## Selected state

- GTA5 alone reads its game-local `saves` folder on first launch and does not repeatedly overwrite saves after successful handling.
- Import status is transient in the game-loading card, and failure never blocks normal game launch.
- After GTA5 provisions its runtime for the first time, the stale environment is terminated and the game is launched again automatically.
- Holding ZL enables gyro angle control; releasing ZL disables it immediately.
- ZR remains the right analog trigger and never opens or keeps the gyro gate.
- Horizontal and vertical gyro sensitivity are the selected candidate's original `1.0x`, equal to `19.6` mouse counts per physical degree. The later `150%` multiplier is not included.

## Public driver restore contract

- `releases/baselines/V1.0.7/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
  - size: `2,529,392` bytes
  - SHA-256: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`
- `releases/baselines/V1.0.7/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
  - size: `1,968,250` bytes
  - SHA-256: `0705aed1537c2df5a61125a1479fc6551ea11f982075ae0a1d6d2b9fecff0f83`
- Runtime destination: `/storage/emulated/0/GameFoldPC/graphics/`.

## Scope and rollback

- GTA5 automatic save import and gyro policy are disabled for all other games.
- GameFold NS and GameFold FC/NES are unchanged and out of scope.
- Direct rollback baseline: `releases/baselines/V1.0.5/GameFold-PC-V1.0.5-GTA5-SAVE-STARTUP-GYRO-10169.apk`.
- Rollback tag: `gamefold-pc-v1.0.5-baseline`.
- V1.0.5 and the V1.0.6 candidate remain frozen and available; neither was overwritten.
- Future GameFold PC work should branch from `gamefold-pc-v1.0.7-baseline` with a new candidate identity.
