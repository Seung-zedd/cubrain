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
          class="absolute top-4 right-4 text-amber-500 text-[10px] font-black tracking-widest"
        >
          P.{card.page}
        </span>
      {:else}
        <div
          class="shrink-0 px-2 py-1 bg-amber-500/10 border border-amber-500/20 rounded text-amber-500 text-[10px] font-black tracking-widest"
        >
          P.{card.page}
        </div>
      {/if}
    {/if}

    <div class={cn("flex flex-col gap-2", viewMode === "list" ? "flex-1" : "")}>
      <span
        class={cn(
          "text-xs font-bold uppercase tracking-wider",
          viewMode === "grid" ? "text-amber-500" : "text-amber-500/80"
        )}
      >
        Question
      </span>
      <p
        class={cn(
          "text-white font-medium leading-relaxed",
          viewMode === "grid" ? "text-lg" : "text-base"
        )}
      >
        <Markdown text={card.question} />
      </p>
    </div>

    {#if viewMode === "grid"}
      <div class="flex flex-col gap-2">
        <span class="text-gray-500 text-xs font-bold uppercase tracking-wider">
          Answer
        </span>
        <p class="text-gray-300 text-base leading-relaxed">
          <Markdown text={card.answer} />
        </p>
      </div>
    {:else}
      <div class="flex flex-col gap-2 flex-1 border-l border-zinc-800 pl-4">
        <span class="text-gray-500 text-xs font-bold uppercase tracking-wider">
          Answer
        </span>
        <p class="text-gray-300 text-base leading-relaxed">
          <Markdown text={card.answer} />
        </p>
      </div>
    {/if}
  </div>
</div>
