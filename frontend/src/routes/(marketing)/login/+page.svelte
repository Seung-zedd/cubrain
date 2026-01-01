<script lang="ts">
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { user } from "$lib/stores/user.svelte";
  import LoginModal from "$lib/components/auth/LoginModal.svelte";

  onMount(() => {
    if (user.current) {
      goto("/library");
    }
  });
</script>

<div class="min-h-screen bg-background flex items-center justify-center p-4">
  <div class="w-full max-w-md">
    <LoginModal onclose={() => goto("/")} />
  </div>
</div>
