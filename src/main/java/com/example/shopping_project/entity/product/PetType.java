package com.example.shopping_project.entity.product;

public enum PetType {
    DOG("강아지"),
    CAT("고양이"),
    COMMON("공용");

    private final String label;

    PetType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
