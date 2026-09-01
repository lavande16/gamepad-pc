package app.gamenative.product

/**
 * The local GamePad PC shell only supports the ARM64 runtime payload.
 * Keep this check independent of storage permission so the first-launch order
 * remains processor -> permission -> payload.
 */
object GameFoldPcProcessorPolicy {
    const val REQUIRED_ABI = "arm64-v8a"

    private val mediatekModelPattern = Regex(
        "(^|[^A-Z0-9])MT\\d{3,}[A-Z0-9]*($|[^A-Z0-9])",
    )

    fun isSupported(supportedAbis: Array<String>): Boolean =
        supportedAbis.any { it.equals(REQUIRED_ABI, ignoreCase = true) }

    fun isSupported(
        supportedAbis: Array<String>,
        socModel: String?,
        hardware: String?,
    ): Boolean = isSupported(supportedAbis) && !isMediaTek(socModel, hardware)

    fun isMediaTek(socModel: String?, hardware: String?): Boolean =
        sequenceOf(socModel, hardware)
            .filterNotNull()
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
            .any { value ->
                value.contains("MEDIATEK") ||
                    value.contains("DIMENSITY") ||
                    value.contains("MTK") ||
                    mediatekModelPattern.containsMatchIn(value)
            }
}
