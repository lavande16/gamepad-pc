# Current GamePad PC V2.3 baseline

The approved personal baseline is **V2.3-EMPTY-HOME-10028**.

- APK: `releases/baselines/GamePad-PC-V2.3/GamePad-PC-V2.3-EMPTY-HOME-10028.apk`
- Package/version: `com.gamepad.pc`, `2.3 / 10028`
- SHA-256: `6263dd4b318b6bfb925e8f5097f18f649f310a1e75da36fcd2a23f8ee283fba7`
- Candidate source SHA-256: `2a4af121865e5d6140ca7554b9f1b70a3b4048035ea0f34e129c7234279c88e9`
- Source commit: `dc52f628550104b315d18719f4c026bb6c6207ee`
- Git tag: `gamepad-pc-v2.3-baseline`

V2.3 shows only the Add Game cartridge after a completed empty scan. It preserves the V2.2 personal run-as/debuggable contract, the approved 3DM download destination, and NFS11 gyro behavior.

Device verification passed on `403G8G00BM611V0 / iPA2693`: the in-place upgrade preserved the first-install timestamp, full-file-access permission, and run-as access. The pulled installed APK is byte-identical to the frozen baseline and has the same SHA-256.
