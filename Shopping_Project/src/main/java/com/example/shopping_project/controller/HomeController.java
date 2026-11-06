package com.example.shopping_project.controller;

import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.PriceRange;
import com.example.shopping_project.entity.productentity.ProductSort;
import com.example.shopping_project.service.productservice.ProductService;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) PetType petType,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) Boolean inStockOnly,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        ProductSort selectedSort = ProductSort.from(sort);
        boolean stockOnly = Boolean.TRUE.equals(inStockOnly);
        model.addAttribute("result", productService.searchActive(
                keyword, categoryId, petType, minPrice, maxPrice, selectedSort, stockOnly, page));
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("petType", petType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedPriceRange",
                PriceRange.fromBounds(minPrice, maxPrice).orElse(null));
        model.addAttribute("sort", selectedSort.name());
        model.addAttribute("selectedSort", selectedSort);
        model.addAttribute("sortOptions", ProductSort.values());
        model.addAttribute("inStockOnly", stockOnly);
        model.addAttribute("pageTitle", "전체 상품");
        return "index";
    }
}
