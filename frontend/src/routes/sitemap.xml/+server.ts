export async function GET() {
  const site = "https://cubrain.app";
  const pages = [
    "", // Home
    "/about",
    "/pricing",
  ];

  const sitemap = `
    <?xml version="1.0" encoding="UTF-8" ?>
    <urlset xmlns="https://www.sitemaps.org/schemas/sitemap/0.9">
      ${pages
        .map(
          (page) => `
      <url>
        <loc>${site}${page}</loc>
        <changefreq>weekly</changefreq>
        <priority>0.8</priority>
      </url>
      `
        )
        .join("")}
    </urlset>`.trim();

  return new Response(sitemap, {
    headers: { "Content-Type": "application/xml" },
  });
}
