# 🔑 Admin Token Testing Guide

## Overview
This guide explains how to extract and use the Firebase ID token for testing the admin dashboard API endpoints.

## 🔄 Token Extraction Flow

### 1. Admin Login Process
1. Open the Rotidote app
2. Navigate to Login screen
3. Enter admin credentials:
   - **Email**: `dineshkarthikeyan.admin@gmail.com`
   - **Password**: `RotidoteApp`
4. Tap "Sign In"

### 2. Automatic Token Fetch
After successful admin login, the app will automatically:
- ✅ Fetch the Firebase ID token using `currentUser.getIdToken(true)`
- ✅ Log the token to console with clear markers
- ✅ Display an alert dialog with the token for easy copy-paste
- ✅ Show a toast notification confirming token fetch

### 3. Token Display Format

#### Console Log Output:
```
D/AdminTokenUtil: Admin user detected, fetching ID token...
I/AdminTokenUtil: === ADMIN FIREBASE ID TOKEN ===
I/AdminTokenUtil: eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vcm90aWRvdGUtYXBwIiwiYXVkIjoicm90aWRvdGUtYXBwIiwiYXV0aF90aW1lIjoxNzM3MzQ1MjAwLCJ1c2VyX2lkIjoiZGVtb1VzZXJJZCIsInN1YiI6ImRlbW9Vc2VySWQiLCJpYXQiOjE3MzczNDUyMDAsImV4cCI6MTczNzM0ODgwMCwiZW1haWwiOiJkaW5lc2hrYXJ0aGlrZXlhbi5hZG1pbkBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJkaW5lc2hrYXJ0aGlrZXlhbi5hZG1pbkBnbWFpbC5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.signature_here
I/AdminTokenUtil: ==============================
```

#### Alert Dialog:
```
🔑 Admin Firebase ID Token

Copy this token to use in TestSprite:

eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ...

📋 Instructions:
1. Copy the token above
2. Open backend/test-admin-endpoints.js
3. Replace 'your-firebase-admin-token-here' with this token
4. Run: node test-admin-endpoints.js
```

## 🧪 Using Token in TestSprite

### Step 1: Copy Token
- **From Console**: Open Android Studio Logcat, search for "ADMIN FIREBASE ID TOKEN"
- **From Alert**: Tap "Copy Token" button in the alert dialog

### Step 2: Update TestSprite
1. Open `backend/test-admin-endpoints.js`
2. Find line 8: `const ADMIN_TOKEN = 'your-firebase-admin-token-here';`
3. Replace with your copied token:
   ```javascript
   const ADMIN_TOKEN = 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ...';
   ```

### Step 3: Run Tests
```bash
cd backend
node test-admin-endpoints.js
```

## 🔍 Expected Test Results

### Successful Token Usage:
```
🧪 TestSprite - Admin Dashboard API Tests
==========================================

✅ PASS - Get All Students
   Response: {"students":[{"id":"user123","name":"John Doe"...

✅ PASS - Get All Sessions
   Response: {"sessions":[{"dayId":"Day1","enabled":false...

✅ PASS - Create/Update Session Day1
   Response: {"message":"Session updated successfully"...

📊 Test Summary
================
Total Tests: 15
Passed: 15
Failed: 0
```

### Token Issues:
```
❌ FAIL - Get All Students
   Expected: 200, Got: 401
   Response: {"error":"Unauthorized"}

💥 ERROR - Get All Students: Network error
```

## 🛠️ Troubleshooting

### Token Expired
- **Symptom**: 401 Unauthorized errors
- **Solution**: Re-login as admin to get fresh token (tokens expire after 1 hour)

### Invalid Token Format
- **Symptom**: 400 Bad Request errors
- **Solution**: Ensure full token is copied (usually 1000+ characters)

### Network Issues
- **Symptom**: Connection timeout errors
- **Solution**: Check backend URL in test file, ensure backend is running

### Admin Verification Failed
- **Symptom**: 403 Forbidden errors
- **Solution**: Verify admin email matches `dineshkarthikeyan.admin@gmail.com`

## 🔒 Security Notes

### Development Only
- ⚠️ **Token display is for development/testing only**
- ⚠️ **Tokens contain sensitive authentication data**
- ⚠️ **Never commit tokens to version control**
- ⚠️ **Tokens expire automatically after 1 hour**

### Production Considerations
- 🔐 Remove token display logic before production release
- 🔐 Use proper API key management in production
- 🔐 Implement token refresh mechanisms
- 🔐 Add rate limiting and monitoring

## 📱 Manual Token Fetch (Alternative)

If automatic fetching doesn't work, you can manually trigger token fetch:

1. Add a debug button in AdminDashboard
2. Call `authViewModel.fetchAdminToken(context)` directly
3. This bypasses the automatic flow

## 🎯 Integration Verification

### Frontend → Backend Flow:
1. **Admin Login** → Firebase Auth → ID Token Generated
2. **Token Display** → Console + Alert for easy access
3. **TestSprite** → Uses token for API authentication
4. **Backend** → Verifies token with Firebase Admin SDK
5. **API Response** → Authenticated admin operations

### Verification Checklist:
- ✅ Token automatically fetched after admin login
- ✅ Token displayed in console with clear markers
- ✅ Alert dialog shows token with copy instructions
- ✅ TestSprite can use token for API calls
- ✅ All admin endpoints return successful responses
- ✅ No direct Firestore calls in admin dashboard
