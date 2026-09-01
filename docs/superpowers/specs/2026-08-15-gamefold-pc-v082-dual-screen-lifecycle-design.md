# GameFold PC V0.8.2 双屏生命周期设计

## 目标与边界

以 `gamefold-pc-v0.8.1-baseline` 为冻结基线，修正 GameFold PC 双屏会话在 Home、切换到其他应用、进入后台和返回前台时的生命周期行为，尽量对齐 GameFold NS：

- GameFold PC 不在前台时，双屏会话必须自动关闭。
- 关闭后返回 GameFold PC 不得自动恢复双屏，也不得卡死。
- 启动副屏 Activity 产生的内部焦点/生命周期切换不能被误判为离开应用。
- 保留 V0.8.1 已验证的副屏触控、黑场后复位和统一关闭链。
- 不修改 FC、NS、PC 容器、Wine/Proton、输入协议和游戏运行时。

本次产物是独立候选版本 `0.8.2`，不覆盖 V0.8.1 基线，也不直接晋升为新基线。

## 已确认问题

### PC 与 NS 的关键差异

PC 的前屏失焦延迟任务额外要求 `userLeaving == true` 才关闭双屏；NS 在内部副屏启动完成后，只要前屏持续失焦就会关闭。Android 切换到其他应用时不保证一定先触发 `onUserLeaveHint()`，因此 PC 会漏关。

PC 还用固定宽限时间整体忽略 `onUserLeaveHint()`。这能规避启动副屏时的误触发，但也可能吞掉宽限期内真实的 Home/切应用事件。NS 只在副屏 Activity 正在启动的明确窗口内忽略内部事件。

副屏触控 Activity 在每次布局回调中无条件重写 `layoutParams` 和 `rotation`；回前台异常路径中会形成连续 `requestLayout()`，放大主线程压力和卡顿风险。

### 上机日志结论

- 标准 Home 路径曾成功进入 `desired=single` 并执行黑场、设备状态复位。
- 切换到系统设置可稳定复现双屏残留；返回后多次 reconcile 仍保持 `desired=dual`。
- 同一时间窗出现密集 `requestLayout()`，但没有 ANR/FATAL，因此当前证据更支持“漏关会话 + 布局风暴导致假死/卡顿”，而不是已确认的 Java 崩溃。

## 选定方案：NS 语义 + 有界内部切屏事务

单纯删除 PC 的保护条件会在启动副屏 Activity 时误关；继续依赖固定宽限时间又会漏掉真实离开事件。因此把“内部副屏启动”建模为一个短暂、可结束、可超时的事务。

### 状态

- `internalRearTransition`：正在执行前屏到副屏 Activity 的内部切换。
- `pendingUserLeave`：内部切换期间收到了 `onUserLeaveHint()`，暂存而不是丢弃。
- `frontStartedCount`：属于 GameFold PC 的前屏 Activity 启动计数。
- `frontResumed` / `frontWindowFocused`：前屏实际前台信号。
- `rearStarting`、会话状态和 generation：继续使用现有字段，避免并发旧任务污染新会话。

### 内部切屏配对

1. 请求启动副屏 Activity 前开启 `internalRearTransition`，记录 generation，并设置超时兜底。
2. 事务期间收到 `onUserLeaveHint()` 时只设置 `pendingUserLeave=true`。
3. 副屏 Activity ready 后结束事务；若前屏仍 started/resumed，则把暂存事件判定为内部切屏并清除。
4. 若事务结束或超时时前屏已经不在前台，则把暂存事件升级为真实离开并关闭双屏。

### 前台丢失判定

采用三个互补信号，任一稳定成立即可进入同一关闭链：

1. 非内部切屏期间的 `onUserLeaveHint()`：短延迟后检查前屏是否仍在前台。
2. 内部切屏结束后的持续前屏失焦：恢复 NS 语义，不再依赖 `userLeaving` 硬门槛。
3. `frontStartedCount == 0`：`onStop()` 后延迟复核，覆盖没有 hint 或焦点回调的系统路径。

所有延迟任务都绑定当前 generation，并在前屏重新 started/resumed/focused 时取消，防止瞬时回调误关。

### 关闭与返回前台

所有后台信号统一调用现有 `closeSession()`，保留顺序：

1. 释放副屏输入；
2. 副屏显示黑场；
3. 等待 frame commit 或超时；
4. 执行 `device_state reset`；
5. 禁用 display 3、结束副屏任务；
6. 会话进入 OFF。

回到 GameFold PC 时只恢复单屏 UI。用户必须再次点击双屏按钮才能开启新会话，任何旧 runnable、ready 回调或 reconcile 都不能复活已关闭 generation。

### 重复请求与布局幂等

- 当当前 generation 已是健康的 ACTIVE 双屏，重复 `requestDualScreen()` 直接 no-op，不再重复启动/迁移副屏。
- 副屏布局先计算目标宽、高、旋转角；仅在值发生变化时更新 `layoutParams` 或 `rotation`。
- 沉浸式标志保持现有视觉行为，但改为幂等应用，避免焦点回调引起无效布局循环。

## 验收矩阵

| 场景 | 预期结果 |
|---|---|
| 单屏启动双屏 | 副屏正常显示和触控，内部切屏不误关 |
| 双屏按 Home | 自动关闭，最终为单屏设备状态 |
| 双屏切到系统设置 | 自动关闭，设置界面不保留副屏 |
| 从系统设置返回 GameFold PC | 不自动恢复双屏，UI 可操作，无连续布局风暴 |
| 双屏最近任务切换到其他应用 | 自动关闭；返回不复活 |
| 双屏锁屏再解锁 | 双屏关闭；回到应用为单屏 |
| 双屏状态重复点击/重复 reconcile | 不重复启动副屏 Activity，不产生布局风暴 |
| 关闭按钮、进程结束、Activity 销毁 | 继续走统一安全关闭链 |

## 风险控制

- 生命周期事件顺序由 vivo 系统决定，所有判断必须使用最终前台状态复核，不能只相信单个回调。
- 延迟常量只用于去抖和等待配对，不作为“这段时间一律忽略离开”的宽限期。
- 不清除应用数据或权限；候选 APK 原位安装，验收后记录包信息、安装时间和拉回 APK 的 SHA-256。
- 若主动游戏运行中仍出现卡死，将单独抓取主线程/ANR/native tombstone；本次不在没有证据时改动 Wine/容器暂停恢复逻辑。
