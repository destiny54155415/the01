package com.team.catalog.web;

import com.team.catalog.domain.Category;
import com.team.catalog.domain.ProductSort;
import com.team.catalog.service.CategoryService;
import com.team.catalog.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShopController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ShopController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String q,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(defaultValue = "NEWEST") String sort,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        ProductSort productSort = ProductSort.from(sort);
        Page<?> result = productService.search(q, categoryId, productSort, page, false);
        Category selected = categoryId == null ? null : categoryService.get(categoryId);
        model.addAttribute("result", result);
        model.addAttribute("categories", categoryService.tree());
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selected", selected);
        model.addAttribute("parentId", parentId(selected));
        model.addAttribute("sort", productSort);
        model.addAttribute("sorts", ProductSort.values());
        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.view(id));
        model.addAttribute("categories", categoryService.tree());
        return "products/detail";
    }

    private Long parentId(Category selected) {
        if (selected == null) {
            return null;
        }
        return selected.getParent() == null ? selected.getId() : selected.getParent().getId();
    }
}
