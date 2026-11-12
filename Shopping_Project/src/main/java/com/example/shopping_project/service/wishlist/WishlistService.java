package com.example.shopping_project.service.wishlist; // 본인 프로젝트 패키지 경로에 맞게 수정하세요

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.entity.wishlist.Wishlist;
import com.example.shopping_project.repository.memberrepository.MemberRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import com.example.shopping_project.repository.wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    /**
     * 찜하기 토글 (이미 되어 있으면 취소, 안 되어 있으면 추가)
     */
    public boolean toggleWishlist(Long memberId, Long productId) {
        // 이미 찜한 기록이 있는지 확인

        Wishlist existingWishlist = wishlistRepository.findByMemberIdAndProduct_ProductId(memberId, productId).orElse(null);


        if (existingWishlist != null) {
            // 이미 존재한다면 찜 취소 (삭제)
            wishlistRepository.delete(existingWishlist);
            return false; // 찜 해제됨
        } else {
            // 존재하지 않는다면 찜 추가
            MemberEntity member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

            Wishlist wishlist = Wishlist.createWishlist(member, product);
            wishlistRepository.save(wishlist);
            return true; // 찜 등록됨
        }
    }

    /**
     * 특정 회원의 위시리스트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Wishlist> getWishlistByMember(Long memberId) {
        return wishlistRepository.findByMember_Id(memberId);
    }

    @Transactional
    public void deleteWishlist(Long wishlistId) {
        wishlistRepository.deleteById(wishlistId);
    }
    /**
     * 특정 회원이 해당 상품을 찜했는지 여부 확인
     */
    @Transactional(readOnly = true) // 또는 그냥 @Transactional(readOnly = true)
    public boolean checkIsWished(Long memberId, Long productId) {
        // wishlistRepository에 이미 이 조합을 찾는 메서드가 있다면 그것을 쓰셔도 되고,
        // 없다면 아래처럼 findBy...().isPresent()로 깔끔하게 확인할 수 있습니다.
        return wishlistRepository.findByMemberIdAndProduct_ProductId(memberId, productId).isPresent();
    }

}