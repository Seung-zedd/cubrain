<script lang="ts">
  import { X, Zap, CircleCheck, Rocket } from "@lucide/svelte";
  import { fade, scale, fly } from "svelte/transition";

  let {
    onclose,
    type = "daily_limit",
    mode = "free",
  } = $props<{
    onclose: () => void;
    type?: "daily_limit";
    mode?: "guest" | "free";
  }>();

  function close() {
    onclose();
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === "Escape") {
      close();
    }
  }

  const content: Record<
    "daily_limit",
    {
      title: string;
      description: string;
      highlight: string;
      features: string[];
    }
  > = {
    daily_limit: {
      title:
        mode === "guest" ? "Unlock more with Cubrain!" : "You're in the zone!",
      description:
        mode === "guest"
          ? "Sign up for free to get more daily uploads and save your decks."
          : "Finish the entire exam scope with Cubrain Pro.",
      highlight:
        mode === "guest" ? "Guest Limit Reached" : "Daily limit reached (3/3)",
      features:
        mode === "guest"
          ? [
              "3 daily PDF uploads",
              "Save decks to library",
              "Track your progress",
              "Access on any device",
            ]
          : [
              "Unlimited daily uploads",
              "Process entire textbooks",
              "Priority AI generation",
              "Advanced study modes",
            ],
    },
  };

  let activeContent = $derived(content[type as "daily_limit"]);
</script>

<svelte:window onkeydown={handleKeydown} />

<div
  class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-4"
  transition:fade={{ duration: 200 }}
  role="dialog"
  aria-modal="true"
>
  <!-- Backdrop click handler -->
  <div
    class="absolute inset-0"
    onclick={close}
    role="button"
    tabindex="-1"
    onkeydown={(e) => e.key === "Enter" && close()}
  ></div>

  <div
    class="relative w-full max-w-lg overflow-hidden rounded-3xl border border-amber-500/30 bg-[#0f0f0f] shadow-[0_0_50px_rgba(255,215,0,0.15)]"
    transition:scale={{ duration: 300, start: 0.9, opacity: 0 }}
  >
    <!-- Premium Gradient Top -->
    <div
      class="absolute top-0 left-0 w-full h-1 bg-linear-to-r from-amber-500 via-yellow-300 to-amber-500"
    ></div>

    <!-- Close button -->
    <button
      onclick={close}
      class="absolute right-6 top-6 z-10 text-zinc-500 hover:text-white transition-colors"
    >
      <X class="w-6 h-6" />
    </button>

    <div class="p-10">
      <div class="flex flex-col items-center text-center mb-8">
        <div
          class="h-16 w-16 rounded-2xl bg-amber-500/10 flex items-center justify-center mb-6 border border-amber-500/20"
        >
          {#if type === "daily_limit"}
            <Rocket class="w-8 h-8 text-amber-500" />
          {:else}
            <Zap class="w-8 h-8 text-amber-500" />
          {/if}
        </div>

        <span
          class="px-3 py-1 rounded-full bg-amber-500/10 text-amber-500 text-xs font-bold uppercase tracking-widest mb-4 border border-amber-500/20"
        >
          {activeContent.highlight}
        </span>

        <h2 class="text-3xl font-bold text-white mb-4 tracking-tight">
          {activeContent.title}
        </h2>

        <p class="text-zinc-400 text-lg leading-relaxed max-w-md">
          {activeContent.description}
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-10">
        {#each activeContent.features as feature}
          <div
            class="flex items-center gap-3 p-3 rounded-xl bg-zinc-900/50 border border-zinc-800"
          >
            <CircleCheck class="w-5 h-5 text-amber-500 shrink-0" />
            <span class="text-zinc-300 text-sm font-medium">{feature}</span>
          </div>
        {/each}
      </div>

      <div class="space-y-4">
        <button
          class="group relative w-full h-14 overflow-hidden rounded-xl bg-amber-500 font-bold text-black shadow-[0_0_30px_rgba(255,215,0,0.3)] hover:bg-amber-400 hover:shadow-[0_0_40px_rgba(255,215,0,0.5)] transition-all duration-300"
        >
          <div class="flex items-center justify-center gap-2">
            <span
              >{mode === "guest"
                ? "Sign up for Free"
                : "Upgrade to Cubrain Pro"}</span
            >
            <Zap class="w-5 h-5 fill-current" />
          </div>

          <!-- Shine effect -->
          <div
            class="absolute inset-0 w-full h-full bg-linear-to-r from-transparent via-white/30 to-transparent -translate-x-full group-hover:animate-shine"
          ></div>
        </button>

        <button
          onclick={close}
          class="w-full py-3 text-zinc-500 hover:text-zinc-300 font-medium transition-colors"
        >
          Maybe later
        </button>
      </div>
    </div>
  </div>
</div>

<style>
  @keyframes shine {
    100% {
      transform: translateX(100%);
    }
  }
  .group-hover\:animate-shine:hover {
    animation: shine 0.8s ease-in-out;
  }
</style>
