# Cubrain Development Rules & Behavioral Guidelines

This skill enforces the project-specific coding standards, cognitive protocols, and architectural guidelines defined in Cubrain's `AGENTS.md`.

## 🧠 Cognitive Protocol (Mandatory MCP Usage)

- For any task involving **logic implementation, refactoring, debugging, or architectural changes**, you **MUST** use the `sequentialthinking` tool (MCP) as your FIRST step.
- Simple content generation (e.g., "Write a README") or trivial fixes (e.g., typos) do not require this.

## 🏛️ SOLID Principles & Spring Boot Best Practices

- **SRP (Single Responsibility)**: Service classes exceeding 200 lines or handling mixed concerns must be split.
- **OCP (Open/Closed)**: Use Interfaces for components that might change. Use Strategy/Polymorphism instead of `if-else` blocks for switching logic.
- **DIP (Dependency Inversion)**: Depend on abstractions (Interfaces). Organise version-specific logic under `domain/{domain_name}/v1`, `v2` package paths. Naming Convention: Do not version class names (e.g. use `PdfRequestDto`, not `PdfRequestDtoV1`).
- **DTO Record Mapping**: Controllers must return Immutable Record DTOs, never `@Entity` objects. Map using static factory methods: `from(Entity entity)` and `of(params...)`.
- **Transactional**: Do NOT apply `@Transactional` on methods involving external API calls (AI, S3, etc.). Declare `@Transactional(readOnly = true)` at class level for Services.

## ⚡ Svelte 5 Runes Refactoring Rules

- **State ($state)**: Convert `let var = val;` to `let var = $state(val);`.
- **Props ($props)**: Replace `export let prop;` with `let { prop } = $props();`.
- **Derived ($derived)**: Convert `$: double = count * 2;` to `let double = $derived(count * 2);`.
- **Effects ($effect)**: Convert `$: { sideEffect(); }` to `$effect(() => { sideEffect(); });`.
- **Events**: Prefer callback props over `createEventDispatcher`.
- **Icons**: Import from `@lucide/svelte/icons/{icon}` individually to ensure tree-shaking (avoid barrel imports).

## 🛡️ Security, secrets & UX Guardrails

- **XSS Prevention**: Never use `{@html}` on user-controlled inputs. Use standard `{}` wrapping and `white-space: pre-wrap;` with `\n` instead.
- **A11y (Accessibility)**: Ensure interactive elements are reachable/operable via keyboard. Use semantic `<button>` or `<a>` tags.
- **Secret Management**: Never commit secrets to git or write raw passwords in `.env`.
- **File Upload Limits**: Ensure 20MB maximum file size limit is validated on the frontend with appropriate toast notifications (red border feedback for exceeding cards).
