<script lang="ts">
  import { onMount } from "svelte";
  import { fade } from "svelte/transition";
  import X from "@lucide/svelte/icons/x";

  let { children, onExit } = $props();

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === "Escape") {
      onExit();
    }
  }

  onMount(() => {
    window.addEventListener("keydown", handleKeydown);
    return () => window.removeEventListener("keydown", handleKeydown);
  });
</script>

<div
  class="fixed inset-0 z-50 bg-black flex items-center justify-center overflow-hidden"
  in:fade={{ duration: 1000 }}
>
  <!-- Spotlight Effect (Vignette) -->
  <div class="spotlight fixed inset-0 pointer-events-none z-50"></div>

  <!-- Exit Button -->
  <button
    onclick={onExit}
    class="fixed top-8 right-8 p-3 text-zinc-600 hover:text-white transition-all duration-300 z-50 hover:rotate-90 active:scale-90 outline-none ring-0"
    aria-label="Exit Study Mode"
  >
    <X class="w-8 h-8" />
  </button>

  <!-- Flashcard Content -->
  <div class="relative z-50 w-full max-w-4xl px-4">
    {@render children()}
  </div>
</div>

<style>
  .spotlight {
    /* Radial gradient for the spotlight effect */
    background: radial-gradient(circle at center, transparent 30%, black 100%);
  }

  button {
    -webkit-tap-highlight-color: transparent;
  }
</style>
