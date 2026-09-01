package app.gamenative.product

object GamePadScreenSizePolicy {
    private const val CUSTOM_ENTRY = "Custom"

    fun isCustom(entries: List<String>, index: Int): Boolean =
        entries.getOrNull(index) == CUSTOM_ENTRY

    fun selectedIndex(entries: List<String>, currentValue: String): Int {
        val presetIndex = entries.indexOfFirst { it.substringBefore(' ') == currentValue }
        if (presetIndex >= 0) return presetIndex

        return entries.indexOf(CUSTOM_ENTRY).takeIf { it >= 0 } ?: 0
    }

    fun resolveSelection(
        entries: List<String>,
        selectedIndex: Int,
        customWidth: String,
        customHeight: String,
        currentValue: String,
    ): String {
        if (isCustom(entries, selectedIndex)) {
            return if (customWidth.isNotEmpty() && customHeight.isNotEmpty()) {
                "${customWidth}x$customHeight"
            } else {
                currentValue
            }
        }

        return entries.getOrNull(selectedIndex)?.substringBefore(' ') ?: currentValue
    }
}
