<script lang="ts">
  import { page } from "$app/state";

  let { title, description, date, version, type, children } = $props<{
    title: string;
    description: string;
    date: string;
    version: string;
    type: string;
    children?: any;
  }>();

  const imageUrl = "https://cubrain.app/og-image.png";
  let fromApp = $state(false);

  $effect(() => {
    fromApp = page.url.searchParams.get("from") === "app";
  });
</script>

<svelte:head>
  <title>{title} | Cubrain What's New</title>
  <meta name="description" content={description} />
  <meta property="og:title" content={title} />
  <meta property="og:description" content={description} />
  <meta property="og:image" content={imageUrl} />
  <meta name="twitter:card" content="summary_large_image" />
</svelte:head>

<div class="max-w-[800px] mx-auto px-6 pt-12">
  <a
    href="/whats-new{fromApp ? '?from=app' : ''}"
    class="group inline-flex items-center gap-2 text-zinc-400 hover:text-[#fbbf24] transition-colors font-medium text-sm"
  >
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2.5"
      stroke-linecap="round"
      stroke-linejoin="round"
      class="transition-transform group-hover:-translate-x-1"
      ><path d="m15 18-6-6 6-6" /></svg
    >
    Back to Updates
  </a>
</div>

<article class="changelog-post">
  <header>
    <div class="meta-info">
      <span class="badge version">{version}</span>
      <span class="badge type">{type}</span>
      <time datetime={date} class="date">{date}</time>
    </div>
    <h1>{title}</h1>
  </header>

  <div class="content">
    {@render children?.()}
  </div>
</article>

<style>
  .changelog-post {
    max-width: 800px;
    margin: 0 auto;
    padding: 2rem 1.5rem 4rem;
  }

  header {
    margin-bottom: 3rem;
  }

  .meta-info {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.25rem;
    font-size: 0.875rem;
    color: var(--muted-foreground);
  }

  .badge {
    display: inline-flex;
    align-items: center;
    padding: 0.35rem 0.85rem;
    border-radius: 9999px;
    font-weight: 600;
    line-height: 1;
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.025em;
  }

  .badge.version {
    background-color: rgba(251, 191, 36, 0.1); /* #fbbf24 with low opacity */
    color: #fbbf24;
    border: 1px solid rgba(251, 191, 36, 0.2);
  }

  .badge.type {
    background-color: var(--secondary);
    color: var(--secondary-foreground);
    border: 1px solid var(--border);
  }

  .date {
    font-weight: 500;
  }

  h1 {
    font-size: 3rem;
    font-weight: 800;
    margin: 0;
    line-height: 1.1;
    letter-spacing: -0.02em;
    color: var(--foreground);
  }

  .content {
    line-height: 1.7;
    color: var(--foreground);
    font-size: 1.125rem;
  }

  .content :global(h2) {
    font-size: 1.75rem;
    font-weight: 700;
    margin-top: 3.5rem;
    margin-bottom: 1.25rem;
    color: var(--foreground);
    border-bottom: 1px solid var(--border);
    padding-bottom: 0.5rem;
  }

  .content :global(h3) {
    font-size: 1.375rem;
    font-weight: 600;
    margin-top: 2.5rem;
    margin-bottom: 1rem;
    color: var(--foreground);
  }

  .content :global(p) {
    margin-bottom: 1.5rem;
  }

  .content :global(ul),
  .content :global(ol) {
    margin-bottom: 1.5rem;
    padding-left: 1.5rem;
  }

  .content :global(li) {
    margin-bottom: 0.875rem; /* Increased margin between list items */
  }

  .content :global(li::marker) {
    color: #fbbf24;
    font-weight: bold;
  }

  .content :global(a) {
    color: #fbbf24;
    text-decoration: underline;
    text-underline-offset: 4px;
    transition: opacity 0.2s;
  }

  .content :global(a:hover) {
    opacity: 0.8;
  }

  .content :global(code) {
    background-color: var(--secondary);
    padding: 0.2rem 0.4rem;
    border-radius: 0.375rem;
    font-size: 0.9em;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,
      "Liberation Mono", "Courier New", monospace;
  }

  .content :global(pre) {
    background-color: #111;
    border: 1px solid var(--border);
    padding: 1.25rem;
    border-radius: 0.75rem;
    overflow-x: auto;
    margin-bottom: 2rem;
  }

  .content :global(pre code) {
    background-color: transparent;
    padding: 0;
    color: #e5e7eb;
    font-size: 0.875rem;
  }

  @media (max-width: 640px) {
    h1 {
      font-size: 2.25rem;
    }

    .meta-info {
      flex-wrap: wrap;
      gap: 0.75rem;
    }
  }
</style>
