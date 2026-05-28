<script lang="ts">
  import { auth, googleProvider } from "$lib/firebaseClient";
  import {
    signInWithPopup,
    signInWithEmailAndPassword,
    createUserWithEmailAndPassword,
    sendEmailVerification,
  } from "firebase/auth";
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
  let email = $state("");
  let password = $state("");
  let isSignUpMode = $state(false); // toggle between sign in and sign up

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

  async function handleEmailAuth(e: Event) {
    e.preventDefault();
    if (!auth) {
      status = "error";
      message = "Authentication service is unavailable.";
      return;
    }

    if (!email || !password) {
      status = "error";
      message = "Please fill in all fields.";
      return;
    }

    status = "loading";
    message = "";

    try {
      let result;
      if (isSignUpMode) {
        result = await createUserWithEmailAndPassword(auth, email, password);
        if (!result.user) throw new Error("Failed to create account.");
        
        // Trigger native Firebase email verification
        try {
          await sendEmailVerification(result.user);
        } catch (emailErr) {
          if (IS_DEV_MODE) {
            console.error("Failed to send verification email:", emailErr);
          }
        }
        
        status = "success";
        message = "Account created! A verification email has been sent. Redirecting...";
      } else {
        result = await signInWithEmailAndPassword(auth, email, password);
        if (!result.user) throw new Error("Failed to sign in.");
        status = "success";
        message = "Signed in successfully! Redirecting...";
      }

      await fetchUser();
      setTimeout(() => {
        window.location.href = redirectTo;
      }, 800);
    } catch (err: any) {
      if (IS_DEV_MODE) {
        console.error("Email auth error:", err);
      }
      status = "error";
      // Provide clean error messages for common Firebase codes
      if (err.code === "auth/invalid-credential") {
        message = "Invalid email or password.";
      } else if (err.code === "auth/email-already-in-use") {
        message = "This email is already in use.";
      } else if (err.code === "auth/weak-password") {
        message = "Password should be at least 6 characters.";
      } else if (err.code === "auth/invalid-email") {
        message = "Please enter a valid email address.";
      } else {
        message = err.message || "Authentication failed.";
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
    class="relative w-full max-w-md overflow-hidden rounded-2xl border border-zinc-800 bg-[#121212] shadow-2xl"
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
      <div class="text-center mb-6 h-16 relative overflow-hidden flex flex-col justify-center">
        <h2 class="text-2xl font-bold text-white mb-1">
          {isSignUpMode ? "Create an account" : "Save your progress"}
        </h2>
        <p class="text-zinc-400 text-xs">
          {isSignUpMode ? "Sign up to start saving your decks and progress." : "Sign in to access your decks and progress."}
        </p>
      </div>

      <div class="space-y-4">
        <!-- Email/Password Form -->
        <form onsubmit={handleEmailAuth} class="space-y-3">
          <div>
            <label for="email" class="block text-[10px] font-bold text-zinc-500 uppercase tracking-wider mb-1">
              Email Address
            </label>
            <input
              id="email"
              type="email"
              bind:value={email}
              placeholder="name@example.com"
              disabled={status === "loading"}
              required
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/50 px-3.5 py-2 text-sm text-white placeholder-zinc-600 focus:border-amber-500/50 focus:outline-none transition-colors"
            />
          </div>
          <div>
            <label for="password" class="block text-[10px] font-bold text-zinc-500 uppercase tracking-wider mb-1">
              Password
            </label>
            <input
              id="password"
              type="password"
              bind:value={password}
              placeholder="••••••••"
              disabled={status === "loading"}
              required
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/50 px-3.5 py-2 text-sm text-white placeholder-zinc-600 focus:border-amber-500/50 focus:outline-none transition-colors"
            />
          </div>
          <button
            type="submit"
            disabled={status === "loading"}
            class="flex w-full items-center justify-center rounded-lg bg-amber-500 hover:bg-amber-400 py-2.5 text-sm font-bold text-black transition-all active:scale-[0.98] disabled:opacity-50"
          >
            {#if status === "loading" && isSignUpMode}
              <div class="h-4 w-4 animate-spin rounded-full border-2 border-black border-t-transparent mr-2"></div>
              Creating Account...
            {:else}
              {isSignUpMode ? "Create Account" : "Sign In"}
            {/if}
          </button>
        </form>

        <div class="relative flex py-2 items-center">
          <div class="flex-grow border-t border-zinc-900"></div>
          <span class="flex-shrink mx-3 text-zinc-600 text-[10px] font-bold uppercase tracking-wider">Or</span>
          <div class="flex-grow border-t border-zinc-900"></div>
        </div>

        <!-- Google Sign In -->
        <button
          onclick={handleGoogleLogin}
          disabled={status === "loading"}
          class="flex w-full items-center justify-center gap-3 rounded-lg border border-zinc-800 bg-zinc-950 px-4 py-2.5 text-sm font-medium text-white transition-transform hover:scale-[1.01] active:scale-[0.99] disabled:opacity-50"
        >
          {#if status === "loading" && !isSignUpMode}
            <div class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
            Connecting...
          {:else}
            <svg class="h-4 w-4" viewBox="0 0 24 24">
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
            class="flex items-center justify-center gap-2 px-1 py-1 text-xs font-semibold text-center {status === 'error' ? 'text-[#f87171]' : 'text-[#4ade80]'}"
          >
            {#if status === 'error'}
              <CircleAlert class="w-3.5 h-3.5 shrink-0" />
            {/if}
            {message}
          </div>
        {/if}

        <div class="text-center mt-4">
          <button
            onclick={() => {
              isSignUpMode = !isSignUpMode;
              message = "";
            }}
            disabled={status === "loading"}
            class="text-xs font-semibold text-amber-500 hover:text-amber-400 transition-colors"
          >
            {isSignUpMode ? "Already have an account? Sign In" : "Don't have an account? Sign Up"}
          </button>
        </div>
      </div>
    </div>
  </div>
</div>
