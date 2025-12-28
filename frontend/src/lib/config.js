// Prioritize VITE_API_BASE_URL (set in Vercel for Dev/Preview)
const envUrl = import.meta.env.VITE_API_BASE_URL;

export const API_BASE_URL = envUrl
  ? envUrl
  : import.meta.env.DEV
  ? "https://api-dev.cubrain.app" // Changed to "https://api-dev.cubrain.app" to use remote dev backend
  : "https://api.cubrain.app";
