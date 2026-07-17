package com.inventory.productservice.service;

import com.inventory.productservice.dto.request.CreateCategoryRequest;
import com.inventory.productservice.dto.request.UpdateCategoryRequest;
import com.inventory.productservice.dto.response.CategoryResponse;
import com.inventory.productservice.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request, String createdBy);

    CategoryResponse getCategoryById(Long id);

    Page<CategoryResponse> getAllCategories(Pageable pageable);

    Page<CategoryResponse> getActiveCategories(Pageable pageable);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request, String updatedBy);

    CategoryResponse deactivateCategory(Long id, String updatedBy);
}
