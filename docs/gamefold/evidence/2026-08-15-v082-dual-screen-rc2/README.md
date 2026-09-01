# GameFold PC V0.8.2 双屏生命周期 RC2 证据

## 身份

- 冻结基线：`gamefold-pc-v0.8.1-baseline` / `58702d48f4bbad7fb1a50e695336e84d63082937`
- 候选源码：`60de8d6e58827bbe4f6d2d77427864b2bfd2cdf2`
- 包名：`com.gamefold.pc`
- 版本：`0.8.2-rc2` / `10132`
- APK：`releases/candidates/V0.8.2-DUAL-SCREEN-RC2/GameFold-PC-V0.8.2-DUAL-SCREEN-RC2-10132.apk`
- APK SHA-256：`fc790b4d00ffd78e7bdde32cc5a591490e9060b2310fff417010048b3c6bf359`
- 签名证书 SHA-256：`24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`

## RC1 反馈闭环

RC1 首次实机启用暴露 vivo 在副屏 ready 后延迟约 355ms 才发送内部失焦/leave hint。RC2 已用测试冻结这一时序，并增加 650ms settle 配对窗口；纯失焦兜底只有在前屏于事务后重新确认焦点时才启用。

## 静态与构建验收

- 新增 RC1 真机时序回归测试：RED 后 GREEN。
- 完整 `testLegacyReleaseUnitTest`：324 tests，0 failures，0 errors，0 skipped。
- `assembleLegacyRelease`：BUILD SUCCESSFUL。
- `git diff --check`：通过。
- 签名与 V0.8.1/RC1 一致；V0.8.1 基线 APK 哈希未变化。

## 实机矩阵

设备：vivo `V2436A`，序列号 `10AF3P0SDY001M6`。RC2 于 `2026-08-15 16:35:29` 原位安装，未清数据、未重置权限。手机拉回 APK SHA-256 与归档包一致：`fc790b4d00ffd78e7bdde32cc5a591490e9060b2310fff417010048b3c6bf359`。

| 场景 | 结果 | 关键证据 |
|---|---|---|
| 首次手动开启双屏 | PASS | 5 秒后仍为 `DOUBLE`；内部 hint 在 650ms settle 事务内被配对为 `KEEP_SESSION` |
| 双屏按前屏 Home | PASS | 进入 vivo 桌面，450ms 后 `front-window-focus-lost`，最终 `OPENED` |
| 双屏切系统设置 | PASS | `front-activities-stopped` 触发统一关闭链，最终 `OPENED` |
| 从系统设置返回 | PASS | HOT 返回，8 秒仍为 `OPENED`，无新的 `desired=dual` |
| 返回后再次手动开启 | PASS | 新 generation 正常进入 `DOUBLE`，证明 UI/状态机未卡死 |
| 双屏进入最近任务 | PASS | 前屏焦点确认后失焦，450ms 关闭，最终 `OPENED` |
| 双屏息屏 | PASS | `mWakefulness=Asleep` 时由 onStop 关闭，最终 `OPENED`；唤醒后未复活 |
| 布局/异常检查 | PASS | 所有场景 `requestLayout() improperly` 为 0；无 ANR、FATAL EXCEPTION |

关键原始日志：`settings-return.log`。日志显示：

1. generation 8 正常进入 dual；
2. 副屏 ready 后保留 settle，最终 `decision=KEEP_SESSION`；
3. 切到设置时 `front-activities-stopped`；
4. 黑场 frame commit 后执行 reset/disable；
5. 返回前台后没有任何新的 dual reconcile。

## 验收边界

本轮已完成首页/空闲状态的客观生命周期验收。没有启动 GTA 5 或其他 PC 游戏，因此“游戏正在运行时切后台再返回”的 Wine/XEnvironment 主线程恢复体验仍需要用户做一次主观终验；若该场景仍卡顿，应另抓主线程、ANR/native tombstone 后再决定是否扩展到运行时暂停恢复逻辑。
