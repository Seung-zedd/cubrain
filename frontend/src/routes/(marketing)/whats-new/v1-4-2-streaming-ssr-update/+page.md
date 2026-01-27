---
layout: changelog
title: "Streaming SSR & Instant Load"
description: "Revolutionizing the loading experience with Streaming SSR and deep performance tuning."
date: "2026-01-27"
version: "v1.4.2"
type: "Patch"
---

## What's New?

This update introduces **Streaming SSR**, a major architectural improvement that makes Cubrain feel faster than ever. We've also polished our performance metrics to ensure a "Good" rating across all Core Web Vitals.

### 🌊 Streaming SSR (Server-Side Rendering)

We've refactored our data fetching layer to use SvelteKit's streaming capabilities.

- **Instant Skeleton UI**: Pages like the Library, Study, and Settings now load their UI skeleton immediately, even if the backend is experiencing a "cold start."
- **Non-blocking Data Fetching**: Data is now streamed to the browser as it becomes available, eliminating the "white screen" wait time.
- **Enhanced Loading States**: Beautiful skeleton loaders provide immediate visual feedback while your decks and flashcards are being fetched.

### ⚡ Performance & Core Web Vitals

We've achieved a significant boost in our Vercel Speed Insights scores:

- **TTFB (Time To First Byte) Optimization**: Reduced server response times by optimizing script loading and pre-rendering strategies.
- **Zero Layout Shift (CLS)**: Fixed layout shifts caused by the launch banner and hero video. The site now feels rock-solid during hydration.
- **LCP (Largest Contentful Paint) Boost**: Optimized hero assets and removed blocking third-party scripts from the critical rendering path.

### 🛠️ Bug Fixes & Refinements

- **Icon Consistency**: Fixed missing icons in the study session loading states.
- **Code Quality**: Resolved naming conflicts and improved type safety in the frontend codebase.
- **Launch Banner**: Improved the hydration logic for the launch banner to prevent jarring layout jumps.

---

#### **🚀 Under the Hood: The Power of Streaming**

By moving from blocking `await` in our server load functions to returning promises, we've decoupled the initial page render from the backend's data retrieval. This is especially powerful for our Spring Boot backend, as it allows the frontend to stay responsive even during occasional latency spikes. Cubrain now feels "instant" from the very first click.
