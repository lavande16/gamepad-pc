package app.gamenative.gamefold.startup

import app.gamenative.utils.GameFoldPcResourceRoot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

class GameFoldPcRuntimeProvisionerTest {
    @Test
    fun permissionDenied_returnsWithoutValidationOrInstall() = runBlocking {
        val fixture = Fixture(permission = false)

        val result = fixture.provisioner.ensureReady()

        assertTrue(result is RuntimeProvisionResult.PermissionRequired)
        assertEquals(0, fixture.validationCalls)
        assertEquals(0, fixture.gateway.installCalls)
        assertTrue(fixture.storeFile.parentFile!!.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun anyMissingRequiredPayload_blocksInstall() = runBlocking {
        val fixture = Fixture(manifest = GameFoldPcResourceRoot.ManifestValidationResult(
            manifestPresent = true,
            valid = false,
            missing = listOf("extras.tzst"),
            manifestSha256 = "manifest-1",
        ))

        val result = fixture.provisioner.ensureReady()

        assertTrue(result is RuntimeProvisionResult.PayloadInvalid)
        assertEquals(0, fixture.gateway.installCalls)
    }

    @Test
    fun firstProvision_fullValidatesThenInstallsAndWritesReceiptAfterInspection() = runBlocking {
        val fixture = Fixture()
        fixture.gateway.installResult = true

        val result = fixture.provisioner.ensureReady()

        assertEquals(RuntimeProvisionResult.Ready(reused = false), result)
        assertEquals(listOf(false, true), fixture.validationModes)
        assertEquals(1, fixture.gateway.installCalls)
        assertEquals("manifest-1", fixture.store.read()!!.manifestSha256)
    }

    @Test
    fun installSuccessButPrivateRuntimeIncomplete_failsAndInvalidatesWithoutReceipt() = runBlocking {
        val fixture = Fixture()
        fixture.gateway.installResult = true
        fixture.gateway.postInstallStatus = PrivateRuntimeStatus(imageFsReady = true, protonReady = false)

        val result = fixture.provisioner.ensureReady()

        assertTrue(result is RuntimeProvisionResult.Failed)
        assertEquals(1, fixture.gateway.invalidateCalls)
        assertEquals(null, fixture.store.read())
    }

    @Test
    fun matchingReceiptAndReadyRuntime_usesCheapValidationAndSkipsInstall() = runBlocking {
        val fixture = Fixture()
        fixture.gateway.status = PrivateRuntimeStatus(imageFsReady = true, protonReady = true)
        fixture.store.write(RuntimeProvisionReceipt(manifestSha256 = "manifest-1"))

        val result = fixture.provisioner.ensureReady()

        assertEquals(RuntimeProvisionResult.Ready(reused = true), result)
        assertEquals(listOf(false), fixture.validationModes)
        assertEquals(0, fixture.gateway.installCalls)
    }

    @Test
    fun manifestMetadataChange_forcesFullValidationEvenWhenRuntimeIsReady() = runBlocking {
        val fixture = Fixture()
        fixture.gateway.status = PrivateRuntimeStatus(imageFsReady = true, protonReady = true)
        fixture.store.write(RuntimeProvisionReceipt(manifestSha256 = "old-manifest"))

        val result = fixture.provisioner.ensureReady()

        assertEquals(RuntimeProvisionResult.Ready(reused = true), result)
        assertEquals(listOf(false, true), fixture.validationModes)
        assertEquals(0, fixture.gateway.installCalls)
        assertEquals("manifest-1", fixture.store.read()!!.manifestSha256)
    }

    @Test
    fun concurrentCallers_shareOneInstall() = runBlocking {
        val fixture = Fixture()
        val results = (1..3).map { async { fixture.provisioner.ensureReady() } }.awaitAll()

        assertEquals(1, fixture.gateway.installCalls)
        assertEquals(1, results.count { it is RuntimeProvisionResult.Ready && !it.reused })
        assertEquals(2, results.count { it is RuntimeProvisionResult.Ready && it.reused })
    }

    @Test
    fun preLaunchSingleFlight_cancelInvalidatesOldTicketAndAllowsImmediateRestart() {
        val coordinator = GameFoldPcPreLaunchSingleFlight()

        val first = coordinator.tryStart("custom:cyberpunk")
        assertNotNull(first)
        assertNull(coordinator.tryStart("custom:cyberpunk"))
        assertNotNull(coordinator.tryStart("custom:gta5"))

        assertTrue(coordinator.cancel("custom:cyberpunk"))
        assertFalse(coordinator.isActive(first!!))

        val restarted = coordinator.tryStart("custom:cyberpunk")
        assertNotNull(restarted)
        assertTrue(coordinator.isActive(restarted!!))

        coordinator.finish(first)
        assertTrue(coordinator.isActive(restarted))
        coordinator.finish(restarted)
        assertFalse(coordinator.isActive(restarted))
    }

    private class Fixture(
        permission: Boolean = true,
        manifest: GameFoldPcResourceRoot.ManifestValidationResult = GameFoldPcResourceRoot.ManifestValidationResult(
            manifestPresent = true,
            valid = true,
            manifestSha256 = "manifest-1",
        ),
    ) {
        val root = Files.createTempDirectory("gamefold-provisioner-").toFile()
        val storeFile = File(root, "files/gamefold_pc/runtime-provision-v1.json")
        val store = GameFoldPcRuntimeProvisionStore(storeFile)
        val gateway = FakeGateway()
        val validationModes = mutableListOf<Boolean>()
        var validationCalls = 0
        private val validator = { verifyHashes: Boolean ->
            validationCalls++
            validationModes += verifyHashes
            manifest
        }
        val provisioner = GameFoldPcRuntimeProvisioner(
            hasStoragePermission = { permission },
            validateManifest = validator,
            gateway = gateway,
            store = store,
        )
    }

    private class FakeGateway : PrivateRuntimeGateway {
        var status = PrivateRuntimeStatus(imageFsReady = false, protonReady = false)
        var installResult = true
        var postInstallStatus: PrivateRuntimeStatus? = null
        val installCounter = AtomicInteger()
        var invalidateCalls = 0
        val installCalls: Int get() = installCounter.get()

        override fun inspect(): PrivateRuntimeStatus = status

        override fun installSharedRuntime(onProgress: (Int) -> Unit): Boolean {
            installCounter.incrementAndGet()
            onProgress(100)
            if (installResult) {
                status = postInstallStatus ?: PrivateRuntimeStatus(imageFsReady = true, protonReady = true)
            }
            return installResult
        }

        override fun invalidateIncomplete() {
            invalidateCalls++
        }
    }
}
