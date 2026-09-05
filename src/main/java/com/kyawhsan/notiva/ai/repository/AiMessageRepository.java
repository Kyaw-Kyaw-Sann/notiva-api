package com.kyawhsan.notiva.ai.repository;

import com.kyawhsan.notiva.ai.entity.AiConversation;
import com.kyawhsan.notiva.ai.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

        List<AiMessage> findAllByConversationOrderByCreatedAtAsc(
                        AiConversation conversation);

        List<AiMessage> findTop20ByConversationOrderByCreatedAtDesc(
                        AiConversation conversation);

        void deleteAllByConversation(
                        AiConversation conversation);
}