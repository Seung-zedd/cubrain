<script lang="ts">
  import { Zap, Rocket } from "@lucide/svelte";
  import { user } from "$lib/stores/user.svelte";
  import { goto } from "$app/navigation";

  let { class: className = "", text = "Upgrade to Pro 🚀" } = $props<{
    class?: string;
    text?: string;
  }>();

  const CHECKOUT_URL =
    "https://cubrain.lemonsqueezy.com/checkout/buy/646b9e10-0039-4c37-bb30-2ffa5fa2b32f";

  function handleUpgrade() {
    if (!user.current) {
      alert("Please log in first");
      goto("/login");
      return;
    }

    const finalUrl = `${CHECKOUT_URL}?checkout[custom][user_id]=${user.current.id}&checkout[email]=${encodeURIComponent(user.current.email)}`;

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
  <div class="flex items-center justify-center gap-2 px-6 py-3">
    <span>{text}</span>
    <Zap class="w-5 h-5 fill-current" />
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
