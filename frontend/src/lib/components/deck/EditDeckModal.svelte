<script lang="ts">
  import X from "@lucide/svelte/icons/x";
  import Save from "@lucide/svelte/icons/save";
  import Plus from "@lucide/svelte/icons/plus";
  import Trash2 from "@lucide/svelte/icons/trash-2";
  import BookOpen from "@lucide/svelte/icons/book-open";
  import { fade, scale, fly } from "svelte/transition";
  import { useModal } from "$lib/modal.svelte";
  import { authFetch } from "$lib/api";
  import Markdown from "$lib/components/ui/Markdown.svelte";
  import { cn } from "$lib/utils";
  import AlertCircle from "@lucide/svelte/icons/alert-circle";

  interface Flashcard {
    id?: number;
    question: string;
    answer: string;
    page?: number;
  }

  interface Deck {
    id: number;
    title: string;
  }

  let { deck, onclose, onsave } = $props<{
    deck: Deck;
    onclose: () => void;
    onsave: (title: string, cards: Flashcard[]) => void;
  }>();

  let cards = $state<Flashcard[]>([]);
  let isLoading = $state(true);
  let isSaving = $state(false);
  let title = $state(deck.title);
  let showValidation = $state(false);

  const hasErrors = $derived(
    cards.some((c) => !c.question.trim() || !c.answer.trim())
  );

  async function fetchCards() {
    try {
      const response = await authFetch(`/api/v1/decks/${deck.id}/cards`);
      if (response.ok) {
        cards = await response.json();
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to fetch cards:", error);
      }
    } finally {
      isLoading = false;
    }
  }

  function addCard() {
    cards = [...cards, { question: "", answer: "" }];
  }

  function removeCard(index: number) {
    cards = cards.filter((_, i) => i !== index);
  }

  async function handleSave() {
    showValidation = true;
    if (hasErrors) return;

    isSaving = true;
    try {
      const response = await authFetch(`/api/v1/decks/${deck.id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title,
          cards,
        }),
      });
      if (response.ok) {
        onsave(title, cards);
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

  const { handleBackdropClick, handleKeydown } = useModal(onclose);

  fetchCards();
</script>

<svelte:window onkeydown={handleKeydown} />

<div
  class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4"
  transition:fade={{ duration: 200 }}
  role="dialog"
  aria-modal="true"
  onclick={handleBackdropClick}
  onkeydown={handleKeydown}
  tabindex="-1"
>
  <div
    class="relative w-full max-w-4xl max-h-[90vh] overflow-hidden rounded-2xl border border-zinc-800 bg-[#1a1a1a] shadow-2xl flex flex-col"
    transition:scale={{ duration: 200, start: 0.95 }}
  >
    <!-- Header -->
    <div class="p-6 border-b border-zinc-800 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <div
          class="h-10 w-10 rounded-lg bg-amber-500/20 flex items-center justify-center"
        >
          <BookOpen class="w-5 h-5 text-amber-500" />
        </div>
        <div>
          <input
            type="text"
            bind:value={title}
            class="bg-transparent text-xl font-bold text-white border-none focus:ring-0 p-0 w-full"
            placeholder="Deck Title"
          />
          <p class="text-zinc-500 text-xs uppercase tracking-wider font-bold">
            Editing Deck
          </p>
        </div>
      </div>
      <button
        onclick={onclose}
        class="text-zinc-500 hover:text-white transition-colors"
      >
        <X class="w-6 h-6" />
      </button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto p-6 space-y-4 custom-scrollbar">
      {#if isLoading}
        <div class="flex flex-col items-center justify-center py-20 gap-4">
          <div
            class="w-8 h-8 border-4 border-amber-500/20 border-t-amber-500 rounded-full animate-spin"
          ></div>
          <p class="text-zinc-500 font-medium">Loading cards...</p>
        </div>
      {:else}
        {#each cards as card, i}
          <div
            class="group relative bg-zinc-900/50 border border-zinc-800 rounded-xl p-6 transition-all hover:border-zinc-700"
            transition:fly={{ y: 10, duration: 200 }}
          >
            <div class="grid md:grid-cols-2 gap-6">
              <div class="space-y-2">
                <label
                  for="q-{i}"
                  class="text-[10px] font-black text-zinc-600 uppercase tracking-widest flex items-center justify-between"
                >
                  <span>Question</span>
                  {#if card.page}
                    <span class="text-amber-500/50">P.{card.page}</span>
                  {/if}
                </label>
                <div class="relative">
                  <textarea
                    id="q-{i}"
                    bind:value={card.question}
                    class={cn(
                      "w-full bg-zinc-800/50 border rounded-lg p-3 text-white focus:ring-1 transition-all resize-none min-h-[100px]",
                      showValidation && !card.question.trim()
                        ? "border-red-500 focus:border-red-500 focus:ring-red-500"
                        : "border-zinc-700 focus:border-amber-500 focus:ring-amber-500"
                    )}
                    placeholder="Enter question..."
                  ></textarea>
                  {#if showValidation && !card.question.trim()}
                    <div
                      class="absolute right-3 top-3 text-red-500 flex items-center gap-1 bg-red-500/10 px-2 py-1 rounded border border-red-500/20 animate-in fade-in zoom-in duration-200"
                    >
                      <AlertCircle class="w-3.5 h-3.5" />
                      <span class="text-[10px] font-bold uppercase"
                        >Required</span
                      >
                    </div>
                  {/if}
                </div>
                <div
                  class="p-3 bg-zinc-950/50 rounded-lg border border-zinc-800/50 min-h-[40px]"
                >
                  <p class="text-[10px] text-zinc-600 uppercase mb-1 font-bold">
                    Preview
                  </p>
                  <div class="text-sm text-zinc-300">
                    <Markdown text={card.question} />
                  </div>
                </div>
              </div>
              <div class="space-y-2">
                <label
                  for="a-{i}"
                  class="text-[10px] font-black text-zinc-600 uppercase tracking-widest"
                  >Answer</label
                >
                <div class="relative">
                  <textarea
                    id="a-{i}"
                    bind:value={card.answer}
                    class={cn(
                      "w-full bg-zinc-800/50 border rounded-lg p-3 text-white focus:ring-1 transition-all resize-none min-h-[100px]",
                      showValidation && !card.answer.trim()
                        ? "border-red-500 focus:border-red-500 focus:ring-red-500"
                        : "border-zinc-700 focus:border-amber-500 focus:ring-amber-500"
                    )}
                    placeholder="Enter answer..."
                  ></textarea>
                  {#if showValidation && !card.answer.trim()}
                    <div
                      class="absolute right-3 top-3 text-red-500 flex items-center gap-1 bg-red-500/10 px-2 py-1 rounded border border-red-500/20 animate-in fade-in zoom-in duration-200"
                    >
                      <AlertCircle class="w-3.5 h-3.5" />
                      <span class="text-[10px] font-bold uppercase"
                        >Required</span
                      >
                    </div>
                  {/if}
                </div>
                <div
                  class="p-3 bg-zinc-950/50 rounded-lg border border-zinc-800/50 min-h-[40px]"
                >
                  <p class="text-[10px] text-zinc-600 uppercase mb-1 font-bold">
                    Preview
                  </p>
                  <div class="text-sm text-zinc-300">
                    <Markdown text={card.answer} />
                  </div>
                </div>
              </div>
            </div>
            <button
              onclick={() => removeCard(i)}
              class="absolute -top-2 -right-2 p-2 bg-red-500/10 text-red-500 border border-red-500/20 rounded-lg opacity-0 group-hover:opacity-100 hover:bg-red-500 hover:text-white transition-all"
              title="Remove card"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        {/each}

        <button
          onclick={addCard}
          class="w-full py-4 border-2 border-dashed border-zinc-800 rounded-xl text-zinc-500 hover:text-amber-500 hover:border-amber-500/50 hover:bg-amber-500/5 transition-all flex items-center justify-center gap-2 font-bold"
        >
          <Plus class="w-5 h-5" />
          Add New Card
        </button>
      {/if}
    </div>

    <!-- Footer -->
    <div
      class="p-6 border-t border-zinc-800 bg-zinc-900/50 flex justify-end gap-3"
    >
      <button
        onclick={onclose}
        class="px-6 py-2.5 rounded-xl border border-zinc-700 text-zinc-400 font-bold hover:bg-zinc-800 hover:text-white transition-all"
      >
        Cancel
      </button>
      <button
        onclick={handleSave}
        disabled={isSaving || isLoading}
        class="px-8 py-2.5 rounded-xl bg-amber-500 text-black font-bold hover:bg-amber-400 shadow-[0_0_20px_rgba(245,158,11,0.2)] transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
      >
        {#if isSaving}
          <div
            class="w-4 h-4 border-2 border-black/30 border-t-black rounded-full animate-spin"
          ></div>
          Saving...
        {:else}
          <Save class="w-4 h-4" />
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
    background: #27272a;
    border-radius: 10px;
  }
  .custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #3f3f46;
  }
</style>
