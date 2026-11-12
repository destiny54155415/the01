package com.example.shopping_project.repository.order;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.order.Order;
import com.example.shopping_project.entity.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 특정 회원의 주문 내역 조회 (최신순)
    List<Order> findByMemberOrderByOrderedAtDesc(MemberEntity member);

    // 주문번호로 주문 조회
    Optional<Order> findByOrderNumber(String orderNumber);

    // 특정 회원의 특정 주문 조회
    Optional<Order> findByOrderIdAndMember(Long orderId, MemberEntity member);

    // 전체 회원의 주문 내역 조회 (최신순)
    List<Order> findAllByOrderByOrderedAtDesc();

    // 주문 상태별 전체 주문 조회 (최신순)
    List<Order> findByOrderStatusOrderByOrderedAtDesc(OrderStatus orderStatus);

    // 주문번호로 검색
    List<Order> findByOrderNumberContainingIgnoreCaseOrderByOrderedAtDesc(
            String orderNumber
    );

    // 주문 상태와 주문번호로 검색
    List<Order> findByOrderStatusAndOrderNumberContainingIgnoreCaseOrderByOrderedAtDesc(
            OrderStatus orderStatus,
            String orderNumber
    );

    // 오늘 주문 건수
    long countByOrderedAtGreaterThanEqualAndOrderedAtLessThan(
            LocalDateTime start,
            LocalDateTime end
    );

    // 특정 상태들의 주문 건수
    long countByOrderStatusIn(List<OrderStatus> statuses);

    // 취소되지 않은 주문의 총 매출
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.orderStatus <> :status")
    BigDecimal getTotalSalesExcludingStatus(
            @Param("status") OrderStatus status
    );
}