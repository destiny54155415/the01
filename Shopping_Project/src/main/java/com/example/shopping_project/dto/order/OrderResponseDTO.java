package com.example.shopping_project.dto.order;

import com.example.shopping_project.entity.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {

    private Long orderId;
    private String orderNumber;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private String recipient;
    private String shippingAddress;
    private LocalDateTime orderedAt;
    private List<OrderItemResponseDTO> orderItems;

    public OrderResponseDTO(Long orderId, String orderNumber,
                            BigDecimal totalPrice, OrderStatus orderStatus,
                            String recipient, String shippingAddress,
                            LocalDateTime orderedAt,
                            List<OrderItemResponseDTO> orderItems) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.recipient = recipient;
        this.shippingAddress = shippingAddress;
        this.orderedAt = orderedAt;
        this.orderItems = orderItems;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public List<OrderItemResponseDTO> getOrderItems() {
        return orderItems;
    }
}