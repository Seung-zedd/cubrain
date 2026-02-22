<script lang="ts">
  import { uiState } from "$lib/stores/ui.svelte";
  import DeckCard from "../deck/DeckCard.svelte";
  import { fade } from "svelte/transition";
  import { IS_DEV_MODE } from "$lib/utils/env";
  import * as m from "$paraglide/messages";

  let {
    decks,
    onStartStudy,
    onDelete,
    onEditCards,
    isLoading = false,
  } = $props();

  async function handleStartStudy(deckId: number) {
    // Play sound
    const audio = new Audio("/sounds/switch-on.mp3");
    audio.play().catch((err) => {
      if (IS_DEV_MODE) {
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

<div class="library-container fixed inset-0 z-10 overflow-y-auto">
  <div class="relative z-10 p-8 md:p-16 max-w-7xl mx-auto">
    <header class="mb-12">
      <h1 class="text-5xl font-bold text-amber-400 tracking-tight mb-4">
        {m.library_title()}
      </h1>
      <p class="text-zinc-300 text-lg max-w-2xl">
        {m.library_subtitle()}
      </p>
    </header>

    <div class="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
      {#if isLoading}
        {#each Array(6) as _}
          <div
            class="h-48 rounded-xl bg-white/5 border border-white/10 animate-pulse"
          ></div>
        {/each}
      {:else}
        {#each decks as deck, i}
          <div in:fade={{ delay: 100 + i * 50 }}>
            <DeckCard
              {deck}
              {onDelete}
              {onEditCards}
              onStartStudy={handleStartStudy}
              isCinematic={true}
            />
          </div>
        {/each}
      {/if}
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
