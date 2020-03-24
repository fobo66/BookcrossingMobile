# Application's own rules
-keep class com.bookcrossing.mobile.util.adapters.* { *; }
-keep class com.bookcrossing.mobile.models.* { *; }

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Algolia

-keep class com.algolia.search.model.** { *; }

# Glide

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
