# AGENTS.md — Notiva Backend Maintenance Guide

## Interaction Rules

- The user may ask questions in English, but always respond in Myanmar language.

- Never add, update, remove, rename, move, fix, or modify any code, file, folder, configuration, dependency, or project structure without explicit permission.

- Do not make any project changes unless the user explicitly says the exact phrase:

  "Build Now"

- Before the phrase "Build Now" is given, only:
  - discuss
  - explain
  - review
  - plan
  - suggest
  - provide commands or code snippets without applying them

- When the user says "Build Now", you may make only the changes that were discussed or explicitly requested.

- Do not interpret similar phrases such as "go ahead", "continue", "start", "do it", or "proceed" as permission to modify the project.

- If "Build Now" has not been explicitly provided, do not modify the project.


## 1. Purpose

This file defines how AI coding agents (especially Codex in VS Code) should work on the **Notiva Spring Boot backend**.

The goal is to maintain and improve the existing project safely rather than rewriting it blindly.

Notiva is a note-taking application with authentication, user-owned notes, AI-assisted features, and planned minimal admin capabilities. The backend should be suitable for a junior-developer portfolio while following professional Spring Boot practices.

Primary technologies:

- Java
- Spring Boot
- Spring Security
- JWT authentication
- PostgreSQL
- JPA / Hibernate
- REST APIs
- Groq API integration for AI features

Frontend applications may include Next.js and React Native, but **this AGENTS.md applies only to the Spring Boot backend project**.

---

## 2. Core Agent Principles

When modifying this project, follow these principles in order:

1. Understand the existing implementation before changing it.
2. Preserve working behavior unless a change is explicitly required.
3. Prefer small, reviewable, incremental changes over large rewrites.
4. Fix root causes instead of hiding symptoms.
5. Keep the architecture understandable for a junior developer.
6. Use production-style practices without unnecessary enterprise complexity.
7. Protect user privacy and authentication data.
8. Avoid introducing dependencies unless they provide clear value.
9. Keep APIs predictable and consistent.
10. After significant changes, verify compilation and tests.

Do not make unrelated changes while solving a specific task.

---

## 3. Maintenance Priorities

The current maintenance priorities are:

### Priority 1 — Stabilize the existing backend

- Make sure the Spring Boot application starts reliably in standard environments such as VS Code and command-line Maven/Gradle.
- Remove environment-specific assumptions from IDE configuration.
- Fix broken imports, configuration problems, startup failures, circular dependencies, and invalid bean definitions.
- Keep secrets outside source code.

### Priority 2 — Improve package and folder structure

Refactor unclear or inconsistent packages into a feature-oriented structure where practical.

Preferred high-level structure:

```text
src/main/java/<base-package>/
├── auth/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── model/            # only when auth-specific domain objects exist
├── user/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── model/
├── note/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── model/
├── admin/
│   ├── controller/
│   ├── service/
│   └── dto/
├── ai/
│   ├── controller/
│   ├── service/
│   ├── client/
│   └── dto/
├── security/
├── config/
└── common/
    ├── exception/
    ├── response/
    ├── validation/
    └── util/
```

Do not mechanically move every class just to match this example. Adapt the structure to the project while keeping responsibilities clear.

### Priority 3 — Authentication and refresh tokens

Implement a proper refresh-token flow if it does not already exist.

Requirements:

- Short-lived access token.
- Longer-lived refresh token.
- Refresh endpoint that issues a new access token only after validating the refresh token.
- Logout/revocation behavior must be clearly defined.
- Do not store plaintext secrets unnecessarily.
- Do not put sensitive information inside JWT claims.
- Validate token type so a refresh token cannot be used as an access token.
- Return appropriate HTTP status codes for expired, malformed, invalid, or revoked tokens.
- Avoid permanent refresh tokens.

If persistent refresh tokens are used, model them explicitly and support revocation/expiration.

Do not create insecure token handling merely to simplify implementation.

### Priority 4 — Fix Groq API integration

Investigate the actual cause of Groq connection failures before changing implementation.

Check, as applicable:

- API base URL
- endpoint path
- API key configuration
- request headers
- authorization format
- request JSON shape
- response JSON mapping
- selected model name
- HTTP client configuration
- connection/read timeouts
- malformed environment variables
- SSL/network errors
- rate limits and API errors

Requirements:

- Keep Groq API keys in environment variables or external configuration.
- Never hardcode API keys.
- Do not log API keys or full authorization headers.
- Create a dedicated AI/Groq client or service boundary instead of scattering HTTP calls across controllers.
- Handle upstream failures gracefully.
- Convert provider-specific failures into safe application-level errors.
- Log enough technical context for debugging without leaking private note content.
- Consider reasonable connection/read timeouts.

### Priority 5 — Minimal admin API

Implement only a **minimal administration layer** appropriate for Notiva.

Useful admin capabilities may include:

- total user count
- total note count
- basic aggregate usage statistics
- list/search users with pagination
- user account status
- enable/disable or moderate user accounts if the existing product requirements support it
- basic operational health/statistics if useful

Privacy rule:

> Administrators must NOT receive unrestricted access to private user note content by default.

Admin APIs may expose metadata and aggregate information where necessary, but private note bodies/content should remain inaccessible unless a future explicit product requirement defines a legitimate, auditable access mechanism.

All admin endpoints must require an appropriate admin role/authority.

### Priority 6 — Remove unnecessary code

Identify and remove only code that is demonstrably unused, duplicated, obsolete, unreachable, or replaced.

Before deleting a class or method:

1. Search for references.
2. Check reflection/framework usage where relevant.
3. Check configuration references.
4. Check serialization/JPA requirements.
5. Confirm no current API depends on it.

Do not delete code merely because its purpose is not immediately obvious.

### Priority 7 — Improve overall backend quality

Improve, where needed:

- validation
- exception handling
- HTTP status codes
- API response consistency
- DTO/entity separation
- transaction boundaries
- security configuration
- logging
- pagination
- database constraints
- naming
- documentation
- tests
- configuration management

---

## 4. Architecture Rules

### Controllers

Controllers should:

- receive HTTP requests
- validate request DTOs
- call services
- return appropriate HTTP responses

Controllers should NOT:

- contain complex business logic
- directly access repositories unless there is an exceptional, justified reason
- construct database queries
- perform complex entity transformations repeatedly
- call external AI providers directly

Keep controllers thin.

### Services

Services should contain application/business logic.

Prefer:

- clear method names
- focused responsibilities
- constructor injection
- explicit transaction boundaries where needed

Avoid "god services" with unrelated responsibilities.

### Repositories

Repositories should focus on persistence and database queries.

Use Spring Data repository conventions when sufficient.

Do not place business rules inside repositories.

### DTOs

Use DTOs for API boundaries instead of exposing JPA entities directly.

Prefer separate request and response DTOs when they have different responsibilities.

Examples:

```text
CreateNoteRequest
UpdateNoteRequest
NoteResponse
LoginRequest
LoginResponse
RefreshTokenRequest
UserSummaryResponse
AdminDashboardResponse
```

Avoid excessive DTO duplication when two structures are genuinely identical and stable.

### Entities

Entities represent persistence/domain state and should not be treated as public API schemas.

Be careful with:

- bidirectional relationships
- cascade rules
- orphan removal
- lazy loading
- equals/hashCode
- JSON serialization

Do not solve recursion problems by randomly changing FetchType to EAGER.

### Mappers

For a project of this size, simple explicit mapper methods/classes are preferred over introducing a heavy mapping library unless there is already one in use.

---

## 5. Security Rules

Security changes require extra care.

### Authentication

- Passwords must be hashed with an appropriate password encoder.
- Never store or log raw passwords.
- Never return password hashes in API responses.
- Authentication errors should not reveal unnecessary account details.

### Authorization

Every user-owned resource must enforce ownership on the server side.

For notes, never trust a user ID supplied by the client to establish ownership.

Prefer the authenticated principal/security context as the source of the current user's identity.

For example, a normal user must not be able to change a URL/body user ID and read, update, or delete another user's note.

### Admin

Admin authorization must be enforced server-side using Spring Security authorities/roles.

Do not rely only on frontend route protection.

### Secrets

Never commit:

- database passwords
- JWT signing secrets
- Groq API keys
- private tokens
- production credentials

Use environment variables and configuration placeholders.

A safe `application.properties` / `application.yml` may reference variables such as:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
groq.api-key=${GROQ_API_KEY}
```

Do not overwrite working configuration conventions unnecessarily.

---

## 6. Note Privacy Rules

Notes are private user data.

Unless explicitly required by the product specification:

- A user can access only their own notes.
- Admins should not see private note body/content.
- Logs should not contain full note contents.
- Exceptions should not expose note content.
- AI requests should send only the data necessary for the requested feature.

When working on search, AI, analytics, or admin features, preserve these privacy boundaries.

---

## 7. API Design Guidelines

Keep REST endpoints consistent.

Prefer resource-oriented routes such as:

```text
/api/auth/login
/api/auth/register
/api/auth/refresh
/api/auth/logout
/api/users/me
/api/notes
/api/notes/{id}
/api/ai/...
/api/admin/dashboard
/api/admin/users
```

Do not rename established public endpoints unless necessary. If renaming is required, clearly identify the breaking change.

Use HTTP methods conventionally:

- GET — retrieve
- POST — create/action
- PUT/PATCH — update
- DELETE — delete

Use appropriate response statuses, for example:

- 200 OK
- 201 Created
- 204 No Content
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 422 Unprocessable Content only when semantically appropriate
- 500 Internal Server Error
- 502/503 for relevant upstream service failures when appropriate

Do not return `200 OK` for every failure.

---

## 8. Exception Handling

Prefer centralized exception handling using `@RestControllerAdvice`.

Create specific application exceptions where useful, for example:

```text
ResourceNotFoundException
UnauthorizedException
ForbiddenException
ConflictException
InvalidTokenException
ExternalAiServiceException
```

Avoid exposing:

- stack traces
- SQL errors
- internal class names
- secrets
- provider credentials

to API clients.

A consistent error structure is preferred, for example:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Note not found",
  "timestamp": "..."
}
```

Use the project's established response model when one already exists and is reasonable.

---

## 9. Validation

Use Jakarta Bean Validation for request DTOs where appropriate.

Examples:

- `@NotBlank`
- `@Email`
- `@Size`
- `@NotNull`

Validate at API boundaries.

Do not rely solely on frontend validation.

Add database constraints for invariants that must remain true even outside the REST layer.

---

## 10. Database and JPA Guidelines

Preserve existing data unless a migration explicitly requires changes.

When modifying the schema:

- assess compatibility with existing data
- prefer migrations when the project uses Flyway/Liquibase
- do not silently use destructive schema generation in production-oriented configuration
- add indexes only when justified by actual query patterns
- enforce useful unique constraints at database level

Avoid N+1 query problems where they are evident.

Use pagination for endpoints that may return large collections, especially admin user lists and note lists.

---

## 11. Transactions

Use `@Transactional` deliberately.

Good candidates include service operations that perform multiple related persistence changes.

Use read-only transactions when useful for query-heavy service operations.

Do not annotate everything with `@Transactional` without understanding why.

---

## 12. Logging

Use structured, meaningful logging.

Good examples include:

- application startup failure context
- authentication failures without sensitive credentials
- failed external AI calls with safe status/error metadata
- unexpected exceptions

Avoid logging:

- passwords
- JWTs
- refresh tokens
- authorization headers
- Groq API keys
- full private note contents

Do not leave large amounts of temporary debug logging after completing a fix.

---

## 13. Groq / AI Integration Design

Keep external AI-provider logic behind a clear boundary.

Preferred flow:

```text
Controller
    -> AI Service
        -> Groq Client
            -> Groq API
```

The rest of the application should not depend heavily on Groq-specific response structures.

Prefer internal application DTOs and translate provider-specific responses at the client boundary.

When an AI request fails:

- do not crash unrelated backend functionality
- provide a meaningful safe error
- log provider status/error information safely
- handle timeout/network failures
- avoid exposing raw provider error payloads if they contain sensitive/internal details

---

## 14. Refresh Token Design Guidance

Before implementing refresh tokens, inspect the current JWT implementation.

Do not create a parallel authentication system if the existing one can be extended cleanly.

A typical flow is:

```text
Login
  -> access token + refresh token

Authenticated API request
  -> access token

Access token expired
  -> POST /api/auth/refresh
  -> validate refresh token
  -> issue new access token

Logout
  -> revoke/delete refresh token when server-side persistence is used
```

Recommended security properties:

- access token has short expiration
- refresh token has longer but finite expiration
- distinguish access and refresh token types
- invalidate revoked tokens
- rotate refresh tokens if the chosen design supports it cleanly

Do not store raw refresh tokens in the database if a secure hashed-token design can be implemented without making the project unnecessarily complex.

When choosing between a simple and advanced design, prefer the simplest design that is still secure and explain major tradeoffs in code comments or documentation.

---

## 15. Minimal Admin Design Guidance

Keep admin functionality deliberately small.

Recommended endpoints may include:

```text
GET /api/admin/dashboard
GET /api/admin/users?page=0&size=20
GET /api/admin/users/{id}
PATCH /api/admin/users/{id}/status
```

Possible dashboard statistics:

- total users
- total notes
- recently registered users count
- active/disabled users count
- basic AI request/error counts only if such telemetry already exists

Do not add invasive surveillance functionality.

Do not expose private note content merely because the requester is an admin.

---

## 16. Code Quality Guidelines

### Prefer

- constructor dependency injection
- `final` dependencies where appropriate
- descriptive names
- small methods
- immutable DTOs/records when compatible with the project's Java version and conventions
- reusable validation/business methods when genuinely shared
- comments that explain *why*, not obvious *what*

### Avoid

- field injection
- massive utility classes
- deep inheritance hierarchies
- unnecessary interfaces with only one trivial implementation unless they provide a useful boundary
- premature design patterns
- excessive abstraction
- static mutable state
- swallowing exceptions
- `catch (Exception e)` unless at a legitimate boundary with proper handling

---

## 17. Lombok

If Lombok is already used consistently, continue using it carefully.

Avoid annotations such as `@Data` on JPA entities when generated `equals`, `hashCode`, or `toString` could cause relationship, lazy-loading, recursion, or sensitive-data problems.

Do not add Lombok to the project solely to reduce a few getters/setters unless there is a clear benefit.

---

## 18. Testing Expectations

When changing important behavior, add or update tests where feasible.

Prioritize tests for:

- authentication
- refresh tokens
- note ownership/authorization
- admin authorization
- service business rules
- Groq failure handling
- exception mappings

Use the existing testing stack.

Do not introduce a completely new testing framework unless necessary.

At minimum after changes, run the project's standard test/build command when possible.

Examples:

```bash
./mvnw test
./mvnw clean verify
```

or, for Gradle projects:

```bash
./gradlew test
./gradlew build
```

Use the project's actual build system; do not assume Maven if it is Gradle or vice versa.

---

## 19. Build and Environment Compatibility

The backend should not depend on a specific IDE.

A healthy project should be runnable using its build tool from a terminal.

When investigating "works in one IDE but not VS Code":

Check:

- selected JDK version
- `JAVA_HOME`
- Maven/Gradle wrapper usage
- annotation processing
- Lombok support
- environment variables
- `.env` assumptions
- active Spring profile
- working directory
- generated sources
- extension-specific launch configuration

Fix project-level issues where possible rather than requiring special IDE behavior.

Do not commit machine-specific absolute paths.

---

## 20. Configuration Profiles

Prefer clear environment-aware configuration.

Possible profiles may include:

```text
application.yml
application-dev.yml
application-test.yml
application-prod.yml
```

Only introduce profiles when they solve a real configuration problem.

Keep secrets outside committed files.

Tests should not require production credentials or live external services unless explicitly designed as integration tests.

---

## 21. API Compatibility

Before modifying an endpoint, inspect its current request and response shape.

Because Notiva may be consumed by Next.js and React Native clients:

- avoid unnecessary response-field renames
- avoid changing endpoint paths casually
- avoid changing enum values casually
- avoid changing authentication headers/token semantics without noting the frontend impact

When a breaking API change is truly necessary, explicitly report:

1. what changed
2. why it changed
3. frontend impact
4. migration/action required

---

## 22. Dependency Management

Before adding a dependency, ask internally:

1. Can standard Spring/Java already solve this cleanly?
2. Is the dependency maintained?
3. Does it materially improve the implementation?
4. Will it make the project harder for a junior developer to understand?

Do not add libraries for trivial functionality.

Keep dependency versions consistent with the Spring Boot dependency-management model.

---

## 23. Comments and Documentation

Add documentation where it improves maintainability.

Useful documentation includes:

- non-obvious security decisions
- refresh-token behavior
- external API configuration
- required environment variables
- setup/run instructions
- unusual database constraints

Avoid comments that merely restate code.

Update README/config examples when a change introduces a new required environment variable or setup step.

---

## 24. Forbidden / High-Risk Changes

Unless explicitly requested and justified, DO NOT:

- rewrite the entire backend
- switch Java frameworks
- switch databases
- replace JWT authentication with a different authentication architecture
- expose user passwords or token secrets
- expose private note content to admins
- disable Spring Security to make an endpoint work
- use `permitAll()` broadly as a shortcut
- disable CSRF/CORS/security controls without understanding the architecture
- hardcode secrets
- commit API keys
- delete database data
- make destructive schema changes
- change public API contracts unnecessarily
- add large frameworks for small problems
- remove tests just because they fail
- catch and ignore exceptions
- remove code without checking references

---

## 25. Working Procedure for Every Task

For each maintenance request, use this sequence.

### Step 1 — Inspect

Read the relevant:

- controller
- service
- repository
- entity
- DTOs
- security configuration
- application configuration
- tests

Search references before modifying shared code.

### Step 2 — Diagnose

State the likely root cause internally before editing.

Distinguish:

- actual bug
- architecture issue
- configuration issue
- environment issue
- missing feature

### Step 3 — Plan minimally

Choose the smallest coherent change that fixes the issue and improves maintainability.

Avoid unrelated refactoring.

### Step 4 — Implement

Follow project conventions unless the conventions themselves are the problem.

Keep the change understandable.

### Step 5 — Verify

Where possible:

- compile
- run relevant tests
- run full tests for security/auth/schema changes
- inspect warnings/errors

### Step 6 — Review

Before finishing, check:

- Did authentication/authorization weaken?
- Can one user access another user's data?
- Did API response shape change?
- Are secrets exposed?
- Are null/error cases handled?
- Did the change introduce dead code?
- Is the solution more complex than necessary?

### Step 7 — Report

Summarize:

- what was changed
- why
- files affected
- verification performed
- remaining issues or tradeoffs

Do not claim a test passed unless it was actually run successfully.

---

## 26. When Refactoring Existing Code

Refactoring should improve clarity without changing behavior unless behavior change is part of the task.

Good refactors include:

- extracting controller business logic into a service
- replacing repeated response/error code
- separating external API client logic
- replacing exposed entities with DTOs
- consolidating duplicate validation
- clarifying package boundaries
- removing genuinely unused classes

Avoid "refactoring" that changes dozens of files only for aesthetics.

For large structural changes, perform them in logical phases and keep the project buildable between phases where practical.

---

## 27. Naming Conventions

Use conventional Spring naming.

Examples:

```text
NoteController
NoteService
NoteRepository
Note
CreateNoteRequest
UpdateNoteRequest
NoteResponse
AuthController
AuthService
JwtService
RefreshTokenService
GroqClient
GroqService
AdminController
AdminService
```

Avoid vague names such as:

```text
Manager
Helper
Stuff
CommonService
ProcessData
UtilService
```

unless the responsibility genuinely matches the name.

---

## 28. Git-Friendly Changes

Keep diffs reviewable.

- Do not reformat unrelated files.
- Do not reorder entire files without reason.
- Do not modify generated files.
- Avoid changing line endings/project-wide formatting as a side effect.
- Preserve existing code style unless actively standardizing it as an explicit task.

When fixing a bug, prefer a focused diff.

---

## 29. Definition of Done

A maintenance task is complete when, as applicable:

- requested behavior is implemented
- code compiles
- relevant tests pass
- security boundaries remain intact
- errors are handled appropriately
- no secrets are introduced
- API changes are documented
- obsolete code created by the change is removed
- new configuration requirements are documented
- solution remains understandable to the project owner

---

## 30. Current Notiva Maintenance Roadmap

Use this as the default order unless a task explicitly requires something else:

### Phase A — Project health

1. Ensure clean command-line startup/build.
2. Fix environment/configuration inconsistencies.
3. Identify broken or duplicated code.
4. Establish stable exception handling and API patterns.

### Phase B — Architecture cleanup

1. Improve package structure.
2. Separate controller/service/repository responsibilities.
3. Introduce/clean DTO boundaries.
4. Remove confirmed unused code.

### Phase C — Authentication improvement

1. Review current JWT implementation.
2. Add refresh tokens.
3. Improve token validation and error handling.
4. Implement safe logout/revocation behavior.
5. Add authentication tests.

### Phase D — Groq integration

1. Diagnose current connection failure.
2. Correct configuration/client behavior.
3. Add safe timeouts and error mapping.
4. Ensure secrets are externalized.
5. Add tests/mocks where feasible.

### Phase E — Minimal admin API

1. Define admin role authorization.
2. Add dashboard aggregate statistics.
3. Add paginated user administration if needed.
4. Preserve note privacy.
5. Add admin authorization tests.

### Phase F — Final quality pass

1. Review validation.
2. Review security.
3. Review database queries and constraints.
4. Review logs for sensitive information.
5. Update documentation.
6. Run final build/tests.

---

## 31. Communication Style for Codex

When responding after making code changes, be concise but informative.

Preferred format:

```text
Changed
- ...
- ...

Why
- ...

Verified
- ./mvnw test
- application starts successfully

Notes
- frontend must update ... (only when applicable)
```

If something could not be verified, say so clearly.

Do not pretend to have run commands that were not run.

---

## 32. Final Principle

The target is not maximum architectural complexity.

The target is a backend that is:

- secure
- maintainable
- understandable
- testable
- consistent
- portfolio-worthy
- close to professional Spring Boot practices

Prefer a clean and correct solution that a junior developer can explain confidently in an interview over an unnecessarily sophisticated design.
