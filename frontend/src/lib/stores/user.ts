import { writable } from "svelte/store";
import { API_BASE_URL } from "$lib/config";

export interface User {
  email: string;
  role: string;
  tier: string;
  dailyUploadCount: number;
}

export const user = writable<User | null>(null);

export async function fetchUser() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      user.set(data);
    } else if (response.status === 401) {
      // Try to refresh tokens
      const refreshResponse = await fetch(
        `${API_BASE_URL}/api/v1/auth/refresh`,
        {
          method: "POST",
          credentials: "include",
        }
      );

      if (refreshResponse.ok) {
        // Retry fetching user
        const retryResponse = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
          credentials: "include",
        });
        if (retryResponse.ok) {
          const data = await retryResponse.json();
          user.set(data);
          return;
        }
      }
      user.set(null);
    } else {
      user.set(null);
    }
  } catch (e) {
    if (import.meta.env.DEV) {
      console.error("Failed to fetch user", e);
    }
    user.set(null);
  }
}

export async function logout() {
  try {
    await fetch(`${API_BASE_URL}/api/v1/auth/logout`, {
      method: "POST",
      credentials: "include",
    });
    user.set(null);
    window.location.href = "/";
  } catch (e) {
    if (import.meta.env.DEV) {
      console.error("Failed to logout", e);
    }
  }
}
