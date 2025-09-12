# Authentication Migration Guide

This document outlines the migration of authentication logic from frontend Firebase Auth to backend authentication using Firebase Admin SDK.

## Overview

The authentication system has been migrated from client-side Firebase Auth to server-side authentication with the following changes:

- **Frontend**: Removed Firebase Auth calls, replaced with HTTP requests to backend
- **Backend**: Added Firebase Admin SDK for user management and authentication
- **Token Management**: Implemented secure token storage and verification

## Architecture Changes

### Before (Frontend Firebase Auth)
```
Android App → Firebase Auth → Firestore
```

### After (Backend Authentication)
```
Android App → Backend API → Firebase Admin SDK → Firestore
```

## Backend Changes

### New Files Created
- `config/firebase.js` - Firebase Admin SDK configuration
- `routes/auth.js` - Authentication endpoints
- `test-auth.js` - Authentication testing script

### New Endpoints
- `POST /auth/signup` - User registration
- `POST /auth/login` - User authentication
- `POST /auth/profile` - Profile updates
- `GET /auth/verify` - Token verification

### Environment Variables
Add these to your `.env` file:
```env
# Firebase Admin SDK Configuration
GOOGLE_APPLICATION_CREDENTIALS=path/to/your/serviceAccountKey.json

# Firebase Web API Key (for authentication)
FIREBASE_WEB_API_KEY=your_firebase_web_api_key_here
```

## Frontend Changes

### New Files Created
- `BackendAuthService.kt` - HTTP client for backend authentication
- `TokenStorageService.kt` - Secure token storage using SharedPreferences

### Modified Files
- `AuthViewModel.kt` - Updated to use backend authentication
- `AppModule.kt` - Added dependency injection for new services

### Removed Dependencies
- Direct Firebase Auth calls from ViewModels
- Firebase Auth service injection

## Setup Instructions

### 1. Backend Setup

1. Install Firebase Admin SDK:
   ```bash
   cd backend
   npm install firebase-admin
   ```

2. Download Firebase service account key:
   - Go to Firebase Console → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Save the JSON file securely

3. Set environment variables:
   ```bash
   cp env.example .env
   # Edit .env with your actual values
   ```

4. Test the authentication endpoints:
   ```bash
   npm run test:auth:local
   ```

### 2. Frontend Setup

1. Update the backend URL in `BackendAuthService.kt`:
   ```kotlin
   private val baseUrl = "https://your-backend-url.com"
   ```

2. Build and test the app:
   ```bash
   ./gradlew assembleDebug
   ```

## Authentication Flow

### Signup Flow
1. User enters email, password, and profile info
2. Frontend sends POST request to `/auth/signup`
3. Backend creates user with Firebase Admin SDK
4. Backend stores user profile in Firestore
5. Backend returns user data and authentication token
6. Frontend stores token securely

### Login Flow
1. User enters email and password
2. Frontend sends POST request to `/auth/login`
3. Backend verifies credentials with Firebase Auth REST API
4. Backend retrieves user profile from Firestore
5. Backend returns user data and authentication token
6. Frontend stores token securely

### Profile Update Flow
1. User updates profile information
2. Frontend sends POST request to `/auth/profile` with token
3. Backend verifies token and updates user profile
4. Backend returns updated user data
5. Frontend updates local user state

### Token Verification
1. App starts and checks for stored token
2. Frontend sends GET request to `/auth/verify` with token
3. Backend verifies token with Firebase Admin SDK
4. Backend returns user data if token is valid
5. Frontend updates authentication state

## Security Considerations

### Token Management
- Tokens are stored securely in SharedPreferences
- Tokens are automatically cleared on logout
- Invalid/expired tokens trigger re-authentication

### Backend Security
- All authentication endpoints validate input
- Firebase Admin SDK provides secure user management
- Environment variables protect sensitive credentials

### API Security
- CORS is configured for allowed origins
- Rate limiting prevents abuse
- Input validation prevents injection attacks

## Testing

### Backend Testing
```bash
# Test local backend
npm run test:auth:local

# Test deployed backend
npm run test:auth:vercel
```

### Frontend Testing
1. Test signup flow with new user
2. Test login flow with existing user
3. Test profile update flow
4. Test token persistence across app restarts
5. Test logout and token clearing

## Troubleshooting

### Common Issues

1. **Firebase Admin SDK initialization error**
   - Ensure service account key is properly configured
   - Check `GOOGLE_APPLICATION_CREDENTIALS` environment variable

2. **Authentication token errors**
   - Verify Firebase Web API Key is set correctly
   - Check token format in requests

3. **CORS errors**
   - Update `ALLOWED_ORIGINS` in backend environment
   - Ensure frontend URL is included

4. **Network errors**
   - Verify backend URL in `BackendAuthService.kt`
   - Check network connectivity

### Debug Steps

1. Check backend logs for authentication errors
2. Verify environment variables are set correctly
3. Test endpoints manually with curl or Postman
4. Check frontend logs for network errors
5. Verify token storage and retrieval

## Migration Checklist

- [ ] Backend Firebase Admin SDK installed
- [ ] Authentication endpoints implemented and tested
- [ ] Environment variables configured
- [ ] Frontend services updated
- [ ] ViewModels migrated to use backend auth
- [ ] Token storage implemented
- [ ] Dependency injection updated
- [ ] Authentication flow tested end-to-end
- [ ] Error handling implemented
- [ ] Security measures verified

## Future Enhancements

1. **Refresh Tokens**: Implement token refresh mechanism
2. **Social Auth**: Add Google, Facebook, etc. authentication
3. **Multi-factor Auth**: Implement 2FA support
4. **Session Management**: Add session timeout and management
5. **Audit Logging**: Track authentication events
6. **Rate Limiting**: Implement per-user rate limiting

## Support

For issues or questions about the authentication migration:
1. Check the troubleshooting section above
2. Review backend and frontend logs
3. Test individual endpoints manually
4. Verify environment configuration

