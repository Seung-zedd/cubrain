# Technical Memo: Problem Solving in Cubrain

## Project 1: Cubrain

"Turn Your PDFs into Flashcards in Seconds."

**Period**: 2025.12.01 ~ 2026.01.01

**Tech Stack**:

- **Frontend**: Svelte 5, Vite, Tailwind CSS 4, Lucide Svelte, Supabase JS.
- **Backend**: Java 21, Spring Boot 3.5, PostgreSQL, Spring Data JPA, QueryDSL, Flyway.
- **AI & PDF**: LangChain4j, Apache PDFBox, Google Gemini.
- **Auth & API**: JWT, OAuth 2.0, Apidog (OpenAPI).

repo address: https://github.com/Seung-zedd/cubrain.git

---

## Technical Problem Solving

### Problem 1: Precision in PDF Annotation Extraction

#### Why: What was the challenge?

The core value of Cubrain is turning _user-marked_ content into flashcards. However, extracting text from PDF annotations (highlights and underlines) is notoriously difficult. Standard PDF text extraction often loses the connection between the visual markup and the underlying text. Simply extracting all text from a page leads to generic, irrelevant questions that don't match the user's specific intent.

#### What: What was the solution?

I developed a coordinate-based extraction system using **Apache PDFBox**. Instead of treating the PDF as a flat text file, the system identifies the exact geometric coordinates (`PDRectangle`) of each highlight and underline. It then "strips" the text only from those specific areas, ensuring that the AI receives exactly what the user intended to study.

#### How: How was it implemented?

1. **Coordinate Mapping**: Used `PDAnnotationTextMarkup` to retrieve the `getRectangle()` coordinates for each annotation.
2. **Area-Based Stripping**: Implemented `PDFTextStripperByArea` to define specific regions for extraction.
3. **Expansion Logic**: Added a small "expansion" (2.0f) to the coordinates to account for anti-aliasing and slight misalignments in PDF rendering, ensuring no characters were cut off.
4. **Contextual Awareness**: For each annotation, the system also extracts a larger "context region" (150f padding) around the highlight. This context is passed to the LLM to prevent hallucinations and provide the necessary background for generating high-quality questions.

---

### Problem 2: LLM Cost and Latency Optimization

#### Why: What was the challenge?

Generating flashcards one-by-one for every highlight in a large PDF is both expensive and slow. Making 50 separate API calls for a 50-highlight document results in significant latency (30-60 seconds) and high token overhead due to repeated system prompts and context.

#### What: What was the solution?

I implemented a **Synthesis-Based Batch Processing** strategy. Instead of individual calls, the system groups annotations by page and processes them in a single, structured LLM request per page. This reduces the number of API calls by up to 90% and allows the AI to "see" all highlights on a page simultaneously, leading to more coherent and non-redundant card generation.

#### How: How was it implemented?

1. **Grouping Logic**: Used Java Streams to group `AnnotationResultDto` objects by `pageIndex` into a `TreeMap`.
2. **Structured Prompting**: Created a prompt that passes a JSON array of annotations (type, text, context) to the LLM.
3. **Synthesis Instruction**: Instructed the LLM to "synthesize these annotations into 1-2 high-quality flashcards" (for free users) or "detailed cards for each" (for pro users), allowing for tier-based value differentiation.
4. **Async Progress Tracking**: Integrated a `JobManager` to track the batch processing progress and update the frontend in real-time via a progress bar, improving the perceived performance for the user.

---

### Problem 3: API Resilience and Rate Limiting

#### Why: What was the challenge?

External AI APIs (like Google Gemini) often impose strict rate limits or experience transient network timeouts. Without a robust handling mechanism, a single failed request would crash the entire PDF processing job, forcing the user to restart and waste time/tokens.

#### What: What was the solution?

I built a **Self-Healing Request Mechanism** using the **Exponential Backoff** strategy. The system automatically detects retryable errors (like 429 Resource Exhausted or timeouts) and pauses before attempting the request again with increasing delays.

#### How: How was it implemented?

1. **Retry Logic**: Wrapped the `chatModel.generate` call in a `generateWithRetry` method.
2. **Error Classification**: Implemented logic to distinguish between "retryable" errors (429, 503, timeouts) and "terminal" errors (401 Unauthorized, 400 Bad Request).
3. **Exponential Backoff**: Used a loop with `Thread.sleep(delayMs)` where `delayMs` doubles after each failed attempt (3000ms -> 6000ms -> 12000ms).
4. **Graceful Degradation**: If all retries fail, the system logs the error for the specific page but continues processing the rest of the document, ensuring the user still gets as many cards as possible.

---

### Problem 4: Hybrid JWT Decoding for Multi-Provider Auth

#### Why: What was the challenge?

Cubrain supports multiple authentication methods: Social Login (Google via Supabase) and traditional Email/Password login. These methods use fundamentally different signing algorithms—Social logins typically use asymmetric encryption (RS256/ES256) via OIDC Discovery, while Email logins use symmetric encryption (HS256) with a shared secret. Standard Spring Security configurations usually expect a single `JwtDecoder`, making it difficult to support both simultaneously without complex workarounds.

#### What: What was the solution?

I implemented a **Hybrid JwtDecoder** that dynamically routes incoming tokens to the appropriate decoding logic based on their header information. This allows the application to seamlessly validate tokens from different providers and algorithms within a single security filter chain.

#### How: How was it implemented?

1. **Dual Decoder Initialization**: Created two distinct `NimbusJwtDecoder` instances: one for asymmetric (OIDC) and one for symmetric (HS256) validation.
2. **Header Inspection**: Implemented a custom `JwtDecoder` lambda that first decodes the JWT header (Base64) to inspect the `alg` (algorithm) claim.
3. **Dynamic Routing**:
   - If the header contains `HS256`, the token is routed to the symmetric decoder.
   - Otherwise, it defaults to the asymmetric decoder (which handles ES256/RS256 via issuer discovery).
4. **Unified Validation**: Both decoders share a common set of validators (Audience, Issuer, Expiration) to ensure consistent security policies across all login types.

---

### Problem 5: Guest Usage Tracking & Rate Limiting

#### Why: What was the challenge?

To provide a low-friction "Try Before You Buy" experience, Cubrain allows guest users to generate flashcards without an account. However, this opens the door to abuse and high API costs if a single user uploads massive amounts of data. Since guests don't have a persistent `userId`, traditional database-level usage tracking isn't directly applicable.

#### What: What was the solution?

I developed a **Client IP-Based Usage Tracking System**. This system treats the user's IP address as a temporary identifier, allowing the application to enforce daily limits on guest uploads while maintaining a frictionless user experience.

#### How: How was it implemented?

1. **IP Extraction**: Implemented logic in `AuthController.java` to extract the client's IP address, prioritizing the `X-Forwarded-For` header (to handle proxy/load balancer scenarios) and falling back to `request.getRemoteAddr()`.
2. **Unified Service Layer**: Integrated the IP-based logic into the existing `UsageLimitService`. The service maintains a separate tracking bucket for guests, keyed by their IP.
3. **Frictionless Fallback**: The `getUsage` endpoint first checks for a valid JWT. If absent, it automatically falls back to IP-based tracking, ensuring the frontend always has accurate usage data to display to the user.
4. **Resilient Identification**: By using the IP address, we strike a balance between preventing mass abuse and allowing legitimate users to explore the product without immediate registration.

---

### Problem 6: Real-time Progress Tracking with SSE

#### Why: What was the challenge?

The initial implementation of the PDF generation progress bar relied on **Short Polling** (`setInterval`). This caused several issues:

1. **Network Overhead**: Over 130 HTTP requests per minute for a single user, leading to unnecessary server load.
2. **Preflight Latency**: Each polling request triggered a CORS `OPTIONS` preflight, adding significant latency and redundant network traffic.
3. **Poor UX**: The progress bar felt "stuttery" due to the fixed polling interval, and mobile users experienced faster battery drain.

#### What: What was the solution?

I refactored the data fetching mechanism from polling to **Server-Sent Events (SSE)**. This allows the server to push progress updates to the client over a single, persistent HTTP connection, providing a smooth, real-time experience while drastically reducing network noise.

#### How: How was it implemented?

1. **Thread-Safe Broadcasting**: Updated the `JobManager` to manage `SseEmitter` instances using a `ConcurrentHashMap` combined with `CopyOnWriteArrayList`. This ensures that multiple async threads can broadcast progress updates without triggering `ConcurrentModificationException`.
2. **Railway/Nginx Compatibility**: Implemented a critical header `X-Accel-Buffering: no` in the `SseController`. Without this, proxies like Nginx or Railway's edge nodes would buffer the stream, causing the progress bar to jump from 0% to 100% instantly instead of showing gradual progress.
3. **Universal SSE Client**: Developed a custom frontend utility using `fetch` and `ReadableStream` instead of the native `EventSource`. This was necessary to support **JWT Authorization headers**, which the native `EventSource` API does not allow.
4. **CORS Optimization**: Set `Access-Control-Max-Age` to `3600` in the `CorsConfig` to cache preflight results for one hour, effectively eliminating redundant `OPTIONS` requests during the session.
5. **Robust Lifecycle Management**: Integrated auto-reconnection logic on the frontend and ensured proper cleanup (aborting the stream) on component unmount or job completion to prevent memory leaks.

---

### Problem 7: Unbiased Randomization for Active Recall

#### Why: What was the challenge?

To prevent "pattern memorization" (where a student remembers the order of cards rather than the content), a robust shuffle mechanism is essential. A naive implementation like `deck.sort(() => Math.random() - 0.5)` is computationally inefficient (O(n log n)) and, more importantly, statistically biased, leading to certain permutations appearing more frequently than others.

#### What: What was the solution?

I implemented the **Fisher-Yates (Knuth) Shuffle** algorithm. This ensures that every possible permutation of the deck is equally likely, providing a truly unbiased randomization for the "Smart Recall" feature.

#### How: How was it implemented?

1. **Algorithm Selection**: Chose Fisher-Yates for its O(n) time complexity and mathematical correctness.
2. **In-Place Mutation**: The algorithm iterates through the array from the last element to the first, swapping each element with a randomly selected one from the remaining unshuffled portion.
3. **Svelte 5 Integration**: Wrapped the shuffle logic in a Svelte 5 `$state` update, ensuring that the UI reacts instantly to the new card order while maintaining the "Restart" capability by keeping a reference to the original sequence.

---

### Problem 8: Non-Intrusive Feedback Collection with Morphing UI

#### Why: What was the challenge?

User feedback is critical for an MVP, but intrusive pop-ups or static forms often lead to low engagement or "form fatigue." The challenge was to create a feedback mechanism that felt like a natural part of the user journey, encouraging participation without disrupting the study flow.

#### What: What was the solution?

I developed a **Morphing Feedback Widget** that uses a multi-stage state machine to guide the user. It starts as a subtle "nudge" and expands into a structured 3-step survey, finally transforming into a "Cheer Mode" once submitted.

#### How: How was it implemented?

1. **State-Driven UI**: Used Svelte 5 Runes to manage the widget's lifecycle (`HIDDEN` -> `NUDGE` -> `SURVEY` -> `CHEER`).
2. **Step-by-Step Survey**: Implemented a 3-step flow (Goals -> Pain Points -> Feedback) to lower the cognitive load for the user.
3. **Cheer Mode**: After submission, the widget morphs into a motivational tool, displaying random quotes to keep the user's momentum high.
4. **Resend Integration**: Connected the backend to the **Resend API** to deliver feedback directly to the development team, ensuring a closed-loop communication channel.

---

### Problem 9: High-Performance Glassmorphism & Visual Polish

#### Why: What was the challenge?

Modern "Premium" aesthetics often rely on heavy blur effects and complex gradients, which can significantly impact rendering performance, especially on mobile devices or lower-end hardware. The goal was to achieve a high-end "Tech" look (Glassmorphism) without sacrificing the 60fps smoothness of the Svelte 5 application.

#### What: What was the solution?

I implemented a **Layered Design System** using **Tailwind CSS 4.0** and optimized CSS properties. By carefully balancing `backdrop-blur` with hardware-accelerated transforms, I achieved a "Frosted Glass" effect that feels premium yet performant.

#### How: How was it implemented?

1. **Tailwind 4 Optimization**: Leveraged the latest Tailwind 4 standards for cleaner utility classes and better build-time optimization.
2. **Neural Network Background**: Created a "Future Tech" background using a subtle, connected texture that sits behind the glass layers, providing depth without adding heavy image assets.
3. **Gradient Typography**: Used `bg-clip-text` with white-to-slate gradients to make titles pop against the dark theme.
4. **Performance Tuning**: Applied `will-change: transform` and limited the use of `backdrop-filter` to key interactive elements to ensure smooth transitions and scrolling.

---

### Problem 10: Internationalization (I18n) Routing and State Mismatch

#### Why: What was the challenge?

Integrating `@inlang/paraglide-sveltekit` with Svelte 5 introduced two major hurdles:

1. **Prerendering Loops**: During the build, SvelteKit's crawler encountered "double language prefixes" (e.g., `/ko/ko/demo`). This happened because `i18n.resolveRoute` was called with the current `page.url.pathname`, which already included a locale, causing it to prepend the locale again.
2. **State Mismatch**: On the client side, the URL would successfully update to `/ko`, but the UI text remained in English. This was because the Paraglide translation runtime (`languageTag()`) was not reactively linked to SvelteKit's routing lifecycle by default in Svelte 5.

#### What: What was the solution?

I implemented a two-fold solution:

1. **Canonical Path Resolution**: Created a utility to strip existing language prefixes before resolving new routes, preventing URL nesting.
2. **Robust Reactive Sync**: Built a bridge between SvelteKit's URL state and Paraglide's runtime state through a global UI store and a layout-level `$effect`.

#### How: How was it implemented?

1. **Path Normalization**: Implemented `getCanonicalPath()` to ensure all localized links are generated from a base path without existing locale segments.
2. **Environment Safety**: Wrapped search parameter access in `building` checks from `$app/environment` to prevent build-time crashes.
3. **Reactive state store**: Added `lang` to the `uiState` store and used `onSetLanguageTag` to create a single source of truth for the active locale.
4. **URL-to-Runtime Bridge**: Added an `$effect` in `+layout.svelte` that monitors `page.url.pathname`. If the URL segment (e.g., `/ko`) differs from the runtime state, it forcefully updates the runtime using `setLanguageTag`.
5. **Component Re-hydration**: Wrapped the application content in a `{#key uiState.lang}` block. This ensures that every component re-renders and re-evaluates its `m.hello()` functions whenever the user switches languages, resolving the stale UI issue.
