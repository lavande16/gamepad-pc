package app.gamenative.gamepad

data class GamePadWindowState(
    val widthPx: Int,
    val heightPx: Int,
    val smallestWidthDp: Int,
    val inMultiWindow: Boolean,
    val inPictureInPicture: Boolean,
)

object GamePadWindowPolicy {
    const val WIDTH_PX = 2304
    const val HEIGHT_PX = 1440
    const val SIXTEEN_NINE_WIDTH_PX = 2304
    const val SIXTEEN_NINE_HEIGHT_PX = 1296
    const val FULL_HD_WIDTH_PX = 1920
    const val FULL_HD_HEIGHT_PX = 1080
    const val MIN_SMALLEST_WIDTH_DP = 576
    const val BORDER_WIDTH_PX = 0
    const val CORNER_RADIUS_PX = 0

    fun isSupported(state: GamePadWindowState): Boolean {
        if (state.inMultiWindow || state.inPictureInPicture) return false

        val isNativeFullScreen =
            state.widthPx == WIDTH_PX &&
                state.heightPx == HEIGHT_PX &&
                state.smallestWidthDp >= MIN_SMALLEST_WIDTH_DP
        val isSixteenNineFullScreen =
            state.widthPx == SIXTEEN_NINE_WIDTH_PX &&
                state.heightPx == SIXTEEN_NINE_HEIGHT_PX
        val isFullHdFullScreen =
            state.widthPx == FULL_HD_WIDTH_PX &&
                state.heightPx == FULL_HD_HEIGHT_PX

        return isNativeFullScreen || isSixteenNineFullScreen || isFullHdFullScreen
    }
}
