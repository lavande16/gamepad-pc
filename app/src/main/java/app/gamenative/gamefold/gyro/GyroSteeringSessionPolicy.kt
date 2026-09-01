package app.gamenative.gamefold.gyro

import app.gamenative.product.GameFoldPcGameIdentity

internal object GyroSteeringSessionPolicy {
    fun shouldEnable(
        localShell: Boolean,
        executablePath: String,
        userEnabled: Boolean = true,
    ): Boolean = localShell &&
        userEnabled &&
        GameFoldPcGameIdentity.isNfsHotPursuitExecutable(executablePath)
}
