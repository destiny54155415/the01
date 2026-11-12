package com.example.shopping_project.entity.order;

public enum OrderStatus {

    ORDERED,    // 주문 완료
    PREPARING,  // 상품 준비 중
    SHIPPING,   // 배송 중
    COMPLETED,  // 배송 완료
    CANCELLED   // 주문 취소
}