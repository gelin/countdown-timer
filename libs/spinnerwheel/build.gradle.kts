plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "antistatic.spinnerwheel"
    compileSdk = 36

    defaultConfig {
        minSdk = 14
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-project.txt")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(files("libs/nineoldandroids-2.2.0.jar"))
}
