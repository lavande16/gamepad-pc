# GameFold PC V0.8.2 双屏生命周期 RC1 证据（实机失败）

RC1 不可用于验收，已由 RC2 替代。真机正常开启双屏时，vivo 在副屏 ready 后约 355ms 才补发内部失焦和 `onUserLeaveHint()`；RC1 在 ready 时立即结束内部事务，因而在 450ms 失焦复核时误关双屏。

证据见 `first-enable.log`：

- `16:26:10.216`：副屏 ready，RC1 立即结束配对事务。
- `16:26:10.571`：内部前屏失焦。
- `16:26:10.576`：内部 user-leave hint。
- `16:26:11.021`：被误判为后台，执行 single reset。

RC2 增加 ready 后 650ms settle 配对窗口，并要求“前屏曾在事务后重新确认焦点”才允许纯失焦兜底关闭；真实 user-leave 和 onStop 仍可独立关闭。

## 身份

- 冻结基线：`gamefold-pc-v0.8.1-baseline` / `58702d48f4bbad7fb1a50e695336e84d63082937`
- 候选源码：`73676c6935032cb8ac378d5b4597ff6848dd9caf`
- 包名：`com.gamefold.pc`
- 版本：`0.8.2` / `10131`
- APK：`releases/candidates/V0.8.2-DUAL-SCREEN-RC1/GameFold-PC-V0.8.2-DUAL-SCREEN-RC1-10131.apk`
- APK SHA-256：`b9713c8c7bf68c889efe4f896e218d8760428ced4643ef67402acfb0f4cc33cb`
- 签名证书 SHA-256：`24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`

签名与 V0.8.1 基线一致，可原位升级；V0.8.1 基线 APK SHA-256 复核仍为 `df8aff8e9556cbe69c2f753f3770fa3dc14f3d7b5c831f2dfdb1bf4cc407e870`。

## 静态与构建验收

- `GameFoldRearDisplayPolicyTest`：RED 后 GREEN。
- `GameFoldPcFrontendPolicyTest` 生命周期与布局契约：RED 后 GREEN。
- 完整 `testLegacyReleaseUnitTest`：323 tests，0 failures，0 errors，0 skipped。
- `assembleLegacyRelease`：BUILD SUCCESSFUL。
- `git diff --check`：通过。
- 相对 V0.8.1 的产品代码只涉及后屏策略、后屏 Activity、后屏控制器和版本号；未改 FC、NS、Wine/Proton、容器、输入协议或游戏运行时。

## 实机矩阵

RC1 已于 2026-08-15 16:24:47 原位安装，未清数据或权限。首项即失败，后续矩阵转由 RC2 执行：

1. 单屏手动开启双屏，不因内部 Activity 切换误关。
2. 双屏按 Home，自动回单屏。
3. 双屏切到系统设置，自动回单屏。
4. 从系统设置返回，不自动复活双屏，无卡死/布局风暴。
5. 最近任务切换、锁屏解锁均关闭双屏。
6. 返回后再次手动开启，副屏触控和关闭链正常。

RC1 手机拉回 APK SHA-256 与归档包一致：`b9713c8c7bf68c889efe4f896e218d8760428ced4643ef67402acfb0f4cc33cb`。
