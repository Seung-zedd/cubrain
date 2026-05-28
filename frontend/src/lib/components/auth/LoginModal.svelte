<script lang="ts">
  import { auth, googleProvider } from "$lib/firebaseClient";
  import { signInWithPopup } from "firebase/auth";
  import { fetchUser } from "$lib/stores/user.svelte";
  import X from "@lucide/svelte/icons/x";
  import CircleAlert from "@lucide/svelte/icons/circle-alert";
  import { fade, scale } from "svelte/transition";
  import { useModal } from "$lib/modal.svelte";
  import { IS_DEV_MODE } from "$lib/utils/env";

  let { onclose, redirectTo = "/library" } = $props<{
    onclose: () => void;
    redirectTo?: string;
  }>();

  let status = $state("idle"); // 'idle' | 'loading' | 'success' | 'error'
  let message = $state("");

  function close() {
    onclose();
  }

  const { handleBackdropClick, handleKeydown } = useModal(close);

  async function handleGoogleLogin() {
    if (!auth) {
      status = "error";
      message = "Authentication service is unavailable.";
      return;
    }

    status = "loading";
    message = "";

    try {
      const result = await signInWithPopup(auth, googleProvider);
      if (!result.user) throw new Error("No user returned from Google Sign-In");

      status = "success";
      message = "Signed in successfully! Redirecting...";

      await fetchUser();
      setTimeout(() => {
        window.location.href = redirectTo;
      }, 800);
    } catch (err: any) {
      if (IS_DEV_MODE) {
        console.error("Google login error:", err);
      }
      status = "error";
      // Handle cancelled popups nicely
      if (err.code === "auth/popup-closed-by-user") {
        message = "Login popup closed. Please try again.";
      } else {
        message = err.message || "Failed to sign in with Google.";
      }
    }
  }
</script>

<div
  class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4"
  transition:fade={{ duration: 200 }}
  role="dialog"
  aria-modal="true"
  onclick={handleBackdropClick}
  onkeydown={handleKeydown}
  tabindex="-1"
>
  <div
    class="relative w-full max-w-md overflow-hidden rounded-2xl border border-gray-700 bg-[#1a1a1a] shadow-2xl"
    transition:scale={{ duration: 200, start: 0.95 }}
  >
    <!-- Close button -->
    <button
      onclick={close}
      class="absolute right-4 top-4 z-10 text-zinc-400 hover:text-white transition-colors"
    >
      <X class="w-5 h-5" />
    </button>

    <div class="p-8">
      <div class="text-center mb-8 h-20 relative overflow-hidden flex flex-col justify-center">
        <h2 class="text-2xl font-bold text-white mb-2">
          Save your progress
        </h2>
        <p class="text-zinc-400 text-sm">
          Sign in to access your decks and progress.
        </p>
      </div>

      <div class="space-y-6">
        <!-- Google Sign In -->
        <button
          onclick={handleGoogleLogin}
          disabled={status === "loading"}
          class="flex w-full items-center justify-center gap-3 rounded-lg bg-white px-4 py-3 font-medium text-black transition-transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50"
        >
          {#if status === "loading"}
            <div class="h-5 w-5 animate-spin rounded-full border-2 border-black border-t-transparent"></div>
            Connecting...
          {:else}
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
          {/if}
        </button>

        {#if message}
          <div
            transition:fade
            class="flex items-center justify-center gap-2 px-1 py-1 text-sm font-medium text-center {status === 'error' ? 'text-[#f87171]' : 'text-[#4ade80]'}"
          >
            {#if status === "error"}
              <CircleAlert class="w-4 h-4 shrink-0" />
            {/if}
            {message}
          </div>
        {/if}
      </div>
    </div>
  </div>
</div>
