import express from 'express';
import muxService from '../services/muxService.js';
import cloudinaryService from '../services/cloudinaryService.js';
const router = express.Router();

// Mux Direct Upload API
router.post('/mux', async (req, res) => {
  try {
    const { title } = req.body;
    
    if (!title) {
      return res.status(400).json({ error: 'Title is required' });
    }
    
    const uploadData = await muxService.createDirectUpload(title);
    
    res.json({
      success: true,
      uploadUrl: uploadData.uploadUrl,
      uploadId: uploadData.uploadId,
      assetId: uploadData.assetId,
      message: 'Mux direct upload URL generated successfully'
    });
  } catch (error) {
    console.error('Error generating Mux upload URL:', error);
    res.status(500).json({ 
      success: false,
      error: 'Failed to generate Mux upload URL' 
    });
  }
});

// Cloudinary Direct Upload API
router.post('/cloudinary', async (req, res) => {
  try {
    const { folder = 'thumbnails', publicId = null } = req.body;
    
    const signatureData = cloudinaryService.generateSignature(folder, publicId);
    
    res.json({
      success: true,
      uploadUrl: `https://api.cloudinary.com/v1_1/${signatureData.cloudName}/image/upload`,
      publicId: publicId || `temp_${Date.now()}`,
      signature: signatureData.signature,
      timestamp: signatureData.timestamp,
      apiKey: signatureData.apiKey,
      cloudName: signatureData.cloudName,
      message: 'Cloudinary upload signature generated successfully'
    });
  } catch (error) {
    console.error('Error generating Cloudinary signature:', error);
    res.status(500).json({ 
      success: false,
      error: 'Failed to generate Cloudinary upload signature' 
    });
  }
});

export default router;
