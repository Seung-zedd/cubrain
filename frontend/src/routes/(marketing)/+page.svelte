<script lang="ts">
  import FlashcardDemo from "$lib/components/FlashcardDemo.svelte";
  import ContextAwareFeature from "$lib/components/marketing/ContextAwareFeature.svelte";
  import HeroVideo from "$lib/components/HeroVideo.svelte";
  import { onMount } from "svelte";
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
  import CheckCircle2 from "@lucide/svelte/icons/check-circle-2";
  import Tag from "@lucide/svelte/icons/tag";
  import FileText from "@lucide/svelte/icons/file-text";
  import GraduationCap from "@lucide/svelte/icons/graduation-cap";
  import LayoutDashboard from "@lucide/svelte/icons/layout-dashboard";
  import Home from "@lucide/svelte/icons/home";
  import ArrowRight from "@lucide/svelte/icons/arrow-right";
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import Play from "@lucide/svelte/icons/play";
  import { cn } from "$lib/utils";

  let showLoginModal = $state(false);
  let showDemoModal = $state(false);
  let isMobileMenuOpen = $state(false);

  const toggleMobileMenu = () => {
    isMobileMenuOpen = !isMobileMenuOpen;
  };

  const CURRENT_VERSION = "1.4.4";
  let hasNewUpdate = $state(false);

  onMount(() => {
    // Initialize Lemon Squeezy if available
    if (window.createLemonSqueezy) {
      window.createLemonSqueezy();
    }

    const seenVersion = localStorage.getItem("cubrain_whats_new_seen");
    hasNewUpdate = seenVersion !== CURRENT_VERSION;

    // Safely inject JSON-LD without using {@html} in the template
    const script = document.createElement("script");
    script.type = "application/ld+json";
    script.text = JSON.stringify(jsonLd);
    document.head.appendChild(script);

    const interval = setInterval(() => {
      currentSlide = (currentSlide + 1) % heroSlides.length;
    }, 4000);

    return () => clearInterval(interval);
  });

  const heroSlides = [
    {
      h: 'Stop <span class="text-[#FFD700]">soul-crushing</span> typing.',
      s: "Turn highlights into flashcards instantly.",
    },
    {
      h: 'Is prep your <span class="text-[#FFD700]">bottleneck</span>?',
      s: "Generate flashcards in seconds, not hours.",
    },
    {
      h: 'Tired of <span class="text-[#FFD700]">random, low-quality</span> AI cards?',
      s: "Generate precise flashcards strictly from highlights.",
    },
  ];

  let currentSlide = $state(0);

  function markAsSeen() {
    localStorage.setItem("cubrain_whats_new_seen", CURRENT_VERSION);
    hasNewUpdate = false;
  }

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
  <link rel="preload" href="/og-image.png" as="image" fetchpriority="high" />
</svelte:head>

<div class="min-h-screen flex flex-col bg-background text-foreground">
  <nav
    class="sticky top-0 left-0 w-full px-6 md:px-12 py-6 flex justify-between items-center z-100 bg-[#050505]/95 backdrop-blur-xl border-b border-white/10 shadow-2xl"
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
        href="/whats-new"
        onclick={markAsSeen}
        class="text-lg font-medium text-white/80 hover:text-[#FFD700] transition-colors flex items-center gap-2"
      >
        What's New
        {#if hasNewUpdate}
          <span class="relative flex h-2 w-2">
            <span
              class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"
            ></span>
            <span class="relative inline-flex rounded-full h-2 w-2 bg-red-500"
            ></span>
          </span>
        {/if}
      </a>
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
                href="/whats-new"
                onclick={() => {
                  markAsSeen();
                  toggleMobileMenu();
                }}
                class="flex items-center gap-4 px-4 py-3.5 rounded-xl text-white/70 hover:text-white hover:bg-white/5 transition-all group"
              >
                <div
                  class="w-8 h-8 rounded-lg bg-white/5 flex items-center justify-center group-hover:bg-[#FFD700]/10 transition-colors"
                >
                  <Sparkles
                    class="w-4 h-4 text-zinc-500 group-hover:text-[#FFD700]"
                  />
                </div>
                <div class="flex-1 flex items-center justify-between">
                  <span class="font-medium">What's New</span>
                  {#if hasNewUpdate}
                    <span class="relative flex h-2 w-2">
                      <span
                        class="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"
                      ></span>
                      <span
                        class="relative inline-flex rounded-full h-2 w-2 bg-red-500"
                      ></span>
                    </span>
                  {/if}
                </div>
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
            <a
              href="/upload"
              onclick={toggleMobileMenu}
              class="flex items-center justify-center gap-3 w-full py-4 rounded-xl font-bold bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_20px_rgba(255,215,0,0.2)] active:scale-[0.98] transition-all"
            >
              <Zap class="w-5 h-5" />
              Get Started
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
        : "",
    )}
  >
    <header
      class="pt-32 md:pt-48 pb-20 flex flex-col items-center text-center max-w-7xl mx-auto min-h-screen px-6 overflow-hidden"
    >
      <div class="z-10 w-full max-w-4xl mx-auto">
        <!-- Hero Carousel Container -->
        <div
          class="min-h-[220px] md:min-h-[280px] lg:min-h-[320px] flex flex-col justify-center items-center"
        >
          {#key currentSlide}
            <div
              in:fly={{ y: 20, duration: 400 }}
              class="space-y-6 md:space-y-8"
            >
              <h1
                class="text-5xl sm:text-7xl lg:text-8xl font-black leading-[1.1] text-white tracking-tight"
              >
                {@html heroSlides[currentSlide].h}
              </h1>
              <p
                class="text-base md:text-xl text-white/60 max-w-2xl mx-auto leading-relaxed"
              >
                {heroSlides[currentSlide].s}
              </p>
            </div>
          {/key}
        </div>

        <div
          class="flex flex-col sm:flex-row gap-4 justify-center mt-8 md:mt-12"
        >
          {#if user.current}
            <a
              href="/library"
              class="px-10 py-4 rounded-xl font-bold text-lg bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_30px_rgba(255,215,0,0.3)] hover:shadow-[0_0_40px_rgba(255,215,0,0.5)] transition-all transform hover:-translate-y-1 text-center"
              >Start Learning</a
            >
          {:else}
            <a
              href="/upload"
              class="px-10 py-4 rounded-xl font-bold text-lg bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_30px_rgba(255,215,0,0.3)] hover:shadow-[0_0_40px_rgba(255,215,0,0.5)] transition-all transform hover:-translate-y-1 text-center"
              >Try with your PDF</a
            >
          {/if}
          <a
            href="#pricing"
            class="px-10 py-4 rounded-xl font-bold text-lg bg-white/5 text-white border border-white/10 hover:bg-white/10 hover:border-white/20 backdrop-blur-sm transition-all text-center"
            >View Pricing</a
          >
          <button
            class="flex items-center justify-center gap-3 px-10 py-4 rounded-xl border border-white/10 bg-white/5 hover:bg-white/10 backdrop-blur-md transition-all group text-white font-bold text-lg"
            onclick={() => (showDemoModal = true)}
          >
            <div
              class="w-8 h-8 rounded-full bg-[#FFD700]/10 flex items-center justify-center text-[#FFD700] group-hover:bg-[#FFD700]/20 transition-all"
            >
              <Play class="w-4 h-4" fill="currentColor" />
            </div>
            <span>Watch Demo</span>
          </button>
        </div>
      </div>

      <!-- Massive Video Container -->
      <div class="relative z-10 w-full max-w-6xl mx-auto mt-20 md:mt-24 group">
        <!-- Massive Backdrop Glow -->
        <div
          class="absolute -inset-20 md:-inset-40 bg-[#FFD700]/10 blur-[120px] md:blur-[180px] rounded-full -z-10 pointer-events-none"
        ></div>

        <div
          class="relative rounded-2xl overflow-hidden border border-white/10 bg-zinc-950/50 shadow-[0_0_100px_-20px_rgba(255,193,7,0.2)] transition-all duration-700"
        >
          <video
            src="/videos/hero-bg.mp4"
            poster="/og-image.png"
            autoplay
            muted
            loop
            playsinline
            preload="auto"
            aria-label="Cubrain App Background Animation"
            class="w-full aspect-video object-cover"
          >
          </video>

          <!-- Glossy Overlay -->
          <div
            class="absolute inset-0 bg-linear-to-t from-zinc-950/20 to-transparent pointer-events-none"
          ></div>
        </div>
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

      <!-- New Context-Aware Primary Feature -->
      <div class="mb-24">
        <ContextAwareFeature />
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
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

        <!-- Feature 3 (Smart Study) -->
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

        <!-- Feature 4 (Easy Export) -->
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
                    >$19.99</span
                  >
                  <div class="flex items-baseline gap-1">
                    <span
                      class="text-6xl font-extrabold text-white tracking-tighter"
                      >$5<sup class="text-3xl font-bold ml-0.5">.59</sup></span
                    >
                    <span class="text-white/40 font-medium ml-2">/ month</span>
                  </div>
                {:else}
                  <div class="flex items-baseline gap-1">
                    <span
                      class="text-6xl font-extrabold text-white tracking-tighter"
                      >$19<sup class="text-3xl font-bold ml-0.5">.99</sup></span
                    >
                    <span class="text-white/40 font-medium ml-2">/ month</span>
                  </div>
                {/if}
              </div>
            </div>

            <ul class="space-y-4 mb-10 flex-1">
              <li class="flex items-center gap-3 text-[#FFD700] font-bold">
                <Star class="w-5 h-5 fill-current" />
                <span>72% OFF Launch Special</span>
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
              plan="monthly"
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
                    >$142.82</span
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
                      >$142<sup class="text-3xl font-bold ml-0.5">.82</sup
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
              plan="yearly"
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

{#if showDemoModal}
  <div
    class="fixed inset-0 z-100 flex items-center justify-center bg-black/90 backdrop-blur-sm p-4 animate-in fade-in duration-200"
    onclick={() => (showDemoModal = false)}
    onkeydown={(e) => e.key === "Escape" && (showDemoModal = false)}
    role="button"
    tabindex="0"
    aria-label="Close modal"
    transition:fade={{ duration: 200 }}
  >
    <div
      class="relative w-full max-w-5xl aspect-video bg-black rounded-2xl overflow-hidden shadow-2xl border border-white/10 ring-1 ring-white/10"
      onclick={(e) => e.stopPropagation()}
      onkeydown={(e) => e.stopPropagation()}
      role="dialog"
      aria-modal="true"
      tabindex="-1"
      transition:fly={{ y: 20, duration: 300 }}
    >
      <button
        class="absolute top-4 right-4 z-10 p-2 bg-black/50 hover:bg-white/20 rounded-full text-white/70 hover:text-white transition-colors"
        onclick={() => (showDemoModal = false)}
        aria-label="Close"
      >
        <X class="w-6 h-6" />
      </button>

      <video
        src="/videos/demo-full.mp4"
        controls
        autoplay
        crossorigin="anonymous"
        class="w-full h-full"
      >
        <track
          kind="captions"
          src="/captions/demo.vtt"
          srclang="en"
          label="English"
          default
        />
      </video>
    </div>
  </div>
{/if}
