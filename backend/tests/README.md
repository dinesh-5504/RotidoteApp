# Authentication Test Suite

This directory contains comprehensive tests for the authentication endpoints of the Rotidote backend API.

## Test Files

- `auth.test.js` - Comprehensive test suite for all authentication endpoints
- `README.md` - This documentation file

## Test Coverage

The test suite covers the following authentication endpoints:

### Signup Endpoint (`POST /auth/signup`)
- ✅ Valid input with complete profile
- ✅ Valid input with minimal data (email + password only)
- ❌ Invalid email format
- ❌ Weak password (too short)
- ❌ Missing required fields
- ❌ Duplicate email addresses

### Login Endpoint (`POST /auth/login`)
- ✅ Valid credentials
- ❌ Non-existent email
- ❌ Invalid password
- ❌ Missing fields
- ❌ Empty credentials

### Token Verification (`GET /auth/verify`)
- ✅ Valid token
- ❌ Invalid token
- ❌ Missing token

### Profile Update (`POST /auth/profile`)
- ✅ Valid token with profile data
- ❌ Invalid token

### Performance & Edge Cases
- ✅ Concurrent signups
- ⚠️ Rate limiting (may not trigger in development)

## Running Tests

### Prerequisites
1. Backend server must be running
2. Firebase Admin SDK must be properly configured
3. Environment variables must be set

### Test Commands

```bash
# Run comprehensive test suite against local server
npm run test:auth:local

# Run comprehensive test suite against deployed server
npm run test:auth:vercel

# Run simple test script
npm run test:auth:simple:local

# Run tests with custom base URL
BASE_URL=http://localhost:3000 npm run test:auth
```

### Test Configuration

The tests use the following configuration:
- **Timeout**: 10 seconds per request
- **Retries**: 3 attempts for failed requests
- **Base URL**: Configurable via `BASE_URL` environment variable

## Test Data

The test suite uses predefined test data:

```javascript
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
    password: '123',
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
```

## Expected Results

### Successful Test Run
```
🚀 Starting Authentication Test Suite...

📍 Testing against: http://localhost:3000

🧪 Running: Signup with valid input
✅ PASSED: Signup with valid input

🧪 Running: Signup with invalid email
✅ PASSED: Signup with invalid email

... (more tests)

============================================================
📊 TEST RESULTS SUMMARY
============================================================
✅ Passed: 16
❌ Failed: 0
📈 Total: 16
🎯 Success Rate: 100.0%

🎉 All tests passed! Authentication system is working correctly.
```

### Failed Test Run
```
❌ FAILED: Login with invalid password - Should reject login with invalid password

============================================================
📊 TEST RESULTS SUMMARY
============================================================
✅ Passed: 15
❌ Failed: 1
📈 Total: 16
🎯 Success Rate: 93.8%

❌ FAILED TESTS:
  - Login with invalid password: Should reject login with invalid password

⚠️  Some tests failed. Please review the errors above.
```

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Ensure backend server is running
   - Check if the correct port is being used
   - Verify BASE_URL is correct

2. **Firebase Admin SDK Errors**
   - Check if `GOOGLE_APPLICATION_CREDENTIALS` is set
   - Verify service account key file exists and is valid
   - Ensure Firebase project is properly configured

3. **Authentication Failures**
   - Verify `FIREBASE_WEB_API_KEY` is set correctly
   - Check if Firebase Auth is enabled in the project
   - Ensure CORS is configured for the test environment

4. **Rate Limiting Issues**
   - Rate limiting tests may not trigger in development
   - This is expected behavior and not a failure

### Debug Mode

To run tests with more verbose output, you can modify the test configuration:

```javascript
const TEST_CONFIG = {
  timeout: 10000,
  retries: 3,
  debug: true  // Add this for verbose logging
};
```

## Adding New Tests

To add new test cases:

1. Create a new test method in the `AuthTestSuite` class
2. Add the test to the `runAllTests()` method
3. Follow the existing pattern for error handling and assertions

Example:
```javascript
async testNewFeature() {
  const response = await makeRequest('POST', '/auth/new-endpoint', testData);
  
  if (response.error) {
    throw new Error(`New feature failed: ${response.data?.error || response.message}`);
  }
  
  // Add your assertions here
  if (!response.data.expectedField) {
    throw new Error('Expected field missing from response');
  }
}
```

## Continuous Integration

These tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run Authentication Tests
  run: |
    cd backend
    npm install
    npm run test:auth:local
  env:
    BASE_URL: http://localhost:3000
    GOOGLE_APPLICATION_CREDENTIALS: ${{ secrets.FIREBASE_SERVICE_ACCOUNT_KEY }}
    FIREBASE_WEB_API_KEY: ${{ secrets.FIREBASE_WEB_API_KEY }}
```
