package app.gamenative.product

import com.winlator.container.Container
import java.io.File

object NfsHotPursuitResolutionConfig {
    private const val EXECUTABLE = "NFS11.exe"
    private const val CONFIG_PATH =
        ".wine/drive_c/users/xuser/Documents/Criterion Games/" +
            "Need for Speed(TM) Hot Pursuit/config.NFS11Save"
    private val SIZE_PATTERN = Regex("^(\\d+)x(\\d+)$")

    fun synchronize(container: Container, screenSize: String): Boolean {
        if (!File(container.executablePath).name.equals(EXECUTABLE, ignoreCase = true)) return false
        val match = SIZE_PATTERN.matchEntire(screenSize) ?: return false
        val width = match.groupValues[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val height = match.groupValues[2].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val root = container.rootDir ?: return false
        val config = File(root, CONFIG_PATH)
        if (!config.isFile) return false

        val original = config.readText()
        val newline = if (original.contains("\r\n")) "\r\n" else "\n"
        val hadTrailingNewline = original.endsWith("\n")
        var foundWidth = false
        var foundHeight = false
        val updated = original.lineSequence().map { line ->
            when {
                line.startsWith("Width=") -> {
                    foundWidth = true
                    "Width=$width"
                }
                line.startsWith("Height=") -> {
                    foundHeight = true
                    "Height=$height"
                }
                else -> line
            }
        }.toList().let { lines ->
            val body = lines.joinToString(newline)
            if (hadTrailingNewline && !body.endsWith(newline)) body + newline else body
        }
        if (!foundWidth || !foundHeight || updated == original) return false

        val temporary = File.createTempFile(config.name, ".tmp", config.parentFile)
        try {
            temporary.writeText(updated)
            if (!temporary.renameTo(config)) {
                temporary.copyTo(config, overwrite = true)
            }
        } finally {
            temporary.delete()
        }
        return true
    }
}
