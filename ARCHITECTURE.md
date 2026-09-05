# Notiva Backend Architecture

## Feature boundaries

- `auth/` owns authentication controllers, services, token DTOs, and persisted
  authentication tokens.
- `user/` owns user entities/repositories, profile, avatar, password-change,
  and account-deletion workflows.
- `note/` owns note/category/version entities and repositories plus note,
  category, image, version, and search API workflows.
- `ai/` owns AI controllers, provider clients, AI DTOs, conversation/chat,
  semantic-search, embedding services, and AI persistence.
- `common/` owns shared API responses, exceptions, health endpoint, and
  utilities as well as shared persistence bases and storage integrations.
- `security/` owns Spring Security, JWT implementation, and authenticated-user
  resolution.
- `config/` owns application and provider configuration.

Controllers use request/response DTOs and delegate to services. Repositories
remain persistence-only; entities are not returned directly from controllers.

## Shared infrastructure

| Area | Types | Reason |
| --- | --- | --- |
| Security context | `security/CurrentUserService` | Used by auth, user, note, and AI services to obtain the authenticated user. |
| Shared media | `common/storage/CloudinaryService`, `common/response/ImageUploadResponse` | Used by both user-avatar and note-image workflows. |
| Persistence base | `common/persistence/BaseEntity`, `CreatedAtEntity` | Shared JPA timestamp bases without feature-specific behavior. |
| pgvector support | `common/persistence/PgVectorService` | Database infrastructure used by the embedding test workflow. |

## Critical cross-feature dependencies

- Note permanent deletion removes embedding chunks, note versions, AI messages,
  and AI conversations before deleting the note.
- User account deletion removes AI conversations/messages, note chunks, note
  versions/notes, categories, AI usage, and auth tokens before deleting the
  user.
- Category deletion clears its category from related notes before deleting the
  category.
- AI conversation and note-AI services resolve notes by authenticated user and
  exclude soft-deleted notes.
- Semantic search filters both `note_chunks.user_id` and `notes.user_id`
  with the authenticated user ID and excludes soft-deleted notes.

Cross-feature imports between these feature-owned entities and repositories are
intentional. Future changes must preserve these cleanup and ownership rules and
be verified with deletion and authorization tests.
