<script lang="ts">
  import FileDropzone from "$lib/components/upload/FileDropzone.svelte";
  import FileItemCard from "$lib/components/upload/FileItemCard.svelte";
  import { fade, fly } from "svelte/transition";
  import { flip } from "svelte/animate";
  import { BookOpen } from "@lucide/svelte";
  import ProgressBar from "$lib/components/ui/ProgressBar.svelte";

  let files = $state<File[]>([]);
  let isGenerating = $state(false);
  let isEmptyState = $state(false);
  let jobId = $state<string | null>(null);
  let jobStatus = $state("PROCESSING");
  let jobProgress = $state(0);
  let pollInterval: ReturnType<typeof setInterval> | null = null;

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
    jobId = null;
    jobProgress = 0;
    jobStatus = "PROCESSING";

    try {
      const formData = new FormData();
      formData.append("file", files[0]);

      // 1. Start Async Job
      const startResponse = await fetch("/api/v1/pdf/generate-cards", {
        method: "POST",
        body: formData,
      });

      if (startResponse.ok) {
        const startData = await startResponse.json();
        jobId = startData.jobId;

        // 2. Poll for Status
        pollInterval = setInterval(async () => {
          if (!jobId) return;

          try {
            const statusResponse = await fetch(
              `/api/v1/pdf/jobs/${startData.jobId}`
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

                if (
                  Array.isArray(statusData.results) &&
                  statusData.results.length === 0
                ) {
                  isEmptyState = true;
                } else {
                  // TODO: Navigate to deck view or show success
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

  function resetView() {
    isEmptyState = false;
    files = [];
  }
</script>

<div class="space-y-8">
  <div>
    <h1 class="text-3xl font-bold text-white tracking-tight">Dashboard</h1>
    <p class="text-zinc-400 mt-2">
      Welcome back! Upload your PDFs to generate new flashcard decks.
    </p>
  </div>

  <!-- Upload Section -->
  <section class="space-y-6">
    {#if isEmptyState}
      <div
        class="flex flex-col items-center justify-center py-12 text-center space-y-4 animate-in fade-in zoom-in duration-300"
      >
        <BookOpen class="w-16 h-16 text-zinc-500 mb-4" />
        <h2 class="text-xl font-bold text-white">No Study Traces Found</h2>
        <p class="text-zinc-400 mt-2">
          We don't provide flashcards without your annotation.<br />
          <span class="text-[#FFD700] font-bold">Go study and come back!</span>
        </p>
        <p class="text-sm text-zinc-500">
          Please highlight or underline key concepts in your PDF and re-upload.
        </p>
        <button
          onclick={resetView}
          class="mt-6 px-6 py-2 bg-[#FFD700] hover:bg-[#FDB931] text-black font-bold rounded-lg shadow-[0_0_15px_rgba(255,215,0,0.2)] transition-all"
        >
          Upload Another File
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

  <!-- Recent Activity Section (Placeholder) -->
  <section class="pt-8 border-t border-white/10">
    <h2 class="text-xl font-semibold text-white mb-6">Recent Decks</h2>
    <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      {#each Array(3) as _, i}
        <div
          class="group rounded-xl border border-zinc-800 bg-zinc-900/50 p-6 shadow-sm hover:border-[#FFD700]/30 transition-all hover:bg-zinc-900/80"
        >
          <div
            class="h-10 w-10 rounded-lg bg-[#FFD700]/10 flex items-center justify-center mb-4 group-hover:bg-[#FFD700]/20 transition-colors"
          >
            <span class="text-xl">📚</span>
          </div>
          <h3 class="text-lg font-semibold text-zinc-100">
            Introduction to AI - Part {i + 1}
          </h3>
          <p class="text-sm text-zinc-400 mt-2">Last studied 2 hours ago</p>
        </div>
      {/each}
    </div>
  </section>
</div>
