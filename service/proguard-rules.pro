# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/abathur/android-sdk-linux/tools/proguard/proguard-android.txt
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
-useuniqueclassmembernames
-keepattributes SourceFile,LineNumberTable
-allowaccessmodification

-keep public class fr.bmartel.android.fadecandy.FadecandyClient {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.IFadecandyListener {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.FadecandyConfig {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.FadecandyDevice {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.FadecandyColor {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.ServerError {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.ServiceType {
  public protected *;
}

-keep class fr.bmartel.android.fadecandy.service.FadecandyService { *; }

-keepclassmembers,allowobfuscation class fr.bmartel.android.fadecandy.service.FadecandyService.** {
    <methods>;
}