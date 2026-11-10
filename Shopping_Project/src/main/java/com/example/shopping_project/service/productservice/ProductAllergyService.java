package com.example.shopping_project.service.productservice;

import com.example.shopping_project.entity.productentity.AllergyIngredient;
import com.example.shopping_project.entity.productentity.ProductEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 상품 담당 공개 API.
 * 장바구니/주문 담당은 이 서비스만 호출하면 된다. (Member/Cart/Pet 코드 수정 없음)
 *
 * 예:
 * boolean unsafe = productAllergyService.containsAnyAllergen(product, List.of("닭고기"));
 * List&lt;ProductEntity&gt; safe = productAllergyService.filterSafeProducts(products, allergyLabels);
 */
@Service
public class ProductAllergyService {

    public AllergyIngredient[] allIngredients() {
        return AllergyIngredient.values();
    }

    public boolean containsAnyAllergen(ProductEntity product, Collection<String> allergyLabels) {
        return AllergenLabels.productContainsAny(product, allergyLabels);
    }

    public List<ProductEntity> filterSafeProducts(Collection<ProductEntity> products,
                                                  Collection<String> allergyLabels) {
        List<ProductEntity> safe = new ArrayList<>();
        if (products == null) {
            return safe;
        }
        for (ProductEntity product : products) {
            if (!containsAnyAllergen(product, allergyLabels)) {
                safe.add(product);
            }
        }
        return safe;
    }

    public List<ProductEntity> filterUnsafeProducts(Collection<ProductEntity> products,
                                                    Collection<String> allergyLabels) {
        List<ProductEntity> unsafe = new ArrayList<>();
        if (products == null) {
            return unsafe;
        }
        for (ProductEntity product : products) {
            if (containsAnyAllergen(product, allergyLabels)) {
                unsafe.add(product);
            }
        }
        return unsafe;
    }

    public Set<String> normalizeLabels(Collection<String> allergyLabels) {
        return AllergenLabels.normalize(allergyLabels);
    }
}
