<script lang="ts">
  import { onMount } from "svelte";
  import { uiState } from "$lib/stores/ui.svelte";
  import AppSidebar from "$lib/components/layout/AppSidebar.svelte";
  import Menu from "@lucide/svelte/icons/menu";
  import PanelLeft from "@lucide/svelte/icons/panel-left";
  import { fade, fly } from "svelte/transition";
  import { cn } from "$lib/utils";
  import LoginModal from "$lib/components/auth/LoginModal.svelte";

  let { children } = $props();
  let isSidebarOpen = $state(false); // Default closed to avoid flash on mobile
  let showLoginModal = $state(false);

  onMount(() => {
    // Default open on desktop
    if (window.innerWidth >= 768) {
      isSidebarOpen = true;
    }
  });

  function toggleSidebar() {
    isSidebarOpen = !isSidebarOpen;
  }

  function handleKeydown(e: KeyboardEvent) {
    // Handle Escape
    if (e.key === "Escape") {
      if (showLoginModal) {
        showLoginModal = false;
      } else if (isSidebarOpen && window.innerWidth < 768) {
        isSidebarOpen = false;
      }
      return;
    }

    // Handle Sidebar Toggle (Ctrl + . or Cmd + .)
    const isTyping = ["INPUT", "TEXTAREA"].includes(
      (e.target as HTMLElement)?.tagName
    );
    if ((e.ctrlKey || e.metaKey) && e.key === "." && !isTyping) {
      e.preventDefault();
      toggleSidebar();
    }
  }
</script>

<svelte:window onkeydown={handleKeydown} />

<div
  class="flex h-screen w-full bg-zinc-950 text-zinc-100 font-sans selection:bg-amber-500/30 overflow-hidden"
>
  <!-- Mobile Sidebar Overlay -->
  {#if isSidebarOpen}
    <div
      class="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm md:hidden"
      onclick={toggleSidebar}
      onkeydown={(e) => e.key === "Enter" && toggleSidebar()}
      role="button"
      tabindex="0"
      aria-label="Close Menu"
      transition:fade={{ duration: 200 }}
    ></div>
  {/if}

  <!-- Sidebar Container -->
  {#if !uiState.isStudyMode}
    <aside
      class={cn(
        "fixed md:relative z-50 h-full bg-zinc-900 border-r border-zinc-800 transition-all duration-300 ease-in-out shrink-0",
        isSidebarOpen
          ? "translate-x-0 w-64"
          : "-translate-x-full md:translate-x-0 md:w-0 md:overflow-hidden md:border-none"
      )}
    >
      <div class="w-64 h-full">
        <AppSidebar
          onNavItemClick={() => {
            if (window.innerWidth < 768) isSidebarOpen = false;
          }}
          onLoginClick={() => (showLoginModal = true)}
        />
      </div>
    </aside>
  {/if}

  <!-- Main Content Area -->
  <div class="flex flex-1 flex-col min-w-0 overflow-hidden">
    <!-- Header with Toggle Button -->
    {#if !uiState.isStudyMode}
      <header class="flex h-16 items-center px-4 shrink-0 z-30">
        <div class="relative group/toggle">
          <button
            onclick={toggleSidebar}
            class="p-2 rounded-lg hover:bg-zinc-800 text-zinc-400 hover:text-white transition-colors"
            aria-label="Toggle Sidebar"
          >
            <div class="md:hidden">
              <Menu class="h-6 w-6" />
            </div>
            <div class="hidden md:block">
              <PanelLeft class="h-5 w-5" />
            </div>
          </button>

          <!-- Tooltip -->
          <div
            class="absolute left-full ml-3 top-1/2 -translate-y-1/2 px-3.5 py-2 bg-zinc-950/90 backdrop-blur-md border border-zinc-800 rounded-lg text-sm font-medium text-zinc-200 whitespace-nowrap opacity-0 group-hover/toggle:opacity-100 pointer-events-none transition-all duration-200 translate-x-[-4px] group-hover/toggle:translate-x-0 z-50 shadow-[0_0_20px_rgba(0,0,0,0.5)] flex items-center gap-4"
          >
            <span>{isSidebarOpen ? "Close sidebar" : "Open sidebar"}</span>
            <kbd
              class="px-2 py-0.5 rounded bg-amber-500/10 border border-amber-500/20 text-amber-500 font-mono text-xs font-bold tracking-tight"
            >
              Ctrl + .
            </kbd>
          </div>
        </div>

        <!-- Mobile Logo (Visible when sidebar is closed) -->
        {#if !isSidebarOpen}
          <a href="/" class="ml-2 md:hidden">
            <img src="/logo-gold.png" alt="Cubrain" class="h-8 w-auto" />
          </a>
        {/if}
      </header>
    {/if}

    <!-- Main Content -->
    <main
      class={cn(
        "flex-1 overflow-y-auto bg-zinc-950",
        !uiState.isStudyMode && "p-4 md:p-8"
      )}
    >
      <div class={cn("mx-auto", !uiState.isStudyMode && "max-w-7xl")}>
        {@render children()}
      </div>
    </main>
  </div>
</div>

{#if showLoginModal}
  <LoginModal onclose={() => (showLoginModal = false)} />
{/if}
