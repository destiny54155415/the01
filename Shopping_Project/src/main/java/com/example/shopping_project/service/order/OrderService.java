package com.example.shopping_project.service.order;

import com.example.shopping_project.dto.order.OrderCreateRequestDTO;
import com.example.shopping_project.dto.order.OrderItemRequestDTO;
import com.example.shopping_project.dto.order.OrderItemResponseDTO;
import com.example.shopping_project.dto.order.OrderResponseDTO;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.order.Order;
import com.example.shopping_project.entity.order.OrderItem;
import com.example.shopping_project.entity.order.OrderStatus;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.repository.order.OrderItemRepository;
import com.example.shopping_project.repository.order.OrderRepository;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import com.example.shopping_project.dto.order.AdminOrderResponseDTO;
import com.example.shopping_project.dto.order.AdminOrderDetailDTO;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository,
                        EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
    }

    // 주문 생성
    public OrderResponseDTO createOrder(Long memberId,
                                        OrderCreateRequestDTO request) {

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("주문 상품이 없습니다.");
        }

        if (request.getRecipient() == null || request.getRecipient().isBlank()
                || request.getShippingAddress() == null
                || request.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("배송 정보를 입력해주세요.");
        }

        // 같은 상품이 중복으로 들어오는 요청 방지
        Set<Long> productIds = new HashSet<>();

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        MemberEntity member =
                entityManager.getReference(MemberEntity.class, memberId);

        // 상품 조회 및 재고 확인, 금액 계산
        for (OrderItemRequestDTO itemRequest : request.getOrderItems()) {

            Long productId = itemRequest.getProductId();
            Integer quantity = itemRequest.getQuantity();

            if (productId == null || quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("잘못된 주문 상품 정보입니다.");
            }

            if (!productIds.add(productId)) {
                throw new IllegalArgumentException("동일한 상품은 한 번만 입력해주세요.");
            }

            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("상품을 찾을 수 없습니다."));

            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new IllegalArgumentException("판매 중인 상품이 아닙니다.");
            }

            if (product.getStock() < quantity) {
                throw new IllegalArgumentException(
                        product.getProductName() + "의 재고가 부족합니다.");
            }

            // 상품 가격 × 수량
            BigDecimal itemPrice =
                    product.getPrice().multiply(BigDecimal.valueOf(quantity));

            totalPrice = totalPrice.add(itemPrice);

            // 주문 상품 생성
            OrderItem orderItem = new OrderItem(product, quantity);
            orderItems.add(orderItem);

            // 재고 차감
            product.setStock(product.getStock() - quantity);
        }

        // 주문 생성
        String orderNumber = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();

        Order order = new Order(
                member,
                orderNumber,
                totalPrice,
                request.getRecipient(),
                request.getShippingAddress()
        );

        // 주문과 주문 상품 연결
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        // 주문 저장 (Order의 cascade 설정으로 OrderItem도 저장)
        Order savedOrder = orderRepository.save(order);

        return convertToDTO(savedOrder);
    }

    // 회원별 주문 내역 조회
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrderList(Long memberId) {

        MemberEntity member =
                entityManager.getReference(MemberEntity.class, memberId);

        List<Order> orders =
                orderRepository.findByMemberOrderByOrderedAtDesc(member);

        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetail(Long orderId, Long memberId) {

        MemberEntity member =
                entityManager.getReference(MemberEntity.class, memberId);

        Order order = orderRepository.findByOrderIdAndMember(orderId, member)
                .orElseThrow(() ->
                        new IllegalArgumentException("주문 내역을 찾을 수 없습니다."));

        return convertToDTO(order);
    }

    // 주문 취소
    public void cancelOrder(Long orderId, Long memberId) {

        // 로그인한 회원 조회
        MemberEntity member =
                entityManager.getReference(MemberEntity.class, memberId);

        // 본인의 주문인지 확인
        Order order = orderRepository.findByOrderIdAndMember(orderId, member)
                .orElseThrow(() ->
                        new IllegalArgumentException("주문 내역을 찾을 수 없습니다."));

        // 주문 상태 확인
        if (order.getOrderStatus() != OrderStatus.ORDERED) {
            throw new IllegalArgumentException(
                    "주문 접수 상태에서만 취소할 수 있습니다.");
        }

        // 주문 상품 재고 복구
        for (OrderItem item : order.getOrderItems()) {

            ProductEntity product = item.getProduct();

            if (product == null) {
                throw new IllegalStateException("상품 정보를 찾을 수 없습니다.");
            }

            product.setStock(product.getStock() + item.getQuantity());
        }

        // 주문 상태 변경
        order.changeOrderStatus(OrderStatus.CANCELLED);
    }
    // 관리자용 전체 주문 내역 조회
    @Transactional(readOnly = true)
    public List<AdminOrderResponseDTO> getAllOrderList() {

        List<Order> orders =
                orderRepository.findAllByOrderByOrderedAtDesc();

        return orders.stream()
                .map(order -> new AdminOrderResponseDTO(
                        order.getOrderId(),
                        order.getOrderNumber(),
                        order.getMember().getName(),
                        order.getRecipient(),
                        order.getTotalPrice(),
                        order.getOrderStatus(),
                        order.getOrderedAt()
                ))
                .collect(Collectors.toList());
    }

    // 관리자용 주문 상태별 조회
    @Transactional(readOnly = true)
    public List<AdminOrderResponseDTO> getOrderListByStatus(OrderStatus status) {

        List<Order> orders =
                orderRepository.findByOrderStatusOrderByOrderedAtDesc(status);

        return orders.stream()
                .map(order -> new AdminOrderResponseDTO(
                        order.getOrderId(),
                        order.getOrderNumber(),
                        order.getMember().getName(),
                        order.getRecipient(),
                        order.getTotalPrice(),
                        order.getOrderStatus(),
                        order.getOrderedAt()
                ))
                .collect(Collectors.toList());
    }

    // 관리자용 주문번호 검색
    @Transactional(readOnly = true)
    public List<AdminOrderResponseDTO> searchOrders(
            String keyword, OrderStatus status) {

        List<Order> orders;

        if (status == null) {
            orders = orderRepository
                    .findByOrderNumberContainingIgnoreCaseOrderByOrderedAtDesc(keyword);
        } else {
            orders = orderRepository
                    .findByOrderStatusAndOrderNumberContainingIgnoreCaseOrderByOrderedAtDesc(
                            status, keyword
                    );
        }

        return orders.stream()
                .map(order -> new AdminOrderResponseDTO(
                        order.getOrderId(),
                        order.getOrderNumber(),
                        order.getMember().getName(),
                        order.getRecipient(),
                        order.getTotalPrice(),
                        order.getOrderStatus(),
                        order.getOrderedAt()
                ))
                .collect(Collectors.toList());
    }
    // Entity를 DTO로 변환
    private OrderResponseDTO convertToDTO(Order order) {

        List<OrderItemResponseDTO> itemDTOs =
                order.getOrderItems().stream()
                        .map(item -> new OrderItemResponseDTO(
                                item.getOrderItemId(),
                                item.getProduct().getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getTotalPrice()
                        ))
                        .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getRecipient(),
                order.getShippingAddress(),
                order.getOrderedAt(),
                itemDTOs
        );
    }

    // 관리자용 주문 상세 조회
    @Transactional(readOnly = true)
    public AdminOrderDetailDTO getAdminOrderDetail(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("주문 내역을 찾을 수 없습니다."));

        List<OrderItemResponseDTO> itemDTOs =
                order.getOrderItems().stream()
                        .map(item -> new OrderItemResponseDTO(
                                item.getOrderItemId(),
                                item.getProduct().getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getTotalPrice()
                        ))
                        .collect(Collectors.toList());

        return new AdminOrderDetailDTO(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getMember().getName(),
                order.getRecipient(),
                order.getShippingAddress(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getOrderedAt(),
                itemDTOs
        );
    }

    // 관리자용 주문 상태 변경
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("주문 내역을 찾을 수 없습니다."));

        OrderStatus currentStatus = order.getOrderStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("변경할 주문 상태를 선택해주세요.");
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("취소된 주문은 변경할 수 없습니다.");
        }

        if (currentStatus == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("배송 완료된 주문은 변경할 수 없습니다.");
        }

        boolean validTransition =
                (currentStatus == OrderStatus.ORDERED
                        && newStatus == OrderStatus.PREPARING)
                        ||
                        (currentStatus == OrderStatus.PREPARING
                                && newStatus == OrderStatus.SHIPPING)
                        ||
                        (currentStatus == OrderStatus.SHIPPING
                                && newStatus == OrderStatus.COMPLETED);

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "현재 주문 상태에서 변경할 수 없는 상태입니다.");
        }

        order.changeOrderStatus(newStatus);
    }
    // 관리자 대시보드 - 전체 주문 건수
    @Transactional(readOnly = true)
    public long getTotalOrderCount() {
        return orderRepository.count();
    }

    // 관리자 대시보드 - 오늘 주문 건수
    @Transactional(readOnly = true)
    public long getTodayOrderCount() {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return orderRepository
                .countByOrderedAtGreaterThanEqualAndOrderedAtLessThan(start, end);
    }

    // 관리자 대시보드 - 취소 제외 총 매출
    @Transactional(readOnly = true)
    public BigDecimal getTotalSales() {
        return orderRepository
                .getTotalSalesExcludingStatus(OrderStatus.CANCELLED);
    }

    // 관리자 대시보드 - 배송 처리 전 주문 건수
    @Transactional(readOnly = true)
    public long getPendingDeliveryCount() {

        List<OrderStatus> statuses = List.of(
                OrderStatus.ORDERED,
                OrderStatus.PREPARING
        );

        return orderRepository.countByOrderStatusIn(statuses);
    }
}