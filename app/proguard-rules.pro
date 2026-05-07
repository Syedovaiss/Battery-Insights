# --- General Android & Kotlin ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

# --- Room Database ---
-keep class com.ovais.batterymonitorer.storage.database.entity.** { *; }
-keep class com.ovais.batterymonitorer.storage.database.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Hilt / Dagger ---
-keep class * extends androidx.activity.ComponentActivity
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.work.ListenableWorker
-keep @dagger.hilt.android.EntryPoint class *
-keep @dagger.hilt.InstallIn class *

# --- Kotlin Serialization (if used for JSON/Preferences) ---
-keepclassmembers class com.ovais.batterymonitorer.** {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class com.ovais.batterymonitorer.** { *; }

# --- Firebase / GMS ---
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# --- Timber ---
-keep class timber.log.** { *; }
-keepclassmembers class * {
    public static void log(...);
}

# --- Preserve line numbers for better Crashlytics reports ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
