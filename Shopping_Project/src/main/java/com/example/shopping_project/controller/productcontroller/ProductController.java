package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.PriceRange;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.service.productservice.CategoryService;
import com.example.shopping_project.service.productservice.ProductService;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) PetType petType,
                       @RequestParam(required = false) PriceRange priceRange,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        BigDecimal minPrice = priceRange == null ? null : priceRange.getMinPrice();
        BigDecimal maxPrice = priceRange == null ? null : priceRange.getMaxPrice();
        Page<ProductEntity> result = productService.searchActive(
                keyword, categoryId, petType, minPrice, maxPrice, page);
        CategoryEntity selectedCategory = categoryId == null ? null : categoryService.getActiveCategory(categoryId);

        model.addAttribute("result", result);
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("petType", petType);
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("priceRanges", PriceRange.values());
        model.addAttribute("pageTitle", buildTitle(keyword, selectedCategory, priceRange));
        return "products";
    }

    @GetMapping("/products/{productId}")
    public String detail(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.getActiveProduct(productId));
        return "product/detail";
    }

    private String buildTitle(String keyword, CategoryEntity selectedCategory, PriceRange priceRange) {
        if (keyword != null && !keyword.isBlank()) {
            return keyword.trim() + " 검색 결과";
        }
        if (selectedCategory != null) {
            return selectedCategory.getCategoryName();
        }
        if (priceRange != null) {
            return priceRange.getLabel() + " 상품";
        }
        return "전체 상품";
    }
}
