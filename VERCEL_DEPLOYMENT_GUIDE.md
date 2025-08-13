# Vercel Deployment Guide for Rotidote Backend

This guide will walk you through deploying the Rotidote backend to Vercel with proper authentication and file upload handling.

## Prerequisites

1. **Vercel Account**: Sign up at [vercel.com](https://vercel.com)
2. **GitHub Repository**: Your project should be on GitHub
3. **Firebase Project**: Set up Firebase for authentication and Firestore
4. **Mux Account**: Set up Mux for video processing
5. **Cloudinary Account**: Set up Cloudinary for image storage

## Step 1: Firebase Setup

### 1.1 Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Enable Authentication with Email/Password
4. Create Firestore Database
5. Set up Firebase Storage

### 1.2 Get Firebase Admin SDK Credentials
1. Go to Project Settings → Service Accounts
2. Click "Generate New Private Key"
3. Download the JSON file
4. Note the following values:
   - `project_id`
   - `client_email`
   - `private_key`
   - `storage_bucket`

## Step 2: Mux Setup

### 2.1 Create Mux Account
1. Go to [Mux Dashboard](https://dashboard.mux.com/)
2. Sign up and complete onboarding
3. Go to Settings → Access Tokens
4. Create a new token with:
   - ✅ Video: Read
   - ✅ Video: Write
5. Copy the Token ID and Token Secret

## Step 3: Cloudinary Setup

### 3.1 Create Cloudinary Account
1. Go to [Cloudinary Console](https://cloudinary.com/console)
2. Sign up and complete setup
3. Note your:
   - Cloud Name
   - API Key
   - API Secret

## Step 4: Deploy to Vercel

### 4.1 Connect Repository
1. Go to [Vercel Dashboard](https://vercel.com/dashboard)
2. Click "New Project"
3. Import your GitHub repository
4. Select the repository

### 4.2 Configure Project
1. **Framework Preset**: Node.js
2. **Root Directory**: `backend`
3. **Build Command**: `npm install`
4. **Output Directory**: Leave empty
5. **Install Command**: `npm install`
6. **Development Command**: `npm run dev`

### 4.3 Set Environment Variables
Click "Environment Variables" and add the following:

#### Firebase Configuration
```
FIREBASE_PROJECT_ID=rotidote-database
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-fbsvc@rotidote-database.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCyqcnY6PUpinSp\nL/qQB/O60sotD5zyXMgLyZg03yMp9kS68nAQq9ge25BS3eH/tlBZNtJFMkAMnJz9\nA4Wg/g0+pR6GFRGgKLGoX+1rFh/oSGB5U9U9GqvpAOPfKJ/lcRjU7NLYj2o/ruEV\nQqMevQH2NBnMNUAROEroINOzUWIxJS3aeA8l7ncLG5tBiR5i8y3f4txYT3csUc8Z\n1MCg8aAlrb6z27j0FGFhAMB+WBH5ukmVNespdNlaN11WQ5a9DsnfYUHiHTlQxrMA\nmhPyOBM/wFzXW8jEQeusjwjiociH5Cvo+ls72MBN4gwTPabjMoTF2cvdBtzCKq7a\nRxtW1Jn/AgMBAAECggEABG0HVovC0wIejLhnDR1dUNH69y3NVgWtYPlf/2NNtDLp\nFfZ+hzMymeUXiK39dL+38tXUVKl/g7B3hR8KtufEPTqK7IhmFOEdMY4azA3SwYbV\nePv2KtrXymBexL71YxIvqsqpGHtJBwXWhpfNgavt+gLlZrUk/qg11tVFWUpZU91M\n0VHdzc4KQlTwm/GNJXdJVzmksn+rdyFjCdX9uZSxx78xvRuYjCQeacAfpdHY2J5q\nlIA2tTzJXhFrNW+oy5Y1mRq3liMfCYcnEGIiplCfzl3WJ+OU2Y69YtWeIKWDXtY+\nFVJFJGQ0e0VQIMeME6TlqsD0VkjmcYbf3nIhDi0vOQKBgQDcEbcLA4JzcOaWaxRC\nHZO0E4yFBv3we3zqcmIpob685idOHYsWYP4Wv82ImFUrnu+7Fou2LcMMt6Im8cya\nSwy0IL4QhVCEWb4H5LE+8F2MkhngOi1G5OAwEv7uUBadqgeZ9uOGld2OMk8Nqwxi\nBVbMlAAyOJNQLIVJLhqCbrXN2QKBgQDP1WsjQAMk5NbP39A5YXOIOXUF2fj+Ap00\nP9g1JID2kt170F5obj3cljw47q9NF1cFjyzk4yzvHKn7rZJ3Kd3yN1+siWQ2tyRT\n00O7UAWOAxezS3bEjQ58Cp8rKpuQXCYQPZBpKUb7obeDekxp0DkE9oqqPYZvbK1M\nPCPYe+lHlwKBgBqaAZhROkmeEiGInBBJ5vrTih7ic3vIUZc9cGQ76wSGhaXJqHBy\nFQTG9p9WWqVFNxFUblr9OSCmafFvNR5JiCWV96OjmdIFzxTBLjO/dm9xkbMrFSIG\ntJrbEPnZsIIqdXTFBUfVgcyYhDnQvVRz9MQR99waPqRoXUw1SHsPyuYxAoGASOtm\nZb3F3KH1xIWQdzF76aXPKzhpJiYgpf5oi2y9WsX4s8JN/XIAfm6vvoHwO6oevDKZ\n28zhLRvvgABMTUcBOkS3ar/hy1jChC6xqvzOlh77p8qoZxnepSLM6cZPQn0yVaxk\nCKGU6CC7VIQKX23RM66UxFpF2r5Up1TKduAy3w0CgYEAjcgSnytZhQ8dDcTnxAd2\n9CmqmIsPGrsqtA4P0GhHIBl8X0+XvUHwn2Yong8zwI+oiVDWujqUDZ4WxQaMG0nJ\nVNucDlfCNilEQywubdkpm6P4Pb2zwlV7PTw3n7u6D75w0cR+25X5Guxhm9o4WQHl\n24KbRnKf4/XLYhRcye5RsQI=\n-----END PRIVATE KEY-----\n"
FIREBASE_STORAGE_BUCKET=rotidote-database.appspot.com
```

#### Mux Configuration
```
MUX_TOKEN_ID=your_mux_token_id_here
MUX_TOKEN_SECRET=your_mux_token_secret_here
```

#### Cloudinary Configuration
```
CLOUDINARY_CLOUD_NAME=dapfnnxjv
CLOUDINARY_API_KEY=534343313393833
CLOUDINARY_API_SECRET=py5rmT6CBx6N1FmU5fQzPEnLY7Y
```

#### Server Configuration
```
NODE_ENV=production
PORT=3000
ALLOWED_ORIGINS=https://your-app-domain.com,https://another-domain.com
```

### 4.4 Deploy
1. Click "Deploy"
2. Wait for deployment to complete
3. Copy the provided URL

## Step 5: Update Android App

### 5.1 Update Backend URL
Edit `app/src/main/java/com/rotidote/app/di/AppModule.kt`:
```kotlin
@Provides
@Singleton
fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://your-vercel-url.vercel.app/") // Replace with your Vercel URL
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

## Step 6: Testing

### 6.1 Test Backend Health
```bash
curl https://your-project.vercel.app/health
```

Expected response:
```json
{
  "status": "OK",
  "timestamp": "2024-01-01T00:00:00.000Z",
  "services": {
    "mux": true,
    "cloudinary": true,
    "firebase": true
  }
}
```

### 6.2 Test Authentication
```bash
# Get Firebase ID token (you'll need to implement this in your app)
curl -H "Authorization: Bearer YOUR_FIREBASE_ID_TOKEN" \
  https://your-project.vercel.app/videos
```

### 6.3 Test Video Upload
```bash
curl -X POST https://your-project.vercel.app/upload-video \
  -H "Authorization: Bearer YOUR_FIREBASE_ID_TOKEN" \
  -F "creatorName=Test Creator" \
  -F "videoTitle=Test Video" \
  -F "duration=60000" \
  -F "adVideo=@/path/to/ad-video.mp4" \
  -F "mainVideo=@/path/to/main-video.mp4" \
  -F "thumbnail=@/path/to/thumbnail.jpg"
```

## Step 7: Troubleshooting

### Common Issues

#### 1. 401 Unauthorized Errors
- **Cause**: Missing or invalid Firebase ID token
- **Solution**: Ensure user is authenticated and token is being sent

#### 2. 413 Payload Too Large
- **Cause**: File size exceeds 100MB limit
- **Solution**: Compress videos or use smaller files

#### 3. CORS Errors
- **Cause**: Origin not in ALLOWED_ORIGINS
- **Solution**: Add your app's domain to ALLOWED_ORIGINS

#### 4. Firebase Connection Errors
- **Cause**: Invalid Firebase credentials
- **Solution**: Verify FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL, and FIREBASE_PRIVATE_KEY

#### 5. Mux Upload Failures
- **Cause**: Invalid Mux credentials
- **Solution**: Verify MUX_TOKEN_ID and MUX_TOKEN_SECRET

#### 6. Cloudinary Upload Failures
- **Cause**: Invalid Cloudinary credentials
- **Solution**: Verify CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET

### Debug Steps
1. Check Vercel deployment logs
2. Test endpoints with Postman
3. Verify environment variables
4. Check Firebase console for errors
5. Monitor Mux dashboard for uploads
6. Check Cloudinary dashboard for images

## Step 8: Environment Variables Reference

| Variable | Description | Required | Example |
|----------|-------------|----------|---------|
| `FIREBASE_PROJECT_ID` | Firebase project ID | Yes | `rotidote-database` |
| `FIREBASE_CLIENT_EMAIL` | Firebase service account email | Yes | `firebase-adminsdk-fbsvc@rotidote-database.iam.gserviceaccount.com` |
| `FIREBASE_PRIVATE_KEY` | Firebase private key | Yes | `-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n` |
| `FIREBASE_STORAGE_BUCKET` | Firebase storage bucket | Yes | `rotidote-database.appspot.com` |
| `MUX_TOKEN_ID` | Mux token ID | Yes | `abcd1234-ef56-7890-abcd-ef1234567890` |
| `MUX_TOKEN_SECRET` | Mux token secret | Yes | `abcd1234-ef56-7890-abcd-ef1234567890` |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | Yes | `dapfnnxjv` |
| `CLOUDINARY_API_KEY` | Cloudinary API key | Yes | `534343313393833` |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | Yes | `py5rmT6CBx6N1FmU5fQzPEnLY7Y` |
| `NODE_ENV` | Environment | Yes | `production` |
| `PORT` | Server port | No | `3000` |
| `ALLOWED_ORIGINS` | CORS origins | Yes | `https://myapp.com,https://another.com` |

## Support

For issues:
1. Check Vercel deployment logs
2. Verify all environment variables
3. Test with minimal examples
4. Check service dashboards (Firebase, Mux, Cloudinary)
5. Review this troubleshooting guide

---

**Note**: Keep your environment variables secure and never commit them to version control.
