# Backend Authentication Scaffold

This is a clean, scaffolded backend authentication system using Firebase Auth + Firestore with Node.js and Express.js.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Express.js    │    │ Firebase Admin  │    │    Firestore    │
│   Server        │───▶│      SDK        │───▶│   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Auth Routes   │    │ Firebase Auth   │    │  User Profiles  │
│   /auth/*       │    │   REST API      │    │   Collection    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📁 File Structure

```
backend/
├── server.js                    # Main entry point
├── routes/
│   └── auth.js                  # Express router for auth endpoints
├── controllers/
│   └── authController.js        # Business logic for authentication
├── config/
│   └── firebase.js              # Firebase Admin SDK initialization
├── package.json                 # Dependencies and scripts
└── .env                         # Environment variables
```

## 🚀 Quick Start

### 1. Install Dependencies

```bash
cd backend
npm install
```

### 2. Set Up Environment Variables

Create a `.env` file:

```env
# Firebase Configuration
FIREBASE_WEB_API_KEY=your_firebase_web_api_key_here
FIREBASE_PROJECT_ID=rotidote-database

# Firebase Admin SDK (choose one method)
# Method 1: Service Account Key File
GOOGLE_APPLICATION_CREDENTIALS=path/to/serviceAccountKey.json

# Method 2: Environment Variables (for production)
# FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n..."
# FIREBASE_CLIENT_EMAIL=firebase-adminsdk-...@rotidote-database.iam.gserviceaccount.com

# Server Configuration
PORT=3000
NODE_ENV=development

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

### 3. Set Up Firebase

1. **Get Firebase Web API Key:**
   - Go to Firebase Console → Project Settings → General
   - Copy the "Web API Key"

2. **Download Service Account Key:**
   - Go to Firebase Console → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Save as `serviceAccountKey.json` in the backend directory

3. **Enable Authentication:**
   - Go to Firebase Console → Authentication → Sign-in method
   - Enable "Email/Password" provider

### 4. Start the Server

```bash
# Development mode
npm run dev

# Production mode
npm start
```

### 5. Test the System

```bash
# Test the scaffolded authentication
npm run test:scaffold:local
```

## 🔐 API Endpoints

### POST /auth/signup

Creates a new user in Firebase Auth and stores profile data in Firestore.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe",
  "grade": "10",
  "section": "A",
  "schoolName": "Example School"
}
```

**Response:**
```json
{
  "message": "User created successfully",
  "user": {
    "id": "firebase-user-id",
    "email": "user@example.com",
    "name": "John Doe",
    "grade": "10",
    "section": "A",
    "schoolName": "Example School"
  },
  "token": "firebase-custom-token",
  "profileComplete": true
}
```

### POST /auth/login

Signs in user with email and password using Firebase Auth REST API.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "message": "Login successful",
  "user": {
    "id": "firebase-user-id",
    "email": "user@example.com",
    "name": "John Doe",
    "grade": "10",
    "section": "A",
    "schoolName": "Example School"
  },
  "token": "firebase-id-token",
  "profileComplete": true
}
```

### GET /auth/profile

Validates ID token and returns user profile from Firestore.

**Headers:**
```
Authorization: Bearer <firebase-id-token>
```

**Response:**
```json
{
  "user": {
    "id": "firebase-user-id",
    "email": "user@example.com",
    "name": "John Doe",
    "grade": "10",
    "section": "A",
    "schoolName": "Example School"
  },
  "profileComplete": true
}
```

## 🧪 Testing

### Available Test Commands

```bash
# Test scaffolded authentication system
npm run test:scaffold:local

# Test comprehensive authentication suite
npm run test:auth:local

# Quick authentication test
npm run test:quick:local

# Simple step-by-step test
npm run test:auth:simple:local
```

### Manual Testing with curl

```bash
# Health check
curl http://localhost:3000/health

# Signup
curl -X POST http://localhost:3000/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","name":"Test User","grade":"10","section":"A","schoolName":"Test School"}'

# Login
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Get profile (replace TOKEN with actual token)
curl http://localhost:3000/auth/profile \
  -H "Authorization: Bearer TOKEN"
```

## 🔧 Configuration

### Firebase Admin SDK Setup

The system supports two methods for Firebase Admin SDK authentication:

#### Method 1: Service Account Key File (Recommended for Development)

```javascript
// config/firebase.js
app = admin.initializeApp({
  credential: admin.credential.cert(require('../serviceAccountKey.json')),
  projectId: 'rotidote-database'
});
```

#### Method 2: Environment Variables (Recommended for Production)

```javascript
// config/firebase.js
app = admin.initializeApp({
  credential: admin.credential.cert({
    projectId: process.env.FIREBASE_PROJECT_ID,
    privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
  }),
  projectId: process.env.FIREBASE_PROJECT_ID
});
```

### CORS Configuration

```javascript
// server.js
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true
}));
```

### Rate Limiting

```javascript
// server.js
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP, please try again later.'
});
```

## 🛡️ Security Features

- **Helmet.js**: Security headers
- **CORS**: Cross-origin resource sharing protection
- **Rate Limiting**: Prevents abuse
- **Input Validation**: Required field validation
- **Firebase Security**: Server-side authentication
- **Token Verification**: Secure token validation

## 🚀 Deployment

### Environment Variables for Production

```env
NODE_ENV=production
PORT=3000
FIREBASE_WEB_API_KEY=your_production_api_key
FIREBASE_PROJECT_ID=rotidote-database
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n..."
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-...@rotidote-database.iam.gserviceaccount.com
ALLOWED_ORIGINS=https://your-frontend-domain.com
```

### Vercel Deployment

The system is ready for Vercel deployment with the existing `vercel.json` configuration.

## 📝 Error Handling

The system includes comprehensive error handling:

- **400 Bad Request**: Missing required fields, invalid input
- **401 Unauthorized**: Invalid credentials, expired tokens
- **404 Not Found**: User not found, profile not found
- **409 Conflict**: Email already exists
- **429 Too Many Requests**: Rate limit exceeded
- **500 Internal Server Error**: Server-side errors

## 🔍 Troubleshooting

### Common Issues

1. **"Firebase Admin SDK initialization error"**
   - Check if `GOOGLE_APPLICATION_CREDENTIALS` is set correctly
   - Verify service account key file exists and is valid
   - Ensure Firebase project ID is correct

2. **"Authentication failed"**
   - Verify `FIREBASE_WEB_API_KEY` is set correctly
   - Check if Firebase Auth is enabled in the project
   - Ensure email/password provider is enabled

3. **"CORS error"**
   - Update `ALLOWED_ORIGINS` in environment variables
   - Ensure frontend URL is included in the list

4. **"Connection refused"**
   - Ensure server is running on the correct port
   - Check if port 3000 is available
   - Verify BASE_URL in test commands

### Debug Mode

Enable debug logging by setting:

```env
NODE_ENV=development
```

This will show detailed error messages in responses.

## 📚 Next Steps

1. **Add Profile Update Endpoint**: Allow users to update their profile information
2. **Implement Logout**: Add token invalidation
3. **Add Social Authentication**: Google, Facebook, etc.
4. **Implement Refresh Tokens**: For better security
5. **Add Email Verification**: Verify email addresses
6. **Add Password Reset**: Forgot password functionality
7. **Add Multi-factor Authentication**: 2FA support
8. **Add Audit Logging**: Track authentication events

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
