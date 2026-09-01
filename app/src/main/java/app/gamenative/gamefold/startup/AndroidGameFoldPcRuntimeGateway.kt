package app.gamenative.gamefold.startup

import android.content.Context
import app.gamenative.product.GameFoldPcDefaults
import com.winlator.container.Container
import com.winlator.contents.ContentProfile
import com.winlator.contents.ContentsManager
import com.winlator.xenvironment.ImageFs
import com.winlator.xenvironment.ImageFsInstaller
import java.io.File

/** Android adapter for the PC runtime; the UI never receives ImageFs/Container objects. */
class AndroidGameFoldPcRuntimeGateway(
    private val context: Context,
) : PrivateRuntimeGateway {
    override fun inspect(): PrivateRuntimeStatus {
        val imageFs = ImageFs.find(context)
        val imageReady = imageFs.isValid() &&
            imageFs.version >= ImageFsInstaller.LATEST_VERSION &&
            imageFs.variant.equals(GameFoldPcDefaults.CONTAINER_VARIANT, ignoreCase = true)
        val protonReady = findProtonProfile() != null
        return PrivateRuntimeStatus(imageReady, protonReady)
    }

    override fun installSharedRuntime(onProgress: (Int) -> Unit): Boolean {
        val bootstrap = Container("GAMEFOLD_PC_BOOTSTRAP").apply {
            containerVariant = GameFoldPcDefaults.CONTAINER_VARIANT
            wineVersion = GameFoldPcDefaults.WINE_VERSION
        }
        return ImageFsInstaller.installSharedRuntimeIfNeededFuture(
            context,
            context.assets,
            bootstrap,
        ) { progress -> onProgress(progress) }.get()
    }

    override fun invalidateIncomplete() {
        // The installer owns its journal/marker cleanup. Do not delete user
        // home, imported Proton, or public GamePadPC payload from this adapter.
        ImageFsInstaller.invalidateIncompleteSharedRuntime(context)
    }

    private fun findProtonProfile(): ContentProfile? {
        val manager = ContentsManager(context)
        val profileName = GameFoldPcDefaults.WINE_VERSION
        val protonRoot = ImageFs.getSharedProtonDir(context)
        // ContentsManager normalizes a profile's versionName to a type-prefixed
        // directory and intentionally drops the version-code suffix. The
        // logical GameFold entry retains that suffix, so accept only these two
        // deterministic names (never an arbitrary walk of private storage).
        val installDirs = listOfNotNull(
            File(protonRoot, profileName),
            File(protonRoot, profileName.removeSuffix("-2")),
        ).distinctBy { it.absolutePath }
        val match = installDirs.asSequence().mapNotNull { installDir ->
            val profile = File(installDir, ContentsManager.PROFILE_NAME)
                .takeIf { it.isFile }
                ?.let(manager::readProfile)
            profile?.let { installDir to it }
        }.firstOrNull { (_, profile) ->
            profile.verName == profileName || profile.verName == profileName.removeSuffix("-2")
        } ?: return null
        val (installDir, profile) = match

        // The installer accepts both the historical Wine-shaped profile and the
        // Proton profile. Validate the installed payload at the exact directory
        // selected by the fixed GameFold entry, rather than resolving a second
        // directory from the parsed type (which can differ after normalization).
        val binPath = profile.wineBinPath?.takeIf { it.isNotBlank() } ?: "bin"
        val libPath = profile.wineLibPath?.takeIf { it.isNotBlank() } ?: "lib"
        val prefixPack = profile.winePrefixPack?.takeIf { it.isNotBlank() }
        val binReady = File(installDir, binPath).isDirectory
        val libReady = File(installDir, libPath).isDirectory
        val prefixReady = prefixPack == null || File(installDir, prefixPack).isFile
        return profile.takeIf { binReady && libReady && prefixReady }
    }
}
