#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';

console.log('🧪 Running Authentication Tests...\n');
console.log(`📍 Testing against: ${BASE_URL}\n`);

// Run the test suite
const testProcess = spawn('node', [path.join(__dirname, 'tests', 'auth.test.js')], {
  stdio: 'inherit',
  env: {
    ...process.env,
    BASE_URL: BASE_URL
  }
});

testProcess.on('close', (code) => {
  if (code === 0) {
    console.log('\n🎉 All authentication tests completed successfully!');
  } else {
    console.log('\n❌ Some authentication tests failed.');
    process.exit(code);
  }
});

testProcess.on('error', (error) => {
  console.error('❌ Failed to run tests:', error.message);
  process.exit(1);
});
