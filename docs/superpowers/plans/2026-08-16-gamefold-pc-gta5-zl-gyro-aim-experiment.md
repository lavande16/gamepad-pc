# GameFold PC GTA5 ZL 陀螺仪瞄准实验实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在不修改 GTA5、Wine/Proton 或冻结基线的前提下，把 Android 手机陀螺仪转换为虚拟 Xbox 手柄右摇杆输入；仅在按住 ZL 时参与 GTA5 瞄准，松开 ZL 后立即恢复为原始右摇杆输入。

**Architecture:** 复用 GameFold PC 已有的 `GameFoldControllerListener -> GamepadState -> WinHandler -> SDL/XInput` 输入链路。新增纯 Kotlin 陀螺仪处理器、Android 传感器适配器和可测试的运行时手柄桥；传感器只生成独立的右摇杆增量，最终与触摸右摇杆相加并限制到 `[-1, 1]`。

**Tech Stack:** Kotlin、Android `SensorManager`/`TYPE_GYROSCOPE`、GameFold 语义控制器、Winlator `GamepadState`/`WinHandler`、JUnit 4、Gradle `legacyRelease`。

## Global Constraints

- 实施时必须从当时最新且已经用户验收的 GameFold PC 正式基线创建新候选；`0.8.3 / 10139` 只作为本计划的输入链路证据，不是强制实施起点。
- 不得改写、移动或重新签名任何冻结基线 APK、`README.md`、`BASELINE.json` 或 Git tag。
- 第一阶段只对 `GTAVLauncher.exe` 和 `GTA5.exe` 生效，不增加设置页、每游戏开关或通用陀螺仪框架。
- 第一阶段只使用虚拟手柄右摇杆，不发送鼠标移动，不修改 GTA5 文件，不修改 Wine/Proton/Box64/evshim 协议。
- 未按 ZL 时，陀螺仪对 `thumbRX`、`thumbRY` 的贡献必须严格为零。
- ZL 松开、Activity 暂停、GameFold Home、游戏退出、AndroidView 释放和传感器异常都必须清零陀螺仪贡献。
- 必须保留原触摸右摇杆；最终输出为 `manualRightStick + gyroContribution`，逐轴限制到 `[-1, 1]`。
- 不申请新的 Android 运行时权限。没有陀螺仪的设备静默退化为当前控制器行为。
- 传感器坐标必须根据 `Display.getRotation()` 映射，不能假定设备自然方向永远是竖屏。
- 源码验证、构建验证和 GTA5 真机验证分别记录；构建成功不能替代真机瞄准验收。

---

## 1. 0.8.3 已确认的技术证据

本计划在 `gamefold-pc-v0.8.3-baseline` 上确认过：

1. `XServerScreen.kt` 的本地 GameFold shell 使用独立 `GamepadState`。
2. `GameFoldButton.ZL` 当前写入 `state.triggerL = 1f/0f`。
3. `GameFoldStick.RIGHT` 当前写入 `state.thumbRX/thumbRY`。
4. 前屏控制器和 vivo 后屏 ZL/ZR 通过同一个 `RearTouchInputRouter`/`GameFoldControllerListener` 汇合。
5. `WinHandler.sendVirtualGamepadState()` 已把右摇杆和扳机写入 SDL/XInput 共享手柄缓冲。
6. GTA5 已有 `GTAVLauncher.exe`/`GTA5.exe` 识别逻辑，可抽出为共享身份策略。

这些事实只证明 Android 层实现链路成立，不证明灵敏度、方向、延迟或设备手感合格。

## 2. 第一阶段固定实验参数

| 参数 | 首包值 | 目的 |
|---|---:|---|
| 传感器 | `Sensor.TYPE_GYROSCOPE` | 使用经过厂商偏置补偿的角速度 |
| 采样周期 | `10_000 µs` | 目标 100 Hz |
| 最大批处理延迟 | `0 µs` | 尽快投递事件 |
| 水平增益 | `0.45 stick/(rad/s)` | GTA5 水平瞄准起始灵敏度 |
| 垂直增益 | `0.35 stick/(rad/s)` | 垂直略低以减少过冲 |
| 角速度死区 | `0.025 rad/s` | 抑制静止噪声 |
| 低通截止频率 | `12 Hz` | 抑制手抖并控制额外延迟 |
| 偏置学习系数 | `0.02` | 未瞄准且近静止时学习零偏 |
| 偏置学习阈值 | `0.15 rad/s` | 避免把主动旋转学成零点 |
| 最大陀螺仪贡献 | `0.85` | 保留触摸右摇杆叠加空间 |
| 发布变化阈值 | `0.001` | 避免重复写入完全相同状态 |

处理公式：

```text
corrected = screenMappedAngularVelocity - learnedBias
deadzoneAdjusted = sign(corrected) * max(abs(corrected) - 0.025, 0)
filtered = onePoleLowPass(deadzoneAdjusted, cutoff=12Hz, dt)
gyroX = clamp(-filteredYaw * 0.45, -0.85, 0.85)
gyroY = clamp(-filteredPitch * 0.35, -0.85, 0.85)
finalRightX = clamp(manualRightX + gyroX, -1, 1)
finalRightY = clamp(manualRightY + gyroY, -1, 1)
```

首包方向定义：手机向右旋转时准星向右；手机上沿向远离用户方向倾斜时准星向上。若真机方向相反，只修改对应轴符号，不同时改变滤波和增益。

## 3. 成功标准

### 静态与单元测试

- GTA5 两个入口文件名均启用实验，其他 EXE 均不启用。
- ZL 未按下时，任意传感器样本输出均为 `(0, 0)`。
- ZL 松开同步返回零向量，不等待下一次传感器事件。
- 四个 Android 显示旋转方向都有确定的轴映射测试。
- 右摇杆叠加不越过 `[-1, 1]`；清零陀螺仪后保留触摸右摇杆原值。
- `testLegacyReleaseUnitTest` 全量通过，`assembleLegacyRelease` 成功。

### 真机功能验收

- vivo 与 OPPO 各执行一次 GTA5 徒步持枪瞄准测试。
- 未按 ZL，连续移动/旋转手机 60 秒，镜头无可见陀螺仪响应。
- 按住 ZL 后，左右转动手机控制水平准星，上下倾斜控制垂直准星。
- 松开 ZL 后 1 个渲染帧内停止陀螺仪影响，不发生持续漂移。
- 同时推动右摇杆和转动手机时，两种输入可叠加；松开 ZL 后右摇杆仍连续工作。
- 连续执行 10 次“按住 ZL -> 转动 -> 松开 ZL”，无粘滞输入。
- 执行 5 次“游戏 -> GameFold Home -> 返回游戏”，无残留输入，控制器恢复正常。
- 执行一次前后台切换和一次正常退出，日志确认传感器监听已注销。

### 性能边界

- 只在 GTA5 游戏会话存在时注册陀螺仪。
- `onSensorChanged()` 只做常量时间的坐标转换、滤波和状态发布。
- 传感器事件到 `sendVirtualGamepadState()` 的应用内日志差值目标为 `p95 <= 25 ms`。
- 相同 GTA5 场景下，实验前后不得出现超过 2 FPS 的持续下降；若超过，先把发布频率降至 60 Hz 复测。

---

## 4. 实施任务

### Task 1: 在最新基线上重新确认接点

**Files:**
- Read: `docs/CURRENT_GAMEFOLD_PC_BASELINE.md`
- Read: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Read: `app/src/main/java/com/winlator/winhandler/WinHandler.java`
- Read: `app/src/main/java/app/gamenative/gamefold/rear/RearTouchInputRouter.kt`
- Read: `app/src/main/java/app/gamenative/utils/GtaFullscreenConfig.kt`

**Interfaces:**
- Consumes: 当时最新正式基线 tag 和源码。
- Produces: ZL、右摇杆、虚拟手柄发布和 GTA5 身份识别的新基线链路记录。

- [ ] **Step 1: 记录工作区与正式 tag**

```bash
git status --short --branch
git tag --list 'gamefold-pc-v*-baseline' --sort=-version:refname | head -n 5
```

Expected: 明确当前分支、未提交文件和最新正式 tag；不执行覆盖或清理命令。

- [ ] **Step 2: 从最新正式 tag 创建独立候选分支或 worktree**

使用当时确定的正式 tag 和新候选身份；不得在 tag 上直接提交，不得复用正式基线的 `versionName`/`versionCode`。

- [ ] **Step 3: 重新搜索输入链路**

```bash
rg -n 'GameFoldButton.ZL|thumbRX|thumbRY|sendVirtualGamepadState|RearTouchInputRouter.register|GTAVLauncher.exe|GTA5.exe' \
  app/src/main/java/app/gamenative \
  app/src/main/java/com/winlator
```

Expected: 能从 ZL/右摇杆追踪到 `GamepadState` 和 `WinHandler`。若新基线结构已变化，先更新本计划中的文件路径和接口，再编码；不能直接套用 0.8.3 补丁。

- [ ] **Step 4: 建立未修改基线测试结果**

```bash
./gradlew :app:testLegacyReleaseUnitTest --offline --no-daemon --console=plain
```

Expected: 新功能实施前套件为绿色；若基线自身失败，停止陀螺仪工作并单独处理。

### Task 2: 统一 GTA5 身份策略

**Files:**
- Create: `app/src/main/java/app/gamenative/product/GameFoldPcGameIdentity.kt`
- Modify: `app/src/main/java/app/gamenative/utils/GtaFullscreenConfig.kt`
- Create: `app/src/test/java/app/gamenative/product/GameFoldPcGameIdentityTest.kt`

**Interfaces:**
- Consumes: Windows EXE 相对路径。
- Produces: `GameFoldPcGameIdentity.isGtaVExecutable(path: String): Boolean`。

- [ ] **Step 1: 写失败测试**

```kotlin
@Test
fun gtaVRecognizesLauncherAndRuntimeCaseInsensitively() {
    assertTrue(GameFoldPcGameIdentity.isGtaVExecutable("Grand Theft Auto V/GTAVLauncher.exe"))
    assertTrue(GameFoldPcGameIdentity.isGtaVExecutable("gta5.EXE"))
}

@Test
fun gtaVRejectsOtherExecutables() {
    assertFalse(GameFoldPcGameIdentity.isGtaVExecutable("PlayGTAV.exe"))
    assertFalse(GameFoldPcGameIdentity.isGtaVExecutable("Cyberpunk2077.exe"))
    assertFalse(GameFoldPcGameIdentity.isGtaVExecutable(""))
}
```

- [ ] **Step 2: 运行并确认测试因策略不存在而失败**

```bash
./gradlew :app:testLegacyReleaseUnitTest \
  --tests 'app.gamenative.product.GameFoldPcGameIdentityTest' \
  --offline --no-daemon --console=plain
```

- [ ] **Step 3: 添加最小实现并让 `GtaFullscreenConfig` 复用它**

```kotlin
object GameFoldPcGameIdentity {
    fun isGtaVExecutable(path: String): Boolean {
        val name = java.io.File(path.replace('\\', '/')).name
        return name.equals("GTAVLauncher.exe", ignoreCase = true) ||
            name.equals("GTA5.exe", ignoreCase = true)
    }
}
```

- [ ] **Step 4: 运行身份与全屏回归测试**

```bash
./gradlew :app:testLegacyReleaseUnitTest \
  --tests 'app.gamenative.product.GameFoldPcGameIdentityTest' \
  --tests 'app.gamenative.utils.GtaFullscreenConfigTest' \
  --offline --no-daemon --console=plain
```

Expected: 两个测试类全部通过。

- [ ] **Step 5: 提交 GTA5 身份策略**

```bash
git add app/src/main/java/app/gamenative/product/GameFoldPcGameIdentity.kt \
  app/src/main/java/app/gamenative/utils/GtaFullscreenConfig.kt \
  app/src/test/java/app/gamenative/product/GameFoldPcGameIdentityTest.kt
git commit -m 'refactor: share GTA V executable identity'
```

### Task 3: 实现纯 Kotlin 陀螺仪处理器

**Files:**
- Create: `app/src/main/java/app/gamenative/gamefold/gyro/GyroAimProcessor.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/gyro/GyroAimProcessorTest.kt`

**Interfaces:**
- Consumes: `GyroSample(xRadPerSec, yRadPerSec, zRadPerSec, timestampNs, rotation)` 和 ZL gate。
- Produces: `GyroAimVector(x, y)`，逐轴范围 `[-0.85, 0.85]`。
- Produces: `setAimHeld(held: Boolean): GyroAimVector`；传入 `false` 同步返回零向量。

- [ ] **Step 1: 写失败测试**

测试必须覆盖：未按 ZL 恒为零、松开立即归零、四个显示旋转、死区、偏置学习、最大贡献限制、异常时间间隔。

```kotlin
@Test
fun samplesAreZeroWhenZlIsNotHeld() {
    val output = GyroAimProcessor().onSample(
        GyroSample(0f, 1f, 0f, 10_000_000L, DisplayQuarterTurn.ROTATION_0),
    )
    assertEquals(GyroAimVector.ZERO, output)
}

@Test
fun releasingZlReturnsZeroImmediately() {
    val processor = GyroAimProcessor()
    processor.setAimHeld(true)
    processor.onSample(GyroSample(0f, 1f, 0f, 10_000_000L, DisplayQuarterTurn.ROTATION_0))
    assertEquals(GyroAimVector.ZERO, processor.setAimHeld(false))
}
```

- [ ] **Step 2: 运行并确认测试失败**

```bash
./gradlew :app:testLegacyReleaseUnitTest \
  --tests 'app.gamenative.gamefold.gyro.GyroAimProcessorTest' \
  --offline --no-daemon --console=plain
```

- [ ] **Step 3: 实现模型与处理器**

```kotlin
internal enum class DisplayQuarterTurn { ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270 }

internal data class GyroSample(
    val xRadPerSec: Float,
    val yRadPerSec: Float,
    val zRadPerSec: Float,
    val timestampNs: Long,
    val rotation: DisplayQuarterTurn,
)

internal data class GyroAimVector(val x: Float, val y: Float) {
    companion object { val ZERO = GyroAimVector(0f, 0f) }
}

internal data class GyroAimTuning(
    val yawGain: Float = 0.45f,
    val pitchGain: Float = 0.35f,
    val deadzoneRadPerSec: Float = 0.025f,
    val cutoffHz: Float = 12f,
    val biasAlpha: Float = 0.02f,
    val biasLearningThreshold: Float = 0.15f,
    val maximumContribution: Float = 0.85f,
)
```

处理器规则：未瞄准且近静止时学习偏置；瞄准期间冻结偏置；`dt` 限制到 `0.001..0.05` 秒；低通系数为 `1 - exp(-2 * PI * cutoffHz * dt)`；首样本直接初始化滤波；`reset()` 清除所有状态。

四方向映射固定为：

```text
ROTATION_0:   screenX = rawX,  screenY = rawY
ROTATION_90:  screenX = rawY,  screenY = -rawX
ROTATION_180: screenX = -rawX, screenY = -rawY
ROTATION_270: screenX = -rawY, screenY = rawX
```

- [ ] **Step 4: 运行处理器测试并提交**

```bash
./gradlew :app:testLegacyReleaseUnitTest \
  --tests 'app.gamenative.gamefold.gyro.GyroAimProcessorTest' \
  --offline --no-daemon --console=plain
git add app/src/main/java/app/gamenative/gamefold/gyro/GyroAimProcessor.kt \
  app/src/test/java/app/gamenative/gamefold/gyro/GyroAimProcessorTest.kt
git commit -m 'feat: add gated gyro aim processor'
```

### Task 4: 抽出可测试的运行时虚拟手柄桥

**Files:**
- Create: `app/src/main/java/app/gamenative/gamefold/gyro/GameFoldRuntimeGamepadBridge.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/gyro/GameFoldRuntimeGamepadBridgeTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`

**Interfaces:**
- Consumes: `GameFoldControllerListener` 事件和 `onGyroAimChanged(vector)`。
- Produces: 完整 `GamepadState`，通过 `(GamepadState) -> Unit` 发布。
- Produces: ZL gate 回调 `(Boolean) -> Unit`。

- [ ] **Step 1: 写失败测试**

测试 ZL 同时控制 `triggerL` 和 gate；测试陀螺仪与手动右摇杆相加、饱和限制、清零后保留手动摇杆，以及现有 A/B/X/Y/L/R/ZR/D-pad/L3/R3/HOME 行为不变。

```kotlin
bridge.onStickChanged(GameFoldStick.RIGHT, 0.40f, -0.20f)
bridge.onGyroAimChanged(GyroAimVector(0.30f, 0.10f))
assertEquals(0.70f, states.last().thumbRX, 0.001f)
assertEquals(-0.10f, states.last().thumbRY, 0.001f)
bridge.onGyroAimChanged(GyroAimVector.ZERO)
assertEquals(0.40f, states.last().thumbRX, 0.001f)
assertEquals(-0.20f, states.last().thumbRY, 0.001f)
```

- [ ] **Step 2: 运行并确认测试失败**

```bash
./gradlew :app:testLegacyReleaseUnitTest \
  --tests 'app.gamenative.gamefold.gyro.GameFoldRuntimeGamepadBridgeTest' \
  --offline --no-daemon --console=plain
```

- [ ] **Step 3: 实现桥并替换 `XServerScreen` 的匿名状态映射**

桥内部必须分开保存手动右摇杆和陀螺仪向量；退出确认对话框使用 `interceptButton(button, pressed): Boolean` 保留现有逻辑；HOME 仍调用 `returnHomeFromGame()`。

```kotlin
val bridge = GameFoldRuntimeGamepadBridge(
    publish = { nextState ->
        handler.setGameFoldVirtualGamepadState(nextState)
        handler.sendVirtualGamepadState(nextState)
    },
    onAimHeldChanged = { held -> gyroAimController?.setAimHeld(held) },
    onHomePressed = ::returnHomeFromGame,
    interceptButton = ::handleExitDialogControllerButton,
)
```

- [ ] **Step 4: 运行控制器回归并提交**

```bash
./gradlew :app:testLegacyReleaseUnitTest \
  --tests 'app.gamenative.gamefold.gyro.GameFoldRuntimeGamepadBridgeTest' \
  --tests 'app.gamenative.gamefold.ui.GameFoldControllerInputTest' \
  --tests 'app.gamenative.gamefold.rear.RearTouchInputRouterTest' \
  --tests 'app.gamenative.gamefold.ui.GameFoldPcFrontendPolicyTest' \
  --offline --no-daemon --console=plain
git add app/src/main/java/app/gamenative/gamefold/gyro/GameFoldRuntimeGamepadBridge.kt \
  app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt \
  app/src/test/java/app/gamenative/gamefold/gyro/GameFoldRuntimeGamepadBridgeTest.kt
git commit -m 'refactor: isolate GameFold runtime gamepad bridge'
```

### Task 5: 接入 Android 陀螺仪生命周期

**Files:**
- Create: `app/src/main/java/app/gamenative/gamefold/gyro/AndroidGyroAimController.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/gyro/GyroAimSessionPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`

**Interfaces:**
- Consumes: `SensorManager`、显示旋转、`setAimHeld(Boolean)`。
- Produces: `start(): Boolean`、`stop()` 和 `(GyroAimVector) -> Unit`。

- [ ] **Step 1: 写会话策略测试**

```kotlin
internal object GyroAimSessionPolicy {
    fun shouldEnable(localShell: Boolean, executablePath: String): Boolean
}
```

测试 `localShell=true + GTA5` 为 true；非本地 shell、其他 EXE 和空路径均为 false。

- [ ] **Step 2: 实现 Android 适配器**

```kotlin
internal class AndroidGyroAimController(
    context: Context,
    private val rotationProvider: () -> Int,
    private val onVectorChanged: (GyroAimVector) -> Unit,
    private val processor: GyroAimProcessor = GyroAimProcessor(),
) : SensorEventListener {
    fun start(): Boolean
    fun setAimHeld(held: Boolean)
    fun stop()
}
```

实现要求：使用 application context；默认 `TYPE_GYROSCOPE`；主线程 Handler；采样 `10_000 µs`；批处理延迟 `0`；无传感器时返回 false；松开 ZL 立即发零；`stop()` 先注销再 reset 并发零；不逐样本写日志。

- [ ] **Step 3: 在 GTA5 会话创建，在释放时销毁**

创建条件为：

```kotlin
GyroAimSessionPolicy.shouldEnable(
    localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
    executablePath = container.executablePath,
)
```

创建顺序：先手柄桥，再传感器控制器。释放顺序：先 `gyroAimController.stop()`，再 `RearTouchInputRouter.unregister()`，避免 teardown 中重发非零值。

- [ ] **Step 4: 运行目标测试并提交**

```bash
./gradlew :app:testLegacyReleaseUnitTest \
  --tests 'app.gamenative.gamefold.gyro.*' \
  --tests 'app.gamenative.product.GameFoldPcGameIdentityTest' \
  --tests 'app.gamenative.gamefold.ui.GameFoldPcFrontendPolicyTest' \
  --offline --no-daemon --console=plain
git add app/src/main/java/app/gamenative/gamefold/gyro \
  app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt \
  app/src/test/java/app/gamenative/gamefold/gyro
git commit -m 'feat: enable GTA V gyro aim while ZL is held'
```

### Task 6: 全量验证与实验候选

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `releases/candidates/` 下的实际候选目录、APK、`README.md` 和 `EVIDENCE.md`

**Interfaces:**
- Consumes: 完成的实验源码与当时最新基线身份。
- Produces: 新版本号、新 versionCode、单独命名且可回滚的实验 APK。

- [ ] **Step 1: 设置新候选身份**

使用严格递增的 `versionCode` 和包含 `GTA5-GYRO-AIM-RC1` 的候选名，不复用正式基线身份。

- [ ] **Step 2: 运行格式、目标测试、全量测试和构建**

```bash
git diff --check
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:testLegacyReleaseUnitTest :app:assembleLegacyRelease \
  --offline --no-daemon --console=plain --rerun-tasks
```

Expected: diff check 无输出、零测试失败、release 构建成功。

- [ ] **Step 3: 归档但不提升正式基线**

候选证据必须记录源 tag/commit、候选 commit、package、版本、APK 大小/SHA-256、签名证书 SHA-256、测试、构建、固定参数，并声明“尚未完成 GTA5 真机瞄准验收，不是正式基线”。

### Task 7: vivo/OPPO GTA5 真机实验

**Files:**
- Modify: 候选目录中的 `EVIDENCE.md`

**Interfaces:**
- Consumes: 已归档实验候选和两台目标设备。
- Produces: 方向、门控、漂移、叠加、生命周期和性能的验收记录。

- [ ] **Step 1: 安装前记录设备和当前包**

```bash
/Users/11085429/Library/Android/sdk/platform-tools/adb devices -l
/Users/11085429/Library/Android/sdk/platform-tools/adb shell dumpsys package com.gamefold.pc | \
  rg 'versionName|versionCode|firstInstallTime|lastUpdateTime'
```

- [ ] **Step 2: 原位安装并验证身份**

vivo 使用 `/Users/11085429/.codex/skills/install-apk-vivo/scripts/install_apk_vivo.py`。不得卸载、清数据或重置权限。安装后拉取 APK，并比较设备 APK 与归档 APK 的大小和 SHA-256。

- [ ] **Step 3: 执行功能矩阵**

| 场景 | 操作 | 必须结果 |
|---|---|---|
| 非瞄准门控 | 不按 ZL，大幅旋转手机 | 镜头不受陀螺仪影响 |
| 基本瞄准 | 按住 ZL，左右/上下转动 | 准星方向正确且连续 |
| 即时释放 | 转动中松开 ZL | 陀螺仪影响立即停止 |
| 右摇杆叠加 | 右摇杆与陀螺仪同向/反向 | 可叠加，无跳零 |
| 重复门控 | 连续 10 次按下/松开 | 无残留输入 |
| Home 恢复 | 5 次 Home/返回游戏 | 无粘滞，控制器正常 |
| 前后台 | 切后台再返回 | 未按 ZL 时保持零输出 |
| 退出 | 正常退出 GTA5 | listener 注销，无后台采样 |

- [ ] **Step 4: 单变量调参**

按固定顺序进行：先修轴符号，再调水平增益，再调垂直增益；漂移时以 `0.005 rad/s` 步长提高死区且不超过 `0.05 rad/s`；抖动时把截止频率由 12 Hz 依次降至 10 Hz、8 Hz；迟滞时恢复更高截止频率，不用提高增益掩盖延迟。

- [ ] **Step 5: 判定结果**

两台设备都满足第 3 节才标记 `PASS`。任一设备存在非 ZL 响应、松开残留、方向不稳定或 Home 恢复后粘滞，标记 `PARTIAL`，不得提升正式基线。

---

## 5. 失败保护与回滚

- `TYPE_GYROSCOPE` 不存在或注册失败：记录一次 warning，保持原控制器链路，不弹阻断对话框。
- 传感器时间戳倒退或间隔超过 50 ms：重置滤波时间基准并限制本次 `dt`。
- ZL 松开：同步清零并发布，不依赖后续 sensor callback。
- GameFold Home/AndroidView release：先停止陀螺仪，再注销后屏输入桥。
- 进程恢复：创建新的 controller，不复用旧滤波状态。
- 实验失败：原位安装回当时正式基线 APK；不修改正式基线指针、tag 或归档。

## 6. 实验通过后的产品化边界

以下能力单独设计，不能混入首个实验候选：

- 每游戏“陀螺仪瞄准”开关；
- 水平/垂直灵敏度和反向轴设置；
- 仅 ZL、始终开启或自定义按键门控；
- 非 GTA5 游戏支持；
- 参数持久化与迁移。

产品化时推荐采用“每游戏设置、默认关闭”，但第一阶段先以 GTA5 自动识别验证技术链路和手感。

## 7. 官方技术参考

- Android motion sensors: <https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion>
- Android sensor coordinate system: <https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview>
- `SensorManager.registerListener`: <https://developer.android.com/reference/android/hardware/SensorManager>
