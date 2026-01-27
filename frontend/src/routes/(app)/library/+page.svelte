<script lang="ts">
  import { onMount } from "svelte";
  import { authFetch } from "$lib/api";
  import { page } from "$app/state";
  import LibraryDashboard from "$lib/components/study/LibraryDashboard.svelte";
  import { goto } from "$app/navigation";
  import { uiState } from "$lib/stores/ui.svelte";
  import EditDeckModal from "$lib/components/deck/EditDeckModal.svelte";
  import ConfirmModal from "$lib/components/ui/ConfirmModal.svelte";
  import PostStudyNudge from "$lib/components/ui/PostStudyNudge.svelte";
  import { IS_DEV_MODE } from "$lib/utils/env";

  let isSessionEnded = $derived(
    page.url.searchParams.get("session_ended") === "true",
  );

  let decks = $state<any[]>([]);
  let isLoading = $state(true);
  let searchQuery = $state("");
  let selectedDeck = $state<any | null>(null);
  let showEditModal = $state(false);

  // Deletion state
  let deckToDelete = $state<number | null>(null);

  onMount(async () => {
    // Reset study mode when entering library
    uiState.setStudyMode(false);

    try {
      const response = await authFetch("/api/v1/decks");
      if (response.ok) {
        const data = await response.json();
        decks = data.content;
      }
    } catch (error) {
      if (IS_DEV_MODE) {
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
      if (IS_DEV_MODE) {
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
      if (IS_DEV_MODE) {
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

  function handleStartStudy(deckId: number) {
    goto(`/study/${deckId}`);
  }
</script>

<svelte:head>
  <link
    rel="preload"
    as="image"
    href="/images/library-bg.jpg"
    fetchpriority="high"
  />
</svelte:head>

<!-- Atmospheric Background (Rendered immediately for better LCP) -->
<div class="bg-overlay fixed inset-0 z-0"></div>

<LibraryDashboard
  {decks}
  {isLoading}
  onStartStudy={handleStartStudy}
  onDelete={(id: number) => (deckToDelete = id)}
  onEditCards={handleEditCards}
/>

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

{#if isSessionEnded}
  <PostStudyNudge />
{/if}

<style>
  .bg-overlay {
    background-image: url("/images/library-bg.jpg");
    background-size: cover;
    background-position: center;
    background-color: rgba(0, 0, 0, 0.75);
    background-blend-mode: overlay;
  }
</style>
