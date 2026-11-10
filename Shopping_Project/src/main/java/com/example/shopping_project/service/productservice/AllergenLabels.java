package com.example.shopping_project.service.productservice;

import com.example.shopping_project.entity.productentity.AllergyIngredient;
import com.example.shopping_project.entity.productentity.ProductEntity;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class AllergenLabels {

    private AllergenLabels() {
    }

    public static String join(Collection<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return null;
        }
        Set<String> normalized = normalize(labels);
        return normalized.isEmpty() ? null : String.join(",", normalized);
    }

    public static Set<String> parse(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return normalize(Arrays.asList(csv.split(",")));
    }

    public static Set<String> normalize(Collection<String> labels) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (labels == null) {
            return result;
        }
        for (String raw : labels) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            AllergyIngredient known = AllergyIngredient.fromLabel(raw.trim());
            result.add(known == null ? raw.trim() : known.getLabel());
        }
        return result;
    }

    public static boolean productContainsAny(ProductEntity product, Collection<String> allergyLabels) {
        if (product == null || allergyLabels == null || allergyLabels.isEmpty()) {
            return false;
        }
        Set<String> allergies = normalize(allergyLabels);
        Set<String> productAllergens = parse(product.getAllergens());
        if (!productAllergens.isEmpty()) {
            for (String allergy : allergies) {
                if (productAllergens.contains(allergy)) {
                    return true;
                }
            }
        }
        // allergens 미등록 상품은 이름/설명 키워드로 보조 판별
        String haystack = ((product.getProductName() == null ? "" : product.getProductName())
                + " "
                + (product.getDescription() == null ? "" : product.getDescription()))
                .toLowerCase(Locale.ROOT);
        for (String allergy : allergies) {
            if (haystack.contains(allergy.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static String inferFromText(String... texts) {
        String haystack = Arrays.stream(texts)
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.joining(" "));
        if (haystack.isBlank()) {
            return null;
        }
        LinkedHashSet<String> found = new LinkedHashSet<>();
        for (AllergyIngredient ingredient : AllergyIngredient.values()) {
            if (haystack.contains(ingredient.getLabel())) {
                found.add(ingredient.getLabel());
            }
        }
        // 흔한 별칭
        if (haystack.contains("치킨") || haystack.contains("닭")) {
            found.add(AllergyIngredient.CHICKEN.getLabel());
        }
        if (haystack.contains("비프")) {
            found.add(AllergyIngredient.BEEF.getLabel());
        }
        if (haystack.contains("연어")) {
            found.add(AllergyIngredient.SALMON.getLabel());
        }
        return join(found);
    }
}
