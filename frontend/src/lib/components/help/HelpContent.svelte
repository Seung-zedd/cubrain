<script lang="ts">
  import { slide } from "svelte/transition";
  import ChevronDown from "@lucide/svelte/icons/chevron-down";
  import ArrowLeft from "@lucide/svelte/icons/arrow-left";

  let { showBackButton = false } = $props();
  let openIndex = $state<number | null>(null);
  let copied = $state(false);

  const faqs = [
    {
      question:
        "Why aren't flashcards generated when I highlight using an iPad or Galaxy Tab?",
      answer:
        'Currently, Cubrain (V1) is highly optimized for extracting <b>standard digital text highlight metadata</b>, providing the fastest and most accurate conversion. Freehand highlights drawn directly with an Apple Pencil or S-Pen are typically rendered as images within the PDF, which limits our parser\'s ability to recognize them in the current version. <div class="mt-4 bg-amber-400/10 border border-amber-400/20 p-4 rounded-xl text-amber-400 leading-relaxed"><strong class="font-bold">💡 Tip:</strong> Use the standard \'Text Highlight\' tool provided by your PDF viewer for perfect extraction! Full handwriting and freehand stroke recognition will be officially supported in our upcoming <strong class="font-bold">V3 update</strong>, which will introduce Multi-Modal AI Vision technology. Stay tuned!</div>',
    },
    {
      question: "What languages do you support for PDF documents?",
      answer:
        "We offer <b>full multilingual</b> support! Cubrain automatically adapts to the language of your uploaded PDF. It intelligently identifies core keywords within the document and seamlessly converts them into natural active recall questions and answer sets in that exact same language.",
    },
    {
      question: "Are there any limits on file size or the number of pages?",
      answer:
        "To ensure a fast and seamless quiz generation experience, we currently recommend uploading PDF documents that are under <b>20 MB</b> and <b>50 pages</b>. Once generated, your flashcard sets are permanently stored in your workspace, allowing you to review and study them anytime.",
    },
  ];

  function toggle(index: number) {
    if (openIndex === index) {
      openIndex = null;
    } else {
      openIndex = index;
    }
  }
</script>

<div class="max-w-3xl mx-auto relative">
  {#if showBackButton}
    <div class="absolute -top-12 left-0 md:-left-20">
      <a
        href="/"
        class="group flex items-center gap-2 text-zinc-500 hover:text-white transition-colors"
      >
        <div
          class="p-2 rounded-full bg-zinc-900 border border-zinc-800 group-hover:border-zinc-700 transition-all"
        >
          <ArrowLeft class="w-5 h-5" />
        </div>
        <span class="text-sm font-medium">Back to Home</span>
      </a>
    </div>
  {/if}

  <!-- Header -->
  <div class="text-center mb-16">
    <h1 class="text-4xl md:text-5xl font-bold text-white mb-4 tracking-tight">
      Cubrain <span class="text-amber-400">Help Center</span>
    </h1>
    <p class="text-white/60 text-lg">
      Everything you need to know about using Cubrain effectively.
    </p>
  </div>

  <!-- Accordion UI -->
  <div class="space-y-4">
    {#each faqs as faq, i (faq.question)}
      <div
        class="rounded-2xl bg-white/5 backdrop-blur-md border outline-none transition-all duration-300 {openIndex ===
        i
          ? 'border-amber-400 ring-1 ring-amber-400/50 bg-white/10'
          : 'border-zinc-800 hover:border-zinc-700'}"
      >
        <button
          class="w-full px-6 py-6 flex items-center justify-between text-left focus:outline-none"
          onclick={() => toggle(i)}
          aria-expanded={openIndex === i}
        >
          <span
            class="text-lg font-medium text-white transition-colors {openIndex ===
            i
              ? 'text-amber-400'
              : ''}"
          >
            {faq.question}
          </span>
          <ChevronDown
            class="w-5 h-5 text-zinc-500 transition-transform duration-300 {openIndex ===
            i
              ? 'rotate-180 text-amber-400'
              : ''}"
          />
        </button>

        {#if openIndex === i}
          <div
            transition:slide={{ duration: 300 }}
            class="px-6 pb-6 text-zinc-400 leading-relaxed text-base"
          >
            <!-- eslint-disable-next-line svelte/no-at-html-tags -->
            {@html faq.answer}
          </div>
        {/if}
      </div>
    {/each}
  </div>

  <!-- Still need help? Section -->
  <div
    class="mt-16 pt-8 border-t border-zinc-900 text-center flex flex-col items-center"
  >
    <p class="text-zinc-500 mb-6 text-lg">Still need help?</p>

    <div
      class="flex flex-col sm:flex-row items-center justify-center gap-4 w-full sm:w-auto"
    >
      <a
        href="mailto:support@cubrain.app"
        class="inline-flex items-center justify-center gap-2 px-8 py-3.5 rounded-xl font-medium bg-zinc-900/50 border border-zinc-800 text-white hover:bg-amber-400/10 hover:text-amber-400 hover:border-amber-400/30 transition-all shadow-[0_4px_14px_0_rgba(0,0,0,0.1)] w-full sm:w-auto"
      >
        Open Mail App
      </a>

      <button
        onclick={() => {
          navigator.clipboard.writeText("support@cubrain.app");
          copied = true;
          setTimeout(() => (copied = false), 2000);
        }}
        class="inline-flex items-center justify-center gap-2 px-8 py-3.5 rounded-xl font-medium bg-zinc-900/50 border border-zinc-800 {copied
          ? 'text-amber-400 border-amber-400/30 bg-amber-400/10'
          : 'text-white hover:bg-amber-400/10 hover:text-amber-400 hover:border-amber-400/30'} transition-all shadow-[0_4px_14px_0_rgba(0,0,0,0.1)] w-full sm:w-auto"
      >
        {copied ? "Copied!" : "Copy Email"}
      </button>
    </div>

    <p class="text-zinc-600 mt-6 text-sm">
      Or reach us explicitly at <span class="text-zinc-400 font-medium"
        >support@cubrain.app</span
      >
    </p>
  </div>
</div>
