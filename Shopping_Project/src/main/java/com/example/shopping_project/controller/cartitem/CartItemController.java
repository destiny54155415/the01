package com.example.shopping_project.controller.cartitem;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.cartitem.CartItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * 최종 통합본 CartItemController 와 동일한 매핑.
 */
@Controller
@RequestMapping("/cart")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping
    public String viewCart(@SessionAttribute(name = "loginUser", required = false) MemberEntity loginUser,
                           Model model) {
        if (loginUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("cartItems", cartItemService.getCartList(loginUser.getId()));
        return "cart/list";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addCart(@RequestParam Long productId,
                                          @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                                          @SessionAttribute(name = "loginUser", required = false) MemberEntity loginUser) {
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용해주세요.");
        }
        cartItemService.addCart(loginUser.getId(), productId, quantity);
        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    @PostMapping("/update/{id}")
    public String updateCartCount(@PathVariable("id") Long cartItemId,
                                  @RequestParam int quantity) {
        cartItemService.updateQuantity(cartItemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/delete/{id}")
    public String deleteCartItem(@PathVariable("id") Long cartItemId) {
        cartItemService.removeCartItem(cartItemId);
        return "redirect:/cart";
    }
}
