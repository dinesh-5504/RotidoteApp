import { v2 as cloudinary } from 'cloudinary';

class CloudinaryService {
  constructor() {
    // Configure Cloudinary
    cloudinary.config({
      cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
      api_key: process.env.CLOUDINARY_API_KEY,
      api_secret: process.env.CLOUDINARY_API_SECRET,
    });
  }

  /**
   * Upload image to Cloudinary
   */
  async uploadImage(fileBuffer, fileName, folder = 'thumbnails') {
    try {
      return new Promise((resolve, reject) => {
        cloudinary.uploader.upload_stream(
          {
            folder: folder,
            public_id: fileName.replace(/\.[^/.]+$/, ''), // Remove file extension
            resource_type: 'image',
            transformation: [
              { width: 800, height: 450, crop: 'fill' }, // 16:9 aspect ratio
              { quality: 'auto' },
              { format: 'webp' }
            ]
          },
          (error, result) => {
            if (error) {
              console.error('Cloudinary upload error:', error);
              reject(new Error('Failed to upload image to Cloudinary'));
            } else {
              resolve({
                publicId: result.public_id,
                url: result.secure_url,
                width: result.width,
                height: result.height,
                format: result.format,
                bytes: result.bytes,
              });
            }
          }
        ).end(fileBuffer);
      });
    } catch (error) {
      console.error('Error uploading to Cloudinary:', error);
      throw new Error('Failed to upload image');
    }
  }

  /**
   * Generate upload signature for client-side upload
   */
  generateSignature(folder = 'thumbnails', publicId = null) {
    try {
      const timestamp = Math.round(new Date().getTime() / 1000);
      const params = {
        timestamp: timestamp,
        folder: folder,
        ...(publicId && { public_id: publicId }),
      };

      const signature = cloudinary.utils.api_sign_request(
        params,
        process.env.CLOUDINARY_API_SECRET
      );

      return {
        signature,
        timestamp,
        apiKey: process.env.CLOUDINARY_API_KEY,
        cloudName: process.env.CLOUDINARY_CLOUD_NAME,
      };
    } catch (error) {
      console.error('Error generating Cloudinary signature:', error);
      throw new Error('Failed to generate upload signature');
    }
  }

  /**
   * Delete image from Cloudinary
   */
  async deleteImage(publicId) {
    try {
      const result = await cloudinary.uploader.destroy(publicId);
      return result.result === 'ok';
    } catch (error) {
      console.error('Error deleting from Cloudinary:', error);
      throw new Error('Failed to delete image');
    }
  }

  /**
   * Get image details
   */
  async getImageDetails(publicId) {
    try {
      const result = await cloudinary.api.resource(publicId);
      return {
        publicId: result.public_id,
        url: result.secure_url,
        width: result.width,
        height: result.height,
        format: result.format,
        bytes: result.bytes,
        createdAt: result.created_at,
      };
    } catch (error) {
      console.error('Error getting image details:', error);
      throw new Error('Failed to get image details');
    }
  }
}

export default new CloudinaryService();
