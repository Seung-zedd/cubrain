# Reddit Promotion Drafts: Cubrain

> [!TIP] > **IP Protection Strategy**: These drafts are designed to build authority without giving away your "recipe."
>
> - **Technical Subs**: We mention the _strategy_ (coordinate-based extraction) to build trust, but not the _implementation_ (exact prompt logic, coordinate math, or padding values).
> - **Non-Technical Subs**: We focus entirely on the _outcome_ (precision) and remove all technical jargon.

---

## 🏗️ r/buildinpublic & r/SaaS

**Focus**: Technical journey and the "why" behind the project.

**Title**: I built a "PDF-to-Anki" tool using Svelte 5 and Spring Boot. Here’s how I solved the "AI Hallucination" problem in PDF parsing.

**Body**:
Hey everyone! I’ve been building **Cubrain**, an AI-powered study tool that turns PDF annotations into flashcards in seconds.

**The Problem**: Most AI study tools just "read" the whole PDF, leading to generic questions. I wanted something that respects _my_ study flow—only turning the parts I actually highlighted into cards.

**The Stack**:

- **Frontend**: Svelte 5 (Runes are a game changer!), Tailwind CSS 4.
- **Backend**: Spring Boot 3.5, Java 21.
- **AI**: LangChain4j + Google Gemini.

**The Technical Choice**: Instead of simple text dumping, I implemented a **precise extraction strategy**. By mapping the exact coordinates of user highlights, I can feed the AI the specific context it needs without the "noise" of the rest of the page. This virtually eliminated hallucinations in the generated cards.

**What I learned**: Batching LLM requests by page saved me ~60% in API costs and significantly reduced latency.

I’d love to hear your thoughts on this "Synthesis-based" prompting approach!

Check it out here: [Link to Cubrain]

---

## 📚 r/studytips & r/GetStudying

**Focus**: Efficiency, saving time, and better grades.

**Title**: Stop wasting hours making flashcards. I made a tool that does it for you from your PDF highlights.

**Body**:
Hi everyone! Like many of you, I spent way too much time _making_ flashcards instead of actually _studying_ them.

I built **Cubrain** to fix this. It’s a simple web app where you:

1. Upload your PDF.
2. It automatically finds all your **Highlights** and **Underlines**.
3. AI generates high-quality flashcards using Tier Synthesis (Smart Synthesis for free users, Deep Synthesis for Pro).

**Why it’s different**:

- **Precision Extraction**: It doesn't just "guess" what's important. It looks exactly at what you highlighted so the questions are actually relevant to your exam.
- **Export to Anki**: You can download everything as a CSV and import it straight into Anki.
- **Gamified Learning(This feature would be added if the early adopters are more than 100 users)**: It has "Elemental Effects" (Fire, Water, Spark, and Wind) when you get answers right to keep you motivated.

It's currently in MVP/Demo mode and I'd love for some students to try it out and tell me what you think!

Try it here: [Link to Cubrain]

---

## 🗃️ r/Anki

**Focus**: Power users, Anki integration, and the "Minimum Information Principle".

**Title**: [Showcase] Automate your Anki card creation from PDF highlights (Precision Extraction)

**Body**:
Fellow Anki users! We all know the "Minimum Information Principle" is key, but manually creating atomic cards from a 100-page PDF is a nightmare.

I built **Cubrain** to automate the "Extraction -> Card Creation" pipeline while keeping the cards high-quality.

**Key Features for Anki Users**:

- **Precision Extraction**: It identifies the exact areas you marked in your PDF. No more copy-pasting or cleaning up messy text dumps.
- **Tier Synthesis**:
  - **Free (Smart Synthesis)**: Groups highlights by page to create concise, high-impact cards.
  - **Pro (Deep Synthesis)**: Performs a deeper analysis for comprehensive, detailed card generation.
- **Atomic Cards**: The AI is specifically tuned to follow the Minimum Information Principle—one fact per card.
- **CSV Export**: One-click export formatted perfectly for Anki import.

I'm looking for some power users to stress-test the extraction logic. Does it handle your complex PDFs correctly?

Link: [Link to Cubrain]

---

## 🚀 r/SideProject

**Focus**: Quick pitch, demo, and feedback.

**Title**: Cubrain: Turn your PDF highlights into flashcards in seconds 🧠⚡

**Body**:
Hey r/SideProject!

I’m sharing **Cubrain**, a tool I built to bridge the gap between "reading a PDF" and "actually remembering it."

**The Flow**:

1. **Upload PDF**: The system maps the exact visual coordinates of your highlights and underlines.
2. **Precision Extraction**: It captures the specific text and surrounding context to ensure the AI understands your intent, eliminating hallucinations.
3. **Tier Synthesis**: AI intelligently batches your notes (Free: Smart Synthesis / Pro: Deep Synthesis) to generate high-quality, non-redundant cards.
4. **Export to Anki**: Download your cards as a perfectly formatted CSV.

Built with **Svelte 5** and **Spring Boot**. I focused heavily on making the extraction precise so the AI doesn't hallucinate.

Would love any feedback on the UI/UX or the card quality!

---

## 🌎 r/languagelearning

**Focus**: Vocabulary in context, reading comprehension, and Anki integration.

**Title**: I built a tool to turn foreign language PDF highlights into Anki cards instantly (with context!)

**Body**:
Hey language learners!

One of the biggest hurdles in reading foreign texts is the "Highlight -> Look up -> Copy to Anki" loop. It breaks your flow.

I built **Cubrain** to automate this. It’s an AI-powered tool that turns your PDF highlights directly into high-quality flashcards.

**Why it’s great for language learning**:

- **Contextual Extraction**: It doesn't just grab the word you highlighted. It captures the surrounding sentence/paragraph so your Anki cards have the necessary context for better retention.
- **Precision**: Because it uses coordinate-based extraction, it handles complex layouts (like dual-language PDFs) much better than standard text scrapers.
- **Anki Ready**: Export your vocabulary list as a CSV and import it straight into your decks.

I’m currently using it for my own reading and would love to see if it helps your workflow too!

Link: [Link to Cubrain]

---

## 🩺 r/medschoolanki

**Focus**: High volume, precision, and "Minimum Information Principle".

**Title**: [Showcase] High-precision PDF-to-Anki converter for heavy study loads (Zero Hallucination)

**Body**:
Fellow med students! We all deal with 1000+ page PDFs and the constant struggle of making cards that actually follow the Minimum Information Principle.

I built **Cubrain** to solve the "AI Hallucination" problem in automated card creation.

**Key Features for Med Students**:

- **Precision Extraction**: It maps the exact visual coordinates of your highlights. No more messy text dumps or the AI "guessing" what you meant.
- **Tier Synthesis**:
  - **Smart Synthesis**: Groups highlights by page to create concise, high-yield cards.
  - **Deep Synthesis**: For when you need every detail captured accurately.
- **Batch Processing**: I implemented a page-based batching strategy that reduces latency by 90%. You can process a whole chapter's highlights in seconds.
- **Atomic Cards**: The AI is tuned to create one-fact-per-card, perfect for the Anki workflow.

It’s in MVP/Demo mode and I’d love some feedback from the community on how it handles complex medical diagrams and terminology.

Link: [Link to Cubrain]

---

## ⚡ r/productivity

**Focus**: Workflow optimization and the "Reading to Remembering" bridge.

**Title**: Cubrain: The missing bridge between "Reading" and "Remembering" 🧠

**Body**:
Hi r/productivity!

We all spend hours reading and highlighting PDFs, but how much of that actually sticks? Most of our highlights just go to "digital graveyards."

I built **Cubrain** to turn those highlights into active recall sessions instantly.

**The Workflow**:

1. **Highlight** your PDF as you normally would.
2. **Upload** to Cubrain.
3. **Generate**: AI turns your specific highlights into atomic flashcards.
4. **Smart Recall**: Use the built-in study mode with **Fisher-Yates Shuffle** and **Instant Restart** to start memorizing immediately.

It’s built with a "Precision First" approach—it only looks at what _you_ marked as important, so the cards are 100% relevant to your goals.

Would love to hear how this fits into your second brain or study workflow!

Link: [Link to Cubrain]

---

## 💻 r/SvelteJS

**Focus**: Technical implementation, Svelte 5 Runes, and performance.

**Title**: Building a complex AI study tool with Svelte 5 Runes. Here’s what I learned.

**Body**:
Hey Svelte devs!

I just finished the MVP for **Cubrain**, a PDF-to-Anki tool built with **Svelte 5** and **Spring Boot**.

**Technical Highlights**:

- **Svelte 5 Runes**: Used `$state`, `$derived`, and `$effect` for a complex PDF viewer and real-time progress tracking. The DX improvement over Svelte 4 is massive.
- **Fisher-Yates Shuffle**: Implemented for unbiased randomization in the study mode.
- **SSE (Server-Sent Events)**: Built a custom SSE client to handle real-time progress updates with JWT authorization (since native `EventSource` doesn't support headers).
- **Tailwind 4 & Glassmorphism**: Leveraged the new Tailwind 4 standards for a premium, high-performance UI.

I’m particularly proud of the **Morphing UI** in the feedback widget—it transitions from a subtle nudge to a 3-step survey using Svelte's built-in transitions.

Happy to answer any questions about the Svelte 5 migration or the AI integration!

Repo: [Link to GitHub]
Demo: [Link to Cubrain]
