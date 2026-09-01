# GameFold PC 0.6.6 NS UI R11 验收记录

## 输入冻结

- PC 稳定基线：`gamefold-pc-v0.6.6-baseline`
- PC 源 commit：`c0320448b6256ada0d6e175385d4c8ca43df64d9`
- PC 基线 APK SHA-256：`3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad`
- NS 2.9 R2 APK SHA-256：`156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292`

## 本候选包

- APK：[GameFold-PC-V0.6.6-NS-UI-R11-10068.apk](../../releases/candidates/V0.6.6-NS-UI-R11/GameFold-PC-V0.6.6-NS-UI-R11-10068.apk)
- package：`com.gamefold.pc.v066nsrc`
- version：`0.6.6-gf-ns-r11 / 10068`
- SHA-256：`4c32e402190e530531bb60a4b1cc899136ca7121d357325e219bd40e9bff0912`
- signer SHA-256：`24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`

## 已通过

- 基线 guard：PC 0.6.6 与 NS 2.9 R2 哈希、tag、包元数据匹配。
- Legacy release focused 回归：`GameFoldPcDefaultsTest`、`GameFoldPcPresentationPolicyTest`、`GameFoldPcPerGameDefaultsTest`、`GameFoldPcRuntimeProvisionerTest`、`GameFoldPcRuntimeProvisionStoreTest`、`GameFoldPcProcessorPolicyTest`，Gradle `BUILD SUCCESSFUL`。
- 新增并通过 `GameFoldControllerInputTest`：固定官方 2480×2200 参考几何、方向键/ABXY、肩键/扳机、菜单键、HOME、摇杆死区与归一化契约，Gradle `BUILD SUCCESSFUL`。
- 首次启动：按用户决定回退为成熟流程，只做处理器与文件访问权限检查，通过后直接进入首页；不在首页校验 manifest 或导入 ImageFS/Proton。
- 新游戏默认：从全局 `PrefManager.screenSize` 继承，GameFold PC 本地壳默认 `1067x600`，不在每游戏初始化硬编码覆盖。
- 启动外壳：处理器检查先于权限；授权返回自动刷新；资源完整性与运行时导入保留在点击具体游戏的启动流程。
- UI：本地库使用 NS 官方卡带帧与添加游戏卡带资源，PC 方形封面叠入中框；长按进入单独游戏设置；添加游戏文案指向 `GameFoldPC/game`；本地壳隐藏图标样式入口；启动品牌为 GameFold 且隐藏固定 25 条提示；动态状态/进度保留。
- 运行策略：游戏画面与底部官方触控控制在竖屏/横屏壳中分区；FPS HUD 使用像素等宽字体并固定左上角；旧 PC 触控层在本地壳中不再绘制，但 WinHandler 保留独立虚拟手柄通道。

## 设备边界

已在 vivo V2436A（serial `10AF3P0SDY001M6`）安装并启动 R11；设备元数据为 `versionCode=10068`、`versionName=0.6.6-gf-ns-r11`，设备回读 APK SHA-256 与候选包一致。清理候选包数据后冷启动，约 8 秒进入首页，UI 树出现 `GameFold PC`、本地游戏卡片、添加游戏卡带和 `高级设置`，未出现 `INSTALLING_SHARED_RUNTIME`，无 ANR/FATAL。R9/R10 同设备进一步进入真实点击时资源加载、GameFold 启动品牌/动态状态、官方上屏+下方手柄壳，并验证第一次返回提示、第二次返回回首页；稳定基线包 `com.gamefold.pc.v04localshell` 未修改。

R10 设备截图证据（本机临时捕获）：首页 `/private/tmp/gamefold-r10-home.png`；R9 运行壳 `/private/tmp/gamefold-r9-runtime2.png`；R9 返回警告 `/private/tmp/gamefold-r9-back.png`；R10 默认配置 UI 树显示 `1067x600 (16:9)`。

## 不得误判为完成的项目

本 RC 已把 PC 运行核心接入 GameFold 壳，并按要求把首次资源导入回退到点击游戏时执行；NS 2.9 R2 的官方手柄/卡带资源按迁移源纳入 PC 工程。通用 `Presentation` 双屏路径仍保留，vivo 后屏私有 display-state 生命周期仍单独列为设备专项，不以通用路径冒充已验证的 vivo 后屏交付。仍需后续专项验收：完整的真机多点按键到游戏输入映射、vivo 后屏生命周期/无闪屏、横竖屏切换全路径、两个目标游戏的长时稳定性，以及像素级全屏差分。候选包不会覆盖 0.6.6 稳定基线。
