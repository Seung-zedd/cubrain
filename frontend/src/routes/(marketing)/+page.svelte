<script lang="ts">
  import heroImage from "$lib/assets/hero.png";
  import FlashcardDemo from "$lib/components/FlashcardDemo.svelte";
  import { authFetch } from "$lib/api";
  import { fade, fly } from "svelte/transition";
  import { user } from "$lib/stores/user.svelte";
  import LoginModal from "$lib/components/auth/LoginModal.svelte";
  import Menu from "@lucide/svelte/icons/menu";
  import X from "@lucide/svelte/icons/x";
  import Zap from "@lucide/svelte/icons/zap";
  import Brain from "@lucide/svelte/icons/brain";
  import RefreshCw from "@lucide/svelte/icons/refresh-cw";
  import CheckCircle2 from "@lucide/svelte/icons/check-circle-2";
  import LayoutDashboard from "@lucide/svelte/icons/layout-dashboard";
  import LogIn from "@lucide/svelte/icons/log-in";
  import Home from "@lucide/svelte/icons/home";
  import { cn } from "$lib/utils";

  let email = $state("");
  let status = $state("idle"); // 'idle' | 'loading' | 'success' | 'error'
  let message = $state("");
  let showLoginModal = $state(false);
  let isMobileMenuOpen = $state(false);

  const toggleMobileMenu = () => {
    isMobileMenuOpen = !isMobileMenuOpen;
  };

  const joinWaitlist = async () => {
    if (!email || !email.includes("@")) {
      status = "error";
      message = "Please enter a valid email address.";
      return;
    }

    status = "loading";
    message = "";

    try {
      const response = await authFetch("/api/v1/waitlist", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email }),
      });

      if (response.ok) {
        status = "success";
        message = "Thanks for joining! We'll be in touch soon.";
        email = "";
      } else {
        const text = await response.text();
        status = "error";
        try {
          const errorData = JSON.parse(text);
          message =
            errorData.message ||
            errorData.details ||
            "Something went wrong. Please try again.";
        } catch (e) {
          message = text || "Something went wrong. Please try again.";
        }
      }
    } catch (err) {
      if (import.meta.env.DEV) {
        console.error(err);
      }
      status = "error";
      message =
        "Failed to connect to the server. Please check your connection.";
    }
  };

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "SoftwareApplication",
    name: "Cubrain",
    applicationCategory: "EducationalApplication",
    operatingSystem: "Web",
    description: "AI-powered study tool that converts PDFs into quizzes.",
  };
</script>

<svelte:head>
  {@html `<script type="application/ld+json">${JSON.stringify(jsonLd)}</script>`}
</svelte:head>

<div
  class="min-h-screen flex flex-col bg-background text-foreground overflow-x-hidden"
>
  <nav
    class="fixed top-4 left-1/2 -translate-x-1/2 w-[95%] md:w-[90%] max-w-6xl px-6 md:px-8 py-4 flex justify-between items-center z-50 bg-black/50 backdrop-blur-xl border border-white/10 rounded-full shadow-2xl"
  >
    <a href="/" class="z-50">
      <img
        src="/logo-gold.png"
        alt="Cubrain AI Logo"
        class="h-8 md:h-10 w-auto object-contain"
      />
    </a>

    <!-- Desktop Menu -->
    <div class="hidden md:flex gap-8 items-center">
      <a
        href="#features"
        class="text-sm font-medium text-white/80 hover:text-[#FFD700] transition-colors"
        >Features</a
      >
      <a
        href="/dashboard"
        class="text-sm font-medium text-white/80 hover:text-[#FFD700] transition-colors"
        >Dashboard</a
      >
      {#if user.current}
        <a
          href="/dashboard"
          class="px-5 py-2.5 text-sm rounded-full font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black hover:shadow-[0_0_20px_rgba(255,215,0,0.4)] transition-all transform hover:-translate-y-0.5"
          >Go to Dashboard</a
        >
      {:else}
        <div class="flex items-center gap-4">
          <button
            onclick={() => (showLoginModal = true)}
            class="text-sm font-medium text-white/60 hover:text-white transition-colors"
            >Sign In</button
          >
          <a
            href="#waitlist"
            class="px-5 py-2.5 text-sm rounded-full font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black hover:shadow-[0_0_20px_rgba(255,215,0,0.4)] transition-all transform hover:-translate-y-0.5"
            >Get Early Access</a
          >
        </div>
      {/if}
    </div>

    <!-- Mobile Menu Button -->
    <button
      class="md:hidden z-50 p-2 text-white/80 hover:text-white transition-colors"
      onclick={toggleMobileMenu}
      aria-label="Toggle Menu"
    >
      <Menu class="w-6 h-6" />
    </button>

    <!-- Mobile Sidebar Drawer -->
    {#if isMobileMenuOpen}
      <!-- Backdrop -->
      <div
        class="fixed inset-0 bg-black/60 backdrop-blur-sm z-[60] md:hidden"
        onclick={toggleMobileMenu}
        onkeydown={(e) => e.key === "Escape" && toggleMobileMenu()}
        role="button"
        tabindex="0"
        aria-label="Close Menu"
        transition:fade={{ duration: 200 }}
      ></div>

      <!-- Sidebar Panel -->
      <div
        class="fixed inset-y-0 right-0 w-[280px] bg-zinc-900 border-l border-white/10 z-[70] md:hidden flex flex-col shadow-2xl"
        transition:fly={{ x: 280, duration: 300, opacity: 1 }}
      >
        <!-- Sidebar Header -->
        <div
          class="flex items-center justify-between p-6 border-b border-white/5"
        >
          <img
            src="/logo-gold.png"
            alt="Cubrain AI Logo"
            class="h-8 w-auto object-contain"
          />
          <button
            onclick={toggleMobileMenu}
            class="p-2 text-white/60 hover:text-white hover:bg-white/5 rounded-lg transition-all"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <!-- Sidebar Content -->
        <div class="flex-1 overflow-y-auto py-6 px-4 space-y-2">
          <a
            href="/"
            onclick={toggleMobileMenu}
            class="flex items-center gap-4 px-4 py-3 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all group"
          >
            <Home class="w-5 h-5 text-zinc-500 group-hover:text-[#FFD700]" />
            <span class="font-medium">Home</span>
          </a>
          <a
            href="#features"
            onclick={toggleMobileMenu}
            class="flex items-center gap-4 px-4 py-3 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all group"
          >
            <Brain class="w-5 h-5 text-zinc-500 group-hover:text-[#FFD700]" />
            <span class="font-medium">Features</span>
          </a>
          <a
            href="/dashboard"
            onclick={toggleMobileMenu}
            class="flex items-center gap-4 px-4 py-3 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all group"
          >
            <LayoutDashboard
              class="w-5 h-5 text-zinc-500 group-hover:text-[#FFD700]"
            />
            <span class="font-medium">Dashboard</span>
          </a>
        </div>

        <!-- Sidebar Footer -->
        <div class="p-6 border-t border-white/5 space-y-4">
          {#if user.current}
            <a
              href="/dashboard"
              onclick={toggleMobileMenu}
              class="flex items-center justify-center gap-2 w-full py-4 rounded-xl font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-lg"
            >
              <LayoutDashboard class="w-5 h-5" />
              Go to Dashboard
            </a>
          {:else}
            <button
              onclick={() => {
                toggleMobileMenu();
                showLoginModal = true;
              }}
              class="flex items-center justify-center gap-2 w-full py-4 rounded-xl font-medium text-white/60 hover:text-white hover:bg-white/5 border border-white/10 transition-all"
            >
              <LogIn class="w-5 h-5" />
              Sign In
            </button>
            <a
              href="#waitlist"
              onclick={toggleMobileMenu}
              class="flex items-center justify-center gap-2 w-full py-4 rounded-xl font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-lg"
            >
              <Zap class="w-5 h-5" />
              Get Early Access
            </a>
          {/if}
        </div>
      </div>
    {/if}
  </nav>

  <main
    class={cn(
      "flex-1 transition-all duration-500 ease-in-out",
      isMobileMenuOpen
        ? "blur-md brightness-50 scale-[0.98] pointer-events-none"
        : ""
    )}
  >
    <header
      class="pt-32 md:pt-40 pb-12 md:pb-20 grid grid-cols-1 md:grid-cols-2 gap-12 md:gap-16 max-w-6xl mx-auto items-center min-h-[90vh] px-6"
    >
      <div class="text-center md:text-left z-10">
        <h1
          class="text-4xl sm:text-5xl md:text-7xl font-extrabold leading-tight mb-6 md:mb-8 text-white tracking-tight"
        >
          Turn Your PDFs into <br class="hidden sm:block" />
          <span
            class="bg-linear-to-r from-[#FFD700] via-[#FDB931] to-[#FFD700] bg-clip-text text-transparent bg-size-[200%_auto] animate-gradient"
            >Flashcards</span
          >
          in Seconds.
        </h1>
        <p
          class="text-base md:text-xl text-white/60 mb-8 md:mb-10 max-w-lg mx-auto md:mx-0 leading-relaxed"
        >
          Zero hallucinations. Zero manual entry. <br class="hidden md:block" />
          Every flashcard is backed by a direct link to the source text, turning
          your PDFs into an interactive knowledge base.
        </p>
        <div
          class="flex flex-col sm:flex-row gap-4 justify-center md:justify-start"
        >
          <a
            href="#waitlist"
            class="px-8 py-4 rounded-xl font-bold text-lg bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_20px_rgba(255,215,0,0.3)] hover:shadow-[0_0_30px_rgba(255,215,0,0.5)] transition-all transform hover:-translate-y-1 text-center"
            >Join the Waitlist</a
          >
          <button
            class="px-8 py-4 rounded-xl font-bold text-lg bg-white/5 text-white border border-white/10 hover:bg-white/10 hover:border-white/20 backdrop-blur-sm transition-all text-center"
            >Learn More</button
          >
        </div>
      </div>
      <div class="relative z-10 group perspective-1000">
        <div
          class="relative transform transition-transform duration-500 group-hover:rotate-y-2 group-hover:rotate-x-2"
        >
          <div
            class="absolute -inset-1 bg-linear-to-r from-[#FFD700] to-[#FDB931] rounded-2xl blur opacity-20 group-hover:opacity-40 transition duration-1000 group-hover:duration-200"
          ></div>
          <img
            src={heroImage}
            alt="Cubrain Interface"
            class="relative w-full rounded-2xl shadow-2xl border border-white/10 bg-black/50"
          />
        </div>
        <!-- Ambient Glow -->
        <div
          class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[150%] h-[150%] bg-[#FFD700]/10 blur-[100px] -z-10 pointer-events-none"
        ></div>
      </div>
    </header>

    <section class="py-20 px-4 relative">
      <div
        class="absolute inset-0 bg-linear-to-b from-transparent via-[#FFD700]/5 to-transparent pointer-events-none"
      ></div>
      <FlashcardDemo />
    </section>

    <section id="features" class="py-24 px-6 max-w-7xl mx-auto">
      <div class="text-center mb-20">
        <h2 class="text-4xl md:text-5xl font-bold mb-6 text-white">
          Why <span class="text-[#FFD700]">Cubrain</span>?
        </h2>
        <p class="text-white/60 max-w-2xl mx-auto text-lg">
          Experience the next generation of study tools designed for efficiency
          and retention.
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- Feature 1 -->
        <div
          class="group p-8 rounded-2xl bg-white/5 backdrop-blur-sm border border-white/10 hover:border-[#FFD700]/50 hover:bg-white/[0.07] transition-all duration-300 hover:-translate-y-1"
        >
          <div
            class="w-12 h-12 rounded-lg bg-[#FFD700]/10 flex items-center justify-center mb-6 group-hover:bg-[#FFD700]/20 transition-colors"
          >
            <Zap class="w-6 h-6 text-[#FFD700]" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-white">Instant Capture</h3>
          <p class="text-white/60 leading-relaxed">
            Upload your PDFs and instantly turn them into flashcards. No context
            switching required.
          </p>
        </div>

        <!-- Feature 2 -->
        <div
          class="group p-8 rounded-2xl bg-white/5 backdrop-blur-sm border border-white/10 hover:border-[#FFD700]/50 hover:bg-white/[0.07] transition-all duration-300 hover:-translate-y-1"
        >
          <div
            class="w-12 h-12 rounded-lg bg-[#FFD700]/10 flex items-center justify-center mb-6 group-hover:bg-[#FFD700]/20 transition-colors"
          >
            <Brain class="w-6 h-6 text-[#FFD700]" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-white">Context Aware</h3>
          <p class="text-white/60 leading-relaxed">
            Cubrain understands the surrounding text to create better, more
            meaningful questions that test true understanding.
          </p>
        </div>

        <!-- Feature 3 -->
        <div
          class="group p-8 rounded-2xl bg-white/5 backdrop-blur-sm border border-white/10 hover:border-[#FFD700]/50 hover:bg-white/[0.07] transition-all duration-300 hover:-translate-y-1"
        >
          <div
            class="w-12 h-12 rounded-lg bg-[#FFD700]/10 flex items-center justify-center mb-6 group-hover:bg-[#FFD700]/20 transition-colors"
          >
            <RefreshCw class="w-6 h-6 text-[#FFD700]" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-white">Seamless Sync</h3>
          <p class="text-white/60 leading-relaxed">
            Automatically syncs with Anki, Notion, and other tools you already
            use, fitting perfectly into your workflow.
          </p>
        </div>

        <!-- Feature 4 -->
        <div
          class="group p-8 rounded-2xl bg-white/5 backdrop-blur-sm border border-white/10 hover:border-[#FFD700]/50 hover:bg-white/[0.07] transition-all duration-300 hover:-translate-y-1"
        >
          <div
            class="w-12 h-12 rounded-lg bg-[#FFD700]/10 flex items-center justify-center mb-6 group-hover:bg-[#FFD700]/20 transition-colors"
          >
            <CheckCircle2 class="w-6 h-6 text-[#FFD700]" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-white">AI Grading</h3>
          <p class="text-white/60 leading-relaxed">
            Get instant feedback on your answers. The AI acts as your personal
            tutor, explaining why you were right or wrong.
          </p>
        </div>
      </div>
    </section>

    <section id="waitlist" class="py-24 px-6 flex justify-center relative">
      <!-- Background Glow -->
      <div
        class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] h-[80%] bg-[#FFD700]/5 blur-[120px] -z-10 pointer-events-none"
      ></div>

      <div
        class="bg-black/40 backdrop-blur-xl border border-white/10 p-8 md:p-12 rounded-3xl shadow-2xl text-center max-w-3xl w-full relative overflow-hidden"
      >
        <div
          class="absolute top-0 left-0 w-full h-1 bg-linear-to-r from-transparent via-[#FFD700] to-transparent opacity-50"
        ></div>

        <h2 class="text-3xl md:text-4xl font-bold mb-4 text-white">
          Join the Waitlist
        </h2>
        <p class="text-white/60 mb-10 text-base md:text-lg">
          Be the first to experience the future of learning. Limited spots
          available.
        </p>

        <form
          class="flex flex-col sm:flex-row gap-4 max-w-lg mx-auto"
          onsubmit={(e) => {
            e.preventDefault();
            joinWaitlist();
          }}
        >
          <input
            type="email"
            placeholder="Enter your email address"
            required
            bind:value={email}
            disabled={status === "loading" || status === "success"}
            class="flex-1 px-6 py-4 rounded-xl border border-white/10 bg-white/5 text-white placeholder:text-white/30 focus:outline-none focus:border-[#FFD700]/50 focus:ring-1 focus:ring-[#FFD700]/50 transition-all disabled:opacity-50"
          />
          <button
            type="submit"
            class="px-8 py-4 rounded-xl font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black hover:shadow-[0_0_20px_rgba(255,215,0,0.3)] disabled:opacity-50 transition-all whitespace-nowrap transform hover:-translate-y-0.5"
            disabled={status === "loading" || status === "success"}
          >
            {#if status === "loading"}
              Joining...
            {:else if status === "success"}
              ✓ Joined!
            {:else}
              Join Now
            {/if}
          </button>
        </form>
        {#if message}
          <p
            class="mt-6 font-medium"
            style="color: {status === 'success' ? '#4ade80' : '#f87171'};"
          >
            {message}
          </p>
        {/if}
      </div>
    </section>

    <footer class="text-center py-12 text-white/40 border-t border-white/5">
      <p>&copy; 2025 Cubrain. All rights reserved.</p>
    </footer>
  </main>
</div>

{#if showLoginModal}
  <LoginModal onclose={() => (showLoginModal = false)} />
{/if}
