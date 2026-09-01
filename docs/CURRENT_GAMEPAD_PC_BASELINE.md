# Current GamePad PC baseline

The current approved personal baseline is **V2.4-2304X1296-10033**.

- Evidence and restore contract: `docs/CURRENT_GAMEPAD_PC_V2_4_10033_BASELINE.md`
- Frozen APK: `releases/baselines/GamePad-PC-V2.4-10033/GamePad-PC-V2.4-2304X1296-10033.apk`
- Package: `com.gamepad.pc`
- Version: `2.4` (`10033`)
- SHA-256: `f3b8cbb53d712537e00083ca8b7164592b0b2204f63bfb7e1a6dc8af9cb17b31`
- Functional source commit: `958a3414ad9fe445c24518bfba6d62ab5d1a36eb`
- Git tag: `gamepad-pc-v2.4-10033-baseline`

This personal baseline keeps the approved empty-library Home behavior, 3DM URL, NFS11 behavior, and run-as contract. It keeps the Pad 16:9 device override at `2304x1296`, the matching selectable game-resolution entry, synchronized global and per-game settings, centered focused resize icon, and `2304x1296` game-window whitelist. Lower controls stay at `translateY=-72px`; only the home/logo layer returns to `translateY=0px`, moving the logo down 72px from 10032.
