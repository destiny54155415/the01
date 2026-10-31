package com.team.catalog.domain;

public enum ProductSort {
    NEWEST("신상품"),
    NAME("상품명"),
    PRICE_ASC("낮은가격"),
    PRICE_DESC("높은가격"),
    POPULAR("인기순"),
    REVIEWS("후기순");

    private final String label;

    ProductSort(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ProductSort from(String value) {
        if (value == null || value.isBlank()) {
            return NEWEST;
        }
        try {
            return ProductSort.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return NEWEST;
        }
    }
}
