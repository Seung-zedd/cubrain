<script lang="ts">
  import { page } from "$app/state";
  import "../app.css";
  import { browser } from "$app/environment";
  import { IS_DEV_MODE } from "$lib/utils/env";
  import { injectSpeedInsights } from "@vercel/speed-insights/sveltekit";
  import { injectAnalytics } from "@vercel/analytics/sveltekit";
  import { onMount } from "svelte";

  import { fetchUser, user } from "$lib/stores/user.svelte";
  import LaunchBanner from "$lib/components/layout/LaunchBanner.svelte";
  import { IS_LAUNCH_SALE } from "$lib/config/config";
  
  let { children } = $props();

  if (browser && !IS_DEV_MODE) {
    injectSpeedInsights();
    injectAnalytics();
  }

  onMount(() => {
    fetchUser();
  });

  // Clarity Cookie-less user identification logic using sessionStorage
  $effect(() => {
    if (!browser || typeof window === "undefined" || !window.clarity) return;

    const currentUser = user.current;
    if (currentUser) {
      let userSessionId = sessionStorage.getItem("clarity_user_session_id");
      if (!userSessionId) {
        userSessionId = crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2);
        sessionStorage.setItem("clarity_user_session_id", userSessionId);
      }
      window.clarity("identify", currentUser.email || `user_${currentUser.id}`, userSessionId);
    } else {
      let guestSessionId = sessionStorage.getItem("clarity_guest_session_id");
      if (!guestSessionId) {
        guestSessionId = crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2);
        sessionStorage.setItem("clarity_guest_session_id", guestSessionId);
      }
      window.clarity("identify", "guest", guestSessionId);
    }
  });

  // SEO Configuration
  const title = "Cubrain - Study Smarter with AI Flashcards";

  const description =
    "Stop drowning in PDFs. Turn your study materials into AI-generated quizzes and flashcards instantly.";
  const url = "https://cubrain.app";
  const imageUrl = "https://cubrain.app/og-image.png";
  let isStudyMode = $derived(page.url.pathname.startsWith("/study"));
  let isPro = $derived(user.current?.tier === "PRO_USER");
</script>

<svelte:head>
  <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />

  <link rel="icon" href="/favicon.ico" type="image/x-icon" />

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

{#if IS_LAUNCH_SALE && !isStudyMode && !isPro}
  <LaunchBanner />
{/if}

{@render children()}
