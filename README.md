# Notiva API

Notiva is a Spring Boot backend for a private note-taking application with JWT authentication, user-owned notes, AI assistance, image uploads, and a privacy-safe admin API.

## Technology

- Java 21 and Spring Boot
- Spring Security with JWT access and refresh tokens
- PostgreSQL with JPA/Hibernate
- Groq and Voyage AI integrations
- Cloudinary image storage
- Google OAuth 2.0 and SMTP email verification/password reset

## Documentation

- [API contract for the Next.js frontend](API_DOCUMENTATION.md)
- [Architecture overview](ARCHITECTURE.md)

## Local setup

1. Install JDK 21 and use PostgreSQL.
2. Copy `.env.example` to `.env`.
3. Fill in the required local credentials in `.env`. Never commit this file.
4. Run the backend:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. Verify availability:

   ```text
   GET http://localhost:8080/api/health
   ```

The default local profile keeps `JPA_DDL_AUTO=update` for the existing development workflow. Do not use that setting in production.

## Environment variables

All variable names are documented in [`.env.example`](.env.example). The essential production values are:

| Group | Variables |
| --- | --- |
| Database | `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` |
| Security | `JWT_SECRET`, `JWT_ACCESS_EXPIRATION_MILLIS`, `JWT_REFRESH_EXPIRATION_MILLIS` |
| Application URLs | `FRONTEND_URL`, `BACKEND_URL`, `OAUTH2_FRONTEND_CALLBACK_URL` |
| Email | `MAIL_USERNAME`, `MAIL_APP_PASSWORD`, `MAIL_FROM` |
| Google OAuth | `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` |
| Media | `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` |
| AI | `GROQ_API_KEY`, `GROQ_MODEL`, `VOYAGE_API_KEY`, `VOYAGE_MODEL` |

`FRONTEND_URL` must be the exact deployed Next.js origin. `BACKEND_URL` must be the public backend origin because email verification links use it. Configure the same production callback URL in both `OAUTH2_FRONTEND_CALLBACK_URL` and Google Cloud Console.

## Build and test

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd test
```

## Production profile

Set `SPRING_PROFILES_ACTIVE=prod` only after the database migration baseline is implemented in Deployment Phase 2. The production profile:

- uses the platform-provided `PORT` with a local fallback of `8080`;
- enables forwarded HTTPS headers;
- disables SQL/debug-oriented output; and
- uses `spring.jpa.hibernate.ddl-auto=validate` so Hibernate cannot silently change the database schema.

## Flyway migrations

The initial migration is [V1__initial_schema.sql](src/main/resources/db/migration/V1__initial_schema.sql). It includes the `vector` extension and the `vector(512)` note-chunk column required by semantic search.

For a **fresh** database, activate the production profile and Flyway applies V1 automatically. For the existing non-empty database, first make a provider backup, then set `FLYWAY_BASELINE_ON_MIGRATE=true` for one deployment only. This records schema version 1 in `flyway_schema_history` without re-running V1 or changing existing application data. Remove the variable after the baseline has completed.

## Security checklist before publishing or deploying

- Do not commit `.env`, credentials, private keys, JWT secrets, or API keys.
- Rotate any credential that was ever shared, logged, or committed accidentally.
- Set all production values through GitHub/environment secret management, never in source code.
- Run `mvn clean compile` and `mvn test` before every release.
- Confirm private endpoints return `401` without a token and `403` where role authorization is required.

## Deployment roadmap

1. **Phase 1 — Production safety and configuration:** secret-safe configuration, environment template, documentation, and production profile.
2. **Phase 2 — Reproducible build and database safety:** Flyway baseline and CI.
3. **Phase 3 — Live deployment:** hosting configuration, production secrets, OAuth/CORS setup, and smoke tests.
