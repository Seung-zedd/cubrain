import { fail, redirect, isRedirect, type RequestEvent } from "@sveltejs/kit";
import type { Actions } from "./$types";
import { API_BASE_URL } from "$lib/config/config";
import { env } from "$env/dynamic/private";

export const load = async ({ fetch, cookies }: RequestEvent) => {
  const token = cookies.get("fb-access-token");

  const fetchProfile = async () => {
    if (!token) return null;

    const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      return null;
    }

    return response.json();
  };

  return {
    streamed: {
      profile: fetchProfile(),
    },
  };
};

export const actions: Actions = {
  deleteAccount: async ({ cookies, fetch, request }) => {
    const token = cookies.get("fb-access-token");
    const firebaseApiKey = env.VITE_PUBLIC_FIREBASE_API_KEY || "AIzaSyBuL8dKtGo9XFcMbtwFMayLk5Y0nPTSOKc";

    if (!firebaseApiKey) {
      console.error("Missing VITE_PUBLIC_FIREBASE_API_KEY");
      return fail(500, { error: "Server configuration error" });
    }

    if (!token) {
      return fail(401, { error: "Not authenticated" });
    }

    // 1. Check Subscription Status via Backend API
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        return fail(response.status, { error: "Failed to verify user status" });
      }

      const userData = await response.json();

      // Block deletion if Active Pro User
      const blockedStatuses = ["ACTIVE", "ON_TRIAL", "PAST_DUE"];
      if (
        userData.tier === "PRO_USER" &&
        blockedStatuses.includes(userData.subscriptionStatus)
      ) {
        return fail(400, {
          error:
            "You have an active subscription. Please cancel your subscription via 'Manage Subscription' before deleting your account to avoid unwanted charges.",
        });
      }

      // 2. Delete User from Firebase Auth via REST API
      const firebaseDeleteResponse = await fetch(
        `https://identitytoolkit.googleapis.com/v1/accounts:delete?key=${firebaseApiKey}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            idToken: token,
          }),
        }
      );

      if (!firebaseDeleteResponse.ok) {
        const errorData = await firebaseDeleteResponse.json().catch(() => ({}));
        const errorCode = errorData.error?.message;
        console.error("Firebase deletion error:", errorCode || "Unknown error");
        if (errorCode === "CREDENTIAL_TOO_OLD_RECENT_LOGIN_REQUIRED") {
          return fail(400, {
            error: "Please sign in again before deleting your account for security reasons.",
          });
        }
        return fail(500, {
          error: "Failed to delete auth account",
        });
      }

      // 3. Delete User from Backend DB via API
      const backendDeleteResponse = await fetch(
        `${API_BASE_URL}/api/v1/auth/me`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (!backendDeleteResponse.ok) {
        console.error(
          "Backend deletion failed:",
          await backendDeleteResponse.text(),
        );
        return fail(backendDeleteResponse.status, {
          error: "Failed to delete user data from database",
        });
      }

      // 4. Post-Deletion: Clear cookies and redirect
      const host = request.headers.get("host") || "";
      const domain = host.includes("cubrain.app") ? ".cubrain.app" : undefined;

      const cookieOptions = {
        path: "/",
        httpOnly: true,
        secure: !host.includes("localhost"),
        domain: domain,
      };

      cookies.delete("fb-access-token", cookieOptions);

      throw redirect(303, "/?deleted=true");
    } catch (err) {
      if (isRedirect(err)) {
        throw err;
      }
      console.error("Delete account error:", err);
      return fail(500, { error: "An unexpected error occurred" });
    }
  },
};
