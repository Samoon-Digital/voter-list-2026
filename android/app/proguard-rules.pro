# ProGuard/R8 rules for Flutter app
# Keep Flutter entry points and plugin classes
-keep class io.flutter.embedding.** { *; }
-keep class io.flutter.plugin.** { *; }
-keep class io.flutter.plugins.** { *; }

# Keep Google Mobile Ads and mediation adapters' public APIs
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }
-keep class com.google.android.gms.measurement.** { *; }

# Keep flutter_inappwebview (uses reflection extensively)
-keep class com.pichillilorenzo.flutter_inappwebview.** { *; }
-dontwarn com.pichillilorenzo.flutter_inappwebview.**

# Keep your app's MainActivity and custom factories (native ad factory)
-keep class com.samoondigital.yojnaplus.** { *; }

# Keep Kotlin metadata (helps with reflection)
-keep class kotlin.Metadata { *; }

# Don't warn about Flutter/JNI generated symbols
-dontwarn io.flutter.**
-dontwarn androidx.lifecycle.**
-dontwarn com.google.android.gms.**


