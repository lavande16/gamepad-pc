package app.gamenative.utils

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocalGameArtworkResolverTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun resolve_prefersSquareCoverAndPngWithinSameBaseName() {
        val folder = temporaryFolder.newFolder("game")
        val squareJpg = File(folder, "cover_square.jpg").also { it.writeText("jpg") }
        File(folder, "cover_square.PNG").writeText("png")
        File(folder, "cover.png").writeText("generic")

        assertEquals(
            File(folder, "cover_square.PNG").absolutePath,
            LocalGameArtworkResolver.resolve(folder)?.absolutePath,
        )
        check(squareJpg.exists())
    }

    @Test
    fun resolve_usesSecondaryCoverNamesBeforeIconNames() {
        val folder = temporaryFolder.newFolder("game")
        val poster = File(folder, "poster.webp").also { it.writeText("poster") }
        File(folder, "icon.png").writeText("icon")

        assertEquals(poster.absolutePath, LocalGameArtworkResolver.resolve(folder)?.absolutePath)
    }

    @Test
    fun resolve_prefersCachedIconForConfiguredExecutableAfterNamedImages() {
        val folder = temporaryFolder.newFolder("game")
        val exe = File(folder, "Game.exe").also { it.writeText("not-a-real-pe") }
        val extracted = File(folder, "Game.extracted.ico").also { it.writeText("ico") }

        assertEquals(
            extracted.absolutePath,
            LocalGameArtworkResolver.resolve(folder, "Game.exe")?.absolutePath,
        )
        check(exe.exists())
    }

    @Test
    fun resolve_usesUniqueExecutableCachedIconWhenNoConfiguredPath() {
        val folder = temporaryFolder.newFolder("game")
        File(folder, "Game.exe").writeText("not-a-real-pe")
        val extracted = File(folder, "Game.extracted.ico").also { it.writeText("ico") }

        assertEquals(extracted.absolutePath, LocalGameArtworkResolver.resolve(folder)?.absolutePath)
    }

    @Test
    fun resolve_usesSingleSupportedImageInImmediateChildDirectory() {
        val folder = temporaryFolder.newFolder("game")
        val child = File(folder, "assets").also { it.mkdirs() }
        val image = File(child, "splash.WEBP").also { it.writeText("image") }

        assertEquals(image.absolutePath, LocalGameArtworkResolver.resolve(folder)?.absolutePath)
    }

    @Test
    fun resolve_returnsNullForAmbiguousNearbyImages() {
        val folder = temporaryFolder.newFolder("game")
        File(folder, "one.png").writeText("one")
        File(folder, "two.jpg").writeText("two")

        assertNull(LocalGameArtworkResolver.resolve(folder))
    }

    @Test
    fun resolve_returnsNullForMissingFolder() {
        val missing = File(temporaryFolder.root, "missing")

        assertNull(LocalGameArtworkResolver.resolve(missing))
    }

    @Test
    fun resolve_findsCachedIconForGameNamedExecutableAtBinX64Depth() {
        val folder = temporaryFolder.newFolder("Cyberpunk 2077")
        File(folder, "REDprelauncher.exe").writeText("launcher")
        val x64 = File(folder, "bin/x64").also { it.mkdirs() }
        File(x64, "Cyberpunk2077.exe").writeText("not-a-real-pe")
        val icon = File(x64, "Cyberpunk2077.extracted.ico").also { it.writeText("ico") }

        assertEquals(icon.absolutePath, LocalGameArtworkResolver.resolve(folder)?.absolutePath)
    }

    @Test
    fun resolve_doesNotWalkBeyondBoundedGameDepth() {
        val folder = temporaryFolder.newFolder("Game")
        val tooDeep = File(folder, "a/b/c/d").also { it.mkdirs() }
        File(tooDeep, "Game.exe").writeText("exe")
        File(tooDeep, "Game.extracted.ico").writeText("ico")

        assertNull(LocalGameArtworkResolver.resolve(folder))
    }
}
