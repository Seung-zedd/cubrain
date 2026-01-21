<script lang="ts">
  import { fade, fly } from "svelte/transition";
  import { cubicOut } from "svelte/easing";
  import X from "@lucide/svelte/icons/x";
  import Info from "@lucide/svelte/icons/info";
  import Check from "@lucide/svelte/icons/check";
  import { cn } from "$lib/utils";
  import { trackEvent } from "$lib/utils/telemetry";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";

  interface Props {
    type: "inline" | "loading" | "toast";
    message: string;
    options?: { label: string; value: string; primary?: boolean }[];
    onAction?: (value: string) => void;
    onDismiss?: () => void;
    className?: string;
    nudgeId?: string; // Optional ID for persistence
  }

  let {
    type,
    message,
    options = [],
    onAction,
    onDismiss,
    className,
    nudgeId,
  }: Props = $props();

  let isVisible = $state(false); // Default to false, check on mount
  let isSubmitted = $state(false);

  // Generate a unique key for this nudge
  const storageKey = $derived(
    `nudge_dismissed_${nudgeId || type}_${message.replace(/\s+/g, "_").toLowerCase()}`,
  );

  onMount(() => {
    if (browser) {
      const isDismissed = localStorage.getItem(storageKey);
      if (!isDismissed) {
        isVisible = true;
      }
    }
  });

  function handleAction(value: string) {
    // Trigger specific Hotjar events for precise tracking
    if (type === "inline") {
      if (value === "perfect") trackEvent("nudge_feedback_perfect");
      else if (value === "bad") trackEvent("nudge_feedback_needs_work");
    } else if (type === "loading") {
      if (value === "textbook") trackEvent("nudge_context_textbook");
      else if (value === "lecture_notes")
        trackEvent("nudge_context_lecture_notes");
      else if (value === "certification")
        trackEvent("nudge_context_certification");
    } else if (type === "toast") {
      if (value === "known") trackEvent("nudge_export_help_dismissed");
      else if (value === "guide") trackEvent("nudge_export_help_clicked");
    }

    if (onAction) onAction(value);

    // Persist the hidden state
    if (browser) {
      localStorage.setItem(storageKey, "true");
    }

    if (type === "inline" || type === "loading") {
      isSubmitted = true;
      setTimeout(() => {
        isVisible = false;
      }, 3000);
    } else {
      isVisible = false;
    }
  }

  function handleDismiss() {
    if (onDismiss) onDismiss();

    // Persist the hidden state
    if (browser) {
      localStorage.setItem(storageKey, "true");
    }
    isVisible = false;
  }
</script>

{#if isVisible}
  {#if type === "inline"}
    <div
      class={cn(
        "w-fit px-4 py-2 rounded-full bg-zinc-900/90 backdrop-blur-md border border-zinc-800 shadow-2xl flex items-center gap-4",
        className,
      )}
      in:fly={{ y: 50, duration: 600, easing: cubicOut, delay: 200 }}
      out:fade={{ duration: 300 }}
    >
      {#if isSubmitted}
        <div
          class="flex items-center gap-2"
          transition:fade={{ duration: 400 }}
        >
          <Check class="w-3.5 h-3.5 text-green-500" />
          <p class="text-sm font-bold text-zinc-300 whitespace-nowrap">
            Thanks! Your feedback will help our app improve.
          </p>
        </div>
      {:else}
        <div
          class="flex items-center gap-4"
          transition:fade={{ duration: 300 }}
        >
          <p class="text-sm font-bold text-zinc-300 whitespace-nowrap">
            {message}
          </p>
          <div class="flex items-center gap-1.5">
            {#each options as option}
              <button
                onclick={() => handleAction(option.value)}
                class={cn(
                  "px-3 py-1 rounded-full text-xs font-black transition-all",
                  option.primary
                    ? "bg-amber-500 text-black hover:bg-amber-400"
                    : "bg-zinc-800 text-zinc-400 hover:text-white hover:bg-zinc-700 border border-zinc-700",
                )}
              >
                {option.label}
              </button>
            {/each}
          </div>
        </div>
      {/if}
    </div>
  {:else if type === "loading"}
    <div
      class={cn(
        "w-full p-5 rounded-2xl bg-zinc-900/50 border border-zinc-800/50 backdrop-blur-sm space-y-3",
        className,
      )}
      in:fly={{ y: 50, duration: 600, easing: cubicOut }}
      out:fade={{ duration: 300 }}
    >
      {#if isSubmitted}
        <div
          class="flex flex-col items-center justify-center py-2 gap-3"
          transition:fade={{ duration: 400 }}
        >
          <div
            class="w-10 h-10 rounded-full bg-green-500/20 flex items-center justify-center"
          >
            <Check class="w-5 h-5 text-green-500" />
          </div>
          <p class="text-zinc-200 font-bold text-base text-center">
            Thanks! Your feedback will help our app improve.
          </p>
        </div>
      {:else}
        <div class="space-y-3" transition:fade={{ duration: 300 }}>
          <h3 class="text-zinc-200 font-bold text-base text-center">
            {message}
          </h3>
          <div class="flex flex-wrap justify-center gap-2">
            {#each options as option}
              <button
                onclick={() => handleAction(option.value)}
                class="py-1.5 px-3 rounded-xl bg-zinc-800/50 hover:bg-amber-500/10 hover:text-amber-500 border border-zinc-700/30 hover:border-amber-500/30 text-zinc-400 text-xs font-bold transition-all"
              >
                {option.label}
              </button>
            {/each}
          </div>
        </div>
      {/if}
    </div>
  {:else if type === "toast"}
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
{/if}
