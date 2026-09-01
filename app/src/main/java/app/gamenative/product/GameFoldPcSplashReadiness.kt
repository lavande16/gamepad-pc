package app.gamenative.product

import android.content.res.Configuration

/** Keeps the system splash visible until both startup routing and display orientation are stable. */
class GameFoldPcSplashReadiness(orientationReady: Boolean) {
    companion object {
        fun orientationMatches(portraitMode: Boolean, configurationOrientation: Int): Boolean {
            val expected = if (portraitMode) {
                Configuration.ORIENTATION_PORTRAIT
            } else {
                Configuration.ORIENTATION_LANDSCAPE
            }
            return configurationOrientation == expected
        }
    }

    private var routeReady: Boolean = false
    private var orientationReady: Boolean = orientationReady

    fun updateRouteReady() {
        routeReady = true
    }

    fun updateOrientationReady(ready: Boolean) {
        orientationReady = ready
    }

    fun shouldKeepOnScreen(): Boolean = !(routeReady && orientationReady)
}
