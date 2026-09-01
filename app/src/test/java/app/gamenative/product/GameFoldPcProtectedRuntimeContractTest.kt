package app.gamenative.product

import android.os.Environment
import app.gamenative.data.GameSource
import app.gamenative.utils.ContainerStorageManager
import app.gamenative.utils.GameFoldPcResourceRoot
import app.gamenative.utils.GtaFullscreenConfig
import com.winlator.container.Container
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
class GameFoldPcProtectedRuntimeContractTest {
    @Test
    fun newCustomGameContainer_inheritsTheGlobalTemplateWithoutHardcodedResolution() {
        val container = Container("CUSTOM_GAME_314").apply {
            screenSize = "1920x1080"
        }

        assertTrue(GameFoldPcPerGameDefaults.applyTo(container))
        assertEquals("1920x1080", container.screenSize)
    }

    @Test
    fun gtaExecutablesReceiveFullscreenArgumentsButOtherGamesDoNot() {
        val gameDirectory = Files.createTempDirectory("gamefold-gta-contract-").toFile()
        try {
            val gta = File(gameDirectory, "GTAVLauncher.exe").apply { createNewFile() }
            val gtaExecutable = File(gameDirectory, "GTA5.exe").apply { createNewFile() }
            val otherGame = File(gameDirectory, "BlackMythPixel.exe").apply { createNewFile() }

            assertTrue(GtaFullscreenConfig.sync(gta, "854x480"))
            assertEquals(
                "-fullscreen\n-width 1067\n-height 600\n",
                File(gameDirectory, "commandline.txt").readText(),
            )
            assertTrue(GtaFullscreenConfig.sync(gtaExecutable, "800x600"))
            assertEquals(
                "-fullscreen\n-width 800\n-height 600\n",
                File(gameDirectory, "commandline.txt").readText(),
            )
            assertFalse(GtaFullscreenConfig.sync(otherGame, "854x480"))
        } finally {
            gameDirectory.deleteRecursively()
        }
    }

    @Test
    fun publicPayloadFile_resolvesFromTheIndependentGamePadRoot() {
        val publicRoot = File(Environment.getExternalStorageDirectory(), "GamePadPC")
        val payload = File(publicRoot, "protected-runtime-contract/proton-contract.wcp")
        try {
            assertTrue(payload.parentFile!!.mkdirs() || payload.parentFile!!.isDirectory)
            payload.writeText("public payload")

            assertEquals(publicRoot.canonicalFile, GameFoldPcResourceRoot.root().canonicalFile)
            assertEquals(payload.canonicalFile, GameFoldPcResourceRoot.resolve(
                "protected-runtime-contract/proton-contract.wcp",
            )?.canonicalFile)
        } finally {
            payload.delete()
            payload.parentFile?.delete()
        }
    }

    @Test
    fun customGameIdentity_requiresTheCustomPrefixAndNumericId() {
        assertEquals(GameSource.CUSTOM_GAME, ContainerStorageManager.detectGameSource("CUSTOM_GAME_314"))
        assertEquals(314, ContainerStorageManager.extractGameId("CUSTOM_GAME_314"))
        assertNull(ContainerStorageManager.extractGameId("CUSTOM_GAME_314_saved"))
    }
}
