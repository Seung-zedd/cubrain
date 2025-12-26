<script lang="ts">
  import { page } from "$app/state";
  import {
    LayoutDashboard,
    CloudUpload,
    Library,
    Settings,
    LogOut,
  } from "@lucide/svelte";
  import { cn } from "$lib/utils";
  import { user, logout } from "$lib/stores/user";
  import LoginModal from "$lib/components/auth/LoginModal.svelte";

  let showLoginModal = $state(false);

  const navItems = [
    { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { href: "/dashboard?mode=upload", label: "Upload PDF", icon: CloudUpload },
    { href: "/library", label: "Library", icon: Library },
    { href: "/settings", label: "Settings", icon: Settings },
  ];
</script>

<aside
  class="hidden h-screen w-64 flex-col border-r border-zinc-800 bg-zinc-900 md:flex"
>
  <!-- Brand Logo -->
  <div class="flex h-16 items-center px-6 border-b border-zinc-800/50">
    <a href="/" class="flex items-center gap-2">
      <img src="/logo-gold.png" alt="Cubrain" class="h-8 w-auto" />
    </a>
  </div>

  <!-- Navigation -->
  <nav class="flex-1 space-y-1 px-3 py-4">
    {#each navItems as item}
      {@const isActive = item.href.includes("?")
        ? page.url.search === new URL("http://dummy" + item.href).search
        : page.url.pathname === item.href && page.url.search === ""}
      <a
        href={item.href}
        class={cn(
          "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200",
          isActive
            ? "bg-amber-500/10 text-amber-500 shadow-[0_0_10px_rgba(245,158,11,0.1)]"
            : "text-zinc-400 hover:bg-zinc-800/50 hover:text-zinc-100"
        )}
      >
        <item.icon
          class={cn("h-5 w-5", isActive ? "text-amber-500" : "text-zinc-500")}
        />
        {item.label}
      </a>
    {/each}
  </nav>

  <!-- User Profile -->
  <div class="border-t border-zinc-800 p-4">
    <div
      onclick={() => !$user && (showLoginModal = true)}
      class="flex items-center gap-3 rounded-lg bg-zinc-950/50 p-3 border border-zinc-800 hover:border-zinc-700 transition-colors cursor-pointer group"
      role="button"
      tabindex="0"
      onkeydown={(e) => e.key === "Enter" && !$user && (showLoginModal = true)}
    >
      <div
        class={cn(
          "h-10 w-10 rounded-full flex items-center justify-center font-bold shadow-lg transition-all duration-300",
          $user
            ? "bg-linear-to-br from-orange-400 to-orange-600 text-black shadow-[0_0_20px_rgba(249,115,22,0.4)]"
            : "bg-linear-to-br from-zinc-700 to-zinc-800 text-zinc-400 group-hover:from-orange-500 group-hover:to-orange-700 group-hover:text-black"
        )}
      >
        {$user ? $user.email.substring(0, 2).toUpperCase() : "G"}
      </div>
      <div class="flex-1 overflow-hidden">
        <p
          class="truncate text-sm font-medium text-zinc-200 group-hover:text-white"
        >
          {$user ? $user.email.split("@")[0] : "Guest Mode"}
        </p>
        <p
          class="truncate text-xs text-zinc-500 group-hover:text-orange-500/80 transition-colors"
        >
          {$user ? $user.email : "Sign in to save progress"}
        </p>
      </div>
      {#if $user}
        <button
          onclick={(e) => {
            e.stopPropagation();
            logout();
          }}
          class="p-1 hover:bg-zinc-800 rounded-full transition-colors"
          aria-label="Logout"
        >
          <LogOut
            class="h-4 w-4 text-zinc-500 hover:text-red-400 transition-colors"
          />
        </button>
      {/if}
    </div>
  </div>
</aside>

{#if showLoginModal}
  <LoginModal onclose={() => (showLoginModal = false)} />
{/if}
