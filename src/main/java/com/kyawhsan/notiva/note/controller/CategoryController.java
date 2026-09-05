package com.kyawhsan.notiva.note.controller;

import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.note.dto.CategoryRequest;
import com.kyawhsan.notiva.note.dto.CategoryResponse;
import com.kyawhsan.notiva.note.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid
            @RequestBody
            CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> response = categoryService.getCategories();

        return ResponseEntity
                .ok(ApiResponse.success("Categories retrieved successfully", response));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @PathVariable
            Long categoryId) {
        CategoryResponse response = categoryService.getCategory(categoryId);

        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", response));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable
            Long categoryId,
            @Valid
            @RequestBody
            CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);

        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable
            Long categoryId) {
        categoryService.deleteCategory(categoryId);

        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
}