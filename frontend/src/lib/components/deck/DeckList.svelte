<script lang="ts">
  import DeckCard from "./DeckCard.svelte";

  interface Deck {
    id: number;
    title: string;
    cardCount: number;
    lastStudiedAt?: string;
    createdAt: string;
  }

  let {
    decks,
    onDelete,
    onEditCards,
    viewMode = "grid",
  } = $props<{
    decks: Deck[];
    onDelete?: (id: number) => void;
    onEditCards?: (deck: Deck) => void;
    viewMode?: "grid" | "list";
  }>();
</script>

<div
  class={viewMode === "grid"
    ? "grid gap-6 md:grid-cols-2 lg:grid-cols-3 pb-4"
    : "flex flex-col gap-4 max-w-3xl mx-auto pb-4 w-full"}
>
  {#each decks as deck (deck.id)}
    <DeckCard {deck} {onDelete} {onEditCards} {viewMode} />
  {/each}
</div>
