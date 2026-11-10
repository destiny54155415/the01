package com.example.shopping_project.service.cartitem;

import com.example.shopping_project.entity.cartitem.CartItemEntity;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.repository.cartitem.CartItemRepository;
import com.example.shopping_project.service.productservice.ProductService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartItemService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<CartItemEntity> getCartList(Long memberId) {
        return cartItemRepository.findByMemberIdWithProduct(memberId);
    }

    public void addCart(Long memberId, Long productId, int quantity) {
        ProductEntity product = productService.getActiveProduct(productId);
        cartItemRepository.findByMemberIdAndProduct_ProductId(memberId, productId).ifPresentOrElse(existing -> {
            existing.setQuantity(existing.getQuantity() + Math.max(quantity, 1));
        }, () -> cartItemRepository.save(new CartItemEntity(memberId, product, quantity)));
    }

    public void updateQuantity(Long cartItemId, int quantity) {
        CartItemEntity item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 상품을 찾을 수 없습니다."));
        item.setQuantity(quantity);
    }

    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional(readOnly = true)
    public List<CartItemEntity> getSelectedCartItems(Long memberId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return List.of();
        }
        return cartItemRepository.findByMemberIdAndIdIn(memberId, cartItemIds);
    }

    public void removeSelectedCartItems(Long memberId, List<Long> cartItemIds) {
        getSelectedCartItems(memberId, cartItemIds).forEach(cartItemRepository::delete);
    }
}
