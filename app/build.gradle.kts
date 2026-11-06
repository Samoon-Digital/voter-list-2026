import java.util.Properties
import java.io.FileInputStream
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

val ProductionPackageName = "com.samoondigital.yojnaplus"
val ProductionAdMobAppId = "ca-app-pub-1638673809508848~3940017763"
val ProductionAppOpenAdUnitId = "ca-app-pub-1638673809508848/5780292909"
val ProductionForegroundAppOpenAdUnitId = "ca-app-pub-1638673809508848/6864651546"
val ProductionBannerAdUnitId = "ca-app-pub-1638673809508848/5540207067"
val ProductionInterstitialAdUnitId = "ca-app-pub-1638673809508848/8518136887"
val DebugAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
val DebugAppOpenAdUnitId = "ca-app-pub-3940256099942544/9257395921"
val DebugForegroundAppOpenAdUnitId = "ca-app-pub-3940256099942544/9257395921"
val DebugBannerAdUnitId = "ca-app-pub-3940256099942544/6300978111"
val DebugInterstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712"

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

val appscopeProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) propertiesFile.inputStream().use { load(it) }
}
val appscopeTrackingKey = providers.gradleProperty("APPSCOPE_TRACKING_KEY")
    .orElse(providers.environmentVariable("APPSCOPE_TRACKING_KEY"))
    .getOrElse(appscopeProperties.getProperty("appscope.tracking.key", ""))
require(appscopeTrackingKey.isEmpty() || appscopeTrackingKey.matches(Regex("[a-f0-9]{48}"))) {
    "APPSCOPE_TRACKING_KEY must be a 48-character hexadecimal tracking key."
}

android {
    namespace = ProductionPackageName
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = ProductionPackageName
        minSdk = 26
        targetSdk = 37
        versionCode = 27
        versionName = "3.3.10"

        buildConfigField("String", "APPSCOPE_ENDPOINT", "\"https://appscope-tracking.alimailsamun.workers.dev/api/track\"")
        buildConfigField("String", "APPSCOPE_APP_ID", "\"app_b8662a3d3b8c9dc5ab23d3f3\"")
        buildConfigField("String", "APPSCOPE_TRACKING_KEY", "\"$appscopeTrackingKey\"")

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
            buildConfigField("String", "ADMOB_FOREGROUND_APP_OPEN_AD_UNIT_ID", "\"$DebugForegroundAppOpenAdUnitId\"")
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$DebugBannerAdUnitId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"$DebugInterstitialAdUnitId\"")
            manifestPlaceholders["admobAppId"] = DebugAdMobAppId
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            buildConfigField("String", "ADMOB_APP_ID", "\"$ProductionAdMobAppId\"")
            buildConfigField("String", "ADMOB_APP_OPEN_AD_UNIT_ID", "\"$ProductionAppOpenAdUnitId\"")
            buildConfigField("String", "ADMOB_FOREGROUND_APP_OPEN_AD_UNIT_ID", "\"$ProductionForegroundAppOpenAdUnitId\"")
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$ProductionBannerAdUnitId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"$ProductionInterstitialAdUnitId\"")
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

androidComponents.onVariants { variant ->
    // Pdf-Viewer 2.4.0 has no decoder hook. Patch only its disk-cache decode call,
    // retaining the upstream renderer, cache policy, lifecycle and zoom behavior.
    variant.instrumentation.transformClassesWith(
        PdfCacheSamplingVisitorFactory::class.java,
        InstrumentationScope.ALL,
    ) {}
    variant.instrumentation.setAsmFramesComputationMode(
        FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS,
    )
}

abstract class PdfCacheSamplingVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun isInstrumentable(classData: ClassData): Boolean =
        classData.className == "com.rajat.pdfviewer.util.CacheManager"

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor =
        object : ClassVisitor(Opcodes.ASM9, nextClassVisitor) {
            private var replacements = 0

            override fun visitMethod(
                access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?,
            ): MethodVisitor {
                val delegate = super.visitMethod(access, name, descriptor, signature, exceptions)
                if (name != "decodeBitmapFromDiskCache") return delegate
                return object : MethodVisitor(Opcodes.ASM9, delegate) {
                    override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
                        if (opcode == Opcodes.INVOKESTATIC && owner == "android/graphics/BitmapFactory" &&
                            name == "decodeFile" && descriptor == "(Ljava/lang/String;)Landroid/graphics/Bitmap;"
                        ) {
                            super.visitVarInsn(Opcodes.ALOAD, 0)
                            super.visitFieldInsn(Opcodes.GETFIELD, "com/rajat/pdfviewer/util/CacheManager", "context", "Landroid/content/Context;")
                            super.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "com/samoondigital/yojnaplus/core/graphics/SampledBitmapDecoder",
                                "decodePdfCacheFile",
                                "(Ljava/lang/String;Landroid/content/Context;)Landroid/graphics/Bitmap;",
                                false,
                            )
                            replacements++
                        } else {
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                        }
                    }
                }
            }

            override fun visitEnd() {
                check(replacements == 1) { "Pdf-Viewer cache decoder changed; review the bitmap sampling bridge." }
                super.visitEnd()
            }
        }
}

dependencies {
    // Core / Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
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

    // PDF viewing
    implementation(libs.afreaky.pdf.viewer)
    implementation(libs.androidx.pdf.viewer.fragment)
    implementation(libs.ads.mobile.sdk)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.ump)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
