<script lang="ts">
  import AppSidebar from "$lib/components/layout/AppSidebar.svelte";
  import { Menu } from "@lucide/svelte";
  import { fade, fly } from "svelte/transition";

  let { children } = $props();
  let isMobileMenuOpen = $state(false);

  function toggleMobileMenu() {
    isMobileMenuOpen = !isMobileMenuOpen;
  }
</script>

<svelte:window
  onkeydown={(e) => {
    if (e.key === "Escape") {
      isMobileMenuOpen = false;
    }
  }}
/>

<div
  class="flex h-screen w-full bg-zinc-950 text-zinc-100 font-sans selection:bg-amber-500/30"
>
  <!-- Desktop Sidebar -->
  <div class="hidden md:block">
    <AppSidebar />
  </div>

  <div class="flex flex-1 flex-col overflow-hidden">
    <!-- Mobile Header -->
    <header
      class="flex h-16 items-center justify-between border-b border-zinc-800 bg-zinc-900 px-4 md:hidden"
    >
      <a href="/" class="flex items-center gap-2">
        <img src="/logo-gold.png" alt="Cubrain" class="h-8 w-auto" />
      </a>
      <button
        class="rounded-md p-2 text-zinc-400 hover:bg-zinc-800 hover:text-white transition-colors"
        onclick={toggleMobileMenu}
        aria-label="Toggle Menu"
      >
        <Menu class="h-6 w-6" />
      </button>
    </header>

    <!-- Mobile Sidebar Overlay -->
    {#if isMobileMenuOpen}
      <div
        class="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm md:hidden"
        onclick={toggleMobileMenu}
        onkeydown={(e) => e.key === "Enter" && toggleMobileMenu()}
        role="button"
        tabindex="0"
        aria-label="Close Menu"
        transition:fade={{ duration: 200 }}
      ></div>

      <div
        class="fixed inset-y-0 left-0 z-50 w-64 md:hidden"
        transition:fly={{ x: -256, duration: 300, opacity: 1 }}
      >
        <AppSidebar onNavItemClick={() => (isMobileMenuOpen = false)} />
      </div>
    {/if}

    <!-- Main Content -->
    <main class="flex-1 overflow-y-auto bg-zinc-950 p-4 md:p-8">
      <div class="mx-auto max-w-7xl">
        {@render children()}
      </div>
    </main>
  </div>
</div>
