package com.kyawhsan.notiva.ai.entity;

import com.kyawhsan.notiva.common.persistence.BaseEntity;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.ai.enums.ConversationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_conversations", indexes = {
        @Index(name = "idx_ai_conversations_user_id", columnList = "user_id"),
        @Index(name = "idx_ai_conversations_note_id", columnList = "note_id"),
        @Index(name = "idx_ai_conversations_user_updated_at", columnList = "user_id, updated_at") })
public class AiConversation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ConversationType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ai_conversations_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "note_id", foreignKey = @ForeignKey(name = "fk_ai_conversations_note"))
    private Note note;
}
