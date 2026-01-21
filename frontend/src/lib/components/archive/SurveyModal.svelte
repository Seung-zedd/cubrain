<!--
  Morphing Logic Documentation:
  This logic handles the CSS transition from a small card to a full modal.
  Keep this intact for future porting to the 'Lucidify' project.
  Relevant lines: 145-150 (isExpanded state and transition classes).
-->
<script lang="ts">
  import { onMount } from "svelte";
  import { fade, fly, slide } from "svelte/transition";
  import { cubicOut } from "svelte/easing";
  import X from "@lucide/svelte/icons/x";
  import Check from "@lucide/svelte/icons/check";
  import { authFetch } from "$lib/api";
  import { cn } from "$lib/utils";

  interface Props {
    onClose?: () => void;
  }

  let { onClose }: Props = $props();

  // State
  let isVisible = $state(false);
  let isExpanded = $state(false);
  let isSubmitted = $state(false);
  let currentStep = $state(1);
  let isSubmitting = $state(false);

  // Survey Data
  let goals = $state<string[]>([]);
  let painPoints = $state<string[]>([]);
  let shortFeedback = $state("");

  // Options
  const goalOptions = ["Speed Reading", "Memory", "Insight", "Habit"];
  const painPointOptions = [
    "Translation Villain",
    "Loading Villain",
    "UI Villain",
    "Focus Villain",
  ];

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
  let randomQuote = $state("");

  // Check history on mount
  onMount(() => {
    const alreadySubmitted =
      localStorage.getItem("cubrain_survey_submitted") === "true";
    isSubmitted = alreadySubmitted;
    randomQuote = quotes[Math.floor(Math.random() * quotes.length)];
    selectedNudge =
      nudgeMessages[Math.floor(Math.random() * nudgeMessages.length)];

    // 800ms delay before showing
    setTimeout(() => {
      isVisible = true;
    }, 800);
  });

  function toggleGoal(goal: string) {
    if (goals.includes(goal)) {
      goals = goals.filter((g) => g !== goal);
    } else if (goals.length < 2) {
      goals = [...goals, goal];
    }
  }

  function togglePainPoint(point: string) {
    if (painPoints.includes(point)) {
      painPoints = painPoints.filter((p) => p !== point);
    } else {
      painPoints = [...painPoints, point];
    }
  }

  async function handleSubmit() {
    isSubmitting = true;
    try {
      const response = await authFetch("/api/v1/feedback", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          goals,
          painPoints,
          shortFeedback,
        }),
      });

      if (response.ok) {
        localStorage.setItem("cubrain_survey_submitted", "true");
        isSubmitted = true;
        isExpanded = false;
        // Show success state briefly then close or switch to cheer mode
        setTimeout(() => {
          handleClose();
        }, 2000);
      }
    } catch (error) {
      console.error("Failed to submit feedback:", error);
    } finally {
      isSubmitting = false;
    }
  }

  function handleClose() {
    isVisible = false;
    if (onClose) onClose();
  }

  function nextStep() {
    if (currentStep < 3) currentStep++;
  }

  function prevStep() {
    if (currentStep > 1) currentStep--;
  }

  function handleExpand() {
    if (!isExpanded && !isSubmitted) {
      isExpanded = true;
    }
  }
</script>

{#if isVisible}
  <div
    class="fixed bottom-6 right-6 z-50 flex flex-col items-end"
    in:fly={{ y: 50, duration: 600, easing: cubicOut }}
    out:fade
  >
    <!-- Main Widget Card -->
    <div
      class={cn(
        "relative bg-zinc-900 border border-zinc-800 rounded-2xl shadow-2xl transition-all duration-500 ease-in-out overflow-hidden",
        isExpanded
          ? "w-[500px] max-w-[95vw] h-auto p-6"
          : "w-[400px] h-[80px] p-5 cursor-pointer hover:border-amber-500/30",
      )}
      role="button"
      tabindex={!isExpanded && !isSubmitted ? 0 : -1}
      onclick={handleExpand}
      onkeydown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          if (!isExpanded && !isSubmitted) {
            e.preventDefault();
            handleExpand();
          }
        }
      }}
    >
      <!-- Close Button (Always Visible) -->
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
          <p class="text-zinc-200 text-sm font-medium">{randomQuote}</p>
        </div>
      {:else if !isExpanded}
        <!-- Nudge State -->
        <div class="flex flex-col justify-center h-full pr-6">
          <p class="text-zinc-200 text-base font-medium leading-tight">
            {selectedNudge.title}
          </p>
          <p class="text-zinc-500 text-xs">{selectedNudge.subtitle}</p>
        </div>
      {:else}
        <!-- Survey Form (Expanded) -->
        <div class="space-y-6" in:fade={{ delay: 200 }}>
          <!-- Progress Dots -->
          <div class="flex gap-1.5">
            {#each [1, 2, 3] as step}
              <div
                class={cn(
                  "h-1 rounded-full transition-all duration-300",
                  currentStep === step ? "w-6 bg-amber-500" : "w-2 bg-zinc-800",
                )}
              ></div>
            {/each}
          </div>

          {#if currentStep === 1}
            <div in:fly={{ x: 20, duration: 300 }}>
              <h4 class="text-white font-bold mb-1">Goals</h4>
              <p class="text-zinc-500 text-xs mb-4">
                What growth do you want? (Max 2)
              </p>
              <div class="grid grid-cols-2 gap-4">
                {#each goalOptions as option}
                  <button
                    class={cn(
                      "py-3 px-4 rounded-xl text-xs font-medium border transition-all",
                      goals.includes(option)
                        ? "bg-amber-500/10 border-amber-500 text-amber-500"
                        : "bg-zinc-800/50 border-zinc-800 text-zinc-400 hover:border-zinc-700",
                    )}
                    onclick={() => toggleGoal(option)}
                  >
                    {option}
                  </button>
                {/each}
              </div>
            </div>
          {:else if currentStep === 2}
            <div in:fly={{ x: 20, duration: 300 }}>
              <h4 class="text-white font-bold mb-1">Villains</h4>
              <p class="text-zinc-500 text-xs mb-4">
                Who broke your flow today?
              </p>
              <div class="grid grid-cols-2 gap-4">
                {#each painPointOptions as option}
                  <button
                    class={cn(
                      "py-3 px-4 rounded-xl text-xs font-medium border transition-all",
                      painPoints.includes(option)
                        ? "bg-amber-500/10 border-amber-500 text-amber-500"
                        : "bg-zinc-800/50 border-zinc-800 text-zinc-400 hover:border-zinc-700",
                    )}
                    onclick={() => togglePainPoint(option)}
                  >
                    {option}
                  </button>
                {/each}
              </div>
            </div>
          {:else if currentStep === 3}
            <div in:fly={{ x: 20, duration: 300 }}>
              <h4 class="text-white font-bold mb-1">Feedback</h4>
              <p class="text-zinc-500 text-xs mb-4">Any short & bold words?</p>
              <textarea
                bind:value={shortFeedback}
                maxlength="30"
                placeholder="Type here..."
                class="w-full bg-zinc-800/50 border border-zinc-800 rounded-xl p-3 text-sm text-white placeholder:text-zinc-600 focus:outline-none focus:border-amber-500/50 transition-colors resize-none h-20"
              ></textarea>
              <div class="flex justify-end mt-1">
                <span class="text-[10px] text-zinc-600"
                  >{shortFeedback.length}/30</span
                >
              </div>
            </div>
          {/if}

          <!-- Navigation -->
          <div class="flex items-center justify-between pt-2">
            {#if currentStep > 1}
              <button
                class="text-zinc-500 text-xs font-bold hover:text-white transition-colors"
                onclick={prevStep}
              >
                Back
              </button>
            {:else}
              <div></div>
            {/if}

            {#if currentStep < 3}
              <button
                class="bg-zinc-800 hover:bg-zinc-700 text-white px-4 py-2 rounded-lg text-xs font-bold transition-all"
                onclick={nextStep}
                disabled={currentStep === 1 && goals.length === 0}
              >
                Next
              </button>
            {:else}
              <button
                class="bg-amber-500 hover:bg-amber-400 disabled:bg-zinc-800 text-black disabled:text-zinc-600 px-6 py-2 rounded-lg text-xs font-bold transition-all flex items-center gap-2"
                onclick={handleSubmit}
                disabled={isSubmitting ||
                  (goals.length === 0 &&
                    painPoints.length === 0 &&
                    !shortFeedback)}
              >
                {#if isSubmitting}
                  <div
                    class="w-3 h-3 border-2 border-black/20 border-t-black rounded-full animate-spin"
                  ></div>
                {/if}
                Submit
              </button>
            {/if}
          </div>
        </div>
      {/if}
    </div>
  </div>
{/if}

<style>
  textarea {
    scrollbar-width: none;
  }
  textarea::-webkit-scrollbar {
    display: none;
  }
</style>
