package app.gamenative.gamepad

enum class GamePadHomeHeaderAction {
    DUAL_SCREEN,
    RESIZE,
    SETTINGS,
}

object GamePadHomeHeaderPolicy {
    fun visibleActions(dualScreenAvailable: Boolean): List<GamePadHomeHeaderAction> = buildList {
        if (dualScreenAvailable) add(GamePadHomeHeaderAction.DUAL_SCREEN)
        add(GamePadHomeHeaderAction.RESIZE)
        add(GamePadHomeHeaderAction.SETTINGS)
    }
}
