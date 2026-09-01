package app.gamenative.gamefold.gyro

enum class RelativeMouseTransportRoute {
    WIN_HANDLER,
    XSERVER_POINTER,
    DROP,
}

object RelativeMouseTransportPolicy {
    fun route(
        relativeMouseMode: Boolean,
        winHandlerReady: Boolean,
    ): RelativeMouseTransportRoute = when {
        relativeMouseMode && winHandlerReady -> RelativeMouseTransportRoute.WIN_HANDLER
        relativeMouseMode -> RelativeMouseTransportRoute.DROP
        else -> RelativeMouseTransportRoute.XSERVER_POINTER
    }
}
