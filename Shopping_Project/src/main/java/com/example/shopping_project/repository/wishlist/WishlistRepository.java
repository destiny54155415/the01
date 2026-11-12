package com.example.shopping_project.repository.wishlist;

import com.example.shopping_project.entity.wishlist.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 🔴 수정: 페치 조인을 사용하여 위시리스트와 상품 정보를 한 번에 함께 조회
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.member.id = :memberId")
    List<Wishlist> findByMember_Id(@Param("memberId") Long memberId);


    Optional<Wishlist> findByMemberIdAndProduct_ProductId(Long memberId, Long productId);

    long countByProduct_ProductId(Long productId);

}