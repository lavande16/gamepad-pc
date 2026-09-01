# Current GamePad PC V1.8 baseline

The approved long-term personal baseline is **V1.8-HOME-RESIZE-NFS11-10019**.

- APK: `releases/baselines/GamePad-PC-V1.8/GamePad-PC-V1.8-HOME-RESIZE-NFS11-10019.apk`
- Package/version: `com.gamepad.pc`, `1.8 / 10019`
- SHA-256: `8b08fd4f15e3f1e807277235e545d02bbac5d8335e2d5e21a66ccedb3058ad27`
- Functional source commit: `35f6135fb6bed8b4ca38406421d5526ccb2032d8`
- Git tag: `gamepad-pc-v1.8-baseline`
- Run-as: enabled by default for this personal baseline

This baseline promotes the user-accepted V1.7 HOME-RESIZE RC4 behavior without functional changes. It freezes the direct home-page toggle between native `1440x2304` and `1080x1920`, synchronized per-game screen sizes, original `NFS11.exe` configuration updates, the 10-second controller auto-hide delay, the accepted 1080p controller/HOME geometry, external-display defaults, and the add-PC-game dialog layout and path label.

Verification is frozen in `releases/baselines/GamePad-PC-V1.8/BASELINE.json`: 547 tests passed, the Release build completed, the APK identity and v2 signature were verified, installation succeeded, `WRITE_SECURE_SETTINGS` and run-as were available, the device resolution remained unchanged, and the pulled installed APK matched the archive byte-for-byte.
