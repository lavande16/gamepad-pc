package app.gamenative.gamefold.startup

import android.content.Context
import app.gamenative.utils.GameFoldPcResourceRoot
import java.io.File

/**
 * Process-wide owner of runtime provisioning. Both the startup gate and click-to-launch path must
 * use this instance; creating a provisioner in each Compose call would give each caller a separate
 * mutex and allow the same ImageFS/Proton transaction to run twice.
 */
object GameFoldPcRuntimeProvisionCoordinator {
    @Volatile
    private var provisioner: GameFoldPcRuntimeProvisioner? = null

    private fun get(context: Context): GameFoldPcRuntimeProvisioner {
        provisioner?.let { return it }
        return synchronized(this) {
            provisioner ?: context.applicationContext.let { appContext ->
                GameFoldPcRuntimeProvisioner(
                    hasStoragePermission = {
                        app.gamenative.offline.OfflineStorageAccess.hasRequiredAccess(appContext)
                    },
                    validateManifest = GameFoldPcResourceRoot::validateManifest,
                    gateway = AndroidGameFoldPcRuntimeGateway(appContext),
                    store = GameFoldPcRuntimeProvisionStore(
                        File(appContext.filesDir, "gamefold_pc/runtime-provision-v1.json"),
                    ),
                ).also { provisioner = it }
            }
        }
    }

    suspend fun ensureReady(
        context: Context,
        forceFullValidation: Boolean = false,
        onProgress: (RuntimeProvisionProgress) -> Unit = {},
    ): RuntimeProvisionResult = get(context).ensureReady(forceFullValidation, onProgress)
}

/** Prevents duplicate taps/recomposition from starting two per-game launch transactions. */
class GameFoldPcPreLaunchTicket internal constructor(
    val appId: String,
    internal val generation: Long,
)

class GameFoldPcPreLaunchSingleFlight {
    private val activeTickets = mutableMapOf<String, GameFoldPcPreLaunchTicket>()
    private var nextGeneration = 0L

    @Synchronized
    fun tryStart(appId: String): GameFoldPcPreLaunchTicket? {
        if (activeTickets.containsKey(appId)) return null
        return GameFoldPcPreLaunchTicket(appId, ++nextGeneration).also {
            activeTickets[appId] = it
        }
    }

    @Synchronized
    fun cancel(appId: String): Boolean = activeTickets.remove(appId) != null

    @Synchronized
    fun isActive(ticket: GameFoldPcPreLaunchTicket): Boolean =
        activeTickets[ticket.appId] == ticket

    @Synchronized
    fun finish(ticket: GameFoldPcPreLaunchTicket) {
        if (activeTickets[ticket.appId] == ticket) {
            activeTickets.remove(ticket.appId)
        }
    }
}

object GameFoldPcPreLaunchCoordinator {
    val singleFlight = GameFoldPcPreLaunchSingleFlight()
}
