# Rotidote
<<<<<<< HEAD
Rotidote app Github repo
=======

A comprehensive Android app built with Kotlin and Jetpack Compose, featuring Firebase integration, Mux video streaming, Cloudinary image uploads, and a modern Material3 UI.

## Features

### 🔐 Authentication
- Firebase Authentication for email/password login
- User profile setup with name, grade, section, and school information
- JWT session management
- Secure token validation

### 🎥 Video Streaming & Upload
- Mux integration for video upload and playback via Node.js backend
- ExoPlayer for smooth video playback
- Ad video and main video sequential playback
- Direct video upload to Mux with progress tracking

### 🖼️ Image Upload
- Cloudinary integration for video thumbnail uploads
- Secure image storage with CDN delivery
- Automatic image optimization

### 📱 User Interface
- Modern Material3 design system
- Jetpack Compose for declarative UI
- Responsive and accessible design
- Dark/light theme support

### 🏠 Screens
1. **Login/Signup Screen** - Email/password authentication
2. **Profile Setup Screen** - Collect user information
3. **Home Screen** - Video feed with filter chips and bottom navigation
4. **Video Player Screen** - Full-screen video playback with controls
5. **Upload Screen** - Creator video upload with file selection

## Tech Stack

### Android App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose
- **Backend Services**: Firebase (Auth, Firestore, Storage)
- **Video Streaming**: Mux + ExoPlayer
- **Image Upload**: Cloudinary SDK
- **Image Loading**: Coil
- **Networking**: Retrofit + OkHttp
- **State Management**: Kotlin Flow

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **Video API**: Mux Direct Upload
- **Security**: Helmet, CORS, Rate Limiting
- **Deployment**: Render/Railway/Vercel (Free Tier)

## Project Structure

```
Rotidote/
├── app/                          # Android app
│   ├── src/main/
│   │   ├── java/com/rotidote/app/
│   │   │   ├── data/
│   │   │   │   ├── models/       # Data classes (User, Video, AuthState)
│   │   │   │   └── services/     # API services (Firebase, Mux, Cloudinary, Backend)
│   │   │   ├── di/               # Dependency injection (Hilt)
│   │   │   ├── ui/
│   │   │   │   ├── components/   # Reusable UI components
│   │   │   │   ├── screens/      # App screens (Login, Home, Video Player, Upload)
│   │   │   │   ├── theme/        # App theme and styling
│   │   │   │   ├── viewmodels/   # ViewModels for each screen
│   │   │   │   └── navigation/   # Navigation setup
│   │   │   └── MainActivity.kt   # Main activity
│   │   └── res/                  # Resources
│   └── build.gradle.kts
├── backend/                      # Node.js backend
│   ├── server.js                 # Main server file
│   ├── package.json              # Dependencies
│   ├── env.example               # Environment variables template
│   └── README.md                 # Backend documentation
├── README.md                     # Main project documentation
└── SETUP_GUIDE.md               # Comprehensive setup guide
```

## Quick Start

For detailed setup instructions, see [SETUP_GUIDE.md](SETUP_GUIDE.md).

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- Kotlin 1.9.10+
- Node.js 18+ (for backend)
- Firebase, Mux, and Cloudinary accounts

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Rotidote
```

### 2. Backend Setup
```bash
cd backend
npm install
cp env.example .env
# Edit .env with your credentials
npm run dev
```

### 3. Android App Setup
1. Open `app/` in Android Studio
2. Update backend URL in `AppModule.kt`
3. Update Cloudinary config in `RotidoteApplication.kt`
4. Replace `google-services.json` with your Firebase config
5. Build and run

### 4. Services Setup
- **Firebase**: Enable Auth, Firestore, Storage
- **Mux**: Get API credentials for backend
- **Cloudinary**: Get cloud name for Android app

## Configuration

### Firebase Configuration
The app uses the following Firebase services:
- **Authentication**: Email/password login
- **Firestore**: User profiles and video metadata
- **Storage**: Video thumbnails

### Backend Configuration
The Node.js backend provides:
- Mux Direct Upload URL generation
- Asset metadata retrieval
- Health monitoring endpoints

### Cloudinary Configuration
- Secure image upload for video thumbnails
- Automatic image optimization and CDN delivery

## Key Features Implementation

### Authentication Flow
1. User enters email/password
2. Firebase Authentication validates credentials
3. If new user, redirect to profile setup
4. If existing user with complete profile, go to home

### Video Playback
1. Ad video plays first (no seek controls)
2. Main video plays after ad completion
3. Full playback controls for main video
4. Like/dislike functionality

### Video Upload
1. Creator selects video files and thumbnail image
2. Videos uploaded directly to Mux via backend
3. Thumbnail uploaded to Cloudinary
4. Metadata stored in Firestore

## Dependencies

### Core Dependencies
- `androidx.core:core-ktx:1.12.0`
- `androidx.lifecycle:lifecycle-runtime-ktx:2.7.0`
- `androidx.activity:activity-compose:1.8.2`

### Compose Dependencies
- `androidx.compose:compose-bom:2023.10.01`
- `androidx.navigation:navigation-compose:2.7.5`
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0`

### Firebase Dependencies
- `com.google.firebase:firebase-bom:32.7.0`
- `com.google.firebase:firebase-auth-ktx`
- `com.google.firebase:firebase-firestore-ktx`
- `com.google.firebase:firebase-storage-ktx`

### Video Dependencies
- `androidx.media3:media3-exoplayer:1.2.0`
- `androidx.media3:media3-ui:1.2.0`

### Other Dependencies
- `dagger.hilt.android:hilt-android:2.48`
- `io.coil-kt:coil-compose:2.5.0`
- `com.squareup.retrofit2:retrofit:2.9.0`
- `com.cloudinary:cloudinary-android:2.3.1`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the repository or contact the development team. 
>>>>>>> 207bbb1 (initial commit)
