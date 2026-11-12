package com.example.shopping_project.entity.order;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.entity.order.OrderItem;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    // 리뷰 작성 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 리뷰 대상 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    // 구매한 주문 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    // 별점 (1~5)
    @Column(nullable = false)
    private Integer rating;

    // 리뷰 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 작성 날짜
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 수정 날짜
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Review() {
    }

    // 리뷰 생성용 생성자
    public Review(MemberEntity member, ProductEntity product,
                  OrderItem orderItem, Integer rating, String content) {
        this.member = member;
        this.product = product;
        this.orderItem = orderItem;
        this.rating = rating;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 리뷰 수정
    public void updateReview(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    // Getter
    public Long getReviewId() {
        return reviewId;
    }

    public MemberEntity getMember() {
        return member;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public Integer getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}