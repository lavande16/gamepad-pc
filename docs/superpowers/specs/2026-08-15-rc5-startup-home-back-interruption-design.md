# GameFold PC V0.8.2 RC5 启动中 HOME/BACK 中断设计

## 目标

修复游戏启动期间 HOME/BACK 偶发无响应、回到首页仍卡在“正在启动游戏”、无法重新进入游戏的问题，同时保持 RC4 已验证的运行中 HOME 返回和手柄重连能力。

## 已确认根因

- 预启动仍在 Home 路由时，启动卡替换了游戏库，但 HOME、B/Y 和系统 Back 没有取消事务的实现。
- XServer 已创建但游戏窗口尚未映射时，HOME 会先暂停 Wine 再返回 Home；`showBootingSplash` 仍等待窗口映射清除，因此形成“暂停运行时等待运行时完成”的循环依赖。
- XServer 离开时把 Back 回调替换成空函数。环境仍存在时，根级 Back 分发会调用空函数，不会回退到路由策略。

## 方案比较

1. **统一可取消启动事务（采用）**：为预启动分配带代次的 ticket；HOME/BACK 立即取消 ticket、清除两层启动状态。XServer HOME 在暂停前同样清除启动状态；Back 回调离开路由时设为 `null`。优点是行为确定、可重入，并与 NS 的“HOME 始终能回主界面”语义一致。
2. 禁用启动期间 HOME/BACK：实现最小，但违背用户预期，也不对齐 NS。
3. HOME 时强制销毁整个运行时：能解除卡死，但启动代价大，并破坏 RC4 的保留会话能力。

## 状态与数据流

1. 点击游戏时 `GameFoldPcPreLaunchSingleFlight.tryStart(appId)` 返回唯一 ticket。
2. 预启动、进度更新及 `MainViewModel.launchApp` 在关键边界检查 ticket 是否仍有效。
3. Home 启动卡收到 HOME、B、Y 或系统 Back 时调用统一取消动作：取消当前 ticket，清除 `loadingDialogVisible` 与 `showBootingSplash`，继续显示正常游戏库。
4. XServer 中收到 HOME 时，在暂停 Wine 和切换路由之前先清除根启动状态；回到 Home 后不再显示启动卡。
5. XServer 注册的 Back 回调改为可空所有权；路由销毁时释放为 `null`，不再留下永久空函数。

## 边界行为

- 取消后，旧事务即使完成耗时的文件检查，也不得更新进度或导航到 XServer。
- 取消后允许立即重新点击同一游戏；旧 ticket 的结束不得删除新 ticket。
- XServer 已经显示时，Back 仍进入退出确认，HOME 仍保留会话返回首页。
- 非 GameFold PC local shell 的现有行为不变。

## 验证

- 单元测试覆盖 ticket 取消/重启隔离、Home 启动卡按键决策、根 Back 路由决策。
- 全量 Legacy Release 单测与构建通过。
- vivo 实机分别在预启动阶段、XServer 已创建但窗口未映射阶段按 HOME/BACK，验证回到正常首页且可重新进入游戏。
