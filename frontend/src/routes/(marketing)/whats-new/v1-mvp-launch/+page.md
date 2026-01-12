---
layout: changelog
title: "The Extractor Solution"
description: "Cubrain V1 Launch: Turn your PDF highlights into flashcards in seconds."
date: "2026-01-02"
version: "v1.0.0"
type: "Launch"
---

## Welcome to Cubrain V1!

“Just put in your study traces. We will do the organizing.”

We are thrilled to launch the MVP of Cubrain. Our goal is to reduce your "flashcard making time" to zero, allowing you to focus entirely on studying.

### 🖍️ The "Extractor" Core

Unlike other AI tools that just "read" the whole PDF, Cubrain respects your study flow. We only process what **you** found important.

- **PDF Annotation Parser**: We use precise coordinate-based extraction to find your highlights and underlines exactly where you made them.
- **Intent-Based Generation**:
  - **Highlights**: AI focuses on concepts, context, and "Why/How" summaries.
  - **Underlines**: AI focuses on facts, definitions, and keywords (Cloze deletion style).
- **Context-Aware Synthesis**: We feed the AI the specific paragraph surrounding your highlight to eliminate hallucinations and ensure accuracy.

### 📥 Export & Integration

We want Cubrain to fit into your existing ecosystem, not replace it.

- **Export to Anki (.csv)**: One-click export formatted perfectly for Anki.
- **Tier Synthesis**:
  - **Free (Smart Synthesis)**: High-impact cards grouped by page.
  - **Pro (Deep Synthesis)**: Comprehensive analysis for detailed, atomic cards.

### 🎯 Who is this for?

Cubrain is built for students and professionals who already use tools like GoodNotes, Notability, or Acrobat. Don't change your habits—just get more value out of them.

---

#### **🛠️ Under the Hood: Precision Extraction**


To solve the "AI Hallucination" problem common in PDF study tools, we developed a **Coordinate-Based Extraction** engine. Instead of dumping raw text, Cubrain maps the exact visual coordinates of your highlights. This allows us to feed the AI the specific text you marked _plus_ the immediate surrounding context (the "Paragraph Anchor"). By anchoring the AI to a specific location on the page, we ensure that the generated cards are 100% grounded in your source material.
