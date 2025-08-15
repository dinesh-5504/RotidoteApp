# Vercel Deployment Guide

## Environment Variables Required

Make sure to add these environment variables in your Vercel project settings:

### Required Variables:
- `MUX_TOKEN_ID` - Your Mux Token ID
- `MUX_TOKEN_SECRET` - Your Mux Token Secret
- `CLOUDINARY_CLOUD_NAME` - Your Cloudinary Cloud Name
- `CLOUDINARY_API_KEY` - Your Cloudinary API Key
- `CLOUDINARY_API_SECRET` - Your Cloudinary API Secret

### Optional Variables:
- `NODE_ENV` - Set to "production" for production deployment
- `ALLOWED_ORIGINS` - Comma-separated list of allowed CORS origins

## Deployment Steps

1. **Connect to Vercel**:
   - Go to [vercel.com](https://vercel.com)
   - Import your GitHub repository
   - Set the root directory to `backend`

2. **Configure Build Settings**:
   - Framework Preset: Other
   - Build Command: `npm run vercel-build`
   - Output Directory: Leave empty
   - Install Command: `npm install`

3. **Add Environment Variables**:
   - Go to Project Settings → Environment Variables
   - Add all required variables listed above

4. **Deploy**:
   - Click "Deploy"
   - Wait for deployment to complete

## API Endpoints

After deployment, your API will be available at:
- Base URL: `https://your-project.vercel.app`
- Health Check: `https://your-project.vercel.app/health`
- Create Upload: `https://your-project.vercel.app/create-upload`
- Asset Details: `https://your-project.vercel.app/asset/:assetId`
- Upload Thumbnail: `https://your-project.vercel.app/upload-thumbnail`

## Testing

Test your deployment with:
```bash
# Health check
curl https://your-project.vercel.app/health

# Base route
curl https://your-project.vercel.app/
```

## Troubleshooting

- **404 Errors**: Make sure vercel.json is in the backend directory
- **Environment Variables**: Verify all required variables are set in Vercel
- **CORS Issues**: Check ALLOWED_ORIGINS variable
- **Build Failures**: Check the build logs in Vercel dashboard
