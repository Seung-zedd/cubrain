<script lang="ts">
  import Markdown from "./Markdown.svelte";
  import { cn } from "$lib/utils";

  interface Card {
    question: string;
    answer: string;
    page?: number;
  }

  let {
    card,
    viewMode = "grid",
    showPageInfo = true,
  } = $props<{
    card: Card;
    viewMode?: "grid" | "list";
    showPageInfo?: boolean;
  }>();
</script>

<div
  class={cn(
    "rounded-xl p-px bg-linear-to-br from-[#fbbf24]/50 to-[#FFD700]/50 hover:from-[#fbbf24] hover:to-[#FFD700] transition-all duration-300 h-full",
    viewMode === "list" ? "w-full" : ""
  )}
>
  <div
    class={cn(
      "relative rounded-xl bg-[#1a1a1a] flex h-full",
      viewMode === "grid" ? "flex-col p-6 gap-6" : "items-start p-5 gap-4"
    )}
  >
    {#if showPageInfo && card.page}
      {#if viewMode === "grid"}
        <span
          class="absolute top-4 right-4 bg-amber-500/10 border border-amber-500/20 text-amber-500 text-sm font-bold px-2 py-0.5 rounded-md transition-all"
        >
          P.{card.page}
        </span>
      {:else}
        <div class="shrink-0 pt-1 w-16">
          <span
            class="px-2 py-1 bg-amber-500/10 border border-amber-500/20 rounded text-amber-500 text-[10px] font-black tracking-widest"
          >
            P.{card.page}
          </span>
        </div>
      {/if}
    {/if}

    {#if viewMode === "grid"}
      <div class="flex flex-col gap-2">
        <span class="text-xs font-bold uppercase tracking-wider text-amber-500">
          Question
        </span>
        <p class="text-white font-medium leading-relaxed text-lg">
          <Markdown text={card.question} />
        </p>
      </div>

      <div class="flex flex-col gap-2">
        <span class="text-gray-500 text-xs font-bold uppercase tracking-wider">
          Answer
        </span>
        <p class="text-gray-300 text-base leading-relaxed">
          <Markdown text={card.answer} />
        </p>
      </div>
    {:else}
      <div class="flex-1 grid grid-cols-1 md:grid-cols-10 gap-6 w-full">
        <!-- Question Box (40%) -->
        <div class="flex flex-col gap-2 md:col-span-4">
          <span
            class="text-xs font-bold uppercase tracking-wider text-amber-500/80"
          >
            Question
          </span>
          <p class="text-white font-medium leading-relaxed text-base">
            <Markdown text={card.question} />
          </p>
        </div>

        <!-- Answer Box (60%) -->
        <div
          class="flex flex-col gap-2 md:col-span-6 md:border-l md:border-zinc-800 md:pl-6"
        >
          <span
            class="text-gray-500 text-xs font-bold uppercase tracking-wider"
          >
            Answer
          </span>
          <p class="text-gray-300 text-base leading-relaxed">
            <Markdown text={card.answer} />
          </p>
        </div>
      </div>
    {/if}
  </div>
</div>

<style>
  div {
    -webkit-tap-highlight-color: transparent;
  }
</style>
