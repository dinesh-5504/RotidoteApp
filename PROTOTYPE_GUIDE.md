# Rotidote Android Prototype Guide

This is the Android prototype for Rotidote, featuring Firestore integration, Jetpack Compose UI, and ExoPlayer for video playback.

## Features

### 1. Upload Page
- **Manual Entry**: Enter video details manually (no file uploads)
- **Fields Required**:
  - Creator Name
  - Video Title
  - Duration (format: mm:ss, e.g., "05:32")
  - Main Video Genre
  - Main Video Playback ID (from Mux)
  - Ad Video Genre
  - Ad Video Playback ID (from Mux)
  - Thumbnail URL (from Cloudinary)

### 2. Home Screen
- **Vertical Video Feed**: Displays videos in a scrollable list
- **Genre Sections**: Every 2 videos, shows "More in {genre}" section
- **Horizontal Scrolling**: Genre videos scroll horizontally
- **Real-time Updates**: Uses Firestore snapshot listener

### 3. Video Player
- **Ad + Main Video**: Plays ad first, then main video (no skip, no seek)
- **Genre Videos**: Skip ad, play main video only
- **ExoPlayer**: Uses ConcatenatingMediaSource for seamless playback
- **Thumbnail Display**: Shows thumbnail until playback starts

## Firestore Data Structure

```json
{
  "videoId": "generated-id",
  "title": "My Video",
  "creatorName": "John Doe",
  "duration": "05:32",
  "mainGenre": "Education",
  "mainVideo": {
    "playbackId": "main12345",
    "thumbnailUrl": "https://cloudinary.com/...thumb.jpg"
  },
  "adGenre": "Technology",
  "adVideo": {
    "playbackId": "ad67890"
  }
}
```

## Setup Instructions

1. **Firebase Setup**:
   - Add your `google-services.json` to `app/`
   - Enable Firestore in Firebase Console
   - Create a "videos" collection

2. **Mux Videos**:
   - Upload videos to Mux manually
   - Get playback IDs from Mux dashboard
   - Use format: `https://stream.mux.com/{playbackId}.m3u8`

3. **Cloudinary Thumbnails**:
   - Upload thumbnail images to Cloudinary
   - Use the generated URL in the upload form

## Usage Example

1. **Add a Video**:
   - Navigate to Upload screen
   - Fill in all fields:
     - Creator: "John Doe"
     - Title: "Math Tutorial"
     - Duration: "05:30"
     - Main Genre: "Education"
     - Main Playback ID: "abc123"
     - Ad Genre: "Technology"
     - Ad Playback ID: "xyz789"
     - Thumbnail: "https://res.cloudinary.com/.../thumb.jpg"

2. **View Videos**:
   - Home screen shows vertical video list
   - Every 2 videos shows genre section
   - Tap vertical videos to play with ad
   - Tap genre videos to skip ad

3. **Video Playback**:
   - Vertical videos: Ad → Main Video
   - Genre videos: Main Video only
   - No seek controls during ad playback

## Technical Notes

- **ExoPlayer**: Uses Media3 ExoPlayer for video playback
- **Firestore**: Real-time updates with snapshot listeners
- **Compose**: Modern UI with Material 3 design
- **Navigation**: Type-safe navigation with Compose Navigation
- **Dependency Injection**: Hilt for dependency management

## Build and Run

```bash
# Build the app
./gradlew assembleDebug

# Install and run on device
./gradlew runDebug
```

## Dependencies

- **Jetpack Compose**: UI framework
- **ExoPlayer**: Video playback
- **Firebase Firestore**: Database
- **Coil**: Image loading
- **Hilt**: Dependency injection
- **Navigation Compose**: Navigation

This prototype demonstrates the core video streaming functionality with ad integration, ready for further development and refinement.
