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
        productService.delete(productId);
        redirectAttributes.addFlashAttribute("message", "상품을 삭제했습니다.");
        return "redirect:/admin/products";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("adminCategories", categoryService.findAllCategories());
        model.addAttribute("petTypes", PetType.values());
    }

    /**
     * 회원 담당이 세션에 loginRole=ADMIN 또는 loginMember.role=ADMIN 을 넣으면 통과합니다.
     * 로컬 개발용으로 adminDev=true 세션도 허용합니다.
     */
    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("loginRole");
        if ("ADMIN".equals(role)) {
            return true;
        }
        Object member = session.getAttribute("loginMember");
        if (member != null) {
            try {
                Object memberRole = member.getClass().getMethod("getRole").invoke(member);
                if ("ADMIN".equals(String.valueOf(memberRole))) {
                    return true;
                }
            } catch (ReflectiveOperationException ignored) {
                // Member 연동 전
            }
        }
        return Boolean.TRUE.equals(session.getAttribute("adminDev"));
    }
}
