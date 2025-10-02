const express = require('express');
const admin = require('firebase-admin');
const multer = require('multer');
const muxService = require('../services/muxService');
const cloudinaryService = require('../services/cloudinaryService');

// Configure multer for file uploads
const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 100 * 1024 * 1024, // 100MB limit
  },
  fileFilter: (req, file, cb) => {
    if (file.fieldname === 'video') {
      // Accept video files
      if (file.mimetype.startsWith('video/')) {
        cb(null, true);
      } else {
        cb(new Error('Only video files are allowed for video field'), false);
      }
    } else if (file.fieldname === 'thumbnail') {
      // Accept image files
      if (file.mimetype.startsWith('image/')) {
        cb(null, true);
      } else {
        cb(new Error('Only image files are allowed for thumbnail field'), false);
      }
    } else {
      cb(new Error('Unexpected field'), false);
    }
  },
});
const router = express.Router();

// Middleware to verify admin access
const verifyAdmin = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ error: 'No token provided' });
    }

    const idToken = authHeader.split('Bearer ')[1];
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    
    // Check if user is admin
    const adminEmail = 'dineshkarthikeyan.admin@gmail.com';
    if (decodedToken.email !== adminEmail) {
      return res.status(403).json({ error: 'Admin access required' });
    }

    req.user = decodedToken;
    next();
  } catch (error) {
    console.error('Admin verification error:', error);
    res.status(401).json({ error: 'Invalid token' });
  }
};

// Get all students with optional filters
router.get('/students', verifyAdmin, async (req, res) => {
  try {
    const { school, grade, section, search } = req.query;
    const db = admin.firestore();
    let query = db.collection('users');

    // Apply filters
    if (school) {
      query = query.where('schoolName', '==', school);
    }
    if (grade) {
      query = query.where('grade', '==', grade);
    }
    if (section) {
      query = query.where('section', '==', section);
    }

    const snapshot = await query.orderBy('createdAt', 'desc').get();
    let students = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));

    // Apply search filter (client-side for now)
    if (search) {
      const searchLower = search.toLowerCase();
      students = students.filter(student => 
        student.name.toLowerCase().includes(searchLower) ||
        student.email.toLowerCase().includes(searchLower)
      );
    }

    res.json({
      students,
      total: students.length
    });
  } catch (error) {
    console.error('Error fetching students:', error);
    res.status(500).json({ error: 'Failed to fetch students' });
  }
});

// Get all sessions (with nested days)
router.get('/sessions', verifyAdmin, async (req, res) => {
  try {
    const db = admin.firestore();
    const snapshot = await db.collection('sessions').get();
    
    const sessions = [];
    snapshot.docs.forEach(doc => {
      const data = doc.data();
      sessions.push({
        id: doc.id,
        title: data.title || 'Untitled Session',
        days: data.days || []
      });
    });
    
    // If no sessions exist, create a default one
    if (sessions.length === 0) {
      const defaultSession = {
        id: 'default_session',
        title: 'Default Session',
        days: Array.from({length: 5}, (_, i) => ({
          id: `Day${i + 1}`,
          enabled: false,
          videos: [],
          permittedStudents: []
        }))
      };
      sessions.push(defaultSession);
    }

    res.json({ success: true, data: { sessions } });
  } catch (error) {
    console.error('Error fetching sessions:', error);
    res.status(500).json({ success: false, error: 'Failed to fetch sessions' });
  }
});

// Create a new session
router.post('/sessions', verifyAdmin, async (req, res) => {
  try {
    const { title } = req.body;
    
    const db = admin.firestore();
    const sessionData = {
      title: title || 'New Session',
      days: Array.from({length: 5}, (_, i) => ({
        id: `Day${i + 1}`,
        enabled: false,
        videos: [],
        permittedStudents: []
      })),
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };
    
    const docRef = await db.collection('sessions').add(sessionData);
    
    res.json({ 
      success: true, 
      data: { 
        id: docRef.id, 
        ...sessionData 
      } 
    });
  } catch (error) {
    console.error('Error creating session:', error);
    res.status(500).json({ success: false, error: 'Failed to create session' });
  }
});





// Update day enabled status within a session
router.patch('/sessions/:sessionId/days/:dayId/enabled', verifyAdmin, async (req, res) => {
  try {
    const { sessionId, dayId } = req.params;
    const { enabled } = req.body;
    
    const db = admin.firestore();
    const sessionRef = db.collection('sessions').doc(sessionId);
    const sessionDoc = await sessionRef.get();
    
    if (!sessionDoc.exists) {
      return res.status(404).json({ success: false, error: 'Session not found' });
    }
    
    const sessionData = sessionDoc.data();
    const updatedDays = sessionData.days.map(day => 
      day.id === dayId ? { ...day, enabled } : day
    );
    
    await sessionRef.update({ days: updatedDays });
    
    res.json({ 
      success: true, 
      message: `Day ${dayId} ${enabled ? 'enabled' : 'disabled'} successfully` 
    });
  } catch (error) {
    console.error('Error updating day enabled status:', error);
    res.status(500).json({ success: false, error: 'Failed to update day status' });
  }
});

// Update permitted students for a day within a session
router.patch('/sessions/:sessionId/days/:dayId/students', verifyAdmin, async (req, res) => {
  try {
    const { sessionId, dayId } = req.params;
    const { permittedStudents } = req.body;
    
    // Ensure deduplication
    const deduplicatedStudents = [...new Set(permittedStudents || [])];
    
    const db = admin.firestore();
    const sessionRef = db.collection('sessions').doc(sessionId);
    const sessionDoc = await sessionRef.get();
    
    if (!sessionDoc.exists) {
      return res.status(404).json({ success: false, error: 'Session not found' });
    }
    
    const sessionData = sessionDoc.data();
    const updatedDays = sessionData.days.map(day => 
      day.id === dayId ? { ...day, permittedStudents: deduplicatedStudents } : day
    );
    
    await sessionRef.update({ days: updatedDays });
    
    res.json({ 
      success: true, 
      message: 'Permitted students updated successfully',
      dayId,
      permittedStudents: deduplicatedStudents,
      count: deduplicatedStudents.length
    });
  } catch (error) {
    console.error('Error updating permitted students:', error);
    res.status(500).json({ success: false, error: 'Failed to update permitted students' });
  }
});

// Add video to a day within a session
router.post('/sessions/:sessionId/days/:dayId/videos', verifyAdmin, async (req, res) => {
  try {
    const { sessionId, dayId } = req.params;
    const { videoId, title, durationMs } = req.body;
    
    const db = admin.firestore();
    const sessionRef = db.collection('sessions').doc(sessionId);
    const sessionDoc = await sessionRef.get();
    
    if (!sessionDoc.exists) {
      return res.status(404).json({ success: false, error: 'Session not found' });
    }
    
    const sessionData = sessionDoc.data();
    const updatedDays = sessionData.days.map(day => {
      if (day.id === dayId) {
        const newVideo = { id: videoId, title, durationMs };
        return { ...day, videos: [...day.videos, newVideo] };
      }
      return day;
    });
    
    await sessionRef.update({ days: updatedDays });
    
    res.json({ 
      success: true, 
      message: 'Video added to day successfully',
      dayId,
      videoId
    });
  } catch (error) {
    console.error('Error adding video to day:', error);
    res.status(500).json({ success: false, error: 'Failed to add video to day' });
  }
});


// Generate Mux upload URL
router.post('/upload/mux-url', verifyAdmin, async (req, res) => {
  try {
    const { title } = req.body;
    
    const uploadData = await muxService.createDirectUpload(title || 'Untitled Video');
    
    res.json({
      uploadUrl: uploadData.uploadUrl,
      uploadId: uploadData.uploadId,
      assetId: uploadData.assetId,
      message: 'Mux upload URL generated successfully'
    });
  } catch (error) {
    console.error('Error generating Mux upload URL:', error);
    res.status(500).json({ error: 'Failed to generate upload URL' });
  }
});

// Generate Cloudinary upload signature
router.post('/upload/cloudinary-signature', verifyAdmin, async (req, res) => {
  try {
    const { folder = 'thumbnails', publicId = null } = req.body;
    
    const signatureData = cloudinaryService.generateSignature(folder, publicId);
    
    res.json({
      ...signatureData,
      message: 'Cloudinary signature generated successfully'
    });
  } catch (error) {
    console.error('Error generating Cloudinary signature:', error);
    res.status(500).json({ error: 'Failed to generate signature' });
  }
});

// Upload video with files (multipart form data)
router.post('/upload', verifyAdmin, upload.fields([
  { name: 'video', maxCount: 1 },
  { name: 'thumbnail', maxCount: 1 }
]), async (req, res) => {
  try {
    const { title, duration, assignedDay } = req.body;
    const videoFile = req.files?.video?.[0];
    const thumbnailFile = req.files?.thumbnail?.[0];
    
    if (!title || !assignedDay) {
      return res.status(400).json({ error: 'Title and assigned day are required' });
    }

    if (!videoFile) {
      return res.status(400).json({ error: 'Video file is required' });
    }

    if (!thumbnailFile) {
      return res.status(400).json({ error: 'Thumbnail image is required' });
    }

    console.log(`Processing upload: ${title} for ${assignedDay}`);

    // Upload thumbnail to Cloudinary
    const thumbnailResult = await cloudinaryService.uploadImage(
      thumbnailFile.buffer,
      `${title}_thumbnail_${Date.now()}`,
      'thumbnails'
    );

    console.log('Thumbnail uploaded:', thumbnailResult.publicId);

    // Create Mux asset (for now, we'll create a placeholder since we can't upload video directly)
    // In production, you would upload the video file to Mux first
    const muxResult = await muxService.createAsset(title);
    
    console.log('Mux asset created:', muxResult.assetId);

    const db = admin.firestore();
    const videoData = {
      title,
      duration: parseInt(duration) || 0,
      assignedDay,
      // Mux data
      muxAssetId: muxResult.assetId,
      muxPlaybackId: muxResult.playbackId,
      muxUrl: `https://stream.mux.com/${muxResult.playbackId}.m3u8`,
      // Cloudinary data
      thumbnailPublicId: thumbnailResult.publicId,
      thumbnailUrl: thumbnailResult.url,
      // Metadata
      uploadedBy: req.user.uid,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };

    const docRef = await db.collection('videos').add(videoData);
    
    // Add video to the session's videos array
    const sessionRef = db.collection('sessions').doc(assignedDay);
    await sessionRef.update({
      videos: admin.firestore.FieldValue.arrayUnion(docRef.id)
    });

    console.log(`Video uploaded successfully: ${docRef.id}`);

    res.json({
      message: 'Video uploaded successfully',
      videoId: docRef.id,
      video: videoData,
      muxAssetId: muxResult.assetId,
      thumbnailPublicId: thumbnailResult.publicId
    });
  } catch (error) {
    console.error('Error uploading video:', error);
    res.status(500).json({ error: 'Failed to upload video: ' + error.message });
  }
});

// Upload video metadata only (for when files are uploaded separately)
router.post('/upload/metadata', verifyAdmin, async (req, res) => {
  try {
    const { title, duration, assignedDay, muxAssetId, muxPlaybackId, muxUrl, thumbnailPublicId, thumbnailUrl } = req.body;
    
    if (!title || !assignedDay) {
      return res.status(400).json({ error: 'Title and assigned day are required' });
    }

    const db = admin.firestore();
    const videoData = {
      title,
      duration: parseInt(duration) || 0,
      assignedDay,
      muxAssetId: muxAssetId || '',
      muxPlaybackId: muxPlaybackId || '',
      muxUrl: muxUrl || '',
      thumbnailPublicId: thumbnailPublicId || '',
      thumbnailUrl: thumbnailUrl || '',
      uploadedBy: req.user.uid,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };

    const docRef = await db.collection('videos').add(videoData);
    
    // Add video to the session's videos array
    const sessionRef = db.collection('sessions').doc(assignedDay);
    await sessionRef.update({
      videos: admin.firestore.FieldValue.arrayUnion(docRef.id)
    });

    res.json({
      message: 'Video metadata uploaded successfully',
      videoId: docRef.id,
      video: videoData
    });
  } catch (error) {
    console.error('Error uploading video metadata:', error);
    res.status(500).json({ error: 'Failed to upload video metadata' });
  }
});

// Get videos by day
router.get('/videos/:dayId', verifyAdmin, async (req, res) => {
  try {
    const { dayId } = req.params;
    const db = admin.firestore();
    
    const snapshot = await db.collection('videos')
      .where('assignedDay', '==', dayId)
      .orderBy('createdAt', 'desc')
      .get();
    
    const videos = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));

    res.json({ videos });
  } catch (error) {
    console.error('Error fetching videos:', error);
    res.status(500).json({ error: 'Failed to fetch videos' });
  }
});

// Get analytics data (placeholder)
router.get('/analytics', verifyAdmin, async (req, res) => {
  try {
    const db = admin.firestore();
    
    // Get total users
    const usersSnapshot = await db.collection('users').get();
    const totalUsers = usersSnapshot.size;
    
    // Get total videos
    const videosSnapshot = await db.collection('videos').get();
    const totalVideos = videosSnapshot.size;
    
    // Get enabled sessions
    const sessionsSnapshot = await db.collection('sessions').where('enabled', '==', true).get();
    const enabledSessions = sessionsSnapshot.size;

    res.json({
      analytics: {
        totalUsers,
        totalVideos,
        enabledSessions,
        lastUpdated: new Date().toISOString()
      }
    });
  } catch (error) {
    console.error('Error fetching analytics:', error);
    res.status(500).json({ error: 'Failed to fetch analytics' });
  }
});

// Generate signed upload URLs for direct upload to Mux and Cloudinary
router.post('/upload/signed-urls', verifyAdmin, async (req, res) => {
  try {
    const { sessionId, dayId, title, durationMs } = req.body;
    
    if (!sessionId || !dayId || !title) {
      return res.status(400).json({ 
        success: false,
        error: 'sessionId, dayId, and title are required' 
      });
    }
    
    // Generate unique upload ID for tracking
    const uploadId = `upload_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    // Get Mux direct upload URL
    const muxUploadData = await muxService.createDirectUpload(title);
    
    // Get Cloudinary signed upload URL
    const cloudinaryUploadData = cloudinaryService.generateSignature('thumbnails', `${title}_${uploadId}`);
    
    res.json({
      success: true,
      muxUploadUrl: muxUploadData.uploadUrl,
      muxAssetId: muxUploadData.assetId,
      cloudinaryUploadUrl: `https://api.cloudinary.com/v1_1/${cloudinaryUploadData.cloudName}/image/upload`,
      cloudinaryPublicId: `${title}_${uploadId}`,
      uploadId: uploadId,
      message: 'Signed upload URLs generated successfully'
    });
  } catch (error) {
    console.error('Error generating signed upload URLs:', error);
    res.status(500).json({ 
      success: false,
      error: 'Failed to generate signed upload URLs' 
    });
  }
});

// Complete upload by saving metadata after successful direct uploads
router.post('/upload/complete', verifyAdmin, async (req, res) => {
  try {
    const { 
      uploadId, 
      sessionId, 
      dayId, 
      title, 
      durationMs, 
      muxAssetId, 
      muxPlaybackId, 
      cloudinaryPublicId, 
      cloudinaryUrl 
    } = req.body;
    
    if (!uploadId || !sessionId || !dayId || !title || !muxAssetId || !cloudinaryPublicId) {
      return res.status(400).json({ 
        success: false,
        error: 'uploadId, sessionId, dayId, title, muxAssetId, and cloudinaryPublicId are required' 
      });
    }
    
    const db = admin.firestore();
    
    // Create video document in videos collection
    const videoData = {
      title,
      durationMs: parseInt(durationMs) || 0,
      sessionId,
      dayId,
      muxAssetId,
      muxPlaybackId: muxPlaybackId || muxAssetId, // Use assetId as fallback
      muxUrl: `https://stream.mux.com/${muxPlaybackId || muxAssetId}.m3u8`,
      cloudinaryPublicId,
      cloudinaryUrl: cloudinaryUrl || `https://res.cloudinary.com/${cloudinaryPublicId}`,
      uploadedBy: req.user.uid,
      uploadId,
      status: 'processing', // Default status, will be updated by webhook
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };
    
    const videoDocRef = await db.collection('videos').add(videoData);
    
    // Add video to the session's day videos array
    const sessionRef = db.collection('sessions').doc(sessionId);
    const sessionDoc = await sessionRef.get();
    
    if (!sessionDoc.exists) {
      return res.status(404).json({ 
        success: false,
        error: 'Session not found' 
      });
    }
    
    const sessionData = sessionDoc.data();
    const updatedDays = sessionData.days.map(day => {
      if (day.id === dayId) {
        const newVideo = {
          id: videoDocRef.id,
          title,
          durationMs: parseInt(durationMs) || 0
        };
        return { ...day, videos: [...day.videos, newVideo] };
      }
      return day;
    });
    
    await sessionRef.update({ days: updatedDays });
    
    res.json({
      success: true,
      message: 'Video metadata saved successfully',
      videoId: videoDocRef.id,
      uploadId
    });
  } catch (error) {
    console.error('Error completing upload:', error);
    res.status(500).json({ 
      success: false,
      error: 'Failed to complete upload' 
    });
  }
});

module.exports = router;

