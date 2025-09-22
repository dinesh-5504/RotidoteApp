const Mux = require('@mux/mux-node');

class MuxService {
  constructor() {
    // Initialize Mux with API keys from environment variables
    this.client = new Mux({
      tokenId: process.env.MUX_TOKEN_ID,
      tokenSecret: process.env.MUX_TOKEN_SECRET,
  });
  }

  /**
   * Create a new asset for video upload
   */
  async createAsset(title, inputUrl = null) {
    try {
      const asset = await this.client.video.assets.create({
        input: inputUrl || [{ url: 'https://storage.googleapis.com/muxdemofiles/mux-video-intro.mp4' }],
        playback_policy: 'public',
        test: process.env.NODE_ENV !== 'production', // Use test mode in development
      });

      return {
        assetId: asset.id,
        playbackId: asset.playback_ids[0].id,
        status: asset.status,
        uploadUrl: null, // Direct upload not implemented yet
      };
    } catch (error) {
      console.error('Error creating Mux asset:', error);
      throw new Error('Failed to create Mux asset');
    }
  }

  /**
   * Generate a direct upload URL for client-side upload
   */
  async createDirectUpload(title) {
    try {
      const upload = await this.client.video.uploads.create({
        new_asset_settings: {
          playback_policy: 'public',
          test: process.env.NODE_ENV !== 'production',
        },
        cors_origin: '*',
      });

      return {
        uploadId: upload.id,
        uploadUrl: upload.url,
        assetId: upload.asset_id,
      };
    } catch (error) {
      console.error('Error creating Mux direct upload:', error);
      throw new Error('Failed to create Mux direct upload');
    }
  }

  /**
   * Get asset details
   */
  async getAsset(assetId) {
    try {
      const asset = await this.client.video.assets.retrieve(assetId);
      return {
        assetId: asset.id,
        playbackId: asset.playback_ids[0]?.id,
        status: asset.status,
        duration: asset.duration,
        aspectRatio: asset.aspect_ratio,
        createdAt: asset.created_at,
      };
    } catch (error) {
      console.error('Error retrieving Mux asset:', error);
      throw new Error('Failed to retrieve Mux asset');
    }
  }

  /**
   * Delete an asset
   */
  async deleteAsset(assetId) {
    try {
      await this.client.video.assets.del(assetId);
      return true;
    } catch (error) {
      console.error('Error deleting Mux asset:', error);
      throw new Error('Failed to delete Mux asset');
    }
  }
}

module.exports = new MuxService();
