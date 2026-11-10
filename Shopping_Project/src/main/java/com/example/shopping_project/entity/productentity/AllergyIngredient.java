package com.example.shopping_project.entity.productentity;

/**
 * 반려동물 등록 화면의 알러지 성분과 동일한 값.
 * 상품 allergens / 펫 allergies 에 라벨 문자열로 저장한다.
 */
public enum AllergyIngredient {
    BEEF("소고기"),
    PORK("돼지고기"),
    LAMB("양고기"),
    DUCK("오리고기"),
    CHICKEN("닭고기"),
    SALMON("연어"),
    EGG("계란"),
    MILK("우유"),
    CHEESE("치즈"),
    SOY("콩"),
    SWEET_POTATO("고구마"),
    POTATO("감자");

    private final String label;

    AllergyIngredient(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AllergyIngredient fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        String trimmed = label.trim();
        for (AllergyIngredient value : values()) {
            if (value.label.equals(trimmed) || value.name().equalsIgnoreCase(trimmed)) {
                return value;
            }
        }
        return null;
    }
}
