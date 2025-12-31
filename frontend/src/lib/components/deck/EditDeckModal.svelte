<script lang="ts">
  import { X, Plus, Trash2, Save, CircleAlert } from "@lucide/svelte";
  import { fade, fly } from "svelte/transition";
  import { authFetch } from "$lib/api";

  interface Flashcard {
    id?: number;
    question: string;
    answer: string;
  }

  interface Deck {
    id: number;
    title: string;
    cards: Flashcard[];
  }

  let { deck, onclose, onsave } = $props<{
    deck: Deck;
    onclose: () => void;
    onsave: (updatedTitle: string, updatedCards: Flashcard[]) => void;
  }>();

  let cards = $state<Flashcard[]>(JSON.parse(JSON.stringify(deck.cards)));
  let editedTitle = $state(deck.title);
  let isSaving = $state(false);
  let validationErrors = $state<Record<string, boolean>>({});

  function addCard() {
    cards = [...cards, { question: "", answer: "" }];
  }

  function removeCard(index: number) {
    cards = cards.filter((_, i) => i !== index);
    // Clear errors for removed card
    validationErrors[`q-${index}`] = false;
    validationErrors[`a-${index}`] = false;
  }

  async function handleSave() {
    // Reset errors
    validationErrors = {};
    let hasError = false;

    cards.forEach((card, index) => {
      if (!card.question.trim()) {
        validationErrors[`q-${index}`] = true;
        hasError = true;
      }
      if (!card.answer.trim()) {
        validationErrors[`a-${index}`] = true;
        hasError = true;
      }
    });

    if (hasError) {
      // Force re-trigger of shake animation by clearing and re-setting errors
      const currentErrors = { ...validationErrors };
      validationErrors = {};
      setTimeout(() => {
        validationErrors = currentErrors;
      }, 10);
      return;
    }

    isSaving = true;
    try {
      // Save title if changed
      if (editedTitle !== deck.title) {
        await authFetch(`/api/v1/decks/${deck.id}`, {
          method: "PATCH",
          body: JSON.stringify({ title: editedTitle }),
        });
      }

      // Save cards
      const response = await authFetch(`/api/v1/decks/${deck.id}/cards`, {
        method: "PUT",
        body: JSON.stringify(cards),
      });

      if (response.ok) {
        onsave(editedTitle, cards);
        onclose();
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to save deck:", error);
      }
    } finally {
      isSaving = false;
    }
  }

  function handleBackdropClick(e: MouseEvent) {
    if (e.target === e.currentTarget) {
      onclose();
    }
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === "Escape") {
      onclose();
    }
  }

  $effect(() => {
    window.addEventListener("keydown", handleKeydown);
    return () => window.removeEventListener("keydown", handleKeydown);
  });
</script>

<div
  class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
  transition:fade={{ duration: 200 }}
  onclick={handleBackdropClick}
  role="button"
  tabindex="-1"
  onkeydown={handleKeydown}
>
  <div
    class="bg-zinc-900 border border-zinc-800 rounded-2xl w-full max-w-2xl shadow-2xl overflow-hidden flex flex-col"
    transition:fly={{ y: 20, duration: 300 }}
  >
    <!-- Header -->
    <div
      class="px-6 py-4 border-b border-zinc-800 flex items-center justify-between bg-zinc-900/50"
    >
      <div>
        <h2 class="text-xl font-bold text-white">Edit Deck</h2>
        <p class="text-zinc-500 text-sm">{editedTitle}</p>
      </div>
      <button
        onclick={onclose}
        class="p-2 hover:bg-zinc-800 rounded-full text-zinc-500 hover:text-white transition-colors"
      >
        <X class="w-6 h-6" />
      </button>
    </div>

    <!-- Content -->
    <div class="p-6 overflow-y-auto max-h-[70vh] space-y-6 custom-scrollbar">
      <!-- Deck Title Section -->
      <div class="pb-6 border-b border-zinc-800">
        <label
          class="block text-sm font-medium text-zinc-400 mb-2"
          for="deck-title">Deck Title</label
        >
        <input
          id="deck-title"
          bind:value={editedTitle}
          placeholder="Enter deck title..."
          class="w-full bg-zinc-900 border border-zinc-700 p-3 rounded-xl text-white focus:border-amber-500 outline-none transition-all placeholder:text-zinc-600 font-bold text-lg"
        />
      </div>

      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-medium text-zinc-400">Flashcards</h3>
          <span class="text-xs text-zinc-500">{cards.length} cards</span>
        </div>

        {#each cards as card, index}
          <div
            class="border rounded-xl p-5 bg-zinc-800/30 border-zinc-800 hover:border-zinc-700 transition-colors shadow-sm relative group animate-in fade-in slide-in-from-bottom-2 duration-300"
          >
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-2">
                <span
                  class="flex items-center justify-center w-6 h-6 rounded-full bg-zinc-800 text-xs font-bold text-zinc-500"
                >
                  {index + 1}
                </span>
                <span class="text-sm font-bold text-zinc-400">Card</span>
              </div>
              <button
                class="text-zinc-500 hover:text-red-400 transition-colors p-1.5 hover:bg-red-500/10 rounded-lg"
                onclick={() => removeCard(index)}
                title="Delete Card"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
            <div class="space-y-4">
              <div>
                <label
                  class="block text-[11px] uppercase tracking-wider font-bold text-zinc-500 mb-1.5"
                  for="q-{index}">Question</label
                >
                <div class="relative">
                  <input
                    id="q-{index}"
                    bind:value={card.question}
                    oninput={() => {
                      validationErrors[`q-${index}`] = false;
                    }}
                    placeholder="Enter question..."
                    class="w-full bg-zinc-900/50 border p-2.5 pr-10 rounded-lg text-white focus:border-amber-500/50 outline-none transition-all placeholder:text-zinc-600 {validationErrors[
                      `q-${index}`
                    ]
                      ? 'border-red-500/50 animate-shake'
                      : 'border-zinc-700/50'}"
                  />
                  {#if validationErrors[`q-${index}`]}
                    <div
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-red-500 animate-in fade-in zoom-in duration-200"
                    >
                      <CircleAlert class="w-4 h-4" />
                    </div>
                  {/if}
                </div>
                {#if validationErrors[`q-${index}`]}
                  <p
                    class="text-[11px] tracking-wider font-bold text-red-500 mt-2 ml-1 animate-in fade-in slide-in-from-top-1"
                  >
                    Question is required.
                  </p>
                {/if}
              </div>

              <div>
                <label
                  class="block text-[11px] uppercase tracking-wider font-bold text-zinc-500 mb-1.5"
                  for="a-{index}">Answer</label
                >
                <div class="relative">
                  <textarea
                    id="a-{index}"
                    bind:value={card.answer}
                    oninput={() => {
                      validationErrors[`a-${index}`] = false;
                    }}
                    placeholder="Enter answer..."
                    class="w-full bg-zinc-900/50 border p-2.5 pr-10 rounded-lg text-white focus:border-amber-500/50 outline-none transition-all placeholder:text-zinc-600 resize-none {validationErrors[
                      `a-${index}`
                    ]
                      ? 'border-red-500/50 animate-shake'
                      : 'border-zinc-700/50'}"
                    rows="2"
                  ></textarea>
                  {#if validationErrors[`a-${index}`]}
                    <div
                      class="absolute right-3 top-6 text-red-500 animate-in fade-in zoom-in duration-200"
                    >
                      <CircleAlert class="w-4 h-4" />
                    </div>
                  {/if}
                </div>
                {#if validationErrors[`a-${index}`]}
                  <p
                    class="text-[11px] tracking-wider font-bold text-red-500 mt-2 ml-1 animate-in fade-in slide-in-from-top-1"
                  >
                    Answer is required.
                  </p>
                {/if}
              </div>
            </div>
          </div>
        {/each}

        <button
          class="w-full py-4 border-2 border-dashed border-zinc-800 text-zinc-500 rounded-xl hover:border-amber-500/50 hover:text-amber-500 hover:bg-amber-500/5 transition-all flex items-center justify-center gap-2 font-medium"
          onclick={addCard}
        >
          <Plus class="w-5 h-5" />
          Add New Card
        </button>
      </div>
    </div>

    <!-- Footer -->
    <div class="px-6 py-4 border-t border-zinc-800 bg-zinc-900/50 flex gap-3">
      <button
        onclick={onclose}
        class="flex-1 px-4 py-2.5 rounded-xl bg-zinc-800 text-white font-bold hover:bg-zinc-700 transition-colors"
      >
        Cancel
      </button>
      <button
        onclick={handleSave}
        disabled={isSaving}
        class="flex-1 px-4 py-2.5 rounded-xl bg-amber-500 text-black font-bold hover:bg-amber-400 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        {#if isSaving}
          <div
            class="w-5 h-5 border-2 border-black/30 border-t-black rounded-full animate-spin"
          ></div>
          Saving...
        {:else}
          <Save class="w-5 h-5" />
          Save Changes
        {/if}
      </button>
    </div>
  </div>
</div>

<style>
  .custom-scrollbar::-webkit-scrollbar {
    width: 6px;
  }
  .custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
  }
  .custom-scrollbar::-webkit-scrollbar-thumb {
    background: #3f3f46;
    border-radius: 10px;
  }
  .custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #52525b;
  }

  .animate-shake {
    animation: shake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
    transform: translate3d(0, 0, 0);
    backface-visibility: hidden;
    perspective: 1000px;
  }

  @keyframes shake {
    10%,
    90% {
      transform: translate3d(-1px, 0, 0);
    }
    20%,
    80% {
      transform: translate3d(2px, 0, 0);
    }
    30%,
    50%,
    70% {
      transform: translate3d(-4px, 0, 0);
    }
    40%,
    60% {
      transform: translate3d(4px, 0, 0);
    }
  }
</style>
