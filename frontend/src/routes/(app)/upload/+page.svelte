<script lang="ts">
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { authFetch } from "$lib/api";
  import { supabase } from "$lib/supabaseClient";
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
  import BookOpen from "@lucide/svelte/icons/book-open";
  import CircleCheck from "@lucide/svelte/icons/circle-check";
  import RefreshCw from "@lucide/svelte/icons/refresh-cw";
  import Save from "@lucide/svelte/icons/save";
  import CircleAlert from "@lucide/svelte/icons/circle-alert";
  import Library from "@lucide/svelte/icons/library";
  import ProgressBar from "$lib/components/ui/ProgressBar.svelte";
  import LoginModal from "$lib/components/auth/LoginModal.svelte";
  import Toast from "$lib/components/ui/Toast.svelte";
  import TierUpgradeModal from "$lib/components/ui/TierUpgradeModal.svelte";
  import SaveDeckModal from "$lib/components/deck/SaveDeckModal.svelte";
  import { cn } from "$lib/utils";
  import Markdown from "$lib/components/ui/Markdown.svelte";
  import LayoutGrid from "@lucide/svelte/icons/layout-grid";
  import List from "@lucide/svelte/icons/list";
  import Flashcard from "$lib/components/ui/Flashcard.svelte";
  import { subscribeToJob } from "$lib/sse";
  import SmartNudge from "$lib/components/SmartNudge.svelte";
  import { trackEvent } from "$lib/utils/telemetry";

  interface Flashcard {
    question: string;
    answer: string;
    page?: number;
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
  let showTierModal = $state(false);
  let proModalType = $state<"daily_limit">("daily_limit");
  let unsubscribe: (() => void) | null = null;
  let errorMessage = $state<string | null>(null);
  let jobMetadata = $state<Record<string, any>>({});
  let sourceFileName = $state<string | null>(null);
  let isSavingDeck = $state(false);
  let isSaved = $state(false);
  let currentFileIndex = $state(0);
  let totalFiles = $state(0);
  let feedbackSubmitted = $state(false);
  let selectedStruggle = $state<string | null>(null);
  let viewMode = $state<"grid" | "list">("list");

  // Stalling Logic
  let uploadStallTimer = $state<NodeJS.Timeout | null>(null);
  let exportStallTimer = $state<NodeJS.Timeout | null>(null);
  let showExportNudge = $state(false);

  $effect(() => {
    // upload_stalled: 30s without action on upload page
    if (files.length === 0 && !isGenerating && !showResults && !isEmptyState) {
      uploadStallTimer = setTimeout(() => {
        trackEvent("upload_stalled");
      }, 30000);
    }

    return () => {
      if (uploadStallTimer) clearTimeout(uploadStallTimer);
    };
  });

  $effect(() => {
    // export_stalled: 60s after generation without export
    if (showResults && !isSaved) {
      exportStallTimer = setTimeout(() => {
        trackEvent("export_stalled");
        showExportNudge = true;
      }, 60000);
    }

    return () => {
      if (exportStallTimer) clearTimeout(exportStallTimer);
    };
  });

  // Validation Logic
  const MAX_SIZE_MB = 20;
  const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024;

  let totalSize = $derived(files.reduce((acc, f) => acc + f.size, 0));
  let isTotalSizeExceeded = $derived(totalSize > MAX_SIZE_BYTES);
  let hasOversizedFiles = $derived(files.some((f) => f.size > MAX_SIZE_BYTES));
  let isGenerationBlocked = $derived(
    isTotalSizeExceeded || hasOversizedFiles || files.length === 0,
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

  let dailyUploadCount = $derived(
    user.current?.dailyUploadCount ?? guestUsage.count,
  );
  let maxLimit = $derived(user.current?.tier === "PRO_USER" ? 100 : 3);
  let badgeColor = $derived(
    dailyUploadCount >= maxLimit
      ? "bg-red-500/20 text-red-400 border-red-500/50"
      : dailyUploadCount >= maxLimit - 1
        ? "bg-amber-500/20 text-amber-400 border-amber-500/50"
        : "bg-slate-800/50 text-slate-400 border-slate-700",
  );

  onMount(async () => {
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

  // Cleanup SSE connection when component unmounts
  $effect(() => {
    return () => {
      if (unsubscribe) {
        unsubscribe();
        unsubscribe = null;
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
            existing.lastModified === newFile.lastModified,
        ),
    );
    files = [...files, ...uniqueNewFiles];
    if (files.length > 0 && !sourceFileName) {
      sourceFileName = files[0].name.replace(/\.pdf$/i, "");
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
    errorMessage = null;
    totalFiles = files.length;
    currentFileIndex = 0;
    feedbackSubmitted = false;
    selectedStruggle = null;

    const filesToProcess = [...files]; // Copy the files array to process

    try {
      for (let i = 0; i < filesToProcess.length; i++) {
        currentFileIndex = i;
        const file = filesToProcess[i];

        // 1. Check daily limit before each file
        if (dailyUploadCount >= maxLimit) {
          proModalType = "daily_limit";
          showTierModal = true;
          isGenerating = false;
          // If we already have some cards, show them instead of just stopping
          if (generatedCards.length > 0) {
            showResults = true;
          }
          return;
        }

        // 2. Start the job for this file
        const formData = new FormData();
        formData.append("file", file);

        const response = await authFetch("/api/v1/pdf/generate-cards", {
          method: "POST",
          body: formData,
        });

        if (!response.ok) {
          const errorData = await response.json();
          const errorMsg =
            errorData.error || `Failed to start generation for ${file.name}`;
          errorMessage = errorMsg;
          if (errorMsg.includes("Daily upload limit reached")) {
            proModalType = "daily_limit";
            showTierModal = true;
            errorMessage = null;
          }
          break; // Stop the batch on error
        }

        const data = await response.json();
        jobId = data.jobId;
        jobStatus = "PROCESSING";
        jobProgress = 0;

        // 3. Subscribe to SSE for this specific job
        const jobResult = await new Promise<Flashcard[]>((resolve, reject) => {
          // Get token from Supabase session
          supabase?.auth.getSession().then(({ data: { session } }) => {
            const token = session?.access_token;

            unsubscribe = subscribeToJob(
              jobId!,
              {
                onInit: (data) => {
                  jobStatus = data.status;
                  jobProgress = data.progress;
                },
                onProgress: (data) => {
                  jobStatus = data.status;
                  jobProgress = data.progress;
                },
                onComplete: async (data) => {
                  if (unsubscribe) {
                    unsubscribe();
                    unsubscribe = null;
                  }
                  // Refresh usage count after each successful job
                  if (user.current) await fetchUser();
                  else await fetchGuestUsage();
                  const results = (data.results as Flashcard[]) ?? [];
                  resolve(results);
                },
                onError: (error: unknown) => {
                  const err = error as any;
                  if (unsubscribe) {
                    unsubscribe();
                    unsubscribe = null;
                  }
                  reject(
                    new Error(
                      err.message || `Generation failed for ${file.name}`,
                    ),
                  );
                },
              },
              token,
            );
          });
        });

        // 4. Accumulate results
        generatedCards = [...generatedCards, ...jobResult];
      }

      // Finalize batch
      isGenerating = false;
      if (generatedCards.length > 0) {
        showResults = true;
        files = []; // Clear queue only on success
      } else if (!errorMessage) {
        isEmptyState = true;
      }
    } catch (error: any) {
      isGenerating = false;
      errorMessage = error.message || "An error occurred during generation.";
      if (generatedCards.length > 0) {
        showResults = true;
      }
    }
  }

  async function dismissAllJobs() {
    try {
      await authFetch("/api/v1/pdf/jobs/dismiss-all", {
        method: "POST",
      });
    } catch (e) {
      if (import.meta.env.DEV) {
        console.error("Failed to dismiss all jobs:", e);
      }
    }
  }

  async function resetView() {
    await dismissAllJobs();
    isEmptyState = false;
    showResults = false;
    isSaved = false;
    generatedCards = [];
    files = [];
    errorMessage = null;
    jobMetadata = {};
    sourceFileName = null;
    jobId = null;
    jobStatus = "PROCESSING";
    jobProgress = 0;
  }

  async function saveToLibrary(customTitle: string) {
    if (!user.current || generatedCards.length === 0) return;

    isSavingDeck = true;
    try {
      const response = await authFetch("/api/v1/decks", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: customTitle,
          cards: generatedCards,
        }),
      });

      if (response.ok) {
        showSaveModal = false;
        isSaved = true;
        // Dismiss jobs so they don't show up as "recent" anywhere else
        await dismissAllJobs();
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to save deck:", error);
      }
    } finally {
      isSavingDeck = false;
    }
  }
</script>

<div class="space-y-8">
  <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-bold text-white tracking-tight">Upload PDF</h1>
      <p class="text-zinc-400 mt-2">
        Upload your PDFs to generate new flashcard decks.
      </p>
    </div>

    <div
      class={cn(
        "flex items-center gap-3 px-4 py-2 rounded-full border text-sm font-medium transition-all duration-300",
        badgeColor,
      )}
    >
      <div class="w-2 h-2 rounded-full bg-current animate-pulse"></div>
      <span>{dailyUploadCount} / {maxLimit} Daily Uploads</span>
    </div>
  </div>

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
                      >{jobMetadata.isAnnotationLimited
                        ? `Limited to ${jobMetadata.annotationLimit} cards per file. Upgrade for more!`
                        : "AI can make mistakes. You can refine these in the library."}</span
                    >
                  </div>
                </div>
              </div>
              <div class="flex items-center gap-4 mt-2">
                <p class="text-zinc-400 text-sm">
                  Created {generatedCards.length} flashcards
                </p>

                <!-- View Toggle -->
                <div
                  class="flex items-center p-1 bg-zinc-900 border border-zinc-800 rounded-lg"
                >
                  <button
                    onclick={() => (viewMode = "list")}
                    class={cn(
                      "p-1.5 rounded-md transition-all duration-200",
                      viewMode === "list"
                        ? "bg-zinc-800 text-amber-500 shadow-sm"
                        : "text-zinc-500 hover:text-zinc-300",
                    )}
                    title="List View"
                  >
                    <List class="w-4 h-4" />
                  </button>
                  <button
                    onclick={() => (viewMode = "grid")}
                    class={cn(
                      "p-1.5 rounded-md transition-all duration-200",
                      viewMode === "grid"
                        ? "bg-zinc-800 text-amber-500 shadow-sm"
                        : "text-zinc-500 hover:text-zinc-300",
                    )}
                    title="Grid View"
                  >
                    <LayoutGrid class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-4">
            <div
              class="px-2 py-0.5 rounded-full {badgeColor} text-[10px] font-bold uppercase tracking-wider border transition-colors duration-300"
            >
              {dailyUploadCount} / {maxLimit} Daily Uploads
            </div>

            {#if isSaved}
              <div
                class="flex items-center gap-3 animate-in fade-in slide-in-from-right-4 duration-500"
              >
                <p class="text-green-400 font-bold text-sm tracking-wide">
                  Deck Saved Successfully!
                </p>
                <div class="flex items-center gap-2">
                  <button
                    onclick={() => goto("/library")}
                    class="flex items-center gap-2 px-4 py-1.5 rounded-full bg-amber-500 text-black text-xs font-bold hover:bg-amber-400 transition-all shadow-lg shadow-amber-500/10"
                  >
                    <Library class="w-3.5 h-3.5" />
                    Go to Library
                  </button>
                  <button
                    onclick={resetView}
                    class="px-4 py-1.5 rounded-full bg-zinc-800 text-zinc-400 text-xs font-bold hover:text-white hover:bg-zinc-700 transition-all border border-zinc-700"
                  >
                    Start Over
                  </button>
                </div>
              </div>
            {:else}
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
            {/if}
          </div>
        </div>

        <div
          class={cn(
            viewMode === "grid"
              ? "grid gap-4 md:grid-cols-2 lg:grid-cols-3"
              : "flex flex-col gap-4 max-w-3xl mx-auto w-full",
          )}
        >
          {#each generatedCards as card, i}
            <div in:fly={{ y: 20, duration: 300, delay: i * 50 }}>
              <Flashcard {card} {viewMode} />
            </div>
          {/each}
        </div>

        <!-- SmartNudge Inline Feedback -->
        <div class="max-w-3xl mx-auto w-full mt-8">
          <SmartNudge
            type="inline"
            message="How is the quality of the flashcards?"
            options={[
              { label: "Perfect", value: "perfect", primary: true },
              { label: "Needs Work", value: "bad" },
            ]}
            onAction={(val) => trackEvent("feedback_quality", { value: val })}
          />
        </div>
      </div>
    {:else if isEmptyState}
      <div
        class="flex flex-col items-center justify-center py-12 text-center space-y-4 animate-in fade-in zoom-in duration-300"
      >
        <BookOpen class="w-16 h-16 text-zinc-500 mb-4" />
        <div class="flex items-center gap-3">
          <h2 class="text-xl font-bold text-white">No Study Traces Found</h2>
          <div
            class="px-2 py-0.5 rounded-full {badgeColor} text-[10px] font-bold uppercase tracking-wider border transition-colors duration-300"
          >
            {dailyUploadCount} / {maxLimit} Daily Uploads
          </div>
        </div>
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
                {isGenerating
                  ? `Generating (${currentFileIndex + 1}/${totalFiles})...`
                  : "Generate All Decks"}
              </button>
            </div>
          </div>

          {#if isGenerating}
            <div class="flex flex-col items-center justify-center py-12">
              <!-- SmartNudge Loading Tip -->
              <div class="mb-8 w-full max-w-md">
                <SmartNudge
                  type="loading"
                  message="Welcome back! What are you studying today?"
                  options={[
                    { label: "Textbook", value: "textbook" },
                    { label: "Lecture Notes", value: "lecture_notes" },
                    { label: "Certification", value: "certification" },
                  ]}
                  onAction={(tag) =>
                    trackEvent("upload_context", { type: tag })}
                />
              </div>

              <div class="w-full max-w-md">
                <ProgressBar progress={jobProgress} status={jobStatus} />
              </div>
              <p class="text-zinc-400 mt-4">
                Analyzing {files[currentFileIndex]?.name || "document"}...
              </p>
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
</div>

{#if showLoginModal}
  <LoginModal redirectTo="/upload" onclose={() => (showLoginModal = false)} />
{/if}

{#if showTotalSizeToast}
  <Toast
    message="The total file size exceeds the 20MB limit. Please remove some files."
    onclose={() => (showTotalSizeToast = false)}
  />
{/if}

{#if showTierModal}
  <TierUpgradeModal
    type={proModalType}
    mode={user.current ? "free" : "guest"}
    onclose={() => (showTierModal = false)}
    onsignup={() => (showLoginModal = true)}
  />
{/if}

{#if showSaveModal}
  <SaveDeckModal
    initialTitle={jobMetadata.title || sourceFileName || ""}
    onclose={() => (showSaveModal = false)}
    onsave={saveToLibrary}
    isSaving={isSavingDeck}
  />
{/if}
