# KinesteX SDK Consumer ProGuard Rules
# These rules are automatically applied to consuming applications

# Keep JavaScript interface for WebView communication
-keepclassmembers class com.kinestex.kinestexsdkkotlin.core.KinesteXWebViewController$MessageHandler {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep all WebViewMessage classes for proper message handling
-keep class com.kinestex.kinestexsdkkotlin.models.WebViewMessage { *; }
-keep class com.kinestex.kinestexsdkkotlin.models.WebViewMessage$** { *; }

# Keep all model classes
-keep class com.kinestex.kinestexsdkkotlin.models.** { *; }

# Keep public SDK API
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

-keep public class com.kinestex.kinestexsdkkotlin.legacy.KinesteXSDKLegacy {
    public <methods>;
}

-keep public class com.kinestex.kinestexsdkkotlin.core.GenericWebView {
    public <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}