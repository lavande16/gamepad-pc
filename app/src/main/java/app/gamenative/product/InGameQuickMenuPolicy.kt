package app.gamenative.product

object InGameQuickMenuPolicy {
    enum class BackAction {
        DISMISS,
        OPEN,
        REQUEST_EXIT,
    }

    fun backAction(enabled: Boolean, visible: Boolean): BackAction = when {
        visible -> BackAction.DISMISS
        enabled -> BackAction.OPEN
        else -> BackAction.REQUEST_EXIT
    }
}
