package app.gamenative.product

object Cyberpunk2077SaveImportLaunchPolicy {
    const val NOTIFICATION_DURATION_MS = 3_000L

    enum class Notification {
        IMPORTED,
        PRESERVED_EXISTING,
        FAILED,
    }

    fun notificationFor(result: Cyberpunk2077SaveAutoImporter.Result): Notification? = when (result) {
        is Cyberpunk2077SaveAutoImporter.Result.Failed -> Notification.FAILED
        is Cyberpunk2077SaveAutoImporter.Result.Imported -> Notification.IMPORTED
        is Cyberpunk2077SaveAutoImporter.Result.PreservedExisting -> Notification.PRESERVED_EXISTING

        Cyberpunk2077SaveAutoImporter.Result.AlreadyHandled,
        Cyberpunk2077SaveAutoImporter.Result.NoSource,
        Cyberpunk2077SaveAutoImporter.Result.NotApplicable,
        -> null
    }
}
