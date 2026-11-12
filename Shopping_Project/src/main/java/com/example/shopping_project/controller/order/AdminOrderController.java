package com.example.shopping_project.controller.order;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.order.OrderStatus;
import com.example.shopping_project.service.order.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 관리자 권한 확인
    private boolean isAdmin(HttpSession session) {
        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        return loginUser != null
                && "ADMIN".equals(loginUser.getRole());
    }

    // 관리자 주문 목록
    @GetMapping
    public String orderList(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        // 관리자 대시보드 통계
        model.addAttribute("totalOrderCount",
                orderService.getTotalOrderCount());

        model.addAttribute("todayOrderCount",
                orderService.getTodayOrderCount());

        model.addAttribute("totalSales",
                orderService.getTotalSales());

        model.addAttribute("pendingDeliveryCount",
                orderService.getPendingDeliveryCount());
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute(
                    "orders",
                    orderService.searchOrders(keyword.trim(), status)
            );
        } else if (status != null) {
            model.addAttribute(
                    "orders",
                    orderService.getOrderListByStatus(status)
            );
        } else {
            model.addAttribute(
                    "orders",
                    orderService.getAllOrderList()
            );
        }

        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        return "admin/order-list";
    }

    // 관리자 주문 상세
    @GetMapping("/{orderId}")
    public String orderDetail(
            @PathVariable Long orderId,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "order",
                orderService.getAdminOrderDetail(orderId)
        );

        model.addAttribute("statuses", OrderStatus.values());

        return "admin/order-detail";
    }

    // 관리자 주문 상태 변경
    @PostMapping("/{orderId}/status")
    public String updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus orderStatus,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            orderService.updateOrderStatus(orderId, orderStatus);
            redirectAttributes.addFlashAttribute(
                    "message", "주문 상태가 변경되었습니다."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        }

        return "redirect:/admin/orders/" + orderId;
    }
}