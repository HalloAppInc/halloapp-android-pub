# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes Signature

# Crashlytics needs these for deobfuscating crash reports better
-keepattributes SourceFile,LineNumberTable

# https://issuetracker.google.com/issues/154315507 TODO(jack): Remove if fixed by Google
-keep class com.google.crypto.tink.proto.** { *; }

# https://github.com/protocolbuffers/protobuf/issues/6463 TODO(jack): Remove if fixed by Google
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
  <fields>;
}

# https://github.com/terl/lazysodium-android/issues/32 TODO(jack): Remove if fixed by lazysodium
-dontwarn java.awt.*
-keep class com.sun.jna.* { *; }
-keepclassmembers class * extends com.sun.jna.* { public *; }

# https://github.com/sannies/mp4parser/blob/master/isoparser/src/main/java/org/mp4parser/PropertyBoxParserImpl.java
# is loading box classes using reflection.
-keep class org.mp4parser.boxes.** { *; }

-keep class org.webrtc.** { *; }

# Huawei depends on HMS Core SDK not being obfuscated
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
