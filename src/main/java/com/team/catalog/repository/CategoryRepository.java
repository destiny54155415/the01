package com.team.catalog.repository;

import com.team.catalog.domain.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = "children")
    List<Category> findByParentIsNullOrderBySortOrderAscIdAsc();

    @EntityGraph(attributePaths = "parent")
    Optional<Category> findWithParentById(Long id);

    boolean existsByParentIdAndName(Long parentId, String name);

    boolean existsByParentIsNullAndName(String name);

    boolean existsByParentId(Long parentId);
}
