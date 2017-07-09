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

# Support library
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

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
