<script lang="ts">
  import { onMount } from "svelte";
  import { authFetch } from "$lib/api";
  import DeckList from "$lib/components/deck/DeckList.svelte";
  import { Library as LibraryIcon, Plus, Search } from "@lucide/svelte";
  import { fade } from "svelte/transition";
  import EditDeckModal from "$lib/components/deck/EditDeckModal.svelte";
  import ConfirmModal from "$lib/components/ui/ConfirmModal.svelte";

  let decks = $state<any[]>([]);
  let isLoading = $state(true);
  let searchQuery = $state("");
  let selectedDeck = $state<any | null>(null);
  let showEditModal = $state(false);

  // Deletion state
  let deckToDelete = $state<number | null>(null);

  let filteredDecks = $derived(
    decks.filter((deck) =>
      deck.title.toLowerCase().includes(searchQuery.toLowerCase())
    )
  );

  onMount(async () => {
    try {
      const response = await authFetch("/api/v1/decks");
      if (response.ok) {
        const data = await response.json();
        decks = data.content;
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to fetch decks:", error);
      }
    } finally {
      isLoading = false;
    }
  });

  async function confirmDelete() {
    if (!deckToDelete) return;
    const id = deckToDelete;
    deckToDelete = null;

    try {
      const response = await authFetch(`/api/v1/decks/${id}`, {
        method: "DELETE",
      });
      if (response.ok) {
        decks = decks.filter((d) => d.id !== id);
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to delete deck:", error);
      }
    }
  }

  async function handleEditCards(deck: any) {
    try {
      const response = await authFetch(`/api/v1/decks/${deck.id}/cards`);
      if (response.ok) {
        const cards = await response.json();
        selectedDeck = { ...deck, cards };
        showEditModal = true;
      }
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error("Failed to fetch deck cards:", error);
      }
    }
  }

  function handleSaveCards(updatedTitle: string, updatedCards: any[]) {
    if (selectedDeck) {
      const deckIndex = decks.findIndex((d) => d.id === selectedDeck.id);
      if (deckIndex !== -1) {
        decks[deckIndex].title = updatedTitle;
        decks[deckIndex].cardCount = updatedCards.length;
      }
    }
  }
</script>

<div class="space-y-8" in:fade>
  <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
    <div>
      <h1
        class="text-3xl font-bold text-white tracking-tight flex items-center gap-3"
      >
        <LibraryIcon class="w-8 h-8 text-amber-500" />
        Library
      </h1>
      <p class="text-zinc-400 mt-2">
        Manage and study your flashcard collections.
      </p>
    </div>
    <a
      href="/upload"
      class="flex items-center justify-center gap-2 px-6 py-2.5 bg-amber-500 hover:bg-amber-400 text-black font-bold rounded-lg transition-all shadow-[0_0_20px_rgba(245,158,11,0.2)]"
    >
      <Plus class="w-5 h-5" />
      Create New Deck
    </a>
  </div>

  <div class="relative">
    <Search
      class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-zinc-500"
    />
    <input
      type="text"
      bind:value={searchQuery}
      placeholder="Search your library..."
      class="w-full bg-zinc-900 border border-zinc-800 rounded-xl py-3 pl-12 pr-4 text-white placeholder:text-zinc-600 focus:outline-none focus:ring-2 focus:ring-amber-500/50 focus:border-amber-500/50 transition-all"
    />
  </div>

  {#if isLoading}
    <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      {#each Array(6) as _}
        <div
          class="h-40 bg-zinc-900/50 rounded-xl animate-pulse border border-zinc-800"
        ></div>
      {/each}
    </div>
  {:else if filteredDecks.length > 0}
    <DeckList
      decks={filteredDecks}
      onDelete={(id) => (deckToDelete = id)}
      onEditCards={handleEditCards}
    />
  {:else}
    <div
      class="flex flex-col items-center justify-center py-20 text-center space-y-4"
    >
      <div
        class="w-20 h-20 rounded-full bg-zinc-900 flex items-center justify-center border border-zinc-800"
      >
        <LibraryIcon class="w-10 h-10 text-zinc-700" />
      </div>
      <h2 class="text-xl font-bold text-white">No decks found</h2>
      <p class="text-zinc-500 max-w-xs">
        {searchQuery
          ? `No decks match "${searchQuery}"`
          : "You haven't created any decks yet. Start by uploading a PDF!"}
      </p>
      {#if !searchQuery}
        <a href="/upload" class="mt-4 text-amber-500 font-bold hover:underline">
          Upload a PDF to get started →
        </a>
      {/if}
    </div>
  {/if}
</div>

{#if showEditModal && selectedDeck}
  <EditDeckModal
    deck={selectedDeck}
    onclose={() => (showEditModal = false)}
    onsave={handleSaveCards}
  />
{/if}

{#if deckToDelete !== null}
  <ConfirmModal
    title="Delete Deck"
    message="Are you sure you want to delete this deck? All associated flashcards will be permanently removed."
    confirmText="Delete Deck"
    onconfirm={confirmDelete}
    oncancel={() => (deckToDelete = null)}
  />
{/if}
