package app.gamenative.product

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldPcProcessorPolicyTest {
    @Test
    fun arm64IsAcceptedBeforeStoragePermission() {
        assertTrue(GameFoldPcProcessorPolicy.isSupported(arrayOf("armeabi-v7a", "arm64-v8a")))
    }

    @Test
    fun nonArm64IsRejected() {
        assertFalse(GameFoldPcProcessorPolicy.isSupported(arrayOf("x86_64", "x86")))
    }

    @Test
    fun mediatekAliasesAreDetected() {
        assertTrue(GameFoldPcProcessorPolicy.isMediaTek("MediaTek Dimensity 9300", "mt6989"))
        assertTrue(GameFoldPcProcessorPolicy.isMediaTek("Dimensity 9400", "unknown"))
        assertTrue(GameFoldPcProcessorPolicy.isMediaTek("", "mtk"))
        assertTrue(GameFoldPcProcessorPolicy.isMediaTek("MT6989", ""))
    }

    @Test
    fun qualcommIdentifiersAreNotMisclassifiedAsMediaTek() {
        assertFalse(GameFoldPcProcessorPolicy.isMediaTek("SM8650", "qcom"))
        assertFalse(GameFoldPcProcessorPolicy.isMediaTek("Snapdragon 8 Gen 3", "kalama"))
    }

    @Test
    fun arm64MediaTekIsRejected() {
        assertFalse(
            GameFoldPcProcessorPolicy.isSupported(
                supportedAbis = arrayOf("arm64-v8a"),
                socModel = "MediaTek Dimensity 9300",
                hardware = "mt6989",
            ),
        )
    }

    @Test
    fun arm64QualcommRemainsSupported() {
        assertTrue(
            GameFoldPcProcessorPolicy.isSupported(
                supportedAbis = arrayOf("arm64-v8a", "armeabi-v7a"),
                socModel = "SM8650",
                hardware = "qcom",
            ),
        )
    }
}
