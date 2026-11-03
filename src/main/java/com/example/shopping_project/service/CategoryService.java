package com.example.shopping_project.service;

import com.example.shopping_project.entity.Category;
import com.example.shopping_project.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAscCategoryIdAsc();
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAscCategoryIdAsc();
    }

    public Category getActiveCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));
        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new NotFoundException("카테고리를 찾을 수 없습니다.");
        }
        return category;
    }

    public Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));
    }
}
