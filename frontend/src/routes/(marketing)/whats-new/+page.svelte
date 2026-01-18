<script lang="ts">
  import { page } from "$app/state";
  import { fade, fly } from "svelte/transition";
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import ArrowRight from "@lucide/svelte/icons/arrow-right";
  import ArrowLeft from "@lucide/svelte/icons/arrow-left";

  let { data } = $props();
  const posts = data.posts;

  let fromApp = $state(false);
  const backHref = $derived(fromApp ? "/library" : "/");
  const backLabel = $derived(fromApp ? "Go to My Library" : "Back to Home");

  let showToast = $state(false);
  let toastTimeout: ReturnType<typeof setTimeout>;

  function triggerToast() {
    showToast = true;
    if (toastTimeout) clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => {
      showToast = false;
    }, 3000);
  }

  $effect(() => {
    fromApp = page.url.searchParams.get("from") === "app";
  });
</script>

<svelte:head>
  <title>What's New | Cubrain</title>
  <meta
    name="description"
    content="Latest updates, features, and improvements to Cubrain."
  />
</svelte:head>

<div
  class="min-h-screen relative text-white selection:bg-amber-500/30 overflow-x-hidden"
>
  <!-- Background Image with Overlay -->
  <div
    class="fixed inset-0 z-0 bg-fixed bg-cover bg-center"
    style="background-image: url('/images/neural-network.jpg');"
  ></div>
  <div class="fixed inset-0 z-0 bg-slate-950/40"></div>

  <div class="relative z-10 max-w-4xl mx-auto px-6 py-20">
    <!-- Header -->
    <header class="mb-16 text-center">
      <div
        class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-500 text-xs font-bold tracking-widest uppercase mb-6"
        in:fly={{ y: -20, duration: 800 }}
      >
        <Sparkles class="w-3 h-3" />
        Product Updates
      </div>
      <h1
        class="text-5xl md:text-6xl font-extrabold tracking-tight mb-6 bg-linear-to-b from-white to-slate-400 bg-clip-text text-transparent drop-shadow-sm"
        in:fly={{ y: 20, duration: 800, delay: 100 }}
      >
        What's New
      </h1>
      <p
        class="text-xl text-zinc-400 max-w-2xl mx-auto"
        in:fly={{ y: 20, duration: 800, delay: 200 }}
      >
        Stay up to date with the latest features, improvements, and bug fixes
        we've been working on.
      </p>
    </header>

    <!-- Posts List -->
    <div class="space-y-8">
      <!-- Lucidify Teaser Card (Pinned) -->
      <div in:fly={{ y: 30, duration: 800, delay: 300 }}>
        <button
          onclick={triggerToast}
          class="w-full text-left group block relative p-8 rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow-xl hover:border-violet-500/30 hover:shadow-[0_0_30px_-5px_rgba(139,92,246,0.3)] transition-all duration-500 cursor-pointer"
        >
          <div
            class="flex flex-col md:flex-row md:items-center justify-between gap-6"
          >
            <div class="space-y-4">
              <div class="flex items-center gap-3">
                <span
                  class="px-2.5 py-0.5 rounded-full bg-violet-500/20 border border-violet-500/30 text-violet-300 text-[10px] font-bold uppercase tracking-wider"
                >
                  Project in Progress
                </span>
              </div>
              <h2
                class="text-2xl font-bold text-transparent bg-clip-text bg-linear-to-r from-violet-300 to-fuchsia-300"
              >
                Lucidify: Visualizing your dreams
              </h2>
              <p class="text-zinc-400">
                Turn your subconscious memories into cinematic AI videos.
                Powered by Gemini 3 & Google Veo.
              </p>
            </div>
            <div class="shrink-0 self-end md:self-center">
              <span
                class="text-xs font-medium text-zinc-500 group-hover:text-zinc-300 transition-colors"
              >
                Coming Feb 2026 🔒
              </span>
            </div>
          </div>
        </button>
      </div>

      <div class="h-4"></div>
      <div class="border-t border-white/5"></div>
      <div class="h-4"></div>

      {#each posts as post, i}
        <div in:fly={{ y: 30, duration: 800, delay: 300 + i * 100 }}>
          <a
            href="/whats-new/{post.slug}{fromApp ? '?from=app' : ''}"
            class="group block relative p-8 rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow-xl hover:bg-white/10 hover:border-white/20 transition-all duration-300"
          >
            <div
              class="flex flex-col md:flex-row md:items-center justify-between gap-6"
            >
              <div class="space-y-4">
                <div class="flex items-center gap-3">
                  <span
                    class="px-2.5 py-0.5 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-500 text-[10px] font-bold uppercase tracking-wider"
                  >
                    {post.version}
                  </span>
                  <span
                    class="px-2.5 py-0.5 rounded-full bg-zinc-800 border border-zinc-700 text-zinc-400 text-[10px] font-bold uppercase tracking-wider"
                  >
                    {post.type}
                  </span>
                  <time class="text-xs text-zinc-500 font-medium">
                    {post.date}
                  </time>
                </div>
                <h2
                  class="text-2xl font-bold text-zinc-100 group-hover:text-white transition-colors"
                >
                  {post.title}
                </h2>
                <p class="text-zinc-400 line-clamp-2">
                  {post.description}
                </p>
              </div>
              <div class="shrink-0">
                <div
                  class="w-12 h-12 rounded-full bg-zinc-800 flex items-center justify-center group-hover:bg-amber-500 group-hover:text-black transition-all duration-300"
                >
                  <ArrowRight
                    class="w-5 h-5 transition-transform group-hover:translate-x-1"
                  />
                </div>
              </div>
            </div>
          </a>
        </div>
      {/each}
    </div>

    <!-- Back Button -->
    <div class="mt-20 text-center" in:fade={{ delay: 800 }}>
      <a
        href={backHref}
        class="inline-flex items-center gap-2 text-zinc-500 hover:text-zinc-300 transition-colors text-sm font-medium"
      >
        <ArrowLeft class="w-4 h-4" />
        {backLabel}
      </a>
    </div>
  </div>

  <!-- Toast Notification -->
  {#if showToast}
    <div
      transition:fly={{ y: 20, duration: 300 }}
      class="fixed bottom-10 left-1/2 -translate-x-1/2 z-50 bg-zinc-900/90 backdrop-blur border border-zinc-700 text-white px-6 py-3 rounded-full shadow-2xl flex items-center gap-3 whitespace-nowrap"
    >
      <span class="text-lg">🚀</span>
      <span class="font-medium"
        >Preparing for launch. Stay tuned for Feb 2026!</span
      >
    </div>
  {/if}
</div>

<style>
  /* Custom scrollbar for the page */
  :global(body) {
    background-color: black;
  }
</style>
