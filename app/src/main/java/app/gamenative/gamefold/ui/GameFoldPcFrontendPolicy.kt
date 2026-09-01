package app.gamenative.gamefold.ui

import kotlin.math.roundToInt

/**
 * Frontend-only contract for the local GamePad PC shell.
 *
 * The runtime and launch pipeline deliberately do not depend on this policy. It only answers
 * where PC-owned content is allowed to render and which NS visual treatment is selected.
 */
object GameFoldPcFrontendPolicy {
    enum class HomeLaunchControlAction {
        CANCEL_LAUNCH,
        ROUTE_NORMALLY,
    }

    fun homeLaunchControlAction(
        localShell: Boolean,
        launchPreparing: Boolean,
        button: GameFoldButton,
    ): HomeLaunchControlAction =
        if (
            localShell &&
            launchPreparing &&
            button in setOf(GameFoldButton.HOME, GameFoldButton.B, GameFoldButton.Y)
        ) {
            HomeLaunchControlAction.CANCEL_LAUNCH
        } else {
            HomeLaunchControlAction.ROUTE_NORMALLY
        }

    fun shouldApplyBootingUpdate(
        localShell: Boolean,
        pausedAtHome: Boolean,
    ): Boolean = !localShell || !pausedAtHome

    enum class HeaderFocusLayer {
        NONE,
        BACKING,
        FOREGROUND,
    }

    fun headerFocusLayer(ownsFrame: Boolean, focused: Boolean): HeaderFocusLayer = when {
        !focused -> HeaderFocusLayer.NONE
        ownsFrame -> HeaderFocusLayer.BACKING
        else -> HeaderFocusLayer.FOREGROUND
    }

    fun headerIconOffsetDp(
        centerOnUnshadowedFace: Boolean,
        focusLayer: HeaderFocusLayer,
    ): Int = if (
        centerOnUnshadowedFace && focusLayer != HeaderFocusLayer.FOREGROUND
    ) {
        -2
    } else {
        0
    }

    data class ControllerNavigationDecision(
        val consumeRequest: Boolean,
        val dispatchToHome: Boolean,
    )

    fun controllerNavigationDecision(
        request: Int,
        lastConsumedRequest: Int,
        modalVisible: Boolean,
    ): ControllerNavigationDecision {
        val isNewRequest = request > lastConsumedRequest
        return ControllerNavigationDecision(
            consumeRequest = isNewRequest,
            dispatchToHome = isNewRequest && !modalVisible,
        )
    }

    fun showHomeLaunchPreparingSurface(
        localShell: Boolean,
        loadingVisible: Boolean,
        bootingVisible: Boolean,
    ): Boolean = localShell && (loadingVisible || bootingVisible)

    fun shouldDismissControllerModal(
        visible: Boolean,
        controllerRequest: Int,
        openedAtRequest: Int,
    ): Boolean = visible && controllerRequest > openedAtRequest

    data class Presentation(
        val useGameFoldShell: Boolean,
        val controllerPersistent: Boolean,
        val contentConstrainedToDisplay: Boolean,
        val useRetroLoadingCard: Boolean,
        val usePixelFps: Boolean,
        val loadingOverlayOwnedByParentShell: Boolean,
        val controllerInputForwarded: Boolean,
    )

    fun forProduct(localShell: Boolean): Presentation = if (localShell) {
        Presentation(
            useGameFoldShell = true,
            controllerPersistent = true,
            contentConstrainedToDisplay = true,
            useRetroLoadingCard = true,
            usePixelFps = true,
            loadingOverlayOwnedByParentShell = true,
            controllerInputForwarded = true,
        )
    } else {
        Presentation(
            useGameFoldShell = false,
            controllerPersistent = false,
            contentConstrainedToDisplay = false,
            useRetroLoadingCard = false,
            usePixelFps = false,
            loadingOverlayOwnedByParentShell = false,
            controllerInputForwarded = false,
        )
    }

    /** NS PixelNumberView displays an integer and never exposes a decimal FPS value. */
    fun pixelFps(value: Float): Int = value.roundToInt().coerceAtLeast(0)

    /**
     * LSFG presentation samples are already post-generation. Never multiply them again or use
     * guest window-update callbacks as a substitute for generated output frames.
     */
    fun displayedFps(
        lsfgActive: Boolean,
        guestFps: Float,
        presentedFps: Float,
    ): Float = (if (lsfgActive) presentedFps else guestFps).coerceAtLeast(0f)

    /**
     * Keeps every local-shell loading surface on the same concrete Chinese stage copy.
     * Dependencies and Wine helpers still emit a few English progress strings; translating at
     * this boundary prevents the home card and XServer card from disagreeing or falling back to
     * an opaque "Loading..." label.
     */
    fun localizeLoadingText(raw: String): String {
        val message = raw.trim()
        val lower = message.lowercase()
        return when {
            message.isBlank() || lower == "loading" || lower == "loading..." -> "正在准备运行环境"
            lower.contains("extracting xaudio") -> "正在准备音频组件"
            lower.contains("installing prerequisites") -> "正在安装运行依赖"
            lower.contains("installing mono") -> "正在安装 Mono"
            lower.contains("handling drm") -> "正在处理 DRM"
            lower.contains("launching game") -> "正在启动游戏"
            lower.startsWith("downloading ") -> "正在下载 ${message.substringAfter(' ')}"
            lower.startsWith("extracting ") -> "正在解压 ${message.substringAfter(' ')}"
            lower.startsWith("installing ") -> "正在安装 ${message.substringAfter(' ')}"
            lower.startsWith("syncing ") -> "正在同步 ${message.substringAfter(' ')}"
            else -> message
        }
    }
}
