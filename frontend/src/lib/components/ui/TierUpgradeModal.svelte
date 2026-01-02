<script lang="ts">
  import {
    X,
    Zap,
    CircleCheck,
    Rocket,
    Star,
    Lock,
    Sparkles,
  } from "@lucide/svelte";
  import { fade, scale, fly } from "svelte/transition";
  import { useModal } from "$lib/modal.svelte";
  import UpgradeButton from "$lib/components/UpgradeButton.svelte";
  import { goto } from "$app/navigation";
  import { IS_LAUNCH_SALE } from "$lib/config/config";

  let {
    onclose,
    onsignup,
    type = "daily_limit",
    mode = "free",
  } = $props<{
    onclose: () => void;
    onsignup?: () => void;
    type?: "daily_limit";
    mode?: "guest" | "free";
  }>();

  function close() {
    onclose();
  }

  const { handleBackdropClick, handleKeydown } = useModal(close);

  const content: Record<
    "daily_limit",
    {
      title: string;
      description: string;
      highlight: string;
      features: string[];
    }
  > = {
    daily_limit: {
      title:
        mode === "guest" ? "Unlock more with Cubrain!" : "You're in the zone!",
      description:
        mode === "guest"
          ? "Sign up for free to get more daily uploads and save your decks."
          : "Get unlimited uploads for just $5.99/mo. (Limited Time Launch Offer)",
      highlight:
        mode === "guest" ? "Guest Limit Reached" : "Daily limit reached (3/3)",
      features:
        mode === "guest"
          ? [
              "Save decks to library",
              "Smart Synthesis (1-2 cards/page)",
              "Track progress",
              "Anki Export Support (.csv)",
              "Access on any device",
            ]
          : [
              "Unlimited uploads",
              "Deep Synthesis (Up to 5 cards/page)",
              "Cover your entire Textbook",
              "Priority AI processing",
            ],
    },
  };

  let activeContent = $derived(content[type as "daily_limit"]);
</script>

<svelte:window onkeydown={handleKeydown} />

<div
  class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-4"
  transition:fade={{ duration: 200 }}
  role="dialog"
  aria-modal="true"
  onclick={handleBackdropClick}
  onkeydown={handleKeydown}
  tabindex="-1"
>
  <div
    class="relative w-full max-w-lg overflow-hidden rounded-3xl border border-amber-500/30 bg-[#0f0f0f] shadow-[0_0_50px_rgba(255,215,0,0.15)]"
    transition:scale={{ duration: 300, start: 0.9, opacity: 0 }}
  >
    <!-- Premium Gradient Top -->
    <div
      class="absolute top-0 left-0 w-full h-1 bg-linear-to-r from-amber-500 via-yellow-300 to-amber-500"
    ></div>

    <!-- Close button -->
    <button
      onclick={close}
      class="absolute right-6 top-6 z-10 text-zinc-500 hover:text-white transition-colors"
    >
      <X class="w-6 h-6" />
    </button>

    <div class="p-10">
      <div class="flex flex-col items-center text-center mb-8">
        <div
          class="h-16 w-16 rounded-2xl bg-amber-500/10 flex items-center justify-center mb-6 border border-amber-500/20"
        >
          {#if type === "daily_limit"}
            <Rocket class="w-8 h-8 text-amber-500" />
          {:else}
            <Zap class="w-8 h-8 text-amber-500" />
          {/if}
        </div>

        <span
          class="px-3 py-1 rounded-full bg-amber-500/10 text-amber-500 text-xs font-bold uppercase tracking-widest mb-4 border border-amber-500/20"
        >
          {activeContent.highlight}
        </span>

        <h2 class="text-3xl font-bold text-white mb-4 tracking-tight">
          {activeContent.title}
        </h2>

        {#if mode === "free" && IS_LAUNCH_SALE}
          <div
            class="px-3 py-1 rounded-full bg-[#FFD700]/20 border border-[#FFD700]/30 text-[#FFD700] text-[10px] font-black uppercase tracking-wider flex items-center gap-1 mb-4"
          >
            <Sparkles class="w-2.5 h-2.5 fill-current" />
            Launch Special
          </div>
        {/if}

        <p class="text-zinc-400 text-lg leading-relaxed max-w-md">
          {activeContent.description}
        </p>
      </div>

      {#if mode === "free" && IS_LAUNCH_SALE}
        <!-- Pricing Card Inside Modal -->
        <div
          class="relative p-6 rounded-2xl bg-linear-to-br from-zinc-900 to-black border-2 border-[#FFD700] shadow-[0_0_30px_rgba(255,215,0,0.1)] overflow-hidden group mb-8"
        >
          <div class="relative z-10">
            <div class="flex justify-between items-start mb-4">
              <div>
                <p
                  class="text-[10px] font-black text-[#FFD700] tracking-widest uppercase mb-1"
                >
                  Monthly Plan
                </p>
                <div class="flex items-baseline gap-2">
                  <span class="text-4xl font-black text-white tracking-tighter"
                    >$5<sup class="text-xl ml-0.5">.99</sup></span
                  >
                  <span class="text-zinc-500 text-sm line-through font-medium"
                    >$11.99</span
                  >
                </div>
              </div>
              <div class="text-right">
                <div
                  class="flex items-center gap-1 text-[#FFD700] text-[10px] font-bold"
                >
                  <Lock class="w-3 h-3" />
                  LOCKED FOREVER
                </div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-y-2 gap-x-4">
              {#each ["Unlimited Uploads", "1,000 Page Limit", "Deep Synthesis", "Priority AI", "Anki Export Support (.csv)"] as feature}
                <div class="flex items-center gap-2 text-zinc-300 text-[11px]">
                  <CircleCheck class="w-3 h-3 text-[#FFD700]" />
                  <span>{feature}</span>
                </div>
              {/each}
            </div>
          </div>

          <!-- Background Glow -->
          <div
            class="absolute -right-4 -bottom-4 w-24 h-24 bg-[#FFD700]/10 blur-2xl rounded-full"
          ></div>
        </div>
      {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-10">
          {#each activeContent.features as feature}
            <div
              class="flex items-center gap-3 p-3 rounded-xl bg-zinc-900/50 border border-zinc-800"
            >
              <CircleCheck class="w-5 h-5 text-amber-500 shrink-0" />
              <span class="text-zinc-300 text-sm font-medium">{feature}</span>
            </div>
          {/each}
        </div>
      {/if}

      <div class="space-y-4">
        {#if mode === "guest"}
          <button
            onclick={() => {
              close();
              if (onsignup) {
                onsignup();
              } else {
                goto("/login");
              }
            }}
            class="group relative w-full h-14 overflow-hidden rounded-xl bg-amber-500 font-bold text-black shadow-[0_0_30px_rgba(255,215,0,0.3)] hover:bg-amber-400 hover:shadow-[0_0_40_rgba(255,215,0,0.5)] transition-all duration-300"
          >
            <div class="flex items-center justify-center gap-2">
              <span>Sign up for Free</span>
              <Zap class="w-5 h-5 fill-current" />
            </div>
            <div
              class="absolute inset-0 w-full h-full bg-linear-to-r from-transparent via-white/30 to-transparent -translate-x-full group-hover:animate-shine"
            ></div>
          </button>
        {:else}
          <UpgradeButton
            class="w-full h-14"
            text={IS_LAUNCH_SALE ? "Lock in $5.99 Forever" : "Upgrade to Pro"}
            onLoginRequired={onsignup}
          />
        {/if}

        <button
          onclick={close}
          class="w-full py-3 text-zinc-500 hover:text-zinc-300 font-medium transition-colors"
        >
          Maybe later
        </button>
      </div>
    </div>
  </div>
</div>

<style>
  @keyframes shine {
    100% {
      transform: translateX(100%);
    }
  }
  .group-hover\:animate-shine:hover {
    animation: shine 0.8s ease-in-out;
  }
</style>
