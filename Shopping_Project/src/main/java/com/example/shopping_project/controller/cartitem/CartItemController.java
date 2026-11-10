package com.example.shopping_project.controller.cartitem;

import com.example.shopping_project.entity.cartitem.CartItemEntity;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.cartitem.CartAllergyFilterService;
import com.example.shopping_project.service.cartitem.CartItemService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class CartItemController {

    private final CartItemService cartItemService;
    private final CartAllergyFilterService cartAllergyFilterService;

    public CartItemController(CartItemService cartItemService,
                              CartAllergyFilterService cartAllergyFilterService) {
        this.cartItemService = cartItemService;
        this.cartAllergyFilterService = cartAllergyFilterService;
    }

    @GetMapping("/cart")
    public String viewCart(@SessionAttribute(value = "loginUser", required = false) MemberEntity loginUser,
                           @RequestParam(defaultValue = "false") boolean showUnsafe,
                           Model model) {
        if (loginUser == null) {
            return "redirect:/login";
        }
        List<CartItemEntity> cartItems = cartItemService.getCartList(loginUser.getId());
        var filtered = cartAllergyFilterService.filter(loginUser.getId(), cartItems, showUnsafe);

        model.addAttribute("cartItems", filtered.visibleItems());
        model.addAttribute("allCartItems", cartItems);
        model.addAttribute("safeCartItems", filtered.safeItems());
        model.addAttribute("unsafeCartItems", filtered.unsafeItems());
        model.addAttribute("unsafeCartItemIds", filtered.unsafeItems().stream()
                .map(CartItemEntity::getId)
                .collect(Collectors.toSet()));
        model.addAttribute("allergyLabels", filtered.allergyLabels());
        model.addAttribute("allergyLabelText", String.join(", ", filtered.allergyLabels()));
        model.addAttribute("showUnsafe", showUnsafe);
        model.addAttribute("filteredOutCount", filtered.unsafeItems().size());
        return "cart/list";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<String> addCart(@RequestParam Long productId,
                                          @RequestParam(defaultValue = "1") int quantity,
                                          @SessionAttribute(value = "loginUser", required = false) MemberEntity loginUser) {
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용해주세요.");
        }
        cartItemService.addCart(loginUser.getId(), productId, quantity);
        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    @PostMapping("/cart/update")
    public String updateCartCount(@RequestParam Long cartItemId, @RequestParam int quantity) {
        cartItemService.updateQuantity(cartItemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/delete")
    public String deleteCartItem(@RequestParam Long cartItemId) {
        cartItemService.removeCartItem(cartItemId);
        return "redirect:/cart";
    }
}
