package com.example.shopping_project.entity.productentity;

import java.math.BigDecimal;

public enum PriceRange {
    UNDER_10000("1만원 미만", "0", "10000"),
    FROM_10000_TO_30000("1만원 ~ 3만원", "10000", "30000"),
    FROM_30000_TO_50000("3만원 ~ 5만원", "30000", "50000"),
    FROM_50000("5만원 이상", "50000", "999999999");

    private final String label;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;

    PriceRange(String label, String minPrice, String maxPrice) {
        this.label = label;
        this.minPrice = new BigDecimal(minPrice);
        this.maxPrice = new BigDecimal(maxPrice);
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
}
