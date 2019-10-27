# Rx stuff
-keepattributes Signature
-dontwarn java.lang.invoke.*

-dontwarn rx.internal.util.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

# Instantsearch uses Eventbus, but we must write Eventbus' Proguard config here for now
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Unknown
-dontwarn org.objectweb.asm.**
-dontwarn net.minidev.asm.**
-dontwarn org.codehaus.jettison.**
-dontwarn com.jayway.jsonpath.**

# Support library
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

# Cardview
-keep class android.support.v7.widget.RoundRectDrawable { *; }

# Support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

# Instantsearch uses jsonpath
-dontwarn com.jayway.jsonpath.spi.json.GsonJsonProvider
-dontwarn com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
-dontwarn com.jayway.jsonpath.spi.json.JacksonJsonProvider
-dontwarn com.jayway.jsonpath.spi.json.TapestryJsdonProvider
-dontwarn com.jayway.jsonpath.spi.json.JsonOrgJsonProvider
-dontwarn com.jayway.jsonpath.spi.json.TapestryJsonProvider
-dontwarn com.jayway.jsonpath.spi.mapper.GsonMappingProvider*
-dontwarn com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
-dontwarn com.jayway.jsonpath.spi.mapper.JsonOrgMappingProvider

# Necessary for ignoring json-smart's logging dependencies
-dontwarn org.slf4j.*

# Application's own rules
-keep class com.bookcrossing.mobile.util.adapters.* { *; }
-keep class com.bookcrossing.mobile.models.* { *; }

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Algolia

-keep class com.algolia.search.model.** { *; }