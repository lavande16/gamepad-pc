package app.gamenative.ui.screen.settings

import app.gamenative.BuildConfig
import app.gamenative.ui.screen.library.gamePadHeaderHorizontalInsetDp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class SettingsGeometryTest {
    private fun sourceFile(path: String): java.io.File = sequenceOf(
        java.io.File(path),
        java.io.File("../$path"),
    ).first { it.exists() }

    @Test
    fun settingsDirectory_usesNsTwoColumnBreakpoint() {
        assertEquals(1, gameFoldSettingsColumns(599))
        assertEquals(2, gameFoldSettingsColumns(600))
        assertEquals(2, gameFoldSettingsColumns(840))
    }

    @Test
    fun settingsContent_usesTheSameHorizontalInsetAsTheTightenedHomeCartridges() {
        assertEquals(145, gamePadHeaderHorizontalInsetDp(screenWidthDp = 922))
    }

    @Test
    fun perGamePages_reuseTheGlobalSettingsHorizontalInset() {
        val geometryClass = Class.forName(
            "app.gamenative.ui.screen.library.OfflineLibraryScreenKt",
        )
        val sharedInset = geometryClass.declaredMethods.singleOrNull {
            it.name == "gamePadOwnedPageHorizontalInsetDp"
        }
        assertTrue("Shared owned-page inset helper must exist", sharedInset != null)
        sharedInset ?: return
        sharedInset.isAccessible = true

        assertEquals(145, sharedInset.invoke(null, 922, true))
        assertEquals(0, sharedInset.invoke(null, 922, false))

        val appScreenSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/library/LibraryAppScreen.kt",
        ).readText()
        val containerSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/component/dialog/ContainerConfigDialog.kt",
        ).readText()
        val offlineDetailsSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt",
        ).readText()
        assertTrue(appScreenSource.contains("gamePadOwnedPageHorizontalInsetDp"))
        assertTrue(containerSource.contains("gamePadOwnedPageHorizontalInsetDp"))
        assertTrue(offlineDetailsSource.contains("detailsHorizontalInsetDp"))
        assertTrue(offlineDetailsSource.contains("padding(horizontal = detailsHorizontalInsetDp.dp)"))

        assertFalse(
            "Container editor must keep its top bar full width",
            containerSource.contains("editorModifier.padding(horizontal = editorHorizontalInsetDp.dp)"),
        )
        assertTrue(
            "Only the container tabs and content should receive the shared inset",
            containerSource.contains("editorHorizontalInsetDp.dp"),
        )
    }

    @Test
    fun emulatorPresetEditors_reuseTheContainerEditorInsetWithoutNarrowingTopBars() {
        val box64Source = sourceFile(
            "app/src/main/java/app/gamenative/ui/component/dialog/Box64PresetsDialog.kt",
        ).readText()
        val fexcoreSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/component/dialog/FEXCorePresetsDialog.kt",
        ).readText()

        listOf(box64Source, fexcoreSource).forEach { source ->
            assertTrue(source.contains("GamePadPresetEditorScaffold"))
            assertFalse(
                "Preset top bars must remain full width",
                source.contains("CenterAlignedTopAppBar(\n                            modifier"),
            )
        }
    }

    @Test
    fun defaultContainerEditor_reusesParentWidthWithoutSecondInset() {
        val geometryClass = Class.forName(
            "app.gamenative.ui.component.dialog.ContainerConfigDialogKt",
        )
        val inset = geometryClass.declaredMethods.singleOrNull {
            it.name == "containerEditorHorizontalInsetDp"
        }
        assertTrue("Container editor inset policy must exist", inset != null)
        inset ?: return
        inset.isAccessible = true

        assertEquals(0, inset.invoke(null, 922, true, true))
        assertEquals(145, inset.invoke(null, 922, true, false))
        assertEquals(0, inset.invoke(null, 922, false, true))
    }

    @Test
    fun gamePadPresetEditors_useTheLightSettingsPalette() {
        val themeClass = runCatching {
            Class.forName("app.gamenative.ui.component.dialog.PresetEditorThemeKt")
        }.getOrNull()
        assertTrue("Shared preset editor theme policy must exist", themeClass != null)
        themeClass ?: return
        val policy = themeClass.declaredMethods.singleOrNull {
            it.name == "gamePadPresetEditorUsesLightPalette"
        }
        assertTrue("Preset editor theme policy must be callable", policy != null)
        policy ?: return
        policy.isAccessible = true

        assertEquals(true, policy.invoke(null, true))
        assertEquals(false, policy.invoke(null, false))
    }

    @Test
    fun controllerBackFromDetail_returnsOneLevelOnly() {
        assertEquals(SettingsBackAction.SHOW_SETTINGS_HOME, settingsBackAction(onSettingsHome = false))
        assertEquals(SettingsBackAction.EXIT_SETTINGS, settingsBackAction(onSettingsHome = true))
    }

    @Test
    fun aboutPage_hostsItsXmlContentUnderTheSharedSettingsHeader() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/settings/GameFoldAboutPage.kt",
        ).readText()
        val settingsSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/settings/SettingsScreen.kt",
        ).readText()

        assertTrue(source.contains("AndroidView"))
        assertTrue(source.contains("R.layout.fragment_about"))
        assertTrue(source.contains("modifier = modifier.fillMaxSize()"))
        assertFalse(source.contains("MaterialToolbar"))
        assertFalse(source.contains("toolbar.title"))
        assertFalse(settingsSource.contains("if (!(localShell && localPage == LocalSettingsPage.ABOUT))"))
        assertTrue(settingsSource.contains("LocalSettingsPage.ABOUT -> \"关于\""))
        assertFalse(source.contains("GameFoldPixelWordmark"))
        assertFalse(source.contains("AboutActionCard"))
        assertFalse(source.contains("gameFoldAboutLayoutMode"))
    }

    @Test
    fun aboutPage_usesThePcCompatibilityLayerDisclaimer() {
        val source = sourceFile(
            "app/src/main/res/values-zh-rCN/gamefold_about_resources.xml",
        ).readText()

        assertTrue(source.contains("基于 Wine、Winlator 及相关开源兼容层"))
        assertFalse(source.contains("Eden"))
    }

    @Test
    fun launcherLabel_usesIndependentGamePadPcTitle() {
        sourceFile("app/src/main/res")
            .walkTopDown()
            .filter { it.name == "strings.xml" }
            .map { it.readText() }
            .filter { it.contains("<string name=\"app_name\">") }
            .forEach { source ->
                assertTrue(source.contains("<string name=\"app_name\">GamePad PC</string>"))
                assertFalse(source.contains("<string name=\"app_name\">GameFold PC</string>"))
                assertFalse(source.contains("<string name=\"app_name\">Game Fold PC</string>"))
                assertFalse(source.contains("<string name=\"app_name\">GameNative</string>"))
            }
    }

    @Test
    fun launcherIcon_usesTheProvidedPcArtworkAcrossAllAliases() {
        val expectedArtworkSha256 =
            "69f38d6bc63a3ca1958d428305b2d45b07f5185360c3fdcfb4e647b9d9982aba"
        val artwork = sourceFile(
            "app/src/main/res/drawable-nodpi/gamefold_pc_app_icon.png",
        ).readBytes()
        val actualArtworkSha256 = MessageDigest.getInstance("SHA-256")
            .digest(artwork)
            .joinToString("") { byte -> "%02x".format(byte) }
        assertEquals(expectedArtworkSha256, actualArtworkSha256)

        val resources = sourceFile("app/src/main/res")
        listOf("mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi")
            .forEach { folder ->
                listOf("ic_launcher", "ic_launcher_round", "ic_launcher_alt", "ic_launcher_alt_round")
                    .forEach { resource ->
                        assertTrue(java.io.File(resources, "$folder/$resource.png").isFile)
                    }
            }
        listOf("mipmap-anydpi", "mipmap-anydpi-v26", "mipmap-anydpi-v33").forEach { folder ->
            listOf("ic_launcher", "ic_launcher_round", "ic_launcher_alt", "ic_launcher_alt_round")
                .forEach { resource ->
                    assertFalse(java.io.File(resources, "$folder/$resource.xml").exists())
                }
        }
    }

    @Test
    fun gamePadPcV24_usesIndependentVersionAndKeepsRunAsEnabled() {
        assertEquals("2.4", BuildConfig.VERSION_NAME)
        assertEquals(10033, BuildConfig.VERSION_CODE)
        assertTrue(BuildConfig.GAMEFOLD_PC_RUN_AS_ENABLED)
    }
}
