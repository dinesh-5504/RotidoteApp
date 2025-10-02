const express = require('express');
const admin = require('firebase-admin');
const muxService = require('../services/muxService');
const router = express.Router();

// Middleware to capture raw body for signature verification
const rawBodyMiddleware = (req, res, next) => {
  let data = '';
  req.setEncoding('utf8');
  req.on('data', chunk => {
    data += chunk;
  });
  req.on('end', () => {
    req.rawBody = data;
    next();
  });
};

// Mux webhook endpoint
router.post('/mux', rawBodyMiddleware, async (req, res) => {
  try {
    const signature = req.headers['mux-signature'];
    const timestamp = req.headers['mux-timestamp'];
    
    if (!signature) {
      console.error('Missing Mux-Signature header');
      return res.status(401).json({ error: 'Missing signature' });
    }

    // Verify webhook signature
    const isValid = muxService.verifyWebhookSignature(req.rawBody, signature, timestamp);
    if (!isValid) {
      console.error('Invalid Mux webhook signature');
      return res.status(401).json({ error: 'Invalid signature' });
    }

    const event = JSON.parse(req.rawBody);
    console.log('Mux webhook received:', event.type, 'for asset:', event.data?.id);

    const db = admin.firestore();

    switch (event.type) {
      case 'video.upload.asset_created':
        await handleUploadAssetCreated(db, event);
        break;
      
      case 'video.asset.ready':
        await handleAssetReady(db, event);
        break;
      
      case 'video.asset.errored':
        await handleAssetErrored(db, event);
        break;
      
      default:
        console.log('Unhandled Mux webhook event type:', event.type);
    }

    res.status(200).json({ received: true });
  } catch (error) {
    console.error('Error processing Mux webhook:', error);
    res.status(500).json({ error: 'Webhook processing failed' });
  }
});

// Handle video.upload.asset_created event
async function handleUploadAssetCreated(db, event) {
  try {
    const assetId = event.data.id;
    const uploadId = event.data.upload_id;
    
    console.log(`Asset created: ${assetId} from upload: ${uploadId}`);
    
    // Find video document by uploadId and update with assetId
    const videosQuery = await db.collection('videos')
      .where('uploadId', '==', uploadId)
      .limit(1)
      .get();
    
    if (!videosQuery.empty) {
      const videoDoc = videosQuery.docs[0];
      await videoDoc.ref.update({
        muxAssetId: assetId,
        status: 'processing',
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
      
      console.log(`Updated video ${videoDoc.id} with asset ${assetId}`);
    } else {
      console.warn(`No video found for uploadId: ${uploadId}`);
    }
  } catch (error) {
    console.error('Error handling upload asset created:', error);
    throw error;
  }
}

// Handle video.asset.ready event
async function handleAssetReady(db, event) {
  try {
    const assetId = event.data.id;
    const playbackIds = event.data.playback_ids || [];
    const playbackId = playbackIds.find(p => p.policy === 'public')?.id || playbackIds[0]?.id;
    
    console.log(`Asset ready: ${assetId}, playback ID: ${playbackId}`);
    
    // Find video document by assetId and update status
    const videosQuery = await db.collection('videos')
      .where('muxAssetId', '==', assetId)
      .limit(1)
      .get();
    
    if (!videosQuery.empty) {
      const videoDoc = videosQuery.docs[0];
      const updateData = {
        status: 'ready',
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      };
      
      if (playbackId) {
        updateData.muxPlaybackId = playbackId;
        updateData.muxUrl = `https://stream.mux.com/${playbackId}.m3u8`;
      }
      
      await videoDoc.ref.update(updateData);
      
      console.log(`Video ${videoDoc.id} is now ready for playback`);
    } else {
      console.warn(`No video found for assetId: ${assetId}`);
    }
  } catch (error) {
    console.error('Error handling asset ready:', error);
    throw error;
  }
}

// Handle video.asset.errored event
async function handleAssetErrored(db, event) {
  try {
    const assetId = event.data.id;
    const errors = event.data.errors || [];
    
    console.log(`Asset errored: ${assetId}`, errors);
    
    // Find video document by assetId and update status
    const videosQuery = await db.collection('videos')
      .where('muxAssetId', '==', assetId)
      .limit(1)
      .get();
    
    if (!videosQuery.empty) {
      const videoDoc = videosQuery.docs[0];
      await videoDoc.ref.update({
        status: 'error',
        error: errors.length > 0 ? errors[0].message : 'Unknown error',
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
      
      console.log(`Video ${videoDoc.id} processing failed`);
    } else {
      console.warn(`No video found for assetId: ${assetId}`);
    }
  } catch (error) {
    console.error('Error handling asset errored:', error);
    throw error;
  }
}

module.exports = router;
