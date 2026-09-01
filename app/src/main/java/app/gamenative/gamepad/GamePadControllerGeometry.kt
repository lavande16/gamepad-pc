package app.gamenative.gamepad

import app.gamenative.gamefold.ui.GameFoldControllerGeometry

data class GamePadPoint(val x: Float, val y: Float)

data class GamePadRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    init {
        require(right >= left)
        require(bottom >= top)
    }

    val width: Float get() = right - left
    val height: Float get() = bottom - top

    fun contains(point: GamePadPoint): Boolean =
        point.x in left..right && point.y in top..bottom

    fun intersects(other: GamePadRect): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top
}

data class GamePadTransform(
    val scale: Float,
    val translateX: Float,
    val translateY: Float,
) {
    init {
        require(scale > 0f)
    }

    fun toScreen(point: GamePadPoint): GamePadPoint = GamePadPoint(
        x = point.x * scale + translateX,
        y = point.y * scale + translateY,
    )

    fun toSource(point: GamePadPoint): GamePadPoint = GamePadPoint(
        x = (point.x - translateX) / scale,
        y = (point.y - translateY) / scale,
    )

    fun mapRect(rect: GamePadRect): GamePadRect {
        val topLeft = toScreen(GamePadPoint(rect.left, rect.top))
        val bottomRight = toScreen(GamePadPoint(rect.right, rect.bottom))
        return GamePadRect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y)
    }
}

object GamePadControllerGeometry {
    const val GROUP_SCALE = 0.72f
    const val SMALL_BUTTON_HIT_RADIUS = GameFoldControllerGeometry.SMALL_BUTTON_RADIUS * 2.5f
    val LEFT_TRANSFORM = GamePadTransform(
        scale = GROUP_SCALE,
        translateX = 19.04f,
        translateY = -115f,
    )
    val RIGHT_TRANSFORM = GamePadTransform(
        scale = GROUP_SCALE,
        translateX = 499.36f,
        translateY = -115f,
    )
    val HOME_BOUNDS = GamePadRect(
        left = 1978f,
        top = 56f,
        right = 2248f,
        bottom = 133f,
    )
}
