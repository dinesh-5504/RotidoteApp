const axios = require('axios');
require('dotenv').config();

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
const FIREBASE_API_KEY = process.env.FIREBASE_API_KEY;

async function testAuth() {
    console.log('🧪 Testing Authentication Endpoints with Firebase ID Tokens...\n');
    
    if (!FIREBASE_API_KEY) {
        console.error('❌ FIREBASE_API_KEY environment variable is required');
        process.exit(1);
    }
    
    try {
        // Test 1: Signup
        console.log('1️⃣ Testing Signup...');
        const signupData = {
            email: 'test@example.com',
            password: 'password123',
            name: 'Test User',
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
        
        // Test 2: Login via Firebase Auth REST API to get ID token
        console.log('\n2️⃣ Testing Login via Firebase Auth REST API...');
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
        
        // Test 3: Use ID token to get profile
        console.log('\n3️⃣ Testing Profile with ID Token...');
        const profileResponse = await axios.get(`${BASE_URL}/auth/profile`, {
            headers: { Authorization: `Bearer ${idToken}` }
        });
        console.log('✅ Profile fetch successful:', {
            userId: profileResponse.data.user.id,
            name: profileResponse.data.user.name,
            profileComplete: profileResponse.data.profileComplete
        });

        // Test 4: Test backend login endpoint (should not return token)
        console.log('\n4️⃣ Testing Backend Login Endpoint...');
        const loginData = {
            email: signupData.email,
            password: signupData.password
        };
        
        const loginResponse = await axios.post(`${BASE_URL}/auth/login`, loginData);
        console.log('✅ Backend login successful:', {
            userId: loginResponse.data.user.id,
            profileComplete: loginResponse.data.profileComplete,
            message: loginResponse.data.message
        });
        
        // Test 5: Test invalid token
        console.log('\n5️⃣ Testing Invalid Token...');
        try {
            await axios.get(`${BASE_URL}/auth/profile`, {
                headers: { Authorization: 'Bearer invalid-token' }
            });
            console.log('❌ Should have failed with invalid token');
        } catch (error) {
            if (error.response?.status === 401) {
                console.log('✅ Invalid token correctly rejected:', error.response.data.error);
            } else {
                throw error;
            }
        }
        
        console.log('\n🎉 All authentication tests completed successfully!');
        
    } catch (error) {
        console.error('❌ Test failed:', error.response?.data || error.message);
        process.exit(1);
    }
}

// Run the test
testAuth();

