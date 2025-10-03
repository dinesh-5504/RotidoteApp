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

    // Get all sessions and check which days the student is permitted for
    const sessionsSnapshot = await db.collection('sessions').get();

    console.log(`📊 [DEBUG] Found ${sessionsSnapshot.size} sessions`);

    const permittedDays = [];

    sessionsSnapshot.forEach(doc => {
      const session = doc.data();
      console.log(`🔍 [DEBUG] Checking session ${session.title}: days = ${session.days?.length || 0}`);

      // Check each day in this session
      if (session.days && Array.isArray(session.days)) {
        session.days.forEach(day => {
          // Check if student is permitted for this day and day is enabled
          if (day.enabled && day.permittedStudents?.includes(userId)) {
            console.log(`✅ [DEBUG] User ${userId} is permitted for ${day.id} in session ${session.title}`);
            
            // Check if we already have this day (avoid duplicates across sessions)
            const existingDay = permittedDays.find(d => d.dayId === day.id);
            if (existingDay) {
              // Update existing day with additional session info
              existingDay.sessionCount += 1;
              existingDay.totalVideoCount += day.videos?.length || 0;
              existingDay.sessions.push({
                sessionId: session.id,
                sessionTitle: session.title
              });
            } else {
              // Add new permitted day
              permittedDays.push({
                dayId: day.id,
                title: day.title || `Day ${day.id.replace('Day', '')}`,
                sessionCount: 1,
                totalVideoCount: day.videos?.length || 0,
                sessions: [{
                  sessionId: session.id,
                  sessionTitle: session.title
                }]
              });
            }
          } else {
            console.log(`❌ [DEBUG] User ${userId} is NOT permitted for ${day.id} in session ${session.title} (enabled: ${day.enabled})`);
          }
        });
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
    
    // Search across all sessions to find the matching dayId
    console.log(`🔍 [DEBUG] Searching for day ${dayId} across all sessions`);
    const sessionsSnapshot = await db.collection('sessions').get();
    
    let targetDay = null;
    let targetSession = null;
    
    // Find the day across all sessions
    sessionsSnapshot.forEach(doc => {
      const session = doc.data();
      if (session.days && Array.isArray(session.days)) {
        const day = session.days.find(d => d.id === dayId);
        if (day) {
          targetDay = day;
          targetSession = session;
          console.log(`✅ [DEBUG] Found day ${dayId} in session ${session.title}`);
        }
      }
    });
    
    if (!targetDay) {
      console.log(`❌ [DEBUG] Day ${dayId} not found in any session`);
      return res.status(404).json({ error: 'Day not found' });
    }
    
    console.log(`📊 [DEBUG] Day ${dayId} data:`, {
      enabled: targetDay.enabled,
      videosCount: targetDay.videos?.length || 0,
      permittedStudentsCount: targetDay.permittedStudents?.length || 0,
      userId: userId
    });
    
    if (!targetDay.enabled) {
      console.log(`❌ [DEBUG] Day ${dayId} is not enabled`);
      return res.status(403).json({ error: 'Day is not enabled' });
    }
    
    // Verify userId is permitted for this day
    if (!targetDay.permittedStudents?.includes(userId)) {
      console.log(`❌ [DEBUG] User ${userId} not permitted for day ${dayId}`);
      return res.status(403).json({ error: 'Not permitted for this day' });
    }
    
    console.log(`✅ [DEBUG] User ${userId} is permitted for day ${dayId}`);
    
    // Get all videos from this day's videos[] array
    const dayVideoIds = targetDay.videos?.map(video => video.id) || [];
    
    if (dayVideoIds.length === 0) {
      console.log(`ℹ️ [DEBUG] No videos found in day ${dayId}`);
      return res.json({
        dayId,
        videos: [],
        totalVideos: 0,
        sessionTitle: targetSession.title
      });
    }
    
    // Fetch video metadata from videos collection
    console.log(`🎬 [DEBUG] Fetching details for ${dayVideoIds.length} videos`);
    const videosSnapshot = await db.collection('videos')
      .where(admin.firestore.FieldPath.documentId(), 'in', dayVideoIds)
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
      sessionTitle: targetSession.title
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
