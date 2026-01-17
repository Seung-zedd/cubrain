<script lang="ts">
  import { onMount } from "svelte";
  import Volume2 from "@lucide/svelte/icons/volume-2";
  import VolumeX from "@lucide/svelte/icons/volume-x";
  import { cn } from "$lib/utils";

  let videoElement = $state<HTMLVideoElement | null>(null);
  let isMuted = $state(true);
  let isHovered = $state(false);

  const toggleMute = () => {
    if (videoElement) {
      videoElement.muted = !videoElement.muted;
      isMuted = videoElement.muted;
    }
  };

  onMount(() => {
    if (videoElement) {
      videoElement.muted = true;
      videoElement.play().catch((err) => {
        console.warn("Autoplay was prevented:", err);
      });
    }
  });
</script>

<div
  class="relative max-w-5xl mx-auto px-6 mb-20 group perspective-1000"
  onmouseenter={() => (isHovered = true)}
  onmouseleave={() => (isHovered = false)}
  role="region"
  aria-label="Hero Video"
>
  <!-- Floating Glass Card Container -->
  <div
    class={cn(
      "relative rounded-2xl overflow-hidden border border-white/10 bg-black/40 backdrop-blur-sm transition-all duration-700 ease-out shadow-2xl",
      "group-hover:rotate-y-1 group-hover:rotate-x-1 group-hover:scale-[1.01]",
      "border-[#FFD700]/30 shadow-[0_0_50px_rgba(255,215,0,0.1)]",
    )}
  >
    <!-- Video Element -->
    <video
      bind:this={videoElement}
      src="/videos/hero-video.mp4"
      poster="/og-image.png"
      autoplay
      muted
      loop
      playsinline
      class="w-full h-auto block"
    >
      <track kind="captions" />
    </video>

    <!-- Sound Toggle Overlay -->
    <button
      onclick={toggleMute}
      class={cn(
        "absolute bottom-6 right-6 flex items-center gap-2 px-4 py-2 rounded-full backdrop-blur-md border transition-all duration-300 z-20",
        isMuted
          ? "bg-[#FFD700]/20 border-[#FFD700]/50 text-[#FFD700] hover:bg-[#FFD700]/30 animate-pulse"
          : "bg-white/10 border-white/20 text-white hover:bg-white/20",
      )}
    >
      {#if isMuted}
        <VolumeX class="w-4 h-4" />
        <span class="text-xs font-bold tracking-wider uppercase"
          >Unmute for the Magic</span
        >
      {:else}
        <Volume2 class="w-4 h-4" />
        <span class="text-xs font-bold tracking-wider uppercase">Mute</span>
      {/if}
    </button>

    <!-- Glass Reflections -->
    <div
      class="absolute inset-0 pointer-events-none bg-linear-to-tr from-white/5 to-transparent opacity-30"
    ></div>
  </div>

  <!-- Ambient Glow -->
  <div
    class={cn(
      "absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[120%] h-[120%] bg-[#FFD700]/10 blur-[120px] -z-10 transition-opacity duration-700",
      isHovered ? "opacity-100" : "opacity-60",
    )}
  ></div>
</div>

<style>
  .perspective-1000 {
    perspective: 1000px;
  }

  .rotate-y-1 {
    transform: rotateY(1deg);
  }

  .rotate-x-1 {
    transform: rotateX(1deg);
  }
</style>
