-optimizationpasses 5
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference


-keep public class * extends android.support.v7.app.ActionBarActivity
-keep public class * extends android.support.v7.app.AppCompatActivity
-keep public class * extends android.support.v4.app.Fragment


-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}


# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ------------------ SUPPORT
-dontwarn android.support.**

-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }


#---------------- OkHTTP
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

#------------------- GSON
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

#------------------- Retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#------------------- Rx
-dontwarn rx.internal.util.unsafe.**

# ------------------ STICKER FACTORY
-dontwarn java.lang.invoke.*
-keepclassmembers class vc908.stickerfactory.** {
    <fields>;
}
-keepattributes InnerClasses

-keep public class vc908.stickerfactory.StickersManager{*;}
-keep public interface vc908.stickerfactory.ui.OnStickerSelectedListener{*;}
-keep public interface vc908.stickerfactory.ui.OnEmojiBackspaceClickListener{*;}

-keep public class vc908.stickerfactory.ui.fragment.StickersFragment{*;}
-keep public class vc908.stickerfactory.ui.fragment.StickersFragment$Builder{*;}

-keep public class vc908.stickerfactory.ui.fragment.StickersFragment$Builder{*;}

-keep public class vc908.stickerfactory.ui.view.BlockingListView{*;}
-keep public class vc908.stickerfactory.ui.view.KeyboardHandleRelativeLayout{*;}
-keep public interface vc908.stickerfactory.ui.view.KeyboardHandleRelativeLayout$KeyboardSizeChangeListener{*;}
-keep public interface vc908.stickerfactory.ui.view.KeyboardHandleRelativeLayout$OnKeyboardHideCallback{*;}

-keep public class vc908.stickerfactory.StickerLoader{*;}

-keep public class vc908.stickerfactory.utils.Utils{*;}
-keep public class vc908.stickerfactory.utils.KeyboardUtils{*;}
-keep public class vc908.stickerfactory.utils.AndroidEmoji{*;}

-keep class vc908.stickerfactory.model.** { *; }