import express from 'express';
import admin from 'firebase-admin';
const router = express.Router();

// Middleware to verify student authentication
const verifyStudent = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ error: 'No authorization token provided' });
    }

    const token = authHeader.split('Bearer ')[1];
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    console.error('Student auth verification failed:', error);
    res.status(401).json({ error: 'Invalid or expired token' });
  }
};

// Get student's permitted days
router.get('/permitted-days', verifyStudent, async (req, res) => {
  try {
    const userId = req.user.uid;
    const db = admin.firestore();
    
    // Get all sessions and check which ones the student is permitted for
    const sessionsSnapshot = await db.collection('sessions')
      .where('enabled', '==', true)
      .get();
    
    const permittedDays = [];
    
    sessionsSnapshot.forEach(doc => {
      const session = doc.data();
      if (session.permittedStudents && session.permittedStudents.includes(userId)) {
        permittedDays.push({
          dayId: session.dayId,
          title: session.title,
          videoCount: session.videos ? session.videos.length : 0
        });
      }
    });
    
    // Sort by day number
    permittedDays.sort((a, b) => {
      const aNum = parseInt(a.dayId.replace('Day', ''));
      const bNum = parseInt(b.dayId.replace('Day', ''));
      return aNum - bNum;
    });
    
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
    const db = admin.firestore();
    
    // First check if student is permitted for this day
    const sessionDoc = await db.collection('sessions').doc(dayId).get();
    if (!sessionDoc.exists) {
      return res.status(404).json({ error: 'Session not found' });
    }
    
    const session = sessionDoc.data();
    if (!session.enabled || !session.permittedStudents?.includes(userId)) {
      return res.status(403).json({ error: 'Not permitted for this session' });
    }
    
    // Get videos for this day
    const videosSnapshot = await db.collection('videos')
      .where('assignedDay', '==', dayId)
      .orderBy('createdAt', 'desc')
      .get();
    
    const videos = videosSnapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));
    
    res.json({
      dayId,
      videos,
      totalVideos: videos.length
    });
  } catch (error) {
    console.error('Error fetching videos for day:', error);
    res.status(500).json({ error: 'Failed to fetch videos' });
  }
});

export default router;
