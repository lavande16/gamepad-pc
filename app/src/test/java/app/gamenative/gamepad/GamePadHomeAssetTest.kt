package app.gamenative.gamepad

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePadHomeAssetTest {
    @Test
    fun committedHomeAsset_preservesApprovedBytesAndIntrinsicSize() {
        val asset = sourceFile("app/src/main/res/drawable-nodpi/gamepad_home_logo.webp")
        assertTrue("GAMEPAD HOME asset must be committed", asset.isFile)

        val hash = MessageDigest.getInstance("SHA-256")
            .digest(asset.readBytes())
            .joinToString("") { byte -> "%02x".format(byte) }
        assertEquals(
            "0c7ebdb2a11bcfcac39f4f142c1b67ac22854cf4fa387bf34e151d033aaf0593",
            hash,
        )

        val (width, height) = readVp8lDimensions(asset.readBytes())
        assertEquals(270, width)
        assertEquals(77, height)
    }

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
