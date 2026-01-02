<script lang="ts">
  import { onMount } from "svelte";
  import { X, Sparkles } from "@lucide/svelte";
  import { fade, slide } from "svelte/transition";

  let isVisible = $state(false);

  onMount(() => {
    const isDismissed = localStorage.getItem("launch-banner-dismissed");
    if (!isDismissed) {
      isVisible = true;
    }
  });

  function dismiss() {
    isVisible = false;
    localStorage.setItem("launch-banner-dismissed", "true");
  }
</script>

{#if isVisible}
  <div
    transition:slide={{ duration: 300 }}
    class="relative z-110 bg-[#FFD700] text-black py-2.5 px-4 shadow-lg"
  >
    <div class="max-w-7xl mx-auto flex items-center justify-center gap-3">
      <Sparkles class="w-4 h-4 animate-pulse" />
      <p class="text-sm md:text-base font-black tracking-tight text-center">
        Launch Special: Lock in Pro for $5.99/mo Forever.
        <span class="hidden md:inline opacity-70 ml-1 font-bold"
          >(Limited to first 100)</span
        >
      </p>
      <button
        onclick={dismiss}
        class="absolute right-4 p-1 hover:bg-black/10 rounded-full transition-colors"
        aria-label="Dismiss"
      >
        <X class="w-4 h-4" />
      </button>
    </div>
  </div>
{/if}
