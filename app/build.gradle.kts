import java.util.Properties
import java.io.FileInputStream
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.serialization)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.ksp)
    alias(libs.plugins.secrets.gradle)
    alias(libs.plugins.room)
}

val keystorePropertiesFile = rootProject.file("app/keystores/keystore.properties")
val keystoreProperties: Properties? = if (keystorePropertiesFile.exists()) {
    Properties().apply {
        load(FileInputStream(keystorePropertiesFile))
    }
} else null

val metaAppId: String = project.findProperty("META_APP_ID") as String? ?: System.getenv("META_APP_ID") ?: ""
val productSku: String = project.findProperty("PRODUCT_SKU") as String? ?: System.getenv("PRODUCT_SKU") ?: ""

val gameFoldPcApplicationId =
    "com.gamepad.pc"
val gameFoldPcVersionCode =
    10033
val gameFoldPcVersionName =
    "2.4"
val gameFoldPcLocalShell =
    providers.gradleProperty("GAMEFOLD_PC_LOCAL_SHELL").orNull?.toBooleanStrictOrNull() ?: true
val gameFoldPcRunAsEnabled =
    providers.gradleProperty("GAMEFOLD_PC_RUN_AS_ENABLED").orNull?.toBooleanStrictOrNull() ?: true
val gameFoldPcSupportedLegacyTestClasses = listOf(
    "app.gamenative.data.ShooterModeConfigTest",
    "app.gamenative.data.TouchGestureConfigTest",
    "app.gamenative.enums.PathTypeTest",
    "app.gamenative.offline.*",
    "app.gamenative.product.*",
    "app.gamenative.gamepad.*",
    "app.gamenative.displayresize.*",
    "app.gamenative.gamefold.startup.*",
    "app.gamenative.gamefold.rear.GameFoldRearDisplayPolicyTest",
    "app.gamenative.gamefold.rear.GameFoldOrientationTest",
    "app.gamenative.gamefold.rear.RearTouchInputRouterTest",
    "app.gamenative.gamefold.gyro.*",
    "app.gamenative.gamefold.session.GameFoldSessionPolicyTest",
    "app.gamenative.gamefold.ui.GameFoldControllerInputTest",
    "app.gamenative.gamefold.ui.GameFoldPcFrontendPolicyTest",
    "app.gamenative.service.SdCardDetectionTest",
    "app.gamenative.ui.component.FpsLimiterUtilsTest",
    "app.gamenative.ui.component.dialog.ContainerConfigDialogContainerUpdateTest",
    "app.gamenative.ui.component.dialog.PresetEditorThemeTest",
    "app.gamenative.ui.model.LibrarySortUtilsTest",
    "app.gamenative.ui.screen.library.OfflineLibraryGeometryTest",
    "app.gamenative.ui.screen.settings.GameFoldAboutPageInflationTest",
    "app.gamenative.ui.screen.settings.SettingsGeometryTest",
    "app.gamenative.utils.CaseInsensitiveFileSystemTest",
    "app.gamenative.utils.CaseInsensitivePathResolutionTest",
    "app.gamenative.utils.ContainerStorageManagerTest",
    "app.gamenative.utils.CustomGameArtworkBoundaryTest",
    "app.gamenative.utils.CustomGameScannerFirstScanTest",
    "app.gamenative.utils.DateTimeUtilsTest",
    "app.gamenative.utils.GtaFullscreenConfigTest",
    "app.gamenative.utils.GameFoldPcResourceRootTest",
    "app.gamenative.utils.KeyValueUtilsTest",
    "app.gamenative.utils.LocalGameArtworkResolverTest",
    "app.gamenative.utils.LocalGameLaunchAnalyzerTest",
    "app.gamenative.utils.LocalGameExecutableResolverTest",
    "app.gamenative.utils.PortableExecutableInspectorTest",
    "app.gamenative.utils.LsfgDllLocatorTest",
    "app.gamenative.utils.StringUtilsTest",
    "app.gamenative.utils.launchdependencies.BionicDefaultProtonDependencyTest",
    "app.gamenative.utils.OpenALStepTest",
    "app.gamenative.utils.PhysXStepTest",
    "app.gamenative.utils.VcRedistStepTest",
    "app.gamenative.utils.XnaFrameworkStepTest",
    "com.winlator.core.*",
    "com.winlator.widget.FrameRateSamplerTest",
    "com.winlator.widget.FrameRatingPresentedCounterTest",
    "com.winlator.xserver.*",
    "com.winlator.xenvironment.*",
)
val gameFoldPcNativePrune =
    providers.gradleProperty("GAMEFOLD_PC_NATIVE_PRUNE").orNull?.toBooleanStrictOrNull()
        ?: gameFoldPcLocalShell
val gameFoldPcLocalWineFastPath =
    providers.gradleProperty("GAMEFOLD_PC_LOCAL_WINE_FAST_PATH").orNull?.toBooleanStrictOrNull()
        // The reduced helper changes R8 reachability across the Wine/Proton
        // launch graph and was proven on-device to terminate GTA5 early.
        // Keep the established command path unless explicitly diagnosing it.
        ?: false
val gameFoldPcDefaultLsfgDllPath =
    "/storage/emulated/0/GamePadPC/lsfg/inputtest/Lossless.dll"
val requestedGameFoldPcLsfgDllPath =
    providers.gradleProperty("GAMEFOLD_PC_LSFG_LOCAL_DLL_PATH").orNull?.trim().orEmpty()
val gameFoldPcLsfgLocalDllPath = when {
    !gameFoldPcLocalShell -> requestedGameFoldPcLsfgDllPath
    requestedGameFoldPcLsfgDllPath.isEmpty() -> gameFoldPcDefaultLsfgDllPath
    requestedGameFoldPcLsfgDllPath.startsWith("/storage/emulated/0/GamePadPC/") ->
        requestedGameFoldPcLsfgDllPath
    else -> error(
        "GAMEFOLD_PC_LSFG_LOCAL_DLL_PATH must be an Android GamePadPC public path, " +
            "not a build-host path: $requestedGameFoldPcLsfgDllPath",
    )
}

val gameFoldPcModernAssetsDir = layout.buildDirectory.dir("generated/gamefoldPcModernAssets")
val gameFoldPcModernAssets by tasks.registering(Sync::class) {
    from(rootProject.file("app/src/main/assets"))
    from(rootProject.file("app/src/modern/assets"))
    into(gameFoldPcModernAssetsDir)
    exclude("imagefs_bionic.txz")
    exclude("imagefs_gamenative.txz")
    exclude("imagefs_patches_gamenative.tzst")
    exclude("proton-*.wcp")
    exclude("proton-*_container_pattern.tzst")
    exclude("extras.tzst")
    exclude("redirect.tzst")
    exclude("container_pattern_*.tzst")
    exclude("experimental-drm-*.tzst")
    exclude("arm64ec_input_dlls.tzst")
    exclude("x86_64_input_dlls.tzst")
    exclude("pulseaudio-*.tzst")
    exclude("box86_64/**/*.tzst")
    exclude("fexcore/**/*.tzst")
    exclude("wowbox64/**/*.tzst")
    exclude("dxwrapper/**/*.tzst")
    exclude("steampipe/**/*.dll")
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Debug-only: package the repo's manifest.json so debug builds read it locally (never in release).
val copyDebugManifest by tasks.registering(Copy::class) {
    from(rootProject.file("manifest.json"))
    into(layout.buildDirectory.dir("generated/debugManifest"))
}

android {
    namespace = "app.gamenative"
    compileSdk = 36

    // https://developer.android.com/ndk/downloads
    ndkVersion = "27.3.13750724"

    signingConfigs {
        create("pluvia") {
            if (keystoreProperties != null) {
                storeFile = file(keystoreProperties["storeFile"].toString())
                storePassword = keystoreProperties["storePassword"].toString()
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
            }
        }
    }

    defaultConfig {
        applicationId = gameFoldPcApplicationId

        minSdk = 26

        manifestPlaceholders["screenOrientation"] = "landscape"
        manifestPlaceholders["gameFoldPcRunAsEnabled"] = false
        buildConfigField("boolean", "XR_BUILD", "false")
        buildConfigField("boolean", "MODERN_XR", "false")

        versionCode = gameFoldPcVersionCode
        versionName = gameFoldPcVersionName

        buildConfigField("boolean", "GAMEFOLD_PC_LAZY", "true")
        buildConfigField("boolean", "GAMEFOLD_PC_LOCAL_SHELL", gameFoldPcLocalShell.toString())
        buildConfigField(
            "boolean",
            "GAMEFOLD_PC_RUN_AS_ENABLED",
            gameFoldPcRunAsEnabled.toString(),
        )
        buildConfigField(
            "boolean",
            "GAMEFOLD_PC_LOCAL_WINE_FAST_PATH",
            gameFoldPcLocalWineFastPath.toString(),
        )
        buildConfigField(
            "String",
            "GAMEFOLD_PC_LSFG_LOCAL_DLL_PATH",
            "\"$gameFoldPcLsfgLocalDllPath\"",
        )

        buildConfigField("boolean", "GOLD", "false")
        fun secret(name: String) =
            project.findProperty(name) as String? ?: System.getenv(name) ?: ""

        buildConfigField("String", "STEAMGRIDDB_API_KEY", "\"${secret("STEAMGRIDDB_API_KEY")}\"")
        buildConfigField("String", "CLOUD_PROJECT_NUMBER", "\"${secret("CLOUD_PROJECT_NUMBER")}\"")
        val iconValue = "@drawable/gamefold_pc_app_icon"
        val iconRoundValue = "@drawable/gamefold_pc_app_icon"
        manifestPlaceholders.putAll(
            mapOf(
                "icon" to iconValue,
                "roundIcon" to iconRoundValue,
            ),
        )

        ndk {
            //abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }

        // Localization support - specify which languages to include
        resourceConfigurations += listOf(
            "en",      // English (default)
            "es",      // Spanish
            "da",      // Danish
            "pt-rBR",  // Portuguese (Brazilian)
            "zh-rTW",  // Traditional Chinese
            "zh-rCN",  // Simplified Chinese
            "fr",      // French
            "de",      // German
            "uk",      // Ukrainian
            "it",      // Italian
            "ro",      // Română
            "pl",      // Polish
            "ru",      // Russian
            "ko",      // Korean
            "ja",      // Japanese
            // TODO: Add more languages here using the ISO 639-1 locale code with regional qualifiers (e.g., "pt-rPT" for European Portuguese)
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        proguardFiles(
            // getDefaultProguardFile("proguard-android-optimize.txt"),
            getDefaultProguardFile("proguard-android.txt"),
            "proguard-rules.pro",
        )
    }

    flavorDimensions += "androidApi"
    productFlavors {
        create("legacy") {
            dimension = "androidApi"
            targetSdk = 28
            ndk.abiFilters += if (gameFoldPcLocalShell && gameFoldPcNativePrune) {
                listOf("arm64-v8a")
            } else {
                listOf("arm64-v8a", "armeabi-v7a")
            }
            buildConfigField("boolean", "MODERN_ANDROID", "false")
            buildConfigField("String", "PRELOAD_BIONIC_SO", "\"libredirect-bionic.so\"")
        }
        create("legacyXr") {
            dimension = "androidApi"
            targetSdk = 28
            ndk.abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            buildConfigField("boolean", "MODERN_ANDROID", "false")
            buildConfigField("String", "PRELOAD_BIONIC_SO", "\"libredirect-bionic.so\"")
            buildConfigField("boolean", "XR_BUILD", "true")
            manifestPlaceholders["screenOrientation"] = "landscape"
        }
        create("modern") {
            dimension = "androidApi"
            minSdk = 29
            targetSdk = 36
            ndk.abiFilters += listOf("arm64-v8a")
            buildConfigField("boolean", "MODERN_ANDROID", "true")
            buildConfigField("String", "PRELOAD_BIONIC_SO", "\"libredirect-bionic-wx.so\"")
        }
        create("modernXr") {
            dimension = "androidApi"
            minSdk = 29
            targetSdk = 36
            ndk.abiFilters += listOf("arm64-v8a")
            buildConfigField("boolean", "MODERN_ANDROID", "true")
            buildConfigField("String", "PRELOAD_BIONIC_SO", "\"libredirect-bionic-wx.so\"")
            buildConfigField("boolean", "XR_BUILD", "true")
            buildConfigField("boolean", "MODERN_XR", "true")
            buildConfigField("String", "META_APP_ID", "\"$metaAppId\"")
            buildConfigField("String", "PRODUCT_SKU", "\"$productSku\"")
            manifestPlaceholders["screenOrientation"] = "landscape"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            manifestPlaceholders["gameFoldPcRunAsEnabled"] = true
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            // Internal diagnostics may explicitly enable run-as without disabling Release optimizations.
            manifestPlaceholders["gameFoldPcRunAsEnabled"] = gameFoldPcRunAsEnabled
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
        }
        create("release-signed") {
            manifestPlaceholders["gameFoldPcRunAsEnabled"] = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("pluvia")
        }
        create("release-gold") {
            manifestPlaceholders["gameFoldPcRunAsEnabled"] = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("pluvia")
            applicationIdSuffix = ".gold"
            buildConfigField("boolean", "GOLD", "true")
            val iconValue = "@mipmap/ic_launcher_gold"
            val iconRoundValue = "@mipmap/ic_launcher_gold_round"
            manifestPlaceholders.putAll(
                mapOf(
                    "icon" to iconValue,
                    "roundIcon" to iconRoundValue,
                ),
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/DebugProbesKt.bin"
            excludes += "/junit/runner/smalllogo.gif"
            excludes += "/junit/runner/logo.gif"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
        jniLibs {
            // 'extractNativeLibs' was not enough to keep the jniLibs and
            // the libs went missing after adding on-demand feature delivery
            useLegacyPackaging = true
            if (gameFoldPcLocalShell && gameFoldPcNativePrune) {
                excludes += setOf(
                    "**/libpatchelf.so",
                    "**/libopenxr_loader.so",
                )
            }
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        // Locale files ship full AndroidX appcompat (abc_*) translations that aren't in the
        // default locale. These extra translations are harmless and pre-existing; without this
        // the release-only lintVital pass fails on 150+ ExtraTranslation errors.
        disable += "ExtraTranslation"
    }
    dynamicFeatures += setOf(":ubuntufs")

    // Configure Assets to be used in different variants
    sourceSets {
        getByName("main") {
            assets.setSrcDirs(emptyList<String>())
        }
        getByName("legacy") {
            java.srcDir("src/nonXr/java")
            assets {
                setSrcDirs(listOf(gameFoldPcModernAssetsDir))
            }
            jniLibs {
                srcDirs("src/legacy/jniLibs")
            }
        }
        getByName("legacyXr") {
            java.srcDir("src/nonXr/java")
            manifest.srcFile("src/legacy/AndroidManifest.xml")
            assets {
                srcDirs("src/legacy/assets", "src/main/assets")
            }
            jniLibs {
                srcDirs("src/legacy/jniLibs")
            }
        }
        getByName("modern") {
            java.srcDir("src/nonXr/java")
            assets {
                setSrcDirs(listOf(gameFoldPcModernAssetsDir))
            }
        }
        getByName("modernXr") {
            assets {
                setSrcDirs(listOf(gameFoldPcModernAssetsDir))
            }
            jniLibs {
                srcDirs("src/modern/jniLibs")
            }
        }
        getByName("debug") {
            assets.srcDir(copyDebugManifest)
        }
    }

    tasks.configureEach {
        if (name != "gameFoldPcModernAssets" &&
            (name.contains("Modern", ignoreCase = true) || name.contains("Legacy", ignoreCase = true)) &&
            (name.contains("Assets", ignoreCase = true) || name.contains("Lint", ignoreCase = true))) {
            dependsOn(gameFoldPcModernAssets)
        }
    }

    kotlinter {
        ignoreFormatFailures  = false
    }

    // externalNativeBuild {
    //   cmake {
    //       path = file("src/main/cpp/asurfacerenderer/CMakeLists.txt")
    //   }
    // }

    // externalNativeBuild {
    //    cmake {
    //        path = file("src/main/cpp/evshim/CMakeLists.txt")
    //    }
    // }

    // xconnectorpatch is shipped as a prebuilt jniLib because our APK packaging flow
    // does not rebuild native libraries during release creation.
    // externalNativeBuild {
    //     cmake {
    //         path = file("src/main/cpp/xconnectorpatch/CMakeLists.txt")
    //         version = "3.22.1"
    //     }
    // }

    // build extras needed in libwinlator_bionic.so
    // externalNativeBuild {
    //     cmake {
    //         path = file("src/main/cpp/extras/CMakeLists.txt")   // the file shown above
    //         version = "3.22.1"
    //     }
    // }

    // cmake on release builds a proot that fails to process ld-2.31.so
    // externalNativeBuild {
    //     cmake {
    //         path = file("src/main/cpp/CMakeLists.txt")
    //         version = "3.22.1"
    //     }
    // }

    // (For now) Uncomment for LeakCanary to work.
    // configurations {
    //     debugImplementation {
    //         exclude(group = "junit", module = "junit")
    //     }
    // }
}

tasks.withType<Test>().configureEach {
    if (gameFoldPcLocalShell && name == "testLegacyReleaseUnitTest") {
        filter {
            gameFoldPcSupportedLegacyTestClasses.forEach(::includeTestsMatching)
            isFailOnNoMatchingTests = true
        }
    }
}

dependencies {
    implementation(libs.material)

    // JavaSteam remains on the build classpath while the upstream source tree is
    // being migrated; R8 removes it completely from the local-shell runtime APK.
    val localBuild = false // Change to 'true' needed when building JavaSteam manually
    if (localBuild) {
        implementation(files("../../JavaSteam/build/libs/javasteam-1.8.0.1-22-SNAPSHOT.jar"))
        implementation(files("../../JavaSteam/javasteam-depotdownloader/build/libs/javasteam-depotdownloader-1.8.0.1-22-SNAPSHOT.jar"))
        implementation(libs.bundles.javasteam.dev)
    } else {
        implementation(libs.javasteam) {
            isChanging = version?.contains("SNAPSHOT") ?: false
        }
        implementation(libs.javasteam.depotdownloader) {
            isChanging = version?.contains("SNAPSHOT") ?: false
        }
    }
    implementation(libs.spongycastle)
    implementation(libs.okhttp.dnsoverhttps)

    // Split Modules
    implementation(libs.bundles.google)

    // Winlator
    implementation(libs.bundles.winlator)
    implementation(libs.libarchive.android)
    implementation(libs.zstd.jni) { artifact { type = "aar" } }
    implementation(libs.xz)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.landscapist.coil)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    debugImplementation(libs.androidx.ui.tooling)

    // Support
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.apng)
    implementation(libs.datastore.preferences)
    implementation(libs.jetbrains.kotlinx.json)
    implementation(libs.kotlin.coroutines)
    implementation(libs.timber)
    implementation(libs.zxing)

    // Google Protobufs
    implementation(libs.protobuf.java)

    // Hilt
    implementation(libs.bundles.hilt)

    // KSP (Hilt, Room)
    ksp(libs.bundles.ksp)

    // Room Database
    implementation(libs.bundles.room)

    // Memory Leak Detection
    // debugImplementation("com.squareup.leakcanary:leakcanary-android:3.0-alpha-8")

    // Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.zstd.jni)
    testImplementation(libs.orgJson)
    testImplementation(libs.mockwebserver)

    "modernXrImplementation"("com.meta.horizon.platform.sdk:core-kotlin:0.2.2")
    "modernXrImplementation"("com.meta.horizon.platform.sdk:iap-kotlin:0.2.2")
}
