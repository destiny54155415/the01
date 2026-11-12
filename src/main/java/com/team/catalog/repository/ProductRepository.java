package com.team.catalog.repository;

import com.team.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    long countByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);
}
