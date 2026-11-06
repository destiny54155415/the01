package com.example.shopping_project.config.productconfig;

import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.PriceRange;
import com.example.shopping_project.entity.productentity.ProductSort;
import com.example.shopping_project.service.productservice.CategoryService;
import com.example.shopping_project.service.productservice.NotFoundException;
import jakarta.servlet.http.HttpSession;
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

    @ModelAttribute("loggedIn")
    public boolean loggedIn(HttpSession session) {
        return resolveLoginName(session) != null;
    }

    @ModelAttribute("loginMemberName")
    public String loginMemberName(HttpSession session) {
        return resolveLoginName(session);
    }

    private static String resolveLoginName(HttpSession session) {
        Object name = session.getAttribute("loginMemberName");
        if (name instanceof String text && !text.isBlank()) {
            return text;
        }
        Object member = session.getAttribute("loginMember");
        if (member instanceof MemberEntity entity) {
            return entity.getName();
        }
        Object user = session.getAttribute("loginUser");
        if (user instanceof MemberEntity entity) {
            return entity.getName();
        }
        if (user instanceof String text && !text.isBlank()) {
            return text;
        }
        return null;
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

    @ModelAttribute("sortOptions")
    public ProductSort[] sortOptions() {
        return ProductSort.values();
    }

    @ModelAttribute("selectedSort")
    public ProductSort selectedSort() {
        return ProductSort.POPULAR;
    }

    @ModelAttribute("sort")
    public String sort() {
        return ProductSort.POPULAR.name();
    }

    @ModelAttribute("inStockOnly")
    public Boolean inStockOnlyDefault() {
        return Boolean.FALSE;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "product/not-found";
    }
}
