<script lang="ts">
  import FlashcardDemo from "$lib/components/FlashcardDemo.svelte";
  import { onMount } from "svelte";
  import { authFetch } from "$lib/api";
  import { fade, fly } from "svelte/transition";
  import { user } from "$lib/stores/user.svelte";
  import { IS_LAUNCH_SALE } from "$lib/config/config";
  import UpgradeButton from "$lib/components/UpgradeButton.svelte";
  import LoginModal from "$lib/components/auth/LoginModal.svelte";
  import Menu from "@lucide/svelte/icons/menu";
  import X from "@lucide/svelte/icons/x";
  import Zap from "@lucide/svelte/icons/zap";
  import Star from "@lucide/svelte/icons/star";
  import Brain from "@lucide/svelte/icons/brain";
  import RefreshCw from "@lucide/svelte/icons/refresh-cw";
  import CheckCircle2 from "@lucide/svelte/icons/check-circle-2";
  import Tag from "@lucide/svelte/icons/tag";
  import FileText from "@lucide/svelte/icons/file-text";
  import ShieldCheck from "@lucide/svelte/icons/shield-check";
  import GraduationCap from "@lucide/svelte/icons/graduation-cap";
  import LayoutDashboard from "@lucide/svelte/icons/layout-dashboard";
  import LogIn from "@lucide/svelte/icons/log-in";
  import Home from "@lucide/svelte/icons/home";
  import ArrowRight from "@lucide/svelte/icons/arrow-right";
  import { cn } from "$lib/utils";

  let showLoginModal = $state(false);
  let isMobileMenuOpen = $state(false);

  const toggleMobileMenu = () => {
    isMobileMenuOpen = !isMobileMenuOpen;
  };

  onMount(() => {
    // Initialize Lemon Squeezy if available
    if (window.createLemonSqueezy) {
      window.createLemonSqueezy();
    }

    // Safely inject JSON-LD without using {@html} in the template
    const script = document.createElement("script");
    script.type = "application/ld+json";
    script.text = JSON.stringify(jsonLd);
    document.head.appendChild(script);
  });

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
  <!-- JSON-LD is injected via onMount for security and compliance with Rule 19 -->
</svelte:head>

<div
  class="min-h-screen flex flex-col bg-background text-foreground overflow-x-hidden"
>
  <nav
    class="relative w-full px-6 md:px-12 py-6 flex justify-between items-center z-40 bg-black border-b border-white/5"
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
        class="text-lg font-medium text-white/80 hover:text-[#FFD700] transition-colors"
        >Features</a
      >
      <a
        href="#pricing"
        class="text-lg font-medium text-white/80 hover:text-[#FFD700] transition-colors"
        >Pricing</a
      >
      <a
        href="/library"
        class="text-lg font-medium text-white/80 hover:text-[#FFD700] transition-colors"
        >Library</a
      >
      {#if user.current}
        <a
          href="/library"
          class="px-5 py-2.5 text-lg rounded-full font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black hover:shadow-[0_0_20px_rgba(255,215,0,0.4)] transition-all transform hover:-translate-y-0.5"
          >Start Learning</a
        >
      {:else}
        <a
          href="/upload"
          class="px-5 py-2.5 text-lg rounded-full font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black hover:shadow-[0_0_20px_rgba(255,215,0,0.4)] transition-all transform hover:-translate-y-0.5"
          >Get Started</a
        >
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
        class="fixed inset-0 bg-black/40 backdrop-blur-md z-100 md:hidden"
        onclick={toggleMobileMenu}
        onkeydown={(e) => e.key === "Escape" && toggleMobileMenu()}
        role="button"
        tabindex="0"
        aria-label="Close Menu"
        transition:fade={{ duration: 200 }}
      ></div>

      <!-- Sidebar Panel -->
      <div
        class="fixed inset-y-0 right-0 w-[300px] bg-zinc-950 border-l border-white/10 z-110 md:hidden flex flex-col shadow-2xl"
        transition:fly={{ x: 300, duration: 300, opacity: 1 }}
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
        <div class="flex-1 overflow-y-auto py-8 px-4 space-y-8">
          <!-- Navigation Section -->
          <div>
            <p
              class="px-4 text-[10px] font-bold tracking-widest text-white/30 uppercase mb-4"
            >
              Navigation
            </p>
            <div class="space-y-1">
              <a
                href="/"
                onclick={toggleMobileMenu}
                class="flex items-center gap-4 px-4 py-3.5 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all group"
              >
                <div
                  class="w-8 h-8 rounded-lg bg-white/5 flex items-center justify-center group-hover:bg-[#FFD700]/10 transition-colors"
                >
                  <Home
                    class="w-4 h-4 text-zinc-500 group-hover:text-[#FFD700]"
                  />
                </div>
                <span class="font-medium">Home</span>
              </a>
              <a
                href="#features"
                onclick={toggleMobileMenu}
                class="flex items-center gap-4 px-4 py-3.5 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all group"
              >
                <div
                  class="w-8 h-8 rounded-lg bg-white/5 flex items-center justify-center group-hover:bg-[#FFD700]/10 transition-colors"
                >
                  <Brain
                    class="w-4 h-4 text-zinc-500 group-hover:text-[#FFD700]"
                  />
                </div>
                <span class="font-medium">Features</span>
              </a>
              <a
                href="#pricing"
                onclick={toggleMobileMenu}
                class="flex items-center gap-4 px-4 py-3.5 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all group"
              >
                <div
                  class="w-8 h-8 rounded-lg bg-white/5 flex items-center justify-center group-hover:bg-[#FFD700]/10 transition-colors"
                >
                  <Tag
                    class="w-4 h-4 text-zinc-500 group-hover:text-[#FFD700]"
                  />
                </div>
                <span class="font-medium">Pricing</span>
              </a>
              <a
                href="/library"
                onclick={toggleMobileMenu}
                class="flex items-center gap-4 px-4 py-3.5 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all group"
              >
                <div
                  class="w-8 h-8 rounded-lg bg-white/5 flex items-center justify-center group-hover:bg-[#FFD700]/10 transition-colors"
                >
                  <LayoutDashboard
                    class="w-4 h-4 text-zinc-500 group-hover:text-[#FFD700]"
                  />
                </div>
                <span class="font-medium">My Library</span>
              </a>
            </div>
          </div>
        </div>

        <!-- Sidebar Footer -->
        <div class="p-6 border-t border-white/5 space-y-4 bg-zinc-900/50">
          <p
            class="px-4 text-[10px] font-bold tracking-widest text-white/30 uppercase mb-2"
          >
            Account
          </p>
          {#if user.current}
            <a
              href="/library"
              onclick={toggleMobileMenu}
              class="flex items-center justify-center gap-3 w-full py-4 rounded-xl font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_20px_rgba(255,215,0,0.2)] active:scale-[0.98] transition-all"
            >
              <LayoutDashboard class="w-5 h-5" />
              Start Learning
            </a>
          {:else}
            <button
              onclick={() => {
                toggleMobileMenu();
                showLoginModal = true;
              }}
              class="flex items-center justify-center gap-3 w-full py-4 rounded-xl font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_20px_rgba(255,215,0,0.2)] active:scale-[0.98] transition-all"
            >
              <Zap class="w-5 h-5" />
              Get Started
            </button>
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
          Soak your brain with AI-generated flashcards. <br
            class="hidden md:block"
          />
          Every flashcard is backed by your PDF file and keeps the text context to
          ensure zero hallucinations.
        </p>
        <div
          class="flex flex-col sm:flex-row gap-4 justify-center md:justify-start"
        >
          {#if user.current}
            <a
              href="/library"
              class="px-8 py-4 rounded-xl font-bold text-lg bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_30px_rgba(255,215,0,0.3)] hover:shadow-[0_0_40px_rgba(255,215,0,0.5)] transition-all transform hover:-translate-y-1 text-center"
              >Start Learning</a
            >
          {:else}
            <button
              onclick={() => (showLoginModal = true)}
              class="px-8 py-4 rounded-xl font-bold text-lg bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_30px_rgba(255,215,0,0.3)] hover:shadow-[0_0_40px_rgba(255,215,0,0.5)] transition-all transform hover:-translate-y-1 text-center"
              >Start for Free</button
            >
          {/if}
          <a
            href="#pricing"
            class="px-8 py-4 rounded-xl font-bold text-lg bg-white/5 text-white border border-white/10 hover:bg-white/10 hover:border-white/20 backdrop-blur-sm transition-all text-center"
            >View Pricing</a
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
            src="/og-image.png"
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

      <div class="mt-12 text-center">
        <p class="text-white/60 mb-6 text-lg">Ready to process your own PDF?</p>
        <a
          href="/upload"
          class="inline-flex items-center gap-2 px-8 py-4 rounded-xl font-bold text-lg bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_20px_rgba(255,215,0,0.2)] hover:shadow-[0_0_30px_rgba(255,215,0,0.4)] transition-all transform hover:-translate-y-1"
        >
          Upload Now
          <ArrowRight class="w-5 h-5" />
        </a>
      </div>
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
            <GraduationCap class="w-6 h-6 text-[#FFD700]" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-white">Smart Study</h3>
          <p class="text-white/60 leading-relaxed">
            Practice directly in your browser with our built-in flashcard
            player. Track your progress and master concepts faster.
          </p>
        </div>

        <!-- Feature 4 -->
        <div
          class="group p-8 rounded-2xl bg-white/5 backdrop-blur-sm border border-white/10 hover:border-[#FFD700]/50 hover:bg-white/[0.07] transition-all duration-300 hover:-translate-y-1"
        >
          <div
            class="w-12 h-12 rounded-lg bg-[#FFD700]/10 flex items-center justify-center mb-6 group-hover:bg-[#FFD700]/20 transition-colors"
          >
            <FileText class="w-6 h-6 text-[#FFD700]" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-white">Easy Export</h3>
          <p class="text-white/60 leading-relaxed">
            Export your generated flashcards to .csv format instantly. Perfectly
            formatted for Anki and other major study tools.
          </p>
        </div>
      </div>
    </section>

    <section id="pricing" class="py-24 px-6 relative">
      <!-- Background Glow -->
      <div
        class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] h-[80%] bg-[#FFD700]/5 blur-[120px] -z-10 pointer-events-none"
      ></div>

      <div class="max-w-5xl mx-auto">
        <div class="text-center mb-16">
          <h2 class="text-4xl md:text-5xl font-bold mb-6 text-white">
            Simple, <span class="text-[#FFD700]">Transparent</span> Pricing
          </h2>
          <p class="text-white/60 max-w-2xl mx-auto text-lg">
            Choose the plan that fits your study needs.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 items-stretch">
          <!-- Monthly Plan -->
          <div
            class="flex flex-col p-8 rounded-3xl bg-white/5 border border-white/10 backdrop-blur-sm relative overflow-hidden group hover:border-white/20 transition-all"
          >
            <div class="mb-8">
              <h3 class="text-xl font-bold text-white mb-4">Monthly</h3>
              <div class="flex flex-col gap-1">
                {#if IS_LAUNCH_SALE}
                  <span class="text-lg text-white/40 line-through font-medium"
                    >$14.99</span
                  >
                  <div class="flex items-baseline gap-1">
                    <span
                      class="text-6xl font-extrabold text-white tracking-tighter"
                      >$5<sup class="text-3xl font-bold ml-0.5">.99</sup></span
                    >
                    <span class="text-white/40 font-medium ml-2">/ month</span>
                  </div>
                {:else}
                  <div class="flex items-baseline gap-1">
                    <span
                      class="text-6xl font-extrabold text-white tracking-tighter"
                      >$14<sup class="text-3xl font-bold ml-0.5">.99</sup></span
                    >
                    <span class="text-white/40 font-medium ml-2">/ month</span>
                  </div>
                {/if}
              </div>
            </div>

            <ul class="space-y-4 mb-10 flex-1">
              <li class="flex items-center gap-3 text-[#FFD700] font-bold">
                <Star class="w-5 h-5 fill-current" />
                <span>60% OFF Launch Special</span>
              </li>
              <li class="flex items-center gap-3 text-white/70">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Unlimited daily uploads</span>
              </li>
              <li class="flex items-center gap-3 text-white/70">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Full Book Support (1,000 pages)</span>
              </li>
              <li class="flex items-center gap-3 text-white/70">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Priority AI processing</span>
              </li>
              <li class="flex items-center gap-3 text-white/70">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Anki Export Support (.csv)</span>
              </li>
              <li class="flex items-center gap-3 text-white/70">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Deep Synthesis (Up to 5 cards/page)</span>
              </li>
            </ul>

            <UpgradeButton
              class="w-full h-14"
              text="Upgrade to Monthly"
              variantId="646b9e10-0039-4c37-bb30-2ffa5fa2b32f"
              onLoginRequired={() => (showLoginModal = true)}
            />
          </div>

          <div
            class="flex flex-col p-8 rounded-3xl bg-zinc-900 border-2 {IS_LAUNCH_SALE
              ? 'border-[#FFD700] shadow-[0_0_40px_rgba(255,215,0,0.15)]'
              : 'border-white/10'} relative overflow-hidden group"
          >
            {#if IS_LAUNCH_SALE}
              <!-- Badge -->
              <div
                class="absolute top-0 right-0 bg-[#FFD700] text-black px-4 py-1.5 rounded-bl-xl text-xs font-black flex items-center gap-1.5 tracking-wider"
              >
                <Star class="w-3.5 h-3.5 fill-current" />
                BEST VALUE
              </div>
            {/if}

            <div class="mb-8">
              <h3 class="text-xl font-bold text-white mb-4">Annual</h3>
              <div class="flex flex-col gap-1">
                {#if IS_LAUNCH_SALE}
                  <span class="text-lg text-white/40 line-through font-medium"
                    >$143.90</span
                  >
                  <div class="flex items-baseline gap-1">
                    <span
                      class="text-6xl font-extrabold text-[#FFD700] tracking-tighter"
                      >$39<sup class="text-3xl font-bold ml-0.5">.99</sup></span
                    >
                    <span class="text-white/40 font-medium ml-2">/ year</span>
                  </div>
                {:else}
                  <div class="flex items-baseline gap-1">
                    <span
                      class="text-6xl font-extrabold text-white tracking-tighter"
                      >$179<sup class="text-3xl font-bold ml-0.5">.88</sup
                      ></span
                    >
                    <span class="text-white/40 font-medium ml-2">/ year</span>
                  </div>
                {/if}
              </div>
            </div>

            <ul class="space-y-4 mb-10 flex-1">
              <li class="flex items-center gap-3 text-white">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Everything in Monthly</span>
              </li>
              {#if IS_LAUNCH_SALE}
                <li class="flex items-center gap-3 text-white">
                  <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                  <span class="font-bold text-[#FFD700]"
                    >Save 72% instantly</span
                  >
                </li>
              {/if}
              <li class="flex items-center gap-3 text-white">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Unlimited daily uploads</span>
              </li>
              <li class="flex items-center gap-3 text-white">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Full Book Support</span>
              </li>
              <li class="flex items-center gap-3 text-white">
                <CheckCircle2 class="w-5 h-5 text-[#FFD700]" />
                <span>Priority Support</span>
              </li>
            </ul>

            <UpgradeButton
              class="w-full h-14 {IS_LAUNCH_SALE
                ? 'bg-linear-to-r from-[#FFD700] to-[#FDB931]'
                : ''}"
              text={IS_LAUNCH_SALE
                ? "Upgrade to Yearly 👑"
                : "Upgrade to Yearly"}
              variantId="YOUR_ANNUAL_VARIANT_ID"
              onLoginRequired={() => (showLoginModal = true)}
            />
          </div>
        </div>
      </div>
    </section>

    <footer class="py-16 px-6 border-t border-white/5">
      <div
        class="max-w-6xl mx-auto flex flex-col md:flex-row justify-between items-center gap-8"
      >
        <div class="flex flex-col items-center md:items-start gap-4">
          <img
            src="/logo-gold.png"
            alt="Cubrain AI Logo"
            class="h-8 w-auto opacity-80"
          />
          <p class="text-white/40 text-sm">
            &copy; 2026 Cubrain. All rights reserved.
          </p>
        </div>

        <div class="flex gap-8">
          <a
            href="/terms"
            class="text-white/40 hover:text-[#FFD700] text-sm transition-colors"
            >Terms of Service</a
          >
          <a
            href="/privacy"
            class="text-white/40 hover:text-[#FFD700] text-sm transition-colors"
            >Privacy Policy</a
          >
        </div>
      </div>
    </footer>
  </main>
</div>

{#if showLoginModal}
  <LoginModal onclose={() => (showLoginModal = false)} />
{/if}
