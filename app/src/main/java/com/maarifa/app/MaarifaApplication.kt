package com.maarifa.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.maarifa.app.di.AppContainer

class MaarifaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase manual kwa kutumia credentials kutoka kwako
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyCcNlowv61JgW0Bda9kkdFtLlWA8QZSIIw")
                .setApplicationId("1:106555594589:android:e743e63b46ed3688ad3ab4")
                .setProjectId("maarifaapp-aa585")
                .setStorageBucket("maarifaapp-aa585.firebasestorage.app")
                .build()

            FirebaseApp.initializeApp(this, options)
        }

        // Initialize AppContainer baada ya Firebase kuwa tayari
        container = AppContainer.get(this)
    }
}
