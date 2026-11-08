plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "social.looscid.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "social.looscid.app"
        minSdk = 26
        targetSdk = 35
        // Bump BOTH of these for every release. Android only installs an
        // update when versionCode goes up.
        versionCode = 1
        versionName = "0.1.0"
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
