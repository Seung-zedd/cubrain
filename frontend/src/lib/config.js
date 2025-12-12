export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  (import.meta.env.LOCAL ? "http://localhost:8080" : "https://api.cubrain.app");
