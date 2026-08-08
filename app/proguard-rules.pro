# Add project specific ProGuard rules here.

# Agora RTC SDK
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# Moshi & JSON models
-keep class com.example.model.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-dontwarn com.squareup.moshi.**

# Retrofit & OkHttp
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

