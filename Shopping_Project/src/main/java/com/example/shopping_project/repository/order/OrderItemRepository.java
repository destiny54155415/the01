package com.example.shopping_project.repository.order;

import com.example.shopping_project.entity.order.Order;
import com.example.shopping_project.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 특정 주문에 포함된 상품 목록 조회
    List<OrderItem> findByOrder(Order order);
}