package com.team.catalog.service;

import com.team.catalog.domain.Category;
import com.team.catalog.repository.CategoryRepository;
import com.team.catalog.repository.ProductRepository;
import com.team.catalog.web.CategoryForm;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> tree() {
        List<Category> roots = categoryRepository.findByParentIsNullOrderBySortOrderAscIdAsc();
        roots.forEach(root -> root.getChildren().forEach(child -> child.getChildren().size()));
        return roots;
    }

    @Transactional(readOnly = true)
    public Category get(Long id) {
        return categoryRepository.findWithParentById(id)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));
    }

    public Category save(CategoryForm form) {
        String name = form.getName().trim();
        Category parent = null;
        if (form.getParentId() != null) {
            parent = get(form.getParentId());
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("카테고리는 두 단계까지만 만들 수 있습니다.");
            }
            if (form.getId() != null && form.getId().equals(parent.getId())) {
                throw new IllegalArgumentException("자기 자신을 상위 카테고리로 둘 수 없습니다.");
            }
        }
        if (isDuplicateName(form.getId(), parent, name)) {
            throw new IllegalArgumentException("같은 위치에 같은 이름의 카테고리가 있습니다.");
        }

        Category category = form.getId() == null ? new Category(name, parent, form.getSortOrder()) : get(form.getId());
        if (form.getId() != null && parent != null && isDescendant(category, parent)) {
            throw new IllegalArgumentException("하위 카테고리를 상위로 올릴 수 없습니다.");
        }
        category.setName(name);
        category.setParent(parent);
        category.setSortOrder(form.getSortOrder());
        category.setVisible(form.isVisible());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = get(id);
        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("하위 카테고리를 먼저 지워 주세요.");
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalArgumentException("이 카테고리에 상품이 있어 지울 수 없습니다.");
        }
        categoryRepository.delete(category);
    }

    private boolean isDuplicateName(Long id, Category parent, String name) {
        boolean exists = parent == null
                ? categoryRepository.existsByParentIsNullAndName(name)
                : categoryRepository.existsByParentIdAndName(parent.getId(), name);
        if (!exists) {
            return false;
        }
        if (id == null) {
            return true;
        }
        Category current = get(id);
        return !current.getName().equals(name)
                || (parent == null && current.getParent() != null)
                || (parent != null && (current.getParent() == null || !current.getParent().getId().equals(parent.getId())));
    }

    private boolean isDescendant(Category category, Category candidateParent) {
        Category cursor = candidateParent;
        while (cursor != null) {
            if (cursor.getId().equals(category.getId())) {
                return true;
            }
            cursor = cursor.getParent();
        }
        return false;
    }
}
