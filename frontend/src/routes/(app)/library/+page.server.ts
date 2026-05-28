import type { RequestEvent } from "@sveltejs/kit";
import { API_BASE_URL } from "$lib/config/config";

export const load = async ({ fetch, cookies }: RequestEvent) => {
  const token = cookies.get("fb-access-token");

  const fetchDecks = async () => {
    if (!token) return { content: [] };

    const response = await fetch(`${API_BASE_URL}/api/v1/decks`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      return { content: [] };
    }

    return response.json();
  };

  return {
    streamed: {
      decks: fetchDecks(),
    },
  };
};
