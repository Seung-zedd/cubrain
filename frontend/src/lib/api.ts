import { supabase } from "./supabaseClient";
import { API_BASE_URL } from "./config/config";

export async function authFetch(path: string, options: RequestInit = {}) {
  let token: string | undefined;

  if (supabase) {
    const {
      data: { session },
    } = await supabase.auth.getSession();
    token = session?.access_token;
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
