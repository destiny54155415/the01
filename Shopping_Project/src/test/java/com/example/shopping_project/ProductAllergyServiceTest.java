package com.example.shopping_project;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.service.productservice.ProductAllergyService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductAllergyServiceTest {

    private final ProductAllergyService service = new ProductAllergyService();

    @Test
    void filtersProductsContainingSelectedAllergens() {
        CategoryEntity category = new CategoryEntity("사료", 1);
        ProductEntity chicken = new ProductEntity(
                category, "강아지 닭고기", "설명", new BigDecimal("1000"), 1, PetType.DOG, null);
        chicken.setAllergens("닭고기");
        ProductEntity potato = new ProductEntity(
                category, "감자 간식", "설명", new BigDecimal("1000"), 1, PetType.DOG, null);
        potato.setAllergens("감자");

        assertThat(service.filterSafeProducts(List.of(chicken, potato), List.of("닭고기")))
                .containsExactly(potato);
        assertThat(service.filterUnsafeProducts(List.of(chicken, potato), List.of("닭고기")))
                .containsExactly(chicken);
    }
}
