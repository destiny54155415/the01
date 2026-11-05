package com.example.shopping_project.entity.productentity;

import java.math.BigDecimal;
import java.util.Optional;

public enum PriceRange {
    UNDER_10000("1만원 미만", null, "10000"),
    FROM_10000_TO_30000("1만원 ~ 3만원", "10000", "30000"),
    FROM_30000_TO_50000("3만원 ~ 5만원", "30000", "50000"),
    FROM_50000("5만원 이상", "50000", null);

    private final String label;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;

    PriceRange(String label, String minPrice, String maxPrice) {
        this.label = label;
        this.minPrice = minPrice == null ? null : new BigDecimal(minPrice);
        this.maxPrice = maxPrice == null ? null : new BigDecimal(maxPrice);
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public static Optional<PriceRange> fromBounds(BigDecimal minPrice, BigDecimal maxPrice) {
        for (PriceRange range : values()) {
            if (same(range.minPrice, minPrice) && same(range.maxPrice, maxPrice)) {
                return Optional.of(range);
            }
        }
        return Optional.empty();
    }

    private static boolean same(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }
}
