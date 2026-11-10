package com.example.shopping_project.service.cartitem;

import com.example.shopping_project.entity.cartitem.CartItemEntity;
import com.example.shopping_project.service.petservice.PetService;
import com.example.shopping_project.service.productservice.AllergenLabels;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CartAllergyFilterService {

    private final PetService petService;

    public CartAllergyFilterService(PetService petService) {
        this.petService = petService;
    }

    public CartAllergyFilterResult filter(Long memberId, List<CartItemEntity> cartItems, boolean showUnsafe) {
        Set<String> allergies = petService.getAllergyLabelsForMember(memberId);
        List<CartItemEntity> safe = new ArrayList<>();
        List<CartItemEntity> unsafe = new ArrayList<>();
        if (cartItems != null) {
            for (CartItemEntity item : cartItems) {
                if (AllergenLabels.productContainsAny(item.getProduct(), allergies)) {
                    unsafe.add(item);
                } else {
                    safe.add(item);
                }
            }
        }
        List<CartItemEntity> visible = showUnsafe || allergies.isEmpty()
                ? List.copyOf(cartItems == null ? List.of() : cartItems)
                : List.copyOf(safe);
        return new CartAllergyFilterResult(allergies, safe, unsafe, visible);
    }

    public record CartAllergyFilterResult(
            Set<String> allergyLabels,
            List<CartItemEntity> safeItems,
            List<CartItemEntity> unsafeItems,
            List<CartItemEntity> visibleItems
    ) {
    }
}
