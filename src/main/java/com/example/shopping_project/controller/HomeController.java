package com.example.shopping_project.controller;

import com.example.shopping_project.service.product.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    // 메인 페이지 — 추천 상품은 DB 상품 목록 일부
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", productService.findBestsellers(4));
        return "index";
    }

    // 로그인 페이지 (templates/member)
    @GetMapping("/login")
    public String login() {
        return "member/login";
    }

    // 회원가입 페이지 (templates/member)
    @GetMapping("/signup")
    public String signup() {
        return "member/signup";
    }
}
