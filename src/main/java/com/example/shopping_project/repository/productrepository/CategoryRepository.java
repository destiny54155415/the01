package com.example.shopping_project.repository.productrepository;

import com.example.shopping_project.entity.productentity.CategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findByIsActiveTrueOrderByDisplayOrderAscCategoryIdAsc();

    List<CategoryEntity> findAllByOrderByDisplayOrderAscCategoryIdAsc();

    boolean existsByCategoryName(String categoryName);
}
