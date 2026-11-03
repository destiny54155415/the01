package com.example.shopping_project.repository.productrepository;

import com.example.shopping_project.entity.productentity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrueOrderByDisplayOrderAscCategoryIdAsc();

    List<Category> findAllByOrderByDisplayOrderAscCategoryIdAsc();

    boolean existsByCategoryName(String categoryName);
}
