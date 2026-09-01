# Current GameFold PC V1.0.5 baseline

The approved GameFold PC baseline is **V1.0.5-GTA5-SAVE-STARTUP-GYRO-10169**. It promotes the user-accepted `1.0.4 / 10168` behavior with a formal `1.0.5 / 10169` Android identity; functional source is otherwise unchanged from the accepted candidate.

- APK: `releases/baselines/V1.0.5/GameFold-PC-V1.0.5-GTA5-SAVE-STARTUP-GYRO-10169.apk`
- SHA-256: `da8b9252042de5960db8f7e4400613ef7172dbbb809fc2016b6d890b5a6c7d80`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `1.0.5` / `10169`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `c066b301c52cca41ad9727433c8d06ced4587aa3`
- Accepted feature commit: `eea7e193cc273ab275c4f670b568020b1e9f91d6`
- Git tag: `gamefold-pc-v1.0.5-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,413,063` bytes

## Verification evidence

- Full unit regression: `433 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully with the formal PC shell flags.
- APK badging reports `com.gamefold.pc`, `1.0.5 / 10169`, minSdk `26`, and targetSdk `28`.
- APK Signature Scheme v2 verification passed with the established certificate.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 remains `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`, and the presented-frame-count JNI symbol remains exported.
- Device `43925730` (vivo V2436A) reports `1.0.5 / 10169` after an in-place update at `2026-08-19 12:51:32`; first-install time `2026-08-19 01:15:52` and `MANAGE_EXTERNAL_STORAGE: allow` were preserved.
- The installed `base.apk` was pulled read-only; its size, SHA-256, and bytes match the frozen artifact exactly.
- Codex did not launch the application after installing the formal-identity build.
- The user accepted GTA5 save import, first-runtime relaunch, and ZL/ZR gyro behavior on the real device, then explicitly requested promotion to the `1.0.5` baseline on `2026-08-19`.

## Accepted state

- GTA5 alone reads its game-local `saves` folder on first launch and imports valid save files into the GTA5 target directory.
- Successful import persists a completion marker so later launches do not repeatedly overwrite the target saves.
- Import success or failure is shown briefly in the existing game-loading message area; failure never blocks normal game launch or leaves a persistent Home message.
- After GTA5 provisions its runtime for the first time, the stale black-screen attempt is terminated and the game is launched again automatically.
- Holding either ZL or ZR enables gyro angle control; sensitivity is `150%` of the prior reduced value, while both analog trigger values remain available to the game.
- The V1.0.3 cold-start, processor gate, controller, container, dual-screen, launcher, and runtime acceptance remains in scope.

## Public driver restore contract

- `releases/baselines/V1.0.5/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
  - size: `2,529,392` bytes
  - SHA-256: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`
- `releases/baselines/V1.0.5/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
  - size: `1,968,250` bytes
  - SHA-256: `0705aed1537c2df5a61125a1479fc6551ea11f982075ae0a1d6d2b9fecff0f83`
- Runtime destination: `/storage/emulated/0/GameFoldPC/graphics/`.

## Scope and rollback

- GTA5 automatic save import is disabled for all other games.
- GameFold NS and GameFold FC/NES are unchanged and out of scope.
- Direct rollback baseline: `releases/baselines/V1.0.3/GameFold-PC-V1.0.3-COLD-START-MTK-10167.apk`.
- Rollback tag: `gamefold-pc-v1.0.3-baseline`.
- Future GameFold PC work should branch from `gamefold-pc-v1.0.5-baseline` with a new candidate identity.
