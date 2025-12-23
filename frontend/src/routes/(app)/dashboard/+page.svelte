<script lang="ts">
  import { page } from "$app/state";
  import FileDropzone from "$lib/components/upload/FileDropzone.svelte";
  import FileItemCard from "$lib/components/upload/FileItemCard.svelte";
  import { fade, fly } from "svelte/transition";
  import { flip } from "svelte/animate";
  import { BookOpen, CircleCheck, RefreshCw, Plus, Save } from "@lucide/svelte";
  import ProgressBar from "$lib/components/ui/ProgressBar.svelte";
  import { API_BASE_URL } from "$lib/config";
  import LoginModal from "$lib/components/auth/LoginModal.svelte";
  import DeckList from "$lib/components/deck/DeckList.svelte";
  import { user } from "$lib/stores/user";

  interface Flashcard {
    question: string;
    answer: string;
  }

  interface Deck {
    id: string;
    title: string;
    lastStudied: string;
    cardCount: number;
    progress?: number;
  }

  let files = $state<File[]>([]);
  let isGenerating = $state(false);
  let isEmptyState = $state(false);
  let jobId = $state<string | null>(null);
  let jobStatus = $state("PROCESSING");
  let jobProgress = $state(0);
  let generatedCards = $state<Flashcard[]>([]);
  let showResults = $state(false);
  let showLoginModal = $state(false);
  let errorMessage = $state<string | null>(null);
  // Mock persistence for demo purposes
  let recentDecks = $state<Deck[]>([
    {
      id: "1",
      title: "Introduction to AI - Part 1",
      lastStudied: "2 hours ago",
      cardCount: 15,
      progress: 45,
    },
    {
      id: "2",
      title: "Introduction to AI - Part 2",
      lastStudied: "2 hours ago",
      cardCount: 12,
      progress: 10,
    },
    {
      id: "3",
      title: "Introduction to AI - Part 3",
      lastStudied: "2 hours ago",
      cardCount: 20,
      progress: 80,
    },
  ]);
  let pollInterval: ReturnType<typeof setInterval> | null = null;

  let mode = $derived(page.url.searchParams.get("mode"));
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
    errorMessage = null;

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

                if (import.meta.env.DEV) {
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
                if (import.meta.env.DEV) {
                  console.error("Job Failed");
                }
              }
            }
          } catch (e) {
            if (import.meta.env.DEV) {
              console.error("Polling error", e);
            }
            if (pollInterval) clearInterval(pollInterval);
            pollInterval = null;
            isGenerating = false;
          }
        }, 1000);
      } else {
        const errorData = await startResponse.json();
        errorMessage = errorData.error || "Failed to start generation job";
        if (import.meta.env.DEV) {
          console.error("Failed to start generation job:", errorMessage);
        }
        isGenerating = false;
      }
    } catch (error) {
      if (import.meta.env.DEV) {
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
    errorMessage = null;
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
      {#if errorMessage}
        <div
          class="p-4 rounded-lg bg-red-500/10 border border-red-500/20 text-red-500 text-sm animate-in fade-in slide-in-from-top-2"
        >
          {errorMessage}
        </div>
      {/if}
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
            <div class="flex items-center gap-3">
              <button
                onclick={resetView}
                class="flex items-center gap-2 px-4 py-2 text-gray-500 hover:text-white font-medium transition-colors"
              >
                <RefreshCw class="w-4 h-4" />
                Start Over
              </button>
              {#if !$user}
                <button
                  onclick={() => (showLoginModal = true)}
                  class="group flex items-center gap-2 px-5 py-2 rounded-lg font-bold transition-all duration-300 border border-[#FFD700] text-white shadow-[0_0_10px_rgba(255,215,0,0.1)] hover:bg-[#FFD700] hover:text-black hover:shadow-[0_0_20px_rgba(255,215,0,0.6)]"
                >
                  <Save class="w-4 h-4" />
                  <span>Sign in to Save</span>
                </button>
              {:else}
                <button
                  class="group flex items-center gap-2 px-5 py-2 rounded-lg font-bold transition-all duration-300 bg-amber-500 text-black hover:bg-amber-400"
                >
                  <Save class="w-4 h-4" />
                  <span>Save to Library</span>
                </button>
              {/if}
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {#each generatedCards as card, i}
              <div
                class="rounded-xl p-px bg-linear-to-br from-[#fbbf24]/50 to-[#FFD700]/50 hover:from-[#fbbf24] hover:to-[#FFD700] transition-all duration-300"
                in:fly={{ y: 20, duration: 300, delay: i * 50 }}
              >
                <div
                  class="rounded-xl bg-[#1a1a1a] p-6 flex flex-col gap-6 h-full"
                >
                  <div class="flex flex-col gap-2">
                    <span
                      class="text-amber-500 text-xs font-bold uppercase tracking-wider"
                      >Question</span
                    >
                    <p class="text-white text-lg font-medium leading-relaxed">
                      {formatQuestion(card.question)}
                    </p>
                  </div>
                  <div class="flex flex-col gap-2">
                    <span
                      class="text-gray-500 text-xs font-bold uppercase tracking-wider"
                      >Answer</span
                    >
                    <p class="text-gray-300 text-base leading-relaxed">
                      {card.answer}
                    </p>
                  </div>
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
            <span class="text-[#FFD700] font-bold"
              >👩‍🏫📢 Go study and come back!</span
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

      <DeckList decks={recentDecks} />
    </section>
  {/if}
</div>

{#if showLoginModal}
  <LoginModal onclose={() => (showLoginModal = false)} />
{/if}
