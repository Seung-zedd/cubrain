<script lang="ts">
  import { getLocale, setLocale, locales } from "$paraglide/runtime";

  import Languages from "@lucide/svelte/icons/languages";
  import ChevronDown from "@lucide/svelte/icons/chevron-down";
  import { fly, fade } from "svelte/transition";
  import { page } from "$app/state";
  import { goto } from "$app/navigation";
  import { IS_DEV_MODE } from "$lib/utils/env";

  const languages = [
    { code: "en", name: "English", flag: "🇺🇸" },
    { code: "ko", name: "한국어", flag: "🇰🇷" },
    { code: "hi", name: "हिन्दी", flag: "🇮🇳" },
    { code: "es", name: "Español", flag: "🇪🇸" },
    { code: "zh", name: "中文", flag: "🇨🇳" },
    { code: "fr", name: "Français", flag: "🇫🇷" },
    { code: "ja", name: "日本語", flag: "🇯🇵" },
    { code: "tr", name: "Türkçe", flag: "🇹🇷" },
  ];

  let isOpen = $state(false);

  // Use a reactive derivation to track the current language from runtime
  // We include page.url.pathname to ensure the derivation re-runs on navigation
  const currentLangCode = $derived(
    page.url.pathname ? getLocale() : getLocale(),
  );

  $effect(() => {
    // Following AGENTS.md Rule #12: Wrapped in IS_DEV_MODE check
    if (IS_DEV_MODE) {
      console.log("🌐 Language changed to:", currentLangCode);
    }
  });

  const currentLang = $derived(
    languages.find((l) => l.code === currentLangCode) || languages[0],
  );

  function toggle() {
    isOpen = !isOpen;
  }

  function selectLanguage(code: any) {
    if (code === currentLangCode) {
      isOpen = false;
      return;
    }

    // setLocale will handle the strategy (setting cookie and redirecting to localized URL)
    setLocale(code);
    isOpen = false;
  }
</script>

<div class="relative inline-block text-left">
  <button
    type="button"
    class="flex items-center gap-1.5 px-2 py-1.5 text-sm font-medium text-white/60 hover:text-white transition-all duration-200 focus:outline-none"
    onclick={toggle}
    aria-haspopup="listbox"
    aria-expanded={isOpen}
  >
    <Languages class="w-4 h-4 opacity-70" />
    <span class="uppercase tracking-wider text-xs">{currentLangCode}</span>
    <ChevronDown
      class="w-3 h-3 transition-transform duration-200 opacity-40 {isOpen
        ? 'rotate-180'
        : ''}"
    />
  </button>

  {#if isOpen}
    <!-- Backdrop for closing -->
    <div
      class="fixed inset-0 z-60"
      onclick={() => (isOpen = false)}
      onkeydown={(e) => e.key === "Escape" && (isOpen = false)}
      role="button"
      tabindex="0"
      aria-label="Close language selector"
    ></div>

    <div
      class="absolute right-0 mt-3 w-48 origin-top-right bg-[#0D0D0D] border border-white/10 rounded-2xl shadow-2xl z-70 overflow-hidden backdrop-blur-xl"
      in:fly={{ y: 5, duration: 200 }}
      out:fade={{ duration: 150 }}
    >
      <div
        class="py-1.5 flex flex-col max-h-[400px] overflow-y-auto custom-scrollbar"
      >
        {#each languages as lang}
          <button
            class="flex items-center justify-between w-full px-4 py-3 text-sm text-left transition-all {currentLangCode ===
            lang.code
              ? 'bg-[#FFD700]/10 text-[#FFD700]'
              : 'text-white/60 hover:bg-white/5 hover:text-white'}"
            onclick={() => selectLanguage(lang.code)}
            role="option"
            aria-selected={currentLangCode === lang.code}
          >
            <div class="flex items-center gap-3">
              <span class="text-base filter grayscale-[0.2]">{lang.flag}</span>
              <span class="font-medium">{lang.name}</span>
            </div>
            {#if currentLangCode === lang.code}
              <div
                class="w-1.5 h-1.5 rounded-full bg-[#FFD700] shadow-[0_0_8px_#FFD700]"
              ></div>
            {/if}
          </button>
        {/each}
      </div>
    </div>
  {/if}
</div>

<style>
  .custom-scrollbar::-webkit-scrollbar {
    width: 4px;
  }
  .custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
  }
  .custom-scrollbar::-webkit-scrollbar-thumb {
    background: #333;
    border-radius: 10px;
  }
</style>
