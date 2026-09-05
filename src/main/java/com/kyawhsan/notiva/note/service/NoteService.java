package com.kyawhsan.notiva.note.service;

import com.kyawhsan.notiva.note.dto.CreateNoteRequest;
import com.kyawhsan.notiva.note.dto.NoteResponse;
import com.kyawhsan.notiva.note.dto.NoteSearchRequest;
import com.kyawhsan.notiva.note.dto.NoteVersionResponse;
import com.kyawhsan.notiva.note.dto.NoteVersionSummaryResponse;
import com.kyawhsan.notiva.common.response.PagedResponse;
import com.kyawhsan.notiva.note.dto.UpdateNoteRequest;
import com.kyawhsan.notiva.ai.entity.AiConversation;
import com.kyawhsan.notiva.note.entity.Category;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.note.entity.NoteVersion;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.note.enums.NoteSort;
import com.kyawhsan.notiva.common.exception.BadRequestException;
import com.kyawhsan.notiva.common.exception.ResourceNotFoundException;
import com.kyawhsan.notiva.ai.repository.AiConversationRepository;
import com.kyawhsan.notiva.ai.repository.AiMessageRepository;
import com.kyawhsan.notiva.note.repository.CategoryRepository;
import com.kyawhsan.notiva.note.repository.NoteRepository;
import com.kyawhsan.notiva.note.repository.NoteSpecifications;
import com.kyawhsan.notiva.note.repository.NoteVersionRepository;
import com.kyawhsan.notiva.security.CurrentUserService;
import com.kyawhsan.notiva.ai.service.NoteEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NoteService {

    private static final int MAX_NOTE_VERSIONS = 20;

    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    private final NoteVersionRepository noteVersionRepository;
    private final AiConversationRepository aiConversationRepository;
    private final AiMessageRepository aiMessageRepository;

    private final NoteEmbeddingService noteEmbeddingService;

    @Transactional
    public NoteResponse createNote(
            CreateNoteRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Category category = resolveCategory(request.categoryId(), currentUser);

        Note note = new Note();

        note.setTitle(request.title().trim());
        note.setContentJson(request.contentJson());
        note.setPlainText(request.plainText());
        note.setBackgroundColor(request.backgroundColor());
        note.setPinned(false);
        note.setFavorite(false);
        note.setDeletedAt(null);
        note.setUser(currentUser);
        note.setCategory(category);

        Note savedNote = noteRepository.save(note);

        noteEmbeddingService.synchronizeNoteSafely(savedNote);

        return NoteResponse.from(savedNote);
    }

    private Category resolveCategory(
            Long categoryId,
            User currentUser) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findByIdAndUser(categoryId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getNotes() {
        User currentUser = currentUserService.getCurrentUser();

        return noteRepository.findAllByUserAndDeletedAtIsNullOrderByUpdatedAtDesc(currentUser)
                .stream().map(NoteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse getNote(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        return NoteResponse.from(note);
    }

    private Note findActiveNoteByIdAndUser(
            Long noteId,
            User currentUser) {
        return noteRepository.findByIdAndUserAndDeletedAtIsNull(noteId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    @Transactional
    public NoteResponse updateNote(
            Long noteId,
            UpdateNoteRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        Category category = resolveCategory(request.categoryId(), currentUser);

        boolean meaningfulContentChanged = hasMeaningfulContentChanged(note, request);

        boolean embeddingContentChanged = hasEmbeddingContentChanged(note, request);

        if (meaningfulContentChanged) {
            createNoteVersion(note);
        }

        note.setTitle(request.title().trim());
        note.setContentJson(request.contentJson());
        note.setPlainText(request.plainText());
        note.setBackgroundColor(request.backgroundColor());
        note.setCategory(category);

        Note updatedNote = noteRepository.save(note);

        if (meaningfulContentChanged) {
            trimNoteVersions(note);
        }

        if (embeddingContentChanged) {
            noteEmbeddingService.synchronizeNoteSafely(updatedNote);
        }

        return NoteResponse.from(updatedNote);
    }

    private boolean hasMeaningfulContentChanged(
            Note note,
            UpdateNoteRequest request) {
        String normalizedTitle = request.title().trim();

        return !Objects.equals(note.getTitle(), normalizedTitle)
                || !Objects.equals(note.getContentJson(), request.contentJson())
                || !Objects.equals(note.getPlainText(), request.plainText());
    }

    private boolean hasEmbeddingContentChanged(
            Note note,
            UpdateNoteRequest request) {
        String normalizedTitle = request.title().trim();

        return !Objects.equals(note.getTitle(), normalizedTitle)
                || !Objects.equals(note.getPlainText(), request.plainText());
    }

    private void createNoteVersion(
            Note note) {
        NoteVersion version = new NoteVersion();

        version.setTitle(note.getTitle());
        version.setContentJson(note.getContentJson());
        version.setPlainText(note.getPlainText());
        version.setNote(note);

        noteVersionRepository.save(version);
    }

    private void trimNoteVersions(
            Note note) {
        long versionCount = noteVersionRepository.countByNote(note);

        if (versionCount <= MAX_NOTE_VERSIONS) {
            return;
        }

        int deleteCount = (int) (versionCount - MAX_NOTE_VERSIONS);

        List<NoteVersion> versions = noteVersionRepository.findAllByNoteOrderByCreatedAtAsc(note);

        List<NoteVersion> oldestVersions = versions.subList(0, deleteCount);

        noteVersionRepository.deleteAll(oldestVersions);
    }

    @Transactional(readOnly = true)
    public List<NoteVersionSummaryResponse> getNoteVersions(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        return noteVersionRepository.findAllByNoteOrderByCreatedAtDesc(note).stream()
                .map(NoteVersionSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public NoteVersionResponse getNoteVersion(
            Long noteId,
            Long versionId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        NoteVersion version = findVersionByIdAndNote(versionId, note);

        return NoteVersionResponse.from(version);
    }

    private NoteVersion findVersionByIdAndNote(
            Long versionId,
            Note note) {
        return noteVersionRepository.findByIdAndNote(versionId, note)
                .orElseThrow(() -> new ResourceNotFoundException("Note version not found"));
    }

    @Transactional
    public NoteResponse restoreNoteVersion(
            Long noteId,
            Long versionId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        NoteVersion selectedVersion = findVersionByIdAndNote(versionId, note);

        createNoteVersion(note);

        note.setTitle(selectedVersion.getTitle());
        note.setContentJson(selectedVersion.getContentJson());
        note.setPlainText(selectedVersion.getPlainText());

        Note restoredNote = noteRepository.save(note);

        trimNoteVersions(note);

        noteEmbeddingService.synchronizeNoteSafely(restoredNote);

        return NoteResponse.from(restoredNote);
    }

    @Transactional
    public void permanentlyDeleteNote(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findDeletedNoteByIdAndUser(noteId, currentUser);

        permanentlyDeleteNoteEntity(note);
    }

    @Transactional
    public int emptyRecycleBin() {
        User currentUser = currentUserService.getCurrentUser();

        List<Note> deletedNotes = noteRepository
                .findAllByUserAndDeletedAtIsNotNullOrderByDeletedAtDesc(currentUser);

        for (Note note : deletedNotes) {
            permanentlyDeleteNoteEntity(note);
        }

        return deletedNotes.size();
    }

    private void permanentlyDeleteNoteEntity(
            Note note) {
        noteEmbeddingService.deleteByNoteId(note.getId());

        deleteNoteDependencies(note);

        noteRepository.delete(note);
    }

    private void deleteNoteDependencies(
            Note note) {
        noteVersionRepository.deleteAllByNote(note);

        List<AiConversation> conversations = aiConversationRepository.findAllByNote(note);

        for (AiConversation conversation : conversations) {
            aiMessageRepository.deleteAllByConversation(conversation);
        }

        aiConversationRepository.deleteAllByNote(note);
    }

    @Transactional
    public NoteResponse pinNote(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        note.setPinned(true);

        return NoteResponse.from(noteRepository.save(note));
    }

    @Transactional
    public NoteResponse unpinNote(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        note.setPinned(false);

        return NoteResponse.from(noteRepository.save(note));
    }

    @Transactional
    public NoteResponse favoriteNote(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        note.setFavorite(true);

        return NoteResponse.from(noteRepository.save(note));
    }

    @Transactional
    public NoteResponse unfavoriteNote(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        note.setFavorite(false);

        return NoteResponse.from(noteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getPinnedNotes() {
        User currentUser = currentUserService.getCurrentUser();

        return noteRepository
                .findAllByUserAndPinnedTrueAndDeletedAtIsNullOrderByUpdatedAtDesc(currentUser)
                .stream().map(NoteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getFavoriteNotes() {
        User currentUser = currentUserService.getCurrentUser();

        return noteRepository
                .findAllByUserAndFavoriteTrueAndDeletedAtIsNullOrderByUpdatedAtDesc(currentUser)
                .stream().map(NoteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<NoteResponse> searchNotes(
            NoteSearchRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        validateSearchFilters(request);

        Category category = resolveSearchCategory(request.getCategoryId(), currentUser);

        Specification<Note> specification = NoteSpecifications.belongsToUser(currentUser)
                .and(NoteSpecifications.isActive())
                .and(NoteSpecifications.hasKeyword(request.getQuery()))
                .and(NoteSpecifications.hasCategory(category))
                .and(NoteSpecifications.isUncategorized(request.getUncategorized()))
                .and(NoteSpecifications.hasBackgroundColor(request.getBackgroundColor()))
                .and(NoteSpecifications.hasPinnedState(request.getPinned()))
                .and(NoteSpecifications.hasFavoriteState(request.getFavorite()));

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                resolveSort(request.getSort()));

        Page<Note> notePage = noteRepository.findAll(specification, pageable);

        return PagedResponse.from(notePage, NoteResponse::from);
    }

    private void validateSearchFilters(
            NoteSearchRequest request) {
        if (request.getCategoryId() != null && Boolean.TRUE.equals(request.getUncategorized())) {
            throw new BadRequestException(
                    "Category and uncategorized filters cannot be used together");
        }
    }

    private Category resolveSearchCategory(
            Long categoryId,
            User currentUser) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findByIdAndUser(categoryId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private Sort resolveSort(
            NoteSort noteSort) {
        NoteSort selectedSort = noteSort == null ? NoteSort.UPDATED_DESC : noteSort;

        return switch (selectedSort) {
        case UPDATED_DESC -> Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"));

        case UPDATED_ASC -> Sort.by(Sort.Order.asc("updatedAt"), Sort.Order.asc("id"));

        case CREATED_DESC -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

        case CREATED_ASC -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));

        case TITLE_ASC -> Sort.by(Sort.Order.asc("title").ignoreCase(), Sort.Order.asc("id"));

        case TITLE_DESC -> Sort.by(Sort.Order.desc("title").ignoreCase(), Sort.Order.desc("id"));
        };
    }

    @Transactional
    public void moveNoteToTrash(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findActiveNoteByIdAndUser(noteId, currentUser);

        note.setDeletedAt(LocalDateTime.now());

        noteRepository.save(note);

        // Soft delete ဖြစ်သောကြောင့်
        // embeddings ကို မဖျက်ပါ။
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getTrashedNotes() {
        User currentUser = currentUserService.getCurrentUser();

        return noteRepository.findAllByUserAndDeletedAtIsNotNullOrderByDeletedAtDesc(currentUser)
                .stream().map(NoteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse getTrashedNote(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findDeletedNoteByIdAndUser(noteId, currentUser);

        return NoteResponse.from(note);
    }

    @Transactional
    public NoteResponse restoreNote(
            Long noteId) {
        User currentUser = currentUserService.getCurrentUser();

        Note note = findDeletedNoteByIdAndUser(noteId, currentUser);

        note.setDeletedAt(null);

        Note restoredNote = noteRepository.save(note);

        // Chunks မဖျက်ထားသောကြောင့်
        // embedding sync ပြန်လုပ်စရာမလိုပါ။

        return NoteResponse.from(restoredNote);
    }

    private Note findDeletedNoteByIdAndUser(
            Long noteId,
            User currentUser) {
        return noteRepository.findByIdAndUserAndDeletedAtIsNotNull(noteId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted note not found"));
    }
}
