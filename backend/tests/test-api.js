// Test script for API endpoints
const axios = require('axios');

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';

async function testAPI() {
  console.log('🧪 Testing Rotidote Backend API...\n');

  try {
    // Test base route
    console.log('1. Testing base route...');
    const baseResponse = await axios.get(`${BASE_URL}/`);
    console.log('✅ Base route:', baseResponse.data);
    console.log('');

    // Test health endpoint
    console.log('2. Testing health endpoint...');
    const healthResponse = await axios.get(`${BASE_URL}/health`);
    console.log('✅ Health check:', healthResponse.data);
    console.log('');

    // Test create upload endpoint (without auth for now)
    console.log('3. Testing create upload endpoint...');
    try {
      const uploadResponse = await axios.post(`${BASE_URL}/create-upload`, {
        filename: 'test.mp4',
        contentType: 'video/mp4'
      });
      console.log('✅ Create upload:', uploadResponse.data);
    } catch (error) {
      console.log('⚠️  Create upload (expected if no Mux credentials):', error.response?.data || error.message);
    }
    console.log('');

    console.log('🎉 API testing completed!');
    console.log(`📡 Base URL: ${BASE_URL}`);
    console.log('📋 Available endpoints:');
    console.log('   - GET  /');
    console.log('   - GET  /health');
    console.log('   - POST /create-upload');
    console.log('   - GET  /asset/:assetId');
    console.log('   - POST /upload-thumbnail');

  } catch (error) {
    console.error('❌ API test failed:', error.message);
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    }
  }
}

// Run the test
testAPI();
