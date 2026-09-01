package app.gamenative

import android.content.Context
import android.system.Os
import android.os.StrictMode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import app.gamenative.events.EventDispatcher
import app.gamenative.service.ActiveGameRegistry
import app.gamenative.service.DownloadService
import app.gamenative.utils.ContainerMigrator
import app.gamenative.utils.IntentLaunchManager
import app.gamenative.gamefold.rear.FrontEdgeLightController
import app.gamenative.gamefold.rear.VivoRearDisplayController
import app.gamenative.gamepad.GamePadProductContract
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.winlator.container.Container
import com.winlator.inputcontrols.InputControlsManager
import com.winlator.widget.InputControlsView
import com.winlator.widget.TouchpadView
import com.winlator.widget.XServerRendererView
import com.winlator.xenvironment.XEnvironment
import com.winlator.xenvironment.components.XServerComponent
import timber.log.Timber
import dagger.hilt.android.HiltAndroidApp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

typealias NavChangedListener = NavController.OnDestinationChangedListener

@HiltAndroidApp
class PluviaApp : SplitCompatApplication() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // The official v1.1.1 evshim binary falls back to app.gamenative's
        // private path. Bind it to this co-installable package before any
        // class can load libevshim.so.
        Os.setenv("EVSHIM_BASE_PATH", filesDir.absolutePath, true)
    }

    override fun onCreate() {
        super.onCreate()

        preloadSystemLibraries()

        // Allows to find resource streams not closed within GameNative and JavaSteam
        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build(),
            )

            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        // Init our custom crash handler.
        CrashHandler.initialize(this)

        // Init our datastore preferences.
        PrefManager.init(this)

        if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL &&
            GamePadProductContract.REAR_DISPLAY_ENABLED
        ) {
            // The controller is opt-in and inert on non-vivo devices. Installing it at process
            // start is required so a rear session can survive the front Activity's pause while
            // the vendor launches the dedicated display Activity.
            FrontEdgeLightController.install(this)
            VivoRearDisplayController.install(this)
        }

        DownloadService.populateDownloadService(this)

        appScope.launch {
            ContainerMigrator.migrateLegacyContainersIfNeeded(
                context = applicationContext,
                onProgressUpdate = null,
                onComplete = null
            )
        }

        // Clear any stale temporary config overrides from previous app sessions
        try {
            IntentLaunchManager.clearAllTemporaryOverrides()
            Timber.d("[PluviaApp]: Cleared temporary config overrides from previous session")
        } catch (e: Exception) {
            Timber.e(e, "[PluviaApp]: Failed to clear temporary config overrides")
        }

    }

    companion object {
        @JvmField
        val events: EventDispatcher = EventDispatcher()
        internal var onDestinationChangedListener: NavChangedListener? = null

        // TODO: find a way to make this saveable, this is terrible (leak that memory baby)
        internal var xEnvironment: XEnvironment? = null
        internal var xServerView: XServerRendererView? = null
        /** App id owning the one preserved PC runtime session, if any. */
        @Volatile
        var activeGameAppId: String? = null
        /** True when Home returned to the launcher without tearing down the game. */
        @Volatile
        var gamePausedAtHome: Boolean = false
        var inputControlsView: InputControlsView? = null
        var inputControlsManager: InputControlsManager? = null
        var touchpadView: TouchpadView? = null
        var achievementWatcher: app.gamenative.service.AchievementWatcher? = null

        var isOverlayPaused by mutableStateOf(false)
        @Volatile
        var isActivityInForeground: Boolean = true

        // Active runtime suspend policy for the current in-game session.
        var activeSuspendPolicy: String = Container.SUSPEND_POLICY_MANUAL
            private set
        private var hasInitializedSuspendPolicyState: Boolean = false

        fun setActiveSuspendPolicy(policy: String) {
            activeSuspendPolicy = Container.normalizeSuspendPolicy(policy)
            hasInitializedSuspendPolicyState = true
        }

        /**
         * full environment teardown — shared by XServerScreen.exit() and
         * MainActivity.onDestroy fallback so both paths clean up identically
         */
        fun shutdownEnvironment() {
            val env = xEnvironment
            Timber.i("shutdownEnvironment: env=%s", env != null)

            // per-step catch so one failing teardown doesn't prevent the rest from running
            runCatching { achievementWatcher?.stop() }
                .onFailure { Timber.e(it, "shutdownEnvironment: achievementWatcher.stop") }
            runCatching { touchpadView?.releasePointerCapture() }
                .onFailure { Timber.e(it, "shutdownEnvironment: releasePointerCapture") }
            runCatching { env?.stopEnvironmentComponents() }
                .onFailure { Timber.e(it, "shutdownEnvironment: stopEnvironmentComponents") }

            xEnvironment = null
            activeGameAppId = null
            gamePausedAtHome = false
            inputControlsView = null
            inputControlsManager = null
            touchpadView = null
            achievementWatcher = null
            ActiveGameRegistry.clear()
            clearActiveSuspendState()
        }

        fun clearActiveSuspendState() {
            activeSuspendPolicy = Container.SUSPEND_POLICY_MANUAL
            isOverlayPaused = false
            hasInitializedSuspendPolicyState = false
        }

        fun hasValidSuspendPolicyState(): Boolean = hasInitializedSuspendPolicyState

        fun isNeverSuspendMode(): Boolean = activeSuspendPolicy.equals(Container.SUSPEND_POLICY_NEVER, ignoreCase = true)

        fun isManualSuspendMode(): Boolean = activeSuspendPolicy.equals(Container.SUSPEND_POLICY_MANUAL, ignoreCase = true)

        /**
         * Home preservation is valid only while the environment still owns the XServer and its
         * WinHandler. A non-null environment alone can be a stale half-teardown after a guest
         * crash or a renderer route disposal, so callers must cold-start when this check fails.
         */
        fun hasResumableGameEnvironment(): Boolean {
            val environment = xEnvironment ?: return false
            val xServer = environment.getComponent(XServerComponent::class.java)?.xServer ?: return false
            return xServer.winHandler != null
        }

    }

    /**
     * Some native libraries we dlopen at runtime (libsteamclient.so via SteamBootstrap,
     * the lsfg-vk layer, etc.) depend on `libjpeg.so`, which isn't on every device's
     * dynamic linker search path. Pre-load the system copy here with RTLD_GLOBAL
     * semantics (System.load is global) so all subsequent dlopens find its symbols.
     *
     * Single place for all: runs once in Application.onCreate before any other
     * native lib is loaded by this process. Failures are non-fatal — devices that
     * don't have the file (or have it elsewhere) just fall through.
     */
    private fun preloadSystemLibraries() {
        val is64 = android.os.Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
        val candidates = if (is64) {
            listOf("/system/lib64/libjpeg.so", "/system/lib/libjpeg.so")
        } else {
            listOf("/system/lib/libjpeg.so", "/system/lib64/libjpeg.so")
        }
        for (path in candidates) {
            if (!File(path).exists()) continue
            try {
                System.load(path)
                Timber.i("[PluviaApp]: Preloaded $path")
                return
            } catch (e: Throwable) {
                Timber.w(e, "[PluviaApp]: System.load($path) failed")
            }
        }
        Timber.w("[PluviaApp]: Could not preload system libjpeg.so (none of the candidate paths worked)")
    }
}
