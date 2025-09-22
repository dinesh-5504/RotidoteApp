// TestSprite - Student Login Flow Test
// Run with: node test-student-flow.js

const fetch = require('node-fetch');
require('dotenv').config();

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';

async function testStudentFlow() {
  console.log('🧪 TestSprite - Student Login Flow Test');
  console.log('=====================================\n');

  try {
    // Test 1: Student Login
    console.log('🔐 Testing student login...');
    const loginResponse = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: 'test@student.com', // Replace with actual test student email
        password: 'testpassword123' // Replace with actual test student password
      })
    });

    const loginData = await loginResponse.json();
    console.log('📊 Login Response:', {
      status: loginResponse.status,
      success: loginResponse.ok,
      message: loginData.message,
      user: loginData.user ? {
        id: loginData.user.id,
        email: loginData.user.email,
        name: loginData.user.name
      } : null,
      profileComplete: loginData.profileComplete
    });

    if (!loginResponse.ok) {
      console.log('❌ Login failed:', loginData.error);
      return;
    }

    // Test 2: Get Firebase ID Token (simulate what frontend does)
    console.log('\n🔑 Getting Firebase ID token...');
    const firebaseAuthUrl = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${process.env.FIREBASE_API_KEY}`;
    
    const firebaseResponse = await fetch(firebaseAuthUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: 'test@student.com', // Replace with actual test student email
        password: 'testpassword123', // Replace with actual test student password
        returnSecureToken: true
      })
    });

    const firebaseData = await firebaseResponse.json();
    console.log('📊 Firebase Auth Response:', {
      status: firebaseResponse.status,
      success: firebaseResponse.ok,
      localId: firebaseData.localId,
      email: firebaseData.email,
      idToken: firebaseData.idToken ? 'Present' : 'Missing'
    });

    if (!firebaseResponse.ok) {
      console.log('❌ Firebase auth failed:', firebaseData.error);
      return;
    }

    // Test 3: Check Permitted Days
    console.log('\n📅 Testing permitted days API...');
    const permittedDaysResponse = await fetch(`${BASE_URL}/students/permitted-days`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${firebaseData.idToken}`,
        'Content-Type': 'application/json',
      }
    });

    const permittedDaysData = await permittedDaysResponse.json();
    console.log('📊 Permitted Days Response:', {
      status: permittedDaysResponse.status,
      success: permittedDaysResponse.ok,
      permittedDays: permittedDaysData.permittedDays || [],
      totalDays: permittedDaysData.totalDays || 0,
      error: permittedDaysData.error
    });

    if (permittedDaysResponse.ok && permittedDaysData.permittedDays && permittedDaysData.permittedDays.length > 0) {
      console.log('✅ Student has permitted days - should redirect to DaySessionsHome');
    } else {
      console.log('ℹ️ Student has no permitted days - should redirect to regular Home');
    }

    console.log('\n🎉 Student flow test completed!');

  } catch (error) {
    console.log('\n❌ Test failed with error:', error.message);
  }
}

// Run the test
if (require.main === module) {
  testStudentFlow().catch(console.error);
}

module.exports = testStudentFlow;
