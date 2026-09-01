package app.gamenative.gamefold.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldPcFrontendPolicyTest {

    @Test
    fun homeBackButtonsCancelOnlyWhileLocalLaunchSurfaceIsPreparing() {
        listOf(GameFoldButton.HOME, GameFoldButton.B, GameFoldButton.Y).forEach { button ->
            assertEquals(
                GameFoldPcFrontendPolicy.HomeLaunchControlAction.CANCEL_LAUNCH,
                GameFoldPcFrontendPolicy.homeLaunchControlAction(
                    localShell = true,
                    launchPreparing = true,
                    button = button,
                ),
            )
        }
        assertEquals(
            GameFoldPcFrontendPolicy.HomeLaunchControlAction.ROUTE_NORMALLY,
            GameFoldPcFrontendPolicy.homeLaunchControlAction(
                localShell = true,
                launchPreparing = true,
                button = GameFoldButton.A,
            ),
        )
        assertEquals(
            GameFoldPcFrontendPolicy.HomeLaunchControlAction.ROUTE_NORMALLY,
            GameFoldPcFrontendPolicy.homeLaunchControlAction(
                localShell = true,
                launchPreparing = false,
                button = GameFoldButton.HOME,
            ),
        )
    }

    @Test
    fun pausedAtHomeIgnoresLateBootProgressFromBackgroundRuntime() {
        assertFalse(
            GameFoldPcFrontendPolicy.shouldApplyBootingUpdate(
                localShell = true,
                pausedAtHome = true,
            ),
        )
        assertTrue(
            GameFoldPcFrontendPolicy.shouldApplyBootingUpdate(
                localShell = true,
                pausedAtHome = false,
            ),
        )
        assertTrue(
            GameFoldPcFrontendPolicy.shouldApplyBootingUpdate(
                localShell = false,
                pausedAtHome = true,
            ),
        )
    }

    @Test
    fun headerFocus_placesOwnedDualScreenFrameBehindButKeepsRegularFramesInFront() {
        assertEquals(
            GameFoldPcFrontendPolicy.HeaderFocusLayer.BACKING,
            GameFoldPcFrontendPolicy.headerFocusLayer(ownsFrame = true, focused = true),
        )
        assertEquals(
            GameFoldPcFrontendPolicy.HeaderFocusLayer.FOREGROUND,
            GameFoldPcFrontendPolicy.headerFocusLayer(ownsFrame = false, focused = true),
        )
        assertEquals(
            GameFoldPcFrontendPolicy.HeaderFocusLayer.NONE,
            GameFoldPcFrontendPolicy.headerFocusLayer(ownsFrame = true, focused = false),
        )
    }

    @Test
    fun headerIcon_centersOnFocusedFrameAndUnfocusedButtonFace() {
        assertEquals(
            -2,
            GameFoldPcFrontendPolicy.headerIconOffsetDp(
                centerOnUnshadowedFace = true,
                focusLayer = GameFoldPcFrontendPolicy.HeaderFocusLayer.NONE,
            ),
        )
        assertEquals(
            0,
            GameFoldPcFrontendPolicy.headerIconOffsetDp(
                centerOnUnshadowedFace = true,
                focusLayer = GameFoldPcFrontendPolicy.HeaderFocusLayer.FOREGROUND,
            ),
        )
        assertEquals(
            0,
            GameFoldPcFrontendPolicy.headerIconOffsetDp(
                centerOnUnshadowedFace = false,
                focusLayer = GameFoldPcFrontendPolicy.HeaderFocusLayer.NONE,
            ),
        )
    }

    @Test
    fun controllerNavigationPressedInsideModal_isConsumedWithoutReplayingOnHome() {
        val modalDecision = GameFoldPcFrontendPolicy.controllerNavigationDecision(
            request = 8,
            lastConsumedRequest = 7,
            modalVisible = true,
        )
        assertTrue(modalDecision.consumeRequest)
        assertFalse(modalDecision.dispatchToHome)

        val afterCloseDecision = GameFoldPcFrontendPolicy.controllerNavigationDecision(
            request = 8,
            lastConsumedRequest = 8,
            modalVisible = false,
        )
        assertFalse(afterCloseDecision.consumeRequest)
        assertFalse(afterCloseDecision.dispatchToHome)
    }

    @Test
    fun localShellPrelaunch_replacesHomeWithPreparingSurfaceImmediately() {
        assertTrue(
            GameFoldPcFrontendPolicy.showHomeLaunchPreparingSurface(
                localShell = true,
                loadingVisible = false,
                bootingVisible = true,
            ),
        )
        assertTrue(
            GameFoldPcFrontendPolicy.showHomeLaunchPreparingSurface(
                localShell = true,
                loadingVisible = true,
                bootingVisible = false,
            ),
        )
        assertFalse(
            GameFoldPcFrontendPolicy.showHomeLaunchPreparingSurface(
                localShell = false,
                loadingVisible = true,
                bootingVisible = true,
            ),
        )
    }

    @Test
    fun controllerModalConsumesOnlyButtonsPressedAfterItOpened() {
        assertFalse(GameFoldPcFrontendPolicy.shouldDismissControllerModal(true, 7, 7))
        assertTrue(GameFoldPcFrontendPolicy.shouldDismissControllerModal(true, 8, 7))
        assertFalse(GameFoldPcFrontendPolicy.shouldDismissControllerModal(false, 8, 7))
    }

    private fun sourceFile(path: String): java.io.File = sequenceOf(
        java.io.File(path),
        java.io.File("../$path"),
    ).firstOrNull { it.isFile }
        ?: error("Source file not found from test working directory: $path")

    @Test
    fun pauseDialogUsesNsWidthAndSquareLoadingCardContract() {
        val dialogSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt",
        ).readText()
        val pixelDialogSource = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcPixelDialog.kt",
        ).readText()
        assertTrue(dialogSource.contains("cardWidthFraction = 0.60f"))
        assertTrue(pixelDialogSource.contains("cardWidthFraction: Float = 0.84f"))
        assertTrue(pixelDialogSource.contains("coerceIn(0.48f, 0.92f)"))
    }

    @Test
    fun localShellUsesNsPermissionSurfaceAndSquareActions() {
        val permissionSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/runtime/GameFoldPcRuntimeProvisionGate.kt",
        ).readText()
        assertTrue(permissionSource.contains("首次启动 · 素材访问"))
        assertTrue(permissionSource.contains("素材源 · \${GameFoldPcResourceRoot.root().path}"))
        assertTrue(permissionSource.contains("GameFoldPcPixelButton"))
        assertTrue(permissionSource.contains("label = \"暂不授权\""))
        assertTrue(permissionSource.contains("label = if (unsupported) \"重新检查\" else \"确认授权\""))
        assertTrue(permissionSource.contains(".fillMaxWidth(0.84f)"))
        assertTrue(permissionSource.contains("label = \"允许文件访问\""))
        assertTrue(permissionSource.contains("background(GameFoldNsTokens.Background)"))
        assertTrue(permissionSource.contains("override fun dispatchNavigationToCompose(): Boolean = true"))
        val librarySource = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt",
        ).readText()
        assertTrue(librarySource.contains("首次启动 · 素材访问"))
        assertTrue(librarySource.contains("label = \"确认授权\""))
        assertTrue(librarySource.contains("GameFoldPcResourceRoot.root().path"))
    }

    @Test
    fun localShellUsesStableStartupRouteAndMtkSurface() {
        val startupSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/runtime/GameFoldPcRuntimeProvisionGate.kt",
        ).readText()
        assertTrue(startupSource.contains("onStartupRouteReady: () -> Unit"))
        assertTrue(startupSource.contains("GameFoldPcStartupRoutePolicy.resolve"))
        assertTrue(startupSource.contains("GameFoldPcStartupRoute.UNSUPPORTED_MTK"))
        assertTrue(startupSource.contains("GameFoldPcMtkUnsupportedScreen"))
        assertTrue(startupSource.contains("R.string.gamefold_mtk_fc_link"))
        assertTrue(startupSource.contains("https://game-fold.pages.dev"))
    }

    @Test
    fun coldStartLocksGamePadLandscapeAndSplashBeforeActivityCreation() {
        val activitySource = sourceFile(
            "app/src/main/java/app/gamenative/MainActivity.kt",
        ).readText()
        val onCreateStart = activitySource.indexOf("override fun onCreate(savedInstanceState: Bundle?)")
        val orientationApply = activitySource.indexOf(
            "requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE",
            onCreateStart,
        )
        val installSplash = activitySource.indexOf("installSplashScreen()", onCreateStart)
        val superCreate = activitySource.indexOf("super.onCreate(savedInstanceState)", onCreateStart)
        assertTrue(orientationApply in (onCreateStart + 1) until superCreate)
        assertTrue(installSplash in (onCreateStart + 1) until superCreate)
        val manifest = sourceFile("app/src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:theme=\"@style/Theme.GameFoldPc.Splash\""))
        val themes = sourceFile("app/src/main/res/values/themes.xml").readText()
        assertTrue(themes.contains("name=\"Theme.GameFoldPc.Splash\""))
        assertTrue(themes.contains("windowSplashScreenAnimatedIcon"))
        assertTrue(themes.contains("@drawable/gamefold_splash_icon"))
    }

    @Test
    fun quickStartEntryAndPageAreAbsentFromGamePadHome() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt",
        ).readText()
        val home = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/HomeScreen.kt",
        ).readText()
        assertFalse(source.contains("快速上手"))
        assertFalse(source.contains("GameFoldPcQuickStartScreen"))
        assertFalse(source.contains("onQuickStart"))
        assertFalse(home.contains("quickStartVisible"))
        assertFalse(source.contains("0xFF79A56A"))
    }

    @Test
    fun localShellBridgesPhysicalGamepadKeysIntoTheSameSemanticController() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcShellHost.kt",
        ).readText()
        assertTrue(source.contains("AndroidEvent.KeyEvent"))
        assertTrue(source.contains("ExternalController.isGameController"))
        assertTrue(source.contains("listenerBridge.onButtonChanged"))
        assertTrue(source.contains("KeyEvent.KEYCODE_BUTTON_MODE -> GameFoldButton.HOME"))
        assertTrue(source.contains("AndroidEvent.MotionEvent"))
        assertTrue(source.contains("STICK_INITIAL_REPEAT_DELAY_MS"))
        assertTrue(source.contains("KeyEvent.KEYCODE_BUTTON_R1"))
        assertTrue(source.contains("isShoulder()"))
    }

    @Test
    fun settingsDetailsForwardDirectionalEventsIntoComposeFocusSearch() {
        val controllerSource = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldControllerInput.kt",
        ).readText()
        val settingsSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/settings/SettingsScreen.kt",
        ).readText()
        assertTrue(controllerSource.contains("dispatchNavigationToCompose"))
        assertTrue(settingsSource.contains("focusManager.moveFocus(FocusDirection.Down)"))
        assertTrue(settingsSource.contains("settingsDetailBackFocusRequester"))
        assertTrue(settingsSource.contains("targetValue = if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) 1f"))
    }

    @Test
    fun settingsBackAffordanceDoesNotRenderAnUnfocusedMaterialCard() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/settings/SettingsScreen.kt",
        ).readText()
        assertTrue(source.contains("BuildConfig.GAMEFOLD_PC_LOCAL_SHELL && !isFocused"))
        assertTrue(source.contains("Color.Transparent"))
    }

    @Test
    fun physicalHomeButtonUsesShellPauseAndReturnHomePath() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt",
        ).readText()
        assertTrue(source.contains("KeyEvent.KEYCODE_BUTTON_MODE"))
        assertTrue(source.contains("returnHomeFromGame()"))
        assertTrue(source.contains("BuildConfig.GAMEFOLD_PC_LOCAL_SHELL &&"))
    }

    @Test
    fun localShellKeepsControllerAndConstrictsChangingContentToDisplay() {
        val policy = GameFoldPcFrontendPolicy.forProduct(localShell = true)

        assertTrue(policy.useGameFoldShell)
        assertTrue(policy.controllerPersistent)
        assertTrue(policy.contentConstrainedToDisplay)
        assertTrue(policy.useRetroLoadingCard)
        assertTrue(policy.usePixelFps)
        assertTrue(policy.loadingOverlayOwnedByParentShell)
        assertTrue(policy.controllerInputForwarded)
    }

    @Test
    fun legacyBuildKeepsExistingSurfaceBehavior() {
        val policy = GameFoldPcFrontendPolicy.forProduct(localShell = false)

        assertFalse(policy.useGameFoldShell)
        assertFalse(policy.controllerPersistent)
        assertFalse(policy.contentConstrainedToDisplay)
        assertFalse(policy.useRetroLoadingCard)
        assertFalse(policy.usePixelFps)
        assertFalse(policy.loadingOverlayOwnedByParentShell)
        assertFalse(policy.controllerInputForwarded)
    }

    @Test
    fun fpsIsRoundedToNonNegativeIntegerLikeNsPixelNumberView() {
        assertEquals(0, GameFoldPcFrontendPolicy.pixelFps(-4.2f))
        assertEquals(28, GameFoldPcFrontendPolicy.pixelFps(27.6f))
        assertEquals(66, GameFoldPcFrontendPolicy.pixelFps(65.5f))
    }

    @Test
    fun lsfgHudUsesMeasuredPresentedRateWithoutMultiplyingGuestCallbacks() {
        assertEquals(
            60f,
            GameFoldPcFrontendPolicy.displayedFps(
                lsfgActive = true,
                guestFps = 125f,
                presentedFps = 60f,
            ),
            0.01f,
        )
        assertEquals(
            0f,
            GameFoldPcFrontendPolicy.displayedFps(
                lsfgActive = true,
                guestFps = 125f,
                presentedFps = 0f,
            ),
            0.01f,
        )
    }

    @Test
    fun nonLsfgHudKeepsGuestFrameRate() {
        assertEquals(
            30f,
            GameFoldPcFrontendPolicy.displayedFps(
                lsfgActive = false,
                guestFps = 30f,
                presentedFps = 60f,
            ),
            0.01f,
        )
    }

    @Test
    fun pixelFpsTextExplicitlyInvalidatesItsCustomHardwareDisplayList() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/ui/widget/PerformanceHudView.kt",
        ).readText()
        assertTrue(source.contains("updatePixelText(safeText)"))
        assertTrue(source.contains("fun updatePixelText(value: String)"))
        assertTrue(source.contains("postInvalidateOnAnimation()"))
    }

    @Test
    fun localLoadingTextNeverFallsBackToOpaqueEnglishStage() {
        assertEquals("正在准备运行环境", GameFoldPcFrontendPolicy.localizeLoadingText("Loading..."))
        assertEquals("正在安装运行依赖", GameFoldPcFrontendPolicy.localizeLoadingText("Installing prerequisites..."))
        assertEquals("正在解压 proton-10.0", GameFoldPcFrontendPolicy.localizeLoadingText("Extracting proton-10.0"))
        assertEquals("正在启动游戏", GameFoldPcFrontendPolicy.localizeLoadingText("Launching game..."))
    }

    @Test
    fun concreteLoadingResourceNameIsKeptForUserVisibleProgress() {
        assertEquals(
            "正在安装 Bionic",
            GameFoldPcFrontendPolicy.localizeLoadingText("Installing Bionic"),
        )
    }

    @Test
    fun pixelSwitchUsesTheNsPaletteAndGeometryInsteadOfButtonActionRed() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldNsDesignSystem.kt",
        ).readText()
        assertTrue(source.contains("checked -> GameFoldNsTokens.Moss"))
        assertTrue(source.contains("else -> GameFoldNsTokens.SurfaceDark"))
        assertTrue(source.contains("checked -> GameFoldNsTokens.Focus"))
        assertTrue(source.contains("else -> GameFoldNsTokens.Surface"))
        assertTrue(source.contains(".height(GameFoldNsTokens.TouchTarget)"))
        assertTrue(source.contains(".size(width = GameFoldNsTokens.SwitchWidth, height = GameFoldNsTokens.SwitchHeight)"))
        assertTrue(source.contains(".size(24.dp)"))
        assertTrue(source.contains("!enabled -> GameFoldNsTokens.DisabledSurface"))
    }

    @Test
    fun loadingCardsUseTheNsApertureWidthInBothRenderingPaths() {
        val composeSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/components/BootingSplash.kt",
        ).readText()
        val nativeSource = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldGameLoadingOverlayView.kt",
        ).readText()
        assertTrue(composeSource.contains("GameFoldNsLoadingContract.CardWidthDp.dp"))
        assertTrue(composeSource.contains("GameFoldNsLoadingContract.CoverSizeDp.dp"))
        assertTrue(composeSource.contains("GameFoldNsLoadingProgress(progress = progress)"))
        assertTrue(nativeSource.contains("GameFoldNsLoadingContract.CardWidthDp * density"))
        assertTrue(nativeSource.contains("GameFoldNsLoadingContract.CoverSizeDp * density"))
        assertTrue(nativeSource.contains("GameFoldNsLoadingContract.SegmentWidthDp * density"))
    }

    @Test
    fun publicDisplayUsesOneNsTokenAndFocusContract() {
        val designSystem = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldNsDesignSystem.kt",
        ).readText()
        assertTrue(designSystem.contains("val Focus = Color(0xFF6D8F9C)"))
        assertTrue(designSystem.contains("val FocusGlow = Color(0xFFD7E7EB)"))
        assertTrue(designSystem.contains("val ButtonHeight = 56.dp"))
        assertTrue(designSystem.contains("val ToolbarHeight = 44.dp"))
        assertTrue(designSystem.contains("val BodyFont = FontFamily.SansSerif"))
        assertTrue(designSystem.contains("val FocusGlowWidth = 5.dp"))
        assertTrue(designSystem.contains("style = Stroke(width = glow)"))

        listOf(
            "app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt",
            "app/src/main/java/app/gamenative/ui/screen/settings/SettingsScreen.kt",
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcPixelDialog.kt",
        ).forEach { path ->
            assertFalse("legacy green focus leaked into $path", sourceFile(path).readText().contains("0xFF79A56A"))
        }
    }

    @Test
    fun homeAndPropertiesFollowNsNavigationAndPageTopology() {
        val home = sourceFile("app/src/main/java/app/gamenative/ui/screen/HomeScreen.kt").readText()
        val library = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt",
        ).readText()
        assertTrue(home.contains("GameFoldButton.HOME -> if (gameDetailsVisible)"))
        assertFalse(home.contains("GameFoldButton.HOME,\n                        ->"))
        assertTrue(library.contains("val dualScreenEnabled = false"))
        assertTrue(library.contains("val dualScreenAvailable = GamePadProductContract.REAR_DISPLAY_ENABLED"))
        assertFalse(library.contains("onToggleOrientation ="))
        assertTrue(library.contains("title = { Text(\"游戏设置\") }"))
        assertTrue(library.contains("label = \"启动游戏\""))
        assertTrue(library.contains("if (showConfigEditor) showConfigEditor = false else onBack()"))
    }

    @Test
    fun rearSessionUsesPairedInternalTransitionAndIndependentBackgroundFallbacks() {
        val controller = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/rear/VivoRearDisplayController.kt",
        ).readText()

        assertTrue(controller.contains("internalRearTransitionGeneration"))
        assertTrue(controller.contains("pendingUserLeave"))
        assertTrue(controller.contains("startedFrontActivities"))
        assertTrue(controller.contains("finishInternalRearTransition"))
        assertTrue(controller.contains("scheduleFinishInternalRearTransition"))
        assertTrue(controller.contains("INTERNAL_REAR_TRANSITION_SETTLE_MS"))
        assertTrue(controller.contains("frontFocusConfirmedAfterRearReady"))
        assertTrue(controller.contains("frontStoppedRunnable"))
        assertTrue(controller.contains("shouldCloseAfterFocusLoss"))
        assertTrue(controller.contains("shouldCloseAfterFrontStopped"))
        assertTrue(controller.contains("rear Activity already active; skip dual reconcile"))
        assertFalse(controller.contains("rearLaunchGraceUntil"))
        assertFalse(controller.contains("if (!userLeaving)"))
    }

    @Test
    fun rearTouchWindowUpdatesLayoutAndImmersiveFlagsIdempotently() {
        val rearActivity = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/rear/GameFoldRearTouchActivity.kt",
        ).readText()

        assertTrue(rearActivity.contains("rearTouchLayout(width, height)"))
        assertTrue(rearActivity.contains("shouldUpdateRearTouchLayout("))
        assertTrue(rearActivity.contains("if (params.gravity != Gravity.CENTER)"))
        assertTrue(rearActivity.contains("if (window.decorView.systemUiVisibility != targetVisibility)"))
        assertTrue(rearActivity.contains("rearWindowLayoutConfigured"))
    }

    @Test
    fun pauseUsesCapturedXServerFrameLikeNsPauseSurface() {
        val xserver = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt",
        ).readText()
        val overlay = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldPauseOverlayView.kt",
        ).readText()
        assertTrue(xserver.contains("PixelCopy.request("))
        assertTrue(xserver.contains("captureGameFoldPausedFrame()"))
        assertTrue(overlay.contains("fun setPausedFrame(frame: Bitmap?)"))
        assertTrue(overlay.contains("canvas.drawBitmap("))
    }

    @Test
    fun settingsRowsAndDropdownsShareTheNsListContract() {
        val rows = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/settings/GameFoldPcSettingsControls.kt",
        ).readText()
        val dropdown = sourceFile(
            "app/src/main/java/app/gamenative/ui/component/settings/SettingsListDropdown.kt",
        ).readText()
        assertTrue(rows.contains("defaultMinSize(minHeight = GameFoldNsTokens.ListItemHeight)"))
        assertTrue(rows.contains(".gameFoldNsSettingsItemSurface(focused)"))
        assertTrue(rows.contains("fontFamily = GameFoldNsTokens.LabelFont"))
        assertTrue(dropdown.contains("BuildConfig.GAMEFOLD_PC_LOCAL_SHELL"))
        assertTrue(dropdown.contains("defaultMinSize(minHeight = GameFoldNsTokens.ListItemHeight)"))
        assertTrue(dropdown.contains("background(GameFoldNsTokens.Surface, RectangleShape)"))
        assertTrue(dropdown.contains("fontFamily = GameFoldNsTokens.NumericFont"))
    }
}
