# Current GamePad PC V1.2 baseline

The approved GamePad PC baseline is **V1.2-TRANSPARENT-SMALL-BUTTONS-10005**. It preserves the accepted GamePad PC 1.0 / 10003 product behavior while replacing exactly six small-button default-state assets with the user's updated transparent versions.

- APK: `releases/baselines/GamePad-PC-V1.2/GamePad-PC-V1.2-TRANSPARENT-SMALL-BUTTONS-10005.apk`
- Package: `com.gamepad.pc`
- Version: `1.2 / 10005`
- SHA-256: `9a3ee878bee0275a17b81bc8f3365b5748962d9bdfcdae74e5e5015c63807e43`
- APK size: `16,379,768` bytes
- Functional source commit: `d26c6df357b816cc4d0021658721eac678d8afdb`
- Git tag: `gamepad-pc-v1.2-baseline`
- Rollback candidate: `releases/candidates/GamePad-PC-V1.0-RC3/GamePad-PC-V1.0-RC3-10003.apk`

## Frozen change scope

- Replaced only `controller_button_zl_default.webp`, `controller_button_l_default.webp`, `controller_button_r_default.webp`, `controller_button_zr_default.webp`, `controller_button_plus_default.webp`, and `controller_button_minus_default.webp`.
- All six assets are `138 × 138` WebP files with alpha, and their exact hashes are locked by `GamePadSmallDefaultAssetTest`.
- Pressed assets and every other controller asset remain unchanged.
- Controller geometry, visible size, coordinates, touch zones, input mapping, haptics, and sound remain unchanged.
- Product pages and game-only overlay visibility rules remain unchanged.
- The formal Release build remains non-debuggable and does not allow `run-as`.

## Verification evidence

- `484` Legacy Release unit tests passed with `0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully; R8 and Lint Vital passed.
- APK badging reports `com.gamepad.pc`, `1.2 / 10005`, minSdk `26`, and targetSdk `28`.
- APK Signature Scheme v2 passed with certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`.
- Device `403G8G00BM611V0` / `iPA2693` reports `1.2 / 10005` after an in-place update at `2026-08-21 21:58:06`; first-install time remains `2026-08-21 16:17:17` and dataDir remains `/data/user/0/com.gamepad.pc`.
- `run-as com.gamepad.pc` is rejected because the package is not debuggable.
- The installed APK pulled from the device matches the frozen baseline APK byte-for-byte and has the same SHA-256.
- User explicitly instructed that this release be frozen as GamePad PC 1.2. This evidence does not claim a separate in-game visual acceptance pass for the six new assets.

## Version history boundary

- GamePad PC `1.0 / 10003` and intermediate `1.1 / 10004` are behavior-equivalent foundations for this change.
- The earlier instruction to mark 1.1 was superseded before a 1.1 baseline archive or tag was created; therefore 1.2 is the first formal GamePad PC baseline.
- The accepted V1.0 RC3 artifact remains preserved as the direct rollback package.

## Restore contract

- Restore code from Git tag `gamepad-pc-v1.2-baseline`.
- Restore the exact APK from `releases/baselines/GamePad-PC-V1.2/` and verify its SHA-256 before installation.
- Do not replace this directory, current-baseline pointer, or Git tag with a later candidate.
- Use the preserved V1.0 RC3 artifact for direct rollback if the revised six default-state visuals are not desired.

