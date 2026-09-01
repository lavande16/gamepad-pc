package app.gamenative.product

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import app.gamenative.BuildConfig
import app.gamenative.R
import app.gamenative.gamepad.GamePadProductContract
import app.gamenative.utils.GameFoldPcResourceRoot
import com.winlator.container.Container
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GamePadProductContractTest {
    @Test
    fun legacyRelease_exposesIndependentGamePadIdentityAndStorage() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        assertEquals("com.gamepad.pc", BuildConfig.APPLICATION_ID)
        assertEquals("2.4", BuildConfig.VERSION_NAME)
        assertEquals(10033, BuildConfig.VERSION_CODE)
        assertEquals("GamePad PC", context.getString(R.string.app_name))
        assertEquals(
            File(Environment.getExternalStorageDirectory(), "GamePadPC").canonicalFile,
            GameFoldPcResourceRoot.root().canonicalFile,
        )
        assertTrue(BuildConfig.GAMEFOLD_PC_RUN_AS_ENABLED)
        assertEquals(BuildConfig.APPLICATION_ID, GamePadProductContract.APPLICATION_ID)
        assertEquals(BuildConfig.VERSION_NAME, GamePadProductContract.VERSION_NAME)
        assertEquals(BuildConfig.VERSION_CODE, GamePadProductContract.VERSION_CODE)
        assertEquals("GamePad PC", GamePadProductContract.APPLICATION_LABEL)
        assertEquals(GameFoldPcResourceRoot.ROOT_NAME, GamePadProductContract.PUBLIC_ROOT_NAME)
        assertTrue(GamePadProductContract.RUN_AS_ENABLED)
        assertFalse(GamePadProductContract.NON_GAME_CONTROLLER_OVERLAY_ENABLED)
        assertEquals(true, GamePadProductContract.GAME_CONTROLLER_OVERLAY_ENABLED)
        val requestedPermissions = context.packageManager
            .getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            ?.toSet()
            .orEmpty()
        assertTrue(Manifest.permission.WRITE_SECURE_SETTINGS in requestedPermissions)
    }

    @Test
    fun containerDefaults_useOnlyTheGamePadPrivateDirectory() {
        assertEquals(true, Container.DEFAULT_DRIVES.contains("/data/data/com.gamepad.pc/storage"))
        assertFalse(Container.DEFAULT_DRIVES.contains("/data/data/com.gamefold.pc/storage"))
        Container.MEDIACONV_ENV_VARS.forEach { value ->
            assertEquals(true, value.contains("/data/data/com.gamepad.pc/"))
            assertFalse(value.contains("/data/data/com.gamefold.pc/"))
        }
    }
}
