package com.inventory.productservice.controller;

import com.inventory.productservice.dto.request.CreateCategoryRequest;
import com.inventory.productservice.dto.request.UpdateCategoryRequest;
import com.inventory.productservice.dto.response.ApiResponse;
import com.inventory.productservice.dto.response.CategoryResponse;
import com.inventory.productservice.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            Authentication authentication) {
        CategoryResponse category = categoryService.createCategory(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Category created successfully", category, java.time.LocalDateTime.now(), "", HttpStatus.CREATED.value()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Category fetched successfully", category, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categories fetched successfully", categories, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request,
            Authentication authentication) {
        CategoryResponse category = categoryService.updateCategory(id, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Category updated successfully", category, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> deactivateCategory(
            @PathVariable Long id,
            Authentication authentication) {
        CategoryResponse category = categoryService.deactivateCategory(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Category deactivated successfully", category, java.time.LocalDateTime.now(), "", HttpStatus.OK.value()));
    }
}
