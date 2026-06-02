import { auth } from "$lib/firebaseClient";
import { onIdTokenChanged } from "firebase/auth";
import { authFetch } from "$lib/api";
import { IS_DEV_MODE } from "$lib/utils/env";

export interface User {
  id: number;
  email: string;
  role: string;
  tier: string;
  dailyUploadCount: number;
  endsAt: string | null;
  customerPortalUrl: string | null;
  subscriptionStatus: string | null;
}

// Svelte 5 Rune-based store
export const user = $state<{ current: User | null }>({
  current: null,
});

export const guestUsage = $state({ count: 0 });

export async function fetchGuestUsage() {
  try {
    const response = await authFetch("/api/v1/auth/usage");
    if (response.ok) {
      const data = await response.json();
      guestUsage.count = data.dailyUploadCount;
    }
  } catch (err) {
    if (IS_DEV_MODE) {
      console.error("fetchGuestUsage error:", err);
    }
  }
}

export async function fetchUser() {
  try {
    if (!auth) return;

    const currentUser = auth.currentUser;
    if (!currentUser) {
      user.current = null;
      return;
    }

    const response = await authFetch("/api/v1/auth/me");

    if (response.ok) {
      const data = await response.json();
      user.current = data;
      if (typeof localStorage !== "undefined") {
        localStorage.removeItem("isLoggedOut");
      }
    } else {
      // Fallback: Use session data if backend is unreachable or returns error
      user.current = {
        id: 0,
        email: currentUser.email || "",
        role: "USER",
        tier: "FREE_USER",
        dailyUploadCount: 0,
        endsAt: null,
        customerPortalUrl: null,
        subscriptionStatus: null,
      };
    }
  } catch (err) {
    if (IS_DEV_MODE) {
      console.error("fetchUser error:", err);
    }
    // Even on network error, try to keep the user "logged in" with session data
    const currentUser = auth?.currentUser;
    if (currentUser) {
      user.current = {
        id: 0,
        email: currentUser.email || "",
        role: "USER",
        tier: "FREE_USER",
        dailyUploadCount: 0,
        endsAt: null,
        customerPortalUrl: null,
        subscriptionStatus: null,
      };
    } else {
      user.current = null;
    }
  }
}

// Initialize auth listener
if (typeof window !== "undefined" && auth) {
  onIdTokenChanged(auth, async (currentUser) => {
    let token = null;
    let expires_in = null;

    if (currentUser) {
      token = await currentUser.getIdToken();
      expires_in = 3600; // default Firebase token lifetime (1 hour)
      if (IS_DEV_MODE) {
        console.log(`[Auth] User signed in or token refreshed: ${currentUser.email}`);
      }
    } else {
      if (IS_DEV_MODE) {
        console.log(`[Auth] User signed out`);
      }
    }

    // Sync session to HttpOnly cookie via server route
    try {
      await fetch("/api/auth/session", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          access_token: token,
          expires_in: expires_in,
        }),
      });
    } catch (e) {
      if (IS_DEV_MODE) {
        console.error("Failed to sync session cookie", e);
      }
    }

    if (currentUser) {
      await fetchUser();

      // If we just signed in and we are on the landing page, go to /library
      if (typeof window !== "undefined" && window.location.pathname === "/") {
        window.location.href = "/library";
      }
    } else {
      user.current = null;
      fetchGuestUsage();

      // Clear nudge persistence on sign out
      if (typeof localStorage !== "undefined") {
        Object.keys(localStorage).forEach((key) => {
          if (key.startsWith("nudge_dismissed_")) {
            localStorage.removeItem(key);
          }
        });
      }
    }
  });

  // Initial fetch for guest usage
  fetchGuestUsage();
}

export async function logout() {
  if (!auth) {
    user.current = null;
    return;
  }
  try {
    await auth.signOut();
  } catch (e) {
    if (IS_DEV_MODE) {
      console.error("Failed to logout", e);
    }
  }

  if (typeof localStorage !== "undefined") {
    localStorage.setItem("isLoggedOut", "true");

    // Clear nudge persistence on logout
    Object.keys(localStorage).forEach((key) => {
      if (key.startsWith("nudge_dismissed_")) {
        localStorage.removeItem(key);
      }
    });
  }

  user.current = null;
  setTimeout(() => {
    window.location.href = "/";
  }, 100);
}
