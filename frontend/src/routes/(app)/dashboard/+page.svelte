<script lang="ts">
  import { onMount } from "svelte";
  import { page } from "$app/state";
  import { authFetch } from "$lib/api";
  import {
    user,
    fetchUser,
    guestUsage,
    fetchGuestUsage,
  } from "$lib/stores/user.svelte";
  import FileDropzone from "$lib/components/upload/FileDropzone.svelte";
  import FileItemCard from "$lib/components/upload/FileItemCard.svelte";
  import { fade, fly } from "svelte/transition";
  import { flip } from "svelte/animate";
  import {
    BookOpen,
    CircleCheck,
    RefreshCw,
    Plus,
    Save,
    CircleAlert,
  } from "@lucide/svelte";
  import ProgressBar from "$lib/components/ui/ProgressBar.svelte";
  import LoginModal from "$lib/components/auth/LoginModal.svelte";
  import DeckList from "$lib/components/deck/DeckList.svelte";
  import Toast from "$lib/components/ui/Toast.svelte";
  import ProUpgradeModal from "$lib/components/ui/ProUpgradeModal.svelte";
  import SaveDeckModal from "$lib/components/deck/SaveDeckModal.svelte";
  import EditDeckModal from "$lib/components/deck/EditDeckModal.svelte";
  import ConfirmModal from "$lib/components/ui/ConfirmModal.svelte";
  import { cn } from "$lib/utils";

  interface Flashcard {
    question: string;
    answer: string;
  }

  interface Deck {
    id: number;
    title: string;
    cardCount: number;
    lastStudiedAt?: string;
    createdAt: string;
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
  let showSaveModal = $state(false);
  let showProModal = $state(false);
  let proModalType = $state<"daily_limit">("daily_limit");
  let recentDecks = $state<Deck[]>([]);
  let pollInterval: ReturnType<typeof setInterval> | null = null;
  let errorMessage = $state<string | null>(null);
  let jobMetadata = $state<Record<string, any>>({});
  let sourceFileName = $state<string | null>(null);

  // Edit/Delete State
  let selectedDeck = $state<any | null>(null);
  let showEditModal = $state(false);
  let deckToDelete = $state<number | null>(null);

  // Validation Logic
  const MAX_SIZE_MB = 20;
  const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024;

  let totalSize = $derived(files.reduce((acc, f) => acc + f.size, 0));
  let isTotalSizeExceeded = $derived(totalSize > MAX_SIZE_BYTES);
  let hasOversizedFiles = $derived(files.some((f) => f.size > MAX_SIZE_BYTES));
  let isGenerationBlocked = $derived(
    isTotalSizeExceeded || hasOversizedFiles || files.length === 0
  );

  let showTotalSizeToast = $state(false);

  // Show toast when total size is exceeded
  $effect(() => {
    if (isTotalSizeExceeded) {
      showTotalSizeToast = true;
    } else {
      showTotalSizeToast = false;
    }
  });

  let mode = $derived(page.url.searchParams.get("mode"));
  let hasDecks = $derived(recentDecks.length > 0);
  let showUpload = $derived(mode === "upload" || !hasDecks);

  let dailyUploadCount = $derived(
    user.current?.dailyUploadCount ?? guestUsage.count
  );
  let maxLimit = $derived(user.current?.tier === "PRO_USER" ? 100 : 3);
  let badgeColor = $derived(
    dailyUploadCount >= maxLimit
      ? "bg-red-500/20 text-red-400 border-red-500/50"
      : dailyUploadCount >= maxLimit - 1
        ? "bg-amber-500/20 text-amber-400 border-amber-500/50"
        : "bg-slate-800/50 text-slate-400 border-slate-700"
  );

  // Fetch recent decks on mount
  onMount(async () => {
    if (user.current) {
      await fetchRecentDecks();
    }

    // Check for pending guest cards after login redirect (IP-based persistence)
    try {
      const response = await authFetch("/api/v1/pdf/recent");
      if (response.ok) {
        const data = await response.json();
        if (data.status === "COMPLETED" && data.results) {
          generatedCards = data.results;
          showResults = true;
          if (data.metadata) {
            jobMetadata = data.metadata;
          }
        }
      }
    } catch (e) {
      if (import.meta.env.DEV) {
        console.error("Failed to fetch recent job", e);
      }
    }
  });

  async function fetchRecentDecks() {
    try {
      const response = await authFetch("/api/v1/decks?size=3");
      if (response.ok) {
        const data = await response.json();
        recentDecks = data.content;
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to fetch recent decks:", error);
      }
    }
  }

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
    if (files.length > 0 && !sourceFileName) {
      sourceFileName = files[0].name.replace(".pdf", "");
    }
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
      // Check daily limit
      if (dailyUploadCount >= maxLimit) {
        proModalType = "daily_limit";
        showProModal = true;
        isGenerating = false;
        return;
      }

      const formData = new FormData();
      formData.append("file", files[0]);

      const response = await authFetch("/api/v1/pdf/generate-cards", {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        const data = await response.json();
        jobId = data.jobId;

        pollInterval = setInterval(async () => {
          if (!jobId) return;

          try {
            const statusResponse = await authFetch(`/api/v1/pdf/jobs/${jobId}`);
            if (statusResponse.ok) {
              const statusData = await statusResponse.json();
              jobStatus = statusData.status;
              jobProgress = statusData.progress;
              if (statusData.metadata) {
                jobMetadata = statusData.metadata;
              }

              if (jobStatus === "COMPLETED") {
                if (pollInterval) clearInterval(pollInterval);
                pollInterval = null;
                isGenerating = false;
                generatedCards = statusData.results || [];

                if (generatedCards.length > 0) {
                  showResults = true;
                } else {
                  isEmptyState = true;
                }

                files = [];
                // Refresh user data to get updated upload count
                if (user.current) {
                  fetchUser();
                } else {
                  fetchGuestUsage();
                }
              } else if (jobStatus === "FAILED") {
                if (pollInterval) clearInterval(pollInterval);
                pollInterval = null;
                isGenerating = false;
                errorMessage = "Generation failed. Please try again.";
              }
            }
          } catch (e) {
            if (pollInterval) clearInterval(pollInterval);
            pollInterval = null;
            isGenerating = false;
          }
        }, 1000);
      } else {
        const errorData = await response.json();
        errorMessage = errorData.error || "Failed to start generation.";

        if (
          errorMessage &&
          errorMessage.includes("Daily upload limit reached")
        ) {
          proModalType = "daily_limit";
          showProModal = true;
          errorMessage = null;
        }
        isGenerating = false;
      }
    } catch (error) {
      isGenerating = false;
      errorMessage = "An error occurred.";
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
    jobMetadata = {};
    sourceFileName = null;
  }

  async function saveToLibrary(customTitle: string) {
    if (!user.current || generatedCards.length === 0) return;

    try {
      const response = await authFetch("/api/v1/decks", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          title: customTitle,
          cards: generatedCards,
        }),
      });

      if (response.ok) {
        await fetchRecentDecks();
        showSaveModal = false;
        resetView();
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to save deck:", error);
      }
    }
  }

  async function handleEditCards(deck: any) {
    try {
      const response = await authFetch(`/api/v1/decks/${deck.id}/cards`);
      if (response.ok) {
        const cards = await response.json();
        selectedDeck = { ...deck, cards };
        showEditModal = true;
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to fetch deck cards:", error);
      }
    }
  }

  function handleSaveCards(updatedTitle: string, updatedCards: any[]) {
    if (selectedDeck) {
      const deckIndex = recentDecks.findIndex((d) => d.id === selectedDeck.id);
      if (deckIndex !== -1) {
        recentDecks[deckIndex].title = updatedTitle;
        recentDecks[deckIndex].cardCount = updatedCards.length;
      }
    }
  }

  async function confirmDelete() {
    if (!deckToDelete) return;
    const id = deckToDelete;
    deckToDelete = null;

    try {
      const response = await authFetch(`/api/v1/decks/${id}`, {
        method: "DELETE",
      });
      if (response.ok) {
        recentDecks = recentDecks.filter((d) => d.id !== id);
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to delete deck:", error);
      }
    }
  }
</script>

<div class="space-y-8">
  <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
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

    <div
      class={cn(
        "flex items-center gap-3 px-4 py-2 rounded-full border text-sm font-medium transition-all duration-300",
        badgeColor
      )}
    >
      <div class="w-2 h-2 rounded-full bg-current animate-pulse"></div>
      <span>{dailyUploadCount} / {maxLimit} Daily Uploads</span>
    </div>
  </div>

  {#if showUpload}
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
                <div class="flex items-center gap-2">
                  <div class="flex items-center gap-2">
                    <h2 class="text-xl font-bold text-white">
                      Generation Complete!
                    </h2>
                    <div
                      class="flex items-center gap-2 text-amber-500/80 text-xs mt-1 font-medium"
                    >
                      <CircleAlert class="w-3.5 h-3.5" />
                      <span
                        >AI can make mistakes. You can refine these in the
                        library.</span
                      >
                    </div>
                  </div>
                  <div
                    class="px-2 py-0.5 rounded-full {badgeColor} text-[10px] font-bold uppercase tracking-wider border transition-colors duration-300"
                  >
                    Daily Limit {dailyUploadCount} / {maxLimit}
                  </div>
                </div>
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
              {#if !user.current}
                <button
                  onclick={() => (showLoginModal = true)}
                  class="group flex items-center gap-2 px-5 py-2 rounded-lg font-bold transition-all duration-300 border border-[#FFD700] text-white shadow-[0_0_10px_rgba(255,215,0,0.1)] hover:bg-[#FFD700] hover:text-black hover:shadow-[0_0_20px_rgba(255,215,0,0.6)]"
                >
                  <Save class="w-4 h-4" />
                  <span>Sign in to Save</span>
                </button>
              {:else}
                <button
                  onclick={() => (showSaveModal = true)}
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
          <button
            onclick={resetView}
            class="mt-6 px-6 py-2 bg-[#FFD700] hover:bg-[#FDB931] text-black font-bold rounded-lg shadow-[0_0_15px_rgba(255,215,0,0.2)] transition-all"
          >
            Okay, I will study... 😔
          </button>
        </div>
      {:else}
        <FileDropzone isGuest={!user.current} onFileSelect={handleFileSelect} />

        {#if files.length > 0}
          <div class="space-y-4" transition:fade>
            <div class="flex items-center justify-between">
              <h2 class="text-xl font-semibold text-white">
                Upload Queue ({files.length})
              </h2>
              <div class="flex items-center gap-4">
                {#if !user.current || user.current?.tier === "FREE_USER"}
                  <div class="text-right">
                    <p
                      class="text-zinc-500 text-[10px] font-bold uppercase tracking-wider"
                    >
                      Daily Usage
                    </p>
                    <p class="text-zinc-300 text-sm font-medium">
                      {dailyUploadCount} / {maxLimit} Uploads
                    </p>
                  </div>
                {/if}
                <button
                  onclick={handleGenerate}
                  disabled={isGenerating || isGenerationBlocked}
                  class="px-6 py-2 bg-[#FFD700] hover:bg-[#FDB931] text-black font-bold rounded-lg shadow-[0_0_15px_rgba(255,215,0,0.2)] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isGenerating ? "Generating..." : "Generate All Decks"}
                </button>
              </div>
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
                    <FileItemCard
                      {file}
                      isOversized={file.size > MAX_SIZE_BYTES}
                      onRemove={() => removeFile(i)}
                    />
                  </div>
                {/each}
              </div>
            {/if}
          </div>
        {/if}
      {/if}
    </section>
  {:else}
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

      <DeckList
        decks={recentDecks}
        onDelete={(id) => (deckToDelete = id)}
        onEditCards={handleEditCards}
      />
    </section>
  {/if}
</div>

{#if showLoginModal}
  <LoginModal onclose={() => (showLoginModal = false)} />
{/if}

{#if showTotalSizeToast}
  <Toast
    message="The total file size exceeds the 20MB limit. Please remove some files."
    onclose={() => (showTotalSizeToast = false)}
  />
{/if}

{#if showProModal}
  <ProUpgradeModal
    type={proModalType}
    mode={user.current ? "free" : "guest"}
    onclose={() => (showProModal = false)}
  />
{/if}

{#if showSaveModal}
  <SaveDeckModal
    initialTitle={jobMetadata.title || sourceFileName || ""}
    onclose={() => (showSaveModal = false)}
    onsave={saveToLibrary}
  />
{/if}

{#if showEditModal && selectedDeck}
  <EditDeckModal
    deck={selectedDeck}
    onclose={() => (showEditModal = false)}
    onsave={handleSaveCards}
  />
{/if}

{#if deckToDelete !== null}
  <ConfirmModal
    title="Delete Deck"
    message="Are you sure you want to delete this deck? All associated flashcards will be permanently removed."
    confirmText="Delete Deck"
    onconfirm={confirmDelete}
    oncancel={() => (deckToDelete = null)}
  />
{/if}
