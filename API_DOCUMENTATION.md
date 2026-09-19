# Notiva Backend API Documentation

This is the API contract for the current Notiva Spring Boot backend. It is written for the Next.js frontend integration. It describes the endpoints as implemented on **2026-09-05**; it contains no real credentials or test accounts.

## 1. Base conventions

### Base URL and authorization

Development base URL: `http://localhost:8080`.

Except for the public endpoints listed below, send the access token on every request:

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

`accessToken` is a short-lived JWT. `refreshToken` is an opaque, rotating token: it is only for `/api/auth/refresh` and `/api/auth/logout`; never send it as a Bearer token. Store tokens carefully (for a browser frontend, an HttpOnly secure cookie is preferable when the backend/frontend design supports it).

Public endpoints are health, register, email verification, login, password-reset endpoints, refresh, logout, and OAuth start/callback endpoints. All note, user, AI, image, and admin endpoints are private. `/api/admin/**` additionally requires `ADMIN`.

### Success envelope

All endpoints except health use this success envelope:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {},
  "timestamp": "2026-09-05T16:30:00"
}
```

`data` can be an object, array, number, or `null` for a successful action with no payload.

### Error envelope

```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/login",
  "validationErrors": {
    "email": "must be a well-formed email address"
  },
  "timestamp": "2026-09-05T16:30:00"
}
```

Common statuses: `400` validation/request error, `401` missing/invalid access token, `403` authenticated but not authorized, `404` inaccessible/not found resource, `409` conflict, `429` AI usage/rate limit, `502` invalid upstream AI response, `503` unavailable AI provider, `504` AI timeout, and `500` unexpected server error. The frontend should display `message`, not infer behavior from raw exception text.

### Shared enums

| Field | Values |
| --- | --- |
| `role` | `USER`, `ADMIN` |
| `plan` | `NORMAL`, `PREMIUM` |
| `backgroundColor` | `DEFAULT`, `YELLOW`, `GREEN`, `BLUE`, `PINK`, `PURPLE`, `GRAY` |
| `sort` | `UPDATED_DESC`, `UPDATED_ASC`, `CREATED_DESC`, `CREATED_ASC`, `TITLE_ASC`, `TITLE_DESC` |
| summary `length` | `SHORT`, `MEDIUM`, `DETAILED` |
| writing `action` | `IMPROVE`, `FIX_GRAMMAR`, `SHORTEN`, `EXPAND`, `PROFESSIONAL`, `CONTINUE`, `CUSTOM` |

---

## 2. Health

### `GET /api/health` — public

Use this for a lightweight backend availability check.

```json
{ "status": "UP", "application": "Notiva API" }
```

---

## 3. Authentication — `/api/auth`

### `POST /register` — public

```json
{
  "displayName": "Aung Aung",
  "email": "aung@example.com",
  "password": "StrongPassword123"
}
```

`displayName`, `email`, and `password` are required. A successful response is `201 Created`; email verification is required before login.

```json
{
  "success": true,
  "message": "Registration successful. Check your email to verify your account.",
  "data": {
    "id": 12,
    "email": "aung@example.com",
    "displayName": "Aung Aung",
    "emailVerified": false
  },
  "timestamp": "..."
}
```

### `GET /verify-email?token=<token>` — public

The token comes from the verification email. On success it returns a success envelope with no `data`.

### `POST /login` — public

```json
{ "email": "aung@example.com", "password": "StrongPassword123" }
```

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "opaque-rotating-token",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "user": {
      "id": 12,
      "email": "aung@example.com",
      "displayName": "Aung Aung",
      "avatarUrl": null,
      "role": "USER",
      "plan": "NORMAL",
      "emailVerified": true
    }
  },
  "timestamp": "..."
}
```

`expiresIn` is milliseconds. Set the frontend access-token expiry from this field; do not decode a token to obtain the user identity.

### `POST /refresh` — public

```json
{ "refreshToken": "opaque-rotating-token" }
```

```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "new-opaque-rotating-token",
    "tokenType": "Bearer",
    "expiresIn": 900000
  },
  "timestamp": "..."
}
```

Replace both locally held tokens with the returned values. A successful refresh revokes the old refresh token. Expired, revoked, malformed, or wrong-token-type values must be treated as a signed-out state.

### `POST /logout` — public

```json
{ "refreshToken": "opaque-rotating-token" }
```

Invalidates the supplied refresh token and returns success with `data: null`. Clear local authentication state whether the request succeeds or the token is already unusable.

### Password recovery — public

| Endpoint | Body | Result |
| --- | --- | --- |
| `POST /forgot-password` | `{ "email": "aung@example.com" }` | Sends an OTP when the account exists; response deliberately does not reveal account existence. |
| `POST /verify-reset-otp` | `{ "email": "aung@example.com", "otp": "123456" }` | Verifies the OTP. |
| `POST /reset-password` | `{ "email": "aung@example.com", "otp": "123456", "newPassword": "NewStrongPassword123" }` | Changes the password. |

### `GET /me` — authenticated

Returns the current authenticated user in the same shape as `LoginResponse.data.user`.

### Google OAuth — browser redirect flow

Start Google sign-in by navigating the browser (not by an AJAX request) to:

```text
GET /oauth2/authorization/google
```

After a successful Google callback, the backend redirects to `OAUTH2_FRONTEND_CALLBACK_URL` (default: `http://localhost:3000/oauth2/callback`) with the access token in the URL fragment:

```text
/oauth2/callback#access_token=eyJ...
```

On failure it redirects with `?error=Google%20authentication%20failed`. This flow currently returns an **access token only**, not a refresh token. The Next.js callback page must read the fragment client-side, remove it from browser history immediately, and obtain the user with `GET /api/auth/me` before entering the app.

---

## 4. User profile — `/api/users/me` (authenticated)

### Read/update profile

| Method and path | Request | Response data |
| --- | --- | --- |
| `GET /api/users/me` | none | full profile object |
| `PATCH /api/users/me` | `{ "displayName": "New name" }` | full profile object |
| `PATCH /api/users/me/password` | `{ "currentPassword": "...", "newPassword": "..." }` | `null` |
| `PATCH /api/users/me/upgrade` | none | full profile object |
| `PATCH /api/users/me/downgrade` | none | full profile object |
| `DELETE /api/users/me` | none | `null`; removes the current account and associated data |

Profile response example:

```json
{
  "id": 12,
  "email": "aung@example.com",
  "displayName": "Aung Aung",
  "avatarUrl": "https://...",
  "role": "USER",
  "plan": "NORMAL",
  "emailVerified": true,
  "createdAt": "2026-09-01T10:00:00",
  "updatedAt": "2026-09-05T16:00:00"
}
```

### Avatar

`POST /api/users/me/avatar` is `multipart/form-data` with required file field `file`. Do not send JSON `Content-Type`.

```text
file: <image file>
```

Its data contains image metadata, for example `{ "url": "https://...", "publicId": "..." }`. `DELETE /api/users/me/avatar` removes the current avatar and returns success with `data: null`.

---

## 5. Categories — `/api/categories` (authenticated)

### Category shape

```json
{ "name": "Work" }
```

| Method and path | Request | Result |
| --- | --- | --- |
| `POST /api/categories` | category shape | `201`, category object |
| `GET /api/categories` | none | category array |
| `GET /api/categories/{categoryId}` | none | category object |
| `PUT /api/categories/{categoryId}` | category shape | category object |
| `DELETE /api/categories/{categoryId}` | none | `null` |

Category responses include `id`, `name`, `createdAt`, and `updatedAt`. A category summary embedded in a note includes `id` and `name`. Deleting a category keeps its notes and safely makes their category `null`.

---

## 6. Notes — `/api/notes` (authenticated)

### Note request and response

Create request:

```json
{
  "title": "Project ideas",
  "contentJson": "{\"type\":\"doc\",\"content\":[]}",
  "plainText": "Project ideas and next steps",
  "backgroundColor": "YELLOW",
  "categoryId": 3
}
```

`title` is required (maximum 255 characters); `contentJson` and `plainText` are required. `categoryId` is optional. `contentJson` is stored as a string: the Next.js editor owns its JSON schema.

Typical note response:

```json
{
  "id": 101,
  "title": "Project ideas",
  "contentJson": "{\"type\":\"doc\",\"content\":[]}",
  "plainText": "Project ideas and next steps",
  "backgroundColor": "YELLOW",
  "category": { "id": 3, "name": "Work" },
  "pinned": false,
  "favorite": true,
  "deletedAt": null,
  "createdAt": "2026-09-05T10:00:00",
  "updatedAt": "2026-09-05T10:00:00"
}
```

### Core note operations

| Method and path | Request | Result |
| --- | --- | --- |
| `POST /api/notes` | create request | `201`, note |
| `GET /api/notes` | none | active-note array |
| `GET /api/notes/{noteId}` | none | note |
| `PUT /api/notes/{noteId}` | create fields (update fields) | updated note |
| `PATCH /api/notes/{noteId}/pin` | none | note |
| `PATCH /api/notes/{noteId}/unpin` | none | note |
| `PATCH /api/notes/{noteId}/favorite` | none | note |
| `PATCH /api/notes/{noteId}/unfavorite` | none | note |
| `GET /api/notes/pinned` | none | note array |
| `GET /api/notes/favorites` | none | note array |

The server enforces ownership for every note ID; a user must never receive or mutate another user's note.

### Search and filters

`GET /api/notes/search` uses query parameters. All are optional:

```text
/api/notes/search?query=project&categoryId=3&pinned=true&sort=UPDATED_DESC&page=0&size=12
```

Supported parameters: `query`, `categoryId`, `uncategorized`, `backgroundColor`, `pinned`, `favorite`, `sort`, `page` (minimum 0; default 0), and `size` (1–50; default 12).

The data is a paged result:

```json
{
  "content": [ { "id": 101, "title": "Project ideas" } ],
  "page": 0,
  "size": 12,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Trash and version history

| Method and path | Purpose |
| --- | --- |
| `DELETE /api/notes/{noteId}` | soft-delete: move the note to recycle bin |
| `GET /api/notes/trash` | list current user's trashed notes |
| `GET /api/notes/trash/{noteId}` | retrieve one trashed note |
| `PATCH /api/notes/{noteId}/restore` | restore it |
| `DELETE /api/notes/{noteId}/permanent` | permanently delete it and related cleanup data |
| `DELETE /api/notes/trash` | empty current user's recycle bin; data is deletion count |
| `GET /api/notes/{noteId}/versions` | version-summary array |
| `GET /api/notes/{noteId}/versions/{versionId}` | full version |
| `POST /api/notes/{noteId}/versions/{versionId}/restore` | restore that version; returns note |

Version responses contain the saved note content and timestamps. Treat permanent deletion as irreversible in the UI.

### Note images

| Method and path | Request | Result |
| --- | --- | --- |
| `POST /api/images/notes` | `multipart/form-data`, required `file` | image upload metadata |
| `DELETE /api/images/notes` | `{ "publicId": "cloudinary-public-id" }` | `null` |

Save returned image URLs/public IDs inside the editor's `contentJson` only according to the frontend editor model. The backend does not attach uploads to a note automatically.

---

## 7. AI — authenticated

AI calls preserve note and conversation ownership and exclude deleted notes. Provider failures return safe error envelopes; display a retry-friendly message, never provider diagnostic text.

### General and note AI

| Method and path | Body | Response data |
| --- | --- | --- |
| `POST /api/ai/generate` | `{ "prompt": "Give three title ideas" }` | `{ "content": "..." }` |
| `GET /api/ai/usage` | none | current-user AI usage/limits |
| `POST /api/notes/{noteId}/ai/generate-title` | no body | generated-title object |
| `POST /api/notes/{noteId}/ai/summarize` | `{ "length": "SHORT" }` | summary object |
| `POST /api/notes/{noteId}/ai/write` | see below | generated-writing object |
| `POST /api/notes/{noteId}/ai/suggest-category` | no body | category suggestion object |

Writing assistance example:

```json
{
  "action": "IMPROVE",
  "selectedText": "This sentence needs better wording.",
  "instruction": null
}
```

For `CUSTOM`, send a meaningful `instruction`. The frontend must handle `429`, `502`, `503`, and `504` as non-destructive failures: never replace editor content unless the user accepts a successful generated result.

### AI conversations

| Method and path | Body | Result |
| --- | --- | --- |
| `POST /api/notes/{noteId}/ai/conversations` | optional `{ "title": "Discuss this note" }` | `201`, conversation |
| `POST /api/ai/conversations/all-notes` | optional `{ "title": "My notes" }` | `201`, conversation |
| `GET /api/ai/conversations` | none | conversation array |
| `GET /api/ai/conversations/{conversationId}` | none | conversation |
| `POST /api/ai/conversations/{conversationId}/messages` | `{ "message": "What are the next steps?" }` | chat response |
| `GET /api/ai/conversations/{conversationId}/messages` | none | message array |
| `PATCH /api/ai/conversations/{conversationId}` | `{ "title": "Renamed conversation" }` | conversation |
| `DELETE /api/ai/conversations/{conversationId}` | none | `null` |

Typical message data includes message identity, role (`USER` or `ASSISTANT`), content, and timestamp. Do not let client-supplied IDs decide ownership; the backend does this from the access token.

Conversation data is `{ "id", "title", "type", "note", "createdAt", "updatedAt" }`. A message is `{ "id", "role", "content", "createdAt" }`. `NoteChatResponse` contains `userMessage`, `assistantMessage`, `model`, and `usage`. AI generation responses include the generated `content` (or `title` / `summary`), the provider `model`, and usage where applicable. Usage data is `{ "plan", "dailyLimit", "used", "remaining", "usageDate" }`.

### Semantic search and embedding maintenance

| Method and path | Body | Intended use |
| --- | --- | --- |
| `POST /api/ai/embeddings/semantic-search` | `{ "question": "Where did I write about the launch plan?" }` | user-owned semantic search results |
| `POST /api/ai/embeddings/test` | `{ "text": "A short test sentence" }` | diagnostic embedding check |
| `GET /api/ai/embeddings/notes/{noteId}/count` | none | chunk count for an owned note |
| `POST /api/ai/embeddings/rebuild-missing` | none | rebuild missing chunks for current user's notes |

The final three are operational/developer-facing endpoints. Do not expose them in normal end-user navigation unless product requirements need them.

---

## 8. Administration — `/api/admin` (ADMIN only)

Unauthenticated requests return `401`; a logged-in `USER` returns `403`.

| Method and path | Query | Response data |
| --- | --- | --- |
| `GET /api/admin/dashboard` | none | aggregate dashboard statistics |
| `GET /api/admin/users` | `search`, `page` (default 0), `size` (default 20, max 100) | paged user metadata |

Example user-list request:

```text
/api/admin/users?search=aung&page=0&size=20
```

Admin user results contain only `id`, `email`, `displayName`, `avatarUrl`, `role`, `plan`, `enabled`, `emailVerified`, and `createdAt`. They deliberately do **not** include passwords, access/refresh/reset/OAuth tokens, note content, or private note bodies. Dashboard data is `{ "totalUsers", "totalNotes", "totalAiRequests" }`.

---

## 9. Next.js integration checklist

1. Centralize `API_BASE_URL` and attach `Authorization: Bearer <accessToken>` only for authenticated calls.
2. On one `401`, attempt one refresh with the stored refresh token; replace both tokens on success. On refresh failure, clear auth state and redirect to sign-in. Avoid concurrent refresh requests.
3. Treat `403` as an authorization state, `404` for notes/conversations as unavailable (including another user's resource), and AI `429/502/503/504` as retryable UI failures.
4. Use `FormData` for the two image-upload endpoints. Do not manually set the multipart boundary or JSON content type for those requests.
5. Preserve `contentJson` exactly as the editor produces it and use `plainText` as a searchable text representation.
6. Read backend `message` and `validationErrors` for form feedback. Do not show raw HTTP/library errors or secrets to users.
7. Re-fetch or update client cache after note/category/user mutations; refresh is rotating, so always persist the newest value.

## 10. Contract safety

Do not use response values that are not documented by the server, and do not send user ID/owner ID fields for user-owned resources. API behavior and ownership are enforced server-side. If an endpoint needs a contract change for the frontend, update this document in the same backend change.
