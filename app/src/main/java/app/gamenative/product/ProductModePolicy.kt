package app.gamenative.product

data class ProductCapabilities(
    val localLibraryOnly: Boolean,
    val platformAccounts: Boolean,
    val onlineUpdates: Boolean,
    val analytics: Boolean,
    val onlineRuntimeDownloads: Boolean,
    val informationSection: Boolean,
)

object ProductModePolicy {
    fun capabilities(localShell: Boolean): ProductCapabilities =
        if (localShell) {
            ProductCapabilities(
                localLibraryOnly = true,
                platformAccounts = false,
                onlineUpdates = false,
                analytics = false,
                onlineRuntimeDownloads = false,
                informationSection = false,
            )
        } else {
            ProductCapabilities(
                localLibraryOnly = false,
                platformAccounts = true,
                onlineUpdates = true,
                analytics = true,
                onlineRuntimeDownloads = true,
                informationSection = true,
            )
        }
}
