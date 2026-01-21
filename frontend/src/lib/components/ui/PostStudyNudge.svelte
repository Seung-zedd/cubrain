<script lang="ts">
  import { onMount } from "svelte";
  import { fade, fly } from "svelte/transition";
  import { cubicOut } from "svelte/easing";
  import X from "@lucide/svelte/icons/x";
  import Check from "@lucide/svelte/icons/check";
  import { cn } from "$lib/utils";

  interface Props {
    onClose?: () => void;
  }

  let { onClose }: Props = $props();

  // State
  let isVisible = $state(false);
  let isSubmitted = $state(false);
  let randomQuote = $state("");

  // Motivational Quotes for Cheer Mode
  const quotes = [
    "Knowledge locked in. 🧠",
    "1% better every day. 🚀",
    "Your brain is growing! ✨",
    "Consistency is key. 🔑",
    "Focus is your superpower. 💪",
  ];

  // Nudge Messages
  const nudgeMessages = [
    { title: "Brains warmed up!", subtitle: "Decide next step in 10s?" },
    { title: "Flow state active.", subtitle: "Capture this momentum?" },
    { title: "Knowledge unlocked.", subtitle: "Lock it in for good?" },
    { title: "Solid focus today.", subtitle: "Want to keep the streak?" },
    { title: "Brain upgraded.", subtitle: "What's your next target?" },
  ];
  let selectedNudge = $state(nudgeMessages[0]);

  onMount(() => {
    randomQuote = quotes[Math.floor(Math.random() * quotes.length)];
    selectedNudge =
      nudgeMessages[Math.floor(Math.random() * nudgeMessages.length)];

    // 800ms delay before showing
    setTimeout(() => {
      isVisible = true;
    }, 800);
  });

  function handleClose() {
    isVisible = false;
    if (onClose) setTimeout(onClose, 400);
  }

  function handleAction() {
    isSubmitted = true;
    // Auto close after 3 seconds in cheer mode
    setTimeout(() => {
      handleClose();
    }, 3000);
  }
</script>

{#if isVisible}
  <div
    class="fixed bottom-6 right-6 z-50 flex flex-col items-end"
    in:fly={{ y: 50, duration: 600, easing: cubicOut }}
    out:fade
  >
    <div
      class={cn(
        "relative bg-zinc-900 border border-zinc-800 rounded-2xl shadow-2xl transition-all duration-500 ease-in-out overflow-hidden",
        "w-[320px] h-[80px] p-5 cursor-pointer hover:border-amber-500/30",
      )}
      role="button"
      tabindex={0}
      onclick={!isSubmitted ? handleAction : undefined}
      onkeydown={(e) =>
        (e.key === "Enter" || e.key === " ") && !isSubmitted && handleAction()}
    >
      <!-- Close Button -->
      <button
        class="absolute top-3 right-3 p-1 text-gray-400 hover:text-white transition-colors z-10"
        onclick={(e) => {
          e.stopPropagation();
          handleClose();
        }}
        aria-label="Close"
      >
        <X class="w-4 h-4" />
      </button>

      {#if isSubmitted}
        <!-- Cheer Mode -->
        <div class="flex items-center gap-3 h-full" in:fade>
          <div
            class="w-8 h-8 rounded-full bg-green-500/20 flex items-center justify-center shrink-0"
          >
            <Check class="w-4 h-4 text-green-500" />
          </div>
          <p class="text-zinc-200 text-base font-medium">{randomQuote}</p>
        </div>
      {:else}
        <!-- Nudge State -->
        <div class="flex flex-col justify-center h-full pr-6">
          <p class="text-zinc-200 text-base font-bold leading-tight">
            {selectedNudge.title}
          </p>
          <p class="text-zinc-500 text-sm">{selectedNudge.subtitle}</p>
        </div>
      {/if}
    </div>
  </div>
{/if}
