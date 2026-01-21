<script lang="ts">
  import { fade, fly } from "svelte/transition";
  import { cubicOut } from "svelte/easing";
  import X from "@lucide/svelte/icons/x";
  import Check from "@lucide/svelte/icons/check";
  import ExternalLink from "@lucide/svelte/icons/external-link";
  import { cn } from "$lib/utils";

  interface Props {
    onClose: () => void;
  }

  let { onClose }: Props = $props();

  let isVisible = $state(true);
  let isExpanded = $state(false);

  const steps = [
    {
      title: "Open Anki",
      desc: "Launch the Anki app on your computer.",
    },
    {
      title: "Import File",
      desc: "Click 'Import File' at the bottom or go to File > Import.",
    },
    {
      title: "Select CSV",
      desc: "Choose the .csv file you just downloaded from Cubrain.",
    },
    {
      title: "Map Fields",
      desc: "Ensure 'Field 1' is Question and 'Field 2' is Answer.",
    },
  ];

  function handleClose() {
    isVisible = false;
    setTimeout(onClose, 300);
  }

  function handleExpand() {
    isExpanded = true;
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
        isExpanded
          ? "w-[450px] max-w-[95vw] h-auto p-6"
          : "w-[320px] h-[80px] p-5 cursor-pointer hover:border-amber-500/30",
      )}
      role="button"
      tabindex={isExpanded ? -1 : 0}
      onclick={handleExpand}
      onkeydown={(e) => e.key === "Enter" && handleExpand()}
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

      {#if !isExpanded}
        <div class="flex items-center gap-3 h-full" in:fade>
          <div
            class="w-10 h-10 rounded-full bg-amber-500/10 flex items-center justify-center shrink-0"
          >
            <ExternalLink class="w-5 h-5 text-amber-500" />
          </div>
          <div>
            <p class="text-zinc-200 text-sm font-bold leading-tight">
              Anki Import Guide
            </p>
            <p class="text-zinc-400 text-xs">Click to see how to import .csv</p>
          </div>
        </div>
      {:else}
        <div class="space-y-6" in:fade={{ delay: 200 }}>
          <div>
            <h3 class="text-xl font-bold text-white flex items-center gap-2">
              <span class="text-amber-500">Anki</span> Import Guide
            </h3>
            <p class="text-zinc-400 text-sm mt-1">
              Follow these steps to load your flashcards.
            </p>
          </div>

          <div class="space-y-4">
            {#each steps as step, i}
              <div
                class="flex gap-4"
                in:fly={{ x: 10, duration: 400, delay: 300 + i * 100 }}
              >
                <div class="flex flex-col items-center">
                  <div
                    class="w-6 h-6 rounded-full bg-amber-500 text-black text-[10px] font-black flex items-center justify-center shrink-0"
                  >
                    {i + 1}
                  </div>
                  {#if i < steps.length - 1}
                    <div class="w-px h-full bg-zinc-800 my-1"></div>
                  {/if}
                </div>
                <div class="pb-2">
                  <p class="text-zinc-200 text-sm font-bold">{step.title}</p>
                  <p class="text-zinc-300 text-xs mt-0.5 leading-relaxed">
                    {step.desc}
                  </p>
                </div>
              </div>
            {/each}
          </div>

          <button
            onclick={handleClose}
            class="w-full py-3 rounded-xl bg-amber-500 text-black font-black text-sm hover:bg-amber-400 transition-all shadow-lg shadow-amber-500/10 flex items-center justify-center gap-2"
          >
            <Check class="w-4 h-4" />
            Got it, thanks!
          </button>
        </div>
      {/if}
    </div>
  </div>
{/if}
