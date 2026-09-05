package com.kyawhsan.notiva.note.entity;

import com.kyawhsan.notiva.common.persistence.CreatedAtEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "note_versions", indexes = {
        @Index(name = "idx_note_versions_note_id", columnList = "note_id"),
        @Index(name = "idx_note_versions_note_created_at", columnList = "note_id, created_at") })
public class NoteVersion extends CreatedAtEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(name = "plain_text", nullable = false, columnDefinition = "TEXT")
    private String plainText;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "note_id", nullable = false, foreignKey = @ForeignKey(name = "fk_note_versions_note"))
    private Note note;
}
