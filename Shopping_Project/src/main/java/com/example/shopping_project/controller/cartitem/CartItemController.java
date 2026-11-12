package com.example.shopping_project.controller.cartitem;

import com.example.shopping_project.entity.cartitem.CartItemEntity;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.cartitem.CartItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    /**
     * 장바구니 페이지 조회
     */
    @GetMapping
    public String viewCart(
            @SessionAttribute(name = "loginUser", required = false) MemberEntity loginUser,
            Model model) {

        // 1. 로그인이 되어있지 않다면(세션에 loginUser가 없다면) 로그인 페이지로 이동
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 2. 로그인된 MemberEntity 객체에서 회원 PK(ID) 추출
        Long memberId = loginUser.getId();

        // 3. 해당 회원의 장바구니 목록 조회
        List<CartItemEntity> cartItems = cartItemService.getCartList(memberId);
        model.addAttribute("cartItems", cartItems);

        return "cart/list";
    }


    /**
     * 장바구니 담기 (AJAX 요청용)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addCart(
            @RequestParam("productId") Long productId,
            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
            @SessionAttribute(name = "loginUser", required = false) MemberEntity loginUser) {

        // 1. 세션에 loginUser가 없으면 비로그인 상태로 판단
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용해주세요.");
        }

        // 2. loginUser 객체에서 ID를 꺼내서 장바구니 서비스 호출
        cartItemService.addCart(loginUser.getId(), productId, quantity);

        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    /**
     * 장바구니 수량 변경
     */
    @PostMapping("/update/{id}")
    public String updateCartCount(@PathVariable("id") Long cartItemId, @RequestParam("quantity") int quantity) {
        cartItemService.updateQuantity(cartItemId, quantity);
        return "redirect:/cart";
    }

    /**
     * 장바구니 상품 삭제
     */
    @PostMapping("/delete/{id}")
    public String deleteCartItem(@PathVariable("id") Long cartItemId) {
        cartItemService.removeCartItem(cartItemId);
        return "redirect:/cart";
    }


}