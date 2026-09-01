package app.gamenative.utils

import java.io.File

/** Resolves an optional local LSFG DLL before the legacy Steam candidate. */
object LsfgDllLocator {
    @JvmStatic
    fun resolve(localOverridePath: String, steamCandidate: File?): File? {
        val localCandidate = localOverridePath.trim()
            .takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?.takeIf { it.isFile && it.length() > 0L }
        return localCandidate ?: steamCandidate?.takeIf { it.isFile && it.length() > 0L }
    }
}
