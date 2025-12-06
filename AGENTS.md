# Agent Behavioral Rules

## Documentation Lookup

When I need code generation, setup or configuration steps, or library/API documentation:

1. **Primary:** Use Context7 MCP tools to resolve library id and get library docs
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
- 🎨 `:art:` `style`: Code style changes (formatting, etc.)
- ♻️ `:recycle:` `refactor`: Code refactoring
- 🚀 `:rocket:` `deploy`: Deployment tasks
- ⚙️ `:gear:` `chore`: Build/config changes
- ✅ `:white_check_mark:` `test`: Adding/fixing tests
- 🚑 `:ambulance:` `hotfix`: Critical hotfixes

**Note:** Scope is optional and can be omitted as the branch name usually contains the issue number.

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

### 3. Package Structure
- **Flat Architecture:** Keep domain packages flat (e.g., `domain/pdf` contains Controller, Service, Repository, DTOs).
- **Refactoring Rule:** Only split a domain package into subpackages (`dto`, `service`, `controller`, etc.) when the file count in that package exceeds 15.
