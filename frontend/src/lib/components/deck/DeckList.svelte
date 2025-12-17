<script lang="ts">
  import DeckCard from "./DeckCard.svelte";

  interface Deck {
    id: string;
    title: string;
    lastStudied: string;
    cardCount: number;
    progress?: number;
  }

  let { decks } = $props<{ decks: Deck[] }>();
</script>

<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3 pb-4">
  {#each decks as deck (deck.id)}
    <DeckCard {deck} />
  {/each}
</div>
