package com.kyawhsan.notiva.ai.entity;

import com.kyawhsan.notiva.common.persistence.BaseEntity;
import com.kyawhsan.notiva.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_usage", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ai_usage_user_date", columnNames = { "user_id",
                "usage_date" }) }, indexes = {
                        @Index(name = "idx_ai_usage_user_id", columnList = "user_id") })
public class AiUsage extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "request_count", nullable = false)
    private int requestCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ai_usage_user"))
    private User user;

    public void incrementRequestCount() {
        this.requestCount++;
    }

}
