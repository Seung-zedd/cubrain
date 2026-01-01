<script lang="ts">
  import { fade, fly } from "svelte/transition";
  import { API_BASE_URL } from "$lib/config";

  interface Flashcard {
    question: string;
    answer: string;
  }

  interface ContextData {
    selection: string;
    localContext: string;
    globalContext: string;
  }

  let showButton = $state(false);
  let btnX = $state(0);
  let btnY = $state(0);
  let selectedText = $state("");
  let isLoading = $state(false);
  let flashcard = $state<Flashcard | null>(null);
  let currentContext = $state<ContextData | null>(null);
  let demoContainer = $state<HTMLElement>(); // Reference to the container
  let errorMessage = $state("");
  let errorTimeout: ReturnType<typeof setTimeout>;

  const MAX_LOCAL_CONTEXT_LENGTH = 2000;
  const MAX_GLOBAL_CONTEXT_LENGTH = 4000;
  const WORD_BOUNDARY_THRESHOLD = 0.9;

  // ==================== HELPER FUNCTIONS ====================

  function getNearestElement(node: Node): Element | null {
    return node.nodeType === Node.ELEMENT_NODE
      ? (node as Element)
      : node.parentElement;
  }

  function getSemanticContext(element: HTMLElement): HTMLElement {
    const semanticTags = [
      "P",
      "DIV",
      "ARTICLE",
      "SECTION",
      "LI",
      "TD",
      "BLOCKQUOTE",
      "H1",
      "H2",
      "H3",
      "H4",
      "H5",
      "H6",
    ];
    let current: HTMLElement | null = element;
    while (current && !semanticTags.includes(current.tagName)) {
      current = current.parentElement;
    }
    return current || element;
  }

  function truncateText(text: string, maxLength: number): string {
    if (!text || text.length <= maxLength) return text || "";
    const truncated = text.substring(0, maxLength);
    const lastSpaceIndex = truncated.lastIndexOf(" ");
    if (lastSpaceIndex > maxLength * WORD_BOUNDARY_THRESHOLD) {
      return truncated.substring(0, lastSpaceIndex) + "...";
    }
    return truncated + "...";
  }

  function captureContext(range: Range, text: string): ContextData | null {
    const commonAncestor = range.commonAncestorContainer;
    const ancestorElement = getNearestElement(commonAncestor);

    if (!ancestorElement || !(ancestorElement instanceof HTMLElement))
      return null;

    const semanticContainer = getSemanticContext(ancestorElement);
    // Ensure we are still within our demo container
    if (!demoContainer || !demoContainer.contains(semanticContainer)) {
      return null;
    }

    const localContext = (
      semanticContainer.textContent ||
      semanticContainer.innerText ||
      ""
    ).trim();
    const globalContext = (
      demoContainer.textContent ||
      demoContainer.innerText ||
      ""
    ).trim();

    return {
      selection: text,
      localContext: truncateText(localContext, MAX_LOCAL_CONTEXT_LENGTH),
      globalContext: truncateText(globalContext, MAX_GLOBAL_CONTEXT_LENGTH),
    };
  }

  // ==================== EVENT HANDLERS ====================

  function handleMouseUp() {
    const selection = window.getSelection();
    if (!selection || selection.isCollapsed) {
      hideButton();
      return;
    }

    const text = selection.toString().trim();
    if (!text) return;

    // Check if selection is inside our demo container
    if (
      !demoContainer ||
      !selection.anchorNode ||
      !demoContainer.contains(selection.anchorNode)
    ) {
      hideButton();
      return;
    }

    const range = selection.getRangeAt(0);
    const rect = range.getBoundingClientRect();
    const containerRect = demoContainer.getBoundingClientRect();

    currentContext = captureContext(range, text);
    if (!currentContext) return;

    if (import.meta.env.DEV || import.meta.env.VITE_SHOW_LOGS === "true") {
      console.log("🔍 Captured Context Data:", {
        Layer1_Selection: currentContext.selection,
        Layer2_LocalContext: currentContext.localContext,
        Layer3_GlobalContext: currentContext.globalContext,
      });
    }

    selectedText = text;

    // Calculate position relative to the container
    // We use the container's rect to offset the button's position
    btnX = rect.left - containerRect.left + rect.width / 2;
    btnY = Math.max(0, rect.top - containerRect.top - 50); // 50px above, clamped to 0

    showButton = true;
  }

  function handleMouseDown(event: MouseEvent) {
    // Hide if clicking outside the button
    const target = event.target as HTMLElement;
    if (target.closest(".cubrain-float-btn")) return;
    hideButton();
  }

  function hideButton() {
    showButton = false;
  }

  function showError(message: string) {
    errorMessage = message;
    if (errorTimeout) clearTimeout(errorTimeout);
    errorTimeout = setTimeout(() => {
      errorMessage = "";
    }, 5000);
  }

  async function createFlashcard() {
    if (!selectedText) return;

    isLoading = true;
    flashcard = null;

    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 1500));

    try {
      const text = selectedText.toLowerCase();
      let mockFlashcard: Flashcard;

      // Easter Egg check
      if (
        text.includes("simplified interactive demo") &&
        text.includes("higher quality")
      ) {
        mockFlashcard = {
          question: "Why did you generate this text except for Demo phrase?",
          answer:
            "Because I'm a very curious learner, so I wanna check this out. <br> I knew you would drag this text. Don't you curious about real AI generation? Just click below button!👇",
        };
      } else if (text.includes("simulation") || text.includes("intelligence")) {
        mockFlashcard = {
          question:
            "What is the core definition of Artificial Intelligence (AI)?",
          answer:
            "AI is the simulation of human intelligence processes by machines, especially computer systems, including learning, reasoning, and self-correction.",
        };
      } else if (text.includes("learning") || text.includes("reasoning")) {
        mockFlashcard = {
          question:
            "What three processes are typically included in AI simulation?",
          answer:
            "AI simulation typically includes learning (acquiring information/rules), reasoning (using rules for conclusions), and self-correction.",
        };
      } else if (
        text.includes("expert systems") ||
        text.includes("speech recognition")
      ) {
        mockFlashcard = {
          question:
            "What are some practical applications of Artificial Intelligence?",
          answer:
            "Particular applications include expert systems, speech recognition, and machine vision.",
        };
      } else if (text.includes("weak") || text.includes("narrow")) {
        mockFlashcard = {
          question: "What is Weak AI (Narrow AI)?",
          answer:
            "Weak AI is an AI system specifically designed and trained for a particular, limited task.",
        };
      } else if (text.includes("strong") || text.includes("generalized")) {
        mockFlashcard = {
          question: "How is Strong AI defined?",
          answer:
            "Strong AI, or artificial general intelligence, is an AI system that possesses generalized human cognitive abilities.",
        };
      } else {
        // Generic fallback that looks like AI generation
        mockFlashcard = {
          question: `Can you explain the significance of "${selectedText}" in this context?`,
          answer: `In the context of Artificial Intelligence, "${selectedText}" refers to a key concept or component that contributes to the simulation of human-like cognitive functions in machines.`,
        };
      }

      flashcard = mockFlashcard;
      // Clear selection
      const selection = window.getSelection();
      if (selection) selection.removeAllRanges();
      showButton = false;
    } catch (error) {
      showError("An unexpected error occurred during the demo.");
    } finally {
      isLoading = false;
    }
  }
  import { onMount } from "svelte";

  onMount(() => {
    if (import.meta.env.DEV || import.meta.env.VITE_SHOW_LOGS === "true") {
      console.log("🔍 Debug Info:");
      console.log("API_BASE_URL:", API_BASE_URL);
      console.log("import.meta.env:", import.meta.env);
    }
  });
</script>

<div
  class="demo-wrapper relative bg-zinc-950/50 backdrop-blur-sm border border-zinc-800 rounded-2xl p-6 md:p-8 shadow-2xl max-w-3xl mx-auto my-8"
  bind:this={demoContainer}
  onmouseup={handleMouseUp}
  onmousedown={handleMouseDown}
  role="presentation"
>
  <div class="flex items-center gap-4 mb-8">
    <span
      class="inline-flex items-center px-4 py-1.5 rounded-full text-sm font-bold bg-[#FFD700] text-black shadow-[0_0_15px_rgba(255,215,0,0.3)]"
    >
      ✨ Interactive Demo
    </span>
    <h3 class="text-xl md:text-2xl font-bold text-white m-0">
      Try it yourself
    </h3>
  </div>

  <div class="demo-content">
    <p
      class="text-zinc-200 leading-relaxed text-lg selection:bg-[#FFD700]/30 selection:text-white mb-6"
    >
      Artificial Intelligence (AI) is the simulation of human intelligence
      processes by machines, especially computer systems. These processes
      include learning (the acquisition of information and rules for using the
      information), reasoning (using rules to reach approximate or definite
      conclusions), and self-correction.
    </p>
    <p
      class="text-zinc-200 leading-relaxed text-lg selection:bg-[#FFD700]/30 selection:text-white"
    >
      Particular applications of AI include expert systems, speech recognition,
      and machine vision. AI can be categorized as either weak or strong. Weak
      AI, also known as narrow AI, is an AI system that is designed and trained
      for a particular task. Strong AI, also known as artificial general
      intelligence, is an AI system with generalized human cognitive abilities.
    </p>
    <p class="instruction-text">
      <span role="img" aria-label="Pointing up">👆</span>
      <strong>Highlight any text above to generate a flashcard!</strong>
    </p>
    <p class="text-xs text-white/40 text-center mt-4 italic">
      Note: This is a simplified interactive demo. Real AI generation in the
      dashboard provides significantly higher quality and deeper context
      analysis.
    </p>
  </div>

  {#if showButton}
    <button
      class="cubrain-float-btn"
      style="left: {btnX}px; top: {btnY}px;"
      onclick={createFlashcard}
      disabled={isLoading}
      transition:fade={{ duration: 150 }}
    >
      {#if isLoading}
        <span class="spinner"></span> Generating...
      {:else}
        ⚡ Make Flashcard
      {/if}
    </button>
  {/if}

  {#if flashcard}
    <div class="result-card" transition:fly={{ y: 20, duration: 300 }}>
      <div class="card-header">
        <span class="icon">✨</span> Generated Flashcard
        <button class="close-btn" onclick={() => (flashcard = null)}>×</button>
      </div>
      <div class="p-6 flex flex-col gap-6 text-left">
        <div class="flex flex-col gap-2">
          <span
            class="text-amber-500 text-xs font-bold uppercase tracking-wider"
          >
            Question
          </span>
          <p class="text-white text-lg font-medium leading-relaxed">
            {flashcard.question}
          </p>
        </div>
        <div class="flex flex-col gap-2">
          <span
            class="text-gray-500 text-xs font-bold uppercase tracking-wider"
          >
            Answer
          </span>
          <p class="text-gray-300 text-base leading-relaxed">
            {@html flashcard.answer}
          </p>
        </div>
      </div>
    </div>
  {/if}

  {#if errorMessage}
    <div class="error-toast" transition:fade={{ duration: 200 }}>
      <span class="icon">⚠️</span>
      {errorMessage}
      <button class="close-btn-small" onclick={() => (errorMessage = "")}
        >×</button
      >
    </div>
  {/if}
</div>

<style>
  .instruction-text {
    margin-top: 2rem;
    color: #fbbf24 !important; /* Gold color */
    text-align: center;
    font-size: 0.9rem;
  }

  /* Floating Button */
  .cubrain-float-btn {
    position: absolute;
    transform: translateX(-50%);
    background: #fbbf24; /* Gold */
    color: #000;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 2rem;
    font-weight: bold;
    cursor: pointer;
    box-shadow: 0 4px 15px rgba(251, 191, 36, 0.3);
    z-index: 100;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    transition:
      transform 0.2s,
      background 0.2s;
    white-space: nowrap;
  }

  .cubrain-float-btn:hover {
    transform: translateX(-50%) scale(1.05);
    background: #f59e0b;
  }

  /* Result Card */
  .result-card {
    margin-top: 2rem;
    background: #1a1a1a;
    border: 1px solid #fbbf24;
    border-radius: 0.75rem;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  }

  .card-header {
    background: rgba(251, 191, 36, 0.1);
    padding: 0.75rem 1rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(251, 191, 36, 0.2);
    color: #fbbf24;
    font-weight: bold;
  }

  .close-btn {
    background: none;
    border: none;
    color: #fbbf24;
    font-size: 1.5rem;
    cursor: pointer;
    padding: 0;
    line-height: 1;
  }

  .spinner {
    width: 1rem;
    height: 1rem;
    border: 2px solid #000;
    border-top-color: transparent;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    display: inline-block;
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  .error-toast {
    position: absolute;
    bottom: 1rem;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(220, 38, 38, 0.9);
    color: white;
    padding: 0.75rem 1rem;
    border-radius: 0.5rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    z-index: 200;
    width: 90%;
    max-width: 400px;
    font-size: 0.9rem;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .close-btn-small {
    background: none;
    border: none;
    color: white;
    margin-left: auto;
    cursor: pointer;
    font-size: 1.2rem;
    padding: 0 0.25rem;
    opacity: 0.8;
  }

  .close-btn-small:hover {
    opacity: 1;
  }
</style>
