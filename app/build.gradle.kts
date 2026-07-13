import java.util.Properties
import java.io.FileInputStream
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val DebugAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
val DebugBannerAdUnitId = "ca-app-pub-3940256099942544/9214589741"
val DebugNativeAdUnitId = "ca-app-pub-3940256099942544/2247696110"
val ReleaseAdMobAppId = "ca-app-pub-1638673809508848~3940017763"
val ReleaseBannerAdUnitId = "ca-app-pub-1638673809508848/3437200595"
val ReleaseNativeAdUnitId = "ca-app-pub-1638673809508848/4367138885"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Load signing config from key.properties (kept in repo root per project requirement).
val keystoreProperties = Properties().apply {
    val keystorePropertiesFile = rootProject.file("key.properties")
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

android {
    namespace = "com.samoondigital.yojnaplus"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.samoondigital.yojnaplus"
        minSdk = 26
        targetSdk = 35
        versionCode = 19
        versionName = "3.3.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.containsKey("storeFile")) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String?
                keyAlias = keystoreProperties["keyAlias"] as String?
                keyPassword = keystoreProperties["keyPassword"] as String?
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            buildConfigField("String", "ADMOB_APP_ID", "\"$DebugAdMobAppId\"")
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$DebugBannerAdUnitId\"")
            buildConfigField("String", "ADMOB_NATIVE_AD_UNIT_ID", "\"$DebugNativeAdUnitId\"")
            buildConfigField("boolean", "ADMOB_USES_TEST_ADS", "true")
            manifestPlaceholders["admobAppId"] = DebugAdMobAppId
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "ADMOB_APP_ID", "\"$ReleaseAdMobAppId\"")
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$ReleaseBannerAdUnitId\"")
            buildConfigField("String", "ADMOB_NATIVE_AD_UNIT_ID", "\"$ReleaseNativeAdUnitId\"")
            buildConfigField("boolean", "ADMOB_USES_TEST_ADS", "false")
            manifestPlaceholders["admobAppId"] = ReleaseAdMobAppId
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Core / Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.pdfium.android)
    implementation(libs.mlkit.text.recognition.devanagari)
    implementation(libs.play.services.ads)
    implementation(libs.ump)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
