package com.maarifa.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.maarifa.app.di.AppContainer

class MaarifaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Firebase kwanza kabisa
        FirebaseApp.initializeApp(this)

        // 2. Ndio utengeneze container
        container = AppContainer.get(this)
    }
}
