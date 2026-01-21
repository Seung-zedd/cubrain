---
layout: changelog
title: "Context-Aware Nudges & Anki Mastery"
description: "Introducing a smarter nudge system, a seamless Anki import guide, and premium post-study motivation."
date: "2026-01-21"
version: "v1.4.0"
type: "Update"
---

## What's New?

Today's update focuses on making Cubrain feel more "alive" and helpful. We've introduced a context-aware nudge system that guides you through the entire workflow, from uploading PDFs to mastering your cards in Anki.

### 🧠 Context-Aware Nudge System

We've replaced static notifications with a dynamic, context-aware system that understands where you are in your study journey.

- **Smart Loading Tips**: While our AI analyzes your PDFs, you'll now see helpful tips and context-gathering nudges right above the progress bar.
- **Floating Feedback Tooltips**: After generating cards, a subtle, centered tooltip appears to gather your feedback on card quality. It's non-intrusive and disappears once you've had your say.
- **Unified Motion Design**: All nudges now share a premium "fly-in" animation logic (y: 50, 600ms) that matches the elegant entry of your flashcards.

### 📥 Anki Import Guide

Exporting to Anki is one of our most popular features, so we've made the import process foolproof.

- **Morphing Guide**: A new "Anki Import Guide" appears after you export a deck. It starts as a small card and morphs into a beautiful, step-by-step modal.
- **Staggered Readability**: Each step of the guide features high-contrast text and staggered entry animations, making it easy to follow even on small screens.

### 🎊 Post-Study "Cheer Mode"

Your hard work deserves to be celebrated. We've reactivated the post-study nudge to give you that extra boost of motivation.

- **Motivational Quotes**: After finishing a session, you'll be greeted with random quotes like "Knowledge locked in. 🧠" or "1% better every day. 🚀"
- **Dismissible & Lightweight**: The nudge is designed to be a quick "pat on the back" that you can dismiss instantly or let it fade away after a few seconds.

### 🚀 Launch Banner for Guests

We want everyone to be part of the Cubrain journey. The "Launch Sale" banner is now visible to guest users, ensuring that even first-time visitors can take advantage of our lifetime discount.

### 🛠️ Refinements & Fixes

- **Hotjar Integration**: We've integrated Hotjar to better understand how you use Cubrain and where we can improve.
- **Contrast Optimization**: Improved text colors in modals and nudges for better accessibility on dark backgrounds.
- **Transition Synchronization**: Fixed several "abrupt" disappearing animations by unifying the `in:fly` and `out:fade` logic across the app.

---

#### **🛠️ Under the Hood: Contextual State Management**

To power the new nudge system, we implemented a more robust contextual state machine. Instead of simple booleans, the system now evaluates the user's current route, job status, and previous interactions to decide which "Nudge" is most relevant. This ensures that you're never overwhelmed with information, but always have the right tip at the right time.
