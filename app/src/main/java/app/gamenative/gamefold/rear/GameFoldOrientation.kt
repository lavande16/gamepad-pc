package app.gamenative.gamefold.rear

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo

/** Explicit GameFold orientation preference shared by the front shell and rear session. */
object GameFoldOrientation {
    private const val PREF_PORTRAIT_MODE = "gamefold_portrait_mode"

    fun isPortrait(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_PORTRAIT_MODE, false)

    fun toggle(context: Context): Boolean {
        val portrait = !isPortrait(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(PREF_PORTRAIT_MODE, portrait)
            .apply()
        return portrait
    }

    internal fun requestedOrientation(context: Context): Int = if (isPortrait(context)) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    } else {
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    fun apply(activity: Activity) {
        val requested = requestedOrientation(activity)
        if (activity.requestedOrientation != requested) activity.requestedOrientation = requested
    }

    private const val PREFS_NAME = "gamefold_rear_orientation"
}
