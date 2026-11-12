package com.example.shopping_project.repository.cartitem;

import com.example.shopping_project.entity.cartitem.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    // 특정 회원의 장바구니 목록 조회
    List<CartItemEntity> findByMemberId(Long memberId);

    // 특정 회원과 상품 조합으로 장바구니 항목 조회 (중복 담기 체크용)

    Optional<CartItemEntity> findByMemberIdAndProduct_ProductId(Long memberId, Long productId);

}