package com.example.shopping_project.entity.wishlist; // 본인 프로젝트 패키지 경로에 맞게 수정하세요

import com.example.shopping_project.entity.memberentity.MemberEntity; // 또는 Member 클래스의 실제 패키지 경로
import com.example.shopping_project.entity.productentity.ProductEntity; // Product도 마찬가지 경로 확인
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "wishlists",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_wishlists_member_product",
                        columnNames = {"member_id", "product_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "product_id", // 👈 ProductEntity에 선언된 실제 기본키 컬럼명(변수명이 아님)을 명시합니다
            nullable = false
    )
    private ProductEntity product;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성 메서드
    public static Wishlist createWishlist(MemberEntity member, ProductEntity product) {
        Wishlist wishlist = new Wishlist();
        wishlist.setMember(member);
        wishlist.setProduct(product);
        return wishlist;
    }

    
}