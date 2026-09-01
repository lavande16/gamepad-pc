package app.gamenative.product

enum class LocalRuntimeLaunchStep {
    BOOTSTRAP_IMAGE_FS,
    CREATE_CONTAINER,
}

object LocalRuntimeBootstrapPolicy {
    private const val BIONIC_VARIANT = "bionic"

    fun launchSteps(
        localShell: Boolean,
        imageFsValid: Boolean,
    ): List<LocalRuntimeLaunchStep> = buildList {
        if (localShell && !imageFsValid) {
            add(LocalRuntimeLaunchStep.BOOTSTRAP_IMAGE_FS)
        }
        add(LocalRuntimeLaunchStep.CREATE_CONTAINER)
    }

    fun shouldBootstrapImageFs(
        localShell: Boolean,
        imageFsValid: Boolean,
    ): Boolean = LocalRuntimeLaunchStep.BOOTSTRAP_IMAGE_FS in launchSteps(localShell, imageFsValid)

    fun bootstrapContainerVariant(localShell: Boolean): String? =
        if (localShell) BIONIC_VARIANT else null
}
