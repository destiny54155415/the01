package com.example.shopping_project.controller.order;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * 팀 OrderController 가 없을 때만 활성화.
 * /orders 주문내역 링크가 깨지지 않도록 빈 목록이라도 렌더링한다.
 */
@Controller
@RequestMapping("/orders")
@ConditionalOnMissingBean(name = "orderController")
public class OrderHistoryController {

    @GetMapping
    public String orderList(@SessionAttribute(name = "loginUser", required = false) MemberEntity loginUser,
                            Model model) {
        if (loginUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("orders", List.of());
        model.addAttribute("message", "주문 내역이 없습니다.");
        return "order/order-list";
    }
}
