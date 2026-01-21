import { createClient, type SupabaseClient } from "@supabase/supabase-js";
import { IS_DEV_MODE } from "$lib/utils/env";

const supabaseUrl = import.meta.env.VITE_PUBLIC_SUPABASE_URL || "";
const supabaseAnonKey = import.meta.env.VITE_PUBLIC_SUPABASE_ANON_KEY || "";

if (!supabaseUrl || !supabaseAnonKey) {
  if (IS_DEV_MODE) {
    console.warn(
      "Supabase URL or Anon Key is missing. Check your environment variables.",
    );
  }
}

export const supabase: SupabaseClient | null =
  supabaseUrl && supabaseAnonKey
    ? createClient(supabaseUrl, supabaseAnonKey)
    : null;
