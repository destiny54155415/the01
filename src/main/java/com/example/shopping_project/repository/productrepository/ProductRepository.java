package com.example.shopping_project.repository.productrepository;

import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductName(String productName);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByIsActiveTrueOrderBySalesCountDescProductIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where (:includeInactive = true or p.isActive = true)
              and (:categoryId is null or p.category.categoryId = :categoryId)
              and (:petType is null or p.petType = :petType)
              and (
                    :keyword is null
                    or lower(p.productName) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(p.description, '')) like lower(concat('%', :keyword, '%'))
                  )
            """)
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("petType") PetType petType,
                         @Param("includeInactive") boolean includeInactive,
                         Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.productId = :productId")
    java.util.Optional<Product> findWithCategoryByProductId(@Param("productId") Long productId);

    long countByCategory_CategoryId(Long categoryId);
}
