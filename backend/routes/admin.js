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

// Get all day sessions (with multiple sessions per day)
router.get('/sessions', verifyAdmin, async (req, res) => {
  try {
    const db = admin.firestore();
    const snapshot = await db.collection('sessions').orderBy('dayId').get();
    
    const daySessions = [];
    snapshot.docs.forEach(doc => {
      const data = doc.data();
      daySessions.push({
        dayId: doc.id,
        dayTitle: data.dayTitle || `Day ${doc.id.replace('Day', '')}`,
        enabled: data.enabled || false,
        sessions: data.sessions || [],
        createdAt: data.createdAt?.toMillis() || Date.now()
      });
    });
    
    // Ensure we have all 5 days
    for (let i = 1; i <= 5; i++) {
      const dayId = `Day${i}`;
      if (!daySessions.find(d => d.dayId === dayId)) {
        daySessions.push({
          dayId,
          dayTitle: `Day ${i}`,
          enabled: false,
          sessions: [],
          createdAt: Date.now()
        });
      }
    }

    res.json({ daySessions });
  } catch (error) {
    console.error('Error fetching day sessions:', error);
    res.status(500).json({ error: 'Failed to fetch day sessions' });
  }
});

// Create or update a session
router.post('/sessions/:dayId', verifyAdmin, async (req, res) => {
  try {
    const { dayId } = req.params;
    const { title, enabled, videos, permittedStudents } = req.body;
    
    const db = admin.firestore();
    const sessionData = {
      dayId,
      title: title || `Day ${dayId.replace('Day', '')}`,
      enabled: enabled || false,
      videos: videos || [],
      permittedStudents: permittedStudents || [],
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };

    await db.collection('sessions').doc(dayId).set(sessionData, { merge: true });

    res.json({
      message: 'Session updated successfully',
      session: sessionData
    });
  } catch (error) {
    console.error('Error updating session:', error);
    res.status(500).json({ error: 'Failed to update session' });
  }
});

// Toggle day session enabled status
router.patch('/sessions/:dayId/toggle', verifyAdmin, async (req, res) => {
  try {
    const { dayId } = req.params;
    const { enabled } = req.body;
    
    const db = admin.firestore();
    await db.collection('sessions').doc(dayId).update({
      enabled: enabled
    });

    res.json({
      message: 'Day session status updated successfully',
      dayId,
      enabled
    });
  } catch (error) {
    console.error('Error toggling day session:', error);
    res.status(500).json({ error: 'Failed to toggle day session' });
  }
});

// Create a new session within a day
router.post('/sessions/:dayId/sessions', verifyAdmin, async (req, res) => {
  try {
    const { dayId } = req.params;
    const { title, enabled, videos, permittedStudents } = req.body;
    
    const db = admin.firestore();
    const sessionRef = db.collection('sessions').doc(dayId);
    
    // Get current day session data
    const daySessionDoc = await sessionRef.get();
    const currentData = daySessionDoc.exists ? daySessionDoc.data() : { sessions: [] };
    const currentSessions = currentData.sessions || [];
    
    // Create new session
    const newSession = {
      sessionId: `${dayId}_Session${currentSessions.length + 1}`,
      dayId,
      sessionNumber: currentSessions.length + 1,
      title: title || `Session ${currentSessions.length + 1}`,
      enabled: enabled || false,
      videos: videos || [],
      permittedStudents: permittedStudents || [],
      createdAt: Date.now()
    };
    
    // Add new session to the day
    currentSessions.push(newSession);
    
    await sessionRef.set({
      ...currentData,
      sessions: currentSessions
    }, { merge: true });
    
    res.json({
      message: 'Session created successfully',
      session: newSession
    });
  } catch (error) {
    console.error('Error creating session:', error);
    res.status(500).json({ error: 'Failed to create session' });
  }
});

// Update a specific session within a day
router.put('/sessions/:dayId/sessions/:sessionId', verifyAdmin, async (req, res) => {
  try {
    const { dayId, sessionId } = req.params;
    const { title, enabled, videos, permittedStudents } = req.body;
    
    const db = admin.firestore();
    const sessionRef = db.collection('sessions').doc(dayId);
    
    // Get current day session data
    const daySessionDoc = await sessionRef.get();
    if (!daySessionDoc.exists) {
      return res.status(404).json({ error: 'Day session not found' });
    }
    
    const currentData = daySessionDoc.data();
    const currentSessions = currentData.sessions || [];
    
    // Find and update the specific session
    const sessionIndex = currentSessions.findIndex(s => s.sessionId === sessionId);
    if (sessionIndex === -1) {
      return res.status(404).json({ error: 'Session not found' });
    }
    
    currentSessions[sessionIndex] = {
      ...currentSessions[sessionIndex],
      title: title || currentSessions[sessionIndex].title,
      enabled: enabled !== undefined ? enabled : currentSessions[sessionIndex].enabled,
      videos: videos || currentSessions[sessionIndex].videos,
      permittedStudents: permittedStudents || currentSessions[sessionIndex].permittedStudents
    };
    
    await sessionRef.set({
      ...currentData,
      sessions: currentSessions
    }, { merge: true });
    
    res.json({
      message: 'Session updated successfully',
      session: currentSessions[sessionIndex]
    });
  } catch (error) {
    console.error('Error updating session:', error);
    res.status(500).json({ error: 'Failed to update session' });
  }
});

// Update permitted students for a session
router.patch('/sessions/:dayId/students', verifyAdmin, async (req, res) => {
  try {
    const { dayId } = req.params;
    const { permittedStudents } = req.body;
    
    // Ensure deduplication - remove any duplicate student IDs
    const deduplicatedStudents = [...new Set(permittedStudents || [])];
    
    const db = admin.firestore();
    await db.collection('sessions').doc(dayId).update({
      permittedStudents: deduplicatedStudents
    });

    console.log(`Updated permitted students for ${dayId}: ${deduplicatedStudents.length} students`);
    
    res.json({
      message: 'Permitted students updated successfully',
      dayId,
      permittedStudents: deduplicatedStudents,
      count: deduplicatedStudents.length
    });
  } catch (error) {
    console.error('Error updating permitted students:', error);
    res.status(500).json({ error: 'Failed to update permitted students' });
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

module.exports = router;

