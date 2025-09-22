# Student Login Flow Testing Instructions

## Issue Fixed
- **Problem**: Student login was not redirecting to DaySessionHome when they have permitted day sessions
- **Root Cause**: The frontend was correctly calling the backend API, but there might be issues with:
  1. Student authentication token
  2. Session data in Firestore
  3. API response parsing

## Debug Logs Added

### Backend Logs (Check server console)
- `🔐 [DEBUG] Student auth attempt` - Shows if token is present
- `✅ [DEBUG] Token verified for user` - Shows successful authentication
- `🔍 [DEBUG] Checking permitted days for user` - Shows user ID being checked
- `📊 [DEBUG] Found X enabled sessions` - Shows how many sessions exist
- `✅ [DEBUG] User is permitted for DayX` - Shows which days user is permitted for
- `📋 [DEBUG] Final permitted days` - Shows final result

### Frontend Logs (Check Android Logcat)
- `🔍 Checking student permitted days...` - Shows API call started
- `📊 API Response - success: X, data: X, error: X` - Shows API response details
- `✅ Student has X permitted days` - Shows successful detection
- `🔄 Auth state changed: X` - Shows navigation decision
- `📅 Day sessions available - navigating to DaySessionsHome` - Shows navigation

## Testing Steps

### 1. Test Backend API Directly
```bash
cd backend
node test-student-flow.js
```

### 2. Test with Real Student
1. **Create a test student** in Firebase Auth with email/password
2. **Add student to Firestore** with complete profile (name, grade, section, schoolName)
3. **Create a session** in Firestore:
   ```javascript
   // In Firestore console, create document in 'sessions' collection
   {
     dayId: "Day1",
     title: "Day 1 Session",
     enabled: true,
     videos: [],
     permittedStudents: ["STUDENT_UID_HERE"] // Add the student's UID
   }
   ```
4. **Test login** with the student credentials

### 3. Check Logs
- **Backend**: Check server console for debug logs
- **Frontend**: Check Android Logcat for debug logs
- **Filter Logcat**: Use tag "AuthViewModel" or "MainActivity"

## Expected Flow

### Admin Login
1. Enter admin credentials → AdminDashboard

### Student with Day Sessions
1. Enter student credentials → DaySessionHome

### Student without Day Sessions  
1. Enter student credentials → HomeScreen

### Student with Incomplete Profile
1. Enter student credentials → ProfileSetup

## Troubleshooting

### If student stays on login screen:
1. Check if student exists in Firebase Auth
2. Check if student profile exists in Firestore
3. Check if session exists and is enabled
4. Check if student UID is in permittedStudents array
5. Check backend logs for authentication errors
6. Check frontend logs for API response errors

### If student goes to wrong screen:
1. Check AuthViewModel logs for API response
2. Check MainActivity logs for navigation decision
3. Verify session data in Firestore

## Test Data Setup

### Create Test Session in Firestore:
```javascript
// Collection: sessions
// Document ID: Day1
{
  dayId: "Day1",
  title: "Day 1 Session",
  enabled: true,
  videos: [],
  permittedStudents: ["STUDENT_UID_HERE"],
  createdAt: 1234567890
}
```

### Create Test Student in Firestore:
```javascript
// Collection: users  
// Document ID: STUDENT_UID_HERE
{
  email: "test@student.com",
  name: "Test Student",
  grade: "10",
  section: "A",
  schoolName: "Test School",
  createdAt: 1234567890
}
```
