# Notiva API

Notiva API is the Spring Boot backend for a private, AI-assisted note-taking application. It provides JWT authentication with refresh tokens, user-owned notes and categories, note images, AI writing tools and conversations, semantic search, email verification/password reset, Google OAuth, and a privacy-safe admin API.

This repository contains the backend only. A Next.js frontend can use the documented REST API contract.

## Features

- JWT access tokens, rotating refresh tokens, and logout revocation
- Email/password registration, verification, and password reset
- Google OAuth 2.0 sign-in
- Private note, category, version-history, trash, restore, and permanent-delete workflows
- Note and avatar image uploads through Cloudinary
- Groq-powered note assistance and AI conversations
- Voyage-powered embeddings and semantic search with PostgreSQL `pgvector`
- Minimal admin dashboard, user list, and search APIs without private note-content access

## Technology

- Java 21, Spring Boot 4.1, Maven Wrapper
- Spring Security, JWT, JPA/Hibernate, Flyway
- PostgreSQL with the `pgvector` extension
- Groq, Voyage AI, Cloudinary, Google OAuth 2.0, and SMTP email

## Documentation

- [API contract for the Next.js frontend](API_DOCUMENTATION.md)
- [Architecture overview](ARCHITECTURE.md)
- [Initial Flyway schema](src/main/resources/db/migration/V1__initial_schema.sql)

## Prerequisites

Install the following before running the backend:

- Git
- JDK 21
- PostgreSQL. For every AI/semantic-search feature, PostgreSQL must support the `pgvector` extension.
- Accounts/credentials for the integrations you intend to use: SMTP, Google OAuth, Cloudinary, Groq, and Voyage AI.

Maven does not need to be installed globally because the repository includes the Maven Wrapper.

Check the JDK version:

```powershell
java -version
```

The output should show Java 21. A newer JDK may work locally, but CI and the supported project target use Java 21.

## Clone the project

```powershell
git clone https://github.com/Kyaw-Kyaw-Sann/notiva-api.git
cd notiva-api
```

## Configure local environment variables

Create a private local environment file:

```powershell
Copy-Item .env.example .env
```

Open `.env` and set the values for your environment. Never commit `.env`.

At minimum, a normal application startup requires valid values for:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
MAIL_USERNAME
MAIL_APP_PASSWORD
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
```

To use every AI feature, also configure:

```text
GROQ_API_KEY
GROQ_MODEL=groq/compound-mini
VOYAGE_API_KEY
VOYAGE_MODEL=voyage-4-lite
```

For a local PostgreSQL database, the connection setting normally looks like this:

```properties
DATABASE_URL=jdbc:postgresql://localhost:5432/notiva_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-local-password
```

For Neon, use its connection details in JDBC format. If Neon provides a URI beginning with `postgresql://`, prefix it with `jdbc:` for `DATABASE_URL`.

```text
jdbc:postgresql://<neon-host>/<database>?sslmode=require
```

Generate a strong Base64 value for `JWT_SECRET`; do not reuse the example below in production:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

The full variable-name reference is in [`.env.example`](.env.example).

## Database setup

### Existing development database

The default local configuration uses `JPA_DDL_AUTO=update` and keeps Flyway disabled. This preserves the existing development workflow, but it is not suitable for production.

### Fresh database

The initial Flyway migration creates the full schema and requires `pgvector` because note embeddings use `vector(512)`. Use a PostgreSQL server where the `vector` extension is available.

For a fresh production-style database, use the `prod` profile. Flyway applies `V1__initial_schema.sql` automatically and Hibernate validates rather than modifies the schema.

### Existing non-empty database

Before the first production deployment, create a provider backup or a Neon branch. Then set the following value for **one deployment only**:

```text
FLYWAY_BASELINE_ON_MIGRATE=true
```

This adds version `1` to `flyway_schema_history` without re-running V1 against existing data. After it succeeds, set it back to `false` and redeploy.

## Run locally

From the repository root, run:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

The default port is `8080`. Stop the application with `Ctrl+C`.

## Verify the backend

Open this URL in a browser:

```text
http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP",
  "application": "Notiva API"
}
```

You can also use PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

## Build and test

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd clean verify --batch-mode
```

`clean verify` is the closest local equivalent to the GitHub Actions CI build.

## API usage

The health endpoint is public. Most endpoints require an access token:

```http
Authorization: Bearer <access-token>
Content-Type: application/json
```

Start with the authentication endpoints in [API_DOCUMENTATION.md](API_DOCUMENTATION.md), then use the returned access token for user, note, AI, and admin APIs. Refresh tokens are only for `/api/auth/refresh` and `/api/auth/logout`; never use them as Bearer tokens.

## Railway + Neon deployment

This project can deploy from GitHub to Railway without Docker Desktop.

1. Push the `master` branch to GitHub.
2. In Railway, create a service from the GitHub repository and select the `master` branch.
3. Set `SPRING_PROFILES_ACTIVE=prod`.
4. Add all required values from `.env.example` as Railway Variables. Store real values only in Railway, never in Git.
5. Set `FLYWAY_BASELINE_ON_MIGRATE=true` only for the first deployment to an existing Neon database after creating a backup/branch.
6. Configure Railway Healthcheck Path as `/api/health`.
7. Generate a Railway public domain and verify `https://<your-domain>/api/health` returns `200`.
8. Set `FLYWAY_BASELINE_ON_MIGRATE=false` and redeploy after baseline success.

After a public backend domain exists, update these variables:

```text
BACKEND_URL=https://<your-railway-domain>
FRONTEND_URL=https://<your-frontend-domain>
OAUTH2_FRONTEND_CALLBACK_URL=https://<your-frontend-domain>/oauth2/callback
```

Google Cloud Console must also allow this backend OAuth redirect URI:

```text
https://<your-railway-domain>/login/oauth2/code/google
```

`FRONTEND_URL` must exactly match the browser frontend origin. The current CORS configuration permits one frontend origin.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Application cannot connect to PostgreSQL | Verify `DATABASE_URL`, username, password, network access, and `sslmode=require` for Neon. |
| Flyway refuses a non-empty database | Restore/confirm a backup, use `FLYWAY_BASELINE_ON_MIGRATE=true` once, then set it back to `false`. |
| AI endpoint fails | Verify the relevant Groq or Voyage key, model, provider availability, and server logs without exposing keys. |
| Google OAuth fails after deployment | Verify the registered redirect URI uses the exact Railway HTTPS domain. |
| Browser frontend gets a CORS error | Set `FRONTEND_URL` to the exact frontend origin and restart/redeploy. |
| `401 Unauthorized` on a private API | Send a valid access token with `Authorization: Bearer <access-token>`. |
| `403 Forbidden` on an admin API | Sign in using an account with the `ADMIN` role. |

## Security notes

- Never commit `.env`, database credentials, API keys, JWT secrets, access tokens, or refresh tokens.
- Do not log passwords, authorization headers, private note content, or provider secrets.
- Rotate any credential that was accidentally committed, shared, or exposed in logs.
- Keep production schema changes in Flyway migrations; production uses Hibernate validation rather than schema updates.

## Contributing workflow

1. Create a branch from `master`.
2. Make a focused change and add tests when behavior changes.
3. Run `./mvnw clean verify --batch-mode`.
4. Open a Pull Request and wait for CI before merging.
