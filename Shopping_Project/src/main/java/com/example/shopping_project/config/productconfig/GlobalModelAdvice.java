package com.example.shopping_project.config.productconfig;

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

    /** 헤더 상품관리/상품등록 노출용 */
    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        if (Boolean.TRUE.equals(session.getAttribute("adminDev"))) {
            return true;
        }
        if (isAdminRole(session.getAttribute("loginRole"))) {
            return true;
        }
        return hasAdminRole(session.getAttribute("loginUser"))
                || hasAdminRole(session.getAttribute("loginMember"));
    }

    private static boolean hasAdminRole(Object member) {
        if (member == null) {
            return false;
        }
        try {
            Object role = member.getClass().getMethod("getRole").invoke(member);
            return isAdminRole(role);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static String resolveLoginName(HttpSession session) {
        Object name = session.getAttribute("loginMemberName");
        if (name instanceof String text && !text.isBlank()) {
            return text;
        }

        String fromMember = readNameProperty(session.getAttribute("loginMember"));
        if (fromMember != null) {
            return fromMember;
        }

        Object user = session.getAttribute("loginUser");
        String fromUser = readNameProperty(user);
        if (fromUser != null) {
            return fromUser;
        }
        if (user instanceof String text && !text.isBlank()) {
            return text;
        }

        // /admin/dev-login 처럼 관리자 세션만 있는 경우
        if (Boolean.TRUE.equals(session.getAttribute("adminDev")) || isAdminRole(session.getAttribute("loginRole"))) {
            Object username = session.getAttribute("loginUsername");
            if (username instanceof String text && !text.isBlank()) {
                return text;
            }
            return "관리자";
        }
        return null;
    }

    private static String readNameProperty(Object target) {
        if (target == null) {
            return null;
        }
        try {
            Object value = target.getClass().getMethod("getName").invoke(target);
            if (value instanceof String text && !text.isBlank()) {
                return text;
            }
        } catch (ReflectiveOperationException ignored) {
            // 회원 엔티티가 아직 없거나 getName 이 없는 경우
        }
        return null;
    }

    private static boolean isAdminRole(Object role) {
        if (role == null) {
            return false;
        }
        String normalized = String.valueOf(role).trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return "ADMIN".equals(normalized);
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
