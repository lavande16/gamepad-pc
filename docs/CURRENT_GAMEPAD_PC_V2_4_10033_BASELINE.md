# Current GamePad PC V2.4 / 10033 baseline

The current approved personal baseline is **V2.4-2304X1296-10033**.

- APK: `releases/baselines/GamePad-PC-V2.4-10033/GamePad-PC-V2.4-2304X1296-10033.apk`
- Package/version: `com.gamepad.pc`, `2.4 / 10033`
- SHA-256: `f3b8cbb53d712537e00083ca8b7164592b0b2204f63bfb7e1a6dc8af9cb17b31`
- Source commit: `958a3414ad9fe445c24518bfba6d62ab5d1a36eb`
- Git tag: `gamepad-pc-v2.4-10033-baseline`

For the `2304×1296` target, lower controls remain at `scale=1`, `translateX=0`, `translateY=-72px`, while the home/logo layer uses `scale=1`, `translateX=0`, `translateY=0px`. This moves only the logo down 72px from 10032; all other display policies remain unchanged.

Device verification passed on `403G8G00BM611V0 / iPA2693`: the APK was installed in place with existing data preserved, the `2304×1296` override was restored, and launching original NFS11 reached the game startup surface.
