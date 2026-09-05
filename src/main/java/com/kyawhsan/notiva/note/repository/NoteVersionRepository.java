package com.kyawhsan.notiva.note.repository;

import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.note.entity.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {

        List<NoteVersion> findAllByNoteOrderByCreatedAtDesc(
                        Note note);

        List<NoteVersion> findAllByNoteOrderByCreatedAtAsc(
                        Note note);

        Optional<NoteVersion> findByIdAndNote(
                        Long id,
                        Note note);

        long countByNote(
                        Note note);

        void deleteAllByNote(
                        Note note);
}