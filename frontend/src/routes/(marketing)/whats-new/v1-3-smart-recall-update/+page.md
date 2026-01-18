---
layout: changelog
title: "Smart Recall & Premium Glass"
description: "Introducing Shuffle, Restart, and a Smart Feedback Widget with a stunning Glassmorphism UI."
date: "2026-01-18"
version: "v1.3.0"
type: "Update"
---

## What's New?

Today's update brings a major leap in study efficiency and UI aesthetics. We've introduced powerful new controls for your study sessions and a beautiful new home for our evolution log.

### 🔀 Smart Recall: Shuffle & Restart

We've enhanced the study experience to prevent "pattern memorization" and promote true active recall.

- **Fisher-Yates Shuffle**: You can now randomize your deck order with a single click. This ensures you're learning the material, not just the sequence of cards.
- **Instant Restart**: Want to go again? The new restart button immediately resets your session to the first card and resets all flip states.
- **Subtle Controls**: These new features are tucked away in a clean control group in the header, keeping your study environment distraction-free.

### 💬 Smart Feedback Widget

We want to grow with you. The new "Smart" Feedback Widget is designed to capture your journey without being intrusive.

- **Morphing UI**: The widget starts as a subtle nudge and expands into a beautiful 3-step survey (Goals, Pain Points, and Short Feedback).
- **Cheer Mode**: Already submitted? The widget transforms into "Cheer Mode," displaying random motivational quotes to keep your momentum high.
- **Resend Integration**: Your feedback is delivered directly to our support team via the Resend API, ensuring your voice is heard.

### 💎 Premium Glassmorphism UI

We've elevated the visual language of Cubrain with a high-end tech aesthetic.

- **Frosted Glass Effect**: The "What's New" page and feedback widget now feature a premium glassmorphism design with `backdrop-blur-md` and subtle borders.
- **Neural Network Background**: The evolution log now sits on top of a "Future Tech" neural network background, visible as a subtle, connected texture.
- **Gradient Typography**: Titles now feature a clean white-to-slate gradient, making the "Evolution Log" feel as advanced as the tech behind it.

### 🛠️ Refinements & Fixes

- **Accessibility Upgrades**: Improved keyboard navigation and ARIA roles for interactive elements.
- **Tailwind Optimization**: Refactored CSS to use modern Tailwind 4.0 standards where applicable.
- **JWT Security**: Enforced strict JWT authentication for the feedback API.

---

#### **🛠️ Under the Hood: The Fisher-Yates Algorithm**

To ensure truly unbiased randomization, we implemented the **Fisher-Yates (Knuth) Shuffle**. Unlike simple `sort(() => Math.random() - 0.5)`, which has a O(n log n) complexity and can be biased, Fisher-Yates runs in O(n) time and guarantees that every permutation of the deck is equally likely. This ensures that your "Smart Recall" sessions are as scientifically sound as they are efficient.
