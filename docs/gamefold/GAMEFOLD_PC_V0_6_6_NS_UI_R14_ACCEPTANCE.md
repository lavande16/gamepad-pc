# GameFold PC 0.6.6 NS UI R15 验收记录

## 输入冻结

- PC 稳定基线：`gamefold-pc-v0.6.6-baseline`
- PC 源 commit：`c0320448b6256ada0d6e175385d4c8ca43df64d9`
- PC 基线 APK SHA-256：`3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad`
- NS 2.9 R2 APK SHA-256：`156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292`

## 本候选包

- APK：[GameFold-PC-V0.6.6-NS-UI-R15-10072.apk](../../releases/candidates/V0.6.6-NS-UI-R15/GameFold-PC-V0.6.6-NS-UI-R15-10072.apk)
- package：`com.gamefold.pc.v066nsrc`
- version：`0.6.6-gf-ns-r15 / 10072`
- SHA-256：`dc79f8c3d3e21a9bba841c449c4871a5de5ab5790b2672d49c15e48141ad1e39`
- signer SHA-256：沿用独立候选签名 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`

## 代码与自动化验收

- 首次启动明确回退为成熟方案：处理器检查 → 文件访问权限；完成后直接进入首页。首页不导入 Proton/ImageFS；点击游戏后沿用既有启动加载流程。
- 0.6.6 默认值保持：LSFG-VK `0.50`、屏幕效果 `FSR 适配`、Steam Input 关闭、XInput+DInput 开启、Preferred Input API `BOTH`、DInput Mapper `Standard`、快捷菜单默认关闭、退出警告默认开启。
- 分辨率迁移为一次性 `null/1280x720 → 1067x600`；自定义值和已有游戏分辨率保留；新游戏从全局默认继承。
- GameFold 官方壳：卡带式游戏卡片、方形 PC 封面、添加游戏路径、长按游戏设置页、GameFold 启动品牌、动态进度、左上角 FPS、上屏运行画面和下方官方手柄。
- 本轮 focused release unit tests：37 tests，0 failures，0 errors，0 skipped；包含默认值、迁移、启动 gate、控制器输入、后屏输入路由和后屏策略。
- Release compile：`compileLegacyReleaseKotlin`、`compileLegacyReleaseJavaWithJavac`、`assembleLegacyRelease` 均 `BUILD SUCCESSFUL`。

## vivo 真机证据

设备：vivo V2436A，serial `10AF3P0SDY001M6`。

- R15 独立包已安装成功，设备回读 SHA 与候选一致；冷启动出现首页 `本地游戏`、`添加游戏`、游戏卡片、`GameFold PC`、`高级设置`，未出现首次资源导入遮罩，无 `FATAL EXCEPTION` / `ANR`。
- 高级设置显示 `GameFold 双屏手柄` 开关，图标样式入口未显示；开关打开后设备创建 Display 3（`local:secondary`，2748×1172），并启动 `GameFoldRearTouchActivity` 于 `d3`，后屏获得焦点。
- 正面截图：`/private/tmp/gamefold-r12-front.png`；后屏截图：`/private/tmp/gamefold-r12-rear.png`。后屏视觉为官方 ZL/ZR 触控图和 GameFold 标识。
- R14 再次启用双屏后，在点击游戏期间持续观察到 Display 3 和 `GameFoldRearTouchActivity` 保持存在；日志确认启动阶段的焦点切换只被当作瞬时过渡，不再误关双屏：`front focus transient with no user-leave; keep rear session`。该修复包含在 R15。
- R14 点击游戏进入 `GameFold` / `Launching game...` 动态启动态，运行过程中后屏保持；未出现崩溃。截图：正面启动态 `/private/tmp/r14-touch-front.png`，后屏 `/private/tmp/r14-touch-rear.png`。
- 长按 `Cyberpunk 2077` 进入独立设置页，UI 树出现 `启动游戏`、`容器设置`、`打开容器`、`测试图形`、`诊断启动`、`返回`。
- 返回流程：快捷菜单默认关闭时第一次 Back 出现“再次按返回键退出”，第二次 Back 进入退出流程。

## 当前边界

- 后屏 Activity、设备状态、视觉资源和 PC 控制器桥接已经接通；实体手指触控的物理触感与两个目标游戏的长时输入稳定性仍需在设备上由人工确认，不能由 ADB 截图替代。
- 通用 Presentation 双屏 fallback 保留；vivo 私有 display-state 路径只在支持的 vivo 横屏设备显示开关。
- R15 是独立候选包，不覆盖 `gamefold-pc-v0.6.6-baseline`；`ContainerManager.createDefaultContainerFuture` 已改为从全局 GameFold 默认读取 `1067x600`，不在每游戏初始化处硬编码。
