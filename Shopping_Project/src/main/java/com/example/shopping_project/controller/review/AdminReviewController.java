
package com.example.shopping_project.controller.review;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.review.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 관리자 권한 확인
    private boolean isAdmin(HttpSession session) {
        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        return loginUser != null
                && "ADMIN".equals(loginUser.getRole());
    }

    // 관리자 리뷰 목록 및 검색
    @GetMapping
    public String reviewList(
            @RequestParam(required = false) String keyword,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute(
                    "reviews",
                    reviewService.searchReviews(keyword.trim())
            );
        } else {
            model.addAttribute(
                    "reviews",
                    reviewService.getAllReviewList()
            );
        }

        model.addAttribute("keyword", keyword);

        return "admin/review-list";
    }

    // 관리자 리뷰 삭제
    @PostMapping("/{reviewId}/delete")
    public String deleteReview(
            @PathVariable Long reviewId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            reviewService.deleteReviewByAdmin(reviewId);
            redirectAttributes.addFlashAttribute(
                    "message", "리뷰가 삭제되었습니다."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        }

        return "redirect:/admin/reviews";
    }
}
