package com.example.customerorder.domain;

public enum OrderStatus {
    PENDING("대기"),
    PAID("결제완료"),
    SHIPPING("배송중"),
    COMPLETED("완료"),
    CANCELLED("취소");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
