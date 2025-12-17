<script lang="ts">
  import { X } from "@lucide/svelte";
  import { fade, scale } from "svelte/transition";

  let { onclose } = $props<{ onclose: () => void }>();

  function close() {
    onclose();
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === "Escape") {
      close();
    }
  }
</script>

<svelte:window on:keydown={handleKeydown} />

<div
  class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4"
  transition:fade={{ duration: 200 }}
  role="dialog"
  aria-modal="true"
>
  <!-- Backdrop click handler -->
  <div
    class="absolute inset-0"
    onclick={close}
    role="button"
    tabindex="-1"
    onkeydown={(e) => e.key === "Enter" && close()}
  ></div>

  <div
    class="relative w-full max-w-md overflow-hidden rounded-2xl border border-gray-700 bg-[#1a1a1a] shadow-2xl"
    transition:scale={{ duration: 200, start: 0.95 }}
  >
    <!-- Close button -->
    <button
      onclick={close}
      class="absolute right-4 top-4 text-zinc-400 hover:text-white transition-colors"
    >
      <X class="w-5 h-5" />
    </button>

    <div class="p-8">
      <div class="text-center mb-8">
        <h2 class="text-2xl font-bold text-white mb-2">Save your progress</h2>
        <p class="text-zinc-400 text-sm">
          Create a free account to save your decks and study later.
        </p>
      </div>

      <div class="space-y-4">
        <!-- Google Sign In -->
        <button
          class="flex w-full items-center justify-center gap-3 rounded-lg bg-white px-4 py-3 font-medium text-black transition-transform hover:scale-[1.02] active:scale-[0.98]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24">
            <path
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              fill="#4285F4"
            />
            <path
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              fill="#34A853"
            />
            <path
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              fill="#FBBC05"
            />
            <path
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              fill="#EA4335"
            />
          </svg>
          Continue with Google
        </button>

        <div class="relative flex items-center py-2">
          <div class="grow border-t border-gray-700"></div>
          <span class="mx-4 shrink-0 text-xs text-zinc-500"
            >Or continue with email</span
          >
          <div class="grow border-t border-gray-700"></div>
        </div>

        <!-- Email Form -->
        <form class="space-y-4" onsubmit={(e) => e.preventDefault()}>
          <div class="space-y-4">
            <input
              type="email"
              placeholder="Email address"
              class="w-full rounded-lg bg-zinc-800/50 border border-zinc-700 px-4 py-3 text-white placeholder:text-zinc-500 focus:border-[#FFD700] focus:outline-none focus:ring-1 focus:ring-[#FFD700] transition-all"
            />
            <input
              type="password"
              placeholder="Password"
              class="w-full rounded-lg bg-zinc-800/50 border border-zinc-700 px-4 py-3 text-white placeholder:text-zinc-500 focus:border-[#FFD700] focus:outline-none focus:ring-1 focus:ring-[#FFD700] transition-all"
            />
          </div>

          <button
            type="submit"
            class="w-full rounded-lg bg-[#FFD700] px-4 py-3 font-bold text-black shadow-[0_0_15px_rgba(255,215,0,0.1)] hover:bg-[#FDB931] hover:shadow-[0_0_20px_rgba(255,215,0,0.3)] transition-all"
          >
            Sign In
          </button>
        </form>

        <div class="text-center pt-2">
          <p class="text-sm text-zinc-400">
            Don't have an account?
            <a
              href="/signup"
              class="font-medium text-[#FFD700] hover:underline hover:text-[#FDB931] transition-colors"
            >
              Sign up
            </a>
          </p>
        </div>
      </div>
    </div>
  </div>
</div>
