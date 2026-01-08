<script lang="ts">
  import Zap from "@lucide/svelte/icons/zap";
  import { user } from "$lib/stores/user.svelte";
  import { paymentStore } from "$lib/stores/payment.svelte";
  import { goto } from "$app/navigation";
  import { IS_LAUNCH_SALE } from "$lib/config/config";
  import { onMount } from "svelte";

  let {
    class: className = "",
    text = "Upgrade to Pro",
    variantId = "", // If empty, will use plan-based variant from config
    plan = "monthly",
    onLoginRequired,
  } = $props<{
    class?: string;
    text?: string;
    variantId?: string;
    plan?: "monthly" | "yearly";
    onLoginRequired?: () => void;
  }>();

  onMount(() => {
    // Pre-fetch config when button is rendered
    if (user.current) {
      paymentStore.fetchConfig();
    }
  });

  async function handleUpgrade() {
    if (!user.current) {
      if (onLoginRequired) {
        onLoginRequired();
      } else {
        goto("/login");
      }
      return;
    }

    // Ensure config is loaded
    const config = await paymentStore.fetchConfig();
    if (!config) {
      alert("Failed to load payment configuration. Please try again later.");
      return;
    }

    const baseCheckoutUrl = config.checkoutUrl;
    const selectedVariantId =
      variantId ||
      (plan === "yearly" ? config.yearlyVariantId : config.monthlyVariantId);

    const checkoutUrl = `${baseCheckoutUrl}${selectedVariantId}`;

    // Validate checkoutUrl
    if (!checkoutUrl.startsWith("http")) {
      console.error(
        "❌ [UpgradeButton] Invalid checkout URL constructed:",
        checkoutUrl
      );
      alert("Invalid payment configuration. Please contact support.");
      return;
    }

    const params = new URLSearchParams();
    params.append("checkout[custom][user_id]", String(user.current.id));
    params.append("checkout[email]", user.current.email);
    if (IS_LAUNCH_SALE) {
      params.append("checkout[discount_code]", "EARLYBIRD");
    }

    const finalUrl = `${checkoutUrl}?${params.toString()}`;

    if (import.meta.env.DEV) {
      console.log("💳 [UpgradeButton] Opening checkout:", finalUrl);
    }

    try {
      if (window.LemonSqueezy) {
        // @ts-ignore
        window.LemonSqueezy.Url.Open(finalUrl);
      } else {
        window.open(finalUrl, "_blank");
      }
    } catch (err) {
      console.error("❌ [UpgradeButton] Error opening checkout:", err);
      // Fallback to direct window.open if LemonSqueezy.js fails
      window.open(finalUrl, "_blank");
    }
  }
</script>

<button
  onclick={handleUpgrade}
  class="group relative overflow-hidden rounded-xl bg-amber-500 font-bold text-black shadow-[0_0_30px_rgba(255,215,0,0.3)] hover:bg-amber-400 hover:shadow-[0_0_40px_rgba(255,215,0,0.5)] transition-all duration-300 {className}"
>
  <div
    class="flex items-center justify-center gap-3 px-6 py-3 whitespace-nowrap"
  >
    <span>{text}</span>
    <Zap class="w-5 h-5 fill-current shrink-0" />
  </div>

  <!-- Shine effect -->
  <div
    class="absolute inset-0 w-full h-full bg-linear-to-r from-transparent via-white/30 to-transparent -translate-x-full group-hover:animate-shine"
  ></div>
</button>

<style>
  @keyframes shine {
    100% {
      transform: translateX(100%);
    }
  }

  .group:hover .group-hover\:animate-shine {
    animation: shine 0.8s ease-in-out;
  }
</style>
