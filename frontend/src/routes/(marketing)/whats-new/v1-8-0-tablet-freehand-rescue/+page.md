---
layout: changelog
title: "Tablet & Freehand Rescue: Ink Drawing & 3-Tier Context Pipeline"
description: "Introducing Apple Pencil & S Pen drawing support (Ink Annotations) and typed study note integration (FreeText Annotations) powered by our 3-Tier Context Pipeline."
date: "2026-06-07"
version: "v1.8.0"
type: "Feature Update"
---

## 🎨 Apple Pencil & S Pen Drawing Integration (Ink Annotation)

You can now review your handwritten notes, drawings, and math formulas created with tablets (iPad, Galaxy Tab).

### 🛡️ What's New?

- **Ink Annotation (PDAnnotationInk) Support**: Automatically detects and calculates bounding boxes for Apple Pencil and S Pen strokes.
- **On-demand Image Cropping**: Dynamically crops the visual region of drawing/handwriting overlaid on top of the underlying background text using PDFBox.
- **Gemini Vision Integration**: Transmits cropped images directly to the Gemini Vision API to perform handwriting recognition (HTR) and visual diagram understanding.

---

## ✍️ User Study Notes Integration (FreeText Annotation)

Your typed notes and summaries placed on the PDF are now part of your active recall loop.

### 🛡️ What's New?

- **FreeText Annotation Support**: Automatically extracts user-typed notes directly from the PDF annotation structure.
- **Context-Aware Cards**: Rather than generating simple cards, the AI contextualizes your notes against the surrounding text to test definitions, applications, or translations.

---

## 🧠 3-Tier Context Pipeline Alignment

To prevent AI hallucinations and visual misreading, all annotation types are processed through our **3-Tier Context Pipeline**:

1. **Tier 1 (Target Content)**: Apple Pencil visual drawing cropped image or typed FreeText note content.
2. **Tier 2 (Underlying Text)**: Original text under the highlight, underline, or drawing.
3. **Tier 3 (Surrounding Context)**: Surrounding paragraph context extracted using our coordinate-padding technology (150f padding).

This ensures highly precise and contextually accurate question-and-answer flashcards.

---

## ⚖️ Token Protection & Tier-based Limits

We updated the rate-limiting and token defense layer to protect server costs:
- **Pro User**: Processes up to 1000 pages and 500 annotations per PDF.
- **Free/Guest User**: Processes up to 50 pages and 50 annotations per PDF.
