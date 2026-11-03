package com.example.shopping_project.controller;

import com.example.shopping_project.service.productservice.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    /** 메인 — 인기 상품(상품 담당) */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", productService.findBestsellers(4));
        return "index";
    }
}
