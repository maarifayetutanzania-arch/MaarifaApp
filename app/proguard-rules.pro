# Firebase / Firestore model classes must keep their no-arg constructors + fields
-keepclassmembers class com.maarifa.app.data.model.** {
    *;
}
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
