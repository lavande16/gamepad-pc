# Current GamePad PC V2.4 / 10032 baseline

The current approved personal baseline is **V2.4-2304X1296-10032**.

- APK: `releases/baselines/GamePad-PC-V2.4-10032/GamePad-PC-V2.4-2304X1296-10032.apk`
- Package/version: `com.gamepad.pc`, `2.4 / 10032`
- SHA-256: `a36d5f2cf2d511be8a26a2e346c8e6bc82eb221c0c38ce16777e8c4a92b5f4fe`
- Source commit: `d74b746`
- Git tag: `gamepad-pc-v2.4-10032-baseline`

This revision keeps the approved 1:1 controller artwork size and X origin between `2304×1440` and `2304×1296`. For the `2304×1296` target, the shared logo/controller transform is `scale=1`, `translateX=0`, `translateY=-72px`, which is 144px higher than the 10031 candidate. All other display policies remain unchanged.

Device verification passed on `403G8G00BM611V0 / iPA2693`: the APK was installed in place with existing data preserved, the `2304×1296` override was applied, and launching original NFS11 reached the runtime surface.
