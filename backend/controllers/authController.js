const { auth, firestore } = require('../config/firebase');

// POST /auth/signup
// Creates a new user in Firebase Auth with email + password
// Stores extra details (name, grade, section, schoolName) in Firestore
const signup = async (req, res) => {
  try {
    const { email, password, name, grade, section, schoolName } = req.body;

    // Validate required fields
    if (!email || !password) {
      return res.status(400).json({
        error: 'Email and password are required'
      });
    }

    // Create user with Firebase Auth
    const userRecord = await auth.createUser({
      email,
      password,
      displayName: name || ''
    });

    // Create user profile in Firestore
    const userProfile = {
      id: userRecord.uid,
      email: userRecord.email,
      name: name || '',
      grade: grade || '',
      section: section || '',
      schoolName: schoolName || '',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    await firestore.collection('users').doc(userRecord.uid).set(userProfile);

    // Create custom token for the user
    const customToken = await auth.createCustomToken(userRecord.uid);

    res.status(201).json({
      message: 'User created successfully',
      user: {
        id: userRecord.uid,
        email: userRecord.email,
        name: userProfile.name,
        grade: userProfile.grade,
        section: userProfile.section,
        schoolName: userProfile.schoolName
      },
      token: customToken,
      profileComplete: !!(name && grade && section && schoolName)
    });

  } catch (error) {
    console.error('Signup error:', error);
    
    if (error.code === 'auth/email-already-exists') {
      return res.status(409).json({
        error: 'User with this email already exists'
      });
    }
    
    if (error.code === 'auth/invalid-email') {
      return res.status(400).json({
        error: 'Invalid email format'
      });
    }
    
    if (error.code === 'auth/weak-password') {
      return res.status(400).json({
        error: 'Password is too weak'
      });
    }

    res.status(500).json({
      error: 'Failed to create user',
      details: error.message
    });
  }
};

// POST /auth/login
// Confirms that the user exists in Firebase Auth
// Client should fetch ID token via Firebase Auth REST API separately
const login = async (req, res) => {
  try {
    const { email, password } = req.body;

    // Validate required fields
    if (!email || !password) {
      return res.status(400).json({
        error: 'Email and password are required'
      });
    }

    // Use Firebase Auth REST API to verify credentials
    const firebaseAuthUrl = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${process.env.FIREBASE_API_KEY}`;
    
    const authResponse = await fetch(firebaseAuthUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: email,
        password: password,
        returnSecureToken: true
      })
    });

    const authData = await authResponse.json();

    if (!authResponse.ok) {
      if (authData.error?.message === 'INVALID_PASSWORD') {
        return res.status(401).json({
          error: 'Invalid password'
        });
      }
      if (authData.error?.message === 'EMAIL_NOT_FOUND') {
        return res.status(404).json({
          error: 'User not found'
        });
      }
      return res.status(400).json({
        error: authData.error?.message || 'Authentication failed'
      });
    }

    const userId = authData.localId;

    // Get user profile from Firestore
    const userDoc = await firestore.collection('users').doc(userId).get();
    
    if (!userDoc.exists) {
      return res.status(404).json({
        error: 'User profile not found'
      });
    }

    const userProfile = userDoc.data();

    res.json({
      message: 'Login successful - use Firebase Auth REST API to get ID token',
      user: {
        id: userId,
        email: userProfile.email,
        name: userProfile.name,
        grade: userProfile.grade,
        section: userProfile.section,
        schoolName: userProfile.schoolName
      },
      profileComplete: !!(userProfile.name && userProfile.grade && userProfile.section && userProfile.schoolName),
      firebaseAuthUrl: `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${process.env.FIREBASE_API_KEY}`
    });

  } catch (error) {
    console.error('Login error:', error);
    
    res.status(500).json({
      error: 'Failed to authenticate user',
      details: error.message
    });
  }
};

// GET /auth/profile
// Validates ID token (Firebase Admin SDK)
// Fetches Firestore user document and returns user profile
const getProfile = async (req, res) => {
  try {
    const token = req.headers.authorization?.replace('Bearer ', '');

    if (!token) {
      return res.status(401).json({
        error: 'Authentication token required'
      });
    }

    // Verify the token
    const decodedToken = await auth.verifyIdToken(token);
    const userId = decodedToken.uid;

    // Get user profile from Firestore
    const userDoc = await firestore.collection('users').doc(userId).get();
    
    if (!userDoc.exists) {
      return res.status(404).json({
        error: 'User profile not found'
      });
    }

    const userProfile = userDoc.data();

    res.json({
      user: {
        id: userId,
        email: userProfile.email,
        name: userProfile.name,
        grade: userProfile.grade,
        section: userProfile.section,
        schoolName: userProfile.schoolName
      },
      profileComplete: !!(userProfile.name && userProfile.grade && userProfile.section && userProfile.schoolName)
    });

  } catch (error) {
    console.error('Profile fetch error:', error);
    
    if (error.code === 'auth/id-token-expired') {
      return res.status(401).json({
        error: 'Token expired'
      });
    }
    
    if (error.code === 'auth/invalid-id-token') {
      return res.status(401).json({
        error: 'Invalid token'
      });
    }

    res.status(500).json({
      error: 'Failed to fetch profile',
      details: error.message
    });
  }
};

module.exports = {
  signup,
  login,
  getProfile
};
