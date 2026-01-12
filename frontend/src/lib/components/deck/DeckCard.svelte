<script lang="ts">
  import Book from "@lucide/svelte/icons/book";
  import Zap from "@lucide/svelte/icons/zap";
  import Clock from "@lucide/svelte/icons/clock";
  import EllipsisVertical from "@lucide/svelte/icons/ellipsis-vertical";
  import Pencil from "@lucide/svelte/icons/pencil";
  import Trash2 from "@lucide/svelte/icons/trash-2";
  import FileDown from "@lucide/svelte/icons/file-down";
  import { getRelativeTime, cn } from "$lib/utils";
  import { authFetch } from "$lib/api";

  interface Deck {
    id: number;
    title: string;
    cardCount: number;
    lastStudiedAt?: string;
    studyProgress?: number;
    createdAt: string;
    page?: number;
  }

  let {
    deck,
    onDelete,
    onEditCards,
    viewMode = "grid",
    showPageInfo = true,
  } = $props<{
    deck: Deck;
    onDelete?: (id: number) => void;
    onEditCards?: (deck: Deck) => void;
    viewMode?: "grid" | "list";
    showPageInfo?: boolean;
  }>();

  let showMenu = $state(false);
  let menuRef = $state<HTMLDivElement | null>(null);

  function toggleMenu(e: MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    showMenu = !showMenu;
  }

  function handleClickOutside(e: MouseEvent) {
    if (showMenu && menuRef && !menuRef.contains(e.target as Node)) {
      showMenu = false;
    }
  }

  async function handleExport(e: MouseEvent) {
    e.stopPropagation();
    showMenu = false;

    try {
      const response = await authFetch(`/api/v1/decks/${deck.id}/export/anki`);
      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;

        // Try to get filename from Content-Disposition header
        const contentDisposition = response.headers.get("Content-Disposition");
        let filename = "";
        if (contentDisposition) {
          const filenameStarMatch = contentDisposition.match(
            /filename\*=UTF-8''([^;]+)/
          );
          if (filenameStarMatch) {
            filename = decodeURIComponent(filenameStarMatch[1]);
          } else {
            const filenameMatch = contentDisposition.match(
              /filename="?([^";]+)"?/
            );
            if (filenameMatch) {
              filename = filenameMatch[1];
            }
          }
        }

        // Fallback to deck title if header parsing fails
        if (!filename) {
          filename = deck.title.replace(/[\\/:*?"<>|]/g, "_") + "_anki.csv";
        }

        a.download = filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to export deck:", error);
      }
    }
  }

  $effect(() => {
    if (showMenu) {
      window.addEventListener("click", handleClickOutside);
      return () => window.removeEventListener("click", handleClickOutside);
    }
  });
</script>

<div class="relative group">
  <a
    href="/study/{deck.id}"
    class="relative block cursor-pointer hover:-translate-y-1 transition-transform duration-300 outline-none ring-0"
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
        <div class="relative">
          <h3
            class="w-full text-left text-lg font-bold text-zinc-100 group-hover:text-[#FFD700] transition-colors truncate pr-8 block"
            title={deck.title}
          >
            {deck.title}
          </h3>

          {#if showPageInfo && deck.page}
            <span
              class={cn(
                "absolute bg-amber-500/10 border border-amber-500/20 text-amber-500 text-sm font-bold px-2 py-0.5 rounded-md transition-all",
                viewMode === "grid" ? "top-0 right-0" : "top-0 right-0"
              )}
            >
              P.{deck.page}
            </span>
          {/if}

          <div class="flex items-center gap-2 mt-1 text-xs text-zinc-500">
            <Clock class="w-3 h-3" />
            <span>{getRelativeTime(deck.lastStudiedAt || null)}</span>
          </div>
        </div>

        <div class="space-y-2 mt-3">
          <!-- Progress Bar -->
          <div class="h-1.5 w-full bg-zinc-800 rounded-full overflow-hidden">
            <div
              class="h-full bg-[#FFD700] rounded-full"
              style="width: {deck.studyProgress || 0}%"
            ></div>
          </div>

          <div class="flex items-center justify-between">
            <span class="text-xs text-zinc-400 font-medium">
              Created {new Date(deck.createdAt).toLocaleDateString()}
            </span>
            <div
              class="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-zinc-800 border border-zinc-700"
            >
              <Zap class="w-3 h-3 text-[#FFD700]" />
              <span class="text-xs font-bold text-zinc-300"
                >{deck.cardCount}</span
              >
            </div>
          </div>
        </div>
      </div>
    </div>
  </a>

  <!-- Kebab Menu -->
  {#if onDelete || onEditCards}
    <div class="absolute top-6 right-6 z-20" bind:this={menuRef}>
      <button
        onclick={toggleMenu}
        class="p-1.5 rounded-lg hover:bg-zinc-800 text-zinc-500 hover:text-white transition-colors outline-none ring-0"
      >
        <EllipsisVertical class="w-5 h-5" />
      </button>

      {#if showMenu}
        <div
          class="absolute right-0 mt-2 w-56 bg-zinc-900 border border-zinc-800 rounded-xl shadow-2xl py-2 z-30 animate-in fade-in zoom-in-95 duration-200"
        >
          {#if onEditCards}
            <button
              onclick={(e) => {
                e.stopPropagation();
                showMenu = false;
                onEditCards(deck);
              }}
              class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:bg-zinc-800 hover:text-white transition-colors outline-none ring-0"
            >
              <Pencil class="w-4 h-4" />
              Edit Cards
            </button>
          {/if}

          <button
            onclick={handleExport}
            class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:bg-zinc-800 hover:text-white transition-colors outline-none ring-0"
          >
            <FileDown class="w-4 h-4" />
            Export to Anki (.csv)
          </button>

          {#if onDelete}
            <button
              onclick={(e) => {
                e.stopPropagation();
                showMenu = false;
                onDelete(deck.id);
              }}
              class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:bg-red-500/10 hover:text-red-300 transition-colors outline-none ring-0"
            >
              <Trash2 class="w-4 h-4" />
              Delete Deck
            </button>
          {/if}
        </div>
      {/if}
    </div>
  {/if}
</div>

<style>
  a,
  button {
    -webkit-tap-highlight-color: transparent;
  }
</style>
