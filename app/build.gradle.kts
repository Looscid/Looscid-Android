plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// The app's version is the Looscid web version it was built alongside
// (YEAR.FEATURES.FIXES, see app/version.json in Looscid/Looscid).
// Change only this line for a release.
val looscidVersion = "2026.138.57"

// Android only installs an update when versionCode goes up, so it's made
// from the version: YEAR * 1,000,000 + FEATURES * 1,000 + FIXES.
// 2026.138.57 -> 2026138057. Every newer version gets a bigger number,
// and it's way above 0.1.0's code (1).
fun versionCodeOf(version: String): Int {
    val (year, features, fixes) = version.split(".").map { it.toInt() }
    require(features < 1000 && fixes < 1000) { "FEATURES and FIXES must stay under 1000" }
    return year * 1_000_000 + features * 1_000 + fixes
}

android {
    namespace = "social.looscid.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "social.looscid.app"
        minSdk = 26
        targetSdk = 35
        // Both come from looscidVersion at the top of this file.
        versionCode = versionCodeOf(looscidVersion)
        versionName = looscidVersion
    }

    signingConfigs {
        // A shared debug key that lives in this repo, so every build (yours
        // and GitHub's) can update the one before it. Release signing is a
        // later step, see the README.
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
