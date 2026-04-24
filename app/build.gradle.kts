import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.aboutlibraries.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "co.adityarajput.mincal"
    compileSdk = 36
    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "co.adityarajput.mincal"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["allowBackup"] = false
            resValue("string", "app_name_launcher", "MinCal Debug")
        }
        create("nightly") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            applicationIdSuffix = ".nightly"
            manifestPlaceholders["allowBackup"] = true
            resValue("string", "app_name_launcher", "MinCal Nightly")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard/nightly.pro",
            )
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["allowBackup"] = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard/release.pro",
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.aboutlibraries.compose)
    implementation(libs.google.crypto.tink)
    implementation(libs.squareup.okhttp)
    implementation(libs.acra.dialog)
    implementation(libs.acra.mail)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
