# Bitcoin Dashboard ProGuard Rules

# WebView JavaScript Interface beibehalten
-keepclassmembers class de.chri57i4n.bitcoindashboard.** {
    @android.webkit.JavascriptInterface <methods>;
}

# Androidx WebView
-keep class androidx.webkit.** { *; }

# ViewBinding
-keep class de.chri57i4n.bitcoindashboard.databinding.** { *; }

# Crash-Reports leserlich halten
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Material Components
-keep class com.google.android.material.** { *; }
