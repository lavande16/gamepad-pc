package app.gamenative.utils

import app.gamenative.product.GameFoldPcDefaults
import app.gamenative.product.GameFoldPcGameIdentity
import timber.log.Timber
import java.io.File
import java.util.Locale

/** Keeps GTA V's render surface aligned with its GamePad PC virtual desktop. */
object GtaFullscreenConfig {
    private const val SAFE_DEFAULT_SCREEN_SIZE = "1067x600"
    private const val MINIMUM_WIDTH = 800
    private const val MINIMUM_HEIGHT = 600
    private val screenSizePattern = Regex("^(\\d+)x(\\d+)$")
    private val managedArgumentPattern = Regex(
        pattern = "^-(?:fullscreen|windowed|borderless|width|height)(?:\\s|=|$)",
        option = RegexOption.IGNORE_CASE,
    )

    fun sync(executable: File, screenSize: String): Boolean {
        if (!GameFoldPcGameIdentity.isGtaVExecutable(executable.path)) return false

        val compatibleScreenSize = compatibleScreenSize(executable, screenSize)
        val match = screenSizePattern.matchEntire(compatibleScreenSize.trim().lowercase(Locale.ROOT))
        val width = match?.groupValues?.get(1)?.toIntOrNull()
        val height = match?.groupValues?.get(2)?.toIntOrNull()
        if (width == null || height == null || width <= 0 || height <= 0) {
            Timber.tag("GtaFullscreenConfig").w(
                "Skipped GTA fullscreen sync because screen size is invalid: %s",
                screenSize,
            )
            return false
        }

        val gameDirectory = executable.parentFile ?: return false
        val commandLineFile = File(gameDirectory, "commandline.txt")
        return runCatching {
            val preservedArguments = if (commandLineFile.isFile) {
                commandLineFile.readLines()
                    .filterNot { managedArgumentPattern.containsMatchIn(it.trim()) }
                    .filter { it.isNotBlank() }
            } else {
                emptyList()
            }
            val synchronizedArguments = preservedArguments + listOf(
                "-fullscreen",
                "-width $width",
                "-height $height",
            )
            commandLineFile.writeText(synchronizedArguments.joinToString("\n", postfix = "\n"))
            Timber.tag("GtaFullscreenConfig").i(
                "Synchronized GTA fullscreen resolution to %sx%s at %s",
                width,
                height,
                commandLineFile.absolutePath,
            )
            true
        }.getOrElse { error ->
            Timber.tag("GtaFullscreenConfig").w(
                error,
                "Could not synchronize GTA fullscreen resolution at %s",
                commandLineFile.absolutePath,
            )
            false
        }
    }

    /**
     * GTA V creates a surface of at least 800x600 even when a smaller virtual
     * desktop is requested. Keeping both sides on the same supported size
     * avoids the stretched, off-centre output caused by mismatched heights.
     */
    fun compatibleScreenSize(executable: File, requestedScreenSize: String): String {
        if (!GameFoldPcGameIdentity.isGtaVExecutable(executable.path)) return requestedScreenSize

        val match = screenSizePattern.matchEntire(
            requestedScreenSize.trim().lowercase(Locale.ROOT),
        ) ?: return requestedScreenSize
        val width = match.groupValues[1].toIntOrNull() ?: return requestedScreenSize
        val height = match.groupValues[2].toIntOrNull() ?: return requestedScreenSize
        if (width <= 0 || height <= 0) return requestedScreenSize

        return if (width < MINIMUM_WIDTH || height < MINIMUM_HEIGHT) {
            SAFE_DEFAULT_SCREEN_SIZE
        } else {
            "${width}x${height}"
        }
    }
}
