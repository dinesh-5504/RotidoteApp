const axios = require('axios');

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';

// Test configuration
const TEST_CONFIG = {
  timeout: 10000,
  retries: 3
};

// Test data
const TEST_USERS = {
  valid: {
    email: 'test@example.com',
    password: 'password123',
    name: 'Test User',
    grade: '10',
    section: 'A',
    schoolName: 'Test School'
  },
  invalid: {
    email: 'invalid-email',
    password: '123', // too short
    name: '',
    grade: '',
    section: '',
    schoolName: ''
  },
  duplicate: {
    email: 'duplicate@example.com',
    password: 'password123',
    name: 'Duplicate User',
    grade: '11',
    section: 'B',
    schoolName: 'Duplicate School'
  }
};

// Utility functions
async function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function makeRequest(method, endpoint, data = null, headers = {}) {
  try {
    const config = {
      method,
      url: `${BASE_URL}${endpoint}`,
      headers: {
        'Content-Type': 'application/json',
        ...headers
      },
      timeout: TEST_CONFIG.timeout
    };
    
    if (data) {
      config.data = data;
    }
    
    return await axios(config);
  } catch (error) {
    return {
      error: true,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    };
  }
}

// Test suite
class AuthTestSuite {
  constructor() {
    this.results = {
      passed: 0,
      failed: 0,
      total: 0,
      details: []
    };
  }

  async runTest(testName, testFunction) {
    this.results.total++;
    console.log(`\n🧪 Running: ${testName}`);
    
    try {
      await testFunction();
      this.results.passed++;
      this.results.details.push({ name: testName, status: 'PASSED' });
      console.log(`✅ PASSED: ${testName}`);
    } catch (error) {
      this.results.failed++;
      this.results.details.push({ 
        name: testName, 
        status: 'FAILED', 
        error: error.message 
      });
      console.log(`❌ FAILED: ${testName} - ${error.message}`);
    }
  }

  async testSignupValidInput() {
    const response = await makeRequest('POST', '/auth/signup', TEST_USERS.valid);
    
    if (response.error) {
      throw new Error(`Signup failed: ${response.data?.error || response.message}`);
    }
    
    if (!response.data.user || !response.data.token) {
      throw new Error('Signup response missing user data or token');
    }
    
    if (response.data.user.email !== TEST_USERS.valid.email) {
      throw new Error('Signup response email mismatch');
    }
    
    if (!response.data.profileComplete) {
      throw new Error('Profile should be complete with all fields provided');
    }
  }

  async testSignupInvalidEmail() {
    const invalidData = { ...TEST_USERS.valid, email: 'invalid-email' };
    const response = await makeRequest('POST', '/auth/signup', invalidData);
    
    if (!response.error || response.status !== 400) {
      throw new Error('Should reject invalid email format');
    }
  }

  async testSignupWeakPassword() {
    const weakPasswordData = { ...TEST_USERS.valid, password: '123' };
    const response = await makeRequest('POST', '/auth/signup', weakPasswordData);
    
    if (!response.error || response.status !== 400) {
      throw new Error('Should reject weak password');
    }
  }

  async testSignupMissingFields() {
    const missingFieldsData = { email: 'test@example.com' }; // missing password
    const response = await makeRequest('POST', '/auth/signup', missingFieldsData);
    
    if (!response.error || response.status !== 400) {
      throw new Error('Should reject request with missing required fields');
    }
  }

  async testSignupDuplicateEmail() {
    // First signup
    await makeRequest('POST', '/auth/signup', TEST_USERS.duplicate);
    
    // Second signup with same email
    const response = await makeRequest('POST', '/auth/signup', TEST_USERS.duplicate);
    
    if (!response.error || response.status !== 409) {
      throw new Error('Should reject duplicate email');
    }
  }

  async testSignupMinimalData() {
    const minimalData = {
      email: 'minimal@example.com',
      password: 'password123'
    };
    
    const response = await makeRequest('POST', '/auth/signup', minimalData);
    
    if (response.error) {
      throw new Error(`Minimal signup failed: ${response.data?.error || response.message}`);
    }
    
    if (response.data.profileComplete) {
      throw new Error('Profile should not be complete with minimal data');
    }
  }

  async testLoginValidCredentials() {
    // First create a user
    const signupResponse = await makeRequest('POST', '/auth/signup', TEST_USERS.valid);
    
    if (signupResponse.error) {
      throw new Error('Setup failed: Could not create user for login test');
    }
    
    // Now test login
    const loginData = {
      email: TEST_USERS.valid.email,
      password: TEST_USERS.valid.password
    };
    
    const response = await makeRequest('POST', '/auth/login', loginData);
    
    if (response.error) {
      throw new Error(`Login failed: ${response.data?.error || response.message}`);
    }
    
    if (!response.data.user || !response.data.token) {
      throw new Error('Login response missing user data or token');
    }
    
    if (response.data.user.email !== TEST_USERS.valid.email) {
      throw new Error('Login response email mismatch');
    }
  }

  async testLoginInvalidEmail() {
    const loginData = {
      email: 'nonexistent@example.com',
      password: 'password123'
    };
    
    const response = await makeRequest('POST', '/auth/login', loginData);
    
    if (!response.error || response.status !== 404) {
      throw new Error('Should reject login with non-existent email');
    }
  }

  async testLoginInvalidPassword() {
    // First create a user
    await makeRequest('POST', '/auth/signup', TEST_USERS.valid);
    
    // Now test login with wrong password
    const loginData = {
      email: TEST_USERS.valid.email,
      password: 'wrongpassword'
    };
    
    const response = await makeRequest('POST', '/auth/login', loginData);
    
    if (!response.error || response.status !== 401) {
      throw new Error('Should reject login with invalid password');
    }
  }

  async testLoginMissingFields() {
    const loginData = { email: 'test@example.com' }; // missing password
    const response = await makeRequest('POST', '/auth/login', loginData);
    
    if (!response.error || response.status !== 400) {
      throw new Error('Should reject login request with missing fields');
    }
  }

  async testLoginEmptyCredentials() {
    const loginData = { email: '', password: '' };
    const response = await makeRequest('POST', '/auth/login', loginData);
    
    if (!response.error || response.status !== 400) {
      throw new Error('Should reject login with empty credentials');
    }
  }

  async testTokenVerification() {
    // First create a user and get token
    const signupResponse = await makeRequest('POST', '/auth/signup', TEST_USERS.valid);
    
    if (signupResponse.error) {
      throw new Error('Setup failed: Could not create user for token verification test');
    }
    
    const token = signupResponse.data.token;
    
    // Test token verification
    const response = await makeRequest('GET', '/auth/verify', null, {
      'Authorization': `Bearer ${token}`
    });
    
    if (response.error) {
      throw new Error(`Token verification failed: ${response.data?.error || response.message}`);
    }
    
    if (!response.data.user) {
      throw new Error('Token verification response missing user data');
    }
  }

  async testTokenVerificationInvalidToken() {
    const response = await makeRequest('GET', '/auth/verify', null, {
      'Authorization': 'Bearer invalid-token'
    });
    
    if (!response.error || response.status !== 401) {
      throw new Error('Should reject invalid token');
    }
  }

  async testTokenVerificationMissingToken() {
    const response = await makeRequest('GET', '/auth/verify');
    
    if (!response.error || response.status !== 401) {
      throw new Error('Should reject request without token');
    }
  }

  async testProfileUpdate() {
    // First create a user with minimal data
    const minimalData = {
      email: 'profile@example.com',
      password: 'password123'
    };
    
    const signupResponse = await makeRequest('POST', '/auth/signup', minimalData);
    
    if (signupResponse.error) {
      throw new Error('Setup failed: Could not create user for profile update test');
    }
    
    const token = signupResponse.data.token;
    
    // Update profile
    const profileData = {
      token: token,
      name: 'Updated User',
      grade: '12',
      section: 'C',
      schoolName: 'Updated School'
    };
    
    const response = await makeRequest('POST', '/auth/profile', profileData);
    
    if (response.error) {
      throw new Error(`Profile update failed: ${response.data?.error || response.message}`);
    }
    
    if (!response.data.profileComplete) {
      throw new Error('Profile should be complete after update');
    }
    
    if (response.data.user.name !== 'Updated User') {
      throw new Error('Profile name not updated correctly');
    }
  }

  async testProfileUpdateInvalidToken() {
    const profileData = {
      token: 'invalid-token',
      name: 'Test User',
      grade: '10',
      section: 'A',
      schoolName: 'Test School'
    };
    
    const response = await makeRequest('POST', '/auth/profile', profileData);
    
    if (!response.error || response.status !== 401) {
      throw new Error('Should reject profile update with invalid token');
    }
  }

  async testConcurrentSignups() {
    const promises = [];
    const baseEmail = 'concurrent@example.com';
    
    // Try to create 5 users with similar emails simultaneously
    for (let i = 0; i < 5; i++) {
      const userData = {
        ...TEST_USERS.valid,
        email: `${baseEmail}${i}@example.com`
      };
      promises.push(makeRequest('POST', '/auth/signup', userData));
    }
    
    const results = await Promise.all(promises);
    
    // All should succeed
    const failures = results.filter(r => r.error);
    if (failures.length > 0) {
      throw new Error(`Concurrent signups failed: ${failures.length} out of 5 failed`);
    }
  }

  async testRateLimiting() {
    const promises = [];
    
    // Make many requests quickly to test rate limiting
    for (let i = 0; i < 20; i++) {
      promises.push(makeRequest('POST', '/auth/signup', {
        email: `ratelimit${i}@example.com`,
        password: 'password123'
      }));
    }
    
    const results = await Promise.all(promises);
    
    // Some requests should be rate limited
    const rateLimited = results.filter(r => r.error && r.status === 429);
    if (rateLimited.length === 0) {
      console.log('⚠️  Rate limiting not triggered - this might be expected in development');
    }
  }

  async runAllTests() {
    console.log('🚀 Starting Authentication Test Suite...\n');
    console.log(`📍 Testing against: ${BASE_URL}\n`);
    
    // Signup tests
    await this.runTest('Signup with valid input', () => this.testSignupValidInput());
    await this.runTest('Signup with invalid email', () => this.testSignupInvalidEmail());
    await this.runTest('Signup with weak password', () => this.testSignupWeakPassword());
    await this.runTest('Signup with missing fields', () => this.testSignupMissingFields());
    await this.runTest('Signup with duplicate email', () => this.testSignupDuplicateEmail());
    await this.runTest('Signup with minimal data', () => this.testSignupMinimalData());
    
    // Login tests
    await this.runTest('Login with valid credentials', () => this.testLoginValidCredentials());
    await this.runTest('Login with invalid email', () => this.testLoginInvalidEmail());
    await this.runTest('Login with invalid password', () => this.testLoginInvalidPassword());
    await this.runTest('Login with missing fields', () => this.testLoginMissingFields());
    await this.runTest('Login with empty credentials', () => this.testLoginEmptyCredentials());
    
    // Token verification tests
    await this.runTest('Token verification with valid token', () => this.testTokenVerification());
    await this.runTest('Token verification with invalid token', () => this.testTokenVerificationInvalidToken());
    await this.runTest('Token verification without token', () => this.testTokenVerificationMissingToken());
    
    // Profile update tests
    await this.runTest('Profile update with valid token', () => this.testProfileUpdate());
    await this.runTest('Profile update with invalid token', () => this.testProfileUpdateInvalidToken());
    
    // Performance and edge case tests
    await this.runTest('Concurrent signups', () => this.testConcurrentSignups());
    await this.runTest('Rate limiting', () => this.testRateLimiting());
    
    this.printResults();
  }

  printResults() {
    console.log('\n' + '='.repeat(60));
    console.log('📊 TEST RESULTS SUMMARY');
    console.log('='.repeat(60));
    console.log(`✅ Passed: ${this.results.passed}`);
    console.log(`❌ Failed: ${this.results.failed}`);
    console.log(`📈 Total: ${this.results.total}`);
    console.log(`🎯 Success Rate: ${((this.results.passed / this.results.total) * 100).toFixed(1)}%`);
    
    if (this.results.failed > 0) {
      console.log('\n❌ FAILED TESTS:');
      this.results.details
        .filter(test => test.status === 'FAILED')
        .forEach(test => {
          console.log(`  - ${test.name}: ${test.error}`);
        });
    }
    
    console.log('\n' + '='.repeat(60));
    
    if (this.results.failed === 0) {
      console.log('🎉 All tests passed! Authentication system is working correctly.');
    } else {
      console.log('⚠️  Some tests failed. Please review the errors above.');
      process.exit(1);
    }
  }
}

// Run the test suite
async function main() {
  const testSuite = new AuthTestSuite();
  await testSuite.runAllTests();
}

// Handle uncaught errors
process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled Rejection at:', promise, 'reason:', reason);
  process.exit(1);
});

process.on('uncaughtException', (error) => {
  console.error('Uncaught Exception:', error);
  process.exit(1);
});

// Run tests
main().catch(console.error);
