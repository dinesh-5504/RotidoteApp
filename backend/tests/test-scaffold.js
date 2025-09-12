#!/usr/bin/env node

const axios = require('axios');

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
const FIREBASE_API_KEY = process.env.FIREBASE_API_KEY;

async function testScaffoldedAuth() {
  console.log('🧪 Testing Scaffolded Authentication System with Firebase ID Tokens\n');
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

    // Test 2: Base route
    console.log('\n2️⃣ Testing base route...');
    const baseResponse = await axios.get(`${BASE_URL}/`);
    console.log('✅ Base route working:', baseResponse.data.message);
    console.log('📋 Available endpoints:', baseResponse.data.endpoints);

    // Test 3: Signup
    console.log('\n3️⃣ Testing signup endpoint...');
    const signupData = {
      email: `scaffold-test-${Date.now()}@example.com`,
      password: 'password123',
      name: 'Scaffold Test User',
      grade: '10',
      section: 'A',
      schoolName: 'Test School'
    };

    const signupResponse = await axios.post(`${BASE_URL}/auth/signup`, signupData);
    console.log('✅ Signup successful:', {
      userId: signupResponse.data.user.id,
      email: signupResponse.data.user.email,
      profileComplete: signupResponse.data.profileComplete,
      hasToken: !!signupResponse.data.token
    });

    // Test 4: Get ID token via Firebase Auth REST API
    console.log('\n4️⃣ Getting ID token via Firebase Auth REST API...');
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

    // Test 5: Profile fetch with ID token
    console.log('\n5️⃣ Testing profile endpoint with ID token...');
    const profileResponse = await axios.get(`${BASE_URL}/auth/profile`, {
      headers: { 'Authorization': `Bearer ${idToken}` }
    });
    console.log('✅ Profile fetch successful:', {
      userId: profileResponse.data.user.id,
      name: profileResponse.data.user.name,
      profileComplete: profileResponse.data.profileComplete
    });

    // Test 6: Backend login endpoint
    console.log('\n6️⃣ Testing backend login endpoint...');
    const loginResponse = await axios.post(`${BASE_URL}/auth/login`, {
      email: signupData.email,
      password: signupData.password
    });
    console.log('✅ Backend login successful:', {
      userId: loginResponse.data.user.id,
      profileComplete: loginResponse.data.profileComplete,
      message: loginResponse.data.message
    });

    console.log('\n🎉 All scaffolded authentication tests passed!');
    console.log('✅ The authentication system is properly scaffolded and working with Firebase ID tokens.');

  } catch (error) {
    console.error('\n❌ Scaffold test failed:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    } else {
      console.error('Error:', error.message);
    }
    
    if (error.code === 'ECONNREFUSED') {
      console.log('\n💡 Make sure the server is running:');
      console.log('   npm start');
      console.log('   or');
      console.log('   npm run dev');
    }
    
    process.exit(1);
  }
}

testScaffoldedAuth();
