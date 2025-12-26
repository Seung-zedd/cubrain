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
  const cookieVal = getCookie("isLoggedOut");

  // Sync: If the server explicitly cleared the logged out status (cookie is "false"),
  // then we must clear our local storage flag as well.
  if (cookieVal === "false") {
    if (typeof localStorage !== "undefined") {
      localStorage.removeItem("isLoggedOut");
    }
  }

  const isLoggedOutCookie = cookieVal === "true";
  const isLoggedOutLocal =
    typeof localStorage !== "undefined" &&
    localStorage.getItem("isLoggedOut") === "true";
  const isLoggedOut = isLoggedOutCookie || isLoggedOutLocal;

  if (import.meta.env.DEV) {
    console.log(
      "fetchUser: isLoggedOut(cookie):",
      isLoggedOutCookie,
      "isLoggedOut(local):",
      isLoggedOutLocal
    );
  }

  if (isLoggedOut) {
    user.set(null);
    return;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
      credentials: "include",
    });

    if (response.ok) {
      const data = await response.json();
      user.set(data);
      // If we successfully got a user, clear the logged out flag just in case
      if (typeof localStorage !== "undefined") {
        localStorage.removeItem("isLoggedOut");
      }
    } else if (response.status === 401) {
      // Check again in case it changed during the request
      const stillLoggedOut =
        getCookie("isLoggedOut") === "true" ||
        (typeof localStorage !== "undefined" &&
          localStorage.getItem("isLoggedOut") === "true");

      if (stillLoggedOut) {
        user.set(null);
        return;
      }

      // Try to refresh tokens
      try {
        const refreshResponse = await fetch(
          `${API_BASE_URL}/api/v1/auth/refresh`,
          {
            method: "POST",
            credentials: "include",
          }
        );

        if (refreshResponse.ok) {
          // Clear the logged out status if refresh succeeded
          if (typeof localStorage !== "undefined")
            localStorage.removeItem("isLoggedOut");
          return fetchUser();
        } else {
          user.set(null);
        }
      } catch {
        user.set(null);
      }
    } else {
      user.set(null);
    }
  } catch (err) {
    if (import.meta.env.DEV) {
      console.error("fetchUser error:", err);
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
  } catch (e) {
    if (import.meta.env.DEV) {
      console.error("Failed to logout", e);
    }
  }

  // Force set the isLoggedOut state on both cookie and localStorage as a fallback
  document.cookie = "isLoggedOut=true; path=/; max-age=1209600; samesite=lax";
  if (typeof localStorage !== "undefined") {
    localStorage.setItem("isLoggedOut", "true");
  }

  user.set(null);
  // Small delay to ensure cookies/storage are processed
  setTimeout(() => {
    window.location.href = "/";
  }, 100);
}
