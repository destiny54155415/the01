package com.example.shopping_project.dto.order;

import com.example.shopping_project.entity.order.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminOrderDetailDTO {

    private Long orderId;
    private String orderNumber;
    private String memberName;
    private String recipient;
    private String shippingAddress;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private LocalDateTime orderedAt;
    private List<OrderItemResponseDTO> orderItems;

    public AdminOrderDetailDTO(
            Long orderId,
            String orderNumber,
            String memberName,
            String recipient,
            String shippingAddress,
            BigDecimal totalPrice,
            OrderStatus orderStatus,
            LocalDateTime orderedAt,
            List<OrderItemResponseDTO> orderItems) {

        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.memberName = memberName;
        this.recipient = recipient;
        this.shippingAddress = shippingAddress;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.orderedAt = orderedAt;
        this.orderItems = orderItems;
    }

}