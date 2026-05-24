# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit interfaces
-keep interface com.example.concentradospt.data.network.** { *; }
-keep interface com.example.concentradospt.data.network.admin.** { *; }

# Data models used by Gson/Retrofit
-keep class com.example.concentradospt.data.model.** { *; }

# Amplify
-keep class com.amplifyframework.** { *; }
-dontwarn com.amplifyframework.**

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
