package app.gamenative.gamefold.startup

import app.gamenative.utils.GameFoldPcResourceRoot
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class RuntimeProvisionPhase {
    VALIDATING_PUBLIC_PAYLOAD,
    INSTALLING_SHARED_RUNTIME,
    VERIFYING_PRIVATE_RUNTIME,
}

data class RuntimeProvisionProgress(
    val phase: RuntimeProvisionPhase,
    val percent: Int? = null,
)

data class PrivateRuntimeStatus(
    val imageFsReady: Boolean,
    val protonReady: Boolean,
) {
    val ready: Boolean get() = imageFsReady && protonReady
}

sealed interface RuntimeProvisionResult {
    data class Ready(val reused: Boolean) : RuntimeProvisionResult
    data object PermissionRequired : RuntimeProvisionResult
    data class PayloadInvalid(val missing: List<String>, val invalid: List<String>) : RuntimeProvisionResult
    data class Failed(val phase: RuntimeProvisionPhase, val message: String) : RuntimeProvisionResult
}

interface PrivateRuntimeGateway {
    fun inspect(): PrivateRuntimeStatus
    fun installSharedRuntime(onProgress: (Int) -> Unit): Boolean
    fun invalidateIncomplete()
}

interface RuntimeProvisioner {
    suspend fun ensureReady(
        forceFullValidation: Boolean = false,
        onProgress: (RuntimeProvisionProgress) -> Unit = {},
    ): RuntimeProvisionResult
}

class GameFoldPcRuntimeProvisioner(
    private val hasStoragePermission: () -> Boolean,
    private val validateManifest: (verifyHashes: Boolean) -> GameFoldPcResourceRoot.ManifestValidationResult,
    private val gateway: PrivateRuntimeGateway,
    private val store: GameFoldPcRuntimeProvisionStore,
) : RuntimeProvisioner {
    private val mutex = Mutex()

    override suspend fun ensureReady(
        forceFullValidation: Boolean,
        onProgress: (RuntimeProvisionProgress) -> Unit,
    ): RuntimeProvisionResult = mutex.withLock {
        if (!hasStoragePermission()) return@withLock RuntimeProvisionResult.PermissionRequired

        onProgress(RuntimeProvisionProgress(RuntimeProvisionPhase.VALIDATING_PUBLIC_PAYLOAD))
        val cheap = validateManifest(false)
        if (!cheap.valid) {
            return@withLock RuntimeProvisionResult.PayloadInvalid(cheap.missing, cheap.invalid)
        }

        val currentStatus = gateway.inspect()
        val receipt = store.read()
        val receiptMatches = receipt?.schema == 1 &&
            receipt.manifestSha256 == cheap.manifestSha256 &&
            receipt.imageFsVersion == 28 &&
            receipt.variant == "bionic" &&
            receipt.protonEntry == "proton-10.0-arm64ec-2"
        val needsFullValidation = forceFullValidation || !receiptMatches || !currentStatus.ready
        val full = if (needsFullValidation) {
            onProgress(RuntimeProvisionProgress(RuntimeProvisionPhase.VALIDATING_PUBLIC_PAYLOAD, 100))
            validateManifest(true)
        } else {
            cheap
        }
        if (!full.valid) {
            return@withLock RuntimeProvisionResult.PayloadInvalid(full.missing, full.invalid)
        }

        if (currentStatus.ready) {
            val manifestSha = full.manifestSha256
                ?: return@withLock RuntimeProvisionResult.Failed(
                    RuntimeProvisionPhase.VERIFYING_PRIVATE_RUNTIME,
                    "manifest fingerprint is unavailable",
                )
            if (!receiptMatches || forceFullValidation) {
                store.write(RuntimeProvisionReceipt(manifestSha256 = manifestSha))
            }
            return@withLock RuntimeProvisionResult.Ready(reused = true)
        }

        onProgress(RuntimeProvisionProgress(RuntimeProvisionPhase.INSTALLING_SHARED_RUNTIME, 0))
        val installed = try {
            gateway.installSharedRuntime { percent ->
                onProgress(RuntimeProvisionProgress(RuntimeProvisionPhase.INSTALLING_SHARED_RUNTIME, percent))
            }
        } catch (t: Throwable) {
            gateway.invalidateIncomplete()
            return@withLock RuntimeProvisionResult.Failed(
                RuntimeProvisionPhase.INSTALLING_SHARED_RUNTIME,
                t.message ?: "shared runtime installation failed",
            )
        }
        if (!installed) {
            gateway.invalidateIncomplete()
            return@withLock RuntimeProvisionResult.Failed(
                RuntimeProvisionPhase.INSTALLING_SHARED_RUNTIME,
                "shared runtime installation failed",
            )
        }

        onProgress(RuntimeProvisionProgress(RuntimeProvisionPhase.VERIFYING_PRIVATE_RUNTIME, 0))
        val afterInstall = gateway.inspect()
        if (!afterInstall.ready) {
            gateway.invalidateIncomplete()
            return@withLock RuntimeProvisionResult.Failed(
                RuntimeProvisionPhase.VERIFYING_PRIVATE_RUNTIME,
                "private ImageFS or Proton is incomplete (imageFs=${afterInstall.imageFsReady}, proton=${afterInstall.protonReady})",
            )
        }

        val manifestSha = full.manifestSha256
            ?: return@withLock RuntimeProvisionResult.Failed(
                RuntimeProvisionPhase.VERIFYING_PRIVATE_RUNTIME,
                "manifest fingerprint is unavailable",
            )
        store.write(RuntimeProvisionReceipt(manifestSha256 = manifestSha))
        RuntimeProvisionResult.Ready(reused = false)
    }
}
