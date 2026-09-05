package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.ai.dto.EmbeddingBatchResult;
import com.kyawhsan.notiva.ai.dto.NoteChunkData;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.ai.repository.NoteChunkJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(NoteEmbeddingService.class);

    private final NoteChunker noteChunker;

    private final EmbeddingClient embeddingClient;

    private final NoteChunkJdbcRepository noteChunkJdbcRepository;

    public void synchronizeNoteSafely(
            Note note) {
        try {
            synchronizeNote(note);

        } catch (RuntimeException exception) {
            log.warn("Unable to synchronize embeddings for note {}: {}", note.getId(),
                    exception.getMessage());
        }
    }

    @Transactional
    public void synchronizeNote(
            Note note) {
        List<NoteChunkData> chunks = noteChunker.chunk(note.getPlainText());

        if (chunks.isEmpty()) {
            noteChunkJdbcRepository.deleteAllByNoteId(note.getId());

            return;
        }

        List<String> embeddingTexts = chunks.stream().map(chunk -> buildEmbeddingText(note, chunk))
                .toList();

        EmbeddingBatchResult result = embeddingClient.embedDocuments(embeddingTexts);

        noteChunkJdbcRepository.deleteAllByNoteId(note.getId());

        noteChunkJdbcRepository.saveAll(note.getId(), note.getUser().getId(), chunks,
                result.vectors());
    }

    @Transactional
    public void deleteByNoteId(
            Long noteId) {
        noteChunkJdbcRepository.deleteAllByNoteId(noteId);
    }

    @Transactional
    public void deleteByUserId(
            Long userId) {
        noteChunkJdbcRepository.deleteAllByUserId(userId);
    }

    public long countByNoteId(
            Long noteId) {
        return noteChunkJdbcRepository.countByNoteId(noteId);
    }

    public boolean hasEmbeddableContent(
            Note note) {
        return note.getPlainText() != null && !note.getPlainText().isBlank();
    }

    private String buildEmbeddingText(
            Note note,
            NoteChunkData chunk) {
        return """
                Note title: %s

                Note content:
                %s
                """.formatted(resolveNoteTitle(note), chunk.content());
    }

    private String resolveNoteTitle(
            Note note) {
        if (note.getTitle() == null || note.getTitle().isBlank()) {
            return "Untitled Note";
        }

        return note.getTitle().trim();
    }
}
