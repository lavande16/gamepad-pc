package app.gamenative.gamepad

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import app.gamenative.gamefold.rear.GameFoldRearTouchActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GamePadDisabledFeatureTest {
    @Test
    fun productContract_disablesEveryFoldAndRearFeature() {
        assertFalse(GamePadProductContract.REAR_DISPLAY_ENABLED)
        assertFalse(GamePadProductContract.REAR_TOUCH_ENABLED)
        assertFalse(GamePadProductContract.FRONT_EDGE_LIGHT_ENABLED)
        assertFalse(GamePadProductContract.PORTRAIT_ENABLED)
    }

    @Test
    fun packagedManifest_hasNoRearTouchActivityEntryPoint() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertThrows(PackageManager.NameNotFoundException::class.java) {
            context.packageManager.getActivityInfo(
                ComponentName(context, GameFoldRearTouchActivity::class.java),
                0,
            )
        }
    }

    @Test
    fun ownedPages_forceImmersiveModeAndHideTheStatusBarPreference() {
        val activity = sourceFile("app/src/main/java/app/gamenative/MainActivity.kt").readText()
        val settings = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/settings/SettingsGroupInterface.kt",
        ).readText()
        assertTrue(
            activity.contains(
                "desiredSystemUiVisible = !BuildConfig.GAMEFOLD_PC_LOCAL_SHELL && it.visible",
            ),
        )
        assertTrue(
            activity.contains(
                "window.decorView.systemUiVisibility = immersiveSystemUiFlags()",
            ),
        )
        assertTrue(settings.contains("if (!BuildConfig.GAMEFOLD_PC_LOCAL_SHELL)"))
    }

    @Test
    fun ownedPageWording_usesOnlyGamePadIdentityAndResourceRoot() {
        val library = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt",
        ).readText()
        val provision = sourceFile(
            "app/src/main/java/app/gamenative/ui/runtime/GameFoldPcRuntimeProvisionGate.kt",
        ).readText()
        assertFalse(library.contains("Game Fold PC"))
        assertFalse(library.contains("GAMEFOLD PC"))
        assertFalse(library.contains("共享存储中的 gamefold 文件夹"))
        assertFalse(provision.contains("共享存储中的 gamefold 文件夹"))
        assertFalse(provision.contains("/sdcard/gamefold"))
        assertTrue(library.contains("GamePad PC"))
        assertTrue(provision.contains("GameFoldPcResourceRoot.root().path"))
    }

    @Test
    fun malformedRuntimeSource_usesExplicitLaunchErrorFlowWithoutRequireNotNull() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt",
        ).readText()
        assertTrue(source.contains("GamePadViewportPolicy.validateRuntimeSource(container.screenSize)"))
        assertTrue(source.contains("onGameLaunchError?.invoke(validation.message)"))
        assertFalse(source.contains("requireNotNull(\n                    GamePadViewportPolicy.parseSourceSize"))
    }

    @Test
    fun controllerAndHomeOverlay_areMountedOnlyInsideTheGameSurface() {
        val nonGameHost = sourceFile(
            "app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcShellHost.kt",
        ).readText()
        val gameSurface = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt",
        ).readText()
        val loadingDialog = sourceFile(
            "app/src/main/java/app/gamenative/ui/component/dialog/LoadingDialog.kt",
        ).readText()

        assertTrue(nonGameHost.contains("GamePadProductContract.NON_GAME_CONTROLLER_OVERLAY_ENABLED"))
        assertTrue(gameSurface.contains("GamePadProductContract.GAME_CONTROLLER_OVERLAY_ENABLED"))
        assertTrue(loadingDialog.contains("GamePadProductContract.NON_GAME_CONTROLLER_OVERLAY_ENABLED"))
    }

    private fun sourceFile(path: String): java.io.File {
        var current = java.io.File(System.getProperty("user.dir")).canonicalFile
        repeat(8) {
            java.io.File(current, path).takeIf(java.io.File::isFile)?.let { return it }
            current = current.parentFile ?: return@repeat
        }
        error("Unable to locate $path")
    }
}
