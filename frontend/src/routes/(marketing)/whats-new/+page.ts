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

  // Sort by date descending
  allPosts.sort(
    (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
  );

  return {
    posts: allPosts,
  };
}
