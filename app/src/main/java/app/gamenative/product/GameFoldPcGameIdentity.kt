package app.gamenative.product

import java.io.File

object GameFoldPcGameIdentity {
    fun isNfsHotPursuitExecutable(path: String): Boolean {
        val name = File(path.replace('\\', '/')).name
        return name.equals("NFS11.exe", ignoreCase = true)
    }

    fun isGtaVExecutable(path: String): Boolean {
        val name = File(path.replace('\\', '/')).name
        return name.equals("GTAVLauncher.exe", ignoreCase = true) ||
            name.equals("GTA5.exe", ignoreCase = true)
    }

    fun isCyberpunk2077Executable(path: String): Boolean {
        val name = File(path.replace('\\', '/')).name
        return name.equals("Cyberpunk2077.exe", ignoreCase = true)
    }
}
