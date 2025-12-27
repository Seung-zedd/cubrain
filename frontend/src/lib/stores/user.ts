import { supabase } from "$lib/supabaseClient";
import { authFetch } from "$lib/api";

export interface User {
  email: string;
  role: string;
  tier: string;
  dailyUploadCount: number;
}

// Svelte 5 Rune-based store
export const user = $state<{ current: User | null }>({
  current: null,
});

export async function fetchUser() {
  try {
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
      user.current = null;
    }
  } catch (err) {
    if (import.meta.env.DEV) {
      console.error("fetchUser error:", err);
    }
    user.current = null;
  }
}

// Initialize auth listener
if (typeof window !== "undefined") {
  supabase.auth.onAuthStateChange((event) => {
    if (import.meta.env.DEV) {
      console.log(`[Auth] Event: ${event}`);
    }
    if (event === "SIGNED_IN" || event === "TOKEN_REFRESHED") {
      fetchUser();
    } else if (event === "SIGNED_OUT") {
      user.current = null;
    }
  });
}

export async function logout() {
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
