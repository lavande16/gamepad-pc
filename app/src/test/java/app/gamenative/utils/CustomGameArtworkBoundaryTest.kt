package app.gamenative.utils

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CustomGameArtworkBoundaryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun findLocalArtworkInFolder_returnsOnlyLocalFileUri() {
        val folder = temporaryFolder.newFolder("game")
        val cover = File(folder, "cover_square.png").also { it.writeText("cover") }

        assertEquals(
            cover.toURI().toString(),
            CustomGameScanner.findLocalArtworkInFolder(folder),
        )
    }
}
