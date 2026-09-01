# GamePad PC V2.4 logo 几何修正版个人基线

这是 V2.4 的 `10033` 修正版基线，针对 `2304×1296` 显示档位将 GamePad logo 下移 72px，同时保持下方手柄位置不变。

- APK：`GamePad-PC-V2.4-2304X1296-10033.apk`
- 包名/版本：`com.gamepad.pc`，`2.4 / 10033`
- SHA-256：`f3b8cbb53d712537e00083ca8b7164592b0b2204f63bfb7e1a6dc8af9cb17b31`
- 基于：`V2.4-2304X1296-10032`
- 功能提交：`958a341`
- Git 标签：`gamepad-pc-v2.4-10033-baseline`

几何规则：`2304×1296` 继续保持 `2304×1440` 的 1:1 尺寸和 X 坐标；下方手柄使用 `translateY=-72px`，logo/home 层使用 `translateY=0px`，因此相对 10032 仅 logo 下移 72px，其他分辨率规则不变。

验证：577 项 Legacy Release 单测全部通过；发布 APK 身份为 `2.4 / 10033`，minSdk 26、targetSdk 28、debuggable=true、run-as 开启；APK 使用 Signature Scheme v2 和既有调试证书。Pad `403G8G00BM611V0 / iPA2693` 原位升级成功，首次安装时间、权限、run-as 和已有游戏数据均保留；拉回 APK 与归档逐字节一致。设备恢复 `2304×1296` 覆盖并启动原版 NFS11，已进入游戏启动画面。

回退基线为上一版：`../GamePad-PC-V2.4-10032/GamePad-PC-V2.4-2304X1296-10032.apk`。
