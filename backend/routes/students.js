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

// Get student's permitted days and sessions
router.get('/permitted-days', verifyStudent, async (req, res) => {
  try {
    const userId = req.user.uid;
    console.log(`🔍 [DEBUG] Checking permitted days for user: ${userId}`);

    const db = admin.firestore();

    // Get all day sessions and check which ones the student is permitted for
    const daySessionsSnapshot = await db.collection('sessions')
      .where('enabled', '==', true)
      .get();

    console.log(`📊 [DEBUG] Found ${daySessionsSnapshot.size} enabled day sessions`);

    const permittedDays = [];

    daySessionsSnapshot.forEach(doc => {
      const daySession = doc.data();
      console.log(`🔍 [DEBUG] Checking day session ${daySession.dayId}: sessions = ${daySession.sessions?.length || 0}`);

      // Check if student is permitted for any session within this day
      const permittedSessions = daySession.sessions?.filter(session => 
        session.enabled && session.permittedStudents?.includes(userId)
      ) || [];

      if (permittedSessions.length > 0) {
        console.log(`✅ [DEBUG] User ${userId} is permitted for ${permittedSessions.length} sessions in ${daySession.dayId}`);
        permittedDays.push({
          dayId: daySession.dayId,
          title: daySession.dayTitle || `Day ${daySession.dayId.replace('Day', '')}`,
          sessionCount: permittedSessions.length,
          totalVideoCount: permittedSessions.reduce((total, session) => total + (session.videos?.length || 0), 0)
        });
      } else {
        console.log(`❌ [DEBUG] User ${userId} is NOT permitted for any sessions in ${daySession.dayId}`);
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

// Get videos for a specific day (from all permitted sessions)
router.get('/videos/:dayId', verifyStudent, async (req, res) => {
  try {
    const { dayId } = req.params;
    const userId = req.user.uid;
    console.log(`🎥 [DEBUG] Fetching videos for ${dayId} for user ${userId}`);
    
    const db = admin.firestore();
    
    // First check if student is permitted for this day
    console.log(`🔍 [DEBUG] Checking day session permissions for ${dayId}`);
    const daySessionDoc = await db.collection('sessions').doc(dayId).get();
    
    if (!daySessionDoc.exists) {
      console.log(`❌ [DEBUG] Day session ${dayId} not found`);
      return res.status(404).json({ error: 'Day session not found' });
    }
    
    const daySession = daySessionDoc.data();
    console.log(`📊 [DEBUG] Day session data:`, {
      enabled: daySession.enabled,
      sessionsCount: daySession.sessions?.length || 0,
      userId: userId
    });
    
    if (!daySession.enabled) {
      console.log(`❌ [DEBUG] Day session ${dayId} is not enabled`);
      return res.status(403).json({ error: 'Day session is not enabled' });
    }
    
    // Get all permitted sessions for this day
    const permittedSessions = daySession.sessions?.filter(session => 
      session.enabled && session.permittedStudents?.includes(userId)
    ) || [];
    
    if (permittedSessions.length === 0) {
      console.log(`❌ [DEBUG] User ${userId} not permitted for any sessions in ${dayId}`);
      return res.status(403).json({ error: 'Not permitted for any sessions in this day' });
    }
    
    console.log(`✅ [DEBUG] User ${userId} is permitted for ${permittedSessions.length} sessions in ${dayId}`);
    
    // Collect all video IDs from permitted sessions
    const allVideoIds = [];
    permittedSessions.forEach(session => {
      if (session.videos && session.videos.length > 0) {
        allVideoIds.push(...session.videos);
        console.log(`📹 [DEBUG] Session ${session.sessionId} has ${session.videos.length} videos`);
      }
    });
    
    if (allVideoIds.length === 0) {
      console.log(`ℹ️ [DEBUG] No videos found in any permitted sessions for ${dayId}`);
      return res.json({
        dayId,
        videos: [],
        totalVideos: 0,
        sessionCount: permittedSessions.length
      });
    }
    
    // Get video details
    console.log(`🎬 [DEBUG] Fetching details for ${allVideoIds.length} videos`);
    const videosSnapshot = await db.collection('videos')
      .where(admin.firestore.FieldPath.documentId(), 'in', allVideoIds)
      .get();
    
    console.log(`📹 [DEBUG] Found ${videosSnapshot.docs.length} video documents`);
    
    const videos = videosSnapshot.docs.map(doc => {
      const data = doc.data();
      console.log(`📄 [DEBUG] Video ${doc.id}:`, {
        title: data.title,
        muxPlaybackId: data.muxPlaybackId,
        thumbnailUrl: data.thumbnailUrl
      });
      return {
        id: doc.id,
        title: data.title || 'Untitled',
        duration: data.duration || 0,
        muxPlaybackId: data.muxPlaybackId || '',
        muxUrl: data.muxPlaybackId ? `https://stream.mux.com/${data.muxPlaybackId}.m3u8` : '',
        thumbnailUrl: data.thumbnailUrl || '',
        assignedDay: data.assignedDay || dayId,
        createdAt: data.createdAt?.toMillis() || Date.now()
      };
    });
    
    console.log(`✅ [DEBUG] Returning ${videos.length} videos for ${dayId}`);
    
    res.json({
      dayId,
      videos,
      totalVideos: videos.length,
      sessionCount: permittedSessions.length
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
