package app.gamenative.product

import android.content.res.Configuration
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldPcSplashReadinessTest {
    @Test
    fun routeReadyCannotDismissSplashBeforeOrientationMatches() {
        val readiness = GameFoldPcSplashReadiness(orientationReady = false)

        readiness.updateRouteReady()

        assertTrue(readiness.shouldKeepOnScreen())
        readiness.updateOrientationReady(true)
        assertFalse(readiness.shouldKeepOnScreen())
    }

    @Test
    fun orientationReadyCannotDismissSplashBeforeRouteIsReady() {
        val readiness = GameFoldPcSplashReadiness(orientationReady = true)

        assertTrue(readiness.shouldKeepOnScreen())
        readiness.updateRouteReady()
        assertFalse(readiness.shouldKeepOnScreen())
    }

    @Test
    fun laterOrientationMismatchKeepsSplashAgain() {
        val readiness = GameFoldPcSplashReadiness(orientationReady = true)
        readiness.updateRouteReady()
        assertFalse(readiness.shouldKeepOnScreen())

        readiness.updateOrientationReady(false)

        assertTrue(readiness.shouldKeepOnScreen())
    }

    @Test
    fun savedDisplayModeMustMatchCurrentConfiguration() {
        assertTrue(
            GameFoldPcSplashReadiness.orientationMatches(
                portraitMode = false,
                configurationOrientation = Configuration.ORIENTATION_LANDSCAPE,
            ),
        )
        assertFalse(
            GameFoldPcSplashReadiness.orientationMatches(
                portraitMode = false,
                configurationOrientation = Configuration.ORIENTATION_PORTRAIT,
            ),
        )
        assertTrue(
            GameFoldPcSplashReadiness.orientationMatches(
                portraitMode = true,
                configurationOrientation = Configuration.ORIENTATION_PORTRAIT,
            ),
        )
    }
}
