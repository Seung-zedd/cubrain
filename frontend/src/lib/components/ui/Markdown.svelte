<script lang="ts">
  let { text = "" } = $props<{ text?: string }>();

  // Helper to parse text into tokens for safe rendering
  let tokens = $derived.by(() => {
    if (!text) return [];

    // 1. Handle Anki-style cloze deletions: {{c1::text}} -> ______
    // We replace them with a special marker that we can split by later if needed,
    // but for now, the requirement is just to show "______".
    let processed = text.replace(/\{\{c\d+::(.*?)\}\}/g, "______");

    // 2. Split by Markdown Bold markers: **text**
    // We use a capturing group so the markers are included in the split array
    const parts = processed.split(/(\*\*.*?\*\*)/g);

    return parts.map((part: string) => {
      if (part.startsWith("**") && part.endsWith("**")) {
        return {
          type: "bold",
          content: part.slice(2, -2),
        };
      }
      return {
        type: "text",
        content: part,
      };
    });
  });
</script>

<span class="whitespace-pre-wrap">
  {#each tokens as token}
    {#if token.type === "bold"}
      <strong class="text-amber-400 font-bold">{token.content}</strong>
    {:else}
      {token.content}
    {/if}
  {/each}
</span>
