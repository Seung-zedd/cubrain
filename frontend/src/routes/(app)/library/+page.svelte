<script lang="ts">
  import { onMount } from "svelte";
  import { authFetch } from "$lib/api";
  import { page } from "$app/state";
  import LibraryDashboard from "$lib/components/study/LibraryDashboard.svelte";
  import { goto } from "$app/navigation";
  import { uiState } from "$lib/stores/ui.svelte";
  import EditDeckModal from "$lib/components/deck/EditDeckModal.svelte";
  import ConfirmModal from "$lib/components/ui/ConfirmModal.svelte";
  import LearnersVoice from "$lib/components/ui/LearnersVoice.svelte";
  import type { PageData } from "./$types";
  import { IS_DEV_MODE } from "$lib/utils/env";
  import { invalidateAll } from "$app/navigation";

  let { data } = $props<{ data: PageData }>();

  let isSessionEnded = $derived(
    page.url.searchParams.get("session_ended") === "true",
  );

  let decks = $state<any[]>([]);
  let selectedDeck = $state<any | null>(null);
  let showEditModal = $state(false);
  let deckToDelete = $state<number | null>(null);

  $effect(() => {
    data.streamed.decks.then((res: any) => {
      decks = res.content || [];
    });
  });

  onMount(() => {
    // Reset study mode when entering library
    uiState.setStudyMode(false);
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

{#await data.streamed.decks}
  <LibraryDashboard
    decks={[]}
    isLoading={true}
    onStartStudy={handleStartStudy}
    onDelete={(id: number) => (deckToDelete = id)}
    onEditCards={handleEditCards}
  />
{:then decksData}
  <LibraryDashboard
    decks={decksData.content}
    isLoading={false}
    onStartStudy={handleStartStudy}
    onDelete={(id: number) => (deckToDelete = id)}
    onEditCards={handleEditCards}
  />
{:catch error}
  <div class="flex items-center justify-center min-h-[60vh] text-red-500">
    Failed to load library: {error.message}
  </div>
{/await}

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
  <LearnersVoice />
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
