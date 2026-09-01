package app.gamenative.product

object ExitConfirmationPolicy {
    enum class Action {
        WARN,
        EXIT,
    }

    fun action(warnBeforeExit: Boolean, warningVisible: Boolean): Action = when {
        !warnBeforeExit -> Action.EXIT
        warningVisible -> Action.EXIT
        else -> Action.WARN
    }
}
