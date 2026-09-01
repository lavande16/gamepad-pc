package app.gamenative.gamefold.gyro

import app.gamenative.product.GameFoldPcGameIdentity

internal object GyroAimSessionPolicy {
    fun shouldEnable(localShell: Boolean, executablePath: String): Boolean =
        localShell && GameFoldPcGameIdentity.isGtaVExecutable(executablePath)
}
