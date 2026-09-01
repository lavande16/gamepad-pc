package app.gamenative.utils

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

class GameFoldPcResourceRootTest {
    @Test
    fun missingManifest_isInvalidAndHasNoFingerprint() {
        withTempRoot { root ->
            val result = GameFoldPcResourceRoot.validateManifestAt(root, verifyHashes = true)
            assertFalse(result.manifestPresent)
            assertFalse(result.valid)
            assertEquals(null, result.manifestSha256)
        }
    }

    @Test
    fun requiredEntry_checksSizeAndHashWhenFullValidationIsRequested() {
        withTempRoot { root ->
            val payload = File(root, "imagefs/imagefs_bionic.txz").apply {
                parentFile!!.mkdirs()
                writeText("payload-v1")
            }
            writeManifest(root, payload, includeHash = true, declaredSize = payload.length())

            val valid = GameFoldPcResourceRoot.validateManifestAt(root, verifyHashes = true)
            assertTrue(valid.valid)

            payload.appendText("corruption")
            val invalid = GameFoldPcResourceRoot.validateManifestAt(root, verifyHashes = true)
            assertFalse(invalid.valid)
            assertTrue(invalid.invalid.any { it.contains("SHA-256") || it.contains("大小") })
        }
    }

    @Test
    fun cheapValidation_skipsHashMismatchButStillChecksPathAndSize() {
        withTempRoot { root ->
            val payload = File(root, "proton/proton.wcp").apply {
                parentFile!!.mkdirs()
                writeText("payload-v1")
            }
            writeManifest(root, payload, includeHash = true, declaredSize = payload.length())
            payload.writeText("payload-v2")

            val cheap = GameFoldPcResourceRoot.validateManifestAt(root, verifyHashes = false)
            assertTrue(cheap.valid)
            val full = GameFoldPcResourceRoot.validateManifestAt(root, verifyHashes = true)
            assertFalse(full.valid)
        }
    }

    @Test
    fun traversalAndEmptyPaths_areInvalidWithoutEscapingRoot() {
        withTempRoot { root ->
            val outside = File(root.parentFile, "outside-${System.nanoTime()}").apply { writeText("do-not-touch") }
            try {
                val manifest = JSONArray()
                    .put(JSONObject().put("path", "../${outside.name}").put("size", outside.length()))
                    .put(JSONObject().put("path", "nested/../../escape").put("size", 1))
                    .put(JSONObject().put("path", ""))
                File(root, GameFoldPcResourceRoot.MANIFEST_NAME).writeText(
                    JSONObject().put("required", manifest).toString(),
                )

                val result = GameFoldPcResourceRoot.validateManifestAt(root, verifyHashes = false)
                assertFalse(result.valid)
                assertTrue(result.invalid.isNotEmpty())
                assertEquals("do-not-touch", outside.readText())
            } finally {
                outside.delete()
            }
        }
    }

    @Test
    fun manifestFingerprint_changesWhenManifestBytesChange() {
        withTempRoot { root ->
            val payload = File(root, "a.bin").apply { writeText("a") }
            writeManifest(root, payload, includeHash = false, declaredSize = payload.length())
            val first = GameFoldPcResourceRoot.validateManifestAt(root, verifyHashes = false)
            File(root, GameFoldPcResourceRoot.MANIFEST_NAME).appendText("\n")
            val second = GameFoldPcResourceRoot.validateManifestAt(root, verifyHashes = false)
            assertNotEquals(first.manifestSha256, second.manifestSha256)
        }
    }

    @Test
    fun exactGraphicsDriverResolution_matchesPublicV25AndV30Archives() {
        withTempRoot { root ->
            val graphics = File(root, "graphics").apply { mkdirs() }
            val v25 = File(graphics, "adrenotools-Turnip_Gen8_V25.tzst").apply { writeText("v25") }
            val v30 = File(graphics, "adrenotools-Turnip Gen8 V30.tzst").apply { writeText("v30") }

            assertEquals(
                v25.canonicalFile,
                GameFoldPcResourceRoot.resolveGraphicsDriverExactAt(root, "Turnip_Gen8_V25")?.canonicalFile,
            )
            assertEquals(
                v30.canonicalFile,
                GameFoldPcResourceRoot.resolveGraphicsDriverExactAt(root, "Turnip Gen8 V30")?.canonicalFile,
            )
        }
    }

    @Test
    fun exactGraphicsDriverResolution_doesNotSubstituteAnotherTurnipBuild() {
        withTempRoot { root ->
            File(root, "graphics/adrenotools-Turnip Gen8 V30.tzst").apply {
                parentFile!!.mkdirs()
                writeText("v30")
            }

            assertEquals(
                null,
                GameFoldPcResourceRoot.resolveGraphicsDriverExactAt(root, "Turnip_Gen8_V25"),
            )
        }
    }

    @Test
    fun exactRuntimeGraphicsDriverResolution_requiresTzstInsteadOfInstallerZip() {
        withTempRoot { root ->
            File(root, "graphics/adrenotools-Turnip Gen8 V30.zip").apply {
                parentFile!!.mkdirs()
                writeText("installer-zip")
            }

            assertEquals(
                null,
                GameFoldPcResourceRoot.resolveGraphicsDriverExactAt(root, "Turnip Gen8 V30"),
            )
        }
    }

    private fun writeManifest(root: File, payload: File, includeHash: Boolean, declaredSize: Long) {
        val entry = JSONObject()
            .put("path", payload.relativeTo(root).invariantSeparatorsPath)
            .put("size", declaredSize)
        if (includeHash) entry.put("sha256", payload.sha256())
        File(root, GameFoldPcResourceRoot.MANIFEST_NAME).writeText(
            JSONObject().put("required", JSONArray().put(entry)).toString(),
        )
    }

    private fun withTempRoot(block: (File) -> Unit) {
        val root = Files.createTempDirectory("gamefold-resource-root-").toFile()
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }

    private fun File.sha256(): String = MessageDigest.getInstance("SHA-256")
        .digest(readBytes())
        .joinToString("") { "%02x".format(it) }
}
