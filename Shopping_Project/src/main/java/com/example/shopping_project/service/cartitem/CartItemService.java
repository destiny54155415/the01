
package com.example.shopping_project.service.cartitem;

import com.example.shopping_project.entity.cartitem.CartItemEntity;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.repository.cartitem.CartItemRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // 장바구니 담기
    @Transactional
    public void addCart(Long memberId, Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));


        Optional<CartItemEntity> existingItem = cartItemRepository.findByMemberIdAndProduct_ProductId(memberId, productId);

        // 💡 1. 최종적으로 담게 될 총 수량 계산 (기존 장바구니 수량 + 이번에 추가할 수량)
        int currentCartQuantity = existingItem.map(CartItemEntity::getQuantity).orElse(0);
        int totalQuantity = currentCartQuantity + quantity;

        // 💡 2. 재고 초과 여부 서버 검증
        if (product.getStock() < totalQuantity) {
            throw new IllegalArgumentException("재고가 부족합니다. (현재 남은 재고: " + product.getStock() + "개)");
        }


        if (existingItem.isPresent()) {
            CartItemEntity cartItem = existingItem.get();
            cartItem.setQuantity(totalQuantity); // 계산된 총 수량 반영
        } else {
            CartItemEntity cartItem = new CartItemEntity();
            cartItem.setMemberId(memberId);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    // 특정 회원의 장바구니 목록 조회
    public List<CartItemEntity> getCartList(Long memberId) {
        return cartItemRepository.findByMemberId(memberId);
    }

    // 장바구니 상품 수량 변경
    @Transactional
    public void updateQuantity(Long cartItemId, int quantity) {

        CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 상품이 존재하지 않습니다."));


        // 💡 연결된 상품 정보 가져오기
        ProductEntity product = cartItem.getProduct();

        // 💡 변경하려는 수량이 상품 재고보다 많을 경우 예외 발생
        if (product != null && product.getStock() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다. (현재 남은 재고: " + product.getStock() + "개)");
        }


        if (quantity > 0) {
            cartItem.setQuantity(quantity);
        }
    }

    // 장바구니 상품 개별 삭제
    @Transactional
    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    // 주문할 장바구니 상품 조회 (회원 소유권 확인)
    public List<CartItemEntity> getSelectedCartItems(
            Long memberId, List<Long> cartItemIds) {

        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("구매할 상품을 선택해주세요.");
        }

        Set<Long> selectedIds = new HashSet<>(cartItemIds);

        if (selectedIds.size() != cartItemIds.size()) {
            throw new IllegalArgumentException("중복된 장바구니 항목이 있습니다.");
        }

        List<CartItemEntity> cartItems = cartItemRepository.findAllById(selectedIds);

        if (cartItems.size() != selectedIds.size()) {
            throw new IllegalArgumentException("장바구니 상품을 찾을 수 없습니다.");
        }

        boolean hasOtherMemberItem = cartItems.stream()
                .anyMatch(item -> !item.getMemberId().equals(memberId));

        if (hasOtherMemberItem) {
            throw new IllegalArgumentException("잘못된 장바구니 접근입니다.");
        }

        return cartItems;
    }

    // 주문 완료 후 구매한 장바구니 상품 삭제
    @Transactional
    public void removeSelectedCartItems(Long memberId, List<Long> cartItemIds) {

        List<CartItemEntity> cartItems =
                getSelectedCartItems(memberId, cartItemIds);

        cartItemRepository.deleteAll(cartItems);
    }
}
