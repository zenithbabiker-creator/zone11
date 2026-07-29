-keep class com.google.ar.** { *; }
-keep class com.huawei.hms.arengine.** { *; }
-keep class com.huawei.hiar.** { *; }
-dontwarn com.huawei.**
-dontwarn com.google.ar.**

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
