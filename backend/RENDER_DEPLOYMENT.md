# Render Deployment Guide

This guide explains how to deploy the Rotidote backend to Render with Firebase authentication.

## 🚀 Prerequisites

1. **Render Account**: Sign up at [render.com](https://render.com)
2. **Firebase Project**: Ensure you have a Firebase project set up
3. **GitHub Repository**: Push your code to GitHub

## 🔧 Environment Variables Setup

### Required Environment Variables

Set these in your Render dashboard under "Environment":

```env
# Firebase Configuration
FIREBASE_API_KEY=your_firebase_web_api_key_here
FIREBASE_PROJECT_ID=rotidote-database
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@rotidote-database.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nYOUR_PRIVATE_KEY_HERE\n-----END PRIVATE KEY-----\n"

# Server Configuration
NODE_ENV=production
PORT=3000

# CORS Configuration
ALLOWED_ORIGINS=https://rotidoteapp.onrender.com,https://your-frontend-domain.com
```

### Getting Firebase Credentials

1. **Firebase Web API Key**:
   - Go to Firebase Console → Project Settings → General
   - Copy the "Web API Key"

2. **Firebase Admin SDK Credentials**:
   - Go to Firebase Console → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Download the JSON file
   - Extract the following values:
     - `project_id` → `FIREBASE_PROJECT_ID`
     - `client_email` → `FIREBASE_CLIENT_EMAIL`
     - `private_key` → `FIREBASE_PRIVATE_KEY`

## 📦 Deployment Steps

### 1. Create New Web Service

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click "New +" → "Web Service"
3. Connect your GitHub repository
4. Select your repository and branch

### 2. Configure Service Settings

```yaml
Name: rotidote-backend
Environment: Node
Build Command: npm install
Start Command: npm start
Plan: Free (or paid for production)
```

### 3. Set Environment Variables

Add all the environment variables listed above in the "Environment" section.

### 4. Deploy

1. Click "Create Web Service"
2. Render will automatically build and deploy your application
3. Monitor the build logs for any errors

## 🔍 Post-Deployment Verification

### Test Your Deployment

```bash
# Test basic health check
curl https://rotidoteapp.onrender.com/health

# Test authentication endpoints
npm run test:render
npm run test:auth:render
npm run test:quick:render
npm run test:scaffold:render
```

### Expected Response

```json
{
  "status": "OK",
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

## 🛠️ Troubleshooting

### Common Issues

1. **Build Failures**:
   - Check Node.js version compatibility
   - Ensure all dependencies are in `package.json`
   - Verify build command is correct

2. **Firebase Authentication Errors**:
   - Verify all Firebase environment variables are set correctly
   - Check that `FIREBASE_PRIVATE_KEY` has proper line breaks (`\n`)
   - Ensure Firebase project ID matches your actual project

3. **CORS Errors**:
   - Update `ALLOWED_ORIGINS` with your frontend domain
   - Ensure HTTPS URLs are used in production

4. **Port Issues**:
   - Render automatically sets the PORT environment variable
   - Don't hardcode port numbers in your application

### Debug Commands

```bash
# Check environment variables
echo $FIREBASE_PROJECT_ID
echo $FIREBASE_CLIENT_EMAIL

# Test Firebase connection
node -e "const admin = require('firebase-admin'); console.log('Firebase initialized');"
```

## 📊 Monitoring

### Render Dashboard Features

1. **Logs**: Monitor real-time application logs
2. **Metrics**: View CPU, memory, and response time metrics
3. **Deployments**: Track deployment history and rollback if needed

### Health Checks

Your application includes a health check endpoint:
- **URL**: `https://rotidoteapp.onrender.com/health`
- **Method**: GET
- **Response**: `{"status": "OK", "timestamp": "..."}`

## 🔄 Continuous Deployment

### Automatic Deploys

Render automatically deploys when you push to your connected branch:
1. Push changes to GitHub
2. Render detects the push
3. Builds and deploys automatically
4. Sends notification of deployment status

### Manual Deploys

You can also trigger manual deploys from the Render dashboard:
1. Go to your service dashboard
2. Click "Manual Deploy"
3. Select the branch/commit to deploy

## 🔐 Security Best Practices

### Environment Variables

- Never commit sensitive credentials to Git
- Use Render's environment variable system
- Regularly rotate Firebase service account keys

### Firebase Security Rules

Ensure your Firestore security rules are properly configured:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 📈 Performance Optimization

### Render Free Tier Limitations

- **Sleep Mode**: Free services sleep after 15 minutes of inactivity
- **Cold Starts**: First request after sleep may be slower
- **Build Time**: Limited build minutes per month

### Production Recommendations

1. **Upgrade to Paid Plan**: For production applications
2. **Optimize Dependencies**: Remove unused packages
3. **Enable Caching**: Use appropriate cache headers
4. **Monitor Performance**: Set up alerts for response times

## 🔗 Integration with Frontend

### Update Frontend Configuration

Update your frontend to use the Render URL:

```javascript
// In your frontend configuration
const API_BASE_URL = 'https://rotidoteapp.onrender.com';

// Update CORS origins in backend if needed
ALLOWED_ORIGINS=https://your-frontend-domain.com
```

### Test Integration

```bash
# Test frontend-backend integration
curl -X POST https://rotidoteapp.onrender.com/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","name":"Test User","grade":"10","section":"A","schoolName":"Test School"}'
```

## 📝 Deployment Checklist

- [ ] Firebase project configured
- [ ] Environment variables set in Render
- [ ] GitHub repository connected
- [ ] Build command configured (`npm install`)
- [ ] Start command configured (`npm start`)
- [ ] Health check endpoint working
- [ ] Authentication endpoints tested
- [ ] CORS configured for frontend domain
- [ ] Firebase Admin SDK credentials verified
- [ ] Production environment variables set

## 🆘 Support

### Render Support

- **Documentation**: [render.com/docs](https://render.com/docs)
- **Community**: [Render Community Forum](https://community.render.com)
- **Status Page**: [status.render.com](https://status.render.com)

### Firebase Support

- **Documentation**: [firebase.google.com/docs](https://firebase.google.com/docs)
- **Console**: [console.firebase.google.com](https://console.firebase.google.com)

## ✅ Success!

Once deployed, your backend will be available at:
**https://rotidoteapp.onrender.com**

Test the deployment:
```bash
curl https://rotidoteapp.onrender.com/health
```

Your authentication system is now live and ready to serve your frontend application!
