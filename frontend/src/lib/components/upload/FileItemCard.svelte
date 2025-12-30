<script lang="ts">
  import FileText from "@lucide/svelte/icons/file-text";
  import X from "@lucide/svelte/icons/x";
  import CircleCheck from "@lucide/svelte/icons/circle-check";
  import CircleAlert from "@lucide/svelte/icons/circle-alert";
  import { fly } from "svelte/transition";

  let {
    file,
    isOversized = false,
    onRemove,
  } = $props<{
    file: File;
    isOversized?: boolean;
    onRemove: () => void;
  }>();

  function formatSize(bytes: number): string {
    if (bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB"];
    const i = Math.max(0, Math.floor(Math.log(bytes) / Math.log(k)));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
  }
</script>

<div
  class="group relative bg-zinc-900/50 border rounded-xl p-4 transition-all duration-300 hover:bg-zinc-900/80 {isOversized
    ? 'border-red-500/50 bg-red-500/5'
    : 'border-white/10 hover:border-[#FFD700]/30'}"
  transition:fly={{ y: 10, duration: 200 }}
>
  <div class="flex items-start gap-4">
    <!-- Icon -->
    <div
      class="w-10 h-10 rounded-lg flex items-center justify-center shrink-0 transition-colors {isOversized
        ? 'bg-red-500/10'
        : 'bg-[#FFD700]/10 group-hover:bg-[#FFD700]/20'}"
    >
      <FileText
        class="w-5 h-5 {isOversized ? 'text-red-500' : 'text-[#FFD700]'}"
      />
    </div>

    <!-- Content -->
    <div class="flex-1 min-w-0">
      <h4 class="text-white font-medium truncate pr-6" title={file.name}>
        {file.name}
      </h4>
      <div class="flex items-center gap-2 mt-1">
        <span
          class="text-xs font-mono {isOversized
            ? 'text-red-400'
            : 'text-white/40'}">{formatSize(file.size)}</span
        >
        <span class="text-white/20">•</span>
        {#if isOversized}
          <span
            class="text-xs text-red-500 flex items-center gap-1 font-bold"
            title="File size exceeds the 20MB limit"
          >
            <CircleAlert class="w-3 h-3" /> Too Large
          </span>
        {:else}
          <span class="text-xs text-[#FFD700]/80 flex items-center gap-1">
            <CircleCheck class="w-3 h-3" /> Ready
          </span>
        {/if}
      </div>
    </div>

    <!-- Remove Button -->
    <button
      onclick={onRemove}
      class="absolute top-2 right-2 p-1.5 rounded-full text-white/40 hover:text-white hover:bg-white/10 transition-all opacity-0 group-hover:opacity-100"
      aria-label="Remove file"
    >
      <X class="w-4 h-4" />
    </button>
  </div>
</div>
