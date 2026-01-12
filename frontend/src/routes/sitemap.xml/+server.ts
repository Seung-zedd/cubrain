export const prerender = true;

export async function GET() {
  const site = "https://cubrain.app";

  // Static pages
  const staticPages = ["", "/pricing", "/whats-new", "/privacy", "/terms"];

  // Dynamically find all "What's New" posts
  const posts = import.meta.glob("../(marketing)/whats-new/**/+page.md");
  const postSlugs = Object.keys(posts).map((path) => {
    // Path looks like: ../(marketing)/whats-new/v1-mvp-launch/+page.md
    const parts = path.split("/");
    return `/whats-new/${parts[parts.length - 2]}`;
  });

  const pages = [...staticPages, ...postSlugs];

  const sitemap = `<?xml version="1.0" encoding="UTF-8" ?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${pages
  .map(
    (page) => `  <url>
    <loc>${site}${page}</loc>
    <changefreq>weekly</changefreq>
    <priority>0.8</priority>
  </url>`
  )
  .join("\n")}
</urlset>`;

  return new Response(sitemap, {
    headers: { "Content-Type": "application/xml" },
  });
}
