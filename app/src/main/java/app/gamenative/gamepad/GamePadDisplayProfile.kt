package app.gamenative.gamepad

import android.content.Context
import app.gamenative.PrefManager
import app.gamenative.displayresize.DisplaySizeSnapshot
import app.gamenative.product.NfsHotPursuitResolutionConfig
import com.winlator.container.Container
import com.winlator.container.ContainerManager

object GamePadDisplayProfile {
    private const val FULL_SCREEN_GAME_SIZE = "2304x1440"
    private const val TV_16_9_GAME_SIZE = "2304x1296"

    fun gameScreenSize(snapshot: DisplaySizeSnapshot): String =
        if (snapshot.isTargetOverride) TV_16_9_GAME_SIZE else FULL_SCREEN_GAME_SIZE

    fun synchronize(
        snapshot: DisplaySizeSnapshot,
        containers: List<Container>,
        writeGlobal: (String) -> Unit,
        persist: (Container) -> Unit,
    ) {
        val screenSize = gameScreenSize(snapshot)
        writeGlobal(screenSize)
        containers.forEach { container ->
            container.screenSize = screenSize
            NfsHotPursuitResolutionConfig.synchronize(container, screenSize)
            persist(container)
        }
    }

    fun apply(context: Context, snapshot: DisplaySizeSnapshot) {
        val containers = ContainerManager(context).containers
        synchronize(
            snapshot = snapshot,
            containers = containers,
            writeGlobal = { PrefManager.screenSize = it },
            persist = { it.saveData() },
        )
    }
}
