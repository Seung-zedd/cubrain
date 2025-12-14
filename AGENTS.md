# Agent Behavioral Rules

♻️this markdown file can be re-used anytime you wanna build a new app

## Documentation Lookup

When I need code generation, setup or configuration steps, or library/API documentation:

1. **Primary:** Always use context7 when I need code generation, setup or configuration steps, or library/API documentation. This means you should automatically use the Context7 MCP tools to resolve library id and get library docs without me having to explicitly ask.

2. **Fallback:** If Context7 is unavailable, use web search to find the official documentation from:
   - GitHub repositories (README, docs folder)
   - Official documentation sites (e.g., docs.langchain4j.dev)
   - Maven Central / package registry pages

Always prioritize official sources over blog posts or Stack Overflow answers.

## Commit Message Convention

We follow a convention combining Gitmoji and Conventional Commits.

**Format:** `[Gitmoji] [Type]: [Title]`

**Gitmoji & Types:**

- ✨ `:sparkles:` `feat`: New features
- 🐛 `:bug:` `fix`: Bug fixes
- 📚 `:books:` `docs`: Documentation changes
- 💄 `:lipstick:` `ui`: UI changes
- ♻️ `:recycle:` `refactor`: Code refactoring
- 🚀 `:rocket:` `deploy`: Deployment tasks
- ⚙️ `:gear:` `chore`: Build/config changes
- ✅ `:white_check_mark:` `test`: Adding/fixing tests
- 🚑 `:ambulance:` `hotfix`: Critical hotfixes

**Note:** Scope is optional and can be omitted as the branch name usually contains the issue number.

**Rule:** After completing a significant task or a series of related changes, **ALWAYS** provide a **single-line** git commit message in the format above. Focus on the most significant change. This allows the user to easily copy and paste it.

## Coding Standards & Design Principles

### 1. SOLID Principles (Strict Enforcement)

- **SRP (Single Responsibility Principle):**
  - Each class must have **one and only one reason to change**.
  - Do NOT create "God Classes". If a Service class exceeds 200 lines or handles mixed concerns (e.g., parsing + saving + email), split it into smaller components.
- **OCP (Open/Closed Principle):**
  - Design for extension. Use **Interfaces** for components that might change implementations (e.g., `AiClient` interface for swapping between Gemini/Claude).
  - Do not use `if-else` blocks for switching logic; use Strategy Pattern or Polymorphism.
- **DIP (Dependency Inversion):**
  - Always depend on abstractions (Interfaces), not concretions.
  - Inject dependencies via Constructor Injection (`@RequiredArgsConstructor`).

### 2. Spring Boot Best Practices

- **DTOs:** Never return `@Entity` objects in Controllers. Always map them to `Record` DTOs (suffix `Dto`).
- **Transactional:** Do NOT apply `@Transactional` on methods involving external API calls (AI, S3, etc.) to prevent connection pool starvation. Apply it only to the DB access layer.
- **Import Style:**
  - **Class Imports:** NO Wildcards. Explicitly import each class (e.g., `import java.util.List;`).
  - **Static Imports:** Aggressively use `import static` for constants, enums, and utility methods to improve readability (e.g., `import static org.springframework.http.HttpStatus.OK;`).

### 3. Package Structure

- **Flat Architecture:** Keep domain packages flat (e.g., `domain/pdf` contains Controller, Service, Repository, DTOs).
- **Refactoring Rule:** Only split a domain package into subpackages (`dto`, `service`, `controller`, etc.) when the file count in that package exceeds 15.

### 4. API Versioning Strategy

- **URI Versioning:** Use URI versioning for all REST API endpoints (e.g., `/api/v1/cards`).
- **Evolution:**
  - Start with `v1` for the MVP.
  - When breaking changes are introduced, create a new controller/endpoint with `v2` (e.g., `/api/v2/cards`).
  - Maintain `v1` for backward compatibility until it can be safely deprecated.
- **Consistency:** Ensure all related endpoints (Controller methods) share the same version prefix.

### 5. Frontend Integration

- **Syncing:** When backend API endpoints change (e.g., versioning updates), IMMEDIATELY update the corresponding frontend API calls.
- **Search:** Grep for the old endpoint path in the `frontend` directory to find all occurrences.

### 6. Svelte 5 Refactoring Rules (Runes)

- **State ($state):** Convert `let var = val;` to `let var = $state(val);`.
- **Props ($props):** Replace `export let prop;` with `let { prop } = $props();`. Use `$bindable()` only if necessary.
- **Derived ($derived):** Convert `$: double = count * 2;` to `let double = $derived(count * 2);`.
- **Effects ($effect):** Convert `$: { sideEffect(); }` to `$effect(() => { sideEffect(); });`.
- **Events:** Prefer callback props over `createEventDispatcher`.
- **Cleanup:** Remove unused imports and ensure `lang="ts"`.

### 7. Documentation Strategy (API & Code)

- **Controllers (Endpoints):**

  - MUST use **`@Operation`** to describe what the API does.
  - **Example:**
    ```java
    @Operation(summary = "Generate Flashcards", description = "Generates Q&A cards from PDF highlights. Returns 429 if quota exceeded.")
    @PostMapping("/generate")
    public ResponseEntity<CardResponseDto> generate(...)
    ```

- **DTOs (Request/Response Bodies):**

  - MUST use **`@Schema`** for fields to provide descriptions and examples.
  - **Reason:** To populate "Body Params" and "Example Value" in Apidog/Swagger.
  - **Example:**

    ```java
    public record CardRequestDto(
        @Schema(description = "Extracted text from PDF", example = "TCP is a connection-oriented protocol...")
        String sourceText,

        @Schema(description = "User Type", example = "FREE_USER")
        String userTier
    ) {}
    ```

- **Internal Logic:**
  - No boilerplate Javadoc. Use inline comments (`//`) only for complex business logic.

### 8. Localization Rule

- **English Only:** All annotations, comments, and documentation MUST be written in English. This applies to all files (Java, Svelte, JS, etc.).
