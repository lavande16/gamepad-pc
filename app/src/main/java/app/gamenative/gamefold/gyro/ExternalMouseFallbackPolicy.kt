package app.gamenative.gamefold.gyro

internal object ExternalMouseFallbackPolicy {
    fun shouldForward(
        hasPointerCapture: Boolean,
        isExternal: Boolean,
        supportsRelativeMouse: Boolean,
        isMove: Boolean,
        dx: Int,
        dy: Int,
    ): Boolean =
        !hasPointerCapture &&
            isExternal &&
            supportsRelativeMouse &&
            isMove &&
            (dx != 0 || dy != 0)
}
