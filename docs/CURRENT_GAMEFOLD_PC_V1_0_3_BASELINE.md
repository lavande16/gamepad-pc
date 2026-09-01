# Current GameFold PC V1.0.3 baseline

The approved GameFold PC baseline is **V1.0.3-COLD-START-MTK-10167**. It promotes the user-accepted `1.0.2 / 10166 RC2` behavior with a formal `1.0.3 / 10167` Android identity; functional source is otherwise unchanged from RC2.

- APK: `releases/baselines/V1.0.3/GameFold-PC-V1.0.3-COLD-START-MTK-10167.apk`
- SHA-256: `a5336299eb31a441aea53930d46d2d848f264b414b6969d0484865d8cc3612f2`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `1.0.3` / `10167`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `31b4fb267e2772de8ed670367513474c1b882d33`
- Git tag: `gamefold-pc-v1.0.3-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,401,279` bytes

## Verification evidence

- Full unit regression: `411 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully.
- APK badging reports `com.gamefold.pc`, `1.0.3 / 10167`, minSdk `26`, and targetSdk `28`.
- APK Signature Scheme v2 verification passed with the established certificate.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 remains `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`.
- Device `43925730` (vivo V2436A) reports `1.0.3 / 10167` after an in-place update at `2026-08-17 23:21:33`; first-install time and `MANAGE_EXTERNAL_STORAGE: allow` were preserved.
- The installed `base.apk` was pulled read-only; its size, SHA-256, and bytes match the frozen artifact exactly.
- Codex did not launch the application or perform product-side visual acceptance.
- The user accepted RC2 on the real device and explicitly requested promotion to the `1.0.3` baseline on `2026-08-17`.

## Accepted state

- Cold start is covered by the NS-style icon-and-title splash until a stable destination is ready.
- An already-authorized device does not expose a transient permission page behind the splash.
- Splash dismissal waits for both startup routing and the activity configuration matching the saved orientation, covering portrait posture launches of saved landscape mode.
- MTK/Dimensity detection routes to the unsupported screen before permission or Home; supported Qualcomm devices remain allowed.
- MTK device acceptance is not claimed because no MTK phone was connected for this release.
- All accepted V1.0.1 controller, gyro, container, dual-screen, launcher, and runtime behavior remains in scope and unchanged.

## Public driver restore contract

- `releases/baselines/V1.0.3/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
  - size: `2,529,392` bytes
  - SHA-256: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`
- `releases/baselines/V1.0.3/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
  - size: `1,968,250` bytes
  - SHA-256: `0705aed1537c2df5a61125a1479fc6551ea11f982075ae0a1d6d2b9fecff0f83`
- Runtime destination: `/storage/emulated/0/GameFoldPC/graphics/`.

## Scope and rollback

- GameFold NS and GameFold FC/NES are unchanged and out of scope.
- Rollback baseline: `releases/baselines/V1.0.1/GameFold-PC-V1.0.1-PORTRAIT-GYRO-DIRECTION-10164.apk`.
- Rollback tag: `gamefold-pc-v1.0.1-baseline`.
- Future GameFold PC work should branch from `gamefold-pc-v1.0.3-baseline` with a new candidate identity.
