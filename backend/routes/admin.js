const express = require('express');
const admin = require('firebase-admin');
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

// Get all sessions
router.get('/sessions', verifyAdmin, async (req, res) => {
  try {
    const db = admin.firestore();
    const snapshot = await db.collection('sessions').orderBy('dayId').get();
    
    const sessions = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));

    res.json({ sessions });
  } catch (error) {
    console.error('Error fetching sessions:', error);
    res.status(500).json({ error: 'Failed to fetch sessions' });
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

// Toggle session enabled status
router.patch('/sessions/:dayId/toggle', verifyAdmin, async (req, res) => {
  try {
    const { dayId } = req.params;
    const { enabled } = req.body;
    
    const db = admin.firestore();
    await db.collection('sessions').doc(dayId).update({
      enabled: enabled
    });

    res.json({
      message: 'Session status updated successfully',
      dayId,
      enabled
    });
  } catch (error) {
    console.error('Error toggling session:', error);
    res.status(500).json({ error: 'Failed to toggle session' });
  }
});

// Update permitted students for a session
router.patch('/sessions/:dayId/students', verifyAdmin, async (req, res) => {
  try {
    const { dayId } = req.params;
    const { permittedStudents } = req.body;
    
    const db = admin.firestore();
    await db.collection('sessions').doc(dayId).update({
      permittedStudents: permittedStudents || []
    });

    res.json({
      message: 'Permitted students updated successfully',
      dayId,
      permittedStudents
    });
  } catch (error) {
    console.error('Error updating permitted students:', error);
    res.status(500).json({ error: 'Failed to update permitted students' });
  }
});

// Generate Mux upload URL
router.post('/upload/mux-url', verifyAdmin, async (req, res) => {
  try {
    // TODO: Implement Mux Direct Upload URL generation
    // For now, return a placeholder response
    const uploadUrl = `https://upload.mux.com/placeholder-${Date.now()}`;
    const uploadId = `upload_${Date.now()}`;
    const assetId = `asset_${Date.now()}`;
    
    res.json({
      uploadUrl,
      uploadId,
      assetId,
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
    // TODO: Implement Cloudinary signature generation
    // For now, return a placeholder response
    const signature = `signature_${Date.now()}`;
    const timestamp = Math.floor(Date.now() / 1000);
    
    res.json({
      signature,
      timestamp,
      apiKey: process.env.CLOUDINARY_API_KEY || 'placeholder',
      cloudName: process.env.CLOUDINARY_CLOUD_NAME || 'placeholder',
      message: 'Cloudinary signature generated successfully'
    });
  } catch (error) {
    console.error('Error generating Cloudinary signature:', error);
    res.status(500).json({ error: 'Failed to generate signature' });
  }
});

// Upload video metadata
router.post('/upload', verifyAdmin, async (req, res) => {
  try {
    const { title, description, duration, muxPlaybackId, thumbnailUrl, assignedDay } = req.body;
    
    if (!title || !assignedDay) {
      return res.status(400).json({ error: 'Title and assigned day are required' });
    }

    const db = admin.firestore();
    const videoData = {
      title,
      description: description || '',
      duration: duration || 0,
      muxPlaybackId: muxPlaybackId || '',
      thumbnailUrl: thumbnailUrl || '',
      assignedDay,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };

    const docRef = await db.collection('videos').add(videoData);
    
    // Add video to the session's videos array
    const sessionRef = db.collection('sessions').doc(assignedDay);
    await sessionRef.update({
      videos: admin.firestore.FieldValue.arrayUnion(docRef.id)
    });

    res.json({
      message: 'Video uploaded successfully',
      videoId: docRef.id,
      video: videoData
    });
  } catch (error) {
    console.error('Error uploading video:', error);
    res.status(500).json({ error: 'Failed to upload video' });
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

