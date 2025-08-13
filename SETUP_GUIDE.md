# Rotidote Project Setup Guide

This guide will walk you through setting up the complete Rotidote project, including the Android app, Node.js backend, and all integrations (Firebase, Mux, Cloudinary).

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Backend Setup](#backend-setup)
4. [Android App Setup](#android-app-setup)
5. [Firebase Setup](#firebase-setup)
6. [Mux Setup](#mux-setup)
7. [Cloudinary Setup](#cloudinary-setup)
8. [Deployment](#deployment)
9. [Testing](#testing)
10. [Troubleshooting](#troubleshooting)

## Prerequisites

### For Android Development
- Android Studio Arctic Fox or later
- Android SDK 24+
- Kotlin 1.9.10+
- Java 8 or later

### For Backend Development
- Node.js 18+
- npm or yarn
- Git

### Accounts Required
- [Firebase Console](https://console.firebase.google.com/) account
- [Mux Dashboard](https://dashboard.mux.com/) account
- [Cloudinary Dashboard](https://cloudinary.com/console) account
- [Render](https://render.com/) or [Railway](https://railway.app/) account (for backend deployment)

## Project Structure

```
Rotidote/
├── app/                          # Android app
│   ├── src/main/
│   │   ├── java/com/rotidote/app/
│   │   │   ├── data/
│   │   │   │   ├── models/       # Data classes
│   │   │   │   └── services/     # API services
│   │   │   ├── di/               # Dependency injection
│   │   │   ├── ui/
│   │   │   │   ├── components/   # Reusable UI components
│   │   │   │   ├── screens/      # App screens
│   │   │   │   ├── theme/        # App theme
│   │   │   │   ├── viewmodels/   # ViewModels
│   │   │   │   └── navigation/   # Navigation
│   │   │   └── MainActivity.kt
│   │   └── res/                  # Resources
│   └── build.gradle.kts
├── backend/                      # Node.js backend
│   ├── server.js                 # Main server file
│   ├── package.json              # Dependencies
│   ├── env.example               # Environment variables template
│   └── README.md                 # Backend documentation
├── README.md                     # Main project documentation
└── SETUP_GUIDE.md               # This file
```

## Backend Setup

### 1. Navigate to Backend Directory
```bash
cd backend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Environment Configuration
1. Copy the environment template:
```bash
cp env.example .env
```

2. Edit `.env` with your credentials:
```env
# Mux API Credentials
MUX_TOKEN_ID=your_mux_token_id_here
MUX_TOKEN_SECRET=your_mux_token_secret_here

# Server Configuration
PORT=3000
NODE_ENV=development

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:3000,https://your-app-domain.com
```

### 4. Test Locally
```bash
# Development mode
npm run dev

# Production mode
npm start
```

The server will start on `http://localhost:3000`

### 5. Test Endpoints
```bash
# Health check
curl http://localhost:3000/health

# Create upload URL
curl -X POST http://localhost:3000/create-upload \
  -H "Content-Type: application/json" \
  -d '{"filename":"test.mp4","contentType":"video/mp4"}'
```

## Android App Setup

### 1. Open Project in Android Studio
1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to the `Rotidote` folder and select it
4. Wait for Gradle sync to complete

### 2. Update Configuration Files

#### Update Backend URL
Edit `app/src/main/java/com/rotidote/app/di/AppModule.kt`:
```kotlin
@Provides
@Singleton
fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://your-deployed-backend-url.com/") // Replace with your backend URL
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

#### Update Cloudinary Configuration
Edit `app/src/main/java/com/rotidote/app/RotidoteApplication.kt`:
```kotlin
override fun onCreate() {
    super.onCreate()
    
    // Initialize Cloudinary
    cloudinaryService.initialize(this, "your_cloudinary_cloud_name") // Replace with your cloud name
}
```

### 3. Build and Run
1. Connect an Android device or start an emulator
2. Click the "Run" button (green play icon) in Android Studio
3. Select your device and click "OK"

## Firebase Setup

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Enter project name: "Rotidote"
4. Follow the setup wizard

### 2. Enable Authentication
1. In Firebase Console, go to "Authentication"
2. Click "Get started"
3. Go to "Sign-in method" tab
4. Enable "Email/Password" provider

### 3. Create Firestore Database
1. Go to "Firestore Database"
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select a location close to your users

### 4. Set up Firebase Storage
1. Go to "Storage"
2. Click "Get started"
3. Choose "Start in test mode" (for development)
4. Select the same location as Firestore

### 5. Download Configuration
1. Go to "Project settings" (gear icon)
2. Scroll down to "Your apps"
3. Click "Add app" → "Android"
4. Enter package name: `com.rotidote.app`
5. Download `google-services.json`
6. Replace the file in `app/google-services.json`

### 6. Security Rules (Optional)
For production, update Firestore security rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /videos/{videoId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## Mux Setup

### 1. Create Mux Account
1. Go to [Mux Dashboard](https://dashboard.mux.com/)
2. Sign up for a free account
3. Complete the onboarding process

### 2. Get API Credentials
1. Go to "Settings" → "Access Tokens"
2. Click "Create new token"
3. Set permissions:
   - ✅ Video: Read
   - ✅ Video: Write
4. Copy the Token ID and Token Secret

### 3. Update Backend Environment
Add the credentials to your backend `.env` file:
```env
MUX_TOKEN_ID=your_token_id_here
MUX_TOKEN_SECRET=your_token_secret_here
```

### 4. Test Mux Integration
1. Restart your backend server
2. Test the upload endpoint:
```bash
curl -X POST http://localhost:3000/create-upload \
  -H "Content-Type: application/json" \
  -d '{"filename":"test.mp4","contentType":"video/mp4"}'
```

## Cloudinary Setup

### 1. Create Cloudinary Account
1. Go to [Cloudinary Console](https://cloudinary.com/console)
2. Sign up for a free account
3. Complete the setup process

### 2. Get Cloud Name
1. In your Cloudinary dashboard, note your "Cloud name"
2. It's displayed at the top of the dashboard

### 3. Update Android App
Edit `app/src/main/java/com/rotidote/app/RotidoteApplication.kt`:
```kotlin
override fun onCreate() {
    super.onCreate()
    
    // Initialize Cloudinary
    cloudinaryService.initialize(this, "your_cloud_name_here")
}
```

### 4. Test Cloudinary Integration
1. Build and run the Android app
2. Go to the Upload screen
3. Try uploading an image as a thumbnail

## Deployment

### Backend Deployment (Render - Free Tier)

1. **Create Render Account**
   - Go to [render.com](https://render.com)
   - Sign up with GitHub

2. **Create Web Service**
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Select the `backend` directory

3. **Configure Service**
   - **Name**: `rotidote-backend`
   - **Environment**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
   - **Plan**: Free

4. **Set Environment Variables**
   - Go to Environment tab
   - Add:
     - `MUX_TOKEN_ID`: Your Mux Token ID
     - `MUX_TOKEN_SECRET`: Your Mux Token Secret
     - `NODE_ENV`: `production`
     - `ALLOWED_ORIGINS`: Your app's domain

5. **Deploy**
   - Click "Create Web Service"
   - Wait for deployment to complete
   - Copy the provided URL

6. **Update Android App**
   - Update the backend URL in `AppModule.kt` with your Render URL

### Alternative Deployment Options

#### Railway
1. Go to [railway.app](https://railway.app)
2. Connect GitHub repository
3. Select `backend` directory
4. Add environment variables
5. Deploy

#### Vercel
1. Go to [vercel.com](https://vercel.com)
2. Import GitHub repository
3. Set root directory to `backend`
4. Add environment variables
5. Deploy

## Testing

### Backend Testing
```bash
# Health check
curl https://backend-nfyoqvmdy-dineshs-projects-aad49f55.vercel.app/health

# Create upload URL
curl -X POST https://backend-nfyoqvmdy-dineshs-projects-aad49f55.vercel.app/create-upload \
  -H "Content-Type: application/json" \
  -d '{"filename":"test.mp4","contentType":"video/mp4"}'

# Get asset details
curl https://backend-nfyoqvmdy-dineshs-projects-aad49f55.vercel.app/asset/your_asset_id
```

### Android App Testing
1. **Authentication Flow**
   - Test sign up with new email
   - Test login with existing account
   - Test profile setup

2. **Video Upload**
   - Select ad video file
   - Select main video file
   - Select thumbnail image
   - Test upload process

3. **Video Playback**
   - Browse videos in home feed
   - Play videos (ad + main)
   - Test like/dislike functionality

## Troubleshooting

### Common Issues

#### Backend Issues
1. **CORS Errors**
   - Check `ALLOWED_ORIGINS` in `.env`
   - Ensure your app's domain is included

2. **Mux Authentication Errors**
   - Verify Mux credentials in `.env`
   - Check token permissions

3. **Port Already in Use**
   - Change `PORT` in `.env`
   - Kill existing processes

#### Android Issues
1. **Build Errors**
   - Clean and rebuild project
   - Sync Gradle files
   - Check dependency versions

2. **Network Errors**
   - Verify backend URL in `AppModule.kt`
   - Check internet permissions
   - Test backend endpoints

3. **File Upload Issues**
   - Check storage permissions
   - Verify file types
   - Test with smaller files

#### Firebase Issues
1. **Authentication Errors**
   - Verify `google-services.json`
   - Check Firebase project settings
   - Enable Email/Password auth

2. **Firestore Errors**
   - Check security rules
   - Verify collection names
   - Test with Firebase console

### Debug Steps
1. Check Android Studio logs
2. Monitor backend server logs
3. Test API endpoints with Postman
4. Verify environment variables
5. Check Firebase console for errors

### Getting Help
- Check the [Firebase Documentation](https://firebase.google.com/docs)
- Review [Mux API Documentation](https://docs.mux.com/)
- Consult [Cloudinary Documentation](https://cloudinary.com/documentation)
- Check deployment platform documentation

## Next Steps

After successful setup:

1. **Customize the UI**
   - Update colors and themes
   - Add your branding
   - Customize layouts

2. **Add Features**
   - User profiles
   - Comments system
   - Video categories
   - Search functionality

3. **Optimize Performance**
   - Implement caching
   - Optimize video loading
   - Add pagination

4. **Security Enhancements**
   - Implement proper authentication
   - Add input validation
   - Secure API endpoints

5. **Monitoring**
   - Add analytics
   - Monitor performance
   - Set up error tracking

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review platform-specific documentation
3. Test with minimal examples
4. Verify all credentials and configurations

---

**Note**: This setup guide assumes you have basic knowledge of Android development, Node.js, and cloud services. If you encounter issues, refer to the official documentation for each service.



