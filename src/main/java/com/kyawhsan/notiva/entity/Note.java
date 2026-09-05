package com.kyawhsan.notiva.entity;

import com.kyawhsan.notiva.enums.NoteBackgroundColor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notes", indexes = { @Index(name = "idx_notes_user_id", columnList = "user_id"),
        @Index(name = "idx_notes_category_id", columnList = "category_id"),
        @Index(name = "idx_notes_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_notes_user_deleted_at", columnList = "user_id, deleted_at") })
public class Note extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(name = "plain_text", nullable = false, columnDefinition = "TEXT")
    private String plainText;

    @Enumerated(EnumType.STRING) @Column(name = "background_color", nullable = false, length = 20)
    private NoteBackgroundColor backgroundColor = NoteBackgroundColor.DEFAULT;

    @Column(nullable = false)
    private boolean pinned = false;

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notes_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_notes_category"))
    private Category category;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void moveToRecycleBin() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}