package com.example.shopping_project.entity.order;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
public class Order {

    // Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // 주문한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 주문 고유 번호
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    // 총 주문 금액
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    // 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    // 받는 사람
    @Column(name = "recipient", nullable = false)
    private String recipient;

    // 배송 주소
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    // 주문 상품 목록
    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 주문 날짜
    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    // 수정 날짜
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Order() {
    }

    // 주문 생성용 생성자
    public Order(MemberEntity member, String orderNumber,
                 BigDecimal totalPrice, String recipient,
                 String shippingAddress) {
        this.member = member;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.recipient = recipient;
        this.shippingAddress = shippingAddress;
        this.orderStatus = OrderStatus.ORDERED;
    }

    @PrePersist
    protected void onCreate() {
        orderedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (orderStatus == null) {
            orderStatus = OrderStatus.ORDERED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 주문 상품 추가
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 주문 상태 변경
    public void changeOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

}