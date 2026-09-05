package com.kyawhsan.notiva.note.service;

import com.kyawhsan.notiva.note.dto.CategoryRequest;
import com.kyawhsan.notiva.note.dto.CategoryResponse;
import com.kyawhsan.notiva.note.entity.Category;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.common.exception.ConflictException;
import com.kyawhsan.notiva.common.exception.ResourceNotFoundException;
import com.kyawhsan.notiva.note.repository.CategoryRepository;
import com.kyawhsan.notiva.note.repository.NoteRepository;
import com.kyawhsan.notiva.security.CurrentUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final NoteRepository noteRepository;

    @Transactional
    public CategoryResponse createCategory(
            CategoryRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        String categoryName = normalizeName(request.name());

        if (categoryRepository.existsByUserAndNameIgnoreCase(currentUser, categoryName)) {
            throw new ConflictException("A category already exists with this name");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setUser(currentUser);

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponse.from(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        User currentUser = currentUserService.getCurrentUser();

        return categoryRepository.findAllByUserOrderByNameAsc(currentUser).stream()
                .map(CategoryResponse::from).toList();
    }

    private String normalizeName(
            String name) {
        return name.trim();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(
            Long categoryId) {
        User currentUser = currentUserService.getCurrentUser();

        Category category = findCategoryByIdAndUser(categoryId, currentUser);

        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse updateCategory(
            Long categoryId,
            CategoryRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Category category = findCategoryByIdAndUser(categoryId, currentUser);

        String categoryName = normalizeName(request.name());

        if (categoryRepository.existsByUserAndNameIgnoreCaseAndIdNot(currentUser, categoryName,
                categoryId)) {

            throw new ConflictException("A category already exists with this name");
        }

        category.setName(categoryName);

        Category updatedCategory = categoryRepository.save(category);

        return CategoryResponse.from(updatedCategory);
    }

    private Category findCategoryByIdAndUser(
            Long categoryId,
            User user) {
        return categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public void deleteCategory(
            Long categoryId) {
        User currentUser = currentUserService.getCurrentUser();

        Category category = findCategoryByIdAndUser(categoryId, currentUser);

        noteRepository.clearCategoryFromNotes(category);
        categoryRepository.delete(category);
    }
}
