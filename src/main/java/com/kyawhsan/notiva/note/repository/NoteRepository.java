package com.kyawhsan.notiva.note.repository;

import com.kyawhsan.notiva.note.entity.Category;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

        Optional<Note> findByIdAndUser(
                        Long id,
                        User user);

        Optional<Note> findByIdAndUserAndDeletedAtIsNull(
                        Long id,
                        User user);

        List<Note> findAllByUserAndDeletedAtIsNullOrderByUpdatedAtDesc(
                        User user);

        List<Note> findAllByUserAndDeletedAtIsNotNullOrderByDeletedAtDesc(
                        User user);

        List<Note> findAllByUserAndCategoryAndDeletedAtIsNullOrderByUpdatedAtDesc(
                        User user,
                        Category category);

        List<Note> findAllByUserAndCategoryIsNullAndDeletedAtIsNullOrderByUpdatedAtDesc(
                        User user);

        List<Note> findAllByUserAndPinnedTrueAndDeletedAtIsNullOrderByUpdatedAtDesc(
                        User user);

        List<Note> findAllByUserAndFavoriteTrueAndDeletedAtIsNullOrderByUpdatedAtDesc(
                        User user);

        boolean existsByCategory(
                        Category category);

        @Modifying
        @Query("""
                        UPDATE Note note
                        SET note.category = null
                        WHERE note.category = :category
                        """)
        int clearCategoryFromNotes(
                        @Param("category")
                        Category category);

        void deleteAllByUser(
                        User user);

        Optional<Note> findByIdAndUserAndDeletedAtIsNotNull(
                        Long id,
                        User user);

        List<Note> findAllByUser(
                        User user);
}