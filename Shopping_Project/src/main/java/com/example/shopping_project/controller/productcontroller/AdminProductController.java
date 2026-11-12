package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.dto.productdto.ProductDto;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.service.productservice.CategoryService;
import com.example.shopping_project.service.productservice.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/admin/products")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) PetType petType,
                       @RequestParam(defaultValue = "0") int page,
                       HttpSession session,
                       Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("result", productService.searchAdmin(keyword, categoryId, petType, page));
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("petType", petType);
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("adminCategories", categoryService.findAllCategories());
        return "product/admin-list";
    }

    @GetMapping("/admin/products/new")
    public String createForm(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ProductDto());
        }
        addFormOptions(model);
        return "product/admin-form";
    }

    @PostMapping("/admin/products")
    public String create(@Valid @ModelAttribute("form") ProductDto form,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "product/admin-form";
        }
        productService.create(form);
        redirectAttributes.addFlashAttribute("message", "상품을 등록했습니다.");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{productId}/edit")
    public String editForm(@PathVariable Long productId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", ProductDto.from(productService.getProduct(productId)));
        }
        addFormOptions(model);
        return "product/admin-form";
    }

    @PostMapping("/admin/products/{productId}")
    public String update(@PathVariable Long productId,
                         @Valid @ModelAttribute("form") ProductDto form,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        form.setProductId(productId);
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "product/admin-form";
        }
        productService.update(productId, form);
        redirectAttributes.addFlashAttribute("message", "상품을 수정했습니다.");
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{productId}/deactivate")
    public String deactivate(@PathVariable Long productId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        productService.deactivate(productId);
        redirectAttributes.addFlashAttribute("message", "상품을 비활성화했습니다.");
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{productId}/delete")
    public String delete(@PathVariable Long productId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            productService.delete(productId);
            redirectAttributes.addFlashAttribute("message", "상품을 삭제했습니다.");
        } catch (RuntimeException ex) {
            productService.deactivate(productId);
            redirectAttributes.addFlashAttribute("message",
                    "주문·장바구니에서 사용 중인 상품이라 삭제 대신 비활성화했습니다.");
        }
        return "redirect:/admin/products";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("adminCategories", categoryService.findAllCategories());
        model.addAttribute("petTypes", PetType.values());
    }

    /**
     * 회원 담당 세션 규약에 맞춤:
     * - 회원 로그인은 loginUser(MemberEntity) 를 넣음
     * - 추가로 loginRole / loginMember / adminDev 도 허용
     * MemberController 는 수정하지 않음.
     */
    private boolean isAdmin(HttpSession session) {
        if (isAdminRole(session.getAttribute("loginRole"))) {
            return true;
        }
        // 회원 모듈 기본 세션 키
        if (hasAdminRole(session.getAttribute("loginUser"))) {
            return true;
        }
        // 호환용
        if (hasAdminRole(session.getAttribute("loginMember"))) {
            return true;
        }
        return Boolean.TRUE.equals(session.getAttribute("adminDev"));
    }

    private static boolean hasAdminRole(Object member) {
        if (member == null) {
            return false;
        }
        try {
            Object memberRole = member.getClass().getMethod("getRole").invoke(member);
            return isAdminRole(memberRole);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
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
}
