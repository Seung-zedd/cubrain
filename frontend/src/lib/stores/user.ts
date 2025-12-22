import { writable } from "svelte/store";
import { API_BASE_URL } from "$lib/config";

export interface User {
  email: string;
  role: string;
  tier: string;
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
    } else {
      user.set(null);
    }
  } catch (e) {
    console.error("Failed to fetch user", e);
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
    console.error("Failed to logout", e);
  }
}
