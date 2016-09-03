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

-keep public class fr.bmartel.android.fadecandy.client.FadecandyClient {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.inter.IFcServerEventListener {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.FadecandyConfig {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.UsbItem {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.constant.Constants {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.inter.IUsbEventListener {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.FadecandyDevice {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.FadecandyColor {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.ServerError {
  public protected *;
}
-keep public class fr.bmartel.android.fadecandy.model.ServiceType {
  public protected *;
}

-keep class fr.bmartel.android.fadecandy.service.FadecandyService { *; }

-keepclassmembers,allowobfuscation class fr.bmartel.android.fadecandy.service.FadecandyService.** {
    <methods>;
}