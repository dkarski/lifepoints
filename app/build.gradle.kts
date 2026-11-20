import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing: the keystore lives outside the repo; paths/passwords come from local.properties.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val releaseStoreFile: String? = localProps.getProperty("release.storeFile")

android {
    namespace = "dev.dkarski.tcglivecounter"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.dkarski.lifepoints"
        minSdk = 31 // Mudita Kompakt: MuditaOS K, AOSP 12 (API 31)
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"
        // Only the app's 63 languages (data/AppLanguage.kt): drops AndroidX translations for other languages,
        // so library texts never appear in a language the app does not offer. "zh" keeps the libraries' Chinese.
        resourceConfigurations += listOf(
            "en", "it", "pl", "cs", "pt", "es", "de", "fr", "nl",
            "in", "ms", "ca", "da", "et", "b+fil", "tl", "gl", "hr", "is", "sw", "lv", "lt", "hu", "nb", "ro", "rm",
            "sk", "sl", "fi", "sv", "vi", "tr", "el", "be", "bg", "kk", "ky", "mk", "mn", "ru", "sr", "uk",
            "ka", "iw", "ur", "fa", "ne", "mr", "hi", "pa", "gu", "ta", "te", "kn", "ml", "si",
            "th", "lo", "my", "km", "zh", "b+zh+Hans", "b+zh+Hant", "ja", "ko",
        )
    }

    lint {
        // Every language must stay complete; scripts/i18n_check.py checks the same plus placeholders.
        error += listOf("MissingTranslation", "ExtraTranslation")
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = localProps.getProperty("release.storePassword")
                keyAlias = localProps.getProperty("release.keyAlias")
                keyPassword = localProps.getProperty("release.keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Separate package: the ADB debug build and the InkApps release build can coexist
            // even though they are signed with different keys.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
            // Do not embed the Git commit hash (META-INF/version-control-info.textproto) in the published APK.
            vcsInfo.include = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = false
        aidl = false
        renderScript = false
        shaders = false
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "DebugProbesKt.bin",
            "kotlin-tooling-metadata.json",
            
            "META-INF/*.version",
            "META-INF/com/android/build/gradle/app-metadata.properties",
            "kotlin/**.kotlin_builtins",
        )
    }
}

// APK name: lifepoints-<versionName>-<buildType>.apk.
// Android itself ignores the file name; updates are matched by package + versionCode.
base {
    archivesName = "lifepoints-${android.defaultConfig.versionName}"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.mudita.mmd)
    implementation(libs.androidx.profileinstaller)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
