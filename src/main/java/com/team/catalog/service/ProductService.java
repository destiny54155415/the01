package com.team.catalog.service;

import com.team.catalog.domain.Category;
import com.team.catalog.domain.Product;
import com.team.catalog.domain.ProductSort;
import com.team.catalog.domain.ProductStatus;
import com.team.catalog.repository.ProductRepository;
import com.team.catalog.web.ProductForm;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    public static final int PAGE_SIZE = 8;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, Long categoryId, ProductSort sort, int page, boolean includeHidden) {
        int safePage = Math.max(page, 0);
        Specification<Product> spec = (root, query, cb) -> {
            Class<?> resultType = query.getResultType();
            if (resultType == null || (resultType != Long.class && resultType != long.class)) {
                root.fetch("category", JoinType.LEFT).fetch("parent", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (!includeHidden) {
                predicates.add(root.get("status").in(ProductStatus.ON_SALE, ProductStatus.SOLD_OUT));
                predicates.add(cb.isTrue(root.get("category").get("visible")));
                predicates.add(cb.or(
                        cb.isNull(root.get("category").get("parent")),
                        cb.isTrue(root.get("category").get("parent").get("visible"))
                ));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("brand")), like),
                        cb.like(cb.lower(root.get("summary")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("category").get("id"), categoryId),
                        cb.equal(root.get("category").get("parent").get("id"), categoryId)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return productRepository.findAll(spec, PageRequest.of(safePage, PAGE_SIZE, toSort(sort)));
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
        product.getCategory().getName();
        if (product.getCategory().getParent() != null) {
            product.getCategory().getParent().getName();
        }
        return product;
    }

    public Product view(Long id) {
        Product product = get(id);
        if (product.getStatus() == ProductStatus.HIDDEN || !product.getCategory().isVisible()) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }
        product.increaseViewCount();
        return product;
    }

    public Product save(ProductForm form) {
        Category category = categoryService.get(form.getCategoryId());
        if (category.getParent() == null) {
            throw new IllegalArgumentException("상품은 하위 카테고리에 넣습니다.");
        }
        Product product = form.getId() == null
                ? new Product(category, form.getName().trim(), form.getBrand().trim(), form.getPrice(), form.getSalePrice(), form.getSummary(), form.getStock())
                : get(form.getId());
        product.setCategory(category);
        product.setName(form.getName().trim());
        product.setBrand(form.getBrand().trim());
        product.setPrice(form.getPrice());
        Integer salePrice = form.getSalePrice();
        product.setSalePrice(salePrice != null && salePrice > 0 ? salePrice : null);
        product.setSummary(blankToNull(form.getSummary()));
        product.setDescription(blankToNull(form.getDescription()));
        product.setStock(form.getStock());
        product.setStatus(form.getStatus());
        product.syncSellingPrice();
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.delete(get(id));
    }

    public void updateRating(Long productId, int reviewCount, double averageRating) {
        get(productId).replaceRating(reviewCount, averageRating);
    }

    private Sort toSort(ProductSort sort) {
        return switch (sort) {
            case NAME -> Sort.by(Sort.Order.asc("name"), Sort.Order.desc("id"));
            case PRICE_ASC -> Sort.by(Sort.Order.asc("sellingPrice"), Sort.Order.desc("id"));
            case PRICE_DESC -> Sort.by(Sort.Order.desc("sellingPrice"), Sort.Order.desc("id"));
            case POPULAR -> Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("id"));
            case REVIEWS -> Sort.by(Sort.Order.desc("reviewCount"), Sort.Order.desc("averageRating"), Sort.Order.desc("id"));
            case NEWEST -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        };
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
