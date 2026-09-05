package com.kyawhsan.notiva.note.entity;

import com.kyawhsan.notiva.common.persistence.BaseEntity;
import com.kyawhsan.notiva.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_categories_user_name", columnNames = { "user_id",
                "name" }) }, indexes = {
                        @Index(name = "idx_categories_user_id", columnList = "user_id") })
public class Category extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_categories_user"))
    private User user;
}
