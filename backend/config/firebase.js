const admin = require('firebase-admin');
const path = require("path");
var serviceAccount = require(path.join(__dirname, "serviceAccountKey.json"));

// admin.initializeApp({
//   credential: admin.credential.cert(serviceAccount)
// });

// Initialize Firebase Admin SDK with service account credentials
// Uses serviceAccountKey.json file for authentication
let app;

try {
  // Try to initialize with existing app
  app = admin.app();
} catch (error) {
  // Initialize new app if none exists
  app = admin.initializeApp({
    //credential: admin.credential.applicationDefault(),
    // Alternative: Use service account key file directly
    credential: admin.credential.cert(serviceAccount),
    projectId: process.env.FIREBASE_PROJECT_ID || 'rotidote-database'
  });
}

// Get Firebase services
const auth = admin.auth();
const firestore = admin.firestore();

// Configure Firestore settings
firestore.settings({
  timestampsInSnapshots: true
});

module.exports = {
  auth,
  firestore,
  admin
};