import express from 'express';
import authController from '../controllers/authController.js';

const router = express.Router();

// POST /auth/signup
// Creates a new user in Firebase Auth with email + password
// Stores extra details (name, grade, section, schoolName) in Firestore
router.post('/signup', authController.signup);

// POST /auth/login  
// Signs in user with email + password using Firebase Auth REST API
// Returns Firebase ID token
router.post('/login', authController.login);

// GET /auth/profile
// Validates ID token (Firebase Admin SDK)
// Fetches Firestore user document and returns user profile
router.get('/profile', authController.getProfile);

export default router;