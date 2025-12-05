<script lang="ts">
  import FileDropzone from "$lib/components/upload/FileDropzone.svelte";
  import FileItemCard from "$lib/components/upload/FileItemCard.svelte";
  import { fade, fly } from "svelte/transition";

  let files: File[] = [];
  let isGenerating = false;

  function handleFileSelect(newFiles: File[]) {
    // Guest mode: Only allow 1 file
    files = [newFiles[0]];
  }

  function removeFile() {
    files = [];
  }

  async function handleGenerate() {
    if (files.length === 0) return;

    isGenerating = true;
    // Mock generation delay
    await new Promise((resolve) => setTimeout(resolve, 2000));
    isGenerating = false;
    alert("This is a demo! In the full app, this would generate flashcards.");
  }
</script>

<div class="min-h-screen pt-32 pb-20 px-4">
  <div class="max-w-4xl mx-auto">
    <!-- Header -->
    <div class="text-center mb-12">
      <h1 class="text-4xl md:text-5xl font-bold text-white mb-4">
        Try <span class="text-[#FFD700]">Cubrain</span> for Free
      </h1>
      <p class="text-white/60 text-lg max-w-2xl mx-auto">
        Upload a PDF to see how our AI transforms your study materials into
        interactive flashcards.
      </p>
    </div>

    <!-- Upload Area -->
    <div class="relative z-10">
      {#if files.length === 0}
        <div in:fade={{ duration: 300 }}>
          <FileDropzone isGuest={true} onFileSelect={handleFileSelect} />
        </div>
      {:else}
        <div class="max-w-2xl mx-auto" in:fly={{ y: 20, duration: 300 }}>
          <div class="mb-8">
            <FileItemCard file={files[0]} onRemove={removeFile} />
          </div>

          <!-- Guest Limit Notice -->
          <div
            class="flex items-center justify-center gap-2 text-[#FFD700]/80 text-sm mb-8 bg-[#FFD700]/5 py-3 px-4 rounded-lg border border-[#FFD700]/10"
          >
            <span>✨</span>
            <span
              >Guest Mode Limit: We'll generate flashcards from the <strong
                >first 3 pages</strong
              > only.</span
            >
          </div>

          <!-- Generate Button -->
          <button
            on:click={handleGenerate}
            disabled={isGenerating}
            class="w-full py-4 bg-[#FFD700] hover:bg-[#FDB931] text-black font-bold text-xl rounded-xl shadow-[0_0_20px_rgba(255,215,0,0.3)] hover:shadow-[0_0_30px_rgba(255,215,0,0.5)] transition-all transform hover:-translate-y-1 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
          >
            {#if isGenerating}
              <span class="inline-block animate-spin mr-2">⏳</span>
              Generating...
            {:else}
              Generate Flashcards ✨
            {/if}
          </button>

          <p class="text-center mt-4 text-white/40 text-sm">
            Want to process the whole file? <a
              href="/#waitlist"
              class="text-[#FFD700] hover:underline">Join the waitlist</a
            >
          </p>
        </div>
      {/if}
    </div>
  </div>
</div>
