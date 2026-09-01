package app.gamenative.gamefold.startup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class GameFoldPcRuntimeProvisionStoreTest {
    @Test
    fun writeThenRead_roundTripsTheReceipt() {
        withTempFile { file ->
            val store = GameFoldPcRuntimeProvisionStore(file)
            val receipt = RuntimeProvisionReceipt(manifestSha256 = "abc")

            store.write(receipt)

            assertEquals(receipt, store.read())
        }
    }

    @Test
    fun truncatedOrMismatchedReceipt_isAbsent() {
        withTempFile { file ->
            val store = GameFoldPcRuntimeProvisionStore(file)
            file.parentFile!!.mkdirs()
            file.writeText("{\"schema\":999}")
            assertNull(store.read())

            file.writeText("not-json")
            assertNull(store.read())
        }
    }

    @Test
    fun clear_removesReceiptWithoutTouchingSiblingData() {
        withTempFile { file ->
            val sibling = File(file.parentFile, "keep.me").apply {
                parentFile!!.mkdirs()
                writeText("keep")
            }
            val store = GameFoldPcRuntimeProvisionStore(file)
            store.write(RuntimeProvisionReceipt(manifestSha256 = "abc"))
            store.clear()

            assertNull(store.read())
            assertTrue(sibling.isFile)
        }
    }

    private fun withTempFile(block: (File) -> Unit) {
        val dir = Files.createTempDirectory("gamefold-provision-store-").toFile()
        try {
            block(File(dir, "runtime-provision-v1.json"))
        } finally {
            dir.deleteRecursively()
        }
    }
}
