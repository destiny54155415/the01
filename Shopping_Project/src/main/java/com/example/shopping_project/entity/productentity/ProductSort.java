package com.example.shopping_project.entity.productentity;

import org.springframework.data.domain.Sort;

public enum ProductSort {
    POPULAR("인기순", Sort.by(Sort.Direction.DESC, "salesCount", "productId")),
    LATEST("최신순", Sort.by(Sort.Direction.DESC, "createdAt", "productId")),
    PRICE_ASC("낮은 가격순", Sort.by(Sort.Direction.ASC, "price", "productId")),
    PRICE_DESC("높은 가격순", Sort.by(Sort.Direction.DESC, "price", "productId"));

    private final String label;
    private final Sort sort;

    ProductSort(String label, Sort sort) {
        this.label = label;
        this.sort = sort;
    }

    public String getLabel() {
        return label;
    }

    public Sort getSort() {
        return sort;
    }

    public static ProductSort from(String value) {
        if (value == null || value.isBlank()) {
            return POPULAR;
        }
        try {
            return ProductSort.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return POPULAR;
        }
    }
}
