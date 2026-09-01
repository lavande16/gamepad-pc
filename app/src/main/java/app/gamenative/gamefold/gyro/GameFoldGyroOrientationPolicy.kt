package app.gamenative.gamefold.gyro

/** Applies the GameFold shell orientation without changing gyro calibration or timing. */
internal object GameFoldGyroOrientationPolicy {
    fun apply(delta: GyroMouseDelta, portraitMode: Boolean): GyroMouseDelta =
        if (portraitMode && delta != GyroMouseDelta.ZERO) {
            GyroMouseDelta(
                dxCounts = -delta.dxCounts,
                dyCounts = -delta.dyCounts,
            )
        } else {
            delta
        }
}
