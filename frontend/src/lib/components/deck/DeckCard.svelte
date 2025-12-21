<script lang="ts">
  import { Book, Zap, Clock } from "@lucide/svelte";
  import ProgressBar from "$lib/components/ui/ProgressBar.svelte";

  interface Deck {
    id: string;
    title: string;
    lastStudied: string;
    cardCount: number;
    progress?: number;
  }

  let { deck } = $props<{ deck: Deck }>();
</script>

<div
  class="relative group cursor-pointer hover:-translate-y-1 transition-transform duration-300"
>
  <!-- Stack Effect Layers -->
  <div
    class="absolute inset-0 bg-zinc-800 rounded-xl translate-x-2 translate-y-2 opacity-40 transition-transform group-hover:translate-x-3 group-hover:translate-y-3"
  ></div>
  <div
    class="absolute inset-0 bg-zinc-700 rounded-xl translate-x-1 translate-y-1 opacity-70 transition-transform group-hover:translate-x-1.5 group-hover:translate-y-1.5"
  ></div>

  <!-- Main Card -->
  <div
    class="relative bg-[#1a1a1a] border border-zinc-700 hover:border-[#FFD700] rounded-xl p-4 flex gap-4 shadow-xl z-10 h-full transition-colors"
  >
    <!-- Thumbnail -->
    <div
      class="shrink-0 w-20 bg-linear-to-br from-zinc-800 to-zinc-700 rounded-lg flex items-center justify-center border border-zinc-700/50"
    >
      <Book
        class="w-8 h-8 text-zinc-500 group-hover:text-[#FFD700] transition-colors"
      />
    </div>

    <!-- Metadata -->
    <div class="flex flex-col justify-between grow min-w-0 py-1">
      <div>
        <h3
          class="text-lg font-bold text-zinc-100 group-hover:text-[#FFD700] transition-colors truncate"
          title={deck.title}
        >
          {deck.title}
        </h3>
        <div class="flex items-center gap-2 mt-1 text-xs text-zinc-500">
          <Clock class="w-3 h-3" />
          <span>{deck.lastStudied}</span>
        </div>
      </div>

      <div class="space-y-2 mt-3">
        <!-- Progress Bar (Visual only for now if no real progress) -->
        <div class="h-1.5 w-full bg-zinc-800 rounded-full overflow-hidden">
          <div
            class="h-full bg-[#FFD700] rounded-full"
            style="width: {deck.progress || 0}%"
          ></div>
        </div>

        <div class="flex items-center justify-between">
          <span class="text-xs text-zinc-400 font-medium">
            {deck.progress || 0}% Complete
          </span>
          <div
            class="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-zinc-800 border border-zinc-700"
          >
            <Zap class="w-3 h-3 text-[#FFD700]" />
            <span class="text-xs font-bold text-zinc-300">{deck.cardCount}</span
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
