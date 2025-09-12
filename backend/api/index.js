// Vercel serverless function entry point
// const app = require('../server');

// module.exports = app;

const app = require('../server'); // Import Express app
const serverless = require('serverless-http'); // Wrap for Vercel

module.exports = app; // for local testing
module.exports.handler = serverless(app); // for Vercel
