package app.gamenative.gamepad

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePadSmallDefaultAssetTest {
    @Test
    fun sixSmallDefaultAssets_preserveTheApprovedTransparentBytesAndSize() {
        val approved = mapOf(
            "controller_button_zl_default.webp" to
                "54c087ad9dbc91323495f4b04b518f53fa408267cc9484ca600b08168591c1b4",
            "controller_button_l_default.webp" to
                "56014d32c71932b0c9ea985fb4af6fbb2329b4c6f2617fef63a372b5de5cf8fd",
            "controller_button_r_default.webp" to
                "33429aac92f7ac47a752f36b2488041bd37e9405fcad6c20a1067dd0c8056e7e",
            "controller_button_zr_default.webp" to
                "8e4b05922b0619f05df60587ab08c502f38d074817721230f0bb0eb3878e70be",
            "controller_button_plus_default.webp" to
                "295b939d68230d6595be2cf09c750db0ab9f9ca490a59962f3a2bbc4d0de4d45",
            "controller_button_minus_default.webp" to
                "869828aec338de0768dcbfa7ce7a55020218b2be5d446a62858e3e2be5268ef3",
        )

        approved.forEach { (name, expectedHash) ->
            val asset = sourceFile("app/src/main/res/drawable-nodpi/$name")
            assertTrue("$name must be committed", asset.isFile)
            assertEquals(expectedHash, sha256(asset))
            assertEquals(138 to 138, readVp8lDimensions(asset.readBytes()))
        }
    }

    private fun sha256(file: File): String = MessageDigest.getInstance("SHA-256")
        .digest(file.readBytes())
        .joinToString("") { byte -> "%02x".format(byte) }

    private fun sourceFile(relativePath: String): File {
        val userDirectory = System.getProperty("user.dir") ?: error("user.dir is unavailable")
        var cursor = File(userDirectory).absoluteFile
        repeat(8) {
            val candidate = File(cursor, relativePath)
            if (candidate.exists()) return candidate
            cursor = cursor.parentFile ?: return candidate
        }
        return File(cursor, relativePath)
    }

    private fun readVp8lDimensions(bytes: ByteArray): Pair<Int, Int> {
        assertTrue(bytes.size >= 25)
        assertEquals("RIFF", bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals("WEBP", bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        assertEquals("VP8L", bytes.copyOfRange(12, 16).toString(Charsets.US_ASCII))
        assertEquals(0x2f, bytes[20].toInt() and 0xff)
        val b0 = bytes[21].toInt() and 0xff
        val b1 = bytes[22].toInt() and 0xff
        val b2 = bytes[23].toInt() and 0xff
        val b3 = bytes[24].toInt() and 0xff
        val width = 1 + (b0 or ((b1 and 0x3f) shl 8))
        val height = 1 + ((b1 shr 6) or (b2 shl 2) or ((b3 and 0x0f) shl 10))
        return width to height
    }
}
