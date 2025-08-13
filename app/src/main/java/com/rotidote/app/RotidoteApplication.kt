package com.rotidote.app

import android.app.Application
import com.rotidote.app.data.services.CloudinaryService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RotidoteApplication : Application() {
    
    @Inject
    lateinit var cloudinaryService: CloudinaryService
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloudinary
        cloudinaryService.initialize(this, "dapfnnxjv") // Replace with your Cloudinary cloud name
    }
} 