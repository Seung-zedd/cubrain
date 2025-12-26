import { writable } from "svelte/store";
import { API_BASE_URL } from "$lib/config";
import { getCookie } from "$lib/utils";

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
      // Check if user explicitly logged out
      const isLoggedOut = getCookie("isLoggedOut") === "true";
      if (import.meta.env.DEV) {
        console.log("fetchUser: 401 Unauthorized. isLoggedOut:", isLoggedOut);
      }

      if (isLoggedOut) {
        user.set(null);
        return;
      }

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
    // Small delay to ensure cookies are processed
    setTimeout(() => {
      window.location.href = "/";
    }, 100);
  } catch (e) {
    if (import.meta.env.DEV) {
      console.error("Failed to logout", e);
    }
  }
}
