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
      })
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

  return {
    posts: allPosts,
  };
}
