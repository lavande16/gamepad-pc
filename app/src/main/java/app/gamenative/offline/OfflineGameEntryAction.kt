package app.gamenative.offline

internal enum class OfflineGameEntryAction {
    Launch,
    Details,
}

internal fun offlineGameEntryAction(longPressed: Boolean): OfflineGameEntryAction =
    if (longPressed) OfflineGameEntryAction.Details else OfflineGameEntryAction.Launch
