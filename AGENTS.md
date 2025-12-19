# Agent Behavioral Rules

♻️ _This markdown file can be re-used anytime you wanna build a new app_

## 1. Documentation Lookup

When I need code generation, setup or configuration steps, or library/API documentation:

1. **Primary:** Always use context7 when I need code generation, setup or configuration steps, or library/API documentation. This means you should automatically use the Context7 MCP tools to resolve library id and get library docs without me having to explicitly ask.
2. **Fallback:** If Context7 is unavailable, use web search to find the official documentation from:
   - GitHub repositories (README, docs folder)
   - Official documentation sites (e.g., docs.langchain4j.dev)
   - Maven Central / package registry pages

Always prioritize official sources over blog posts or Stack Overflow answers.

## 2. Commit Message Convention

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

## 3. Coding Standards & Design Principles

### 3.1 SOLID Principles (Strict Enforcement)

- **SRP (Single Responsibility Principle):**
  - Each class must have **one and only one reason to change**.
  - Do NOT create "God Classes". If a Service class exceeds 200 lines or handles mixed concerns, split it.
- **OCP (Open/Closed Principle):**
  - Design for extension. Use **Interfaces** for components that might change implementations.
  - Do not use `if-else` blocks for switching logic; use Strategy Pattern or Polymorphism.
- **DIP (Dependency Inversion):**
  - Always depend on abstractions (Interfaces), not concretions.
  - Inject dependencies via Constructor Injection (`@RequiredArgsConstructor`).

### 3.2 Spring Boot Best Practices

- **DTOs:** Never return `@Entity` objects in Controllers. Always map them to `Record` DTOs.
- **Naming Convention:** All Data Transfer Objects must end with the suffix `Dto` (e.g., `CardRequestDto`).
- **Transactional:** Do NOT apply `@Transactional` on methods involving external API calls (AI, S3, etc.) to prevent connection pool starvation. Apply it only to the DB access layer.
- **Import Style:**
  - **Class Imports:** NO Wildcards. Explicitly import each class.
  - **Static Imports:** Aggressively use `import static` for constants, enums, and utility methods (e.g., `HttpStatus.OK`) to improve readability.
- **Annotation Style:**
  - **Implicit Names:** Omit the name parameter if the variable name matches (e.g., use `@RequestParam String name` instead of `@RequestParam("name") String name`).

### 3.3 Object Creation & Mapping Strategy (Entity vs. DTO)

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
- **Implementation:** Use Java record and `@Builder` together.
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

### 3.4 Package Structure

- **Flat Architecture:** Keep domain packages flat (e.g., `domain/pdf` contains Controller, Service, Repository, DTOs).
- **Refactoring Rule:** Only split a domain package into subpackages (`dto`, `service`, `controller`, etc.) when the file count in that package exceeds 15.

### 3.5 API Versioning Strategy

- **URI Versioning:** Use URI versioning for all REST API endpoints (e.g., `/api/v1/cards`).
- **Evolution:** Start with v1 for the MVP. When breaking changes are introduced, create a new controller/endpoint with v2. Maintain v1 for backward compatibility.

### 3.6 Frontend Integration

- **Syncing:** When backend API endpoints change, IMMEDIATELY update the corresponding frontend API calls.
- **Search:** Grep for the old endpoint path in the frontend directory to find all occurrences.

### 3.7 Svelte 5 Refactoring Rules (Runes)

- **State ($state):** Convert `let var = val;` to `let var = $state(val);`.
- **Props ($props):** Replace `export let prop;` with `let { prop } = $props();`.
- **Derived ($derived):** Convert `$: double = count * 2;` to `let double = $derived(count * 2);`.
- **Effects ($effect):** Convert `$: { sideEffect(); }` to `$effect(() => { sideEffect(); });`.
- **Events:** Prefer callback props over `createEventDispatcher`.
- **Icons:** Use `@lucide/svelte` for icon imports.
- **Cleanup:** Remove unused imports and ensure `lang="ts"`.

### 3.8 Frontend Logging & Environment Checks

- **Logging:** All `console.log`, `console.error`, and other debug logs MUST be wrapped in an environment check to prevent leaking information in production.
- **Environment Check:** Use `import.meta.env.DEV` (Vite standard) to check if the app is running in development mode.
- **Example:**
  ```javascript
  if (import.meta.env.DEV) {
    console.log("Debug info:", data);
  }
  ```

## 4. Documentation Strategy (API & Code)

Rule: Whenever you create or modify a Controller or DTO class, you MUST immediately apply the following documentation annotations.

- **Controllers (Endpoints):** MUST use `@Operation` to describe what the API does.
- **DTOs (Request/Response Bodies):** MUST use `@Schema` for fields to provide descriptions and examples.
- **Internal Logic:** No boilerplate Javadoc. Use inline comments (`//`) only for complex business logic.

## 5. Localization Rule

- **English Only:** All annotations, comments, and documentation MUST be written in English. This applies to all files (Java, Svelte, JS, etc.).
