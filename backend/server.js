const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const multer = require('multer');
require('dotenv').config();

const Mux = require('@mux/mux-node');
const cloudinary = require('cloudinary').v2;

const app = express();
const PORT = process.env.PORT || 3000;

// Initialize Mux client
const muxClient = new Mux(
  process.env.MUX_TOKEN_ID,
  process.env.MUX_TOKEN_SECRET
);
const {Video} = muxClient;
// Configure Cloudinary
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET
});

// Configure multer for file uploads
const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB limit
  }
});

// Security middleware
app.use(helmet());
app.set('trust proxy', 1); // trust first proxy (Vercel / reverse proxy)

// CORS configuration
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true
}));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP, please try again later.'
});
app.use(limiter);

// Body parsing middleware
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Base route for testing
app.get('/', (req, res) => {
  res.json({ 
    message: 'Rotidote Backend API is running!',
    status: 'OK', 
    timestamp: new Date().toISOString(),
    endpoints: {
      health: '/health',
      createUpload: '/create-upload',
      assetDetails: '/asset/:assetId',
      uploadThumbnail: '/upload-thumbnail'
    }
  });
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Mux Direct Upload endpoint
app.post('/create-upload', async (req, res) => {
  try {
    const { filename, contentType } = req.body;

    if (!filename || !contentType) {
      return res.status(400).json({
        error: 'Missing required fields: filename and contentType are required'
      });
    }

    // Create a Direct Upload
    const upload = await Video.Uploads.create({
      new_asset_settings: {
        playback_policy: ['public'],
        // mp4_support: 'standard'
      },
      cors_origin: '*'
    });

    res.json({
      uploadUrl: upload.url,
      uploadId: upload.id,
      assetId: upload.asset_id
    });

  } catch (error) {
    console.error('Error creating upload:', error);
    res.status(500).json({
      error: 'Failed to create upload URL',
      details: error.message
    });
  }
});

// Get asset details endpoint
app.get('/asset/:assetId', async (req, res) => {
  try {
    const { assetId } = req.params;

    if (!assetId) {
      return res.status(400).json({
        error: 'Asset ID is required'
      });
    }

    const asset = await Video.Assets.get(assetId);

    res.json({
      assetId: asset.id,
      playbackId: asset.playback_ids?.[0]?.id,
      status: asset.status,
      duration: asset.duration,
      aspectRatio: asset.aspect_ratio,
      createdAt: asset.created_at
    });

  } catch (error) {
    console.error('Error getting asset:', error);
    res.status(500).json({
      error: 'Failed to get asset details',
      details: error.message
    });
  }
});

// Cloudinary thumbnail upload endpoint
app.post('/upload-thumbnail', upload.single('thumbnail'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        error: 'No thumbnail file provided'
      });
    }

    // Convert buffer to base64 for Cloudinary
    const fileBuffer = req.file.buffer;
    const base64File = `data:${req.file.mimetype};base64,${fileBuffer.toString('base64')}`;

    // Upload to Cloudinary
    const result = await cloudinary.uploader.upload(base64File, {
      folder: 'rotidote-thumbnails',
      resource_type: 'image',
      transformation: [
        { width: 400, height: 225, crop: 'fill' }, // 16:9 aspect ratio
        { quality: 'auto' }
      ]
    });

    res.json({
      thumbnailUrl: result.secure_url,
      publicId: result.public_id,
      width: result.width,
      height: result.height
    });

  } catch (error) {
    console.error('Error uploading thumbnail:', error);
    res.status(500).json({
      error: 'Failed to upload thumbnail',
      details: error.message
    });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    error: 'Something went wrong!',
    details: process.env.NODE_ENV === 'development' ? err.message : 'Internal server error'
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Endpoint not found'
  });
});

// For Vercel serverless deployment
if (process.env.NODE_ENV !== 'production') {
  app.listen(PORT, () => {
    console.log(`🚀 Rotidote Backend server running on port ${PORT}`);
    console.log(`📡 Health check: http://localhost:${PORT}/health`);
    console.log(`🎥 Mux upload endpoint: http://localhost:${PORT}/create-upload`);
    console.log(`🖼️  Thumbnail upload endpoint: http://localhost:${PORT}/upload-thumbnail`);
  });
}

module.exports = app;


