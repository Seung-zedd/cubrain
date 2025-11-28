# Copilot Instructions for Cubrain

This document provides instructions for GitHub Copilot when working in this repository.

## Project Overview

Cubrain is a full-stack monorepo consisting of:
- **Backend**: Spring Boot 3.x application with Java 21, JPA, and PostgreSQL
- **Frontend**: SvelteKit application with TypeScript and Vite

## Project Structure

```
cubrain/
├── .github/           # GitHub configuration and Copilot instructions
├── .vscode/           # VS Code settings
├── backend/           # Spring Boot backend
│   ├── src/
│   │   ├── main/java/com/cubrain/
│   │   └── test/
│   ├── build.gradle   # Backend Gradle configuration
│   └── gradlew        # Gradle wrapper
├── frontend/          # SvelteKit frontend
│   ├── src/
│   │   ├── lib/
│   │   └── routes/
│   └── package.json   # Frontend dependencies (uses pnpm)
├── gradle/            # Gradle wrapper files
├── build.gradle       # Root Gradle configuration
├── settings.gradle    # Gradle settings
├── gradlew            # Gradle wrapper script (Unix)
├── gradlew.bat        # Gradle wrapper script (Windows)
├── docker-compose.yml # Docker configuration
└── README.md
```

## Tech Stack

### Backend
- Spring Boot 3.x
- Java 21
- JPA (Hibernate)
- PostgreSQL
- Flyway (database migrations)
- Lombok
- LangChain4j with Google Gemini
- SpringDoc OpenAPI for API documentation

### Frontend
- SvelteKit
- TypeScript
- Vite
- pnpm (package manager)

> **Note**: The frontend uses pnpm as the package manager. While the root `build.gradle` contains npm-based tasks for Windows compatibility, use pnpm commands directly when working on the frontend.

## Build and Development Commands

### Backend
```bash
cd backend
./gradlew build       # Build the project
./gradlew test        # Run tests
./gradlew bootRun     # Run the application
```

### Frontend
```bash
cd frontend
pnpm install          # Install dependencies
pnpm run dev          # Run development server
pnpm run build        # Build for production
pnpm run check        # Type checking with svelte-check
```

### Root Level (Gradle tasks)
```bash
./gradlew buildAll    # Build all subprojects
./gradlew cleanAll    # Clean all subprojects
```

## Coding Conventions

### Commit Message Convention

This project uses Gitmoji combined with Conventional Commits:

**Format**: `[Gitmoji] [type]([scope]): [description]`

| Gitmoji | Type | Description |
|---------|------|-------------|
| ✨ `:sparkles:` | feat | New feature |
| 🐛 `:bug:` | fix | Bug fix |
| 📚 `:books:` | docs | Documentation |
| 🎨 `:art:` | style | Code formatting (no logic changes) |
| ♻️ `:recycle:` | refactor | Code refactoring |
| 🚀 `:rocket:` | deploy | Build/deployment |
| ⚙️ `:gear:` | chore | Configuration changes |
| ✅ `:white_check_mark:` | test | Tests |
| 🚑 `:ambulance:` | hotfix | Critical hotfix |

**Examples**:
- `✨ feat(auth): Add social login feature`
- `🐛 fix(api): Fix NPE when querying guest confirmations`
- `♻️ refactor: Extract duplicate code in AuthService to method`

## Git Workflow

This project uses a Feature Branch Strategy:

1. **main**: Production-ready stable code
2. **dev**: Integration branch for testing
3. **feature/***: Feature development branches

### Workflow:
1. Create feature branches from `dev`
2. Submit PRs to `dev` for code review (CodeRabbit)
3. After approval, merge to `dev`
4. When ready for release, merge `dev` to `main`

## API Documentation

API documentation is generated using SpringDoc OpenAPI.

- Access at: `http://localhost:8080/v3/api-docs`
- Import to Apidog for better API management

## Code Style Guidelines

### Java (Backend)
- Use Lombok annotations to reduce boilerplate
- Follow standard Spring Boot project structure (controller, service, repository layers)
- Use JPA entities with proper annotations
- Validate input using `@Valid` and validation annotations

### TypeScript/Svelte (Frontend)
- Use TypeScript for type safety
- Follow SvelteKit conventions for routing and components
- Use proper TypeScript types and interfaces

## Testing

### Backend
- Use JUnit 5 with Spring Boot Test
- Test service layer with unit tests
- Exclude p6spy from test dependencies

### Frontend
- Use svelte-check for type checking
- Run `pnpm run check` before committing
