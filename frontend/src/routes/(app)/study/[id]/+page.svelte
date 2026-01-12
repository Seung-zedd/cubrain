<script lang="ts">
  import { onMount } from "svelte";
  import { page } from "$app/state";
  import { beforeNavigate } from "$app/navigation";
  import { authFetch } from "$lib/api";
  import { fade, fly } from "svelte/transition";
  import ChevronLeft from "@lucide/svelte/icons/chevron-left";
  import ChevronRight from "@lucide/svelte/icons/chevron-right";
  import RotateCcw from "@lucide/svelte/icons/rotate-ccw";
  import CheckCircle2 from "@lucide/svelte/icons/check-circle-2";
  import BookOpen from "@lucide/svelte/icons/book-open";
  import { cn } from "$lib/utils";
  import Markdown from "$lib/components/ui/Markdown.svelte";
  import { uiState } from "$lib/stores/ui.svelte";
  import StudySession from "$lib/components/study/StudySession.svelte";
  import { goto } from "$app/navigation";

  interface Flashcard {
    id: number;
    question: string;
    answer: string;
    page?: number;
  }

  let deckId = $derived(page.params.id);
  let cards = $state<Flashcard[]>([]);
  let currentIndex = $state(0);
  let isFlipped = $state(false);
  let isLoading = $state(true);
  let isComplete = $state(false);

  let currentCard = $derived(cards[currentIndex]);
  let progress = $derived(
    cards.length > 0 ? Math.round(((currentIndex + 1) / cards.length) * 100) : 0
  );

  async function updateProgress() {
    if (cards.length === 0) return;

    // If complete, progress is 100. Otherwise calculate based on current index.
    const currentProgress = isComplete
      ? 100
      : Math.round(((currentIndex + 1) / cards.length) * 100);

    try {
      await authFetch(`/api/v1/decks/${deckId}/progress`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ progress: currentProgress }),
      });
    } catch (e) {
      if (import.meta.env.DEV) {
        console.error("Failed to update progress", e);
      }
    }
  }

  beforeNavigate(() => {
    updateProgress();
  });

  onMount(async () => {
    // Ensure study mode is active when entering this page
    uiState.setStudyMode(true);

    try {
      const response = await authFetch(`/api/v1/decks/${deckId}/cards`);
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
  });

  function nextCard() {
    if (currentIndex < cards.length - 1) {
      isFlipped = false;
      setTimeout(() => {
        currentIndex++;
      }, 150);
    } else {
      isComplete = true;
      updateProgress();
    }
  }

  function prevCard() {
    if (currentIndex > 0) {
      isFlipped = false;
      setTimeout(() => {
        currentIndex--;
      }, 150);
    }
  }

  function toggleFlip() {
    isFlipped = !isFlipped;
  }

  function restart() {
    currentIndex = 0;
    isFlipped = false;
    isComplete = false;
  }

  function handleKeydown(e: KeyboardEvent) {
    if (isComplete || isLoading) return;

    if (e.code === "Space") {
      e.preventDefault();
      toggleFlip();
    } else if (e.key === "ArrowRight") {
      nextCard();
    } else if (e.key === "ArrowLeft") {
      prevCard();
    }
  }

  function handleExit() {
    uiState.setStudyMode(false);
    goto("/library");
  }

  onMount(() => {
    window.addEventListener("keydown", handleKeydown);
    return () => window.removeEventListener("keydown", handleKeydown);
  });
</script>

<StudySession onExit={handleExit}>
  <div class="space-y-8">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div class="text-zinc-500 font-medium">
        {#if !isLoading && cards.length > 0}
          {currentIndex + 1} / {cards.length}
        {/if}
      </div>
    </div>

    {#if isLoading}
      <div
        class="h-[400px] bg-zinc-900/50 rounded-2xl animate-pulse border border-zinc-800 flex items-center justify-center"
      >
        <BookOpen class="w-12 h-12 text-zinc-800" />
      </div>
    {:else if cards.length === 0}
      <div class="text-center py-20 space-y-4">
        <h2 class="text-2xl font-bold text-white">No cards in this deck</h2>
        <p class="text-zinc-500">This deck seems to be empty.</p>
        <button
          onclick={handleExit}
          class="text-amber-500 font-bold hover:underline"
          >Return to Library</button
        >
      </div>
    {:else if isComplete}
      <div class="text-center py-20 space-y-8" in:fly={{ y: 20 }}>
        <div
          class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-500/20 border border-green-500/50"
        >
          <CheckCircle2 class="w-10 h-10 text-green-500" />
        </div>
        <div class="space-y-2">
          <h2 class="text-3xl font-bold text-white">Deck Mastered!</h2>
          <p class="text-zinc-400">
            You've gone through all the cards in this deck.
          </p>
        </div>
        <div class="flex items-center justify-center gap-4">
          <button
            onclick={restart}
            class="flex items-center gap-2 px-6 py-3 bg-zinc-800 hover:bg-zinc-700 text-white font-bold rounded-xl transition-all"
          >
            <RotateCcw class="w-5 h-5" />
            Study Again
          </button>
          <button
            onclick={handleExit}
            class="px-6 py-3 bg-amber-500 hover:bg-amber-400 text-black font-bold rounded-xl transition-all"
          >
            Finish Session
          </button>
        </div>
      </div>
    {:else}
      <!-- Progress Bar -->
      <div
        class="h-2 w-full bg-zinc-900 rounded-full overflow-hidden border border-zinc-800"
      >
        <div
          class="h-full bg-amber-500 transition-all duration-500 ease-out"
          style="width: {progress}%"
        ></div>
      </div>

      <!-- Flashcard -->
      <button
        type="button"
        class="relative h-[400px] w-full perspective-1000 cursor-pointer group block text-left"
        onclick={toggleFlip}
      >
        <div
          class={cn(
            "relative w-full h-full transition-all duration-500 preserve-3d",
            isFlipped ? "rotate-y-180" : ""
          )}
        >
          <!-- Front (Question) -->
          <div
            class="absolute inset-0 w-full h-full backface-hidden bg-zinc-900 border-2 border-zinc-800 rounded-2xl p-8 flex flex-col items-center justify-center text-center shadow-2xl group-hover:border-amber-500/30 transition-colors"
          >
            <span
              class="absolute top-6 left-6 text-xs font-bold uppercase tracking-widest text-amber-500/50"
              >Question</span
            >
            <p
              class="text-2xl md:text-3xl font-medium text-white leading-relaxed"
            >
              <Markdown text={currentCard.question} />
            </p>
            <div
              class="absolute bottom-16 flex items-center gap-4 text-xs text-zinc-500 opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <div class="flex items-center gap-1.5">
                <span
                  class="px-1.5 py-0.5 bg-zinc-800 rounded border border-zinc-700 font-mono text-[10px] text-zinc-400"
                  >Space</span
                >
                <span>Flip</span>
              </div>
              <div class="flex items-center gap-1.5">
                <div class="flex gap-0.5">
                  <span
                    class="px-1.5 py-0.5 bg-zinc-800 rounded border border-zinc-700 font-mono text-[10px] text-zinc-400"
                    >←</span
                  >
                  <span
                    class="px-1.5 py-0.5 bg-zinc-800 rounded border border-zinc-700 font-mono text-[10px] text-zinc-400"
                    >→</span
                  >
                </div>
                <span>Nav</span>
              </div>
            </div>
            <p
              class="absolute bottom-8 text-zinc-500 text-sm font-medium animate-pulse"
            >
              Click to flip
            </p>
          </div>

          <!-- Back (Answer) -->
          <div
            class="absolute inset-0 w-full h-full backface-hidden bg-zinc-800 border-2 border-amber-500/50 rounded-2xl p-8 flex flex-col items-center justify-center text-center shadow-2xl rotate-y-180"
          >
            <span
              class="absolute top-6 left-6 text-xs font-bold uppercase tracking-widest text-amber-500"
              >Answer</span
            >
            <p class="text-xl md:text-2xl text-zinc-100 leading-relaxed">
              <Markdown text={currentCard.answer} />
            </p>
            <div
              class="absolute bottom-16 flex items-center gap-4 text-xs text-zinc-500 opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <div class="flex items-center gap-1.5">
                <span
                  class="px-1.5 py-0.5 bg-zinc-800 rounded border border-zinc-700 font-mono text-[10px] text-zinc-400"
                  >Space</span
                >
                <span>Flip</span>
              </div>
              <div class="flex items-center gap-1.5">
                <div class="flex gap-0.5">
                  <span
                    class="px-1.5 py-0.5 bg-zinc-800 rounded border border-zinc-700 font-mono text-[10px] text-zinc-400"
                    >←</span
                  >
                  <span
                    class="px-1.5 py-0.5 bg-zinc-800 rounded border border-zinc-700 font-mono text-[10px] text-zinc-400"
                    >→</span
                  >
                </div>
                <span>Nav</span>
              </div>
            </div>
            <p class="absolute bottom-8 text-zinc-400 text-sm font-medium">
              Click to flip back
            </p>
          </div>
        </div>
      </button>

      <!-- Controls -->
      <div class="flex items-center justify-between gap-4">
        <button
          onclick={prevCard}
          disabled={currentIndex === 0}
          class="flex-1 flex items-center justify-center gap-2 px-6 py-4 bg-zinc-900 hover:bg-zinc-800 text-white font-bold rounded-xl border border-zinc-800 disabled:opacity-30 disabled:cursor-not-allowed transition-all"
        >
          <ChevronLeft class="w-6 h-6" />
          Previous
        </button>
        <button
          onclick={nextCard}
          class="flex-1 flex items-center justify-center gap-2 px-6 py-4 bg-amber-500 hover:bg-amber-400 text-black font-bold rounded-xl shadow-[0_0_20px_rgba(245,158,11,0.2)] transition-all"
        >
          {currentIndex === cards.length - 1 ? "Finish" : "Next"}
          <ChevronRight class="w-6 h-6" />
        </button>
      </div>
    {/if}
  </div>
</StudySession>

<style>
  .perspective-1000 {
    perspective: 1000px;
  }
  .preserve-3d {
    transform-style: preserve-3d;
  }
  .backface-hidden {
    backface-visibility: hidden;
  }
  .rotate-y-180 {
    transform: rotateY(180deg);
  }
</style>
