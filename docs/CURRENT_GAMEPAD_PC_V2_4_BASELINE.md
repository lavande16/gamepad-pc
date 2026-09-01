# Current GamePad PC V2.4 baseline

The approved personal baseline is **V2.4-2304X1296-10029**.

- APK: `releases/baselines/GamePad-PC-V2.4/GamePad-PC-V2.4-2304X1296-10029.apk`
- Package/version: `com.gamepad.pc`, `2.4 / 10029`
- SHA-256: `1eb04ac368b2dcfe6f484bbbf2da75d7bca8fa6e313098bda6deadd542a90dbb`
- Source commit: `b42febc135e0725bd1f3aa4d193cbc1874000a81`
- Git tag: `gamepad-pc-v2.4-baseline`

V2.4 changes the Pad 16:9 device override from 1080P to portrait `1296x2304` / landscape `2304x1296`, adds `2304x1296 (16:9)` to the selectable game resolutions, and synchronizes the global preference and every existing container when the device toggle is used. New games inherit the synchronized global value.

Device verification passed on `403G8G00BM611V0 / iPA2693`: the in-place upgrade preserved the first-install timestamp, full-file-access permission, `WRITE_SECURE_SETTINGS`, and run-as access. The live toggle produced `Override size: 1296x2304`; the global preference, all five existing containers, and the original NFS11 resolution config read back as `2304x1296`. The pulled installed APK is byte-identical to the frozen baseline.
