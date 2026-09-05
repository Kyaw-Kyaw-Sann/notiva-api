package com.kyawhsan.notiva.entity;

import com.kyawhsan.notiva.enums.MessageRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_messages", indexes = {
        @Index(name = "idx_ai_messages_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_ai_messages_conversation_created_at", columnList = "conversation_id, created_at") })
public class AiMessage extends CreatedAtEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "conversation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ai_messages_conversation"))
    private AiConversation conversation;

}