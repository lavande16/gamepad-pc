package app.gamenative.product

object GameFoldPcDriverPolicy {
    const val TURNIP_GEN8_V25 = "Turnip_Gen8_V25"
    const val TURNIP_GEN8_V30 = "Turnip Gen8 V30"

    private val eliteSocModels = setOf("SM8750", "SM8850")

    @JvmStatic
    fun defaultDriverForSoc(socModel: String?, fallback: String): String {
        return when (socModel?.trim()?.uppercase()) {
            in eliteSocModels -> TURNIP_GEN8_V30
            "SM8650" -> TURNIP_GEN8_V25
            else -> fallback
        }
    }

    @JvmStatic
    fun shouldReplaceAutomaticDriver(current: String, previousAutomatic: String): Boolean {
        val normalized = current.trim()
        return normalized.isEmpty() ||
            normalized.equals("System", ignoreCase = true) ||
            normalized.equals(previousAutomatic, ignoreCase = true)
    }

    @JvmStatic
    fun publicDriverIds(): List<String> = listOf(TURNIP_GEN8_V25, TURNIP_GEN8_V30)
}
