package app.gamenative.diagnostics

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.gamenative.BuildConfig
import app.gamenative.PluviaApp
import app.gamenative.events.AndroidEvent

/** Shell-only bridge to the existing in-game quick menu. */
class AdbQuickMenuReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!BuildConfig.GAMEFOLD_PC_LOCAL_SHELL || intent.action != ACTION_OPEN_QUICK_MENU) {
            Log.w(TAG, "Rejected quick-menu broadcast action=${intent.action}")
            return
        }

        Log.i(TAG, "Accepted ADB quick-menu request")
        PluviaApp.events.emit(AndroidEvent.OpenAdbQuickMenu)
    }

    companion object {
        const val ACTION_OPEN_QUICK_MENU = "com.gamepad.pc.action.OPEN_QUICK_MENU"
        const val TAG = "GamePadAdbQuickMenu"
    }
}
