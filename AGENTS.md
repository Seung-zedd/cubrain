# Agent Behavioral Rules

♻️ _This markdown file can be re-used anytime you wanna build a new app_

## 1. 🧠 Cognitive Protocol (Mandatory MCP Usage)

**Rule: Think Before You Code.**
For any task involving **logic implementation, refactoring, debugging, or architectural changes**, you **MUST** use the `sequentialthinking` tool (MCP) as your FIRST step.

**Trigger Conditions:**

- When the user asks for a feature implementation (e.g., "Implement Prompt Batching").
- When analyzing a bug or a complex error log.
- When planning a refactoring strategy.

**Execution Steps:**

1.  **Initiate `sequentialthinking`:** Do not output any code or text explanation until you have invoked this tool.
2.  **Analyze & Plan:** Use the tool to:
    - Break down the user's request into atomic steps.
    - Identify potential risks (e.g., "Will this break the `dev` branch?", "Is this thread-safe?").
    - Formulate a hypothesis or a step-by-step implementation plan.
3.  **Review:** Only after the sequential thinking process is complete and you have a clear path, proceed to write code or answer.

**Exception:**

- Simple content generation (e.g., "Write a README") or trivial fixes (e.g., typos) do not require this tool.

## 2. Documentation Lookup

When I need code generation, setup or configuration steps, or library/API documentation:

1. **Primary:** Always use context7 when I need code generation, setup or configuration steps, or library/API documentation. This means you should automatically use the Context7 MCP tools to resolve library id and get library docs without me having to explicitly ask.
2. **Fallback:** If Context7 is unavailable, use web search to find the official documentation from:
   - GitHub repositories (README, docs folder)
   - Official documentation sites (e.g., docs.langchain4j.dev)
   - Maven Central / package registry pages

Always prioritize official sources over blog posts or Stack Overflow answers.

## 3. Commit Message Convention

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

**Rule:** After completing a significant task or a series of related changes, **ALWAYS** provide a **single-line** git commit message in the format above with adding adequate gitmoji at the start of the commit message. Focus on the most significant change.

## 4. Testing Strategy (TestSprite Priority)

i won't use testsprite for a while since it's not working well.(maybe this will be changed in the future)

We prioritize automated testing using **TestSprite** to ensure high quality and reliability.

1. **Primary:** Always use **TestSprite** for test generation and execution (Frontend & Backend).
   - Use `testsprite_bootstrap_tests` to initialize.
   - Use `testsprite_generate_backend_test_plan` or `testsprite_generate_frontend_test_plan`.
   - Use `testsprite_generate_code_and_execute` to run tests and generate reports.
2. **Secondary:** Use conventional testing frameworks (JUnit 5, Mockito for Java; Vitest/Svelte Testing Library for Frontend) only when:
   - TestSprite test generation or execution has failed for **3 consecutive attempts**.
   - TestSprite is unavailable or unable to cover specific edge cases.
   - Writing simple unit tests that don't require full environment orchestration.

Always provide the TestSprite markdown report after running tests.

## 5. Coding Standards & Design Principles

### 5.1 SOLID Principles (Strict Enforcement)

- **SRP (Single Responsibility Principle):**
  - Each class must have **one and only one reason to change**.
  - Do NOT create "God Classes". If a Service class exceeds 200 lines or handles mixed concerns, split it.
- **OCP (Open/Closed Principle):**
  - Design for extension. Use **Interfaces** for components that might change implementations.
  - Do not use `if-else` blocks for switching logic; use Strategy Pattern or Polymorphism.
- **DIP (Dependency Inversion):**

  - Always depend on abstractions (Interfaces), not concretions.
  - **Package-based Versioning**: Organize all version-specific logic into sub-packages: `domain/{domain_name}/v1`, `domain/{domain_name}/v2`.
  - **Naming Convention**: Do NOT include version numbers in class names (e.g., use `PdfRequestDto`, not `PdfRequestDtoV1`). The version context must be provided ONLY by the package path. This applies to Controllers, Services, and DTOs.
  - **Encapsulation**: Keep the `v1` package as a completely independent "Actor". Shared JPA Entities remain in the root of the domain package.;

  - **Flat Architecture**: Keep all files directly under the `v1` or `v2` folder. Do not create deeper sub-folders like `/v1/dto/` unless the file count exceeds 15. if the file count exceeds 15, create a sub-folder like `/v1/dto/`.
  - **Dependency Injection**: Always inject the interface, not the concrete class. Use `@RequiredArgsConstructor` for constructor injection. Use `@Primary` or `@Qualifier` if multiple versions exist.

### 6. Spring Boot Best Practices

- **DTOs:** Never return `@Entity` objects in Controllers. Always map them to `Record` DTOs.
- **Configuration Properties:** Use Java `record` for `@ConfigurationProperties` to ensure immutability. Use compact constructors for validation and `@ConfigurationPropertiesScan` in the main application class to enable constructor binding.
- **Naming Convention:** All Data Transfer Objects must end with the suffix `Dto` (e.g., `CardRequestDto`).
- **Transactional:** Do NOT apply `@Transactional` on methods involving external API calls (AI, S3, etc.) to prevent connection pool starvation. Apply it only to the DB access layer.
  Also, declare `@Transactional(readOnly = true)` for class level and override for method level if need for every ServiceImpl layer.
- **Import Style:**
  - **Class Imports:** NO Wildcards. Explicitly import each class.
  - **Static Imports:** Aggressively use `import static` for constants, enums, and utility methods (e.g., `HttpStatus.OK`) to improve readability.
- **Annotation Style:**
  - **Implicit Names:** Omit the name parameter if the variable name matches (e.g., use `@RequestParam String name` instead of `@RequestParam("name") String name`).

### 7. Object Creation & Mapping Strategy (Entity vs. DTO)

To maintain consistency, readability, and encapsulation, we will distinguish how Entities and DTOs are instantiated.

**A. Entity Creation (Domain Layer)**

- **Context:** Creating new data in Service methods to save into the DB.
- **Rule:** Use the Builder Pattern (`@Builder`) directly.
- **Reason:** Entities represent the core state construction.
- **Example:**
  ```java
  // ✅ Good: Service Layer
  Bookmark bookmark = Bookmark.builder()
      .userNickname(userNickname)
      .sentence(sentence)
      .build();
  ```

**B. DTO Creation (Response Layer)**

- **Context:** Converting Entities to DTOs to return data to the Controller/Frontend.
- **Rule:** Do NOT expose the .builder() of a DTO directly in the Service layer. Instead, use Static Factory Methods inside the DTO (Record).
- **Naming:**
  - `from(Entity entity)`: Mandatory for mapping an Entity to a DTO.
  - `of(params...)`: For creating a DTO from individual arguments.
    -- **Implementation:** Use Java record and `@Builder` together.
- **Example:**
  ```java
  @Builder(access = AccessLevel.PRIVATE)
  public record MyDto(String name, int age) {
      public static MyDto from(MyEntity entity) {
          return MyDto.builder()
                  .name(entity.getName())
                  .age(entity.getAge())
                  .build();
      }
  }
  ```

### 8. Package Structure

- **Flat Architecture:** Keep domain packages flat (e.g., `domain/pdf` contains Controller, Service, Repository, DTOs).
- **Refactoring Rule:** Only split a domain package into subpackages (`dto`, `service`, `controller`, etc.) when the file count in that package exceeds 15.

### 9. API Versioning Strategy

- **URI Versioning:** Use URI versioning for all REST API endpoints (e.g., `/api/v1/cards`).
- **Evolution:** Start with v1 for the MVP. When breaking changes are introduced, create a new controller/endpoint with v2. Maintain v1 for backward compatibility.

### 10. Frontend Integration

- **Syncing:** When backend API endpoints change, IMMEDIATELY update the corresponding frontend API calls.
- **Search:** Grep for the old endpoint path in the frontend directory to find all occurrences.

### 11. Svelte 5 Refactoring Rules (Runes)

- **State ($state):** Convert `let var = val;` to `let var = $state(val);`.
- **Props ($props):** Replace `export let prop;` with `let { prop } = $props();`.
- **Derived ($derived):** Convert `$: double = count * 2;` to `let double = $derived(count * 2);`.
- **Effects ($effect):** Convert `$: { sideEffect(); }` to `$effect(() => { sideEffect(); });`.
- **Events:** Prefer callback props over `createEventDispatcher`.
- **Icons:** Use `@lucide/svelte` for icon imports. **Rule:** Always use individual sub-path imports (e.g., `import Menu from "@lucide/svelte/icons/menu"`) instead of barrel imports to avoid deprecation warnings and improve tree-shaking.
- **Cleanup:** Remove unused imports and ensure `lang="ts"`.

### 12. Frontend Logging & Environment Checks

- **Logging:** All `console.log`, `console.error`, and other debug logs MUST be wrapped in an environment check to prevent leaking information in production.
- **Environment Check:** Use `import.meta.env.DEV` (Vite standard) to check if the app is running in development mode.
- **Example:**
  ```javascript
  if (import.meta.env.DEV) {
    console.log("Debug info:", data);
  }
  ```

## 13. Documentation Strategy (API & Code)

Rule: Whenever you create or modify a Controller or DTO class, you MUST immediately apply the following documentation annotations.

- **Controllers (Endpoints):** MUST use `@Operation` to describe what the API does.
  -- **DTOs (Request/Response Bodies):** MUST use `@Schema` for fields to provide descriptions and examples.
- **Internal Logic:** No boilerplate Javadoc. Use inline comments (`//`) only for complex business logic.

## 14. Localization Rule

- **English Only:** All annotations, comments, and documentation MUST be written in English. This applies to all files (Java, Svelte, JS, etc.).

## 15. 📁 File Upload & Validation Rules

- **Size Limit:** The maximum allowed size is **20MB** (Total and individual files).
- **Validation Logic:** - If any file or the total size exceeds 20MB, prevent the "Generate All Decks" action.
- **UI/UX Feedback:**
  - **Global Error:** Show a **Red Toast notification** with a message like "Total file size exceeds the 20MB limit."
  - **Individual File Feedback:** For specific files exceeding the limit, highlight the file card with a **Red Border** and display a **Red Exclamation Icon (Tooltip)** explaining the error.
- **Implementation Note:** Use Svelte 5 Runes for state management and ensure smooth transitions for toast/tooltip visibility.

## 16. 📦 Dependency Management (pnpm-lock.yaml)

- **Rule: Review Lockfile After Changes.**
- Whenever you add, remove, or update frontend dependencies (e.g., using `pnpm add`), you **MUST** run `pnpm install` to ensure `pnpm-lock.yaml` is up to date and consistent with `package.json`.
- **Deployment Safety:** Before finishing a task that involves dependency changes, verify that the lockfile does not contain unexpected changes or conflicts that could break the deployment.

## 17. 🗄️ Database Migrations (Flyway)

- **Rule: Sync Entity Changes with Flyway.**
- Whenever you add, remove, or modify a field in a JPA Entity (e.g., adding a new `@Column`), you **MUST** immediately create a corresponding Flyway migration script (e.g., `V{N}__Description.sql`) in `src/main/resources/db/migration`.
- This ensures that the database schema remains in sync with the application's domain model across all environments.

## 18. 🔍 QueryDSL vs. Native Queries

- **Preference**: Always prefer **QueryDSL** over Native SQL queries for complex or dynamic queries.
- **Type Safety**: QueryDSL provides compile-time type checking and better refactoring support.
- **Readability**: Use QueryDSL's fluent API to build queries that are easier to read and maintain than raw string-based SQL.
- **Native Query Exception**: Only use Native Queries if a specific database feature is required that is not supported by QueryDSL or JPA, and only after consulting with the team.

## 19. ♿ Accessibility (a11y)

- **Interactive Elements**: Use semantic tags like `<button>` or `<a>` for clickable elements. Avoid adding `onclick` to non-interactive tags like `<div>`, `<span>`, or `<h3>`. If a non-interactive tag must be used, it MUST be accompanied by appropriate ARIA roles and keyboard event handlers.
- **Focus Management**: Avoid the `autofocus` attribute as it can disrupt screen readers and navigation flow. Use a custom Svelte action (e.g., `use:focus`) to manage focus programmatically when elements appear or state changes.
- **Keyboard Support**: Ensure all interactive elements are reachable and operable via keyboard. Standard `<button>` and `<a>` tags provide this by default.
- **Alt Text**: All images and icons must have descriptive `alt` text or be marked as decorative (e.g., `aria-hidden="true"`) if they don't convey unique information.

## 20. API Request Headers (415 Error Prevention)

- **Content-Type**: When making `POST`, `PUT`, or `PATCH` requests using `fetch` or `authFetch`, you **MUST** explicitly set the `Content-Type` header to `application/json` if the body is a JSON string.
- **Example**:
  ```javascript
  await authFetch("/api/v1/resource", {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  ```

## 21. 📊 Vercel Insights & Analytics

- **Rule: Wrap Injection in Environment Checks.**
- Always wrap `injectSpeedInsights()` and `injectAnalytics()` in a check for `browser && !dev` from `$app/environment`.
- This prevents 404 errors and console noise in local development and non-production environments where these services might not be enabled.
- **Example:**
  ```javascript
  if (browser && !dev) {
    injectSpeedInsights();
    injectAnalytics();
  }
  ```

## 22. 📦 Import Verification Rule

- **Rule: Verify Imports After Every Edit.**
- Whenever you add a new class reference, service, or library to a file (Frontend or Backend), you **MUST** immediately verify and add the corresponding import statement.
- **Backend (Java):** Ensure no wildcard imports are used and every new class is explicitly imported.
- **Frontend (Svelte/TS):** Ensure all components, icons, and utilities are correctly imported and unused imports are removed.
- **Verification:** Before finishing a task, run a build or check (e.g., `pnpm run check` or `./gradlew compileJava`) to ensure no "cannot find symbol" or "missing import" errors exist.

## 23. 🛡️ XSS Prevention Rule

- **Rule: Avoid `{@html}` with User-Controlled Input.**
- To prevent Cross-Site Scripting (XSS) attacks, **NEVER** use Svelte's `{@html}` tag to render strings that contain or are derived from user input (e.g., selected text, form inputs, URL parameters).
- **Safe Alternative for Line Breaks:** Use standard Svelte interpolation `{}` combined with the CSS property `white-space: pre-wrap;` and newline characters (`\n`) in your strings. This allows Svelte to automatically escape dangerous HTML while still rendering line breaks correctly.
- **Exception:** Only use `{@html}` for trusted, hardcoded content or content that has been explicitly sanitized using a robust library like DOMPurify.

## 24. 🎨 Cubrain Aesthetic & UI Adaptation (Uiverse, etc.)

When adapting external UI code (e.g., from Uiverse) into Cubrain:

1.  **Color Mapping:** Strictly map primary colors to Cubrain Gold (`#fbbf24` / `text-amber-400`).
2.  **Dark Mode First:** Optimize all backgrounds and contrasts for a dark theme (Black/Zinc-900).
3.  **Tailwind Conversion:** Prefer Tailwind CSS classes over raw `<style>` tags. Use arbitrary values (e.g., `hover:shadow-[0_0_20px_#fbbf24]`) for complex effects.
4.  **Consistency:** Match existing design tokens:
    - **Border Radius:** Use `rounded-lg` as the default.
    - **Typography:** Match the project's font settings.
    - **Transitions:** Use smooth, consistent transitions.
