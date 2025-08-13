const fetch = require('node-fetch');

// Configuration
const BASE_URL = process.env.BACKEND_URL || 'http://localhost:3000';
const TEST_TOKEN = process.env.TEST_TOKEN || 'test-token';

// Test functions
async function testHealthCheck() {
    console.log('🔍 Testing health check...');
    try {
        const response = await fetch(`${BASE_URL}/health`);
        const data = await response.json();
        
        if (response.ok) {
            console.log('✅ Health check passed');
            console.log('   Status:', data.status);
            console.log('   Services:', data.services);
        } else {
            console.log('❌ Health check failed:', data.error);
        }
    } catch (error) {
        console.log('❌ Health check error:', error.message);
    }
}

async function testAuthentication() {
    console.log('\n🔐 Testing authentication...');
    try {
        const response = await fetch(`${BASE_URL}/videos`, {
            headers: {
                'Authorization': `Bearer ${TEST_TOKEN}`
            }
        });
        
        if (response.status === 401) {
            console.log('✅ Authentication middleware working (expected 401 for invalid token)');
        } else if (response.status === 403) {
            console.log('✅ Authentication middleware working (expected 403 for invalid token)');
        } else {
            console.log('⚠️  Unexpected response:', response.status);
        }
    } catch (error) {
        console.log('❌ Authentication test error:', error.message);
    }
}

async function testMissingAuth() {
    console.log('\n🚫 Testing missing authentication...');
    try {
        const response = await fetch(`${BASE_URL}/videos`);
        
        if (response.status === 401) {
            console.log('✅ Missing auth correctly rejected');
        } else {
            console.log('⚠️  Unexpected response for missing auth:', response.status);
        }
    } catch (error) {
        console.log('❌ Missing auth test error:', error.message);
    }
}

async function testCORS() {
    console.log('\n🌐 Testing CORS...');
    try {
        const response = await fetch(`${BASE_URL}/health`, {
            method: 'OPTIONS',
            headers: {
                'Origin': 'https://test-app.com',
                'Access-Control-Request-Method': 'GET',
                'Access-Control-Request-Headers': 'Authorization'
            }
        });
        
        const corsHeaders = response.headers.get('access-control-allow-origin');
        if (corsHeaders) {
            console.log('✅ CORS headers present:', corsHeaders);
        } else {
            console.log('⚠️  No CORS headers found');
        }
    } catch (error) {
        console.log('❌ CORS test error:', error.message);
    }
}

// Main test runner
async function runTests() {
    console.log('🚀 Starting backend tests...\n');
    console.log(`📍 Testing backend at: ${BASE_URL}\n`);
    
    await testHealthCheck();
    await testAuthentication();
    await testMissingAuth();
    await testCORS();
    
    console.log('\n✨ Tests completed!');
    console.log('\n📝 Next steps:');
    console.log('1. Set up Firebase Admin SDK credentials');
    console.log('2. Configure Mux and Cloudinary API keys');
    console.log('3. Test with real Firebase ID tokens');
    console.log('4. Test file uploads with actual video files');
}

// Run tests if this file is executed directly
if (require.main === module) {
    runTests().catch(console.error);
}

module.exports = {
    testHealthCheck,
    testAuthentication,
    testMissingAuth,
    testCORS,
    runTests
};
