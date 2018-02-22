# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class natage.luxbatterysaver.charging.** {
 *;
}
-dontwarn com.squareup.okhttp.**

-keepattributes EnclosingMethod

-dontnote com.google.android.gms.internal.zzry

-keep class android.support.** { *; }

-keep public class com.google.android.gms.ads.** {
  public *;
}

-keep public class com.google.ads.** {
  public *;
}

-keep class com.google.android.gms.ads.** {
   *;
}

-keep class com.google.ads.mediation.admob.AdMobAdapter {
    *;
}

-keep class com.google.ads.mediation.AdUrlAdapter {
    *;
}

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses