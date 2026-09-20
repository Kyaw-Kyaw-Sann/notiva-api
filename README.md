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

Generate a strong Base64 value for `JWT_SECRET`; do not reuse the example below outside local development:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

The full variable-name reference is in [`.env.example`](.env.example).

## Database setup

### Existing development database

The default local configuration uses `JPA_DDL_AUTO=update` and keeps Flyway disabled. This preserves the existing development workflow.

### Fresh database

The initial Flyway migration creates the full schema and requires `pgvector` because note embeddings use `vector(512)`. Use a PostgreSQL server where the `vector` extension is available.

The migration is [V1__initial_schema.sql](src/main/resources/db/migration/V1__initial_schema.sql). Review it before applying it to a database that already contains data.

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

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Application cannot connect to PostgreSQL | Verify `DATABASE_URL`, username, password, network access, and `sslmode=require` for Neon. |
| AI endpoint fails | Verify the relevant Groq or Voyage key, model, provider availability, and server logs without exposing keys. |
| Google OAuth fails | Verify the registered redirect URI and Google client credentials match the active environment. |
| Browser frontend gets a CORS error | Set `FRONTEND_URL` to the exact frontend origin and restart the application. |
| `401 Unauthorized` on a private API | Send a valid access token with `Authorization: Bearer <access-token>`. |
| `403 Forbidden` on an admin API | Sign in using an account with the `ADMIN` role. |

## Security notes

- Never commit `.env`, database credentials, API keys, JWT secrets, access tokens, or refresh tokens.
- Do not log passwords, authorization headers, private note content, or provider secrets.
- Rotate any credential that was accidentally committed, shared, or exposed in logs.
- Keep schema changes in reviewed Flyway migrations rather than relying on automatic updates.

## Contributing workflow

1. Create a branch from `master`.
2. Make a focused change and add tests when behavior changes.
3. Run `./mvnw clean verify --batch-mode`.
4. Open a Pull Request and wait for CI before merging.
