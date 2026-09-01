package app.gamenative.gamepad

import org.junit.Assert.assertEquals
import org.junit.Test

class GamePadAddGamePolicyTest {
    @Test
    fun downloadActionOpensTheApproved3dmGamePage() {
        var openedUrl = ""

        GamePadAddGamePolicy.openDownloadPage { openedUrl = it }

        assertEquals("https://dl.3dmgame.com/all_all_1_hot_cn/", openedUrl)
    }
}
