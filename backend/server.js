import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import dotenv from 'dotenv';
dotenv.config();

// Import routes
import authRoutes from './routes/auth.js';
import adminRoutes from './routes/admin.js';
import studentRoutes from './routes/students.js';
import uploadRoutes from './routes/upload.js';

const app = express();
const PORT = process.env.PORT || 3000;

// Security middleware
app.use(helmet());
app.set('trust proxy', 1);

// CORS configuration
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true
}));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: 'Too many requests from this IP, please try again later.'
});
app.use(limiter);

// Body parsing middleware
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Mount routes
app.use('/auth', authRoutes);
app.use('/admin', adminRoutes);
app.use('/students', studentRoutes);
app.use('/api/upload', uploadRoutes);

// Base route
app.get('/', (req, res) => {
  res.json({ 
    message: 'Rotidote Backend API',
    status: 'OK', 
    timestamp: new Date().toISOString(),
    endpoints: {
      auth: {
        signup: 'POST /auth/signup',
        login: 'POST /auth/login',
        profile: 'GET /auth/profile'
      },
      admin: {
        students: 'GET /admin/students',
        sessions: 'GET /admin/sessions',
        upload: 'POST /admin/upload',
        analytics: 'GET /admin/analytics'
      },
      students: {
        permittedDays: 'GET /students/permitted-days',
        videos: 'GET /students/videos/:dayId'
      },
      upload: {
        mux: 'POST /api/upload/mux',
        cloudinary: 'POST /api/upload/cloudinary'
      }
    }
  });
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    error: 'Something went wrong!',
    details: process.env.NODE_ENV === 'development' ? err.message : 'Internal server error'
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Endpoint not found'
  });
});

// Validate environment variables on startup
console.log('🔍 Validating environment variables...');
const requiredEnvVars = [
  'FIREBASE_API_KEY'
];

const missingVars = requiredEnvVars.filter(varName => !process.env[varName]);
if (missingVars.length > 0) {
  console.warn('⚠️  Missing environment variables:', missingVars);
  console.warn('📝 Some features may not work properly without these variables');
} else {
  console.log('✅ All required environment variables are set');
}

//const PORT = process.env.PORT || 3000;

if (import.meta.url === `file://${process.argv[1]}`) {
  app.listen(PORT, () => {
    console.log(`🚀 Rotidote Backend server running on port ${PORT}`);
    console.log(`📡 Health check: http://localhost:${PORT}/health`);
    console.log(`🔐 Auth endpoints: http://localhost:${PORT}/auth/*`);
  });
}

export default app;