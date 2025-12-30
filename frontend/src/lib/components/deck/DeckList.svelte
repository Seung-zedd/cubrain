<script lang="ts">
  import DeckCard from "./DeckCard.svelte";

  interface Deck {
    id: number;
    title: string;
    cardCount: number;
    lastStudiedAt?: string;
    createdAt: string;
  }

  let { decks, onDelete, onEditCards, onUpdate } = $props<{
    decks: Deck[];
    onDelete?: (id: number) => void;
    onEditCards?: (deck: Deck) => void;
    onUpdate?: (id: number, updates: Partial<Deck>) => void;
  }>();
</script>

<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3 pb-4">
  {#each decks as deck (deck.id)}
    <DeckCard {deck} {onDelete} {onEditCards} {onUpdate} />
  {/each}
</div>
