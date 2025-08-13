const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
require('dotenv').config();

// Import services
const Mux = require('@mux/mux-node');
const cloudinary = require('cloudinary').v2;
const admin = require('firebase-admin');

const app = express();
const PORT = process.env.PORT || 3000;

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n')
    }),
  });
}

const db = admin.firestore();

// Initialize Mux client
const muxClient = new Mux(
  process.env.MUX_TOKEN_ID,
  process.env.MUX_TOKEN_SECRET
);

// Initialize Cloudinary
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET
});

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const uploadDir = 'uploads/';
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});

const upload = multer({ 
  storage: storage,
  limits: {
    fileSize: 100 * 1024 * 1024 // 100MB limit
  },
  fileFilter: (req, file, cb) => {
    // Allow video and image files
    if (file.mimetype.startsWith('video/') || file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('Invalid file type. Only video and image files are allowed.'), false);
    }
  }
});

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

// Authentication middleware
const authenticateToken = async (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    return res.status(401).json({ error: 'Access token required' });
  }

  try {
    // Verify Firebase ID token
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    console.error('Token verification failed:', error);
    return res.status(403).json({ error: 'Invalid or expired token' });
  }
};

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    timestamp: new Date().toISOString(),
    services: {
      mux: !!process.env.MUX_TOKEN_ID,
      cloudinary: !!process.env.CLOUDINARY_CLOUD_NAME,
      firebase: !!process.env.FIREBASE_PROJECT_ID
    }
  });
});

// Mux Direct Upload endpoint (for individual video uploads)
app.post('/create-upload', authenticateToken, async (req, res) => {
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
app.get('/asset/:assetId', authenticateToken, async (req, res) => {
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

// Main upload endpoint for complete video upload (ad + main + thumbnail)
app.post('/upload-video', authenticateToken, upload.fields([
  { name: 'adVideo', maxCount: 1 },
  { name: 'mainVideo', maxCount: 1 },
  { name: 'thumbnail', maxCount: 1 }
]), async (req, res) => {
  try {
    const { creatorName, videoTitle, duration } = req.body;
    const files = req.files;

    // Validate required fields
    if (!creatorName || !videoTitle || !files) {
      return res.status(400).json({
        error: 'Missing required fields: creatorName, videoTitle, and files are required'
      });
    }

    if (!files.adVideo || !files.mainVideo || !files.thumbnail) {
      return res.status(400).json({
        error: 'All files are required: adVideo, mainVideo, and thumbnail'
      });
    }

    const userId = req.user.uid;
    const uploadResults = {};

    // Upload ad video to Mux
    console.log('Uploading ad video to Mux...');
    const adVideoUpload = await muxClient.Video.Uploads.create({
      new_asset_settings: {
        playback_policy: ['public'],
        mp4_support: 'standard'
      },
      cors_origin: '*'
    });

    // Upload the ad video file to Mux
    const adVideoFile = files.adVideo[0];
    const adVideoBuffer = fs.readFileSync(adVideoFile.path);
    await fetch(adVideoUpload.url, {
      method: 'PUT',
      body: adVideoBuffer,
      headers: {
        'Content-Type': adVideoFile.mimetype
      }
    });

    uploadResults.adVideoAssetId = adVideoUpload.asset_id;

    // Upload main video to Mux
    console.log('Uploading main video to Mux...');
    const mainVideoUpload = await muxClient.Video.Uploads.create({
      new_asset_settings: {
        playback_policy: ['public'],
        mp4_support: 'standard'
      },
      cors_origin: '*'
    });

    // Upload the main video file to Mux
    const mainVideoFile = files.mainVideo[0];
    const mainVideoBuffer = fs.readFileSync(mainVideoFile.path);
    await fetch(mainVideoUpload.url, {
      method: 'PUT',
      body: mainVideoBuffer,
      headers: {
        'Content-Type': mainVideoFile.mimetype
      }
    });

    uploadResults.mainVideoAssetId = mainVideoUpload.asset_id;

    // Upload thumbnail to Cloudinary
    console.log('Uploading thumbnail to Cloudinary...');
    const thumbnailFile = files.thumbnail[0];
    const thumbnailResult = await cloudinary.uploader.upload(thumbnailFile.path, {
      folder: 'rotidote/thumbnails',
      public_id: `thumbnail_${Date.now()}`,
      transformation: [
        { width: 400, height: 225, crop: 'fill' }
      ]
    });

    uploadResults.thumbnailUrl = thumbnailResult.secure_url;

    // Get video metadata from Mux
    console.log('Getting video metadata...');
    const mainAsset = await muxClient.Video.Assets.get(mainVideoUpload.asset_id);
    const adAsset = await muxClient.Video.Assets.get(adVideoUpload.asset_id);

    // Create video document for Firestore
    const videoData = {
      title: videoTitle,
      creatorName: creatorName,
      creatorId: userId,
      duration: mainAsset.duration || parseFloat(duration || 0),
      adVideoMuxKey: adVideoUpload.asset_id,
      mainVideoMuxKey: mainVideoUpload.asset_id,
      adVideoPlaybackId: adAsset.playback_ids?.[0]?.id,
      mainVideoPlaybackId: mainAsset.playback_ids?.[0]?.id,
      thumbnailUrl: thumbnailResult.secure_url,
      status: 'processing', // Mux will process the videos
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };

    // Save to Firestore
    console.log('Saving to Firestore...');
    const videoRef = await db.collection('videos').add(videoData);

    // Clean up uploaded files
    Object.values(files).flat().forEach(file => {
      fs.unlinkSync(file.path);
    });

    res.json({
      success: true,
      videoId: videoRef.id,
      message: 'Video upload completed successfully',
      data: {
        ...uploadResults,
        videoData
      }
    });

  } catch (error) {
    console.error('Error in video upload:', error);
    
    // Clean up any uploaded files on error
    if (req.files) {
      Object.values(req.files).flat().forEach(file => {
        if (fs.existsSync(file.path)) {
          fs.unlinkSync(file.path);
        }
      });
    }

    res.status(500).json({
      error: 'Failed to upload video',
      details: error.message
    });
  }
});

// Get user's videos
app.get('/videos', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.uid;
    const videosSnapshot = await db.collection('videos')
      .where('creatorId', '==', userId)
      .orderBy('createdAt', 'desc')
      .get();

    const videos = [];
    videosSnapshot.forEach(doc => {
      videos.push({
        id: doc.id,
        ...doc.data()
      });
    });

    res.json({ videos });
  } catch (error) {
    console.error('Error fetching videos:', error);
    res.status(500).json({
      error: 'Failed to fetch videos',
      details: error.message
    });
  }
});

// Get all videos (for home feed)
app.get('/videos/public', async (req, res) => {
  try {
    const videosSnapshot = await db.collection('videos')
      .where('status', '==', 'ready')
      .orderBy('createdAt', 'desc')
      .limit(20)
      .get();

    const videos = [];
    videosSnapshot.forEach(doc => {
      videos.push({
        id: doc.id,
        ...doc.data()
      });
    });

    res.json({ videos });
  } catch (error) {
    console.error('Error fetching public videos:', error);
    res.status(500).json({
      error: 'Failed to fetch videos',
      details: error.message
    });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  
  if (err instanceof multer.MulterError) {
    if (err.code === 'LIMIT_FILE_SIZE') {
      return res.status(400).json({
        error: 'File too large. Maximum size is 100MB.'
      });
    }
  }
  
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
  console.log(`🎥 Upload endpoint: http://localhost:${PORT}/upload-video`);
  console.log(`🔐 Authentication: Firebase ID tokens required`);
});

module.exports = app;

