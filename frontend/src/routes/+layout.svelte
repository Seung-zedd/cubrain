<script lang="ts">
  import "../app.css";
  import { browser, dev } from "$app/environment";
  import { injectSpeedInsights } from "@vercel/speed-insights/sveltekit";
  import { injectAnalytics } from "@vercel/analytics/sveltekit";

  import { onMount } from "svelte";
  import { fetchUser } from "$lib/stores/user.svelte";
  import LaunchBanner from "$lib/components/layout/LaunchBanner.svelte";

  if (browser && !dev) {
    injectSpeedInsights();
    injectAnalytics();
  }

  let { children } = $props();

  onMount(() => {
    fetchUser();
  });

  // SEO Configuration
  const title = "Cubrain - Study Smarter with AI Flashcards";
  const description =
    "Stop drowning in PDFs. Turn your study materials into AI-generated quizzes and flashcards instantly.";
  const url = "https://cubrain.app";
  const imageUrl = "https://cubrain.app/og-image.png";
</script>

<svelte:head>
  <link rel="icon" href="/favicon.ico" />
  <title>{title}</title>
  <meta name="description" content={description} />
  <link rel="canonical" href={url} />

  <meta property="og:type" content="website" />
  <meta property="og:url" content={url} />
  <meta property="og:title" content={title} />
  <meta property="og:description" content={description} />
  <meta property="og:image" content={imageUrl} />

  <meta property="twitter:card" content="summary_large_image" />
  <meta property="twitter:url" content={url} />
  <meta property="twitter:title" content={title} />
  <meta property="twitter:description" content={description} />
  <meta property="twitter:image" content={imageUrl} />
</svelte:head>

<LaunchBanner />
{@render children()}
