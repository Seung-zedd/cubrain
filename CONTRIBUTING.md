# 🤝 Contributing to Cubrain

Welcome to the Cubrain project! We're excited to have you here. To ensure code quality, stability, and a smooth development experience, we follow a structured **Feature Branch Workflow** and specific coding conventions.

Please read this document carefully before you start contributing.

---

## 🌳 Branching Strategy

We use a structured branching model to separate development from production.

| Branch          | Role            | Description                                                                                                                                             |
| :-------------- | :-------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`main`**      | **Production**  | 🛡️ **The Sanctuary.** Contains only stable, deployable code. Direct pushes are forbidden. Merges happen only via Pull Requests from `dev` for releases. |
| **`dev`**       | **Staging**     | 🔧 **Integration Hub.** The active development branch. All feature branches merge here for integration testing.                                         |
| **`feature/*`** | **Development** | ✨ **Workspace.** Dedicated branches for specific features or fixes (e.g., `feature/login-auth`).                                                       |

---

## 🚀 Workflow Guide

### 1. Start a New Feature

Always start your work from the latest `dev` branch to avoid conflicts.

```bash
# Sync local dev with remote
git checkout dev
git pull origin dev

# Create a feature branch
git checkout -b feature/your-feature-name
```

### 2. Develop & Commit

Make small, atomic commits with clear messages.

- **Convention**: This project uses **Gitmoji** combined with **Conventional Commits**.
- **Format**: `[Gitmoji] [Type]: [Title]`

| Gitmoji                 | Type       | Description           |
| :---------------------- | :--------- | :-------------------- |
| ✨ `:sparkles:`         | `feat`     | New features          |
| 🐛 `:bug:`              | `fix`      | Bug fixes             |
| 📚 `:books:`            | `docs`     | Documentation changes |
| 💄 `:lipstick:`         | `ui`       | UI changes            |
| ♻️ `:recycle:`          | `refactor` | Code refactoring      |
| 🚀 `:rocket:`           | `deploy`   | Deployment tasks      |
| ⚙️ `:gear:`             | `chore`    | Build/config changes  |
| ✅ `:white_check_mark:` | `test`     | Adding/fixing tests   |
| 🚑 `:ambulance:`        | `hotfix`   | Critical hotfixes     |

**Rule**: After completing a significant task or a series of related changes, **ALWAYS** provide a **single-line** git commit message in the format above. Focus on the most significant change.

**Example**: `✨ feat: implement basic login logic`

```bash
git add .
git commit -m "✨ feat: implement basic login logic"
git push -u origin feature/your-feature-name
```

### 3. Pull Request (PR) & AI Code Review

⚠️ **IMPORTANT**: We utilize automated code reviews to maintain high standards.

1. **Create PR**: Open a Pull Request on GitHub targeting the `dev` branch (NOT `main`).
2. **Trigger AI Review**:
   - Use your AI agent (Cursor, Windsurf, etc.) to review the changes.
   - **Prompt Example**: _"Please review my PR on branch feature/your-feature-name. Check for potential bugs, security issues, and architectural consistency."_
3. **Refine**: Apply the feedback, push changes, and repeat until the code is polished.

### 4. Merge & Cleanup

Once the PR is approved and CI passes:

1. **Squash and Merge**: Use the "Squash and Merge" option on GitHub to keep a clean history on `dev`.
2. **Sync & Delete**:
   ```bash
   git checkout dev
   git pull origin dev
   git branch -d feature/your-feature-name
   ```

### 5. Release (Main Branch)

Deployment to production (`main`) is handled during release cycles.

```bash
# Merge dev to main
git checkout main
git pull origin main
git merge dev
git push origin main

# Tagging (Triggers Deployment)
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

---

## 🛠️ Development Commands

### Backend (Spring Boot)

- **Run**: `./gradlew bootRun`
- **Test**: `./gradlew test`
- **Build**: `./gradlew build`

### Frontend (SvelteKit)

- **Run**: `pnpm run dev`
- **Check**: `pnpm run check`
- **Lint**: `pnpm run lint`

---

## 🎨 Coding Standards

To maintain a high-quality codebase, we adhere to the following principles. For a full breakdown, see [AGENTS.md](./AGENTS.md).

### 1. General Principles

- **SOLID**: We strictly follow SOLID principles. No "God Classes" (keep services under 200 lines).
- **English Only**: All code comments, documentation, and annotations must be in **English**.
- **Documentation**: Use `@Operation` (Controllers) and `@Schema` (DTOs) for all API-related classes.

### 2. Backend (Spring Boot)

- **DTOs**: Never return entities directly. Use `record` DTOs with static factory methods (`from`, `of`).
- **Imports**: No wildcard imports. Use static imports for constants and enums.
- **Transactional**: Avoid `@Transactional` on methods with external API calls (AI, S3).

### 3. Frontend (Svelte 5)

- **Runes**: Use Svelte 5 Runes (`$state`, `$props`, `$derived`, `$effect`) exclusively.
- **Logging**: Wrap all `console.log` in `if (import.meta.env.DEV)` checks.
- **Icons**: Use `@lucide/svelte`.

---

## 💡 General Guidelines

- **Stay Focused**: One feature/fix per branch.
- **Documentation**: Update `README.md` or relevant docs if your changes introduce new features.
- **Communication**: If you're unsure about something, open an issue or ask in the project's communication channel.

Thank you for contributing to Cubrain! 🚀
