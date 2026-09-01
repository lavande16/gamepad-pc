package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldPcDriverPolicyTest {
    @Test
    fun eliteSocUsesV30() {
        assertEquals(
            GameFoldPcDriverPolicy.TURNIP_GEN8_V30,
            GameFoldPcDriverPolicy.defaultDriverForSoc("SM8750", "fallback"),
        )
        assertEquals(
            GameFoldPcDriverPolicy.TURNIP_GEN8_V30,
            GameFoldPcDriverPolicy.defaultDriverForSoc(" sm8850 ", "fallback"),
        )
    }

    @Test
    fun gen3SocUsesV25() {
        assertEquals(
            GameFoldPcDriverPolicy.TURNIP_GEN8_V25,
            GameFoldPcDriverPolicy.defaultDriverForSoc("sm8650", "fallback"),
        )
    }

    @Test
    fun unknownSocKeepsExistingFallback() {
        assertEquals("fallback", GameFoldPcDriverPolicy.defaultDriverForSoc("SM8550", "fallback"))
        assertEquals("fallback", GameFoldPcDriverPolicy.defaultDriverForSoc(null, "fallback"))
    }

    @Test
    fun migrationReplacesOnlyAutomaticDriverValues() {
        assertTrue(GameFoldPcDriverPolicy.shouldReplaceAutomaticDriver("", "Turnip v26.2.0 R4"))
        assertTrue(GameFoldPcDriverPolicy.shouldReplaceAutomaticDriver("System", "Turnip v26.2.0 R4"))
        assertTrue(
            GameFoldPcDriverPolicy.shouldReplaceAutomaticDriver(
                "Turnip v26.2.0 R4",
                "Turnip v26.2.0 R4",
            ),
        )
        assertFalse(
            GameFoldPcDriverPolicy.shouldReplaceAutomaticDriver(
                "Turnip Adreno Driver T26 (@Mr_Purple_666)",
                "Turnip v26.2.0 R4",
            ),
        )
        assertFalse(
            GameFoldPcDriverPolicy.shouldReplaceAutomaticDriver(
                GameFoldPcDriverPolicy.TURNIP_GEN8_V30,
                "Turnip v26.2.0 R4",
            ),
        )
    }
}
