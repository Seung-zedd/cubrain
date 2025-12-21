// Prioritize VITE_API_BASE_URL (set in Vercel for Dev/Preview)
const envUrl = import.meta.env.VITE_API_BASE_URL;

export const API_BASE_URL = envUrl
  ? envUrl
  : import.meta.env.DEV
  ? "http://localhost:8080"
  : "https://api.cubrain.app"; // Fallback for production
