package com.example.shopping_project.controller.review;

import com.example.shopping_project.dto.review.ReviewCreateRequestDTO;
import com.example.shopping_project.dto.review.ReviewResponseDTO;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.review.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 상품별 리뷰 목록
    @GetMapping("/product/{productId}")
    public String reviewList(
            @PathVariable Long productId,
            @RequestParam(required = false) Long orderItemId,
            HttpSession session,
            Model model) {

        List<ReviewResponseDTO> reviews =
                reviewService.getReviewList(productId);

        Double averageRating =
                reviewService.getAverageRating(productId);

        long reviewCount =
                reviewService.getReviewCount(productId);

        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("productId", productId);

        // 리뷰 작성 폼 표시 여부
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        boolean alreadyReviewed = false;

        if (loginUser != null && orderItemId != null) {
            model.addAttribute("orderItemId", orderItemId);

            alreadyReviewed = reviewService.hasReview(
                    loginUser.getId(), productId
            );
        }

        model.addAttribute("alreadyReviewed", alreadyReviewed);

        return "review/review-list";
    }

    // 리뷰 작성
    @PostMapping("/create")
    public String createReview(
            @RequestParam Long productId,
            @ModelAttribute ReviewCreateRequestDTO request,
            HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        reviewService.createReview(loginUser.getId(), request);

        return "redirect:/reviews/product/" + productId;
    }

    // 마이페이지 상품평 수정 화면
    @GetMapping("/edit/{reviewId}")
    public String editReviewForm(
            @PathVariable Long reviewId,
            HttpSession session,
            Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        // 본인 리뷰인지 확인하면서 기존 리뷰 조회
        ReviewResponseDTO review =
                reviewService.getMyReview(reviewId, loginUser.getId());

        model.addAttribute("review", review);

        return "review/review-edit";
    }
    // 리뷰 수정
    @PostMapping("/update/{reviewId}")
    public String updateReview(
            @PathVariable Long reviewId,
            @RequestParam(required = false) Long productId,
            @RequestParam Integer rating,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean fromMypage,
            HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        reviewService.updateReview(
                reviewId,
                loginUser.getId(),
                rating,
                content
        );

        if (fromMypage) {
            return "redirect:/mypage/reviews";
        }

        return "redirect:/reviews/product/" + productId;
    }

    // 리뷰 삭제
    @PostMapping("/delete/{reviewId}")
    public String deleteReview(
            @PathVariable Long reviewId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "false") boolean fromMypage,
            HttpSession session) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        reviewService.deleteReview(reviewId, loginUser.getId());

        // 마이페이지에서 삭제한 경우
        if (fromMypage) {
            return "redirect:/mypage/reviews";
        }

        // 상품 상세 리뷰 페이지에서 삭제한 경우
        return "redirect:/reviews/product/" + productId;
    }
}