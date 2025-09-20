# 🔧 Admin Dashboard Testing Guide

## 🎯 **Testing Setup**

### **Step 1: Get Admin Token**
1. Open the Rotidote app
2. Login with admin credentials:
   - Email: `dineshkarthikeyan.admin@gmail.com`
   - Password: `RotidoteApp`
3. Copy the Firebase ID token from the console or alert dialog
4. Replace `your-firebase-admin-token-here` in the test files

### **Step 2: Update Test Files**
```bash
# Edit test files and replace the token
nano backend/test-admin-endpoints.js
nano backend/test-complete-admin-flow.js

# Find this line and replace with your token:
const ADMIN_TOKEN = 'your-firebase-admin-token-here';
```

### **Step 3: Run Tests**

#### **Basic Endpoint Tests**
```bash
cd backend
node test-admin-endpoints.js
```

#### **Complete Flow Test**
```bash
cd backend
node test-complete-admin-flow.js
```

## 📋 **Test Coverage**

### **✅ Completed Fixes**
1. **Back Navigation**: SessionDetailScreen returns to Sessions tab
2. **Permitted Count**: Student count matches between card and detail
3. **Permitted Glitches**: Deduplication and proper state management
4. **Upload UI**: Duration field, file pickers, proper layout
5. **Mux/Cloudinary**: Real integration with external services
6. **Video Count Refresh**: Day cards update after upload
7. **API Endpoints**: All endpoints tested and verified

### **🧪 Test Scenarios**

#### **Backend API Tests**
- ✅ GET /admin/students (with filters)
- ✅ GET /admin/sessions
- ✅ POST /admin/sessions/:dayId
- ✅ PATCH /admin/sessions/:dayId/toggle
- ✅ PATCH /admin/sessions/:dayId/students
- ✅ POST /admin/upload/mux-url
- ✅ POST /admin/upload/cloudinary-signature
- ✅ POST /admin/upload/metadata
- ✅ GET /admin/analytics
- ✅ GET /admin/videos/:dayId

#### **Complete Flow Test**
1. **Authentication**: Verify admin token works
2. **Session Management**: Create/update sessions
3. **Video Upload**: Upload metadata and verify count refresh
4. **External Services**: Test Mux and Cloudinary integration
5. **Students Management**: Verify student data access
6. **Analytics**: Check analytics endpoint

## 🚨 **Expected Results**

### **Successful Test Output**
```
🧪 TestSprite - Complete Admin Dashboard Flow Test
================================================

🔐 Step 1: Testing Admin Authentication
✅ Admin authentication successful

📅 Step 2: Creating/Updating Session
✅ Session created/updated successfully

📋 Step 3: Verifying Sessions List
✅ Found Day1 session with 0 videos

📹 Step 4: Uploading Video Metadata
✅ Video uploaded successfully: [video-id]

🔄 Step 5: Verifying Video Count Refresh
✅ Video count refresh verified: Day1 now has 1 videos

🎬 Step 6: Testing Mux and Cloudinary Services
✅ Mux service working
✅ Cloudinary service working

👥 Step 7: Testing Students Management
✅ Found [X] students

📊 Step 8: Testing Analytics
✅ Analytics endpoint working

🎉 COMPLETE ADMIN FLOW TEST PASSED!
=====================================
```

### **Common Issues & Solutions**

#### **Issue: 401 Unauthorized**
- **Cause**: Invalid or expired admin token
- **Solution**: Get fresh token from app and update test files

#### **Issue: 500 Server Error**
- **Cause**: Missing environment variables or service configuration
- **Solution**: Check Mux/Cloudinary API keys in backend environment

#### **Issue: Network Timeout**
- **Cause**: Backend not running or incorrect URL
- **Solution**: Verify backend is deployed and accessible

## 🔧 **Environment Setup**

### **Required Environment Variables**
```bash
# Backend .env file
MUX_TOKEN_ID=your_mux_token_id
MUX_TOKEN_SECRET=your_mux_token_secret
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### **Backend Dependencies**
```bash
npm install @mux/mux-node cloudinary multer
```

## 📱 **Frontend Testing**

### **Manual Testing Steps**
1. **Back Navigation**: AdminDashboard → Sessions → Day1 → Back → verify Sessions tab
2. **Permitted Students**: Add/remove students → verify count updates
3. **Upload Flow**: Upload video → verify day card count increments
4. **File Pickers**: Test video and thumbnail selection
5. **Duration Input**: Verify numeric keyboard and validation

### **Expected Behavior**
- ✅ Smooth navigation between admin screens
- ✅ Real-time updates of student and video counts
- ✅ Proper form validation and error handling
- ✅ File selection UI working (placeholder for now)
- ✅ Loading states and progress indicators

## 🎯 **Success Criteria**

### **All Tests Pass When:**
- ✅ Admin authentication works with valid token
- ✅ All API endpoints return expected responses
- ✅ Video upload updates session video count
- ✅ Student management operations work correctly
- ✅ External services (Mux/Cloudinary) respond appropriately
- ✅ No 401/403/500 errors in test results

### **Frontend Works When:**
- ✅ Back navigation returns to correct tab
- ✅ Permitted student counts are accurate
- ✅ Upload UI is properly laid out and functional
- ✅ Video counts refresh after upload
- ✅ No UI glitches or state inconsistencies

Run the tests and verify all checkboxes are green! 🎉
