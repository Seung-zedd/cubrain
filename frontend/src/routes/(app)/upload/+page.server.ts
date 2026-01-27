import type { RequestEvent } from "@sveltejs/kit";
import { API_BASE_URL } from "$lib/config/config";

export const load = async ({ fetch, cookies }: RequestEvent) => {
  const token = cookies.get("sb-access-token");

  const fetchRecentJob = async () => {
    if (!token) return null;

    const response = await fetch(`${API_BASE_URL}/api/v1/pdf/recent`, {
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
      recentJob: fetchRecentJob(),
    },
  };
};
