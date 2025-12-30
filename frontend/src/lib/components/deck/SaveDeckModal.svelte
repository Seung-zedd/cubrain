<script lang="ts">
  import { X, Save, BookText, CircleAlert } from "@lucide/svelte";
  import { fade, scale, fly } from "svelte/transition";

  let {
    onclose,
    onsave,
    initialTitle = "",
    isSaving = false,
  } = $props<{
    onclose: () => void;
    onsave: (title: string) => void;
    initialTitle?: string;
    isSaving?: boolean;
  }>();

  let title = $state(initialTitle);
  let inputError = $state(false);

  function handleSave() {
    if (isSaving) return;
    if (!title || title.trim().length === 0) {
      inputError = false;
      setTimeout(() => {
        inputError = true;
      }, 10);
      return;
    }
    onsave(title.trim());
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === "Escape") {
      onclose();
    } else if (e.key === "Enter") {
      handleSave();
    }
  }
  function focusAction(node: HTMLInputElement) {
    node.focus();
  }
</script>

<svelte:window onkeydown={handleKeydown} />

<div
  class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4"
  transition:fade={{ duration: 200 }}
  role="dialog"
  aria-modal="true"
>
  <!-- Backdrop click handler -->
  <div
    class="absolute inset-0"
    onclick={onclose}
    role="button"
    tabindex="-1"
    onkeydown={(e) => e.key === "Enter" && onclose()}
  ></div>

  <div
    class="relative w-full max-w-md overflow-hidden rounded-2xl border border-gray-700 bg-[#1a1a1a] shadow-2xl"
    transition:scale={{ duration: 200, start: 0.95 }}
  >
    <!-- Close button -->
    <button
      onclick={onclose}
      class="absolute right-4 top-4 z-10 text-zinc-400 hover:text-white transition-colors"
    >
      <X class="w-5 h-5" />
    </button>

    <div class="p-8">
      <div class="flex flex-col items-center text-center mb-8">
        <div
          class="h-12 w-12 rounded-full bg-amber-500/20 flex items-center justify-center mb-4"
        >
          <BookText class="w-6 h-6 text-amber-500" />
        </div>
        <h2 class="text-2xl font-bold text-white mb-2">Name Your Deck</h2>
        <p class="text-zinc-400 text-sm">
          Give your new flashcard deck a descriptive name to find it easily in
          your library.
        </p>
      </div>

      <div class="space-y-6">
        <div class="relative">
          <label
            for="deck-name"
            class="block text-xs font-bold uppercase tracking-wider text-zinc-500 mb-2 ml-1"
          >
            Deck Name
          </label>
          <div class="relative">
            <input
              id="deck-name"
              type="text"
              placeholder="write your deck name"
              bind:value={title}
              use:focusAction
              oninput={() => (inputError = false)}
              class="w-full rounded-lg bg-zinc-800/50 border {inputError
                ? 'border-red-500 ring-1 ring-red-500'
                : 'border-zinc-700'} px-4 pr-10 py-3 text-white placeholder:text-zinc-500 focus:border-amber-500 focus:outline-none focus:ring-1 focus:ring-amber-500 transition-all {inputError
                ? 'animate-shake'
                : ''}"
            />
            {#if inputError}
              <div
                class="absolute right-3 top-1/2 -translate-y-1/2 text-red-500 animate-in fade-in zoom-in duration-200"
              >
                <CircleAlert class="w-4 h-4" />
              </div>
            {/if}
          </div>

          {#if inputError}
            <p
              transition:fly={{ y: -5, duration: 200 }}
              class="text-red-500 text-xs mt-2 ml-1 font-medium"
            >
              Please enter a deck name.
            </p>
          {/if}
        </div>

        <div class="flex gap-3">
          <button
            onclick={onclose}
            class="flex-1 px-4 py-3 rounded-lg border border-zinc-700 text-zinc-300 font-bold hover:bg-zinc-800 hover:text-white transition-all"
          >
            Cancel
          </button>
          <button
            onclick={handleSave}
            disabled={isSaving}
            class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-lg bg-amber-500 text-black font-bold hover:bg-amber-400 shadow-[0_0_15px_rgba(245,158,11,0.2)] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {#if isSaving}
              <div
                class="w-4 h-4 border-2 border-black/30 border-t-black rounded-full animate-spin"
              ></div>
              Saving...
            {:else}
              <Save class="w-4 h-4" />
              Save to Library
            {/if}
          </button>
        </div>
      </div>
    </div>
  </div>
</div>

<style>
  @keyframes shake {
    0%,
    100% {
      transform: translateX(0);
    }
    25% {
      transform: translateX(-4px);
    }
    75% {
      transform: translateX(4px);
    }
  }
  .animate-shake {
    animation: shake 0.2s ease-in-out 0s 2;
  }
</style>
