# GameFold PC 0.7.1 → 0.7.2 × GameFold NS UI 更新与验收清单

## 1. 文档目的

本文汇总 2026-08-14 已确认的 GameFold PC UI 与交互修改需求，并记录本轮实现与验收证据，作为后续任务拆分、截图对照和真机验收的统一依据。

本文件区分“代码已实现”“候选包已构建/安装”和“用户最终视觉与游戏行为确认”。任何一项都不能仅因代码已修改或 APK 能安装而标记为最终通过：

1. 对照 GameFold NS 同场景源码与参考截图；
2. 在 GameFold PC 独立候选版本中实现；
3. 完成自动化回归和真机交互验证；
4. 由用户确认视觉和实际游戏行为后，才能标记为通过。

## 2. 基线与边界

### 2.1 当前开发基线

- 产品：GameFold PC
- 当前基线：`V0.7.2-NS-UI-R56-10121`
- APK：`releases/baselines/V0.7.2/GameFold-PC-V0.7.2-NS-UI-R56-10121.apk`
- APK SHA-256：`00a4a5045a257b332c46a1550e418825ede37ebb88bf3db7d2b8d3ec456c06b0`
- 包名：`com.gamefold.pc`
- `versionName` / `versionCode`：`0.7.2` / `10121`
- 构建开关：`GAMEFOLD_PC_LOCAL_SHELL=true`、`GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- 源码提交：`f309b6d`（`feat: finalize GameFold PC 0.7.2 R56 UI baseline`）
- Git Tag：`gamefold-pc-v0.7.2-baseline`
- 基线说明：`docs/CURRENT_GAMEFOLD_PC_V0_7_2_BASELINE.md`

0.7.2 是在冻结的 0.7.1 基础上承接本轮 NS UI 与会话逻辑修正后的新基线；0.7.1 APK、提交和标签继续保留，可用于回滚。

### 2.2 视觉与交互参考

- GameFold PC 当前截图：`/Users/11085429/Desktop/GameFold PC/`
- GameFold NS 对照截图：`/Users/11085429/Desktop/gamefold NS/`
- GameFold NS 源码：`/Users/11085429/Documents/Game Fold/gamefold-auto-assembly-v1/src/android/`

### 2.3 必须保留的能力

- 不修改 GameFold PC 已验证的离线扫描、可执行文件识别、DirectX Wrapper 选择、Bionic/Proton、Wine、容器和游戏启动语义。
- Cyberpunk 2077、GTA5、The Ramp、黑神话像素版必须继续正常启动和运行。
- 不修改 GameFold FC/NES 产品及其源码。
- 不覆盖或改写 0.7.1 冻结基线；开发产物必须使用新的候选版本标识、版本号、APK 文件和哈希记录。
- 验收安装默认保留用户数据、权限、游戏目录与既有容器，除非另行明确授权。

### 2.4 本轮实现候选与证据

- 工作分支：`codex/offline-launch-analyzer-candidate`
- 候选 R24：`releases/candidates/V0.7.1-NS-UI-R24/GameFold-PC-V0.7.1-NS-UI-R24-10088.apk`
- 候选 R24 SHA-256：`dae028ae0982b36c120be07e87282f9e96b1d460c85740b214f3d58f48797198`
- 候选 R25（包含冷启动焦点重试修正）：`releases/candidates/V0.7.1-NS-UI-R25/GameFold-PC-V0.7.1-NS-UI-R25-10089.apk`
- 候选 R25 SHA-256：`b3f0f1484d17784aa44ce3d5dcb1ecbe2c0bea37a27b135400fc7f584d75d6eb`
- R25 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R25`，`versionCode=10089`，debug 签名。
- R25 已安装设备：`10AF3P0SDY001M6`（vivo V2436A）；设备拉回 APK SHA-256 与构建产物一致，安装时间 `2026-08-14 01:57:20`。
- 最终候选 R26：`releases/candidates/V0.7.1-NS-UI-R26/GameFold-PC-V0.7.1-NS-UI-R26-10090.apk`
- R26 SHA-256：`67f7d02fe4b4d637e2c109bb0ba81454eba84596da009aa3f6eb21afa5287932`
- R26 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R26`，`versionCode=10090`，debug 签名；已安装同一设备，设备拉回哈希一致，安装时间 `2026-08-14 02:03:55`。
- 最终 R27（退出弹窗 NS 对齐修正）：`releases/candidates/V0.7.1-NS-UI-R27/GameFold-PC-V0.7.1-NS-UI-R27-10091.apk`
- R27 SHA-256：`fcbb33ab4372b60f81d8d7003c28864b829d07efbd8f1b77f5b7da7de8d18b13`
- R27 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R27`，`versionCode=10091`，debug 签名；已安装同一设备，设备拉回哈希一致，安装时间 `2026-08-14 02:11:33`。
- 当前候选 R28（补齐全局 A/X、B/Y 焦点语义；移除加载卡片自身的全屏黑色遮罩）：`releases/candidates/V0.7.1-NS-UI-R28/GameFold-PC-V0.7.1-NS-UI-R28-10092.apk`
- R28 SHA-256：`f0a0c9dc6e91e5e8a30f7ed36c4e3994231cf28ea4e27b2cbc16544c316845bb`
- R28 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R28`，`versionCode=10092`，debug 签名；已安装设备 `10AF3P0SDY001M6`，设备拉回 APK SHA-256 与构建产物一致，安装时间 `2026-08-14 02:21:39`。
- 当前候选 R29（快速上手和通用像素弹窗增加默认手柄焦点）：`releases/candidates/V0.7.1-NS-UI-R29/GameFold-PC-V0.7.1-NS-UI-R29-10093.apk`
- R29 SHA-256：`caf70840e6bea4e7513c2996b62934aadf8438de84a54b2cfc099f0d52a9a166`
- R29 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R29`，`versionCode=10093`，debug 签名；已安装设备 `10AF3P0SDY001M6`，设备拉回 APK SHA-256 与构建产物一致，安装时间 `2026-08-14 02:26:29`。
- 候选 R30（授权背景页、快速上手页头、设置/关于页和游戏设置浅色 Tab/方形开关）：`releases/candidates/V0.7.1-NS-UI-R30/GameFold-PC-V0.7.1-NS-UI-R30-10094.apk`
- R30 SHA-256：`37ef0ebcafb1e57fc441fa699f2173b3e04c8f56b15a4288a168b8158aba44b8`
- R30 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R30`，`versionCode=10094`，debug 签名；设备拉回 APK SHA-256 与构建产物一致，安装时间 `2026-08-14 02:42:44`。
- 当前候选 R31（首页显式手柄选中索引与 A/X 确认路径）：`releases/candidates/V0.7.1-NS-UI-R31/GameFold-PC-V0.7.1-NS-UI-R31-10095.apk`
- R31 SHA-256：`568446b4bc2096fab680a0895974771325dc4f0f1817eef86c8134804b422da1`
- R31 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R31`，`versionCode=10095`，debug 签名；已安装设备 `10AF3P0SDY001M6`，设备拉回 APK SHA-256 与构建产物一致。
- 候选 R32（修复 ContextWrapper 下横竖屏/双屏按钮不生效）：`releases/candidates/V0.7.1-NS-UI-R32/GameFold-PC-V0.7.1-NS-UI-R32-10096.apk`，SHA-256：`0c766446eac69014b5cc65991901069b59f37205c97659ac4b94ddeb82799284`。
- 候选 R33（快速上手横屏几何对齐）：`releases/candidates/V0.7.1-NS-UI-R33/GameFold-PC-V0.7.1-NS-UI-R33-10097.apk`，SHA-256：`b54f27ddb9cb65d642bbd6f61a2243335e23846bbf00ddc87626272ce3f1916e`。
- 当前候选 R34（品牌区、右上角按钮和快速上手正文最终几何对齐）：`releases/candidates/V0.7.1-NS-UI-R34/GameFold-PC-V0.7.1-NS-UI-R34-10098.apk`。
- R34 SHA-256：`28fcec741753d0f9194dfb021a07a921edeb1e5999d171ba2139ad2d474aac9f`。
- R34 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R34`，`versionCode=10098`，debug 签名；已安装设备 `10AF3P0SDY001M6`，设备拉回 APK SHA-256 与构建产物一致，安装时间 `2026-08-14 03:36:34`。
- 最终候选 R35（纳入双屏入口 ContextWrapper 修正并重新以明确版本参数构建）：`releases/candidates/V0.7.1-NS-UI-R35/GameFold-PC-V0.7.1-NS-UI-R35-10099.apk`。
- R35 SHA-256：`8524b2b669f3b6e3e238f6d8cfd10ea60621e1899f502ecd1dbca260aab9cea8`。
- R35 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R35`，`versionCode=10099`，debug 签名；已安装设备 `10AF3P0SDY001M6`，设备拉回 APK SHA-256 与构建产物一致。
- 最终候选 R36（快速上手/设置显式手柄选中轨道、加载卡片透明叠层）：`releases/candidates/V0.7.1-NS-UI-R36/GameFold-PC-V0.7.1-NS-UI-R36-10100.apk`。
- R36 SHA-256：`f2803b9e4d3fe6ef23689f68cf2e7c36649d2b91a4ab484dd41630a20e548d9a`。
- R36 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R36`，`versionCode=10100`，debug 签名；已安装设备 `10AF3P0SDY001M6`，设备拉回 APK SHA-256 与构建产物一致，安装时间 `2026-08-14 04:12:53`。
- 最终候选 R37（补齐快速上手/设置的显式手柄确认分发与焦点请求）：`releases/candidates/V0.7.1-NS-UI-R37/GameFold-PC-V0.7.1-NS-UI-R37-10101.apk`。
- R37 SHA-256：`73ae8d533e881f43684931b1c032e8335634f66dca5d06643452fefe4e0d6325`。
- R37 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R37`，`versionCode=10101`，debug 签名；构建成功且 `aapt dump badging` 与哈希均匹配。vivo 真机安装阶段两次返回 `INSTALL_FAILED_ABORTED: User rejected permissions`，因此 R37 尚未完成设备安装验证；当前设备仍保留已验证的 R36，不能把 R37 的焦点修改标记为真机通过。
- 最新候选 R38（加载阶段文案本地化、NS 字体标识/标题按钮素材复用）：`releases/candidates/V0.7.1-NS-UI-R38/GameFold-PC-V0.7.1-NS-UI-R38-10102.apk`。
- R38 SHA-256：`92acac5db0c47ff00246aa452db0298d1d03ff0f49f7a5a35910335223f4ec21`。
- R38 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R38`，`versionCode=10102`，debug 签名；release 构建成功且 `aapt dump badging` 与哈希匹配。vivo 真机安装仍返回 `INSTALL_FAILED_ABORTED: User rejected permissions`，因此当前设备继续保留已验证的 R36，R38 只能作为待安装候选。
- 最新候选 R39（设置详情 A/X 确认、详情返回焦点闭环：首页顶部双屏/快速上手/设置三入口加入同一条手柄选择轨道）：`releases/candidates/V0.7.1-NS-UI-R39/GameFold-PC-V0.7.1-NS-UI-R39-10103.apk`。
- R39 SHA-256：`10adffb38667105ca39513285192478510bc89eb6446a70c11dd8b228724d876`。
- R39 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R39`，`versionCode=10103`，debug 签名；release 构建成功且 `aapt dump badging` 与哈希匹配。vivo 真机安装仍返回 `INSTALL_FAILED_ABORTED: User rejected permissions`，因此当前设备继续保留已验证的 R36，R39 只能作为待安装候选。
- 最新候选 R40（游戏单独设置页的手柄状态路由：B/Y 返回详情、A/X/方向键交给设置控件，不再把详情页返回误判为退出应用）：`releases/candidates/V0.7.1-NS-UI-R40/GameFold-PC-V0.7.1-NS-UI-R40-10104.apk`。
- R40 SHA-256：`73e1af45410fb914d5f86e9ff2973ea3ad192e13b1867eb7f7924056e4e93f54`。
- R40 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R40`，`versionCode=10104`，debug 签名；执行 `clean :app:assembleLegacyRelease` 成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- 最新候选 R41（关于页副标题改用 NS 同源 5×7 像素字标，补齐 `H/V/U/N/!` 字形；关于页三张信息卡继续保持真实 NS 图标并可获得焦点）：`releases/candidates/V0.7.1-NS-UI-R41/GameFold-PC-V0.7.1-NS-UI-R41-10105.apk`。
- R41 SHA-256：`11467c9bde6c2eb5cc217d9e3b2c38a8f3661518da58e27d7b04b6da776a4025`。
- R41 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R41`，`versionCode=10105`，debug 签名；`packageLegacyRelease` 完整构建成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- 最新候选 R42（关于页 NS 像素字标与信息卡几何收口：44dp 顶栏、16dp 内容边距、品牌区 42dp 顶部间距、开发者字标与 68/68/56dp 信息卡）：`releases/candidates/V0.7.1-NS-UI-R42/GameFold-PC-V0.7.1-NS-UI-R42-10106.apk`。
- R42 SHA-256：`1466787e1cca78e32c5d0993e2f27809477abb4ca227d61c2d6b71624fa63933`。
- R42 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R42`，`versionCode=10106`，debug 签名；`clean :app:packageLegacyRelease` 完整构建成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- 最新候选 R43（同游戏 Home 恢复不再闪现加载卡；关于页补齐 NS 上移、16dp 列间距与 0.9 品牌缩放；快速上手标题、图标和正文规格对齐 NS）：`releases/candidates/V0.7.1-NS-UI-R43/GameFold-PC-V0.7.1-NS-UI-R43-10107.apk`。
- R43 SHA-256：`2b81e1ccc1fe6e4e1a2c60b31b142866d6bf728fd546cc84b381642d55c78873`。
- R43 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R43`，`versionCode=10107`，debug 签名；`clean :app:packageLegacyRelease` 完整构建成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- 最新候选 R44（统一本地 shell 预启动文案/进度与首页大加载卡状态源；启动阶段更新不再停留在“正在准备运行环境”）：`releases/candidates/V0.7.1-NS-UI-R44/GameFold-PC-V0.7.1-NS-UI-R44-10108.apk`。
- R44 SHA-256：`a73db6c0a520489fbf79d5d07675a57f8cd1166d6cbdb50ca14dbfeea791c7e3`。
- R44 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R44`，`versionCode=10108`，debug 签名；`clean :app:packageLegacyRelease` 与 `:app:assembleLegacyRelease` 均成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- 编译证据：R44 前置 `:app:compileLegacyDebugKotlin` 与 `:app:testLegacyDebugUnitTest` 定向任务成功；新增 `MainStateLoadingContractTest` 与既有 `OfflineLibraryGeometryTest`、`ExitConfirmationPolicyTest`、`GameFoldPcFrontendPolicyTest`、`GameFoldControllerInputTest` 均成功；R44 release 构建成功。
- 最新候选 R45（暂停弹窗采用 NS 窄卡片比例；Home 保留会话状态并在同游戏重新挂载后恢复渲染；新增纯会话替换/恢复策略）：`releases/candidates/V0.7.1-NS-UI-R45/GameFold-PC-V0.7.1-NS-UI-R45-10109.apk`。
- R45 SHA-256：`c5159a40c6dd80f2906ed0bc7b0e21ace1c2e575a87cd1c8aea4695152e35c0a`。
- R45 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R45`，`versionCode=10109`，debug 签名；`packageLegacyRelease` 构建成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- R45 编译/策略证据：`:app:compileLegacyDebugKotlin` 成功；定向单元测试 16 项全通过，包含 `GameFoldSessionPolicyTest`、`GameFoldPcFrontendPolicyTest`、`MainStateLoadingContractTest`、`OfflineLibraryGeometryTest`、`GameFoldControllerInputTest`、`ExitConfirmationPolicyTest`；新增暂停弹窗宽度契约测试覆盖 `cardWidthFraction=0.60f` 与边界约束。
- 最新候选 R46（授权页 NS 显示区比例与功能背景几何契约固化；Home 恢复陈旧会话身份保护；定向回归修正）：`releases/candidates/V0.7.1-NS-UI-R46/GameFold-PC-V0.7.1-NS-UI-R46-10110.apk`。
- R46 SHA-256：`b6c4e76ec74443150a2212f2f3465a3ba82a4065adb8da944d9de7775fa7a2f5`。
- R46 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R46`，`versionCode=10110`，debug 签名；`packageLegacyRelease` 完整构建成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- R46 定向回归：编译成功；15 项目标单元测试全通过，包含 `GameFoldSessionPolicyTest`、`GameFoldPcFrontendPolicyTest`、`MainStateLoadingContractTest`、`OfflineLibraryGeometryTest`、`GameFoldControllerInputTest`、`ExitConfirmationPolicyTest`；授权页契约覆盖浅色功能背景、`/sdcard/gamefold` 说明、NS 方形按钮和 `fillMaxWidth(0.86f)` 显示区比例；新增 `XEnvironment → XServer → WinHandler` 有效性保护，陈旧会话按冷启动处理。
- 最终候选 R47（包含 R46 全部 UI/交互与会话有效性保护的源码一致构建）：`releases/candidates/V0.7.1-NS-UI-R47/GameFold-PC-V0.7.1-NS-UI-R47-10111.apk`。
- R47 SHA-256：`8beaad6070edee356a3b1caf666a83f477e9dc148dfbd20ae044acf6a7cb1b55`。
- R47 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R47`，`versionCode=10111`，debug 签名；`packageLegacyRelease` 完整构建成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- R48（源码一致候选：快速上手页禁用自身入口的焦点，双屏切图仅在获得焦点时叠加绿色选择框；PC Shell 将实体手柄 KeyEvent 接入同一套 GameFoldControllerListener 语义桥）：`releases/candidates/V0.7.1-NS-UI-R48/GameFold-PC-V0.7.1-NS-UI-R48-10112.apk`。
- R48 SHA-256：`94d71b54fdd27d572ec29696ed5fd72768235d64e9c3b77989b23dde8132bd2c`。
- R48 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R48`，`versionCode=10112`，debug 签名；`packageLegacyRelease` 完整构建成功，`aapt dump badging` 与哈希匹配。未重复发起 vivo 安装，设备仍保留 R36。
- R48 定向回归：`:app:compileLegacyDebugKotlin` 与 15 项目标单元测试全通过；新增快速上手焦点轨道、双屏焦点态和实体手柄 KeyEvent 语义桥源码契约测试。
- 最新候选 R49（授权页方向键/左摇杆可进入方形操作；游戏单独设置肩键可切换 NS Tab；设置返回入口取消本地弹性放大；实体手柄桥接补齐肩键 Compose 事件）：`releases/candidates/V0.7.1-NS-UI-R49/GameFold-PC-V0.7.1-NS-UI-R49-10113.apk`。
- R49 SHA-256：`f64e51296f69b60dcd69c5da4a40dbfedb901436207f22aef149fa38713daa53`。
- R49 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R49`，`versionCode=10113`，debug 签名；`packageLegacyRelease` 完整构建成功，`aapt dump badging` 与哈希匹配。
- R49 定向回归：`:app:compileLegacyDebugKotlin` 与 15 项目标单元测试全通过；新增授权页 `dispatchNavigationToCompose`、实体手柄肩键 `R1/L1/R2/L2` Compose 路由和设置静态返回按钮契约检查。
- 最新候选 R50（统一首页、XServer 与 ViewModel 的本地加载文案归一化，消除 `Loading...`、阶段英文和多路径文案漂移）：`releases/candidates/V0.7.1-NS-UI-R50/GameFold-PC-V0.7.1-NS-UI-R50-10114.apk`。
- R50 SHA-256：`80b11fda1e708eb85d63a59fafcb64f8733db77e0cdc398adad93944ffe3fe53`。
- R50 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R50`，`versionCode=10114`，debug 签名；`packageLegacyRelease` 完整构建成功，`aapt dump badging` 与哈希匹配。
- R50 定向回归：`:app:compileLegacyDebugKotlin` 与目标单元测试全通过；新增加载文案契约覆盖 `Loading...`、依赖安装、解压和启动阶段。当前工作树于 2026-08-14 再次重跑同一组编译/定向测试，`BUILD SUCCESSFUL`（49 actionable tasks）。
- NS 素材复用证据：`pixel_icon_orientation_toggle.webp` 与 GameFold NS `mainlineRelease` 打包资源 SHA-256 均为 `e95ddc5685714c19bdeee612a73c29719375b25422a172f3aa21488d30c8b44d`，文件字节一致；双屏开关 `off/on` 素材也分别与 NS 原图字节一致（`246afe…67a42` / `9583a6…7f84`）。
- 最新候选 R51（实体手柄 Guide/Home 键在本地 Shell 中强制走 NS Home 暂停并回首页路径）：`releases/candidates/V0.7.1-NS-UI-R51/GameFold-PC-V0.7.1-NS-UI-R51-10115.apk`。
- R51 SHA-256：`c795c9ee415b4d7d919690b90b0bd5660c3c1bb5ca435c867866f5fdad7330ca`。
- R51 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R51`，`versionCode=10115`，debug 签名；`packageLegacyRelease` 构建成功，`aapt dump badging` 与哈希匹配。R51 尚未发起真机安装，避免重复触发 vivo 权限拦截。
- R51 定向回归：`:app:compileLegacyDebugKotlin` 与目标单元测试全通过；新增实体 `KEYCODE_BUTTON_MODE` → `returnHomeFromGame()` 及具体 `Installing Bionic` 文案契约。
- 最新候选 R52（设置页返回入口未选中态去除多余灰色方框，改为 NS 图标态；仅获得手柄焦点时显示绿色选择框）：`releases/candidates/V0.7.1-NS-UI-R52/GameFold-PC-V0.7.1-NS-UI-R52-10116.apk`。
- R52 SHA-256：`f1bb60349a4fbb4c2bab536cbaae4881220eb5c55bf40673fcd135a62d6afa35`。
- R52 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R52`，`versionCode=10116`，debug 签名；`packageLegacyRelease` 构建成功，`aapt dump badging` 与哈希匹配。R52 尚未发起真机安装，避免重复触发 vivo 权限拦截。
- R52 定向回归：`:app:compileLegacyDebugKotlin` 与目标单元测试全通过；新增设置返回入口未选中态透明/无 Material 卡片契约。
- 最新候选 R53（开关控件与 NS 源码同源收口：56×32dp 方形轨道、24dp 方形滑块、NS moss/steel 色板与禁用态；避免沿用按钮 action 红）：`releases/candidates/V0.7.1-NS-UI-R53/GameFold-PC-V0.7.1-NS-UI-R53-10117.apk`。
- R53 SHA-256：`a77b10cb47f3b4999e948e38aa3161ed375850824eb97cb0a19312937cd81cfe`。
- R53 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R53`，`versionCode=10117`，debug 签名；`packageLegacyRelease` 构建成功，`aapt dump badging` 与哈希匹配。R53 尚未发起真机安装，避免重复触发 vivo 权限拦截。
- R53 定向回归：`:app:testLegacyDebugUnitTest --tests app.gamenative.gamefold.ui.GameFoldPcFrontendPolicyTest` 成功，13 项全通过；新增契约覆盖 NS 开关色板、轨道/滑块几何和禁用态。随后完整执行 `:app:testLegacyDebugUnitTest`，结果为 `988 tests completed, 71 failed`，失败仍为既有资源/清单类测试，未出现 UI 定向测试失败。
- 最新候选 R54（加载卡片按 NS 参考截图收口为约 66% 显示区宽度，同时覆盖 Compose 与原生 Overlay 两条渲染路径；保留 R53 的 NS 方形开关）：`releases/candidates/V0.7.1-NS-UI-R54/GameFold-PC-V0.7.1-NS-UI-R54-10118.apk`。
- R54 SHA-256：`db7fba72df87ea044bf3ac18b3d8db57ff8a78961890c1c1197d57325d5d621a`。
- R54 APK 元数据：包名 `com.gamefold.pc`，`versionName=0.7.1-NS-UI-R54`，`versionCode=10118`，debug 签名；`packageLegacyRelease` 构建成功，`aapt dump badging` 与哈希匹配。R54 尚未发起真机安装，避免重复触发 vivo 权限拦截。
- R54 定向回归：`:app:testLegacyDebugUnitTest --tests app.gamenative.gamefold.ui.GameFoldPcFrontendPolicyTest` 成功，14 项全通过；新增契约覆盖 Compose/native 加载卡片的 NS 66% 显示区宽度。随后完整执行 `:app:testLegacyDebugUnitTest`，结果为 `989 tests completed, 71 failed`，失败仍为既有资源/清单类测试。
- R54 复核：该包构建时未带 `GAMEFOLD_PC_LOCAL_SHELL=true`，因此只能作为普通壳候选记录，不作为 PC Local Shell UI 验收依据。
- R55（已废弃）：`releases/candidates/V0.7.1-NS-UI-R55/GameFold-PC-V0.7.1-NS-UI-R55-10119.apk`。启动即因 Compose `painterResource` 读取 Android layer-list drawable 而崩溃，不能作为基线。
- R56 修正版：`releases/candidates/V0.7.1-NS-UI-R56/GameFold-PC-V0.7.1-NS-UI-R56-10120.apk`。将 NS layer-list 按钮底图改为原生 `ImageView` 承载，修复 R55 启动崩溃；该修正版现以正式版本元数据重建为 0.7.2 基线 `V0.7.2-NS-UI-R56-10121`。
- 0.7.2 正式基线：`releases/baselines/V0.7.2/GameFold-PC-V0.7.2-NS-UI-R56-10121.apk`，SHA-256：`00a4a5045a257b332c46a1550e418825ede37ebb88bf3db7d2b8d3ec456c06b0`；`aapt dump badging`、BuildConfig、APK Signature Scheme v2 均已校验。
- 0.7.2 定向回归：`GameFoldPcFrontendPolicyTest` 共 14 项，`failures=0`、`errors=0`；设备手动安装后包元数据为 `versionName=0.7.2`、`versionCode=10121`，拉回 APK SHA-256 与构建产物一致。
- 真机静态证据：R31 已截取首页（方向触控后绿色选中框移动到第二张卡）、快速上手、设置首页、关于页，截图暂存于 `/private/tmp/gamefold-pc-v071-ui-r31-touch-right.png`、`/private/tmp/gamefold-pc-v071-ui-r31-quickstart.png`、`/private/tmp/gamefold-pc-v071-ui-r31-settings.png`、`/private/tmp/gamefold-pc-v071-ui-r31-about.png`。R34 冷启动首页截图为 `/private/tmp/gamefold-pc-v071-ui-r34-home.png`；R33 横屏快速上手截图为 `/private/tmp/gamefold-pc-v071-ui-r33-quick.png`；R35 最终候选首页和快速上手截图为 `/private/tmp/gamefold-pc-v071-ui-r35-home.png`、`/private/tmp/gamefold-pc-v071-ui-r35-quick.png`；已安装的 R36 首页、快速上手、加载和 Back 截图为 `/private/tmp/gamefold-pc-v071-ui-r36-home.png`、`/private/tmp/gamefold-pc-v071-ui-r36-quick.png`、`/private/tmp/gamefold-pc-v071-ui-r36-cyber-loading.png`、`/private/tmp/gamefold-pc-v071-ui-r36-pause.png`。UI 树确认快速上手完整文案、横竖屏入口、设置四项入口和真实构建版本均可读；R37 新增的显式手柄确认分发尚未在真机安装包中验证。
- 横竖屏真机证据：R32 点击快速上手横竖屏按钮后，截图从竖向 `2200×2480` 切换为横向 `2480×2200`，截图 `/private/tmp/gamefold-pc-v071-ui-r32-after-orientation.png`；R36 保留该修复并将 Activity 解包方式用于双屏入口，同时快速上手新增横竖屏/双屏/设置三项显式手柄选择轨道。双屏在支持后屏上的完整开关结果仍待补充。
- 真机运行态探测：R31 在真实 Cyberpunk 2077 进程运行中触发 Back，显示“退出模拟 / 要退出当前游戏吗？”方形弹窗；`wineserver` 与 `Cyberpunk2077.exe` 在弹窗期间仍存活并处于 stopped 状态。点击“继续”后同一进程恢复运行。证据截图：`/private/tmp/gamefold-pc-v071-ui-r31-pause.png`。
- 真机 Home/会话探测：R36 已完成一次 Cyberpunk“运行 → Back/继续 → Home → 首页 → 同游戏恢复”循环；本轮 Cyberpunk PID `13808` 在 Home 后仍为 stopped，同游戏再次打开后仍为同一 PID 并恢复运行。随后切换 GTA V 时旧 Cyberpunk 进程消失，GTA V 的 `GTAVLauncher.exe` PID `15156` 与 `GTA5.exe` PID `15175` 均进入运行态，再通过 Back→“退出模拟”清理并回到首页；完整 5 次循环仍待补。
- 四游戏探测：R36 已再次确认 Cyberpunk、GTA V、黑神话像素版 `A:\BlackMythPixel.exe` 可进入真实启动进程；黑神话本轮只确认到启动进程，未完成 Back/Home 的完整清理与恢复证据。当前设备 `/sdcard/GameFoldPC/game` 中没有 The Ramp 目录，无法在本机完成该游戏运行态验收，不能据此标记为通过。
- 全量单元测试备注：仓库中下载器/资源清单类测试依赖未随离线裁剪构建提供的资产，出现既有 `FileNotFoundException`；本轮定向测试未受影响，不能将该批资源前置问题误判为 UI 回归。
- 全量回归：R50 为 `984 tests completed, 71 failed`；R51 当前工作树新增实体 Home 与具体加载阶段契约后为 `986 tests completed, 71 failed`；R52 再加入设置返回入口未选中态契约后为 `987 tests completed, 71 failed`；R53 加入 NS 开关契约后为 `988 tests completed, 71 failed`；R54 加入加载卡片几何契约后为 `989 tests completed, 71 failed`。失败仍集中在既有 `ModMaterializerTest`、`ManifestIdCorrelationTest` 以及缺少资源/清单资产导致的 `SteamUtilsFileSearchTest`、各 Downloader 测试。

### 2.5 每场景截图对照审计

下表把已获取的 PC 截图与 NS 参考截图放在同一条验收链上。截图文件来自同一套 2480×2200 设备画布；R54 尚未安装，因此“最终候选截图”不能用旧候选截图冒充，必须在后续现场安装后补拍。

| 场景 | NS 参考 | 已有 PC 截图 | 对照发现与当前处理 | 截图状态 |
|---|---|---|---|---|
| UI-01 授权 | `/Users/11085429/Desktop/gamefold NS/1授权.jpg` | `/private/tmp/gamefold-pc-r16-permission-final.png`（旧流程，非当前候选授权弹窗） | 旧 PC 为授权后首页，不能证明授权弹窗；当前源码已补浅色功能说明页、NS 方形按钮和真实授权回调，待 R54 现场首启补拍 | △ 待补 |
| UI-02 首页 | `/Users/11085429/Desktop/gamefold NS/2首页.jpg` | `/private/tmp/gamefold-pc-v071-ui-r36-home-clean.png`、`/private/tmp/gamefold-pc-v071-ui-r31-touch-right.png` | R36 已证明首页浅色壳、品牌区、三入口、卡带和绿色选中框；R54 仅在源码继续收口，需补最终候选截图验证顶部三按钮间距与封面底边 | △ 待补 |
| UI-03 快速上手 | `/Users/11085429/Desktop/gamefold NS/3快速上手.jpg` | `/private/tmp/gamefold-pc-v071-ui-r35-quick-clean.png`、`/private/tmp/gamefold-pc-v071-ui-r31-landscape-quick.png` | R35 截图已是全屏页和完整文案；R43 后又调整标题、图标和正文几何，需补 R54 最终截图及横竖屏前后各一张 | △ 待补 |
| UI-04 设置首页 | `/Users/11085429/Desktop/gamefold NS/4设置.jpg` | `/private/tmp/gamefold-pc-v071-ui-r31-settings.png` | R31 旧截图仍显示未选中返回入口的灰色方框，且为 R52 前版本；R52 已改为未选中纯图标、聚焦才显示选择框，需补最终截图 | △ 待补 |
| UI-05 关于 | `/Users/11085429/Desktop/gamefold NS/5关于.jpg` | `/private/tmp/gamefold-pc-v071-ui-r31-about.png` | R31 旧截图的品牌/信息卡几何仍偏大；R41/R42 已按 NS 字标、卡片尺寸和内容列重新收口，需补最终截图验证动态版本文本 | △ 待补 |
| UI-06 游戏单独设置 | `/Users/11085429/Desktop/gamefold NS/6游戏单独设置.jpg` | 目前没有 R54 安装后的专门截图 | 源码已移除黑色 Tab、接入浅色 NS 壳并统一全局/单独设置方形开关；R53 又把开关色板/几何改为 NS 同源，需补开/关/焦点三态截图 | △ 待补 |
| UI-07 游戏加载 | `/Users/11085429/Desktop/gamefold NS/7游戏加载.jpg` | `/private/tmp/gamefold-pc-v071-ui-r31-switch-loading.png` | R31 截图的卡片仍约 74% 显示区宽度，明显比 NS 约 66% 更宽；R54 已同步 Compose/native 两条路径改为 66%，需补真实阶段（安装/解压/启动）截图 | △ 待补 |
| UI-08 暂停 / Back | `/Users/11085429/Desktop/gamefold NS/8游戏暂停.jpg` | `/private/tmp/gamefold-pc-v071-ui-r36-pause-clean.png`、`/private/tmp/gamefold-pc-v071-ui-r31-pause.png` | 旧 R36 弹窗仍是早期宽度/焦点态；R45 后已按 NS 约 60% 对话框宽度和继续/退出分支收口，需补 R54 最终截图 | △ 待补 |
| UI-09 Home 暂停恢复 | 以 NS 首页与暂停参考组合验收 | `/private/tmp/gamefold-pc-v071-ui-r31-home-return.png`、`/private/tmp/gamefold-pc-v071-ui-r31-resume.png` | R31/R36 已有同游戏 PID 保活/恢复和异游戏清理的运行态证据；R51 又补实体 Guide/Home 键路径，最终候选仍需补 5 次循环的首页/恢复截图序列 | △ 待补 |

截图审计结论：旧候选截图中已明确发现的三类视觉偏差（设置返回未选中框、关于页几何、加载卡片过宽）已分别在 R42/R52/R54 修复；快速上手与暂停的后续几何修复已在 R43/R45 进入源码。0.7.2 已由用户手动安装并完成 APK 回读哈希校验，最终场景截图和完整手柄/游戏行为仍保留为现场验收项。

## 3. 总览

| ID | 场景 | 核心修改 | 风险等级 | 验收类型 | 状态 |
|---|---|---|---|---|---|
| UI-01 | 授权访问权限 | NS 方形像素按钮；补齐浅色功能说明背景页 | 中 | 截图 + 权限流程 | 部分通过 |
| UI-02 | 首页 | 标题、右上角按钮、卡带封面、全局手柄焦点系统 | 高 | 截图 + 手柄遍历 | 部分通过 |
| UI-03 | 快速上手 | 整页像素级移植 NS，包含完整文案与横竖屏切换 | 中 | 截图 + 交互 | 部分通过 |
| UI-04 | 设置首页 | NS 白色底板和阴影；调整入口结构 | 中 | 截图 + 手柄遍历 | 部分通过 |
| UI-05 | 关于 | 完整移植 NS 关于页面 | 低 | 截图 + 返回交互 | 部分通过 |
| UI-06 | 游戏单独设置 | 移除黑色 Tab；统一 NS 色彩、底板和方形开关 | 中 | 截图 + 设置持久化 | 部分通过 |
| UI-07 | 游戏加载 | 合并为一种大卡片；动态真实状态；缩小进度条 | 高 | 分阶段截图 + 启动回归 | 部分通过 |
| UI-08 | 游戏暂停 / Back | NS 退出确认弹窗替代 Toast 和二次返回退出 | 高 | 截图 + 分支交互 | 部分通过 |
| UI-09 | Home 暂停与恢复 | 保活回首页；同游戏恢复；异游戏替换旧会话 | 最高 | 真机运行态验证 | 部分通过 |

## 4. 详细修改与验收标准

### UI-01｜授权访问权限

参考截图：

- PC：`/Users/11085429/Desktop/GameFold PC/1授权.jpg`
- NS：`/Users/11085429/Desktop/gamefold NS/1授权.jpg`

修改要求：

- 将“暂不授权”和“确认授权”改为 GameFold NS 的方形像素按钮设计。
- 对齐 NS 的边框、底色、文字、阴影、按钮尺寸、按钮间距和排列位置。
- 背景不能继续使用纯黑色。
- 从 NS 对应源代码移植浅色背景页面，保留其功能说明文字、信息层级、间距和布局；授权弹窗叠加在该页面之上。
- 弹窗外区域、显示区裁切和下方控制器区域保持 GameFold Shell 的统一结构。
- 不改变权限申请的实际目标、拒绝分支和授权后的资源初始化逻辑。

验收标准：

- 首次进入授权场景时，背景是完整浅色说明页，而不是纯黑底或空页面。
- 两个按钮均为 NS 方形像素样式，没有 PC 原有圆角按钮残留。
- “暂不授权”仍走拒绝/稍后处理分支；“确认授权”仍发起正确的系统权限流程。
- 拒绝、返回和重新授权均不会卡死或跳过必要的资源检查。

### UI-02｜首页

参考截图：

- PC：`/Users/11085429/Desktop/GameFold PC/2首页.jpg`
- NS：`/Users/11085429/Desktop/gamefold NS/2首页.jpg`

#### UI-02A｜左上角标题与应用 Icon

- 缩小现有 GameFold 标题与应用 Icon。
- 直接以 NS 的尺寸、相对位置、图标与文字间距、顶部和左侧边距为准。
- 不重新设计品牌图形，不使用近似字形或临时替代资源。

#### UI-02B｜右上角三个按钮

- 三个按钮的尺寸、纵向基线、间距和整体位置与 NS 对齐。
- 最左侧“双屏显示开关”直接显示现有切图。该素材已经包含边框和阴影，外层不得再增加背景、边框、卡片或阴影。
- “快速上手”和“设置”使用 NS 的“底图 + 阴影”组合方式，复用 NS 的真实素材和叠放规则。
- 保留三个按钮各自现有功能，不因视觉替换改变业务语义。

#### UI-02C｜游戏封面与卡带

- 游戏图片与卡带边框必须严密贴合。
- 修复底部白边、图片越界、裁切不足、拉伸或卡带内留白不一致的问题。
- 对齐 NS 的封面槽位尺寸、裁切方式、边缘位置和卡带层级关系。
- 不改变 GameFold PC 的本地封面查找和游戏标题数据来源。

#### UI-02D｜全局按钮选择系统

- 移植 GameFold NS 的手柄 UI 导航体系，不能只在首页局部增加一个装饰框。
- 支持方向键和左摇杆移动选中项，A/X 确认，B/Y 返回或取消；触屏继续可用。
- 首页选中游戏时显示 NS 提供的绿色选中框图片，尺寸和位置与卡带完全对齐。
- 首页顶部三个按钮、游戏卡带、添加游戏等主要操作都必须可通过手柄到达。
- 设置首页、关于、快速上手、游戏单独设置、弹窗及后续可交互页面统一接入相同焦点系统。
- 页面进入、返回、弹窗关闭和列表滚动后必须有明确且合理的默认焦点；焦点不得丢失到不可见控件。

验收标准：

- 同尺寸截图下，左上角品牌区和右上角三个按钮与 NS 的边界、基线和间距一致。
- 双屏切图外没有重复边框或双重阴影。
- 每张游戏封面与卡带边缘对齐，底部无白边。
- 仅使用手柄即可遍历并触发首页所有主要入口；绿色焦点框随选择移动且不抖动、不残留。
- 手柄焦点和触摸操作可以交替使用，不出现一次触摸后手柄失效的问题。

### UI-03｜快速上手

参考截图：

- PC：`/Users/11085429/Desktop/GameFold PC/3快速上手.jpg`
- NS：`/Users/11085429/Desktop/gamefold NS/3快速上手.jpg`

修改要求：

- 废弃 PC 当前信息较少的小弹窗形式。
- 将 GameFold NS 的快速上手页面整体移植到 PC，使用接近全屏的页面呈现。
- 文案、标题、图标、按钮、横竖屏切换按钮、尺寸、间距、位置、背景、边框和阴影均以 NS 为准。
- 不删减 NS 页面已有提示内容，也不额外改写文案。
- 横竖屏切换必须是实际可用的交互，不是静态图标。
- 页面必须接入 UI-02D 的全局手柄焦点系统，并保留触摸操作。

验收标准：

- 快速上手以 NS 的完整页面形式打开，不再是旧小弹窗。
- 页面内容无缺项、截断、重叠或超出显示区。
- 横竖屏切换后布局和按钮状态正确，返回后应用方向状态符合 NS 逻辑。
- 手柄可以进入切换按钮、执行切换并正常退出页面。

### UI-04｜设置首页

参考截图：

- PC：`/Users/11085429/Desktop/GameFold PC/4设置.jpg`
- NS：`/Users/11085429/Desktop/gamefold NS/4设置.jpg`

修改要求：

- 将 PC 当前“大尺寸纯边框设置项”改成 NS 的白色底板与像素阴影设计。
- 对齐设置项的尺寸、行高、图标、文字、间距、边框、阴影和列表节奏。
- 第四个入口由“GPU 驱动管理器”改为“关于”。
- “关于”的图标、文字布局和入口样式直接复用 NS。
- 原第四项错误打开模拟设置的路由必须移除，点击“关于”只进入 UI-05 关于页面。
- 设置列表完整接入 UI-02D 的全局手柄焦点系统。

验收标准：

- 所有设置项均呈现 NS 白色底板和阴影，不保留 PC 原有大边框卡片外观。
- 第四项名称与图标均为“关于”，点击后路由正确。
- 手柄能依次遍历全部入口，焦点样式与首页统一，返回后恢复到原设置项。

### UI-05｜关于

参考截图：

- PC：`/Users/11085429/Desktop/GameFold PC/5关于.jpg`
- NS：`/Users/11085429/Desktop/gamefold NS/5关于.jpg`

修改要求：

- 将 GameFold NS 的关于页面完整移植到 GameFold PC。
- 页面结构、品牌信息、图标、文案、版本信息区域、卡片、边框、阴影、间距和返回方式均以 NS 为准。
- 产品名称和版本值必须读取 GameFold PC 的真实构建信息；不得硬编码 NS 的产品版本。
- 页面接入全局手柄焦点与返回体系。

验收标准：

- 页面视觉结构与 NS 对照一致，没有旧 PC 关于页或设置页样式混入。
- 显示的应用名称、`versionName`、`versionCode` 等动态信息与实际安装包一致。
- 触摸和手柄都能正常返回设置首页，且焦点恢复到“关于”。

### UI-06｜游戏单独设置与开关样式

参考截图：

- PC：`/Users/11085429/Desktop/GameFold PC/6游戏单独设置.jpg`
- NS：`/Users/11085429/Desktop/gamefold NS/6游戏单独设置.jpg`

修改要求：

- 移除 PC 游戏单独设置顶部的黑色 Tab 栏。
- 以 NS 设置页面的色彩体系、浅色背景、白色底板、像素边框和阴影重新组织视觉层级。
- 保留 PC 游戏单独设置中仍然有效的选项和真实容器配置语义，不因视觉移植变更配置值或路径。
- PC 的游戏单独设置和全局设置中，圆角、绿色 + 黑色的现代开关统一替换为 NS 方块式开关。
- 对齐开关的开/关图形、颜色、尺寸、文本间距、禁用态和焦点态。
- Tab、选项、开关、下拉项和返回入口均接入全局手柄焦点系统。

验收标准：

- 页面不再出现黑底 Tab 栏，整体视觉属于同一套 NS 像素语言。
- 全局设置和单独设置中不再混用旧圆角开关。
- 仅用手柄可切换 Tab、遍历选项、修改开关和返回。
- 修改值后退出并重新进入，配置仍正确保存；不影响其他游戏的独立配置。

### UI-07｜游戏加载

参考截图：

- PC 大卡片：`/Users/11085429/Desktop/GameFold PC/7游戏加载1.jpg`
- PC 小卡片：`/Users/11085429/Desktop/GameFold PC/7游戏加载2.jpg`
- NS：`/Users/11085429/Desktop/gamefold NS/7游戏加载.jpg`

修改要求：

- 将 PC 当前“大卡片”和“小卡片”两种加载表现合并成唯一的大卡片样式。
- 新样式整体像素级复刻 NS，包括封面、标题、说明文字、卡片、边框、阴影、层级和位置。
- 下方进度条相对当前大卡片稍微缩小，并以 NS 的比例和间距为准。
- 中间状态文案不能只显示笼统的“加载中”。
- 状态文案必须来自真实启动阶段，例如“正在准备运行环境”“正在安装 Bionic”“正在准备 Proton”“正在创建容器”“正在启动游戏”等；只有代码确实处于对应阶段时才能显示相应文案。
- 进度值和状态文案必须来自同一个启动状态源，避免文案跳变、倒退或进度已经完成但仍显示安装中。
- 加载过程中始终只出现一个主卡片；不得叠加旧 Loading Dialog、小卡片或全屏黑色加载层。
- 保留现有按点击时机准备 PC 运行资源的流程，不把资源准备提前到应用启动阶段。

验收标准：

- 所有游戏、所有启动阶段统一显示同一种大卡片。
- 加载卡片与 NS 的几何、素材层级和色彩一致，进度条不再显得过长或过厚。
- 录屏或分阶段截图能证明状态文字与日志中的真实阶段一致，全流程不再出现单独的“加载中”。
- 首次准备资源、已有资源再次启动、启动失败和重试时均不会叠加两套加载 UI。
- 四款基线游戏仍能进入运行画面。

### UI-08｜游戏暂停与 Back 退出确认

参考截图：

- PC：`/Users/11085429/Desktop/GameFold PC/8游戏暂停.jpg`
- NS：`/Users/11085429/Desktop/gamefold NS/8游戏暂停.jpg`

修改要求：

- 删除 PC 当前“第一次 Back 显示 Toast、第二次 Back 直接退出”的交互。
- 游戏运行中第一次触发 Back 时暂停游戏，并显示 NS 像素级退出确认窗口。
- 弹窗标题：`退出模拟`。
- 弹窗正文：`要退出当前游戏吗？`。
- 左侧按钮：`继续`；右侧按钮：`退出模拟`。
- 弹窗的卡片、边框、阴影、按钮、字体、尺寸和位置均直接对齐 NS。
- 弹窗显示期间暂停运行环境并停止向游戏传递输入；不得让弹窗操作同时触发游戏按键。
- 点击“继续”：关闭弹窗并恢复原游戏、声音和输入。
- 点击“退出模拟”：杀掉游戏进程，清理运行环境和当前会话，返回首页。
- 使用 Back/B 取消弹窗时等同于“继续”，不能退出游戏。
- 防止重复 Back 创建多个弹窗或重复执行退出。

验收标准：

- 第一次 Back 直接出现 NS 弹窗，整个流程不再出现旧 Toast。
- 弹窗期间画面、声音和游戏输入均暂停；弹窗自身可使用触摸和手柄操作。
- “继续”恢复到弹窗出现前的游戏状态；“退出模拟”完全结束进程并回首页。
- 连续快速按 Back、B 或按钮不会产生双弹窗、重复退出、黑屏或输入穿透。

### UI-09｜GameFold Home 键暂停、回首页与恢复会话

参考截图：

- PC Home 键：`/Users/11085429/Desktop/GameFold PC/9 home.jpg`
- NS 行为参考：GameFold NS `GameFoldInputBridge`、`EmulationActivity`、暂停画面及首页恢复流程。

修改要求：

- 游戏运行中点击 GameFold Logo/Home 键时，不得继续复用 Back 逻辑，也不得显示退出确认弹窗。
- Home 键执行“暂停当前游戏并立即返回首页”。
- 返回首页时保留当前游戏进程、容器、运行环境和游戏状态，但停止画面推进、声音以及向游戏传递的控制输入。
- GameFold PC 同一时间只允许存在一个受管理的有效游戏会话。

同游戏恢复：

- 首页再次打开与暂停会话相同的游戏时，复用已有进程和运行环境。
- 不重新安装运行资源、不新建容器、不重新启动游戏、不重走 UI-07 加载流程。
- 重新挂载游戏画面、声音和输入，并从暂停位置继续。
- 恢复交互和暂停画面以 NS 为准，包括 NS 的暂停帧和中央红色继续按钮/手柄确认逻辑；最终行为需通过 NS 真机对照确定。

异游戏切换：

- 首页打开不同游戏时，先完整结束当前暂停会话。
- 等待旧游戏进程、XServer/环境、输入桥和会话注册清理完成后，再启动新游戏。
- 新游戏按照正常 UI-07 加载流程启动；不能让两个游戏共享旧容器状态或并行占用运行环境。

异常恢复：

- 如果暂停期间进程已经崩溃、被系统回收或会话记录失效，应清除陈旧状态并正常冷启动所选游戏。
- 多次执行“游戏 → Home → 首页 → 同游戏恢复”时，不得出现黑屏、花屏、无声、输入失效、重复进程或回到错误游戏。

验收标准：

- Home 与 Back 是两条独立路径：Home 暂停回首页，Back 打开退出确认。
- 回到首页后，ADB/进程证据能证明原游戏进程仍存活，但游戏画面、声音和输入处于暂停状态。
- 重新选择同一游戏可以从原来的具体画面继续，不重新显示加载卡片，不创建第二个游戏进程。
- 选择另一游戏会先结束旧进程，再启动新游戏；旧游戏不残留音频、窗口或输入。
- 至少连续完成 5 次 Home 暂停/恢复，以及同游戏和异游戏各一次切换验证。

### 4.10｜本轮代码落地摘要（供验收逐项对照）

- 公共像素控件：新增方形按钮、矩形开关、焦点边框和 NS 颜色 Token；全局设置组已切换到该开关。
- 授权页：替换为 GameFold Shell 内的浅色说明背景、方形按钮和真实权限回调。
- 首页：缩小品牌区；双屏切图不再套二次边框；快速上手/设置按钮采用底图+阴影；卡带封面槽位重新对齐并保留绿色焦点素材；冷启动焦点增加一次延迟重试；R31 增加首页控制器显式选中索引，方向触控可移动绿色框，A/X 可确认启动或添加游戏。
- 快速上手：替换为全屏 NS 文案页面，接入真实横竖屏切换；B/Y/返回可关闭。
- 设置：设置首页采用白底+像素阴影卡片，第四项改为“关于”；新增读取构建版本的关于页。
- 开关：全局设置和带 action 的设置行在 PC Local Shell 使用矩形方块开关；非 PC flavor 保留原 Material 控件。
- 加载：统一卡片阴影/比例，进度段高度缩小；启动准备、运行依赖、Mono、DRM、启动游戏等阶段通过事件映射为明确中文状态；通用空状态不再显示单独“加载中”。
- Back：PC Local Shell 第一次 Back 直接暂停环境并显示 NS 风格“退出模拟”弹窗；继续/退出、B/Y 取消、方向键选中均已接通。
- Home：控制器 Home 独立于 Back；暂停 XServer/运行环境并回首页；同一 `appId` 复用环境恢复，不同 `appId` 先清理旧环境再冷启动。

上述内容代表代码实现边界。R31 已补齐 UI-08 的真实 Cyberpunk Back/继续证据和 UI-09 的一轮 Home/同游戏恢复/异游戏清理证据，R51 又补齐实体 Guide/Home 键的本地 Shell 回首页分支；但这不等于完整四游戏、多轮循环和用户最终确认已完成。

## 5. 共用视觉验收规则

- 必须在同一设备、同一显示方向、同一页面状态下分别截取 PC 与 NS 对照图。
- 全屏 Shell、上方显示区和具体组件分别比较，避免游戏封面或动态画面干扰判断。
- 在统一到 2480 × 2200 的参考尺寸后：Shell 主要边界目标误差不超过 4 px；卡片、按钮、弹窗和间距目标误差不超过 6 px。
- 稳定色块的 RGB 单通道目标差值不超过 8；游戏封面、动态游戏画面和抗锯齿边缘除外。
- 优先直接复用 NS 真实位图、Nine-patch、Drawable、字体渲染、尺寸和色彩 Token，不使用肉眼近似的临时素材。
- 不得出现现代圆角控件、额外描边、双重阴影、纯黑占位层或超出上方显示区的弹窗。
- 每个主操作必须同时验证触摸和手柄；焦点可见、顺序合理、返回后可恢复。

## 6. 建议实施批次

| 批次 | 内容 | 依赖关系 | 完成门槛 |
|---|---|---|---|
| A｜设计基础 | UI-02D 全局焦点系统、NS 共用 Token/底板/按钮/开关/阴影组件 | 后续所有页面 | 共用组件和焦点自动化测试通过 |
| B｜静态页面 | UI-01、UI-02A/B/C、UI-03、UI-04、UI-05、UI-06 | 依赖批次 A | 全部参考场景截图通过第一轮对比 |
| C｜启动状态 | UI-07 动态加载卡片 | 依赖共用组件和真实启动状态源 | 多阶段文案、进度、四游戏启动回归通过 |
| D｜暂停退出 | UI-08 Back 暂停与退出确认 | 依赖暂停 API、输入隔离和像素弹窗 | 继续/退出/取消/快速重复操作通过 |
| E｜会话生命周期 | UI-09 Home、同游戏恢复、异游戏替换 | 依赖 UI-08 的暂停能力和会话状态机 | 真机多轮保活、恢复、切换测试通过 |
| F｜总回归 | 01—09 完整截图、手柄、方向切换、权限、四游戏运行 | 依赖 A—E | 用户确认后才可候选晋级 |

## 7. 候选版本总体验收表

| 检查项 | 结果 | 证据/备注 |
|---|---|---|
| 新候选版本未覆盖 0.7.1 基线 | ☑ | 0.7.2 使用独立目录、APK 文件、版本号、哈希和 Git 标签；0.7.1 基线未改写 |
| 包名、versionName、versionCode、签名、APK SHA-256 已记录 | ☑ | 0.7.2：`com.gamefold.pc` / `0.7.2` / `10121` / APK v2 debug 签名 / `00a4...c06b0`；设备拉回哈希一致 |
| UI-01 授权页通过 | △ | R56/0.7.2 保留 NS 对照的浅色功能背景、`/sdcard/gamefold` 说明、方形按钮及 0.86 显示区比例契约；授权页方向键/左摇杆已接入 Compose 焦点，待首次授权截图 |
| UI-02 首页与全局手柄焦点通过 | △ | 首页截图与卡带几何定向测试完成；R56 保留顶部三入口与游戏网格统一选择轨道，并把实体手柄 KeyEvent、左摇杆、重复节奏和 Guide/Home 语义接入同一桥；0.7.2 已安装，物理手柄完整遍历待验证 |
| UI-03 快速上手通过 | △ | R36 设备截图与方向切换已完成；R56 禁用快速上手自身入口的焦点，保留方向/双屏/设置三项轨道；0.7.2 已安装，最终截图待补 |
| UI-04 设置首页通过 | △ | R36 设备截图已完成；R52—R56 保留设置详情 A/X 确认、方向键焦点管理，并将未选中返回入口收口为 NS 图标态、仅焦点显示选择框；0.7.2 已安装，最终截图待补 |
| UI-05 关于通过 | △ | R43—R56 已收口 About 顶栏、内容上移、16dp 列间距、0.9 品牌缩放、开发者字标、副标题字标和 68/68/56dp 信息卡；仍待 0.7.2 真机截图与 NS 参考图逐像素对照 |
| UI-06 游戏单独设置与方形开关通过 | △ | R49—R56 已补详情页 B/Y 返回、A/X/方向键焦点路由、L/R/ZL/ZR 肩键 Tab 事件以及 NS 同源方形开关色板/几何；全局/单独设置持久化与完整选项仍待真机验证 |
| UI-07 单一加载卡片与真实状态文案通过 | △ | R44/R45 已将本地 shell 预启动的消息/进度镜像到首页大卡片；R50 再将 ViewModel、PluviaMain、XServer 的状态统一归一化，并补 `Loading...`/依赖安装/解压/启动契约测试；R56/0.7.2 保留统一大卡片及真实启动进程路径，完整四游戏分阶段文案与 The Ramp 证据待验证 |
| UI-08 Back 暂停/继续/退出通过 | △ | R31 已在 Cyberpunk/GTA V 真实进程完成 Back 弹窗、stopped 进程、继续恢复与 GTA V“退出模拟”清理；R45 将弹窗卡片收窄至 NS 参考约 60% 显示区宽度并补几何契约测试；0.7.2 已安装，快速重复操作仍待补 |
| UI-09 Home 保活、同游戏恢复、异游戏切换通过 | △ | R36 已完成一次 Home→首页→同游戏 PID 复用，以及异游戏前旧进程清理；R45—R56 新增纯 `GameFoldSessionPolicy`、陈旧 activeAppId/pausedAtHome 保护、有效 XEnvironment/XServer/WinHandler 保护、路由销毁保留暂停状态、View attach 后 resume/reassert guard，以及实体 Guide/Home 强制回首页；仍需 0.7.2 真机 5 次循环和新游戏最终运行证据 |
| 横屏、竖屏与双屏显示状态通过 | △ | R32 已真机验证快速上手横竖屏切换尺寸变化；双屏入口仍需在支持后屏实际开关一次 |
| 触摸和手柄完整遍历通过 | △ | 触摸静态页面已核验；手柄遍历待补 |
| Cyberpunk 2077 启动与运行回归通过 | △ | R31 已真实进入 Cyberpunk 2077 画面并完成 Back/继续/Home/同游戏恢复探测 |
| GTA5 启动与运行回归通过 | △ | R31 已真实进入 `GTAVLauncher.exe` 与 `GTA5.exe`，截图 `/private/tmp/gamefold-pc-v071-ui-r31-gta-after20.png` |
| The Ramp 启动与运行回归通过 | ☐ | 当前设备 `/sdcard/GameFoldPC/game` 无 The Ramp 目录，待补齐游戏资源后再测 |
| 黑神话像素版启动与运行回归通过 | △ | R36 已见 `A:\BlackMythPixel.exe` 启动进程；Back/Home 完整行为与最终运行画面仍待补 |
| 未修改 GameFold FC/NES | ☑ | 当前工作树变更路径未包含 FC/NES/FCEU/ROM 模块；本轮修改仅落在 GameFold PC UI、运行会话和对应测试/资源。 |
| 用户完成真机视觉与行为验收 | ☐ | |

安装记录：自动安装 R56/0.7.2 时曾被 vivo 安全确认流程挂起；用户随后手动完成安装。设备 `10AF3P0SDY001M6` 当前识别 `versionName=0.7.2`、`versionCode=10121`，拉回 APK SHA-256 与构建产物 `00a4a504...c06b0` 一致。旧候选 R37—R54 的安装阻塞记录保留用于历史追溯，不影响 0.7.2 基线。

## 8. 状态定义

- `待实施`：需求已确认，尚未修改代码。
- `开发中`：正在修改，不能作为候选版本验收。
- `待验证`：已构建独立候选并完成基础自动化检查，等待截图或真机验证。
- `部分通过`：只有自动化、截图或部分交互通过，仍缺少必要证据。
- 表格中的 `△`：代码/静态截图/定向测试已完成，但仍缺用户最终视觉确认或完整运行态证据。
- `通过`：自动化、截图、触摸、手柄、运行态和用户真机确认全部完成。
- `阻塞`：存在明确阻塞原因，并在备注中记录复现条件和所需输入。

任何单项都不能因为“代码已提交”或“APK 能安装”直接标记为通过。
