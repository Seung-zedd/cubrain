<script lang="ts">
  import { CloudUpload, FileText, AlertCircle, Info } from "@lucide/svelte";
  import { fade } from "svelte/transition";

  let { isGuest = false, onFileSelect } = $props<{
    isGuest?: boolean;
    onFileSelect: (files: File[]) => void;
  }>();

  let isDragging = $state(false);
  let errorMessage = $state("");
  let fileInput = $state<HTMLInputElement>();
  let timeoutId: ReturnType<typeof setTimeout> | undefined;

  // Cleanup timeout when component unmounts
  $effect(() => {
    return () => {
      if (timeoutId) clearTimeout(timeoutId);
    };
  });

  const MAX_SIZE_MB = 20;
  const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024;

  function handleDragEnter(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    isDragging = true;
  }

  function handleDragLeave(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    // Only set dragging to false if we're leaving the dropzone itself
    if (e.currentTarget === e.target) {
      isDragging = false;
    }
  }

  function handleDragOver(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    isDragging = true;
  }

  function validateFiles(files: FileList | null): File[] {
    if (!files || files.length === 0) return [];

    const validFiles: File[] = [];
    const errors: string[] = [];
    errorMessage = "";

    for (let i = 0; i < files.length; i++) {
      const file = files[i];

      if (file.type !== "application/pdf") {
        errors.push(`"${file.name}" is not a PDF`);
        continue;
      }

      if (file.size > MAX_SIZE_BYTES) {
        errors.push(`"${file.name}" is too large (> ${MAX_SIZE_MB}MB)`);
        continue;
      }

      validFiles.push(file);
    }

    if (errors.length > 0) {
      if (errors.length === 1) {
        errorMessage = errors[0];
      } else {
        errorMessage = `${errors.length} files rejected (Invalid type or too large)`;
      }
      // Auto-clear error after 5 seconds
      if (timeoutId) clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        errorMessage = "";
      }, 5000);
    }

    return validFiles;
  }

  function handleDrop(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    isDragging = false;

    const files = validateFiles(e.dataTransfer?.files || null);
    if (files.length > 0) {
      onFileSelect(files);
    }
  }

  function handleFileInput(e: Event) {
    const input = e.target as HTMLInputElement;
    const files = validateFiles(input.files);
    if (files.length > 0) {
      onFileSelect(files);
    }
    // Reset input so same file can be selected again if needed
    input.value = "";
  }

  function triggerFileInput() {
    fileInput?.click();
  }
</script>

<div
  class="relative w-full max-w-4xl mx-auto transition-all duration-300 ease-out"
  role="region"
  aria-label="File Upload Dropzone"
  ondragenter={handleDragEnter}
  ondragleave={handleDragLeave}
  ondragover={handleDragOver}
  ondrop={handleDrop}
>
  <input
    type="file"
    accept=".pdf"
    multiple={!isGuest}
    class="hidden"
    bind:this={fileInput}
    onchange={handleFileInput}
  />

  <div
    class="
      relative flex flex-col items-center justify-center
      min-h-[300px] p-12
      border-2 border-dashed rounded-3xl
      transition-all duration-300
      {isDragging
      ? 'border-[#FFD700] bg-[#FFD700]/10 scale-[1.02]'
      : 'border-white/10 bg-black/20 hover:border-white/20 hover:bg-black/30'}
    "
  >
    <!-- Icon / Illustration -->
    <div class="mb-8 relative">
      <div
        class="w-20 h-20 rounded-2xl bg-[#FFD700]/10 flex items-center justify-center
        {isDragging ? 'scale-110' : ''} transition-transform duration-300"
      >
        <CloudUpload
          class="w-10 h-10 text-[#FFD700] {isDragging ? 'animate-bounce' : ''}"
        />
      </div>
      <!-- Decorative glow -->
      <div
        class="absolute inset-0 bg-[#FFD700] blur-2xl opacity-20 rounded-full -z-10"
      ></div>
    </div>

    <!-- Main Action Button -->
    <button
      onclick={triggerFileInput}
      class="
        px-8 py-4 mb-6
        bg-[#FFD700] hover:bg-[#FDB931]
        text-black font-bold text-xl rounded-xl
        shadow-[0_0_20px_rgba(255,215,0,0.3)]
        hover:shadow-[0_0_30px_rgba(255,215,0,0.5)]
        transform hover:-translate-y-1
        transition-all duration-300
        flex items-center gap-3
      "
    >
      <FileText class="w-6 h-6" />
      Select PDF files
    </button>

    <!-- Secondary Text -->
    <p class="text-white/60 text-lg mb-2 font-medium">or drop PDFs here</p>

    <!-- Limits / Info -->
    <div class="text-white/40 text-sm flex items-center justify-center gap-2">
      {#if isGuest}
        Guest mode: First 3 pages only • Max {MAX_SIZE_MB}MB
      {:else}
        <span class="group relative flex items-center gap-1.5 cursor-help">
          Full PDF
          <div class="relative">
            <Info class="w-4 h-4" />
            <div
              class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-56 p-3 bg-zinc-900/95 backdrop-blur text-zinc-200 text-xs rounded-xl opacity-0 group-hover:opacity-100 transition-all duration-200 pointer-events-none border border-white/10 text-center shadow-xl z-10"
            >
              <p class="font-medium text-white mb-1">Free Plan Limits</p>
              10 highlights + 10 underlines<br />per PDF
              <div
                class="absolute -bottom-1 left-1/2 -translate-x-1/2 w-2 h-2 bg-zinc-900/95 border-r border-b border-white/10 rotate-45"
              ></div>
            </div>
          </div>
        </span>
        • Max {MAX_SIZE_MB}MB
      {/if}
    </div>

    <!-- Error Message -->
    {#if errorMessage}
      <div
        transition:fade
        class="absolute bottom-4 left-1/2 -translate-x-1/2 flex items-center gap-2 text-red-400 bg-red-950/50 px-4 py-2 rounded-lg border border-red-500/20"
      >
        <AlertCircle class="w-4 h-4" />
        <span class="text-sm font-medium">{errorMessage}</span>
      </div>
    {/if}
  </div>
</div>
