<script lang="ts">
  let { progress = 0, status = "PROCESSING" } = $props();
</script>

<div class="w-full bg-zinc-800 rounded-full h-4 mb-4 overflow-hidden">
  <div
    class="bg-[#FFD700] h-4 rounded-full transition-all duration-500 ease-out"
    style="width: {progress}%"
  ></div>
</div>

<div class="flex justify-between text-sm text-zinc-400">
  <span>
    {#if status === "PROCESSING"}
      Analyzing PDF... {progress}%
    {:else if status === "COMPLETED"}
      <span class="text-[#FFD700]">Analysis Complete!</span>
    {:else if status === "FAILED"}
      <span class="text-red-500">Analysis Failed</span>
    {/if}
  </span>
  <span>{progress}%</span>
</div>
