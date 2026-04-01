---
layout: changelog
title: "PDF Stability & Infrastructure Fix"
description: "A specialized update resolving PDF processing failures and optimizing background generation stability."
date: "2026-04-01"
version: "v1.5.1"
type: "Hotfix"
---

## What's New?

This patch brings a critical infrastructure fix to our PDF flashcard generation engine. We've completely refactored how the system handles uploaded documents to ensure high-reliability processing for large academic files.

### 🚑 Robust PDF Processing Engine

We've moved beyond the unstable "single-read" stream model to a high-memory-stability byte-array pipeline:

- **Stream Exhaustion Fix**: Resolved a bug where larger PDFs (5MB+) would fail to produce study traces because the document stream was prematurely exhausted.
- **Async Safety**: Improved the reliability of background generation tasks. Your flashcard generation is now immune to temporary file deletions that could happen during high-traffic periods.
- **Multi-Read Support**: The system now holds a stable, memory-resident copy of the PDF for the duration of the generation lifecycle, ensuring every highlight and underline is captured perfectly.

### 🧹 System Optimization

- **Tracking Cleanup**: Removed legacy tracking scripts (Hotjar/Contentsquare) to further speed up the application's initial load time and enhance user privacy.
- **Stable Metadata Extraction**: Refined the default deck naming logic to better handle special characters and OS-specific filename encodings.

---

#### **🚀 Why This Matters?**

Version 1.5.1 is about **trust**. You should never have to wonder "Will this PDF work?" This update ensures that whether it's a 1-page summary or a 50-page complex academic paper like `comnet-03.pdf`, Cubrain will handle it with precision.
