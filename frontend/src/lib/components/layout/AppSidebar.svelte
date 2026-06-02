<script lang="ts">
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import CloudUpload from "@lucide/svelte/icons/cloud-upload";
  import Library from "@lucide/svelte/icons/library";
  import Settings from "@lucide/svelte/icons/settings";
  import HelpCircle from "@lucide/svelte/icons/help-circle";
  import LogOut from "@lucide/svelte/icons/log-out";
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import CreditCard from "@lucide/svelte/icons/credit-card";
  import { cn } from "$lib/utils";
  import { user, logout } from "$lib/stores/user.svelte";

  let {
    class: className,
    onNavItemClick,
    onLoginClick,
  } = $props<{
    class?: string;
    onNavItemClick?: () => void;
    onLoginClick?: () => void;
  }>();

  const CURRENT_VERSION = "1.1.0";
  let isDropdownOpen = $state(false);
  let hasNewUpdate = $state(false);

  onMount(() => {
    const seenVersion = localStorage.getItem("cubrain_whats_new_seen");
    hasNewUpdate = seenVersion !== CURRENT_VERSION;
  });

  function markAsSeen() {
    localStorage.setItem("cubrain_whats_new_seen", CURRENT_VERSION);
    hasNewUpdate = false;
    isDropdownOpen = false;
  }

  const navItems = [
    { href: "/upload", label: "Upload PDF", icon: CloudUpload },
    { href: "/help-center", label: "Help Center", icon: HelpCircle },
    { href: "/library", label: "My Library", icon: Library },
    { href: "/settings", label: "Settings", icon: Settings },
  ];

  function toggleDropdown(e: MouseEvent) {
    e.stopPropagation();
    if (user.current) {
      isDropdownOpen = !isDropdownOpen;
    } else {
      onLoginClick?.();
    }
  }

  // Close dropdown when clicking outside
  $effect(() => {
    const handleOutsideClick = () => {
      isDropdownOpen = false;
    };
    window.addEventListener("click", handleOutsideClick);
    return () => {
      window.removeEventListener("click", handleOutsideClick);
    };
  });
</script>

<aside
  class={cn(
    "flex h-full w-full flex-col bg-zinc-900 relative overflow-hidden",
    className,
  )}
>
  <!-- Brand Logo -->
  <div class="flex h-16 items-center px-6 border-b border-zinc-800/50 shrink-0">
    <a href="/" class="flex items-center gap-2" onclick={onNavItemClick}>
      <img src="/logo-gold.png" alt="Cubrain" class="h-8 w-auto" />
    </a>
  </div>

  <!-- Navigation -->
  <nav class="flex-1 space-y-1 px-3 py-4 overflow-y-auto custom-scrollbar">
    {#each navItems as item (item.href)}
      {@const isActive = item.href.includes("?")
        ? page.url.search === new URL("http://dummy" + item.href).search
        : page.url.pathname === item.href && page.url.search === ""}
      <a
        href={item.href}
        onclick={onNavItemClick}
        class={cn(
          "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200",
          isActive
            ? "bg-amber-500/10 text-amber-500 shadow-[0_0_10px_rgba(245,158,11,0.1)]"
            : "text-zinc-400 hover:bg-zinc-800/50 hover:text-zinc-100",
        )}
      >
        <item.icon
          class={cn("h-5 w-5", isActive ? "text-amber-500" : "text-zinc-500")}
        />
        <span class="flex-1">{item.label}</span>
      </a>
    {/each}
  </nav>

  <!-- User Profile -->
  <div class="border-t border-zinc-800 p-4 relative shrink-0">
    <!-- Dropdown Menu -->
    {#if isDropdownOpen && user.current}
      <div
        class="absolute bottom-full left-4 right-4 mb-2 bg-zinc-900 border border-zinc-800 rounded-xl shadow-2xl py-2 z-50 animate-in fade-in slide-in-from-bottom-2 duration-200"
        onclick={(e) => e.stopPropagation()}
        role="presentation"
      >
        <a
          href="/settings"
          onclick={() => (isDropdownOpen = false)}
          class="flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-zinc-400 hover:bg-zinc-800 hover:text-zinc-100 transition-colors"
        >
          <CreditCard class="h-4 w-4 text-zinc-500" />
          Subscription/Billing
        </a>

        <a
          href="/whats-new?from=app"
          onclick={markAsSeen}
          class="flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-zinc-400 hover:bg-zinc-800 hover:text-zinc-100 transition-colors"
        >
          <Sparkles class="h-4 w-4 text-zinc-500" />
          <span class="flex-1">What's New</span>
          {#if hasNewUpdate}
            <span class="flex h-2 w-2 rounded-full bg-red-500"></span>
          {/if}
        </a>

        <div class="h-px bg-zinc-800 my-1 mx-2"></div>

        <button
          onclick={() => {
            isDropdownOpen = false;
            logout();
          }}
          class="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-zinc-400 hover:bg-red-500/10 hover:text-red-400 transition-colors"
        >
          <LogOut class="h-4 w-4" />
          Sign Out
        </button>
      </div>
    {/if}

    <button
      onclick={toggleDropdown}
      class="w-full flex items-center gap-3 rounded-lg bg-zinc-950/50 p-3 border border-zinc-800 hover:border-zinc-700 transition-colors cursor-pointer group relative text-left"
    >
      <div class="relative">
        <div
          class={cn(
            "h-10 w-10 rounded-full flex items-center justify-center font-bold shadow-lg transition-all duration-300",
            user.current
              ? "bg-linear-to-br from-amber-400 to-amber-600 text-black shadow-[0_0_20px_rgba(245,158,11,0.4)]"
              : "bg-linear-to-br from-zinc-700 to-zinc-800 text-zinc-400 group-hover:from-amber-500 group-hover:to-amber-700 group-hover:text-black",
          )}
        >
          {user.current
            ? user.current.email.substring(0, 2).toUpperCase()
            : "G"}
        </div>
        {#if hasNewUpdate && user.current}
          <span
            class="absolute -top-0.5 -right-0.5 h-3 w-3 rounded-full bg-red-500 border-2 border-zinc-900"
          ></span>
        {/if}
      </div>
      <div class="flex-1 overflow-hidden">
        <p
          class="truncate text-sm font-medium text-zinc-200 group-hover:text-white"
        >
          {user.current ? user.current.email.split("@")[0] : "Guest Mode"}
        </p>
        <p
          class="truncate text-xs text-zinc-500 group-hover:text-amber-500/80 transition-colors"
        >
          {user.current ? user.current.email : "Sign in to save progress"}
        </p>
      </div>
    </button>
  </div>
</aside>
