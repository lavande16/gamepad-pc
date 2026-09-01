package app.gamenative.gamepad

internal enum class GamePadQuickMenuChordButton { L3, R3 }

/**
 * Detects one L3+R3 chord without delaying or consuming either original button event.
 * A new chord is armed only after both buttons have been released.
 */
internal class GamePadQuickMenuChordDetector(
    private val chordWindowMs: Long = 500L,
) {
    private var l3DownAtMs: Long? = null
    private var r3DownAtMs: Long? = null
    private var triggeredForCurrentChord = false

    fun onButtonChanged(
        button: GamePadQuickMenuChordButton,
        pressed: Boolean,
        nowMs: Long,
    ): Boolean {
        if (!pressed) {
            when (button) {
                GamePadQuickMenuChordButton.L3 -> l3DownAtMs = null
                GamePadQuickMenuChordButton.R3 -> r3DownAtMs = null
            }
            if (l3DownAtMs == null && r3DownAtMs == null) {
                triggeredForCurrentChord = false
            }
            return false
        }

        when (button) {
            GamePadQuickMenuChordButton.L3 -> if (l3DownAtMs == null) l3DownAtMs = nowMs
            GamePadQuickMenuChordButton.R3 -> if (r3DownAtMs == null) r3DownAtMs = nowMs
        }
        if (triggeredForCurrentChord) return false

        val l3At = l3DownAtMs ?: return false
        val r3At = r3DownAtMs ?: return false
        if (kotlin.math.abs(l3At - r3At) > chordWindowMs) return false

        triggeredForCurrentChord = true
        return true
    }
}
