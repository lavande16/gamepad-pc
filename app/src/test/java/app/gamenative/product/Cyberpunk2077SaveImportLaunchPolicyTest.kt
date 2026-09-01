package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Cyberpunk2077SaveImportLaunchPolicyTest {
    @Test
    fun failedImportContinuesWithTemporaryFailureNotification() {
        val notification = Cyberpunk2077SaveImportLaunchPolicy.notificationFor(
            Cyberpunk2077SaveAutoImporter.Result.Failed("disk full"),
        )

        assertEquals(Cyberpunk2077SaveImportLaunchPolicy.Notification.FAILED, notification)
        assertEquals(3_000L, Cyberpunk2077SaveImportLaunchPolicy.NOTIFICATION_DURATION_MS)
    }

    @Test
    fun completedImportContinuesWithImportedNotification() {
        val notification = Cyberpunk2077SaveImportLaunchPolicy.notificationFor(
            Cyberpunk2077SaveAutoImporter.Result.Imported(4),
        )

        assertEquals(Cyberpunk2077SaveImportLaunchPolicy.Notification.IMPORTED, notification)
    }

    @Test
    fun preservedTargetContinuesWithPreservationNotification() {
        val notification = Cyberpunk2077SaveImportLaunchPolicy.notificationFor(
            Cyberpunk2077SaveAutoImporter.Result.PreservedExisting(1),
        )

        assertEquals(
            Cyberpunk2077SaveImportLaunchPolicy.Notification.PRESERVED_EXISTING,
            notification,
        )
    }

    @Test
    fun noOpResultsContinueSilently() {
        listOf(
            Cyberpunk2077SaveAutoImporter.Result.NotApplicable,
            Cyberpunk2077SaveAutoImporter.Result.NoSource,
            Cyberpunk2077SaveAutoImporter.Result.AlreadyHandled,
        ).forEach { result ->
            assertNull(Cyberpunk2077SaveImportLaunchPolicy.notificationFor(result))
        }
    }
}
