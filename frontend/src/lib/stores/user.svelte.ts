import { supabase } from "$lib/supabaseClient";
import { authFetch } from "$lib/api";

export interface User {
  id: number;
  email: string;
  role: string;
  tier: string;
  dailyUploadCount: number;
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
    if (import.meta.env.DEV) {
      console.error("fetchGuestUsage error:", err);
    }
  }
}

export async function fetchUser() {
  try {
    if (!supabase) return;

    const {
      data: { session },
    } = await supabase.auth.getSession();

    if (!session) {
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
      // This allows local frontend development without a running backend
      user.current = {
        id: 0,
        email: session.user.email || "",
        role: "USER",
        tier: "FREE_USER",
        dailyUploadCount: 0,
      };
    }
  } catch (err) {
    if (import.meta.env.DEV) {
      console.error("fetchUser error:", err);
    }
    // Even on network error, try to keep the user "logged in" with session data
    if (supabase) {
      const {
        data: { session },
      } = await supabase.auth.getSession();
      if (session) {
        user.current = {
          id: 0,
          email: session.user.email || "",
          role: "USER",
          tier: "FREE_USER",
          dailyUploadCount: 0,
        };
      } else {
        user.current = null;
      }
    } else {
      user.current = null;
    }
  }
}

// Initialize auth listener
if (typeof window !== "undefined" && supabase) {
  supabase.auth.onAuthStateChange(async (event, session) => {
    if (import.meta.env.DEV) {
      console.log(`[Auth] Event: ${event}`);
    }

    // Sync session to HttpOnly cookie via server route
    try {
      await fetch("/api/auth/session", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          access_token: session?.access_token || null,
          expires_in: session?.expires_in || null,
        }),
      });
    } catch (e) {
      console.error("Failed to sync session cookie", e);
    }

    if (event === "SIGNED_IN" || event === "TOKEN_REFRESHED") {
      fetchUser();

      // Clean up the URL hash if it contains auth tokens (Supabase OAuth flow)
      if (
        typeof window !== "undefined" &&
        window.location.hash.includes("access_token")
      ) {
        window.history.replaceState(
          null,
          "",
          window.location.pathname + window.location.search
        );

        // If we just signed in via OAuth, ensure we are on the library
        if (window.location.pathname === "/") {
          window.location.href = "/library";
        }
      }
    } else if (event === "SIGNED_OUT") {
      user.current = null;
      fetchGuestUsage();
    }
  });

  // Initial fetch for guest usage
  fetchGuestUsage();
}

export async function logout() {
  if (!supabase) {
    user.current = null;
    return;
  }
  try {
    await supabase.auth.signOut();
  } catch (e) {
    if (import.meta.env.DEV) {
      console.error("Failed to logout", e);
    }
  }

  if (typeof localStorage !== "undefined") {
    localStorage.setItem("isLoggedOut", "true");
  }

  user.current = null;
  setTimeout(() => {
    window.location.href = "/";
  }, 100);
}
