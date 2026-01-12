<script lang="ts">
  import { onMount } from "svelte";
  import { user, logout } from "$lib/stores/user.svelte";
  import { supabase } from "$lib/supabaseClient";
  import { goto } from "$app/navigation";
  import User from "@lucide/svelte/icons/user";
  import CreditCard from "@lucide/svelte/icons/credit-card";
  import LogOut from "@lucide/svelte/icons/log-out";
  import Trash2 from "@lucide/svelte/icons/trash-2";
  import Copy from "@lucide/svelte/icons/copy";
  import Check from "@lucide/svelte/icons/check";
  import ShieldAlert from "@lucide/svelte/icons/shield-alert";
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import UpgradeButton from "$lib/components/UpgradeButton.svelte";

  let supabaseUser: any = $state(null);
  let copied = $state(false);

  onMount(async () => {
    if (supabase) {
      const {
        data: { user },
      } = await supabase.auth.getUser();
      supabaseUser = user;
    }
  });

  function copyToClipboard(text: string) {
    navigator.clipboard.writeText(text);
    copied = true;
    setTimeout(() => {
      copied = false;
    }, 2000);
  }

  function handleDeleteAccount() {
    if (
      confirm(
        "Are you sure you want to delete your account? This action cannot be undone."
      )
    ) {
      window.location.href =
        "mailto:support@cubrain.app?subject=Account Deletion Request";
    }
  }

  let isPro = $derived(user.current?.tier === "PRO_USER");
  let isGracePeriod = $derived(isPro && user.current?.endsAt !== null);
  let isActivePro = $derived(isPro && user.current?.endsAt === null);

  function formatDate(dateStr: string | null | undefined) {
    if (!dateStr) return "";
    return new Date(dateStr).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }

  function handleManageSubscription() {
    if (user.current?.customerPortalUrl) {
      window.open(user.current.customerPortalUrl, "_blank");
    } else {
      alert("Billing portal is not available. Please contact support.");
    }
  }
</script>

<div class="max-w-2xl mx-auto py-12 px-6">
  <h1 class="text-3xl font-bold text-white mb-8">Settings</h1>

  <div class="space-y-8">
    <!-- Section A: Identity -->
    <section class="bg-zinc-900/50 rounded-2xl border border-white/5 p-6">
      <div class="flex items-center gap-3 mb-6">
        <div class="p-2 rounded-lg bg-blue-500/10 text-blue-500">
          <User class="w-5 h-5" />
        </div>
        <h2 class="text-lg font-semibold text-white">Identity</h2>
      </div>

      <div class="space-y-6">
        <div>
          <span class="block text-sm font-medium text-zinc-400 mb-1"
            >Email Address</span
          >
          <p class="text-xl font-medium text-white">
            {user.current?.email || "Loading..."}
          </p>
        </div>

        <div>
          <span class="block text-sm font-medium text-zinc-400 mb-1"
            >User UID</span
          >
          <div class="flex items-center gap-2 group">
            <code
              class="px-3 py-1.5 rounded-lg bg-black/50 border border-white/5 text-zinc-500 font-mono text-sm"
            >
              {supabaseUser?.id || "Loading..."}
            </code>
            <button
              onclick={() => copyToClipboard(supabaseUser?.id || "")}
              class="p-1.5 rounded-md hover:bg-white/10 text-zinc-500 transition-colors relative"
              title="Copy UID"
            >
              {#if copied}
                <Check class="w-4 h-4 text-green-500" />
              {:else}
                <Copy class="w-4 h-4" />
              {/if}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Section B: Subscription -->
    <section class="bg-zinc-900/50 rounded-2xl border border-white/5 p-6">
      <div class="flex items-center gap-3 mb-6">
        <div class="p-2 rounded-lg bg-[#FFD700]/10 text-[#FFD700]">
          <CreditCard class="w-5 h-5" />
        </div>
        <h2 class="text-lg font-semibold text-white">Subscription</h2>
      </div>

      <div class="flex items-center justify-between">
        <div>
          <div class="flex items-center gap-2 mb-1">
            <span class="text-zinc-400">Current Plan:</span>
            {#if isPro}
              <span
                class="px-2 py-0.5 rounded-full bg-[#FFD700]/20 text-[#FFD700] text-xs font-bold border border-[#FFD700]/20 flex items-center gap-1"
              >
                <Sparkles class="w-3 h-3 fill-current" />
                PRO
              </span>
            {:else}
              <span
                class="px-2 py-0.5 rounded-full bg-zinc-800 text-zinc-400 text-xs font-bold border border-white/5"
              >
                FREE
              </span>
            {/if}
          </div>
          <p class="text-sm text-zinc-500">
            {#if isActivePro}
              Thank you for supporting Cubrain!
            {:else if isGracePeriod}
              Your Pro access expires on <span class="text-red-500 font-bold"
                >{formatDate(user.current?.endsAt)}</span
              >. Renew now to keep your lifetime 72% discount!
            {:else}
              Upgrade to unlock unlimited uploads and advanced features.
            {/if}
          </p>
        </div>

        {#if isActivePro}
          <button
            onclick={handleManageSubscription}
            class="px-4 py-2 rounded-lg bg-white/5 hover:bg-white/10 text-white text-sm font-medium transition-colors border border-white/10"
          >
            Manage Subscription
          </button>
        {:else if isGracePeriod}
          <div class="flex flex-col gap-2 items-end">
            <UpgradeButton
              class="h-10 text-sm font-bold text-black"
              text="Renew Subscription ⚡"
            />
            <button
              onclick={handleManageSubscription}
              class="text-xs text-zinc-500 hover:text-zinc-300 underline transition-colors"
            >
              Billing Portal
            </button>
          </div>
        {:else}
          <UpgradeButton
            class="h-12 text-lg font-bold text-black"
            text="Upgrade to Pro"
          />
        {/if}
      </div>
    </section>

    <!-- Section C: Session -->
    <section class="bg-zinc-900/50 rounded-2xl border border-white/5 p-6">
      <div class="flex items-center gap-3 mb-6">
        <div class="p-2 rounded-lg bg-zinc-800 text-zinc-400">
          <LogOut class="w-5 h-5" />
        </div>
        <h2 class="text-lg font-semibold text-white">Session</h2>
      </div>

      <div class="flex items-center justify-between">
        <p class="text-zinc-400 text-sm">
          Sign out of your account on this device.
        </p>
        <button
          onclick={logout}
          class="px-4 py-2 rounded-lg border border-white/10 hover:bg-white/5 text-zinc-400 hover:text-white text-sm font-medium transition-colors"
        >
          Sign Out
        </button>
      </div>
    </section>

    <!-- Section D: Danger Zone -->
    <section
      class="rounded-2xl border border-red-500/20 bg-red-500/5 p-6 relative overflow-hidden"
    >
      <div class="flex items-center gap-3 mb-6 relative z-10">
        <div class="p-2 rounded-lg bg-red-500/10 text-red-500">
          <ShieldAlert class="w-5 h-5" />
        </div>
        <h2 class="text-lg font-semibold text-red-500">Danger Zone</h2>
      </div>

      <div class="flex items-center justify-between relative z-10">
        <div>
          <h3 class="text-white font-medium mb-1">Delete Account</h3>
          <p class="text-red-400/60 text-sm">
            Permanently delete your account and all data.
          </p>
        </div>
        <button
          onclick={handleDeleteAccount}
          class="px-4 py-2 rounded-lg border border-red-500/30 hover:bg-red-500/10 text-red-500 text-sm font-medium transition-colors flex items-center gap-2"
        >
          <Trash2 class="w-4 h-4" />
          Delete Account
        </button>
      </div>
    </section>
  </div>
</div>
