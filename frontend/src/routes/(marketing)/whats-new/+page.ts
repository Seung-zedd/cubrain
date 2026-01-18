export const prerender = true;

interface Post {
  metadata: {
    title: string;
    description: string;
    date: string;
    version: string;
    type: string;
  };
}

interface Teaser {
  id: string;
  badge: string;
  title: string;
  description: string;
  cta: string;
  toast: string;
  bgImage: string;
  theme: {
    badge: string;
    title: string;
    hover: string;
  };
}

export async function load() {
  const posts = import.meta.glob("./**/+page.md");
  const postPromises = [];

  for (const path in posts) {
    postPromises.push(
      posts[path]().then((post) => {
        const typedPost = post as Post;
        const slug = path.split("/")[1];
        return {
          slug,
          ...typedPost.metadata,
        };
      }),
    );
  }

  const allPosts = await Promise.all(postPromises);

  // Sort by date descending, then by version descending
  allPosts.sort((a, b) => {
    const dateDiff = new Date(b.date).getTime() - new Date(a.date).getTime();
    if (dateDiff !== 0) return dateDiff;

    // Tie-breaker: Version descending (e.g., v1.2.0 > v1.1.0)
    return b.version.localeCompare(a.version, undefined, {
      numeric: true,
      sensitivity: "base",
    });
  });

  const teasers: Teaser[] = [
    {
      id: "lucidify",
      badge: "Project in Progress",
      title: "Lucidify: Visualizing your dreams",
      description:
        "Turn your subconscious memories into cinematic AI videos. Powered by Gemini 3 & Google Veo.",
      cta: "Coming Feb 2026 🔒",
      toast: "🚀 Preparing for launch. Stay tuned for Feb 2026!",
      bgImage: "/images/purple-dream.gif",
      theme: {
        badge: "bg-violet-500/20 text-violet-300 border-violet-500/30",
        title: "from-violet-300 to-fuchsia-300",
        hover:
          "hover:border-violet-500/30 hover:shadow-[0_0_30px_-5px_rgba(139,92,246,0.3)]",
      },
    },
  ];

  return {
    posts: allPosts,
    teasers,
  };
}
