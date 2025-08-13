const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const Mux = require('@mux/mux-node');

const app = express();
const PORT = process.env.PORT || 3000;

// Initialize Mux client
const muxClient = new Mux(
  process.env.MUX_TOKEN_ID,
  process.env.MUX_TOKEN_SECRET
);

// Security middleware
app.use(helmet());

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
    const upload = await muxClient.Video.Uploads.create({
      new_asset_settings: {
        playback_policy: ['public'],
        mp4_support: 'standard'
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

    const asset = await muxClient.Video.Assets.get(assetId);

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

app.listen(PORT, () => {
  console.log(`🚀 Rotidote Backend server running on port ${PORT}`);
  console.log(`📡 Health check: http://localhost:${PORT}/health`);
  console.log(`🎥 Mux upload endpoint: http://localhost:${PORT}/create-upload`);
});

module.exports = app;

