<script lang="ts">
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import X from "@lucide/svelte/icons/x";
  import { fade, slide } from "svelte/transition";
  import { onMount } from "svelte";

  let isVisible = $state(false);

  onMount(() => {
    const dismissed = localStorage.getItem("launch_banner_dismissed");
    if (!dismissed) {
      isVisible = true;
    }
  });

  function dismiss() {
    isVisible = false;
    localStorage.setItem("launch_banner_dismissed", "true");
  }
</script>

{#if isVisible}
  <div
    class="sticky top-0 z-50 w-full bg-linear-to-r from-amber-600 via-amber-500 to-amber-600 text-black py-2.5 px-4 shadow-lg"
    transition:slide={{ duration: 300 }}
  >
    <div class="max-w-7xl mx-auto flex items-center justify-between gap-4">
      <div class="flex-1 flex items-center justify-center gap-3">
        <div class="hidden sm:flex items-center gap-1">
          <Sparkles class="w-4 h-4 fill-current animate-pulse" />
          <span class="text-[10px] font-black uppercase tracking-tighter"
            >Launch Sale</span
          >
        </div>
        <p class="text-sm sm:text-base font-bold tracking-tight">
          Early Bird Offer: Get <span class="underline decoration-2"
            >72% OFF</span
          >
          Lifetime Pro with code
          <span
            class="bg-black text-amber-500 px-2 py-0.5 rounded-md font-black"
            >EARLYBIRD</span
          >
        </p>
      </div>
      <button
        onclick={dismiss}
        class="p-1 hover:bg-black/10 rounded-full transition-colors shrink-0"
        aria-label="Dismiss banner"
      >
        <X class="w-4 h-4" />
      </button>
    </div>
  </div>
{/if}
