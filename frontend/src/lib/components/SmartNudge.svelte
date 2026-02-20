<script lang="ts">
  import { fade, fly } from "svelte/transition";
  import { cubicOut } from "svelte/easing";
  import X from "@lucide/svelte/icons/x";
  import Info from "@lucide/svelte/icons/info";
  import { cn } from "$lib/utils";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";

  interface Props {
    type: "toast";
    message: string;
    options?: { label: string; value: string; primary?: boolean }[];
    onAction?: (value: string) => void;
    onDismiss?: () => void;
    className?: string;
    nudgeId?: string; // Optional ID for persistence
    expiryHours?: number; // Hours to hide the nudge (default: 24h)
  }

  let {
    type,
    message,
    options = [],
    onAction,
    onDismiss,
    className,
    nudgeId,
    expiryHours = 24,
  }: Props = $props();

  let isVisible = $state(false); // Default to false, check on mount
  let isSubmitted = $state(false);

  // Generate a unique key for this nudge
  const storageKey = $derived(
    `nudge_dismissed_${nudgeId || type}_${message.replace(/\s+/g, "_").toLowerCase()}`,
  );

  onMount(() => {
    if (browser) {
      const storedValue = localStorage.getItem(storageKey);
      if (!storedValue) {
        isVisible = true;
      } else {
        const timestamp = parseInt(storedValue);
        if (isNaN(timestamp)) {
          // Handle legacy "true" values by showing the nudge again
          isVisible = true;
        } else {
          const elapsedHours = (Date.now() - timestamp) / (1000 * 60 * 60);
          if (elapsedHours >= expiryHours) {
            isVisible = true;
          }
        }
      }
    }
  });

  function handleAction(value: string) {
    if (onAction) onAction(value);

    // Persist the dismissal timestamp
    if (browser) {
      localStorage.setItem(storageKey, Date.now().toString());
    }

    isVisible = false;
  }

  function handleDismiss() {
    if (onDismiss) onDismiss();

    // Persist the dismissal timestamp
    if (browser) {
      localStorage.setItem(storageKey, Date.now().toString());
    }
    isVisible = false;
  }
</script>

{#if isVisible && type === "toast"}
  <div
    class={cn(
      "fixed bottom-8 left-1/2 -translate-x-1/2 z-50 min-w-[320px] max-w-md",
      className,
    )}
    in:fly={{ y: 50, duration: 600, easing: cubicOut }}
    out:fade={{ duration: 400 }}
  >
    <div
      class="flex items-center gap-3 p-4 rounded-xl border border-zinc-700/50 bg-zinc-900/90 backdrop-blur-md shadow-2xl text-zinc-200"
    >
      <div class="shrink-0">
        <Info class="w-5 h-5 text-amber-500" />
      </div>
      <p class="flex-1 text-base font-medium leading-tight">{message}</p>
      <div class="flex items-center gap-2">
        {#each options as option}
          <button
            onclick={() => handleAction(option.value)}
            class={cn(
              "px-3 py-1.5 rounded-lg text-sm font-bold transition-all whitespace-nowrap",
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
