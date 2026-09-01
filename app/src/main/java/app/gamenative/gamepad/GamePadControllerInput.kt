package app.gamenative.gamepad

import app.gamenative.gamefold.ui.GameFoldActiveControl
import app.gamenative.gamefold.ui.GameFoldControlGroup
import app.gamenative.gamefold.ui.GameFoldPoint
import app.gamenative.gamefold.ui.classifyGameFoldControlInGroup

internal data class GamePadControlHit(
    val control: GameFoldActiveControl,
    val sourcePosition: GameFoldPoint,
)

internal fun classifyGamePadControl(screenPosition: GamePadPoint): GamePadControlHit? {
    if (GamePadControllerGeometry.HOME_BOUNDS.contains(screenPosition)) {
        return GamePadControlHit(
            control = GameFoldActiveControl.Home,
            sourcePosition = GameFoldPoint(screenPosition.x, screenPosition.y),
        )
    }

    val left = GamePadControllerGeometry.LEFT_TRANSFORM.toSource(screenPosition)
    classifyGameFoldControlInGroup(
        GameFoldPoint(left.x, left.y),
        GameFoldControlGroup.LEFT,
        GamePadControllerGeometry.SMALL_BUTTON_HIT_RADIUS,
    )?.let { control ->
        return GamePadControlHit(control, GameFoldPoint(left.x, left.y))
    }

    val right = GamePadControllerGeometry.RIGHT_TRANSFORM.toSource(screenPosition)
    classifyGameFoldControlInGroup(
        GameFoldPoint(right.x, right.y),
        GameFoldControlGroup.RIGHT,
        GamePadControllerGeometry.SMALL_BUTTON_HIT_RADIUS,
    )?.let { control ->
        return GamePadControlHit(control, GameFoldPoint(right.x, right.y))
    }
    return null
}
