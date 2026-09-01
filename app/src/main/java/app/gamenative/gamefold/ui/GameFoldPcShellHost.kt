package app.gamenative.gamefold.ui

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import app.gamenative.BuildConfig
import app.gamenative.PluviaApp
import app.gamenative.events.AndroidEvent
import app.gamenative.gamepad.GamePadProductContract
import app.gamenative.ui.components.GameFoldRetroLoadingCard
import com.winlator.inputcontrols.ExternalController
import kotlin.math.abs

data class GameFoldPcOverlayContent(
    val text: String,
    val progress: Float,
    val artworkUrl: String = "",
    val title: String = "GamePad PC",
    val customContent: (@Composable () -> Unit)? = null,
)

@Stable
class GameFoldPcShellOverlayController {
    var content by mutableStateOf<GameFoldPcOverlayContent?>(null)
        private set
    private val entries = LinkedHashMap<Any, GameFoldPcOverlayContent>()

    fun showLoading(owner: Any, text: String, progress: Float, artworkUrl: String = "", title: String = "GamePad PC") {
        entries[owner] = GameFoldPcOverlayContent(text, progress, artworkUrl, title)
        content = entries.values.lastOrNull()
    }

    fun showCustom(owner: Any, customContent: @Composable () -> Unit) {
        entries[owner] = GameFoldPcOverlayContent("", -1f, customContent = customContent)
        content = entries.values.lastOrNull()
    }

    fun clear(owner: Any) {
        entries.remove(owner)
        content = entries.values.lastOrNull()
    }
}

val LocalGameFoldPcShellOverlay = staticCompositionLocalOf<GameFoldPcShellOverlayController?> { null }

@Stable
class GameFoldPcControllerBackRegistry {
    private val handlers = LinkedHashMap<Any, () -> Unit>()

    fun register(owner: Any, handler: () -> Unit) {
        handlers[owner] = handler
    }

    fun unregister(owner: Any) {
        handlers.remove(owner)
    }

    fun dispatch(pressed: Boolean): Boolean {
        val handler = handlers.values.lastOrNull() ?: return false
        if (pressed) handler()
        return true
    }
}

val LocalGameFoldPcControllerBackRegistry = staticCompositionLocalOf {
    GameFoldPcControllerBackRegistry()
}

@Stable
class GameFoldPcShellOverlayRegistry {
    private val hosts = LinkedHashMap<Any, GameFoldPcShellOverlayController>()
    var activeController by mutableStateOf<GameFoldPcShellOverlayController?>(null)
        private set

    fun attach(owner: Any, controller: GameFoldPcShellOverlayController) {
        hosts[owner] = controller
        activeController = controller
    }

    fun detach(owner: Any) {
        hosts.remove(owner)
        activeController = hosts.values.lastOrNull()
    }

    fun showLoading(owner: Any, text: String, progress: Float, artworkUrl: String = "", title: String = "GamePad PC") {
        activeController?.showLoading(owner, text, progress, artworkUrl, title)
    }

    fun showCustom(owner: Any, customContent: @Composable () -> Unit) {
        activeController?.showCustom(owner, customContent)
    }

    fun clear(owner: Any) {
        hosts.values.forEach { it.clear(owner) }
    }
}

val LocalGameFoldPcShellOverlayRegistry = staticCompositionLocalOf {
    GameFoldPcShellOverlayRegistry()
}

/**
 * Hosts one PC-owned Compose surface inside the official GameFold display/controller shell.
 *
 * XServerScreen owns its existing shell because it also bridges the emulator WinHandler. This
 * host is for Home, Settings, and transient PC-owned states only; it intentionally has no
 * dependency on the PC runtime objects.
 */
@Composable
fun GameFoldPcShellHost(
    modifier: Modifier = Modifier.fillMaxSize(),
    enabled: Boolean = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL &&
        GamePadProductContract.NON_GAME_CONTROLLER_OVERLAY_ENABLED,
    controllerListener: GameFoldControllerListener? = null,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }

    val currentContent = rememberUpdatedState(content)
    val currentControllerListener = rememberUpdatedState(controllerListener)
    val composeViewHolder = remember { arrayOfNulls<ComposeView>(1) }
    val controllerBackRegistry = remember { GameFoldPcControllerBackRegistry() }
    val listenerBridge = remember {
        object : GameFoldControllerListener {
            private val stickHandler = Handler(Looper.getMainLooper())
            private var stickDirection: GameFoldButton? = null
            private val stickRepeat = object : Runnable {
                override fun run() {
                    stickDirection?.let { onButtonChanged(it, true) }
                    if (stickDirection != null) {
                        stickHandler.postDelayed(this, STICK_REPEAT_INTERVAL_MS)
                    }
                }
            }

            override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
                routeGameFoldControllerButton(
                    button = button,
                    pressed = pressed,
                    backRegistry = controllerBackRegistry,
                ) {
                    currentControllerListener.value?.onButtonChanged(button, pressed)
                    if (pressed && (button.toComposeKeyCode() != null)) {
                        val dispatchNavigation = (button.isDirectional() || button.isShoulder()) &&
                            currentControllerListener.value?.dispatchNavigationToCompose() == true
                        val dispatchConfirm = (button == GameFoldButton.A || button == GameFoldButton.X) &&
                            currentControllerListener.value?.dispatchConfirmToCompose() != false
                        if (!dispatchNavigation && !dispatchConfirm) return@routeGameFoldControllerButton
                        button.toComposeKeyCode()?.let { keyCode ->
                            composeViewHolder[0]?.post {
                                val view = composeViewHolder[0] ?: return@post
                                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                                view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                            }
                        }
                    }
                }
            }

            override fun onStickChanged(stick: GameFoldStick, x: Float, y: Float) {
                currentControllerListener.value?.onStickChanged(stick, x, y)
                if (stick != GameFoldStick.LEFT) return
                val nextDirection = stickDirection(x, y, stickDirection)
                if (nextDirection == stickDirection) return
                stickHandler.removeCallbacks(stickRepeat)
                stickDirection?.let { onButtonChanged(it, false) }
                stickDirection = nextDirection
                nextDirection?.let {
                    onButtonChanged(it, true)
                    stickHandler.postDelayed(stickRepeat, STICK_INITIAL_REPEAT_DELAY_MS)
                }
            }

            fun release() {
                stickHandler.removeCallbacks(stickRepeat)
                stickDirection?.let { onButtonChanged(it, false) }
                stickDirection = null
            }
        }
    }

    // MainActivity publishes physical gamepad KeyEvents before Compose sees them. Reuse the
    // same semantic bridge as the on-screen controller so Home, Settings, Quick Start, and the
    // permission surface are not touch-only. XServerScreen owns its own listener and does not
    // mount this host, so game input is not intercepted while a title is running.
    val onPhysicalKeyEvent: (AndroidEvent.KeyEvent) -> Boolean = remember {
        { event ->
            if (!ExternalController.isGameController(event.event.device)) {
                false
            } else {
                val button = event.event.toGameFoldButton()
                if (button == null) {
                    false
                } else {
                    listenerBridge.onButtonChanged(
                        button,
                        event.event.action != KeyEvent.ACTION_UP,
                    )
                    true
                }
            }
        }
    }
    val onPhysicalMotionEvent: (AndroidEvent.MotionEvent) -> Boolean = remember {
        { event ->
            val motion = event.event
            if (motion == null || !ExternalController.isGameController(motion.device)) {
                false
            } else {
                if (motion.actionMasked == MotionEvent.ACTION_MOVE) {
                    listenerBridge.onStickChanged(
                        GameFoldStick.LEFT,
                        motion.getAxisValue(MotionEvent.AXIS_X),
                        motion.getAxisValue(MotionEvent.AXIS_Y),
                    )
                }
                true
            }
        }
    }
    DisposableEffect(Unit) {
        PluviaApp.events.on<AndroidEvent.KeyEvent, Boolean>(onPhysicalKeyEvent)
        PluviaApp.events.on<AndroidEvent.MotionEvent, Boolean>(onPhysicalMotionEvent)
        onDispose {
            PluviaApp.events.off<AndroidEvent.KeyEvent, Boolean>(onPhysicalKeyEvent)
            PluviaApp.events.off<AndroidEvent.MotionEvent, Boolean>(onPhysicalMotionEvent)
            listenerBridge.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            GameFoldShellLayout(context).apply {
                setControllerInputListener(listenerBridge)
                val composeView = ComposeView(context).apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                    )
                    setContent {
                        val overlayController = remember { GameFoldPcShellOverlayController() }
                        val overlayRegistry = LocalGameFoldPcShellOverlayRegistry.current
                        val hostOwner = remember { Any() }
                        DisposableEffect(overlayRegistry, overlayController, hostOwner) {
                            overlayRegistry.attach(hostOwner, overlayController)
                            onDispose { overlayRegistry.detach(hostOwner) }
                        }
                        CompositionLocalProvider(
                            LocalGameFoldPcShellOverlay provides overlayController,
                            LocalGameFoldPcControllerBackRegistry provides controllerBackRegistry,
                        ) {
                            Box(Modifier.fillMaxSize()) {
                                currentContent.value()
                                overlayController.content?.let { overlay ->
                                    Box(Modifier.fillMaxSize().zIndex(20f)) {
                                        overlay.customContent?.invoke()
                                            ?: GameFoldRetroLoadingCard(
                                                text = overlay.text,
                                                progress = overlay.progress,
                                                heroImageUrl = overlay.artworkUrl,
                                                title = overlay.title,
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
                composeViewHolder[0] = composeView
                addContentView(composeView)
                // Give DPAD/A events a focused Compose root on cold entry. Individual NS cards
                // still install their own FocusRequester, but this guarantees the first physical
                // controller press reaches Compose during the initial composition frame.
                composeView.post { composeView.requestFocus() }
            }
        },
        update = { shell ->
            // Keep the bridge stable across recompositions while allowing the caller to replace
            // its semantic listener. The nested ComposeView is measured by GameFoldShellLayout.
            shell.setControllerInputListener(listenerBridge)
        },
        onRelease = { shell ->
            shell.releaseControllerInputs()
            listenerBridge.release()
            (shell.getChildAt(0) as? ComposeView)?.disposeComposition()
            shell.removeAllViews()
        },
    )
}

internal fun routeGameFoldControllerButton(
    button: GameFoldButton,
    pressed: Boolean,
    backRegistry: GameFoldPcControllerBackRegistry,
    dispatchToPage: () -> Unit,
) {
    if ((button == GameFoldButton.B || button == GameFoldButton.Y) && backRegistry.dispatch(pressed)) {
        return
    }
    dispatchToPage()
}

private fun GameFoldButton.toComposeKeyCode(): Int? = when (this) {
    GameFoldButton.DPAD_UP -> KeyEvent.KEYCODE_DPAD_UP
    GameFoldButton.DPAD_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
    GameFoldButton.DPAD_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
    GameFoldButton.DPAD_LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
    // A and X are both the NS confirm action.  B/Y are intentionally handled by the
    // screen-level semantic listener so a Back/Cancel action cannot leak into a focused
    // text field or gamepad control.
    GameFoldButton.A,
    GameFoldButton.X -> KeyEvent.KEYCODE_DPAD_CENTER
    // The NS tab rail uses the shoulder buttons to move between per-game settings tabs.
    // Forward the same Android key codes into the focused Compose tree when a form screen opts
    // into the shared navigation bridge.
    GameFoldButton.L -> KeyEvent.KEYCODE_BUTTON_L1
    GameFoldButton.R -> KeyEvent.KEYCODE_BUTTON_R1
    GameFoldButton.ZL -> KeyEvent.KEYCODE_BUTTON_L2
    GameFoldButton.ZR -> KeyEvent.KEYCODE_BUTTON_R2
    else -> null
}

private fun GameFoldButton.isDirectional(): Boolean = when (this) {
    GameFoldButton.DPAD_UP,
    GameFoldButton.DPAD_RIGHT,
    GameFoldButton.DPAD_DOWN,
    GameFoldButton.DPAD_LEFT,
    -> true
    else -> false
}

private fun GameFoldButton.isShoulder(): Boolean = when (this) {
    GameFoldButton.L,
    GameFoldButton.R,
    GameFoldButton.ZL,
    GameFoldButton.ZR,
    -> true
    else -> false
}

private fun KeyEvent.toGameFoldButton(): GameFoldButton? = when (keyCode) {
    KeyEvent.KEYCODE_DPAD_UP -> GameFoldButton.DPAD_UP
    KeyEvent.KEYCODE_DPAD_RIGHT -> GameFoldButton.DPAD_RIGHT
    KeyEvent.KEYCODE_DPAD_DOWN -> GameFoldButton.DPAD_DOWN
    KeyEvent.KEYCODE_DPAD_LEFT -> GameFoldButton.DPAD_LEFT
    KeyEvent.KEYCODE_BUTTON_A -> GameFoldButton.A
    KeyEvent.KEYCODE_BUTTON_B -> GameFoldButton.B
    KeyEvent.KEYCODE_BUTTON_X -> GameFoldButton.X
    KeyEvent.KEYCODE_BUTTON_Y -> GameFoldButton.Y
    KeyEvent.KEYCODE_BUTTON_SELECT -> GameFoldButton.MINUS
    KeyEvent.KEYCODE_BUTTON_START -> GameFoldButton.PLUS
    KeyEvent.KEYCODE_BUTTON_L1 -> GameFoldButton.L
    KeyEvent.KEYCODE_BUTTON_R1 -> GameFoldButton.R
    KeyEvent.KEYCODE_BUTTON_L2 -> GameFoldButton.ZL
    KeyEvent.KEYCODE_BUTTON_R2 -> GameFoldButton.ZR
    KeyEvent.KEYCODE_BUTTON_THUMBL -> GameFoldButton.L3
    KeyEvent.KEYCODE_BUTTON_THUMBR -> GameFoldButton.R3
    KeyEvent.KEYCODE_BUTTON_MODE -> GameFoldButton.HOME
    else -> null
}

private fun stickDirection(
    x: Float,
    y: Float,
    previous: GameFoldButton?,
): GameFoldButton? {
    val releaseThreshold = if (previous == null) STICK_ENGAGE_THRESHOLD else STICK_RELEASE_THRESHOLD
    if (abs(x) < releaseThreshold && abs(y) < releaseThreshold) return null
    if (abs(x) >= abs(y)) {
        if (abs(x) < STICK_ENGAGE_THRESHOLD && previous == null) return null
        return if (x >= 0f) GameFoldButton.DPAD_RIGHT else GameFoldButton.DPAD_LEFT
    }
    if (abs(y) < STICK_ENGAGE_THRESHOLD && previous == null) return null
    return if (y >= 0f) GameFoldButton.DPAD_DOWN else GameFoldButton.DPAD_UP
}

private const val STICK_INITIAL_REPEAT_DELAY_MS = 350L
private const val STICK_REPEAT_INTERVAL_MS = 140L
private const val STICK_ENGAGE_THRESHOLD = 0.55f
private const val STICK_RELEASE_THRESHOLD = 0.35f
