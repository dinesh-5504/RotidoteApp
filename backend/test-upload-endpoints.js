// TestSprite - Upload Endpoints Test
// Run with: node test-upload-endpoints.js

const https = require('https');
const http = require('http');

const BASE_URL = 'https://rotidoteapp.onrender.com';

// Test utility function
async function testEndpoint(endpoint, method = 'GET', body = null, expectedStatus = 200) {
  return new Promise((resolve, reject) => {
    const url = new URL(BASE_URL + endpoint);
    const options = {
      hostname: url.hostname,
      port: url.port || (url.protocol === 'https:' ? 443 : 80),
      path: url.pathname + url.search,
      method: method,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    const req = (url.protocol === 'https:' ? https : http).request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        const result = {
          endpoint,
          method,
          statusCode: res.statusCode,
          success: res.statusCode === expectedStatus,
          data: data ? JSON.parse(data) : null,
          headers: res.headers
        };
        resolve(result);
      });
    });

    req.on('error', (err) => {
      reject({ endpoint, method, error: err.message });
    });

    if (body) {
      req.write(JSON.stringify(body));
    }
    req.end();
  });
}

// Upload Endpoints Test
async function runUploadEndpointsTest() {
  console.log('🧪 TestSprite - Upload Endpoints Test');
  console.log('=====================================\n');

  const tests = [
    {
      name: 'Mux Direct Upload URL',
      endpoint: '/api/upload/mux',
      method: 'POST',
      body: { title: 'Test Video Upload' },
      expectedStatus: 200
    },
    {
      name: 'Cloudinary Upload Signature',
      endpoint: '/api/upload/cloudinary',
      method: 'POST',
      body: { folder: 'thumbnails' },
      expectedStatus: 200
    }
  ];

  const results = [];
  
  for (const test of tests) {
    try {
      console.log(`🔍 Testing: ${test.name}`);
      const result = await testEndpoint(test.endpoint, test.method, test.body, test.expectedStatus);
      results.push(result);
      
      if (result.success) {
        console.log(`✅ ${test.name}: PASSED`);
        console.log(`   Status: ${result.statusCode}`);
        if (result.data) {
          console.log(`   Response: ${JSON.stringify(result.data, null, 2)}`);
        }
      } else {
        console.log(`❌ ${test.name}: FAILED`);
        console.log(`   Status: ${result.statusCode}`);
        if (result.data) {
          console.log(`   Error: ${JSON.stringify(result.data, null, 2)}`);
        }
      }
      console.log('');
    } catch (error) {
      console.log(`❌ ${test.name}: ERROR`);
      console.log(`   Error: ${error.error}`);
      console.log('');
      results.push({ ...test, success: false, error: error.error });
    }
  }

  // Summary
  const passed = results.filter(r => r.success).length;
  const total = results.length;
  
  console.log('📊 Test Summary');
  console.log('===============');
  console.log(`✅ Passed: ${passed}/${total}`);
  console.log(`❌ Failed: ${total - passed}/${total}`);
  
  if (passed === total) {
    console.log('\n🎉 All upload endpoint tests passed!');
  } else {
    console.log('\n⚠️  Some tests failed. Check the output above for details.');
  }
}

// Run the test
if (require.main === module) {
  runUploadEndpointsTest().catch(console.error);
}

module.exports = { testEndpoint, runUploadEndpointsTest };
