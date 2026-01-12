<script lang="ts">
  import { uiState } from "$lib/stores/ui.svelte";
  import Play from "@lucide/svelte/icons/play";
  import { fade } from "svelte/transition";

  let { decks, onStartStudy } = $props();

  async function handleStartStudy(deckId: number) {
    // Play sound
    const audio = new Audio("/sounds/switch-on.mp3");
    audio.play().catch((err) => {
      if (import.meta.env.DEV) {
        console.warn("Audio playback failed:", err);
      }
    });

    // Trigger transition
    uiState.setTransitioning(true);

    // Wait 1s (bulb heating up simulation)
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Enter study mode
    uiState.setTransitioning(false);
    uiState.setStudyMode(true);
    onStartStudy(deckId);
  }
</script>

<div class="library-container fixed inset-0 z-0 overflow-y-auto" in:fade>
  <!-- Atmospheric Background with Overlay -->
  <div class="bg-overlay fixed inset-0 -z-10"></div>

  <div class="relative z-10 p-8 md:p-16 max-w-7xl mx-auto">
    <header class="mb-12" in:fade={{ delay: 200 }}>
      <h1 class="text-5xl font-bold text-amber-400 tracking-tight mb-4">
        My Library
      </h1>
      <p class="text-zinc-300 text-lg max-w-2xl">
        Select a collection to begin your immersive study session.
      </p>
    </header>

    <div class="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
      {#each decks as deck, i}
        <div
          class="deck-card group relative bg-zinc-900/40 backdrop-blur-md border border-zinc-800/50 rounded-2xl p-6 hover:border-amber-500/50 transition-all duration-500 hover:shadow-[0_0_30px_rgba(251,191,36,0.1)]"
          in:fade={{ delay: 300 + i * 100 }}
        >
          <div class="mb-6">
            <h2
              class="text-2xl font-bold text-white mb-2 group-hover:text-amber-400 transition-colors"
            >
              {deck.title}
            </h2>
            <p class="text-zinc-400 flex items-center gap-2">
              <span class="w-1.5 h-1.5 rounded-full bg-amber-500/50"></span>
              {deck.cardCount} Cards
            </p>
          </div>

          <button
            onclick={() => handleStartStudy(deck.id)}
            class="w-full flex items-center justify-center gap-2 px-6 py-4 bg-amber-500 hover:bg-amber-400 text-black font-bold rounded-xl transition-all shadow-lg hover:shadow-amber-500/20 active:scale-[0.98]"
          >
            <Play class="w-5 h-5 fill-current" />
            Start Study
          </button>
        </div>
      {/each}
    </div>
  </div>
</div>

{#if uiState.isTransitioning}
  <div
    class="fixed inset-0 z-50 bg-black"
    transition:fade={{ duration: 300 }}
  ></div>
{/if}

<style>
  .bg-overlay {
    background-image: url("/images/library-bg.jpg");
    background-size: cover;
    background-position: center;
    /* 85% black overlay with blend mode for "ganji" look */
    background-color: rgba(0, 0, 0, 0.85);
    background-blend-mode: overlay;
  }

  .library-container {
    scrollbar-width: thin;
    scrollbar-color: rgba(251, 191, 36, 0.2) transparent;
  }

  .library-container::-webkit-scrollbar {
    width: 6px;
  }

  .library-container::-webkit-scrollbar-track {
    background: transparent;
  }

  .library-container::-webkit-scrollbar-thumb {
    background-color: rgba(251, 191, 36, 0.2);
    border-radius: 20px;
  }
</style>
