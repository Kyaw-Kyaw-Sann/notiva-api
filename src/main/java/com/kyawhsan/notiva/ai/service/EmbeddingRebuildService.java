package com.kyawhsan.notiva.ai.service;

import com.kyawhsan.notiva.service.CurrentUserService;

import com.kyawhsan.notiva.ai.dto.EmbeddingRebuildResponse;
import com.kyawhsan.notiva.entity.Note;
import com.kyawhsan.notiva.entity.User;
import com.kyawhsan.notiva.repository.NoteChunkJdbcRepository;
import com.kyawhsan.notiva.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingRebuildService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingRebuildService.class);

    private final NoteRepository noteRepository;

    private final NoteChunkJdbcRepository noteChunkJdbcRepository;

    private final NoteEmbeddingService noteEmbeddingService;

    private final CurrentUserService currentUserService;

    public EmbeddingRebuildResponse rebuildMissingEmbeddings() {
        User currentUser = currentUserService.getCurrentUser();

        List<Note> activeNotes = noteRepository
                .findAllByUserAndDeletedAtIsNullOrderByUpdatedAtDesc(currentUser);

        int alreadyEmbedded = 0;
        int rebuilt = 0;
        int failed = 0;

        for (Note note : activeNotes) {
            long chunkCount = noteChunkJdbcRepository.countByNoteId(note.getId());

            if (chunkCount > 0) {
                alreadyEmbedded++;
                continue;
            }

            try {
                noteEmbeddingService.synchronizeNote(note);

                rebuilt++;

            } catch (RuntimeException exception) {
                failed++;

                log.warn("Unable to rebuild embeddings for note {}: {}", note.getId(),
                        exception.getMessage());
            }
        }

        return new EmbeddingRebuildResponse(activeNotes.size(), alreadyEmbedded, rebuilt, failed);
    }
}
