# GameFold PC 竖屏陀螺仪方向修复设计

## 目标

在不改变 GameFold PC 1.0.0 横屏陀螺仪手感、灵敏度、ZL 门控和鼠标传输链路的前提下，修复 vivo 与 OPPO 竖屏模式下左右、上下同时反向的问题，并产出 `1.0.1 / 10164` Release RC1。

## 现场结论

- 品牌不是变量：vivo 与 OPPO 均为横屏正确、竖屏双轴反向。
- `GameFoldOrientation` 是产品内部横竖屏选择的唯一权威来源。
- 现有 `GyroMouseProcessor` 和测试只锁定了横屏方向；竖屏缺少输出符号策略与回归测试。

## 设计

新增一个纯函数式方向策略：横屏原样返回 `GyroMouseDelta`，竖屏返回 `(-dx, -dy)`。该策略在浮点陀螺仪输出进入 `FractionalMouseDeltaAccumulator` 之前执行，避免改变余量累计精度。

`AndroidGyroMouseController` 在创建时接收本次游戏会话的 `portraitMode`。`XServerScreen` 使用 `GameFoldOrientation.isPortrait(context)` 传入该值。GameFold 的方向切换会重建 Activity/运行视图，因此会话内缓存方向不会产生过期状态。

## 范围约束

- 不增加 OPPO、vivo 或其他品牌判断。
- 不修改 `19.6 counts/degree`、水平/垂直 `1x` 灵敏度、偏置校准、采样周期或最大时间间隔。
- 不修改 ZL 按下/松开逻辑、Wine raw mouse 路由或 GTA5 Windows 输入限制。
- 不改写已冻结的 `gamefold-pc-v1.0.0-baseline`；新版本是独立的 1.0.1 RC1。

## 验证

- 单元测试证明竖屏双轴同时取反、横屏输出逐值保持、零输出保持为零。
- 现有陀螺仪与全量 `legacyRelease` 测试全部通过。
- APK badging 必须为 `com.gamefold.pc`、`1.0.1 / 10164`。
- 在当前 OPPO PLP110 原位安装，设备回读版本与 APK SHA-256 必须和归档包一致。
