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

  const navItems = [
    { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { href: "/upload", label: "Upload PDF", icon: CloudUpload },
    { href: "/library", label: "Library", icon: Library },
    { href: "/settings", label: "Settings", icon: Settings },
  ];
</script>

<aside
  class="hidden h-screen w-64 flex-col border-r border-zinc-800 bg-zinc-900 md:flex"
>
  <!-- Brand Logo -->
  <div class="flex h-16 items-center px-6 border-b border-zinc-800/50">
    <a href="/dashboard" class="flex items-center gap-2">
      <img src="/logo-gold.png" alt="Cubrain" class="h-8 w-auto" />
    </a>
  </div>

  <!-- Navigation -->
  <nav class="flex-1 space-y-1 px-3 py-4">
    {#each navItems as item}
      {@const isActive = page.url.pathname === item.href}
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
      class="flex items-center gap-3 rounded-lg bg-zinc-950/50 p-3 border border-zinc-800 hover:border-zinc-700 transition-colors cursor-pointer group"
    >
      <div
        class="h-10 w-10 rounded-full bg-linear-to-br from-amber-500 to-amber-700 flex items-center justify-center text-black font-bold shadow-lg"
      >
        ST
      </div>
      <div class="flex-1 overflow-hidden">
        <p
          class="truncate text-sm font-medium text-zinc-200 group-hover:text-white"
        >
          Student User
        </p>
        <p class="truncate text-xs text-zinc-500">student@cubrain.app</p>
      </div>
      <LogOut
        class="h-4 w-4 text-zinc-500 hover:text-red-400 transition-colors"
      />
    </div>
  </div>
</aside>
