import { supabase } from "./supabaseClient";
import { API_BASE_URL } from "./config";

export async function authFetch(path: string, options: RequestInit = {}) {
  const {
    data: { session },
  } = await supabase.auth.getSession();
  const token = session?.access_token;

  const headers = new Headers(options.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const url = path.startsWith("http") ? path : `${API_BASE_URL}${path}`;

  return fetch(url, {
    ...options,
    headers,
  });
}
