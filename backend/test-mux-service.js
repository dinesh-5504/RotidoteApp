// TestSprite - Mux Service Test
// Run with: node test-mux-service.js

import muxService from './services/muxService.js';
import dotenv from 'dotenv';

dotenv.config();

async function testMuxService() {
  console.log('🧪 TestSprite - Mux Service Test');
  console.log('================================\n');

  try {
    console.log('🔍 Testing Mux Service initialization...');
    
    // Test 1: Create Direct Upload
    console.log('📤 Testing createDirectUpload...');
    const uploadResult = await muxService.createDirectUpload('Test Video');
    console.log('✅ createDirectUpload result:', {
      uploadId: uploadResult.uploadId,
      uploadUrl: uploadResult.uploadUrl ? 'Present' : 'Missing',
      assetId: uploadResult.assetId
    });

    // Test 2: Create Asset
    console.log('\n📹 Testing createAsset...');
    const assetResult = await muxService.createAsset('Test Asset');
    console.log('✅ createAsset result:', {
      assetId: assetResult.assetId,
      playbackId: assetResult.playbackId,
      status: assetResult.status
    });

    // Test 3: Get Asset
    console.log('\n🔍 Testing getAsset...');
    const getAssetResult = await muxService.getAsset(assetResult.assetId);
    console.log('✅ getAsset result:', {
      assetId: getAssetResult.assetId,
      playbackId: getAssetResult.playbackId,
      status: getAssetResult.status
    });

    console.log('\n🎉 All Mux Service tests passed!');
    console.log('✅ Mux SDK integration working correctly');

  } catch (error) {
    console.log('\n❌ Mux Service test failed!');
    console.log('Error:', error.message);
    
    if (error.message.includes('Failed to create Mux asset') || 
        error.message.includes('Failed to create Mux direct upload')) {
      console.log('\n💡 This might be due to:');
      console.log('   - Missing MUX_TOKEN_ID or MUX_TOKEN_SECRET environment variables');
      console.log('   - Invalid Mux API credentials');
      console.log('   - Network connectivity issues');
    }
  }
}

// Run the test
if (import.meta.url === `file://${process.argv[1]}`) {
  testMuxService().catch(console.error);
}

export default testMuxService;
