package com.example.neatnest

import android.app.Application
import com.example.neatnest.di.appModule
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NeatNestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // dynamic colors on android 12+
        DynamicColors.applyToActivitiesIfAvailable(this)

        // koin init
        startKoin {
            androidContext(this@NeatNestApplication)
            modules(appModule)
        }
    }
}

