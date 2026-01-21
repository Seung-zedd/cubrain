<script lang="ts">
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import X from "@lucide/svelte/icons/x";
  import { fade, slide } from "svelte/transition";
  import { onMount } from "svelte";
  import { user } from "$lib/stores/user.svelte";
  import { authFetch } from "$lib/api";
  import AlertTriangle from "@lucide/svelte/icons/alert-triangle";

  let isVisible = $state(false);
  let remainingSpots = $state(100);

  async function fetchCount() {
    try {
      const res = await authFetch("/api/v1/subscription/early-bird-count");
      if (res.ok) {
        const data = await res.json();
        remainingSpots = data.remainingSpots;
      }
    } catch (e) {
      if (import.meta.env.DEV)
        console.error("Failed to fetch early bird count", e);
    }
  }

  onMount(() => {
    const dismissed = localStorage.getItem("launch_banner_dismissed");
    if (!dismissed) {
      isVisible = true;
    }
    fetchCount();
  });

  function dismiss() {
    isVisible = false;
    localStorage.setItem("launch_banner_dismissed", "true");
  }

  let isGuest = $derived(!user.current);
  let isFreeUser = $derived(user.current?.tier === "FREE_USER");
  let isGracePeriod = $derived(
    user.current?.tier === "PRO_USER" && user.current?.endsAt !== null,
  );
  let isActivePro = $derived(
    user.current?.tier === "PRO_USER" && user.current?.endsAt === null,
  );

  let showBanner = $derived(
    isVisible && !isActivePro && (isGuest || isFreeUser || isGracePeriod),
  );
</script>

{#if showBanner}
  <div
    class="sticky top-0 z-50 w-full {isGracePeriod
      ? 'bg-linear-to-r from-red-600 via-red-500 to-red-600 text-white'
      : 'bg-linear-to-r from-amber-600 via-amber-500 to-amber-600 text-black'} py-2.5 px-4 shadow-lg"
    transition:slide={{ duration: 300 }}
  >
    <div class="max-w-7xl mx-auto flex items-center justify-between gap-4">
      <div class="flex-1 flex items-center justify-center gap-3">
        {#if isGracePeriod}
          <div class="flex items-center gap-2">
            <AlertTriangle class="w-5 h-5 animate-pulse" />
            <p class="text-sm sm:text-base font-bold tracking-tight">
              ⚠️ Your Pro plan is ending soon. Don't lose your Early Bird
              benefits!
            </p>
          </div>
        {:else}
          <div class="hidden sm:flex items-center gap-1">
            <Sparkles class="w-4 h-4 fill-current animate-pulse" />
            <span class="text-[10px] font-black uppercase tracking-tighter"
              >Launch Sale</span
            >
          </div>
          <p class="text-sm sm:text-base font-bold tracking-tight">
            🔥 Only <span class="underline decoration-2">{remainingSpots}</span>
            spots left for the 72% Lifetime Discount!
            <a
              href="/settings"
              class="ml-2 bg-black text-amber-500 px-2 py-0.5 rounded-md font-black hover:scale-105 transition-transform inline-block"
            >
              Claim Now
            </a>
          </p>
        {/if}
      </div>
      <button
        onclick={dismiss}
        class="p-1 hover:bg-black/10 rounded-full transition-colors shrink-0"
        aria-label="Dismiss banner"
      >
        <X class="w-4 h-4" />
      </button>
    </div>
  </div>
{/if}
