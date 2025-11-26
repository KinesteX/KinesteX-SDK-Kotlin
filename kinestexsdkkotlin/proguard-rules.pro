# KinesteX SDK ProGuard Rules
# These rules ensure proper functionality when code obfuscation is enabled

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep JavaScript interface for WebView communication
-keepclassmembers class com.kinestex.kinestexsdkkotlin.core.KinesteXWebViewController$MessageHandler {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep all WebViewMessage sealed class and its subclasses
-keep class com.kinestex.kinestexsdkkotlin.models.WebViewMessage { *; }
-keep class com.kinestex.kinestexsdkkotlin.models.WebViewMessage$** { *; }

# Keep all model classes used for JSON serialization
-keep class com.kinestex.kinestexsdkkotlin.models.** { *; }
-keepclassmembers class com.kinestex.kinestexsdkkotlin.models.** { *; }

# Keep public API classes and methods
-keep public class com.kinestex.kinestexsdkkotlin.KinesteXSDK {
    public <methods>;
}

-keep public class com.kinestex.kinestexsdkkotlin.core.KinesteXInitializer {
    public <methods>;
}

-keep public class com.kinestex.kinestexsdkkotlin.core.KinesteXViewBuilder {
    public <methods>;
}

-keep public class com.kinestex.kinestexsdkkotlin.api.KinesteXAPI {
    public <methods>;
}

# Keep legacy SDK for backward compatibility
-keep public class com.kinestex.kinestexsdkkotlin.legacy.KinesteXSDKLegacy {
    public <methods>;
}

# Keep GenericWebView and its methods
-keep public class com.kinestex.kinestexsdkkotlin.core.GenericWebView {
    public <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Gson specific rules (if used for JSON parsing)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# OkHttp specific rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# WebView related
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}