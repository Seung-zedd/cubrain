<script lang="ts">
  import { fade, fly } from "svelte/transition";
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
    <div class="space-y-8">
      <div
        class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#FFC107]/10 border border-[#FFC107]/20 text-[#FFC107] text-sm font-bold tracking-wide uppercase"
      >
        <Sparkles class="w-4 h-4" />
        Trust & Accuracy
      </div>

      <h2
        class="text-4xl md:text-5xl lg:text-6xl font-bold text-white leading-tight"
      >
        Context-Aware <br /> <span class="text-[#FFC107]">Accuracy</span>
      </h2>

      <p class="text-lg md:text-xl text-white/60 leading-relaxed max-w-xl">
        We don't just guess. Cubrain links every flashcard back to the exact
        paragraph in your PDF.
      </p>

      <ul class="space-y-4">
        {#each ["Precision Context Extraction", "Zero Hallucination Verified", "Direct Source Linking"] as feature}
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
          class="inline-flex items-center gap-2 px-8 py-4 rounded-xl font-bold text-lg bg-white/5 text-white border border-white/10 hover:bg-white/10 hover:border-white/20 backdrop-blur-sm transition-all transform hover:-translate-y-1"
        >
          View Live Demo
          <ArrowRight class="w-5 h-5" />
        </a>
      </div>
    </div>

    <!-- Right: MacBook Pro Mockup -->
    <div class="relative" in:fly={{ y: 20, duration: 800, delay: 200 }}>
      <!-- Ambient Glow -->
      <div
        class="absolute -inset-20 bg-[#FFC107]/5 blur-[120px] -z-10 rounded-full"
      ></div>

      <!-- MacBook Pro Frame -->
      <div class="relative mx-auto max-w-[600px] perspective-2000">
        <!-- The Laptop Chassis -->
        <div
          class="relative bg-zinc-800 rounded-4xl p-4 shadow-2xl border border-white/10 ring-1 ring-white/5"
        >
          <!-- The Screen -->
          <div
            class="relative bg-black rounded-xl overflow-hidden aspect-16/10 border border-white/5"
          >
            <!-- App UI Inside Screen -->
            <div
              class="absolute inset-0 flex flex-col pt-4 px-4 pb-0 bg-zinc-950"
            >
              <!-- Header inside mockup -->
              <div
                class="flex items-center justify-between mb-4 border-b border-white/5 pb-2"
              >
                <div class="flex gap-1.5">
                  <div class="w-2 h-2 rounded-full bg-white/10"></div>
                  <div class="w-2 h-2 rounded-full bg-white/10"></div>
                  <div class="w-2 h-2 rounded-full bg-white/10"></div>
                </div>
                <div
                  class="text-[8px] text-white/20 font-mono tracking-widest uppercase"
                >
                  Cubrain Reader v1.4
                </div>
              </div>

              <!-- Split View Content -->
              <div class="flex-1 grid grid-cols-2 gap-4 pb-4">
                <div
                  class="bg-zinc-900/50 rounded-lg p-3 border border-white/5 overflow-hidden flex flex-col"
                >
                  <div
                    class="text-[9px] text-zinc-500 font-bold mb-2 uppercase tracking-tighter"
                  >
                    PDF View
                  </div>
                  <div class="space-y-2 font-serif">
                    <div class="h-1 w-full bg-white/5 rounded-full"></div>
                    <div class="h-1 w-5/6 bg-white/5 rounded-full"></div>
                    <div class="relative h-4 w-full">
                      <div
                        class="absolute top-1.5 h-1 w-full bg-white/5 rounded-full"
                      ></div>
                      <div
                        class="absolute top-0 left-0 h-4 bg-amber-400/30 rounded transition-all duration-1000 delay-500"
                        style="width: {activeHighlight ? '66.6%' : '0%'}"
                      ></div>
                      <div
                        class="absolute top-1.5 left-0 h-1 w-2/3 bg-white/20 rounded-full"
                        class:opacity-0={activeHighlight}
                      ></div>
                      <div
                        class="absolute top-1.5 left-0 h-1 w-2/3 bg-white/60 rounded-full opacity-0 transition-opacity duration-1000 delay-500"
                        class:opacity-100={activeHighlight}
                      ></div>
                    </div>
                    <div class="h-1 w-full bg-white/5 rounded-full"></div>
                    <div class="h-1 w-4/5 bg-white/5 rounded-full"></div>
                    <div class="h-1 w-3/4 bg-white/5 rounded-full"></div>
                  </div>
                </div>

                <!-- Flashcard Section (Right) -->
                <div class="flex flex-col justify-center gap-4">
                  <div
                    class="bg-zinc-900 border border-white/10 rounded-xl p-4 shadow-xl transform transition-all duration-700"
                    class:translate-y-0={isVisible}
                    class:translate-y-4={!isVisible}
                    class:opacity-100={isVisible}
                    class:opacity-0={!isVisible}
                  >
                    <div class="flex items-center gap-1.5 mb-3">
                      <Brain class="w-3 h-3 text-[#FFC107]" />
                      <span
                        class="text-[8px] font-bold text-[#FFC107] uppercase tracking-widest"
                        >Question</span
                      >
                    </div>
                    <div
                      class="text-[10px] text-white/80 font-medium leading-relaxed mb-3"
                    >
                      What triggers neurotransmitter release?
                    </div>

                    <div
                      class="transition-all duration-700 delay-[1.2s]"
                      class:opacity-100={activeHighlight}
                      class:translate-y-0={activeHighlight}
                      class:opacity-0={!activeHighlight}
                      class:translate-y-1={!activeHighlight}
                    >
                      <hr class="border-white/5 mb-3" />
                      <div class="flex items-center gap-2">
                        <span class="w-1 h-3 bg-[#FFC107] rounded-full"></span>
                        <span class="text-[10px] font-bold text-white"
                          >Action Potential Influx</span
                        >
                      </div>
                    </div>
                  </div>

                  <!-- Connection Badge -->
                  <div
                    class="flex items-center justify-center gap-2 py-1.5 px-3 rounded-full bg-emerald-500/10 border border-emerald-500/20 transition-opacity duration-700 delay-[1.8s]"
                    class:opacity-100={activeHighlight}
                    class:opacity-0={!activeHighlight}
                  >
                    <div
                      class="w-1.5 h-1.5 rounded-full bg-emerald-500 shadow-[0_0_8px_#10b981]"
                    ></div>
                    <span
                      class="text-[8px] font-bold text-emerald-400 uppercase tracking-widest"
                      >Verified 100%</span
                    >
                  </div>
                </div>
              </div>
            </div>

            <!-- Screen Reflection Finish -->
            <div
              class="absolute inset-0 bg-linear-to-tr from-white/5 to-transparent pointer-events-none"
            ></div>
          </div>
        </div>

        <!-- Bottom Base/Lip -->
        <div
          class="relative -mt-4 mx-12 h-2 bg-zinc-700 rounded-b-xl shadow-xl border-x border-b border-white/10"
        ></div>
        <div
          class="relative mx-auto mt-px w-[150px] h-1.5 bg-zinc-900 rounded-b-xl opacity-50 shadow-inner"
        ></div>
      </div>

      <!-- Verified Floating Badge -->
      <div
        class="absolute -bottom-6 -right-6 lg:-right-12 bg-white/5 border border-white/10 backdrop-blur-xl p-4 rounded-2xl shadow-2xl flex items-center gap-4 group"
      >
        <div
          class="w-10 h-10 rounded-full bg-[#FFC107]/10 flex items-center justify-center text-[#FFC107] group-hover:bg-[#FFC107]/20 transition-colors"
        >
          <CheckCircle2 class="w-6 h-6" />
        </div>
        <div>
          <div
            class="text-xs font-black text-white uppercase tracking-widest mb-0.5"
          >
            Zero Hallucination
          </div>
          <div class="text-[10px] text-white/40">Context-Aware Core Engine</div>
        </div>
      </div>
    </div>
  </div>
</section>

<style>
  :global(.perspective-2000) {
    perspective: 2000px;
  }
</style>
