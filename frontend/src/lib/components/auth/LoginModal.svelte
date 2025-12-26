<script lang="ts">
  import { API_BASE_URL } from "$lib/config";
  import { fetchUser } from "$lib/stores/user";
  import { X, CircleAlert, CircleCheck } from "@lucide/svelte";
  import { fade, scale, slide, fly } from "svelte/transition";

  let { onclose } = $props<{ onclose: () => void }>();

  let email = $state("");
  let verificationCode = $state("");
  let isSignUpMode = $state(false);
  let showVerification = $state(false);
  let status = $state("idle"); // 'idle' | 'loading' | 'success' | 'error'
  let message = $state("");
  let emailError = $state(false);
  let codeError = $state(false);

  function validateEmail(email: string) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  function close() {
    onclose();
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === "Escape") {
      close();
    }
  }

  async function handleRequestCode() {
    if (!email || !validateEmail(email)) {
      emailError = true;
      status = "error";
      message = "Please enter a valid email address.";
      setTimeout(() => (emailError = false), 500); // Reset shake animation
      return;
    }

    status = "loading";
    message = "";
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 10000); // 10s timeout

    try {
      const mode = isSignUpMode ? "SIGN_UP" : "SIGN_IN";
      const url = `${API_BASE_URL}/api/v1/auth/request-code?mode=${mode}`;
      if (import.meta.env.DEV) {
        console.log(`Requesting code from: ${url}`);
      }

      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
        signal: controller.signal,
        credentials: "include",
      });
      clearTimeout(timeoutId);

      if (response.ok) {
        const responseText = await response.text();
        if (responseText) {
          // If we got a response body, it means we skipped verification and got tokens
          status = "success";
          message = "Welcome back! Logging you in...";

          // Clear logged out state immediately
          document.cookie =
            "isLoggedOut=false; path=/; max-age=1209600; samesite=lax";
          if (typeof localStorage !== "undefined") {
            localStorage.removeItem("isLoggedOut");
          }

          await fetchUser();
          setTimeout(() => {
            window.location.href = "/dashboard";
          }, 800);
        } else {
          showVerification = true;
          status = "success";
          message = "Your verification code is sent to your email!";
        }
      } else {
        const errorData = await response.text();
        if (import.meta.env.DEV) {
          console.error("Auth error response:", errorData);
        }
        status = "error";

        try {
          // Try to parse as JSON if it's a structured error
          const json = JSON.parse(errorData);
          message = json.message || json.details || "Request failed.";
        } catch (e) {
          message =
            errorData ||
            (isSignUpMode ? "Account already exists." : "Account not found.");
        }
      }
    } catch (err: any) {
      clearTimeout(timeoutId);
      if (import.meta.env.DEV) {
        console.error("Fetch error:", err);
      }
      status = "error";
      if (err.name === "AbortError") {
        message = "Request timed out. Please try again.";
      } else {
        message = "Connection failed. Please check your network.";
      }
    } finally {
      if (status === "loading") {
        status = "error";
      }
    }
  }

  async function handleVerify() {
    status = "loading";
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 10000);

    try {
      const mode = isSignUpMode ? "SIGN_UP" : "SIGN_IN";
      const url = `${API_BASE_URL}/api/v1/auth/verify?mode=${mode}`;

      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, code: verificationCode }),
        signal: controller.signal,
        credentials: "include",
      });
      clearTimeout(timeoutId);

      if (response.ok) {
        status = "success";
        message = "Successful verification done!";

        // Clear logged out state immediately
        document.cookie =
          "isLoggedOut=false; path=/; max-age=1209600; samesite=lax";
        if (typeof localStorage !== "undefined") {
          localStorage.removeItem("isLoggedOut");
        }

        await fetchUser();
        setTimeout(() => {
          window.location.href = "/dashboard";
        }, 800);
      } else {
        const errorData = await response.text();
        status = "error";
        codeError = true;
        setTimeout(() => (codeError = false), 500);
        try {
          const json = JSON.parse(errorData);
          message = json.message || "Invalid verification code.";
        } catch (e) {
          message = errorData || "Invalid verification code.";
        }
      }
    } catch (err: any) {
      clearTimeout(timeoutId);
      if (import.meta.env.DEV) {
        console.error("Verify error:", err);
      }
      status = "error";
      if (err.name === "AbortError") {
        message = "Verification timed out. Please try again.";
      } else {
        message = "Verification failed. Please try again.";
      }
    } finally {
      if (status === "loading") {
        status = "error";
      }
    }
  }

  function toggleMode() {
    isSignUpMode = !isSignUpMode;
    showVerification = false;
    message = "";
    status = "idle";
  }
</script>

<svelte:window onkeydown={handleKeydown} />

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
      class="absolute right-4 top-4 z-10 text-zinc-400 hover:text-white transition-colors"
    >
      <X class="w-5 h-5" />
    </button>

    <div class="p-8">
      <div class="text-center mb-8 h-20 relative overflow-hidden">
        {#key isSignUpMode}
          <div
            class="absolute inset-0 flex flex-col justify-center"
            in:fly={{ y: 20, duration: 400, delay: 200 }}
            out:fly={{ y: -20, duration: 200 }}
          >
            <h2 class="text-2xl font-bold text-white mb-2">
              {isSignUpMode ? "Join Cubrain" : "Save your progress"}
            </h2>
            <p class="text-zinc-400 text-sm">
              {isSignUpMode
                ? "Create a free account to start your journey."
                : "Sign in to access your decks and progress."}
            </p>
          </div>
        {/key}
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
        <form
          class="space-y-4"
          onsubmit={(e) => {
            e.preventDefault();
            showVerification ? handleVerify() : handleRequestCode();
          }}
        >
          <div class="space-y-4">
            <div class="relative">
              <input
                type="email"
                placeholder="Email address"
                bind:value={email}
                oninput={() => {
                  emailError = false;
                  if (status === "error") {
                    status = "idle";
                    message = "";
                  }
                }}
                disabled={showVerification}
                class="w-full rounded-lg bg-zinc-800/50 border {emailError
                  ? 'border-red-500 ring-1 ring-red-500'
                  : 'border-zinc-700'} px-4 py-3 text-white placeholder:text-zinc-500 focus:border-[#FFD700] focus:outline-none focus:ring-1 focus:ring-[#FFD700] transition-all disabled:opacity-50 {emailError
                  ? 'animate-shake'
                  : ''}"
              />

              {#if emailError && message}
                <div
                  transition:fly={{ y: 5, duration: 200 }}
                  class="absolute -top-10 left-0 z-20 w-full"
                >
                  <div
                    class="bg-red-500 text-white text-xs font-bold px-3 py-2 rounded shadow-lg relative"
                  >
                    {message}
                    <div
                      class="absolute -bottom-1 left-4 w-2 h-2 bg-red-500 rotate-45"
                    ></div>
                  </div>
                </div>
              {/if}
            </div>

            {#if showVerification}
              <div transition:slide={{ duration: 300 }} class="relative">
                <input
                  type="text"
                  placeholder="6-digit code"
                  bind:value={verificationCode}
                  oninput={() => {
                    codeError = false;
                    if (status === "error") {
                      status = "idle";
                      message = "";
                    }
                  }}
                  maxlength="6"
                  class="w-full rounded-lg bg-zinc-800/50 border {codeError
                    ? 'border-red-500 ring-1 ring-red-500'
                    : 'border-zinc-700'} px-4 py-3 text-white placeholder:text-zinc-500 focus:border-[#FFD700] focus:outline-none focus:ring-1 focus:ring-[#FFD700] transition-all {codeError
                    ? 'animate-shake'
                    : ''}"
                />

                {#if codeError && message}
                  <div
                    transition:fly={{ y: 5, duration: 200 }}
                    class="absolute -top-10 left-0 z-20 w-full"
                  >
                    <div
                      class="bg-red-500 text-white text-xs font-bold px-3 py-2 rounded shadow-lg relative"
                    >
                      {message}
                      <div
                        class="absolute -bottom-1 left-4 w-2 h-2 bg-red-500 rotate-45"
                      ></div>
                    </div>
                  </div>
                {/if}
              </div>
            {/if}

            {#if message}
              <div
                transition:fade
                class="flex items-center gap-2 px-1 py-1 text-sm font-medium {status ===
                'error'
                  ? 'text-[#f87171]'
                  : 'text-[#4ade80]'}"
              >
                {#if status === "error"}
                  <CircleAlert class="w-4 h-4" />
                {:else}
                  <CircleCheck class="w-4 h-4" />
                {/if}
                {message}
              </div>
            {/if}
          </div>

          <button
            type="submit"
            disabled={status === "loading"}
            class="relative w-full h-12 overflow-hidden rounded-lg bg-[#FFD700] font-bold text-black shadow-[0_0_15px_rgba(255,215,0,0.1)] hover:bg-[#FDB931] hover:shadow-[0_0_20px_rgba(255,215,0,0.3)] transition-all disabled:opacity-50"
          >
            {#key `${isSignUpMode}-${showVerification}-${status === "loading"}`}
              <div
                class="absolute inset-0 flex items-center justify-center"
                in:fly={{ y: 15, duration: 300, delay: 150 }}
                out:fly={{ y: -15, duration: 150 }}
              >
                {#if status === "loading"}
                  <div class="flex items-center gap-2">
                    <div
                      class="h-4 w-4 animate-spin rounded-full border-2 border-black border-t-transparent"
                    ></div>
                    Processing...
                  </div>
                {:else if showVerification}
                  Verify Code
                {:else}
                  {isSignUpMode ? "Create Account" : "Sign In"}
                {/if}
              </div>
            {/key}
          </button>
        </form>

        <div class="text-center pt-2">
          <p class="text-sm text-zinc-400">
            {isSignUpMode
              ? "Already have an account?"
              : "Don't have an account?"}
            <button
              onclick={toggleMode}
              class="font-medium text-[#FFD700] hover:underline hover:text-[#FDB931] transition-colors"
            >
              {isSignUpMode ? "Sign in" : "Sign up"}
            </button>
          </p>
        </div>
      </div>
    </div>
  </div>
</div>

<style>
  @keyframes shake {
    0%,
    100% {
      transform: translateX(0);
    }
    25% {
      transform: translateX(-4px);
    }
    75% {
      transform: translateX(4px);
    }
  }
  .animate-shake {
    animation: shake 0.2s ease-in-out 0s 2;
  }
</style>
