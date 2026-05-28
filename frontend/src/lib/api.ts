import { auth } from "./firebaseClient";
import { API_BASE_URL } from "./config/config";

export async function authFetch(path: string, options: RequestInit = {}) {
  let token: string | undefined;

  if (auth && auth.currentUser) {
    try {
      token = await auth.currentUser.getIdToken();
    } catch (e) {
      console.error("Failed to get ID token", e);
    }
  }

  const headers = new Headers(options.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  // Automatically set Content-Type for JSON bodies
  if (
    options.body &&
    typeof options.body === "string" &&
    !headers.has("Content-Type")
  ) {
    headers.set("Content-Type", "application/json");
  }

  const url = path.startsWith("http") ? path : `${API_BASE_URL}${path}`;

  return fetch(url, {
    ...options,
    headers,
    credentials: "include",
  });
}
