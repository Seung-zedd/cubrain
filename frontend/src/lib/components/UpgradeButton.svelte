<script lang="ts">
  import Zap from "@lucide/svelte/icons/zap";
  import { user } from "$lib/stores/user.svelte";
  import { goto } from "$app/navigation";
  import { IS_LAUNCH_SALE } from "$lib/config/config";

  let {
    class: className = "",
    text = "Upgrade to Pro 🚀",
    variantId = "646b9e10-0039-4c37-bb30-2ffa5fa2b32f",
    onLoginRequired,
  } = $props<{
    class?: string;
    text?: string;
    variantId?: string;
    onLoginRequired?: () => void;
  }>();

  const BASE_CHECKOUT_URL = "https://cubrain.lemonsqueezy.com/checkout/buy/";

  function handleUpgrade() {
    if (!user.current) {
      if (onLoginRequired) {
        onLoginRequired();
      } else {
        goto("/login");
      }
      return;
    }

    const checkoutUrl = `${BASE_CHECKOUT_URL}${variantId}`;
    const params = new URLSearchParams();
    params.append("checkout[custom][user_id]", String(user.current.id));
    params.append("checkout[email]", user.current.email);
    if (IS_LAUNCH_SALE) {
      params.append("checkout[discount_code]", "EARLYBIRD");
    }

    const finalUrl = `${checkoutUrl}?${params.toString()}`;

    if (window.LemonSqueezy) {
      // @ts-ignore
      window.LemonSqueezy.Url.Open(finalUrl);
    } else {
      window.open(finalUrl, "_blank");
    }
  }
</script>

<button
  onclick={handleUpgrade}
  class="group relative overflow-hidden rounded-xl bg-amber-500 font-bold text-black shadow-[0_0_30px_rgba(255,215,0,0.3)] hover:bg-amber-400 hover:shadow-[0_0_40px_rgba(255,215,0,0.5)] transition-all duration-300 {className}"
>
  <div
    class="flex items-center justify-center gap-2 px-6 py-3 whitespace-nowrap"
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
