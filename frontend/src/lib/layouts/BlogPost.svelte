<script lang="ts">
  // mdsvex passes frontmatter (title, date, etc.) as props
  let { title, description, date } = $props<{
    title: string;
    description: string;
    date: string;
  }>();

  // Default fallback for social image
  const imageUrl = "https://cubrain.app/og-image.png";
</script>

<svelte:head>
  <title>{title} | Cubrain Blog</title>

  <meta name="description" content={description} />

  <meta property="og:type" content="article" />
  <meta property="og:title" content={title} />
  <meta property="og:description" content={description} />
  <meta property="og:image" content={imageUrl} />

  <meta property="twitter:card" content="summary_large_image" />
  <meta property="twitter:title" content={title} />
  <meta property="twitter:description" content={description} />
</svelte:head>

<article class="blog-post">
  <header>
    <h1>{title}</h1>
    <p class="date">{date}</p>
  </header>

  <div class="content">
    <slot />
  </div>
</article>

<style>
  .blog-post {
    max-width: 48rem; /* max-w-3xl */
    margin: 0 auto; /* mx-auto */
    padding: 2.5rem 1rem; /* py-10 px-4 */
    font-family: "Inter", sans-serif;
    color: var(--text-color);
  }

  header {
    margin-bottom: 2rem; /* mb-8 */
    text-align: center;
  }

  h1 {
    font-size: 2.25rem; /* text-4xl */
    font-weight: 700; /* font-bold */
    margin-bottom: 0.5rem; /* mb-2 */
    line-height: 1.2;
    color: var(--text-color);
  }

  .date {
    color: var(--text-muted);
    font-size: 1rem;
  }

  /* Typography for markdown content */
  .content :global(h2) {
    font-size: 1.5rem;
    font-weight: 600;
    margin-top: 2rem;
    margin-bottom: 1rem;
    color: var(--text-color);
  }

  .content :global(h3) {
    font-size: 1.25rem;
    font-weight: 600;
    margin-top: 1.5rem;
    margin-bottom: 0.75rem;
    color: var(--text-color);
  }

  .content :global(p) {
    margin-bottom: 1.25rem;
    line-height: 1.75;
    color: var(--text-color);
  }

  .content :global(ul),
  .content :global(ol) {
    margin-bottom: 1.25rem;
    padding-left: 1.5rem;
    color: var(--text-color);
  }

  .content :global(ul) {
    list-style-type: disc;
  }

  .content :global(ol) {
    list-style-type: decimal;
  }

  .content :global(li) {
    margin-bottom: 0.5rem;
  }

  .content :global(a) {
    color: #fbbf24; /* Matching primary gradient start */
    text-decoration: underline;
    text-underline-offset: 2px;
  }

  .content :global(blockquote) {
    border-left: 4px solid var(--glass-border);
    padding-left: 1rem;
    font-style: italic;
    color: var(--text-muted);
    margin-bottom: 1.25rem;
  }

  .content :global(code) {
    background-color: rgba(255, 255, 255, 0.1);
    padding: 0.2rem 0.4rem;
    border-radius: 0.25rem;
    font-size: 0.875em;
    font-family: monospace;
    color: var(--text-color);
  }

  .content :global(pre) {
    background-color: rgba(0, 0, 0, 0.3);
    border: 1px solid var(--glass-border);
    color: var(--text-color);
    padding: 1rem;
    border-radius: 0.5rem;
    overflow-x: auto;
    margin-bottom: 1.5rem;
  }

  .content :global(pre code) {
    background-color: transparent;
    padding: 0;
    color: inherit;
  }
</style>
