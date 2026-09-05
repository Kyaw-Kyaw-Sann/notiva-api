package com.kyawhsan.notiva.note.repository;

import com.kyawhsan.notiva.note.entity.Category;
import com.kyawhsan.notiva.note.entity.Note;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.note.enums.NoteBackgroundColor;
import org.springframework.data.jpa.domain.Specification;

public final class NoteSpecifications {

    private NoteSpecifications() {
    }

    public static Specification<Note> belongsToUser(
            User user) {
        return (
                root,
                query,
                criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    public static Specification<Note> isActive() {
        return (
                root,
                query,
                criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<Note> hasKeyword(
            String keyword) {
        return (
                root,
                query,
                criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String normalizedKeyword = keyword.trim().toLowerCase();

            String searchPattern = "%" + normalizedKeyword + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("plainText")),
                            searchPattern));
        };
    }

    public static Specification<Note> hasCategory(
            Category category) {
        return (
                root,
                query,
                criteriaBuilder) -> {
            if (category == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    public static Specification<Note> isUncategorized(
            Boolean uncategorized) {
        return (
                root,
                query,
                criteriaBuilder) -> {
            if (!Boolean.TRUE.equals(uncategorized)) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.isNull(root.get("category"));
        };
    }

    public static Specification<Note> hasBackgroundColor(
            NoteBackgroundColor backgroundColor) {
        return (
                root,
                query,
                criteriaBuilder) -> {
            if (backgroundColor == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("backgroundColor"), backgroundColor);
        };
    }

    public static Specification<Note> hasPinnedState(
            Boolean pinned) {
        return (
                root,
                query,
                criteriaBuilder) -> {
            if (pinned == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("pinned"), pinned);
        };
    }

    public static Specification<Note> hasFavoriteState(
            Boolean favorite) {
        return (
                root,
                query,
                criteriaBuilder) -> {
            if (favorite == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("favorite"), favorite);
        };
    }
}