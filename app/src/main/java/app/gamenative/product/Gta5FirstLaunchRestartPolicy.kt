package app.gamenative.product

object Gta5FirstLaunchRestartPolicy {
    fun shouldRestartAfterPreInstall(
        localShell: Boolean,
        customGame: Boolean,
        executablePath: String,
        preInstallStepCount: Int,
    ): Boolean = localShell &&
        customGame &&
        preInstallStepCount > 0 &&
        GameFoldPcGameIdentity.isGtaVExecutable(executablePath)
}
