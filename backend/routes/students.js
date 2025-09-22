const express = require('express');
const admin = require('firebase-admin');
const router = express.Router();

// Middleware to verify student authentication
const verifyStudent = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    console.log(`🔐 [DEBUG] Student auth attempt - header present: ${!!authHeader}`);
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      console.log(`❌ [DEBUG] No valid authorization header`);
      return res.status(401).json({ error: 'No authorization token provided' });
    }

    const token = authHeader.split('Bearer ')[1];
    console.log(`🔍 [DEBUG] Token extracted, length: ${token.length}`);
    
    const decodedToken = await admin.auth().verifyIdToken(token);
    console.log(`✅ [DEBUG] Token verified for user: ${decodedToken.uid}, email: ${decodedToken.email}`);
    req.user = decodedToken;
    next();
  } catch (error) {
    console.error('❌ [DEBUG] Student auth verification failed:', error);
    res.status(401).json({ error: 'Invalid or expired token' });
  }
};

// Get student's permitted days
router.get('/permitted-days', verifyStudent, async (req, res) => {
  try {
    const userId = req.user.uid;
    console.log(`🔍 [DEBUG] Checking permitted days for user: ${userId}`);
    
    const db = admin.firestore();
    
    // Get all sessions and check which ones the student is permitted for
    const sessionsSnapshot = await db.collection('sessions')
      .where('enabled', '==', true)
      .get();
    
    console.log(`📊 [DEBUG] Found ${sessionsSnapshot.size} enabled sessions`);
    
    const permittedDays = [];
    
    sessionsSnapshot.forEach(doc => {
      const session = doc.data();
      console.log(`🔍 [DEBUG] Checking session ${session.dayId}: permittedStudents = ${session.permittedStudents}`);
      
      if (session.permittedStudents && session.permittedStudents.includes(userId)) {
        console.log(`✅ [DEBUG] User ${userId} is permitted for ${session.dayId}`);
        permittedDays.push({
          dayId: session.dayId,
          title: session.title,
          videoCount: session.videos ? session.videos.length : 0
        });
      } else {
        console.log(`❌ [DEBUG] User ${userId} is NOT permitted for ${session.dayId}`);
      }
    });
    
    // Sort by day number
    permittedDays.sort((a, b) => {
      const aNum = parseInt(a.dayId.replace('Day', ''));
      const bNum = parseInt(b.dayId.replace('Day', ''));
      return aNum - bNum;
    });
    
    console.log(`📋 [DEBUG] Final permitted days for user ${userId}: ${permittedDays.map(d => d.dayId)}`);
    
    res.json({
      permittedDays,
      totalDays: permittedDays.length
    });
  } catch (error) {
    console.error('Error fetching permitted days:', error);
    res.status(500).json({ error: 'Failed to fetch permitted days' });
  }
});

// Get videos for a specific day
router.get('/videos/:dayId', verifyStudent, async (req, res) => {
  try {
    const { dayId } = req.params;
    const userId = req.user.uid;
    console.log(`🎥 [DEBUG] Fetching videos for ${dayId} for user ${userId}`);
    
    const db = admin.firestore();
    
    // First check if student is permitted for this day
    console.log(`🔍 [DEBUG] Checking session permissions for ${dayId}`);
    const sessionDoc = await db.collection('sessions').doc(dayId).get();
    
    if (!sessionDoc.exists) {
      console.log(`❌ [DEBUG] Session ${dayId} not found`);
      return res.status(404).json({ error: 'Session not found' });
    }
    
    const session = sessionDoc.data();
    console.log(`📊 [DEBUG] Session data:`, {
      enabled: session.enabled,
      permittedStudents: session.permittedStudents,
      userId: userId,
      isPermitted: session.permittedStudents?.includes(userId)
    });
    
    if (!session.enabled) {
      console.log(`❌ [DEBUG] Session ${dayId} is not enabled`);
      return res.status(403).json({ error: 'Session is not enabled' });
    }
    
    if (!session.permittedStudents?.includes(userId)) {
      console.log(`❌ [DEBUG] User ${userId} not permitted for session ${dayId}`);
      return res.status(403).json({ error: 'Not permitted for this session' });
    }
    
    // Get videos for this day
    console.log(`🎬 [DEBUG] Fetching videos assigned to ${dayId}`);
    const videosSnapshot = await db.collection('videos')
      .where('assignedDay', '==', dayId)
      .get(); // Removed orderBy to avoid potential index issues
    
    console.log(`📹 [DEBUG] Found ${videosSnapshot.docs.length} videos for ${dayId}`);
    
    const videos = videosSnapshot.docs.map(doc => {
      const data = doc.data();
      console.log(`📄 [DEBUG] Video ${doc.id}:`, {
        title: data.title,
        assignedDay: data.assignedDay,
        muxPlaybackId: data.muxPlaybackId,
        thumbnailUrl: data.thumbnailUrl
      });
      return {
        id: doc.id,
        title: data.title || 'Untitled',
        duration: data.duration || 0,
        muxPlaybackId: data.muxPlaybackId || '',
        thumbnailUrl: data.thumbnailUrl || '',
        assignedDay: data.assignedDay || dayId,
        createdAt: data.createdAt?.toMillis() || Date.now()
      };
    });
    
    console.log(`✅ [DEBUG] Returning ${videos.length} videos for ${dayId}`);
    
    res.json({
      dayId,
      videos,
      totalVideos: videos.length
    });
  } catch (error) {
    console.error(`❌ [DEBUG] Error fetching videos for day ${req.params.dayId}:`, error);
    console.error('Error details:', error.message);
    console.error('Error stack:', error.stack);
    res.status(500).json({ 
      error: 'Failed to fetch videos',
      details: error.message 
    });
  }
});

module.exports = router;
