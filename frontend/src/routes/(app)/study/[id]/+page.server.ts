import type { RequestEvent } from "@sveltejs/kit";
import { API_BASE_URL } from "$lib/config/config";

export const load = async ({ fetch, cookies, params }: RequestEvent) => {
  const token = cookies.get("fb-access-token");
  const { id } = params;

  const fetchCards = async () => {
    if (!token) return [];

    const response = await fetch(`${API_BASE_URL}/api/v1/decks/${id}/cards`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      return [];
    }

    return response.json();
  };

  return {
    streamed: {
      cards: fetchCards(),
    },
  };
};
