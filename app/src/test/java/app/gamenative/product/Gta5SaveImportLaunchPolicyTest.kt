package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Gta5SaveImportLaunchPolicyTest {
    @Test
    fun failedImportContinuesWithTemporaryFailureNotification() {
        val notification = Gta5SaveImportLaunchPolicy.notificationFor(
            Gta5SaveAutoImporter.Result.Failed("disk full"),
        )

        assertEquals(Gta5SaveImportLaunchPolicy.Notification.FAILED, notification)
        assertEquals(3_000L, Gta5SaveImportLaunchPolicy.NOTIFICATION_DURATION_MS)
    }

    @Test
    fun completedImportContinuesWithImportedNotification() {
        val notification = Gta5SaveImportLaunchPolicy.notificationFor(
            Gta5SaveAutoImporter.Result.Imported(2),
        )

        assertEquals(Gta5SaveImportLaunchPolicy.Notification.IMPORTED, notification)
    }

    @Test
    fun preservedTargetContinuesWithPreservationNotification() {
        val notification = Gta5SaveImportLaunchPolicy.notificationFor(
            Gta5SaveAutoImporter.Result.PreservedExisting(1),
        )

        assertEquals(
            Gta5SaveImportLaunchPolicy.Notification.PRESERVED_EXISTING,
            notification,
        )
    }

    @Test
    fun noOpResultsContinueSilently() {
        val noOpResults = listOf(
            Gta5SaveAutoImporter.Result.NotApplicable,
            Gta5SaveAutoImporter.Result.NoSource,
            Gta5SaveAutoImporter.Result.AlreadyHandled,
        )

        noOpResults.forEach { result ->
            assertNull(Gta5SaveImportLaunchPolicy.notificationFor(result))
        }
    }
}
