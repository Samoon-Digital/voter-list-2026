import java.util.Properties
import java.io.FileInputStream
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ProductionPackageName = "com.samoondigital.yojnaplus"
val ProductionAdMobAppId = "ca-app-pub-1638673809508848~3940017763"
val ProductionAppOpenAdUnitId = "ca-app-pub-1638673809508848/5780292909"
val ProductionBannerAdUnitId = "ca-app-pub-1638673809508848/5540207067"
val ProductionInterstitialAdUnitId = "ca-app-pub-1638673809508848/8518136887"
val ProductionNativeAdUnitId = "ca-app-pub-1638673809508848/3565193102"
val DebugAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
val DebugAppOpenAdUnitId = "ca-app-pub-3940256099942544/9257395921"
val DebugBannerAdUnitId = "ca-app-pub-3940256099942544/6300978111"
val DebugInterstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712"
val DebugNativeAdUnitId = "ca-app-pub-3940256099942544/2247696110"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

// Load signing config from key.properties (kept in repo root per project requirement).
val keystoreProperties = Properties().apply {
    val keystorePropertiesFile = rootProject.file("key.properties")
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

android {
    namespace = ProductionPackageName
    compileSdk = 36
    compileSdkExtension = 19

    defaultConfig {
        applicationId = ProductionPackageName
        minSdk = 26
        targetSdk = 36
        versionCode = 22
        versionName = "3.3.5"

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
            isMinifyEnabled = false
            buildConfigField("String", "ADMOB_APP_ID", "\"$DebugAdMobAppId\"")
            buildConfigField("String", "ADMOB_APP_OPEN_AD_UNIT_ID", "\"$DebugAppOpenAdUnitId\"")
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$DebugBannerAdUnitId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"$DebugInterstitialAdUnitId\"")
            buildConfigField("String", "ADMOB_NATIVE_AD_UNIT_ID", "\"$DebugNativeAdUnitId\"")
            manifestPlaceholders["admobAppId"] = DebugAdMobAppId
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "ADMOB_APP_ID", "\"$ProductionAdMobAppId\"")
            buildConfigField("String", "ADMOB_APP_OPEN_AD_UNIT_ID", "\"$ProductionAppOpenAdUnitId\"")
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$ProductionBannerAdUnitId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"$ProductionInterstitialAdUnitId\"")
            buildConfigField("String", "ADMOB_NATIVE_AD_UNIT_ID", "\"$ProductionNativeAdUnitId\"")
            manifestPlaceholders["admobAppId"] = ProductionAdMobAppId
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
    implementation(libs.androidx.appcompat)

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
    implementation(libs.afreaky.pdf.viewer)
    implementation(libs.androidx.pdf.viewer.fragment)
    implementation(libs.play.services.ads)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.ump)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
