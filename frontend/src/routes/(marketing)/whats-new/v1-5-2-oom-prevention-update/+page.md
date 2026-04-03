---
layout: changelog
title: "Maximum Memory Efficiency: Disk Spooling Update"
description: "A critical refactoring to the PDF ingestion pipeline, replacing memory-heavy buffers with local disk spooling to handle large files with zero OOM risk."
date: "2026-04-03"
version: "v1.5.2"
type: "Hotfix"
---

## What's New?

Building on our recent stability improvements, we've upgraded our PDF ingestion architecture to be even more memory-efficient. We've transitioned from a memory-resident buffer to a **Local Disk Spooling** model.

### 🛡️ OOM-Safe PDF Ingestion

The system now handles PDF files without loading them entirely into the application's RAM (Heap). This ensures that even the largest documents can be processed without crashing the server.

- **Disk Spooling**: Uploaded files are now safely written to a secure temporary location on the local disk before being processed.
- **On-Demand Loading**: Our PDF engine now loads only the necessary parts of the document from disk, drastically reducing the memory footprint.
- **Zero-Waste Lifecycle**: We've implemented a "garbage-proof" cleanup mechanism that guarantees the deletion of temporary files immediately after processing, even if an error occurs.

### ⚙️ Enhanced Backend Stability

- **Async Reliability**: Refined the asynchronous processing pipeline for flashcard generation, ensuring robustness across the entire document lifecycle.
- **Environment Parity**: Standardized our build infrastructure across development and production environments by locking in JDK 21 at the project level.

---

#### **🚀 Why This Matters?**

While version 1.5.1 introduced a stable data pipeline, version 1.5.2 ensures **scalability**. Whether you're uploading a quick lecture note or a massive 20MB textbook chapter, Cubrain remains lightning-fast and rock-solid, protecting our infrastructure while delivering your study cards with precision.
