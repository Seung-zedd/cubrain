<script lang="ts">
  import { fade, fly } from "svelte/transition";
  import Brain from "@lucide/svelte/icons/brain";
  import CheckCircle2 from "@lucide/svelte/icons/check-circle-2";
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import Zap from "@lucide/svelte/icons/zap";
  import { onMount } from "svelte";

  let isVisible = $state(false);
  let activeHighlight = $state(false);

  onMount(() => {
    isVisible = true;
    setTimeout(() => {
      activeHighlight = true;
    }, 1500);
  });
</script>

<section
  id="context-aware"
  class="py-32 md:py-48 px-6 max-w-7xl mx-auto overflow-hidden relative"
>
  <!-- Ambient Glow Background -->
  <div
    class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-4xl h-96 bg-amber-500/10 blur-[120px] rounded-full -z-10 pointer-events-none"
  ></div>

  <div class="flex flex-col gap-16 items-center">
    <!-- Header Side (Top, Centered) -->
    <div
      class="w-full flex flex-col items-center text-center space-y-8 max-w-4xl"
    >
      <div
        class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#FFC107]/10 border border-[#FFC107]/20 text-[#FFC107] text-xs font-black tracking-widest uppercase"
      >
        <Sparkles class="w-4 h-4" />
        The Proof of Truth
      </div>

      <h2
        class="text-5xl md:text-6xl lg:text-7xl font-black text-white leading-[1.05] tracking-tight"
      >
        Context-Aware <span class="text-[#FFC107]">Accuracy</span>
      </h2>

      <p class="text-xl text-white/60 leading-relaxed max-w-2xl mx-auto">
        Stop guessing. Cubrain links every flashcard back to the exact paragraph
        in your PDF. <span class="text-white font-medium"
          >Zero hallucinations, 100% traceability.</span
        >
      </p>

      <div class="flex flex-wrap justify-center gap-6 pt-4">
        {#each ["Precision Context Extraction", "Zero Hallucination Verified", "Direct Source Linking"] as feature, i}
          <div
            class="flex items-center gap-3 text-white/80 group/item"
            in:fly={{ y: 10, duration: 400, delay: 600 + i * 100 }}
          >
            <div
              class="w-6 h-6 rounded-full bg-zinc-900 border border-white/10 flex items-center justify-center text-[#FFC107] group-hover/item:border-[#FFC107]/50 transition-colors"
            >
              <CheckCircle2 class="w-4 h-4" />
            </div>
            <span class="text-base font-bold tracking-tight">{feature}</span>
          </div>
        {/each}
      </div>
    </div>

    <!-- Visual Mockup (Bottom, Full Width Glass Stage) -->
    <div
      class="w-full relative group"
      in:fly={{ y: 20, duration: 800, delay: 200 }}
    >
      <!-- Background Glow for Mockup -->
      <div
        class="absolute -inset-4 bg-amber-500/10 blur-3xl opacity-50 group-hover:opacity-70 transition-opacity duration-700 rounded-full"
      ></div>

      <!-- Massive Glass Stage Container -->
      <div
        class="relative bg-zinc-950/40 backdrop-blur-3xl border border-white/10 rounded-[2.5rem] overflow-hidden shadow-2xl p-8 md:p-12"
      >
        <div
          class="grid grid-cols-1 md:grid-cols-2 gap-12 lg:gap-24 items-center"
        >
          <!-- Left: PDF Document View -->
          <div class="flex flex-col relative">
            <div
              class="bg-white rounded-xl shadow-[0_30px_60px_-15px_rgba(0,0,0,0.5)] border border-white/5 overflow-hidden flex flex-col aspect-210/297 w-full max-w-[500px] mx-auto transition-transform duration-1000 group-hover:scale-[1.02]"
            >
              <!-- PDF Header/Toolbar UI -->
              <div
                class="bg-gray-100 px-4 py-2 border-b border-gray-200 flex items-center justify-between shrink-0"
              >
                <div class="flex items-center gap-1.5">
                  <div class="w-2.5 h-2.5 rounded-full bg-red-400"></div>
                  <div class="w-2.5 h-2.5 rounded-full bg-amber-400"></div>
                  <div class="w-2.5 h-2.5 rounded-full bg-emerald-400"></div>
                </div>
                <div
                  class="text-[9px] text-gray-400 font-mono tracking-tighter"
                >
                  comnet-03.pdf
                </div>
              </div>

              <!-- PDF Page Content -->
              <div
                class="p-10 flex-1 bg-[#FDFDFD] overflow-y-auto selection:bg-amber-100 scrollbar-hide"
              >
                <h4
                  class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-8 border-b border-gray-100 pb-2"
                >
                  3.2.1 The Domain Name System (DNS)
                </h4>
                <div
                  class="space-y-6 text-xs md:text-sm text-gray-700 leading-relaxed font-serif"
                >
                  <p>
                    Internet hosts and routers communicate using IP addresses
                    (32-bit). However, humans prefer using hostnames (e.g.,
                    www.google.com).
                  </p>

                  <p class="relative">
                    <span
                      class="relative z-10 font-bold transition-all duration-1000 box-decoration-clone px-1 {activeHighlight
                        ? 'bg-[#FFC107]/40 border-b-2 border-[#FFC107] text-black'
                        : ''}"
                    >
                      The DNS is a distributed database implemented in a
                      hierarchy of many name servers.
                    </span>
                    It serves as an application-layer protocol that allows hosts
                    to query the database and resolve hostnames to IP addresses.
                  </p>

                  <p class="opacity-40">
                    DNS is critically important for the internet application
                    environment because it simplifies complex IP addresses into
                    something easily remembered by humans...
                  </p>
                </div>

                <!-- Footnote/Page info -->
                <div class="mt-20 pt-4 border-t border-gray-50/50">
                  <div
                    class="text-[9px] text-gray-300 font-mono text-center italic"
                  >
                    PAGE 42 OF 312
                  </div>
                </div>
              </div>
            </div>

            <!-- Connection Line (Desktop Only) -->
            {#if activeHighlight}
              <div
                class="hidden md:block absolute top-[45%] -right-12 lg:-right-24 w-12 lg:w-24 h-[2px] bg-linear-to-r from-[#FFC107]/60 to-[#FFC107] z-30 animate-in fade-in slide-in-from-left-4 duration-1000 origin-left"
              >
                <div
                  class="absolute -right-1 -top-1 w-2 h-2 rounded-full bg-[#FFC107] shadow-[0_0_10px_#FFC107]"
                ></div>
              </div>
            {/if}
          </div>

          <!-- Right: Flashcard Implementation -->
          <div class="flex flex-col gap-8 items-center md:items-start">
            <!-- Generation Status -->
            <div
              class="flex items-center gap-3 py-2 px-4 rounded-full bg-emerald-500/10 border border-emerald-500/20 transition-all duration-1000"
              class:opacity-100={activeHighlight}
              class:opacity-0={!activeHighlight}
            >
              <div
                class="w-2 h-2 rounded-full bg-emerald-500 shadow-[0_0_8px_#10b981] animate-pulse"
              ></div>
              <span
                class="text-[10px] font-black text-emerald-400 uppercase tracking-widest"
                >Grounding Verified</span
              >
            </div>

            <!-- The Flashcard -->
            <div
              class="w-full max-w-md bg-zinc-900 shadow-2xl border border-white/5 rounded-3xl p-8 relative overflow-hidden group/card hover:border-[#FFC107]/30 transition-all duration-700 {activeHighlight
                ? 'opacity-100 translate-x-0'
                : 'opacity-0 translate-x-12'}"
            >
              <div
                class="absolute top-0 left-0 w-2 h-full bg-[#FFC107]/20 group-hover/card:bg-[#FFC107]/40 transition-colors"
              ></div>

              <div class="flex items-center justify-between mb-8">
                <div
                  class="bg-amber-500/10 border border-amber-500/20 text-amber-500 text-[10px] font-black px-3 py-1 rounded-lg"
                >
                  P.42
                </div>
                <Sparkles
                  class="w-5 h-5 text-[#FFC107]/40 group-hover/card:text-[#FFC107] transition-colors"
                />
              </div>

              <div class="space-y-6">
                <div>
                  <div
                    class="text-[10px] text-zinc-500 font-black uppercase tracking-widest mb-3"
                  >
                    Question
                  </div>
                  <p
                    class="text-lg md:text-xl text-white font-bold leading-tight"
                  >
                    What is the definition of <span class="text-[#FFC107]"
                      >DNS</span
                    >?
                  </p>
                </div>

                <div class="h-px bg-white/5 w-full"></div>

                <div>
                  <div
                    class="text-[10px] text-zinc-500 font-black uppercase tracking-widest mb-3"
                  >
                    Answer
                  </div>
                  <p
                    class="text-sm md:text-base text-white/80 leading-relaxed font-semibold"
                  >
                    A <span class="text-[#FFC107]">distributed database</span>
                    implemented in a hierarchy of name servers that
                    <span class="text-[#FFC107]">resolves hostnames</span> to IP
                    addresses.
                  </p>
                </div>
              </div>
            </div>

            <!-- CTA/Action -->
            <div
              class="pt-4 transition-all duration-1000 delay-500"
              class:opacity-100={activeHighlight}
              class:opacity-0={!activeHighlight}
            >
              <button
                class="bg-[#FFC107] hover:bg-[#FDB931] text-black px-8 py-4 rounded-2xl font-black shadow-xl shadow-amber-500/20 transition-all transform hover:-translate-y-1 flex items-center gap-3"
              >
                <Brain class="w-5 h-5" />
                Save Context to Library
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Floating Achievement Badge -->
      <div
        class="absolute -bottom-12 left-1/2 -translate-x-1/2 bg-zinc-950/80 backdrop-blur-2xl border border-white/10 p-6 rounded-4xl shadow-2xl flex items-center gap-6 z-20 group-hover:border-[#FFC107]/30 transition-all duration-500"
      >
        <div
          class="w-16 h-16 rounded-full bg-amber-400/10 flex items-center justify-center text-[#FFC107] border border-amber-500/20"
        >
          <CheckCircle2 class="w-9 h-9" />
        </div>
        <div>
          <div
            class="text-sm font-black text-white uppercase tracking-widest mb-1"
          >
            Zero Hallucination
          </div>
          <div class="text-xs text-white/40 font-mono tracking-tight">
            Direct Source Context Extractor v2.4
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<style>
  /* Optional: Custom scroll-aware or timing-aware styles could go here */
</style>
