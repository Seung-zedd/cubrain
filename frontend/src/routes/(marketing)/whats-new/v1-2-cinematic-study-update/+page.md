---
layout: changelog
title: "Cinematic Study Experience"
description: "Immersive library dashboard, desk lamp lighting, and real-time markdown previews."
date: "2026-01-12"
version: "v1.2.0"
type: "Update"
---

## What's New?

This update transforms your study environment into a premium, cinematic experience. We've focused on immersion, lighting, and a more intuitive card editing workflow.

### 🎭 Cinematic Library Dashboard

We've completely redesigned the library view to feel like a high-end study sanctuary.

- **Frosted Glass Aesthetic**: Deck cards now feature a premium glassmorphism effect with `backdrop-blur` and semi-transparent backgrounds, blending perfectly with the library environment.
- **"The Ritual" Transition**: Entering a study session now triggers a cinematic "switch-on" sound and a smooth fade-to-black transition, simulating the heating up of a vintage projector bulb.
- **Data-Rich Cards**: We've restored the yellow progress bars and detailed metadata (last studied, card count) to the library view, so you can track your progress at a glance.

### 💡 Desk Lamp Lighting (The "God Ray")

The study session lighting has been refined to mimic a real desk lamp.

- **Top-Down Lighting**: The spotlight now originates from the top center of the screen, casting a warm, focused glow over your flashcards.
- **Downward Shadows**: To reinforce the lighting illusion, flashcards now cast realistic downward shadows, adding depth and immersion to your study session.
- **Warm Amber Tint**: We've mixed in a subtle amber tint to the light source to match the Cubrain theme and reduce eye strain during long study sessions.

### 📝 Real-time Markdown Previews

Editing your decks is now more intuitive than ever.

- **Live Previews**: The "Edit Deck" modal now features a real-time preview section for both questions and answers. See exactly how your bold text and cloze deletions will look before you save.
- **Strict Validation**: We've added visual validation (red borders and tooltips) to prevent saving empty cards, ensuring your decks are always high-quality.

### 🐛 Bug Fixes & Refinements

- Fixed a critical bug where flashcards would not appear after entering study mode.
- Corrected markdown rendering issues in the flashcard display.
- Removed unwanted browser artifacts like focus outlines and mobile tap highlights for a purely visual experience.

---

#### **🛠️ Under the Hood: The Physics of Light**

To achieve the "Desk Lamp" effect, we moved away from a simple centered radial gradient. By positioning the gradient center at `50% -10%` and using a mix of warm amber (`rgba(255, 190, 100, 0.15)`) and deep black, we created a conical light spread that naturally draws the eye to the center of the screen. Combined with custom `box-shadow` logic that shifts based on this virtual light source, the UI achieves a level of physical presence rarely seen in study apps.
