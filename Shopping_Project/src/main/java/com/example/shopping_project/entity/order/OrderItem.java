package com.example.shopping_project.entity.order;

import com.example.shopping_project.entity.productentity.ProductEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    // 어떤 주문에 포함된 상품인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 실제 상품 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    // 주문 당시 상품명
    @Column(name = "product_name", nullable = false)
    private String productName;

    // 주문 당시 상품 가격
    @Column(name = "unit_price", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // 주문 수량
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    protected OrderItem() {
    }

    // 주문 상품 생성
    public OrderItem(ProductEntity product, Integer quantity) {
        this.product = product;
        this.productName = product.getProductName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
    }

    // 주문 연결
    public void setOrder(Order order) {
        this.order = order;
    }

    // 상품별 주문 금액 계산
    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Getter
    public Long getOrderItemId() {
        return orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }
}