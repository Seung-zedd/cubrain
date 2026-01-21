<script lang="ts">
  import { fade, fly } from "svelte/transition";
  import X from "@lucide/svelte/icons/x";
  import Info from "@lucide/svelte/icons/info";
  import { cn } from "$lib/utils";

  interface Props {
    type: "inline" | "loading" | "toast";
    message: string;
    options?: { label: string; value: string; primary?: boolean }[];
    onAction?: (value: string) => void;
    onDismiss?: () => void;
    className?: string;
  }

  let {
    type,
    message,
    options = [],
    onAction,
    onDismiss,
    className,
  }: Props = $props();

  let isVisible = $state(true);

  function handleAction(value: string) {
    if (onAction) onAction(value);
    isVisible = false;
  }

  function handleDismiss() {
    if (onDismiss) onDismiss();
    isVisible = false;
  }
</script>

{#if isVisible}
  {#if type === "inline"}
    <div
      class={cn(
        "p-4 rounded-xl bg-zinc-900 border border-zinc-800 flex flex-col sm:flex-row items-center justify-between gap-4 animate-in fade-in slide-in-from-bottom-2",
        className,
      )}
      transition:fade
    >
      <p class="text-sm font-medium text-zinc-200">{message}</p>
      <div class="flex items-center gap-2">
        {#each options as option}
          <button
            onclick={() => handleAction(option.value)}
            class={cn(
              "px-3 py-1.5 rounded-lg text-xs font-bold transition-all",
              option.primary
                ? "bg-amber-500 text-black hover:bg-amber-400"
                : "bg-zinc-800 text-zinc-300 hover:text-white hover:bg-zinc-700 border border-zinc-700",
            )}
          >
            {option.label}
          </button>
        {/each}
      </div>
    </div>
  {:else if type === "loading"}
    <div
      class={cn(
        "w-full p-6 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-sm space-y-4",
        className,
      )}
      transition:fade
    >
      <h3 class="text-white font-bold text-center">{message}</h3>
      <div class="grid grid-cols-3 gap-2">
        {#each options as option}
          <button
            onclick={() => handleAction(option.value)}
            class="py-2 px-3 rounded-xl bg-white/5 hover:bg-amber-500/10 hover:text-amber-500 border border-white/5 hover:border-amber-500/30 text-zinc-400 text-xs font-medium transition-all"
          >
            {option.label}
          </button>
        {/each}
      </div>
    </div>
  {:else if type === "toast"}
    <div
      class={cn(
        "fixed bottom-8 left-1/2 -translate-x-1/2 z-50 min-w-[320px] max-w-md",
        className,
      )}
      in:fly={{ y: 20, duration: 400 }}
      out:fade
    >
      <div
        class="flex items-center gap-3 p-4 rounded-xl border border-zinc-700/50 bg-zinc-900/80 backdrop-blur-md shadow-2xl text-zinc-200"
      >
        <div class="shrink-0">
          <Info class="w-5 h-5 text-amber-500" />
        </div>
        <p class="flex-1 text-sm font-medium leading-tight">{message}</p>
        <div class="flex items-center gap-2">
          {#each options as option}
            <button
              onclick={() => handleAction(option.value)}
              class={cn(
                "px-3 py-1.5 rounded-lg text-xs font-bold transition-all whitespace-nowrap",
                option.primary
                  ? "bg-amber-500 text-black hover:bg-amber-400"
                  : "bg-zinc-800 text-zinc-300 hover:text-white hover:bg-zinc-700 border border-zinc-700",
              )}
            >
              {option.label}
            </button>
          {/each}
        </div>
        <button
          onclick={handleDismiss}
          class="shrink-0 p-1 rounded-lg hover:bg-white/10 transition-colors"
          aria-label="Dismiss"
        >
          <X class="w-4 h-4 opacity-60 hover:opacity-100" />
        </button>
      </div>
    </div>
  {/if}
{/if}
