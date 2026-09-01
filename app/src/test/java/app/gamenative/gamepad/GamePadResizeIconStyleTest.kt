package app.gamenative.gamepad

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePadResizeIconStyleTest {
    @Test
    fun resizeGlyphIsWhiteAndFocusedIconUsesTheStableCenteringPolicy() {
        val icon = File("src/main/res/drawable/pixel_icon_resize.xml").readText()
        val source = File(
            "src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt",
        ).readText()

        assertTrue(icon.contains("android:fillColor=\"#FFFFFF\""))
        assertTrue(source.contains("tint = Color.White"))
        assertTrue(source.split("centerOnUnshadowedFace = true").size - 1 >= 2)
        assertTrue(source.contains("GameFoldPcFrontendPolicy.headerIconOffsetDp"))
        assertTrue(source.contains(".offset(x = iconOffsetDp.dp, y = iconOffsetDp.dp)"))
    }
}
