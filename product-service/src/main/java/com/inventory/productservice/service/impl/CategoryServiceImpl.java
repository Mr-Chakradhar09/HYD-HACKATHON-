package com.inventory.productservice.service.impl;

import com.inventory.productservice.dto.request.CreateCategoryRequest;
import com.inventory.productservice.dto.request.UpdateCategoryRequest;
import com.inventory.productservice.dto.response.CategoryResponse;
import com.inventory.productservice.entity.Category;
import com.inventory.productservice.enums.CategoryStatus;
import com.inventory.productservice.exception.ResourceNotFoundException;
import com.inventory.productservice.exception.DuplicateResourceException;
import com.inventory.productservice.exception.InvalidOperationException;
import com.inventory.productservice.repository.CategoryRepository;
import com.inventory.productservice.repository.ProductRepository;
import com.inventory.productservice.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, String createdBy) {
        String normalizedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Category already exists: " + normalizedName);
        }
        Category category = new Category(normalizedName, createdBy);
        category.setDescription(request.getDescription());
        category.setWarehouseId(request.getWarehouseId());
        categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Override
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<CategoryResponse> getActiveCategories(Pageable pageable) {
        return categoryRepository.findByStatus(CategoryStatus.ACTIVE, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request, String updatedBy) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        String normalizedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new DuplicateResourceException("Category name already exists: " + normalizedName);
        }
        category.setName(normalizedName);
        category.setDescription(request.getDescription());
        category.setUpdatedBy(updatedBy);
        categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse deactivateCategory(Long id, String updatedBy) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (category.isProtectedCategory()) {
            throw new InvalidOperationException("Cannot deactivate protected 'Others' category");
        }
        if (category.getStatus() == CategoryStatus.INACTIVE) {
            throw new InvalidOperationException("Category is already inactive");
        }
        category.setStatus(CategoryStatus.INACTIVE);
        category.setUpdatedBy(updatedBy);
        categoryRepository.save(category);
        return mapToResponse(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setStatus(category.getStatus());
        response.setProtectedCategory(category.isProtectedCategory());
        response.setWarehouseId(category.getWarehouseId());
        response.setCreatedBy(category.getCreatedBy());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedBy(category.getUpdatedBy());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
