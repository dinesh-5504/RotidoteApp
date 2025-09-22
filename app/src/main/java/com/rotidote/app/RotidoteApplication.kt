package com.rotidote.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RotidoteApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
} 