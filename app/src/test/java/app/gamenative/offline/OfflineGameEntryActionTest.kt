package app.gamenative.offline

import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineGameEntryActionTest {
    @Test
    fun tapLaunchesWhileLongPressOpensDetails() {
        assertEquals(OfflineGameEntryAction.Launch, offlineGameEntryAction(longPressed = false))
        assertEquals(OfflineGameEntryAction.Details, offlineGameEntryAction(longPressed = true))
    }
}
