package app.gamenative.gamepad

internal object GamePadAddGamePolicy {
    private const val DOWNLOAD_URL = "https://dl.3dmgame.com/all_all_1_hot_cn/"

    fun openDownloadPage(openUri: (String) -> Unit) {
        openUri(DOWNLOAD_URL)
    }
}
