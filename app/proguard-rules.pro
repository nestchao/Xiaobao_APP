# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Keep Moshi generated adapters
-keep class com.xiaobaotv.app.data.model.** { *; }
-keepclassmembers class com.xiaobaotv.app.data.model.** { *; }

# Keep Moshi annotations
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep Retrofit interfaces
-keep,allowobfuscation interface com.xiaobaotv.app.data.remote.** { *; }

# Keep Jsoup (reflection-based)
-keep class org.jsoup.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
