# GameFold PC 0.8.0 × NS 2.9 R2 静态对齐闸门

## 结论

GameFold PC `0.8.0 / 10122` 已通过装机前代码闸门，可进入真机安装与同场景截图验收。
本结论只覆盖源码、资源、编译、单元测试和 APK 身份，不把未执行的真机视觉、后屏触控或游戏运行行为标记为通过。

## 冻结输入

- PC 起点：`gamefold-pc-v0.7.2-baseline`，APK
  `releases/baselines/V0.7.2/GameFold-PC-V0.7.2-NS-UI-R56-10121.apk`，SHA-256
  `00a4a5045a257b332c46a1550e418825ede37ebb88bf3db7d2b8d3ec456c06b0`。
- NS UI 真值：`GameFold-2.9-Baseline-R2.apk`，包名
  `com.switch.fold.baseline.lite`，`2.9 / 33466084`，SHA-256
  `156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292`。
- 当前 NS Android 工作树只作为实现迁移参考；其 Git HEAD 早于 2.9 且工作树未冻结。出现冲突时，以冻结 APK 和已接受设备行为为准。

## 静态对齐范围

| 场景 | 代码级处理 | 静态状态 |
| --- | --- | --- |
| 外壳与显示孔 | PC/NS `GameFoldShellLayout` 归一化包名和 `R` 导入后无源码差异；PC 未修改已对齐外壳 | PASS |
| 首页 | NS 48dp 页头、真实顶部动作素材、四列卡带；封面恢复 72% 正方形槽位及 `13/112` 顶部偏移；标题恢复 NS 等宽 12sp/40dp | PASS |
| 快速上手 | 完整 NS 文案、24/12dp 页面内边距、18sp 标题、40dp 方向按钮、13sp/19sp 正文；双屏入口按设备与横屏能力显示 | PASS |
| 设置首页与详情 | 首页使用 `card_home_option` 的 68dp 白底/像素阴影；详情使用 44dp 工具栏、48dp 深色节标题和统一 64dp 设置行 | PASS |
| 关于 | 44dp 工具栏、双列布局、品牌缩放与位移、68/68/56dp 信息行、28/32dp 图标、15/12sp 字级 | PASS |
| 游戏属性与逐游戏设置 | 144dp 游戏摘要、112dp 正方形封面、NS 设置行、右下角动作按钮；容器编辑器改为浅色 44dp 顶栏和矩形标签栏 | PASS |
| 通用控件 | 统一 NS 色板、5dp+2dp 无占位焦点叠层、drawable 等价按钮/面板、56×32dp 开关、矩形下拉/搜索/文本输入 | PASS |
| 权限、空态、错误和弹窗 | 权限功能背景、方形操作、NS 对话框面板；空态和启动错误使用统一浅色像素表面 | PASS |
| 加载 | Compose 与 XServer View 共用 380×144dp、112dp 封面、220dp 文本列、192×16dp 轨道；六段为 24×8dp、6dp 间隔、蓝/红交替 | PASS |
| 暂停与 Home | 暂停前用 `PixelCopy` 捕获 XServer 帧，叠加 72dp 恢复按钮；Home 保留会话并回首页，B/Y 分层返回 | PASS |
| FPS | PC 本地壳 FPS 与 NS `PixelNumberView` 同为 3×5 字形、4dp 像素、1 像素右下阴影及 6 像素行高 | PASS |

## 文件级对照证据

- `GameFoldShellLayout.kt`：归一化包名及 `R` 导入后 `diff` 无输出。
- `pixel_dimens.xml`、`pixel_styles.xml`、`pixel_attrs.xml`、`pixel_peripheral_styles.xml`：逐字节一致。
- 关键 `pixel_icon_*` 与 `pixel_state_*`：逐字节一致。
- `gamefold_add_game_cartridge_ns.webp`、`gamefold_cartridge_focus_frame_ns.webp`、
  `gamefold_dual_screen_off_ns.webp`、`gamefold_dual_screen_on_ns.webp`：与 NS 迁移源对应原文件逐字节一致。
- 冻结 NS 2.9 R2 APK 和 PC 0.7.2 基线 APK 的 SHA-256 在本轮复核后保持不变。

## 自动化闸门

- `:app:testLegacyReleaseUnitTest`：`255 tests / 0 failures / 0 errors / 0 skipped`。
- `:app:assembleLegacyRelease`：成功，包含 Release Kotlin、R8、资源收缩和 `lintVital`。
- `git diff --check`：通过。

## 0.8.0 候选身份

- APK：`releases/candidates/V0.8.0-NS-FRONTEND-ALIGN/GameFold-PC-V0.8.0-NS-FRONTEND-ALIGN-10122.apk`
- 包名：`com.gamefold.pc`
- `versionName / versionCode`：`0.8.0 / 10122`
- `BuildConfig.GAMEFOLD_PC_LOCAL_SHELL`：`true`
- `BuildConfig.GAMEFOLD_PC_LOCAL_WINE_FAST_PATH`：`false`
- 文件大小：`16,119,103` bytes
- SHA-256：`52bf0311e5c1176ba8bcf1337776cc6dcf91d25ed8959d7b70a1e026175c09f0`
- 签名：APK Signature Scheme v2；证书 SHA-256
  `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`。

## 真机闸门

状态：`HOLD_FOR_DEVICE`。

本轮尚未调用 ADB、尚未安装 APK。安装获准后必须保留现有数据和权限，核验设备端包名、版本、
安装时间与回读 APK SHA-256，并按授权、首页、快速上手、设置、关于、逐游戏设置、加载、暂停、
Home 恢复、横竖屏和 vivo 后屏场景补齐真机证据。
