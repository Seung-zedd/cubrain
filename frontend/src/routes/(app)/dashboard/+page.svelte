<script lang="ts">
  import { page } from "$app/stores";
  import FileDropzone from "$lib/components/upload/FileDropzone.svelte";
  import FileItemCard from "$lib/components/upload/FileItemCard.svelte";
  import { fade, fly } from "svelte/transition";
  import { flip } from "svelte/animate";
  import { BookOpen, CircleCheck, RefreshCw, Plus } from "@lucide/svelte";
  import ProgressBar from "$lib/components/ui/ProgressBar.svelte";
  import { API_BASE_URL } from "$lib/config";

  interface Flashcard {
    question: string;
    answer: string;
  }

  interface Deck {
    id: string;
    title: string;
    lastStudied: string;
    cardCount: number;
  }

  let files = $state<File[]>([]);
  let isGenerating = $state(false);
  let isEmptyState = $state(false);
  let jobId = $state<string | null>(null);
  let jobStatus = $state("PROCESSING");
  let jobProgress = $state(0);
  let generatedCards = $state<Flashcard[]>([]);
  let showResults = $state(false);
  // Mock persistence for demo purposes
  let recentDecks = $state<Deck[]>([
    {
      id: "1",
      title: "Introduction to AI - Part 1",
      lastStudied: "2 hours ago",
      cardCount: 15,
    },
    {
      id: "2",
      title: "Introduction to AI - Part 2",
      lastStudied: "2 hours ago",
      cardCount: 12,
    },
    {
      id: "3",
      title: "Introduction to AI - Part 3",
      lastStudied: "2 hours ago",
      cardCount: 20,
    },
  ]);
  let pollInterval: ReturnType<typeof setInterval> | null = null;

  let mode = $derived($page.url.searchParams.get("mode"));
  let hasDecks = $derived(recentDecks.length > 0);
  let showUpload = $derived(mode === "upload" || !hasDecks);

  // Cleanup polling interval when component unmounts
  $effect(() => {
    return () => {
      if (pollInterval) {
        clearInterval(pollInterval);
        pollInterval = null;
      }
    };
  });

  function handleFileSelect(newFiles: File[]) {
    // Auth mode: Append new files to existing ones
    // Filter out duplicates based on name and size
    const uniqueNewFiles = newFiles.filter(
      (newFile) =>
        !files.some(
          (existing) =>
            existing.name === newFile.name &&
            existing.size === newFile.size &&
            existing.lastModified === newFile.lastModified
        )
    );
    files = [...files, ...uniqueNewFiles];
  }

  function removeFile(index: number) {
    files = files.filter((_, i) => i !== index);
  }

  async function handleGenerate() {
    if (files.length === 0) return;

    isGenerating = true;
    isEmptyState = false;
    showResults = false;
    generatedCards = [];
    jobId = null;
    jobProgress = 0;
    jobStatus = "PROCESSING";

    try {
      const formData = new FormData();
      formData.append("file", files[0]);

      // 1. Start Async Job
      const startResponse = await fetch(
        `${API_BASE_URL}/api/v1/pdf/generate-cards`,
        {
          method: "POST",
          body: formData,
        }
      );

      if (startResponse.ok) {
        const startData = await startResponse.json();
        jobId = startData.jobId;

        // 2. Poll for Status
        pollInterval = setInterval(async () => {
          if (!jobId) return;

          try {
            const statusResponse = await fetch(
              `${API_BASE_URL}/api/v1/pdf/jobs/${startData.jobId}`
            );
            if (statusResponse.ok) {
              const statusData = await statusResponse.json();
              jobStatus = statusData.status;
              jobProgress = statusData.progress;

              if (jobStatus === "COMPLETED") {
                if (pollInterval) clearInterval(pollInterval);
                pollInterval = null;
                isGenerating = false;

                if (import.meta.env.LOCAL) {
                  console.log("Job Completed:", statusData.results);
                }

                let newCards: Flashcard[] = [];
                if (
                  Array.isArray(statusData.results) &&
                  statusData.results.length > 0
                ) {
                  newCards = statusData.results;
                } else if (
                  statusData.results &&
                  !Array.isArray(statusData.results)
                ) {
                  newCards = [statusData.results];
                }

                if (newCards.length > 0) {
                  generatedCards = newCards;
                  showResults = true;
                  // Add to recent decks (Mock)
                  recentDecks = [
                    {
                      id: Date.now().toString(),
                      title: files[0].name.replace(".pdf", ""),
                      lastStudied: "Just now",
                      cardCount: newCards.length,
                    },
                    ...recentDecks,
                  ];
                } else {
                  isEmptyState = true;
                }
                files = [];
              } else if (jobStatus === "FAILED") {
                if (pollInterval) clearInterval(pollInterval);
                pollInterval = null;
                isGenerating = false;
                if (import.meta.env.LOCAL) {
                  console.error("Job Failed");
                }
              }
            }
          } catch (e) {
            if (import.meta.env.LOCAL) {
              console.error("Polling error", e);
            }
            if (pollInterval) clearInterval(pollInterval);
            pollInterval = null;
            isGenerating = false;
          }
        }, 1000);
      } else {
        if (import.meta.env.LOCAL) {
          console.error("Failed to start generation job");
        }
        isGenerating = false;
      }
    } catch (error) {
      if (import.meta.env.LOCAL) {
        console.error("Error:", error);
      }
      isGenerating = false;
    }
  }

  function formatQuestion(text: string) {
    return text.replace(/\{\{c\d+::.*?\}\}/g, "______");
  }

  function resetView() {
    isEmptyState = false;
    showResults = false;
    generatedCards = [];
    files = [];
  }
</script>

<div class="space-y-8">
  <div>
    <h1 class="text-3xl font-bold text-white tracking-tight">Dashboard</h1>
    <p class="text-zinc-400 mt-2">
      {#if showUpload}
        Upload your PDFs to generate new flashcard decks.
      {:else}
        Pick up where you left off.
      {/if}
    </p>
  </div>

  {#if showUpload}
    <!-- Upload / Results Section -->
    <section class="space-y-6">
      {#if showResults}
        <div
          class="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div
                class="h-10 w-10 rounded-full bg-green-500/20 flex items-center justify-center"
              >
                <CircleCheck class="w-6 h-6 text-green-500" />
              </div>
              <div>
                <h2 class="text-xl font-bold text-white">
                  Generation Complete!
                </h2>
                <p class="text-zinc-400 text-sm">
                  Created {generatedCards.length} flashcards
                </p>
              </div>
            </div>
            <div class="flex gap-2">
              <button
                onclick={() => (window.location.href = "/dashboard")}
                class="flex items-center gap-2 px-4 py-2 bg-zinc-800 hover:bg-zinc-700 text-white rounded-lg transition-colors"
              >
                View Decks
              </button>
              <button
                onclick={resetView}
                class="flex items-center gap-2 px-4 py-2 bg-[#FFD700] hover:bg-[#FDB931] text-black font-bold rounded-lg transition-colors"
              >
                <RefreshCw class="w-4 h-4" />
                Start Over
              </button>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {#each generatedCards as card, i}
              <div
                class="bg-zinc-900/50 border border-zinc-800 p-6 rounded-xl hover:border-[#FFD700]/30 transition-all"
                in:fly={{ y: 20, duration: 300, delay: i * 50 }}
              >
                <div class="mb-4">
                  <span
                    class="text-xs font-bold text-[#FFD700] uppercase tracking-wider"
                    >Question</span
                  >
                  <p class="text-zinc-100 mt-1">
                    {formatQuestion(card.question)}
                  </p>
                </div>
                <div>
                  <span
                    class="text-xs font-bold text-zinc-500 uppercase tracking-wider"
                    >Answer</span
                  >
                  <p class="text-zinc-300 mt-1">{card.answer}</p>
                </div>
              </div>
            {/each}
          </div>
        </div>
      {:else if isEmptyState}
        <div
          class="flex flex-col items-center justify-center py-12 text-center space-y-4 animate-in fade-in zoom-in duration-300"
        >
          <BookOpen class="w-16 h-16 text-zinc-500 mb-4" />
          <h2 class="text-xl font-bold text-white">No Study Traces Found</h2>
          <p class="text-zinc-400 mt-2">
            We don't provide flashcards without your annotation.<br />
            <span class="text-[#FFD700] font-bold">👩‍🏫📢 Go study and come back!</span
            >
          </p>
          <p class="text-sm text-zinc-500">
            Please highlight or underline key concepts in your PDF and
            re-upload.
          </p>
          <button
            onclick={resetView}
            class="mt-6 px-6 py-2 bg-[#FFD700] hover:bg-[#FDB931] text-black font-bold rounded-lg shadow-[0_0_15px_rgba(255,215,0,0.2)] transition-all"
          >
            Okay, I will study... 😔
          </button>
        </div>
      {:else}
        <FileDropzone isGuest={false} onFileSelect={handleFileSelect} />

        {#if files.length > 0}
          <div class="space-y-4" transition:fade>
            <div class="flex items-center justify-between">
              <h2 class="text-xl font-semibold text-white">
                Upload Queue ({files.length})
              </h2>
              <button
                onclick={handleGenerate}
                disabled={isGenerating}
                class="px-6 py-2 bg-[#FFD700] hover:bg-[#FDB931] text-black font-bold rounded-lg shadow-[0_0_15px_rgba(255,215,0,0.2)] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {#if isGenerating}
                  Generating...
                {:else}
                  Generate All Decks
                {/if}
              </button>
            </div>

            {#if isGenerating}
              <div class="flex flex-col items-center justify-center py-12">
                <div class="w-full max-w-md">
                  <ProgressBar progress={jobProgress} status={jobStatus} />
                </div>
                <p class="text-zinc-400 mt-4">Analyzing your document...</p>
              </div>
            {:else}
              <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {#each files as file, i (file.name + file.size)}
                  <div animate:flip={{ duration: 300 }}>
                    <FileItemCard {file} onRemove={() => removeFile(i)} />
                  </div>
                {/each}
              </div>
            {/if}
          </div>
        {/if}
      {/if}
    </section>
  {:else}
    <!-- Recent Decks Section -->
    <section class="space-y-6 animate-in fade-in slide-in-from-bottom-4">
      <div class="flex items-center justify-between">
        <h2 class="text-xl font-semibold text-white">Recent Decks</h2>
        <a
          href="/dashboard?mode=upload"
          class="flex items-center gap-2 px-4 py-2 bg-zinc-800 hover:bg-zinc-700 text-white rounded-lg transition-colors text-sm font-medium"
        >
          <Plus class="w-4 h-4" />
          New Deck
        </a>
      </div>

      <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {#each recentDecks as deck}
          <div
            class="group rounded-xl border border-zinc-800 bg-zinc-900/50 p-6 shadow-sm hover:border-[#FFD700]/30 transition-all hover:bg-zinc-900/80 cursor-pointer"
          >
            <div class="flex justify-between items-start mb-4">
              <div
                class="h-10 w-10 rounded-lg bg-[#FFD700]/10 flex items-center justify-center group-hover:bg-[#FFD700]/20 transition-colors"
              >
                <span class="text-xl">📚</span>
              </div>
              <span
                class="text-xs font-medium px-2 py-1 rounded-full bg-zinc-800 text-zinc-400"
              >
                {deck.cardCount} cards
              </span>
            </div>
            <h3
              class="text-lg font-semibold text-zinc-100 group-hover:text-[#FFD700] transition-colors"
            >
              {deck.title}
            </h3>
            <p class="text-sm text-zinc-400 mt-2">
              Last studied {deck.lastStudied}
            </p>
          </div>
        {/each}
      </div>
    </section>
  {/if}
</div>
