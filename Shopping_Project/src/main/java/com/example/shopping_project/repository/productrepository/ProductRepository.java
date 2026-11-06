package com.example.shopping_project.repository.productrepository;

import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.ProductEntity;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByProductName(String productName);

    @EntityGraph(attributePaths = "category")
    Page<ProductEntity> findByIsActiveTrueOrderBySalesCountDescProductIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from ProductEntity p
            where (:includeInactive = true or p.isActive = true)
              and (:categoryId is null or p.category.categoryId = :categoryId)
              and (:petType is null or p.petType = :petType)
              and p.price >= :minPrice
              and p.price < :maxPrice
              and (:inStockOnly = false or p.stock > 0)
              and (
                    :keyword is null
                    or lower(p.productName) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(p.description, '')) like lower(concat('%', :keyword, '%'))
                  )
            """)
    Page<ProductEntity> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("petType") PetType petType,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("inStockOnly") boolean inStockOnly,
                         @Param("includeInactive") boolean includeInactive,
                         Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("select p from ProductEntity p where p.productId = :productId")
    java.util.Optional<ProductEntity> findWithCategoryByProductId(@Param("productId") Long productId);

    long countByCategory_CategoryId(Long categoryId);
}
