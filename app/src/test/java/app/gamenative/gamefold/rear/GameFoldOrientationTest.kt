package app.gamenative.gamefold.rear

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GameFoldOrientationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferences
        get() = context.getSharedPreferences("gamefold_rear_orientation", Context.MODE_PRIVATE)

    @Before
    fun resetOrientationPreference() {
        preferences.edit().clear().commit()
    }

    @After
    fun clearOrientationPreference() {
        preferences.edit().clear().commit()
    }

    @Test
    fun defaultOrientation_isFixedLandscape() {
        assertFalse(GameFoldOrientation.isPortrait(context))
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
            GameFoldOrientation.requestedOrientation(context),
        )
    }

    @Test
    fun quickStartToggle_isThePersistedPortraitSource() {
        assertTrue(GameFoldOrientation.toggle(context))
        assertTrue(GameFoldOrientation.isPortrait(context))
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
            GameFoldOrientation.requestedOrientation(context),
        )

        assertFalse(GameFoldOrientation.toggle(context))
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
            GameFoldOrientation.requestedOrientation(context),
        )
    }
}
