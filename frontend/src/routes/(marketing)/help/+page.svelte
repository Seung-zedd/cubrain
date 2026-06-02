<script lang="ts">
  import HelpContent from "$lib/components/help/HelpContent.svelte";
</script>

<svelte:head>
  <title>Help Center | Cubrain</title>
  <meta
    name="description"
    content="Cubrain Help Center and Frequently Asked Questions."
  />
</svelte:head>

<div
  class="min-h-screen bg-black text-white/80 font-light tracking-tight py-20 px-6"
>
  <HelpContent showBackButton={true} />
</div>
