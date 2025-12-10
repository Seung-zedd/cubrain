<script lang="ts">
  let { children } = $props();
</script>

<div class="dark min-h-screen bg-background text-foreground antialiased">
  {@render children()}
</div>
