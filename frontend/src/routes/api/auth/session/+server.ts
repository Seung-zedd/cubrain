import { json } from "@sveltejs/kit";
import type { RequestHandler } from "@sveltejs/kit";

export const POST: RequestHandler = async ({ request, cookies }) => {
  try {
    const { access_token, expires_in } = await request.json();

    if (access_token) {
      // Determine domain for cookie
      const host = request.headers.get("host") || "";
      const domain = host.includes("cubrain.app") ? ".cubrain.app" : undefined;

      cookies.set("sb-access-token", access_token, {
        path: "/",
        httpOnly: true,
        secure: !host.includes("localhost"),
        sameSite: "lax",
        domain: domain,
        maxAge: expires_in || 60 * 60 * 24 * 7,
      });
    } else {
      const host = request.headers.get("host") || "";
      const domain = host.includes("cubrain.app") ? ".cubrain.app" : undefined;

      cookies.delete("sb-access-token", {
        path: "/",
        domain: domain,
      });
    }

    return json({ success: true });
  } catch {
    return json({ success: false, error: "Invalid request" }, { status: 400 });
  }
};
