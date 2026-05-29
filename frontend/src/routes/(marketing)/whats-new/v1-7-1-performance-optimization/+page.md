---
layout: changelog
title: "Performance & LCP Optimization: Asset Cleanup & Media Preload"
description: "Optimized Cubrain's initial page load speed by removing heavy external teaser resources, purging unused 4MB assets, and tuning background video loading."
date: "2026-05-30"
version: "v1.7.1"
type: "Performance Update"
---

## ⚡ Largest Contentful Paint (LCP) & Performance Tuning

We have implemented key performance optimizations to significantly improve Cubrain's Largest Contentful Paint (LCP) score and speed up rendering times for users.

### 🛡️ What actions were taken?

- **Removed Lucidify Teaser**: Retired the "what's new" teaser card referencing Lucidify, which loaded a heavy 4MB dynamic asset under initial render conditions.
- **Purged Unused Assets**: Permanently deleted [purple-dream.gif](file:///C:/Users/sdok1/projects/cubrain/frontend/static/images/purple-dream.gif) from our static assets, reducing overall bundle size and cleaning up network transfers.
- **Tuned Hero Video Loading**: Changed the main hero background video tag to use `preload="metadata"` instead of `preload="auto"`. This prevents browsers from immediately downloading the video stream during initial HTML parsing.
- **Introduced WebM Fallback Structure**: Restructured background media rendering into a two-tier `<source>` setup, prioritizing high-efficiency `webm` (VP9) format with a legacy fallback to `mp4`.
- **Verified Modal Behavior**: Checked that the large 32MB modal demo video (`demo-full.mp4`) remains properly nested within Svelte's conditional rendering (`{#if showDemoModal}`), completely avoiding background loading until the user explicitly triggers it.
