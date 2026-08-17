package com.maarifa.app

import android.app.Application
import com.maarifa.app.di.AppContainer

class MaarifaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer.get(this)
    }
}
