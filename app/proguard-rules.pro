# ============================================================
# Qawmi Alumni — R8 / ProGuard rules
# ============================================================

# Keep line numbers for readable crash stack traces, hide source file names
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# ---------- Kotlinx Serialization ----------
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-dontnote kotlinx.serialization.**

# ---------- Retrofit ----------
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn org.codehaus.mojo.animal_sniffer.*

# ---------- OkHttp ----------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---------- Firestore DTOs ----------
# Firestore maps documents to classes via reflection (toObject()).
# All Firestore DTOs must live in this package.
-keep class com.qawmialumni.app.data.remote.dto.** { *; }
-keepclassmembers class com.qawmialumni.app.data.remote.dto.** {
    public <init>();
}

# ---------- Image upload API response models ----------
-keep class com.qawmialumni.app.core.network.model.** { *; }

# ---------- Room ----------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---------- Credential Manager / Google ID ----------
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }

# ---------- Strip debug logging in release ----------
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
