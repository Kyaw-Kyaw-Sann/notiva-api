# Notiva Backend Architecture

## Feature boundaries

- `auth/` owns authentication controllers, services, token DTOs, and persisted
  authentication tokens.
- `user/` owns profile, avatar, password-change, and account-deletion API
  workflows.
- `note/` owns note, category, image, version, and search API workflows.
- `ai/` owns AI controllers, provider clients, AI DTOs, conversation/chat,
  semantic-search, and embedding application services.
- `common/` owns shared API responses, exceptions, health endpoint, and
  utilities.
- `security/` owns Spring Security and JWT implementation.
- `config/` owns application and provider configuration.

Controllers use request/response DTOs and delegate to services. Repositories
remain persistence-only; entities are not returned directly from controllers.

## Intentionally shared or legacy persistence types

The following types remain outside feature packages because moving them would
create broad cross-feature import changes without improving an API boundary:

| Area | Types | Reason |
| --- | --- | --- |
| Shared security context | `CurrentUserService` | Used by auth, user, note, and AI services to obtain the authenticated user. |
| Shared media | `CloudinaryService`, `ImageUploadResponse` | Used by both user-avatar and note-image workflows. |
| pgvector support | `PgVectorService` | Shared database infrastructure used by the embedding test workflow. |
| Core persistence | `User`, `Note`, `Category`, `NoteVersion` and their repositories | JPA relationships and account/note deletion workflows span auth, user, note, and AI features. |
| AI persistence | `AiConversation`, `AiMessage`, `AiUsage` and their repositories | Note deletion and user deletion must delete conversations, messages, and usage data in a defined order. |
| Note chunk persistence | `NoteChunkJdbcRepository`, `NoteSpecifications` | Semantic search and embedding cleanup require direct persistence access while preserving user ownership filtering. |

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

These dependencies are intentional maintenance boundaries. Any future
repository/entity package migration must preserve them and be verified with
deletion and ownership tests.
