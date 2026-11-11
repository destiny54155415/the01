package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.PriceRange;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.entity.productentity.ProductSort;
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
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false, defaultValue = "false") boolean inStockOnly,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        ProductSort selectedSort = ProductSort.from(sort);
        boolean stockOnly = inStockOnly;
        Page<ProductEntity> result = productService.searchActive(
                keyword, categoryId, petType, minPrice, maxPrice, selectedSort, stockOnly, page);
        CategoryEntity selectedCategory = categoryId == null ? null : categoryService.getActiveCategory(categoryId);
        PriceRange selectedPriceRange = PriceRange.fromBounds(minPrice, maxPrice).orElse(null);

        model.addAttribute("result", result);
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("petType", petType);
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedPriceRange", selectedPriceRange);
        model.addAttribute("priceRanges", PriceRange.values());
        model.addAttribute("sort", selectedSort.name());
        model.addAttribute("selectedSort", selectedSort);
        model.addAttribute("sortOptions", ProductSort.values());
        model.addAttribute("inStockOnly", stockOnly);
        model.addAttribute("pageTitle", buildTitle(keyword, selectedCategory, selectedPriceRange));
        return "products";
    }

    @GetMapping("/products/{productId}")
    public String detail(@PathVariable Long productId, Model model) {
        model.addAttribute("product", productService.getActiveProduct(productId));
        return "product/detail";
    }

    private String buildTitle(String keyword, CategoryEntity selectedCategory, PriceRange selectedPriceRange) {
        if (keyword != null && !keyword.isBlank()) {
            return keyword.trim() + " 검색 결과";
        }
        if (selectedCategory != null) {
            return selectedCategory.getCategoryName();
        }
        if (selectedPriceRange != null) {
            return selectedPriceRange.getLabel() + " 상품";
        }
        return "전체 상품";
    }
}
