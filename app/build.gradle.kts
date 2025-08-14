plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.rotidote.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rotidote.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    // implementation ("com.google.android.gms:play-services-tasks-ktx:18.1.0")


    // Compose
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-common:1.2.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    // JWT
    implementation("com.auth0.android:jwtdecode:2.0.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Custom task to build, install, and launch the app
tasks.register("runDebug") {
    group = "application"
    description = "Builds debug APK, installs it on connected device, and launches the app"
    
    dependsOn("assembleDebug")
    
    doLast {
        val sdkDir = android.sdkDirectory
        val adbPath = if (System.getProperty("os.name").lowercase().contains("windows")) {
            "$sdkDir/platform-tools/adb.exe"
        } else {
            "$sdkDir/platform-tools/adb"
        }
        val adbFile = file(adbPath)
        if (!adbFile.exists()) {
            throw GradleException("ADB not found at: $adbPath. Please ensure Android SDK platform-tools are installed.")
        }
        
        val packageName = android.defaultConfig.applicationId
        
        // For now, use the known correct format since we know the manifest structure
        val mainActivity = "$packageName/.MainActivity"
        
        println("🚀 Building and installing $packageName...")
        println("📱 Package: $packageName")
        println("🎯 Activity: $mainActivity")
        
        // Install the APK
        println("📦 Installing APK...")
        exec {
            commandLine(adbPath, "install", "-r", "-t", "build/outputs/apk/debug/app-debug.apk")
            isIgnoreExitValue = false
        }
        
        // Launch the app
        println("🚀 Launching app...")
        exec {
            commandLine(adbPath, "shell", "am", "start", "-n", mainActivity)
            isIgnoreExitValue = false
        }
        
        println("✅ App installed and launched successfully!")
        println("🎉 You can now use: ./gradlew runDebug")
    }
} 