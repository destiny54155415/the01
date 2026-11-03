package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.service.productservice.CategoryService;
import com.example.shopping_project.service.productservice.ProductService;
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
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<ProductEntity> result = productService.searchActive(keyword, categoryId, petType, page);
        CategoryEntity selectedCategory = categoryId == null ? null : categoryService.getActiveCategory(categoryId);

        model.addAttribute("result", result);
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("petType", petType);
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("pageTitle", buildTitle(keyword, selectedCategory));
        return "product/list";
    }

    @GetMapping("/products/{productId}")
    public String detail(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.getActiveProduct(productId));
        return "product/detail";
    }

    private String buildTitle(String keyword, CategoryEntity selectedCategory) {
        if (keyword != null && !keyword.isBlank()) {
            return keyword.trim() + " 검색 결과";
        }
        if (selectedCategory != null) {
            return selectedCategory.getCategoryName();
        }
        return "전체 상품";
    }
}
