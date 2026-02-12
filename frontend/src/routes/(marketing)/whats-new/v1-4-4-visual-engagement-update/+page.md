---
layout: changelog
title: "Visual Engagement Update"
description: "Introducing a Two-Track Video Strategy to the Hero section for better performance and cinematic storytelling."
date: "2026-02-12"
version: "v1.4.4"
type: "Patch"
---

## What's New?

We've overhauled the first impression of Cubrain! This update introduces a **Two-Track Video Strategy** to the landing page, balancing high-speed performance with an immersive audio-visual experience.

### 🎥 Two-Track Video Strategy

We now serve different video experiences tailored to how you interact with the page:

- **Optimized Background**: The Hero section now features `hero-bg.mp4`, a lightweight, muted background video that loads instantly (LCP optimized) to show the app in action without slowing you down.
- **Cinematic Demo Modal**: Click the new **"Watch Demo"** button to open a high-quality video modal. This plays `demo-full.mp4` with full audio and voiceover for those who want the complete deep dive.
- **Accessible Captions**: We've added a custom English caption track to the demo video, ensuring everyone can follow the value of Cubrain regardless of their environment.

### 💄 UI & Accessibility Polish

- **Glassmorphism Design**: The "Watch Demo" button features a sleek, translucent design that fits perfectly within our Cubrain Gold aesthetic.
- **A11y Enhancements**: Added `aria-label`, `crossorigin` support, and proper `track` elements to ensure the landing page is accessible to all users.
- **Improved Performance**: Leveraging `preload="auto"` for the background video to ensure a smooth transition from the 3D placeholder image.

---

#### **🚀 Why This Matters?**

First impressions count, but so does performance. By splitting our video content, we ensure that new visitors see a dynamic, "living" interface immediately while still providing access to high-fidelity storytelling for those who want it.
