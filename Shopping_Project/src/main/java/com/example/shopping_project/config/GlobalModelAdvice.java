package com.example.shopping_project.config;

import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.PriceRange;
import com.example.shopping_project.service.productservice.CategoryService;
import com.example.shopping_project.service.productservice.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CategoryService categoryService;

    public GlobalModelAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute("categories")
    public Object categories() {
        return categoryService.findActiveCategories();
    }

    @ModelAttribute("petTypes")
    public PetType[] petTypes() {
        return PetType.values();
    }

    @ModelAttribute("priceRanges")
    public PriceRange[] priceRanges() {
        return PriceRange.values();
    }

    @ModelAttribute("categoryId")
    public Long categoryId() {
        return null;
    }

    @ModelAttribute("keyword")
    public String keyword() {
        return "";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "product/not-found";
    }
}
