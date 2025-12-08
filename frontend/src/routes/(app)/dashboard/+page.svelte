<script lang="ts">
  import FileDropzone from "$lib/components/upload/FileDropzone.svelte";
  import FileItemCard from "$lib/components/upload/FileItemCard.svelte";
  import { fade, fly } from "svelte/transition";
  import { flip } from "svelte/animate";

  let files: File[] = [];
  let isGenerating = false;

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
    // Mock generation delay
    await new Promise((resolve) => setTimeout(resolve, 2000));
    isGenerating = false;
    // alert(`Generating decks for ${files.length} files...`);
    // TODO: Replace with real backend API call
    console.log(`[Mock] Generating decks for ${files.length} files...`);

    // Reset after "generation"
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
    <FileDropzone isGuest={false} onFileSelect={handleFileSelect} />

    {#if files.length > 0}
      <div class="space-y-4" transition:fade>
        <div class="flex items-center justify-between">
          <h2 class="text-xl font-semibold text-white">
            Upload Queue ({files.length})
          </h2>
          <button
            on:click={handleGenerate}
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

        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {#each files as file, i (file.name + file.size)}
            <div animate:flip={{ duration: 300 }}>
              <FileItemCard {file} onRemove={() => removeFile(i)} />
            </div>
          {/each}
        </div>
      </div>
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
