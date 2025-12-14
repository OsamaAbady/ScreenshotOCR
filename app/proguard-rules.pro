# Add project specific ProGuard rules here.
# Optimized for minimal offline OCR app

# Keep ML Kit classes (essential for OCR functionality)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep MainActivity and core app classes
-keep class com.example.screenshotocr.MainActivity { *; }
-keep class com.example.screenshotocr.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Minimal coroutines support
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# General Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Optimize aggressively but keep essential classes
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification