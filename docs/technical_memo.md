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
