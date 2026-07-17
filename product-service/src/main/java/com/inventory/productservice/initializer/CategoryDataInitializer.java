package com.inventory.productservice.initializer;

import com.inventory.productservice.entity.Category;
import com.inventory.productservice.enums.CategoryStatus;
import com.inventory.productservice.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            List<String> defaultCategories = List.of(
                    "Electronics", "Furniture", "Office Supplies",
                    "Networking", "Accessories", "Stationery", "Others"
            );
            for (String categoryName : defaultCategories) {
                Category category = new Category(categoryName, "SYSTEM");
                if (categoryName.equals("Others")) {
                    category.setProtectedCategory(true);
                }
                categoryRepository.save(category);
            }
            System.out.println("Default categories initialized");
        }
    }
}
