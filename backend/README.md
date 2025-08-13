# Rotidote Backend

A minimal Node.js backend for the Rotidote Android app that handles Mux Direct Upload URL generation.

## Features

- **Mux Direct Upload Integration**: Generate secure upload URLs for video content
- **Asset Management**: Retrieve video asset details and playback information
- **Security**: Rate limiting, CORS protection, and helmet security headers
- **Health Monitoring**: Health check endpoint for monitoring

## API Endpoints

### POST /create-upload
Generate a Mux Direct Upload URL for video uploads.

**Request Body:**
```json
{
  "filename": "video.mp4",
  "contentType": "video/mp4"
}
```

**Response:**
```json
{
  "uploadUrl": "https://upload.mux.com/...",
  "uploadId": "upload_id",
  "assetId": "asset_id"
}
```

### GET /asset/:assetId
Get details about a specific video asset.

**Response:**
```json
{
  "assetId": "asset_id",
  "playbackId": "playback_id",
  "status": "ready",
  "duration": 120.5,
  "aspectRatio": "16:9",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### GET /health
Health check endpoint.

**Response:**
```json
{
  "status": "OK",
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

## Setup Instructions

### Prerequisites
- Node.js 18+ 
- npm or yarn
- Mux account and API credentials

### 1. Install Dependencies
```bash
cd backend
npm install
```

### 2. Environment Configuration
1. Copy `env.example` to `.env`:
```bash
cp env.example .env
```

2. Update the `.env` file with your Mux credentials:
```env
MUX_TOKEN_ID=your_mux_token_id_here
MUX_TOKEN_SECRET=your_mux_token_secret_here
PORT=3000
NODE_ENV=production
ALLOWED_ORIGINS=https://your-app-domain.com
```

### 3. Get Mux Credentials
1. Go to [Mux Dashboard](https://dashboard.mux.com/)
2. Navigate to Settings > Access Tokens
3. Create a new token with the following permissions:
   - Video: Read
   - Video: Write
4. Copy the Token ID and Token Secret to your `.env` file

### 4. Run Locally
```bash
# Development mode
npm run dev

# Production mode
npm start
```

The server will start on `http://localhost:3000`

## Deployment

### Deploy to Render (Free Tier)

1. **Create a Render Account**
   - Go to [render.com](https://render.com)
   - Sign up for a free account

2. **Create a New Web Service**
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Select the `backend` directory

3. **Configure the Service**
   - **Name**: `rotidote-backend`
   - **Environment**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
   - **Plan**: Free

4. **Set Environment Variables**
   - Go to Environment tab
   - Add the following variables:
     - `MUX_TOKEN_ID`: Your Mux Token ID
     - `MUX_TOKEN_SECRET`: Your Mux Token Secret
     - `NODE_ENV`: `production`
     - `ALLOWED_ORIGINS`: Your app's domain (comma-separated if multiple)

5. **Deploy**
   - Click "Create Web Service"
   - Render will automatically deploy your backend

### Deploy to Railway (Free Tier)

1. **Create a Railway Account**
   - Go to [railway.app](https://railway.app)
   - Sign up with GitHub

2. **Create a New Project**
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Choose your repository and `backend` directory

3. **Configure Environment Variables**
   - Go to Variables tab
   - Add the same environment variables as above

4. **Deploy**
   - Railway will automatically deploy your backend

### Deploy to Vercel (Free Tier)

1. **Create a Vercel Account**
   - Go to [vercel.com](https://vercel.com)
   - Sign up with GitHub

2. **Import Project**
   - Click "New Project"
   - Import your GitHub repository
   - Set the root directory to `backend`

3. **Configure Environment Variables**
   - Go to Settings → Environment Variables
   - Add the same environment variables as above

4. **Deploy**
   - Click "Deploy"

## Security Considerations

- **API Keys**: Never commit your `.env` file to version control
- **CORS**: Configure `ALLOWED_ORIGINS` to only allow your app's domain
- **Rate Limiting**: The API includes rate limiting (100 requests per 15 minutes per IP)
- **HTTPS**: Always use HTTPS in production

## Testing

Test the endpoints using curl or Postman:

```bash
# Health check
curl http://localhost:3000/health

# Create upload URL
curl -X POST http://localhost:3000/create-upload \
  -H "Content-Type: application/json" \
  -d '{"filename":"test.mp4","contentType":"video/mp4"}'

# Get asset details
curl http://localhost:3000/asset/your_asset_id
```

## Troubleshooting

### Common Issues

1. **CORS Errors**: Make sure `ALLOWED_ORIGINS` includes your app's domain
2. **Mux Authentication Errors**: Verify your Mux credentials are correct
3. **Port Already in Use**: Change the `PORT` in your `.env` file

### Logs
Check the deployment platform's logs for detailed error information.

## Support

For issues related to:
- **Mux API**: Check [Mux Documentation](https://docs.mux.com/)
- **Deployment**: Check your deployment platform's documentation
- **Backend Code**: Check the server logs and error messages

