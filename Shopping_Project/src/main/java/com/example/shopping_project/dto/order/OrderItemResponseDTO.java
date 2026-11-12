package com.example.shopping_project.dto.order;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemResponseDTO {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;

    public OrderItemResponseDTO(Long orderItemId, Long productId,
                                String productName, BigDecimal unitPrice,
                                Integer quantity, BigDecimal totalPrice) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

}