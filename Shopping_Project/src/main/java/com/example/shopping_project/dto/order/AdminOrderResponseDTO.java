package com.example.shopping_project.dto.order;

import com.example.shopping_project.entity.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminOrderResponseDTO {

    private Long orderId;
    private String orderNumber;
    private String memberName;
    private String recipient;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private LocalDateTime orderedAt;

    public AdminOrderResponseDTO(
            Long orderId,
            String orderNumber,
            String memberName,
            String recipient,
            BigDecimal totalPrice,
            OrderStatus orderStatus,
            LocalDateTime orderedAt) {

        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.memberName = memberName;
        this.recipient = recipient;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.orderedAt = orderedAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getRecipient() {
        return recipient;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }
}