<script lang="ts">
  import { fade, fly } from "svelte/transition";
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import ArrowRight from "@lucide/svelte/icons/arrow-right";
  import ArrowLeft from "@lucide/svelte/icons/arrow-left";

  let { data } = $props();
  const posts = data.posts;
</script>

<svelte:head>
  <title>What's New | Cubrain</title>
  <meta
    name="description"
    content="Latest updates, features, and improvements to Cubrain."
  />
</svelte:head>

<div class="min-h-screen bg-black text-white selection:bg-amber-500/30">
  <div class="max-w-4xl mx-auto px-6 py-20">
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
        class="text-5xl md:text-6xl font-extrabold tracking-tight mb-6 bg-linear-to-b from-white to-white/60 bg-clip-text text-transparent"
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
      {#each posts as post, i}
        <div in:fly={{ y: 30, duration: 800, delay: 300 + i * 100 }}>
          <a
            href="/whats-new/{post.slug}"
            class="group block relative p-8 rounded-2xl bg-zinc-900/50 border border-zinc-800 hover:border-amber-500/50 transition-all duration-300 hover:shadow-[0_0_30px_rgba(245,158,11,0.1)]"
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

    <!-- Back to Home -->
    <div class="mt-20 text-center" in:fade={{ delay: 800 }}>
      <a
        href="/"
        class="inline-flex items-center gap-2 text-zinc-500 hover:text-zinc-300 transition-colors text-sm font-medium"
      >
        <ArrowLeft class="w-4 h-4" />
        Go Back
      </a>
    </div>
  </div>
</div>

<style>
  /* Custom scrollbar for the page */
  :global(body) {
    background-color: black;
  }
</style>
