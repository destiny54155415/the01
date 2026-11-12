package com.example.shopping_project.repository.review;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.entity.order.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 상품의 리뷰 목록 (최신순)
    List<Review> findByProductOrderByCreatedAtDesc(ProductEntity product);

    // 특정 회원이 작성한 리뷰 목록
    List<Review> findByMember(MemberEntity member);

    // 특정 상품의 평균 평점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double findAverageRatingByProduct(@Param("product") ProductEntity product);

    // 특정 상품의 리뷰 개수
    long countByProduct(ProductEntity product);

    // 특정 회원이 특정 상품에 리뷰를 작성했는지 확인
    boolean existsByMemberAndProduct(MemberEntity member, ProductEntity product);

    // 관리자 리뷰 전체 목록 (최신순)
    List<Review> findAllByOrderByCreatedAtDesc();

    // 관리자 리뷰 검색 (리뷰 내용)
    List<Review> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);
}