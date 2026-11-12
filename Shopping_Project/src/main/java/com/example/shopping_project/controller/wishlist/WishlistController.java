package com.example.shopping_project.controller.wishlist;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.wishlist.Wishlist;
import com.example.shopping_project.service.wishlist.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * 내 위시리스트 페이지 이동 (/wishlist 접속 시 실행)
     */
    @GetMapping
    public String wishlistPage(
            @SessionAttribute(name = "loginUser", required = false) MemberEntity loginUser,
            Model model) {

        // 로그인이 되어있지 않다면 로그인 페이지로 리다이렉트 (장바구니와 동일한 방식)
        if (loginUser == null) {
            return "redirect:/login";
        }

        Long memberId = loginUser.getId();
        List<Wishlist> wishlists = wishlistService.getWishlistByMember(memberId);

        model.addAttribute("wishlists", wishlists);
        return "wishlist/list"; // templates/wishlist/list.html 파일 경로
    }

    /**
     * 찜하기 토글 API (AJAX 요청용)
     */
    @PostMapping("/api/wishlist/{productId}")
    @ResponseBody
    public ResponseEntity<Object> toggleWishlist(
            @PathVariable Long productId,
            @SessionAttribute(name = "loginUser", required = false) MemberEntity loginUser) {

        if (loginUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long memberId = loginUser.getId();
        boolean isWished = wishlistService.toggleWishlist(memberId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isWished", isWished);
        response.put("message", isWished ? "위시리스트에 담겼습니다." : "위시리스트에서 취소되었습니다.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete/{id}")
    public String deleteWishlist(
            @PathVariable("id") Long wishlistId,
            @SessionAttribute(name = "loginUser", required = false) MemberEntity loginUser) {

        if (loginUser == null) {
            return "redirect:/login";
        }

        wishlistService.deleteWishlist(wishlistId);
        return "redirect:/wishlist";
    }

}