package app.gamenative.gamefold.startup

import androidx.core.util.AtomicFile
import org.json.JSONObject
import java.io.File

data class RuntimeProvisionReceipt(
    val schema: Int = 1,
    val manifestSha256: String,
    val imageFsVersion: Int = 28,
    val variant: String = "bionic",
    val protonEntry: String = "proton-10.0-arm64ec-2",
)

class GameFoldPcRuntimeProvisionStore(private val file: File) {
    private val atomicFile = AtomicFile(file)

    @Synchronized
    fun read(): RuntimeProvisionReceipt? {
        if (!file.isFile) return null
        return runCatching {
            val json = JSONObject(file.readText())
            val receipt = RuntimeProvisionReceipt(
                schema = json.getInt("schema"),
                manifestSha256 = json.getString("manifestSha256"),
                imageFsVersion = json.getInt("imageFsVersion"),
                variant = json.getString("variant"),
                protonEntry = json.getString("protonEntry"),
            )
            receipt.takeIf {
                it.schema == 1 && it.manifestSha256.isNotBlank() &&
                    it.imageFsVersion > 0 && it.variant.isNotBlank() && it.protonEntry.isNotBlank()
            }
        }.getOrNull()
    }

    @Synchronized
    fun write(receipt: RuntimeProvisionReceipt) {
        file.parentFile?.mkdirs()
        val output = atomicFile.startWrite()
        try {
            val writer = output.writer(Charsets.UTF_8)
            writer.write(
                JSONObject()
                    .put("schema", receipt.schema)
                    .put("manifestSha256", receipt.manifestSha256)
                    .put("imageFsVersion", receipt.imageFsVersion)
                    .put("variant", receipt.variant)
                    .put("protonEntry", receipt.protonEntry)
                    .toString(),
            )
            writer.flush()
            atomicFile.finishWrite(output)
        } catch (t: Throwable) {
            atomicFile.failWrite(output)
            throw t
        }
    }

    @Synchronized
    fun clear() {
        atomicFile.delete()
    }
}
