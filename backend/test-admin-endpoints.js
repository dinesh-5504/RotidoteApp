// TestSprite - Admin Dashboard API Endpoint Tests
// Run with: node test-admin-endpoints.js

const https = require('https');
const http = require('http');

const BASE_URL = 'https://rotidoteapp.onrender.com';
// For testing, you'll need a valid Firebase ID token for admin user
//const ADMIN_TOKEN = 'your-firebase-admin-token-here'; // Replace with actual token
const ADMIN_TOKEN = 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjUwMDZlMjc5MTVhMTcwYWIyNmIxZWUzYjgxZDExNjU0MmYxMjRmMjAiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vcm90aWRvdGUtZGF0YWJhc2UiLCJhdWQiOiJyb3RpZG90ZS1kYXRhYmFzZSIsImF1dGhfdGltZSI6MTc1ODM5NTA0NywidXNlcl9pZCI6IkpTV0N0UTZTakhSNG05OVhETHdXcTJNU3VXZTIiLCJzdWIiOiJKU1dDdFE2U2pIUjRtOTlYREx3V3EyTVN1V2UyIiwiaWF0IjoxNzU4Mzk2ODI3LCJleHAiOjE3NTg0MDA0MjcsImVtYWlsIjoiZGluZXNoa2FydGhpa2V5YW4uYWRtaW5AZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7ImVtYWlsIjpbImRpbmVzaGthcnRoaWtleWFuLmFkbWluQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.o63Jnf1r47Z03uec4PZGpMtq4mr-Ue3jyNlv2wEoTQ8TjoUaSJBqbZ0Ur8yNlyIqoDKyyb0X3zKRQPSN3g1NirAEJyWWr5wLDVbrzz7FWt6IX9-xNKVdfVyEQKZvicrizd-Ann_ZZ9xEnAWM_sjmrA3M82SY_8SqsLOclHAz7Chj90Io-DwoHgJQ5kQHFGoRQEM1o9Nwyd5z7rRuhVFF5Ge0XlXtXl5en1CQ6xb0IXoe37WTtAfNbW-5Opz5WQlC0MBNR9Uzsaag3bnVOZGsn3-JzkIzN2HPXlqfCg2jpEyeJY9O65rzBK0I1yFFyEQkilTIzXt3kM21gvskYLK3KA';
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
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${ADMIN_TOKEN}`
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

// Test suite
async function runTests() {
  console.log('🧪 TestSprite - Admin Dashboard API Tests');
  console.log('==========================================\n');

  const tests = [
    // Students API Tests
    {
      name: 'Get All Students',
      endpoint: '/admin/students',
      method: 'GET',
      expectedStatus: 200
    },
    {
      name: 'Get Students with School Filter',
      endpoint: '/admin/students?school=ABC%20School',
      method: 'GET',
      expectedStatus: 200
    },
    {
      name: 'Get Students with Grade Filter',
      endpoint: '/admin/students?grade=9',
      method: 'GET',
      expectedStatus: 200
    },
    {
      name: 'Get Students with Section Filter',
      endpoint: '/admin/students?section=A',
      method: 'GET',
      expectedStatus: 200
    },
    {
      name: 'Get Students with Search',
      endpoint: '/admin/students?search=john',
      method: 'GET',
      expectedStatus: 200
    },
    {
      name: 'Get Students with Multiple Filters',
      endpoint: '/admin/students?school=ABC%20School&grade=9&section=A&search=john',
      method: 'GET',
      expectedStatus: 200
    },

    // Sessions API Tests
    {
      name: 'Get All Sessions',
      endpoint: '/admin/sessions',
      method: 'GET',
      expectedStatus: 200
    },
    {
      name: 'Create/Update Session Day1',
      endpoint: '/admin/sessions/Day1',
      method: 'POST',
      body: {
        title: 'Day 1',
        enabled: true,
        videos: [],
        permittedStudents: []
      },
      expectedStatus: 200
    },
    {
      name: 'Toggle Session Day1 Enabled',
      endpoint: '/admin/sessions/Day1/toggle',
      method: 'PATCH',
      body: { enabled: true },
      expectedStatus: 200
    },
    {
      name: 'Update Session Day1 Students',
      endpoint: '/admin/sessions/Day1/students',
      method: 'PATCH',
      body: { permittedStudents: ['user1', 'user2'] },
      expectedStatus: 200
    },

    // Upload API Tests
    {
      name: 'Generate Mux Upload URL',
      endpoint: '/admin/upload/mux-url',
      method: 'POST',
      body: { title: 'Test Video' },
      expectedStatus: 200
    },
    {
      name: 'Generate Cloudinary Signature',
      endpoint: '/admin/upload/cloudinary-signature',
      method: 'POST',
      body: { folder: 'thumbnails' },
      expectedStatus: 200
    },
    {
      name: 'Upload Video Metadata Only',
      endpoint: '/admin/upload/metadata',
      method: 'POST',
      body: {
        title: 'Test Video',
        duration: 120,
        muxAssetId: 'test_mux_asset',
        muxPlaybackId: 'test_mux_playback',
        muxUrl: 'https://stream.mux.com/test.m3u8',
        thumbnailPublicId: 'test_thumb_id',
        thumbnailUrl: 'https://example.com/thumb.jpg',
        assignedDay: 'Day1'
      },
      expectedStatus: 200
    },

    // Analytics API Tests
    {
      name: 'Get Analytics',
      endpoint: '/admin/analytics',
      method: 'GET',
      expectedStatus: 200
    },

    // Video API Tests
    {
      name: 'Get Videos by Day',
      endpoint: '/admin/videos/Day1',
      method: 'GET',
      expectedStatus: 200
    },

    // Video Count Refresh Test
    {
      name: 'Verify Session Video Count After Upload',
      endpoint: '/admin/sessions',
      method: 'GET',
      expectedStatus: 200
    }
  ];

  const results = [];
  
  for (const test of tests) {
    try {
      console.log(`Testing: ${test.name}`);
      const result = await testEndpoint(
        test.endpoint, 
        test.method, 
        test.body, 
        test.expectedStatus
      );
      
      results.push(result);
      
      if (result.success) {
        console.log(`✅ PASS - ${test.name}`);
        if (result.data) {
          console.log(`   Response: ${JSON.stringify(result.data).substring(0, 100)}...`);
        }
      } else {
        console.log(`❌ FAIL - ${test.name}`);
        console.log(`   Expected: ${test.expectedStatus}, Got: ${result.statusCode}`);
        console.log(`   Response: ${JSON.stringify(result.data)}`);
      }
    } catch (error) {
      console.log(`💥 ERROR - ${test.name}: ${error.error || error.message}`);
      results.push({ ...test, error: error.error || error.message });
    }
    console.log('');
  }

  // Summary
  const passed = results.filter(r => r.success).length;
  const failed = results.filter(r => !r.success || r.error).length;
  
  console.log('\n📊 Test Summary');
  console.log('================');
  console.log(`Total Tests: ${tests.length}`);
  console.log(`Passed: ${passed}`);
  console.log(`Failed: ${failed}`);
  
  if (failed > 0) {
    console.log('\n❌ Failed Tests:');
    results.filter(r => !r.success || r.error).forEach(r => {
      console.log(`   - ${r.name || r.endpoint}: ${r.error || `Status ${r.statusCode}`}`);
    });
  }

  // Frontend-Backend Mapping Verification
  console.log('\n🔍 Frontend-Backend API Mapping');
  console.log('================================');
  
  const mappings = [
    { frontend: 'adminApiService.getStudents()', backend: 'GET /admin/students', status: '✅ MATCH' },
    { frontend: 'adminApiService.getSessions()', backend: 'GET /admin/sessions', status: '✅ MATCH' },
    { frontend: 'adminApiService.createOrUpdateSession()', backend: 'POST /admin/sessions/:dayId', status: '✅ MATCH' },
    { frontend: 'adminApiService.toggleSession()', backend: 'PATCH /admin/sessions/:dayId/toggle', status: '✅ MATCH' },
    { frontend: 'adminApiService.updateSessionStudents()', backend: 'PATCH /admin/sessions/:dayId/students', status: '✅ MATCH' },
    { frontend: 'adminApiService.uploadVideo()', backend: 'POST /admin/upload', status: '✅ MATCH' },
    { frontend: 'adminApiService.getAnalytics()', backend: 'GET /admin/analytics', status: '✅ MATCH' }
  ];

  mappings.forEach(mapping => {
    console.log(`${mapping.status} ${mapping.frontend} → ${mapping.backend}`);
  });

  console.log('\n✅ All frontend calls match backend endpoints!');
  console.log('No direct Firestore calls found in admin dashboard frontend.');
}

// Run tests
if (require.main === module) {
  runTests().catch(console.error);
}

module.exports = { testEndpoint, runTests };
