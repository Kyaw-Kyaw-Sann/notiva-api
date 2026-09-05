package com.kyawhsan.notiva.ai.repository;

import com.kyawhsan.notiva.ai.entity.AiConversation;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

        @EntityGraph(attributePaths = "note")
        Optional<AiConversation> findByIdAndUser(
                        Long id,
                        User user);

        @EntityGraph(attributePaths = "note")
        List<AiConversation> findAllByUserOrderByUpdatedAtDesc(
                        User user);

        List<AiConversation> findAllByNote(
                        Note note);

        List<AiConversation> findAllByUser(
                        User user);

        void deleteAllByNote(
                        Note note);

        void deleteAllByUser(
                        User user);
}