package app.gamenative.gamefold.gyro

internal class GyroMouseOutputDispatcher(
    private val sendRelativeMove: (dx: Int, dy: Int) -> Int,
) {
    fun dispatch(dx: Int, dy: Int): Int =
        if (dx == 0 && dy == 0) 0 else sendRelativeMove(dx, dy)
}
