# AGENT.md

This file provides guidance to the AI Agent when working with code in this repository.

## Communication Style

- **Concise Mode**: Answer concisely. Avoid verbose explanations unless specifically asked. Get straight to the point to prevent the user from getting lost.

## Project Overview

**Cubrain** is a full-stack monorepo application consisting of:

- **Frontend** (`frontend/`): Vanilla JavaScript/Vite text highlighting and context capture prototype
- **Backend** (`backend/`): Spring Boot REST API with Kakao OAuth2 authentication and JWT token management

The application allows users to highlight text on a webpage, capture surrounding context, and save it to a "deck" - similar to browser-based annotation or learning tools. Authentication is handled via Kakao OAuth2 with custom JWT tokens for session management.

## Development Commands

### Frontend (`frontend/`)

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Backend (`backend/`)

```bash
# Run Spring Boot application
./gradlew bootRun

# Run tests
./gradlew test

# Build JAR
./gradlew build

# Clean build artifacts
./gradlew clean
```

## Architecture

### Frontend Architecture

**Single-page vanilla JavaScript application** - The entire application logic resides in `frontend/index.html`:

1. **Text Selection Detection** (`mouseup` listener) - Monitors user text highlighting
2. **Button Positioning** - Displays floating "Save to Deck" button above selected text
3. **Context Capture** - Extracts both the highlighted text and its surrounding paragraph/context

**Key Implementation Details:**

- **Text Selection & Positioning (index.html:74-90)**: Uses `window.getSelection()` and `Range.getBoundingClientRect()` to detect highlighted text. Button positioning accounts for scroll offset via `window.scrollY`.
- **Context Capture Logic (index.html:104-110)**: Finds the `commonAncestorContainer` of the selected range and extracts `innerText` from the parent node to preserve full context.
- **State Management**: No formal state management - uses DOM selection state directly.

### Backend Architecture

**Spring Boot 3.5.5 with Java 21** - RESTful API with OAuth2 + JWT authentication:

#### Project Structure

```
backend/src/main/java/com/example/springboot_starter_auth/
├── global/
│   ├── auth/                    # Authentication & authorization
│   │   ├── controller/          # Auth endpoints (Kakao OAuth2 callback, login URL)
│   │   ├── service/             # AuthService - Kakao token exchange & user registration
│   │   ├── handler/             # OAuth2SuccessHandler, OAuth2FailureHandler
│   │   ├── jwt/                 # JwtTokenProvider, JwtAuthenticationFilter
│   │   ├── user/                # User entity, repository, service, controller
│   │   └── dto/                 # Kakao API response DTOs
│   ├── config/
│   │   ├── security/            # SecurityConfig - CORS, auth rules, JWT filter chain
│   │   ├── web/                 # WebClientConfig - HTTP client for Kakao API
│   │   └── audit/               # JPA audit config, BaseEntity, BaseTimeEntity
│   ├── logging/                 # P6Spy SQL formatter, naming strategy
│   ├── exception/               # GlobalExceptionHandler
│   └── util/                    # SecurityUtils, UuidUtil, EnvironmentUtil
└── SpringbootStarterAuthApplication.java
```

#### Authentication Flow

1. **Kakao OAuth2 Login** (`AuthController.java`):

   - GET `/auth/kakao/login-url` - Returns Kakao authorization URL
   - GET `/auth/kakao/callback` - Receives authorization code from Kakao

2. **Token Exchange** (`AuthService.java`):

   - Exchanges authorization code for Kakao access token
   - Fetches user info from Kakao API
   - Registers new user or retrieves existing user
   - Generates custom JWT access/refresh tokens

3. **JWT Validation** (`JwtAuthenticationFilter.java`):
   - Intercepts requests with `Authorization: Bearer <token>` header
   - Validates JWT and sets Spring Security authentication context
   - Positioned before `UsernamePasswordAuthenticationFilter`

#### Security Configuration

- **Environment-based CORS** (`SecurityConfig.java:122-159`):

  - `local`/`dev`: Allows `http://localhost:*` via origin patterns
  - `prod`: HTTPS-only with explicit domain whitelist

- **Public Endpoints**:

  - `/auth/kakao/**` - OAuth2 callback and login URL
  - `/` and `/main.html` - Landing page
  - `/test/auth/**` - Test endpoints (local profile only)
  - `/v3/api-docs`, `/swagger-ui/**` - OpenAPI docs (local profile only)

- **Protected Endpoints**:
  - `/home.html` and `/views/**` - Require authentication
  - All other endpoints require authentication by default

#### Database & Persistence

- **PostgreSQL** (production) with **H2** (testing)
- **Flyway** for schema migrations (currently disabled)
- **JPA Auditing**: `BaseEntity` and `BaseTimeEntity` for automatic `createdAt`/`updatedAt` tracking
- **P6Spy** for SQL logging with custom formatter (`P6SpySqlFormatter.java`)
- **Naming Strategy**: `CamelCaseToSnakeAndUpperCaseStrategy` converts Java camelCase to UPPER_SNAKE_CASE in database

#### Key Dependencies

- Spring Boot 3.5.5 (Java 21)
- Spring Security OAuth2 Client & Resource Server
- Spring WebFlux (WebClient for Kakao API calls)
- JJWT 0.12.6 (JWT generation/validation)
- SpringDoc OpenAPI 2.8.9 (API documentation)
- Lombok (code generation)
- P6Spy 1.9.0 (SQL logging)

#### Multi-Environment Configuration

Profiles: `local`, `dev`, `prod` (grouped with `common` in `application.yml`)

- `application-common.yml` - Shared config (Kakao OAuth2 settings, JPA, Flyway)
- `application-local.yml` - Local development settings
- `application-dev.yml` - Development environment
- `application-prod.yml` - Production environment

Default profile: `dev`

## Context7 MCP Integration

**IMPORTANT**: Before implementing features with external libraries, ALWAYS use Context7 to fetch the latest documentation.

### Workflow

1. **Resolve library ID** using `mcp__context7__resolve-library-id`:

   ```
   mcp__context7__resolve-library-id("library-name")
   ```

   Example: `mcp__context7__resolve-library-id("spring-boot")`

2. **Fetch latest documentation** using `mcp__context7__get-library-docs`:

   ```
   mcp__context7__get-library-docs("/org/project", topic: "specific-topic")
   ```

   - Use the library ID from step 1
   - Specify relevant topic (e.g., "security", "oauth2", "jpa")
   - Adjust token limits based on complexity (default: 10000)

3. **Implement using current best practices** from the fetched documentation:
   - Use the latest APIs and patterns
   - Follow library's current best practices
   - Avoid deprecated methods

### Example

```
User: "Add Google OAuth2 login support"

1. mcp__context7__resolve-library-id("spring-security-oauth2")
2. mcp__context7__get-library-docs("/spring-projects/spring-security", topic: "oauth2-login")
3. Review latest OAuth2 client configuration patterns
4. Implement Google provider alongside existing Kakao provider
```

**Benefits**: Ensures code stays up-to-date with latest library versions, reduces technical debt, and avoids compatibility issues.

## Testing

### Backend Testing

```bash
# Run all tests
cd backend && ./gradlew test

# Run specific test class
./gradlew test --tests "ClassName"

# Run with coverage
./gradlew test jacocoTestReport
```

Test configuration uses H2 in-memory database (see `application-test.yml`).

## API Documentation

When running locally (`local` profile):

- OpenAPI JSON schema: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

**Apidog Integration**: Copy JSON from `/v3/api-docs` and import into Apidog for API documentation.
