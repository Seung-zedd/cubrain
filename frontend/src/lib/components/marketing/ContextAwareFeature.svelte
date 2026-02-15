<script lang="ts">
  import { fade, fly, scale } from "svelte/transition";
  import Brain from "@lucide/svelte/icons/brain";
  import CheckCircle2 from "@lucide/svelte/icons/check-circle-2";
  import Sparkles from "@lucide/svelte/icons/sparkles";
  import ArrowRight from "@lucide/svelte/icons/arrow-right";
  import { onMount } from "svelte";

  let isVisible = $state(false);
  let activeHighlight = $state(false);

  onMount(() => {
    isVisible = true;
    setTimeout(() => {
      activeHighlight = true;
    }, 1000);
  });
</script>

<section
  id="context-aware"
  class="py-24 px-6 max-w-7xl mx-auto overflow-hidden"
>
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
    <!-- Left: Content -->
    <div class="space-y-8" in:fly={{ x: -20, duration: 800 }}>
      <div
        class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#FFC107]/10 border border-[#FFC107]/20 text-[#FFC107] text-sm font-bold tracking-wide uppercase"
      >
        <Sparkles class="w-4 h-4" />
        Advanced AI Context
      </div>

      <h2 class="text-4xl md:text-6xl font-bold text-white leading-[1.1]">
        Stop the <span
          class="bg-linear-to-r from-red-400 to-red-600 bg-clip-text text-transparent italic"
          >Soul-Crushing</span
        > <br class="hidden md:block" /> Manual Bottleneck.
      </h2>

      <p class="text-lg md:text-xl text-white/60 leading-relaxed max-w-xl">
        Cubrain isn't just another AI. It understands deep context, ensuring
        every flashcard is a perfect reflection of your source material.
      </p>

      <ul class="space-y-4">
        {#each ["Precision Context Extraction", "Zero Hallucination Proof", "Technical Terminology Mastery"] as feature}
          <li class="flex items-center gap-3 text-white/80">
            <div
              class="w-6 h-6 rounded-full bg-[#FFC107]/20 flex items-center justify-center text-[#FFC107]"
            >
              <CheckCircle2 class="w-4 h-4" />
            </div>
            <span class="font-medium">{feature}</span>
          </li>
        {/each}
      </ul>

      <div class="pt-4">
        <a
          href="/upload"
          class="inline-flex items-center gap-2 px-8 py-4 rounded-xl font-bold text-lg bg-linear-to-r from-[#FFD700] to-[#FDB931] text-black shadow-[0_0_20px_rgba(255,215,0,0.2)] hover:shadow-[0_0_30px_rgba(255,215,0,0.4)] transition-all transform hover:-translate-y-1"
        >
          Try it Now
          <ArrowRight class="w-5 h-5" />
        </a>
      </div>
    </div>

    <!-- Right: Clay Mockup Proof -->
    <div
      class="relative perspective-2000"
      in:fly={{ x: 20, duration: 800, delay: 200 }}
    >
      <!-- Ambient Glow -->
      <div
        class="absolute -inset-20 bg-[#FFC107]/10 blur-[100px] -z-10 rounded-full"
      ></div>

      <!-- Clay Mockup Container -->
      <div
        class="relative bg-zinc-900/50 backdrop-blur-xl border border-white/10 rounded-3xl p-6 md:p-8 shadow-2xl overflow-hidden group"
      >
        <!-- PDF/Source Side -->
        <div
          class="space-y-4 opacity-80 group-hover:opacity-100 transition-opacity"
        >
          <div class="flex items-center justify-between mb-2">
            <div class="flex gap-1.5">
              <div class="w-2.5 h-2.5 rounded-full bg-red-500/50"></div>
              <div class="w-2.5 h-2.5 rounded-full bg-amber-500/50"></div>
              <div class="w-2.5 h-2.5 rounded-full bg-emerald-500/50"></div>
            </div>
            <div
              class="text-[10px] text-white/30 font-mono tracking-widest uppercase"
            >
              Source: Neuroscience_Vol2.pdf
            </div>
          </div>

          <h4
            class="text-sm font-bold text-white/40 uppercase tracking-tighter"
          >
            Chapter 4: Synaptic Dynamics
          </h4>

          <div class="text-[13px] leading-relaxed text-white/50 font-serif">
            <p>
              The arrival of an action potential at the presynaptic terminal
              triggers the opening of voltage-gated calcium channels. The
              resulting influx of Ca2+ ions facilitates the docking of synaptic
              vesicles to the active zone via the
              <span
                class={activeHighlight
                  ? "bg-[#FFC107]/40 text-[#FFC107] px-1 rounded transition-all duration-1000 delay-500"
                  : ""}
              >
                SNARE complex
              </span>
              mediating neurotransmitter release into the synaptic cleft. This process
              is highly regulated by various proteins including synaptotagmin and
              complexin.
            </p>
          </div>
        </div>

        <!-- Flashcard Output Overlay (The "Result") -->
        <div
          class="absolute bottom-6 right-6 md:bottom-12 md:right-12 w-[280px] md:w-[320px] transform transition-all duration-700"
          class:translate-y-0={isVisible}
          class:translate-y-8={!isVisible}
          class:opacity-100={isVisible}
          class:opacity-0={!isVisible}
        >
          <div
            class="bg-zinc-800/90 backdrop-blur-md border border-white/20 rounded-2xl p-6 shadow-2xl ring-1 ring-white/10 overflow-hidden relative group/card"
          >
            <!-- Glow effect on card -->
            <div
              class="absolute inset-0 bg-linear-to-br from-[#FFC107]/5 to-transparent pointer-events-none"
            ></div>

            <div class="relative z-10">
              <div class="flex items-center gap-2 mb-4">
                <div
                  class="w-8 h-8 rounded-lg bg-[#FFC107]/10 flex items-center justify-center text-[#FFC107]"
                >
                  <Brain class="w-4 h-4" />
                </div>
                <div
                  class="text-[10px] font-bold text-[#FFC107] tracking-widest uppercase"
                >
                  Verified Card
                </div>
              </div>

              <div class="space-y-4">
                <div>
                  <div
                    class="text-[10px] text-white/30 uppercase font-black mb-1"
                  >
                    Question
                  </div>
                  <div class="text-sm font-medium text-white leading-snug">
                    Which protein complex facilitates the docking of synaptic
                    vesicles to the active zone?
                  </div>
                </div>

                <div
                  class="transition-all duration-700 delay-[1.5s]"
                  class:opacity-100={activeHighlight}
                  class:translate-y-0={activeHighlight}
                  class:opacity-0={!activeHighlight}
                  class:translate-y-2={!activeHighlight}
                >
                  <div
                    class="text-[10px] text-[#FFC107] uppercase font-black mb-1"
                  >
                    Answer
                  </div>
                  <div
                    class="text-sm font-bold text-white flex items-center gap-2"
                  >
                    <span class="w-1.5 h-4 bg-[#FFC107] rounded-full"></span>
                    SNARE complex
                  </div>
                </div>
              </div>
            </div>

            <!-- Connection Line (Visual Cue) -->
            <div
              class="absolute -top-10 -left-10 w-40 h-40 bg-[#FFC107]/5 blur-3xl -z-10 group-hover/card:bg-[#FFC107]/10 transition-colors"
            ></div>
          </div>
        </div>
      </div>

      <!-- Supporting Badges -->
      <div
        class="absolute -top-4 -right-4 bg-emerald-500/10 border border-emerald-500/20 backdrop-blur-md px-4 py-2 rounded-xl flex items-center gap-2 shadow-xl animate-bounce"
        style="animation-duration: 3s;"
      >
        <div
          class="w-2 h-2 rounded-full bg-emerald-500 shadow-[0_0_8px_#10b981]"
        ></div>
        <span
          class="text-xs font-bold text-emerald-400 uppercase tracking-tighter"
          >Zero Hallucination</span
        >
      </div>
    </div>
  </div>
</section>

<style>
  :global(.perspective-2000) {
    perspective: 2000px;
  }

  :global(.rotate-y-2) {
    transform: rotateY(2deg);
  }

  :global(.rotate-x-2) {
    transform: rotateX(2deg);
  }
</style>
