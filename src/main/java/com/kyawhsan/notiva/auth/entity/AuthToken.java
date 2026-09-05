package com.kyawhsan.notiva.auth.entity;

import com.kyawhsan.notiva.auth.enums.AuthTokenType;
import com.kyawhsan.notiva.entity.CreatedAtEntity;
import com.kyawhsan.notiva.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "auth_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_auth_tokens_token", columnNames = "token") }, indexes = {
                @Index(name = "idx_auth_tokens_user_type", columnList = "user_id, type"),
                @Index(name = "idx_auth_tokens_expires_at", columnList = "expires_at") })
public class AuthToken extends CreatedAtEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String token;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private AuthTokenType type;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_auth_tokens_user"))
    private User user;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public void markAsUsed() {
        this.used = true;
    }
}
