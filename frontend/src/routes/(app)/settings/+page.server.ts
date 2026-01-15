import { fail, redirect } from "@sveltejs/kit";
import type { Actions } from "./$types";
import { createClient } from "@supabase/supabase-js";
import { API_BASE_URL } from "$lib/config/config";

import { env } from "$env/dynamic/private";

const SUPABASE_URL =
  env.SUPABASE_URL ||
  env.VITE_PUBLIC_SUPABASE_URL ||
  "https://nsnbzlzttvvxdlhsuskt.supabase.co";
const SUPABASE_SERVICE_ROLE_KEY = env.SUPABASE_SERVICE_ROLE_KEY;

export const actions: Actions = {
  deleteAccount: async ({ cookies, fetch }) => {
    const token = cookies.get("sb-access-token");

    if (!SUPABASE_SERVICE_ROLE_KEY) {
      console.error("Missing SUPABASE_SERVICE_ROLE_KEY");
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
      // BLOCK if status is ACTIVE, ON_TRIAL, or PAST_DUE
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

      // 2. Initialize Supabase Admin
      const supabaseAdmin = createClient(
        SUPABASE_URL,
        SUPABASE_SERVICE_ROLE_KEY as string,
        {
          auth: {
            autoRefreshToken: false,
            persistSession: false,
          },
        }
      );

      // 3. Get User ID from Supabase Auth
      const {
        data: { user },
        error: userError,
      } = await supabaseAdmin.auth.getUser(token);

      if (userError || !user) {
        return fail(401, { error: "Could not identify user" });
      }

      // 4. Delete User from Supabase Auth
      const { error: deleteError } = await supabaseAdmin.auth.admin.deleteUser(
        user.id
      );

      if (deleteError) {
        console.error("Deletion error:", deleteError);
        return fail(500, { error: "Failed to delete account" });
      }

      // 5. Post-Deletion: Clear cookies and redirect
      cookies.delete("sb-access-token", { path: "/" });
    } catch (err) {
      console.error("Delete account error:", err);
      return fail(500, { error: "An unexpected error occurred" });
    }

    throw redirect(303, "/?deleted=true");
  },
};
