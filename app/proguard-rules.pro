# Keep Kotlinx Serialization metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.samoondigital.yojnaplus.**$$serializer { *; }
-keepclassmembers class com.samoondigital.yojnaplus.** {
    *** Companion;
}
-keepclasseswithmembers class com.samoondigital.yojnaplus.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# OkHttp platform
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# WorkManager creates the analytics worker by its persisted class name.
-keep class com.samoondigital.yojnaplus.core.analytics.AppscopeSyncWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
