package com.example.customerorder.web.form;

import com.example.customerorder.domain.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class OrderForm {

    @NotNull(message = "고객을 선택하세요.")
    private Long customerId;

    @NotBlank(message = "주문번호를 입력하세요.")
    @Size(max = 50, message = "주문번호는 50자 이하로 입력하세요.")
    private String orderNumber;

    @NotNull(message = "주문 금액을 입력하세요.")
    @DecimalMin(value = "0.00", message = "주문 금액은 0 이상이어야 합니다.")
    @Digits(integer = 10, fraction = 2, message = "주문 금액은 소수점 둘째 자리까지 입력하세요.")
    private BigDecimal totalPrice;

    @NotNull(message = "주문 상태를 선택하세요.")
    private OrderStatus status = OrderStatus.PENDING;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
