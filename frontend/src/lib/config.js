// Prioritize VITE_API_BASE_URL (set in Vercel for Dev/Preview)
const envUrl = import.meta.env.VITE_API_BASE_URL;

export const API_BASE_URL = envUrl
  ? envUrl
  : import.meta.env.LOCAL
  ? "http://localhost:8080" // Fallback for local development
  : "https://api.cubrain.app"; // Fallback for production
