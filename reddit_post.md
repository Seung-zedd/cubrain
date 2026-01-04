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
- **AI**: LangChain4j + Naver CLOVA Studio.

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
3. It generates high-quality Q&A flashcards based on _exactly_ what you marked.

**Why it’s different**:

- **Precision Extraction**: It doesn't just "guess" what's important. It looks exactly at what you highlighted so the questions are actually relevant to your exam.
- **Gamified Learning**: It has "Elemental Effects" (Fire, Water, etc.) when you get answers right to keep you motivated.
- **Export to Anki**: You can download everything as a CSV and import it straight into Anki.

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
- **Intent-Based Prompting**:
  - `Highlight` -> Conceptual questions (Why/How).
  - `Underline` -> Factual/Cloze-style questions.
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

1. Upload PDF.
2. AI extracts your highlights with high precision.
3. Review generated cards with "Elemental Effects" animations.
4. Export to Anki.

Built with **Svelte 5** and **Spring Boot**. I focused heavily on making the extraction precise so the AI doesn't hallucinate.

Would love any feedback on the UI/UX or the card quality!

Link: [Link to Cubrain]
