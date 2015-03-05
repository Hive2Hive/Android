# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
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

-dontwarn javax.mail.**
-dontwarn javax.swing.**
-dontwarn javax.naming.**
-dontwarn sun.misc.Unsafe
-dontwarn sun.misc.Cleaner
-dontwarn sun.nio.ch.FileChannelImpl
-dontwarn sun.reflect.ReflectionFactory
-dontwarn java.nio.channels.**
-dontwarn org.apache.log4j.**
-dontwarn ch.qos.logback.core.net.**
-dontwarn org.apache.commons.logging.LogFactory
-dontwarn com.sun.jdi.**
-dontwarn java.beans.**
-dontwarn java.awt.**
-dontwarn java.applet.Applet
-dontwarn java.net.ProtocolFamily
-dontwarn java.net.StandardProtocolFamily
-dontwarn net.tomp2p.holep.testapp.**
-dontwarn org.bitlet.weupnp.**
-dontwarn org.bouncycastle.**

# Fix for netty
-keepattributes Signature,InnerClasses
-keepclasseswithmembers class io.netty.** { *; }
-keepnames class sun.misc.Unsafe { *; }

# For MBassador event bus
-keepattributes *Annotation*
-keep class javax.el.**,net.engio.**,android.support.annotation.** { *; }
-keep class org.hive2hive.mobile.files.AndroidFileEventListener { *; }
-keep class org.hive2hive.core.events.** { *; }

# For FST serializer and encryption
-keep class org.nustaq.**,javassist.**,org.objenesis.** { *; }
-keep class org.spongycastle.** { *; }
-keepclasseswithmembers class org.spongycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey { *; }
-keepclasseswithmembers class org.spongycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey { *; }
-keepclasseswithmembers class org.spongycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey { *; }

#-keepclasseswithmembernames class org.hive2hive.core.**,net.tomp2p.** { *; }

# Keep compatibility for serialized classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# For debug
#-keep class ch.qos.** { *; }
#-keep class org.slf4j.** { *; }