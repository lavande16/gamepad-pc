package app.gamenative.gamefold.gyro

import app.gamenative.product.GameFoldPcGameIdentity
import com.winlator.container.Container

internal object GyroSteeringContainerSettings {
    const val EXTRA_ENABLED = "gamePadGyroSteeringEnabled"

    fun isAvailable(executablePath: String): Boolean =
        GameFoldPcGameIdentity.isNfsHotPursuitExecutable(executablePath)

    fun decodeEnabled(storedValue: String?): Boolean =
        storedValue?.toBooleanStrictOrNull() ?: true

    fun isEnabled(container: Container): Boolean =
        decodeEnabled(container.getExtra(EXTRA_ENABLED, ""))

    fun write(container: Container, enabled: Boolean) {
        container.putExtra(EXTRA_ENABLED, enabled.toString())
    }
}
