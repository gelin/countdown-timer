plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ru.gelin.android.countdown"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.gelin.android.countdown"
        minSdk = 14
        targetSdk = 36
        versionCode = 3
        versionName = "0.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
}
