package app.gamenative.gamepad

data class GamePadViewport(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
) {
    val right: Int get() = left + width
    val bottom: Int get() = top + height
}

data class GamePadSourceSize(val width: Int, val height: Int)

sealed interface GamePadRuntimeSourceValidation {
    data class Valid(val size: GamePadSourceSize) : GamePadRuntimeSourceValidation
    data class Invalid(val message: String) : GamePadRuntimeSourceValidation
}

object GamePadViewportPolicy {
    fun validateRuntimeSource(raw: String?): GamePadRuntimeSourceValidation =
        parseSourceSize(raw)?.let(GamePadRuntimeSourceValidation::Valid)
            ?: GamePadRuntimeSourceValidation.Invalid(
                "GamePad PC 无法读取游戏分辨率：${raw ?: "<empty>"}",
            )

    fun parseSourceSize(raw: String?): GamePadSourceSize? {
        val parts = raw?.trim()?.split('x', 'X') ?: return null
        if (parts.size != 2) return null
        val width = parts[0].trim().toIntOrNull() ?: return null
        val height = parts[1].trim().toIntOrNull() ?: return null
        if (width <= 0 || height <= 0) return null
        return GamePadSourceSize(width, height)
    }

    fun fit(
        windowWidth: Int,
        windowHeight: Int,
        sourceWidth: Int,
        sourceHeight: Int,
    ): GamePadViewport {
        require(windowWidth > 0 && windowHeight > 0)
        require(sourceWidth > 0 && sourceHeight > 0)

        val sourceIsWider = sourceWidth.toLong() * windowHeight >
            windowWidth.toLong() * sourceHeight
        val width: Int
        val height: Int
        if (sourceIsWider) {
            width = windowWidth
            height = (windowWidth.toLong() * sourceHeight / sourceWidth).toInt()
        } else {
            height = windowHeight
            width = (windowHeight.toLong() * sourceWidth / sourceHeight).toInt()
        }
        return GamePadViewport(
            left = (windowWidth - width) / 2,
            top = (windowHeight - height) / 2,
            width = width,
            height = height,
        )
    }
}
