#!/usr/bin/env node

const axios = require('axios');
require('dotenv').config();
const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
const FIREBASE_API_KEY = process.env.FIREBASE_API_KEY;

async function quickTest() {
  console.log('🚀 Quick Authentication Test with Firebase ID Tokens\n');
  console.log(`📍 Testing: ${BASE_URL}\n`);

  if (!FIREBASE_API_KEY) {
    console.error('❌ FIREBASE_API_KEY environment variable is required');
    process.exit(1);
  }

  try {
    // Test 1: Health check
    console.log('1️⃣ Testing health endpoint...');
    const healthResponse = await axios.get(`${BASE_URL}/health`);
    console.log('✅ Health check passed:', healthResponse.data.status);

    // Test 2: Signup
    console.log('\n2️⃣ Testing signup...');
    const signupData = {
      email: `test-${Date.now()}@example.com`,
      password: 'password123',
      name: 'Quick Test User',
      grade: '10',
      section: 'A',
      schoolName: 'Test School'
    };

    const signupResponse = await axios.post(`${BASE_URL}/auth/signup`, signupData);
    console.log('✅ Signup successful:', {
      userId: signupResponse.data.user.id,
      email: signupResponse.data.user.email,
      profileComplete: signupResponse.data.profileComplete
    });

    // Test 3: Get ID token via Firebase Auth REST API
    console.log('\n3️⃣ Getting ID token via Firebase Auth REST API...');
    const firebaseAuthUrl = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FIREBASE_API_KEY}`;
    
    const firebaseLoginResponse = await axios.post(firebaseAuthUrl, {
      email: signupData.email,
      password: signupData.password,
      returnSecureToken: true
    });
    
    const idToken = firebaseLoginResponse.data.idToken;
    console.log('✅ Firebase Auth login successful:', {
      userId: firebaseLoginResponse.data.localId,
      hasIdToken: !!idToken
    });

    // Test 4: Profile fetch with ID token
    console.log('\n4️⃣ Testing profile endpoint with ID token...');
    const profileResponse = await axios.get(`${BASE_URL}/auth/profile`, {
      headers: { 'Authorization': `Bearer ${idToken}` }
    });
    console.log('✅ Profile fetch successful:', {
      userId: profileResponse.data.user.id,
      name: profileResponse.data.user.name,
      profileComplete: profileResponse.data.profileComplete
    });

    // Test 5: Backend login endpoint
    console.log('\n5️⃣ Testing backend login endpoint...');
    const loginResponse = await axios.post(`${BASE_URL}/auth/login`, {
      email: signupData.email,
      password: signupData.password
    });
    console.log('✅ Backend login successful:', {
      userId: loginResponse.data.user.id,
      profileComplete: loginResponse.data.profileComplete,
      message: loginResponse.data.message
    });

    console.log('\n🎉 All quick tests passed! Authentication system is working correctly with Firebase ID tokens.');

  } catch (error) {
    console.error('\n❌ Quick test failed:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    } else {
      console.error('Error:', error.message);
    }
    process.exit(1);
  }
}

quickTest();