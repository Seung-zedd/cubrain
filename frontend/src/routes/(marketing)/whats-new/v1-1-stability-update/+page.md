---
layout: changelog
title: "Stability & Performance Update"
description: "Faster processing, batching, and the new Visual Progress UX."
date: "2026-01-12"
version: "v1.1.0"
type: "Update"
---

## What's New?

We've been working hard to make Cubrain even more reliable and faster. This update focuses on performance optimization and a more engaging processing experience.

### 🚀 Performance & Optimization

- **Batch Processing**: We now group 5-10 highlights into a single AI prompt. This significantly reduces API costs and speeds up the generation process by up to 60%.
- **Async Queue System**: A new background processing system ensures that large PDFs don't time out. You can now track your progress in real-time.
- **Empty File Validation**: Added strict validation for file uploads. No more "ghost" decks from empty or unreadable PDFs!

### 🏃 Visual Progress UX (The Pacer)

We've introduced **"The Pacer"**—a real-time character animation that reflects the processing state.

- **Running**: When AI is actively synthesizing your cards.
- **Success**: When your deck is ready for review.
- _Purpose_: We've turned technical latency into an entertaining visual element to keep you engaged while you wait.

### 🛡️ Stability & Subscription

- **Grace Period Logic**: Pro users now have a 3-day grace period after their subscription expires before losing access to premium features.
- **Supabase Auth Refactor**: Improved session management for a more stable login experience.

### 🐛 Bug Fixes

- Fixed a layout shift on the landing page when the launch banner was active.
- Improved the real-time counter for the early bird discount.

---

#### **🛠️ Under the Hood: Batching & The Pacer**

To handle the massive textbooks used by Med and Law students, we implemented a **Vectorized Batching** strategy. Instead of processing highlights one-by-one (which is slow and expensive), we group related annotations into a single high-context prompt.

Additionally, we've decoupled the AI processing from the web request using an **Asynchronous Task Queue**. This is why you see **"The Pacer"** animation—it's not just a decoration, but a real-time listener to our backend queue, ensuring that even if a synthesis takes 30 seconds, your browser connection remains rock-solid.
