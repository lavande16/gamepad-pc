package app.gamenative.product

object Gta5SaveImportLaunchPolicy {
    const val NOTIFICATION_DURATION_MS = 3_000L

    enum class Notification {
        IMPORTED,
        PRESERVED_EXISTING,
        FAILED,
    }

    fun notificationFor(result: Gta5SaveAutoImporter.Result): Notification? = when (result) {
        is Gta5SaveAutoImporter.Result.Failed -> Notification.FAILED
        is Gta5SaveAutoImporter.Result.Imported -> Notification.IMPORTED
        is Gta5SaveAutoImporter.Result.PreservedExisting -> Notification.PRESERVED_EXISTING

        Gta5SaveAutoImporter.Result.AlreadyHandled,
        Gta5SaveAutoImporter.Result.NoSource,
        Gta5SaveAutoImporter.Result.NotApplicable,
        -> null
    }
}
