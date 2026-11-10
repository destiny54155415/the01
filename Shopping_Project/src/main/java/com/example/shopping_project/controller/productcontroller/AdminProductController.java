package com.example.shopping_project.controller.productcontroller;

import com.example.shopping_project.dto.productdto.ProductDto;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.service.productservice.CategoryService;
import com.example.shopping_project.service.productservice.ProductImageStorageService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductImageStorageService productImageStorageService;

    public AdminProductController(ProductService productService,
                                  CategoryService categoryService,
                                  ProductImageStorageService productImageStorageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productImageStorageService = productImageStorageService;
    }

    @GetMapping("/admin/products")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) PetType petType,
                       @RequestParam(defaultValue = "0") int page,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(session, redirectAttributes);
        if (denied != null) {
            return denied;
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
    public String createForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(session, redirectAttributes);
        if (denied != null) {
            return denied;
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
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam(value = "imageFile2", required = false) MultipartFile imageFile2,
                         @RequestParam(value = "imageFile3", required = false) MultipartFile imageFile3,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "product/admin-form";
        }
        try {
            applyUploadedImages(form, imageFile, imageFile2, imageFile3);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("imageUpload", ex.getMessage());
            addFormOptions(model);
            return "product/admin-form";
        }
        productService.create(form);
        redirectAttributes.addFlashAttribute("message", "상품을 등록했습니다.");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{productId}/edit")
    public String editForm(@PathVariable Long productId,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(session, redirectAttributes);
        if (denied != null) {
            return denied;
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
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam(value = "imageFile2", required = false) MultipartFile imageFile2,
                         @RequestParam(value = "imageFile3", required = false) MultipartFile imageFile3,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        form.setProductId(productId);
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "product/admin-form";
        }
        try {
            applyUploadedImages(form, imageFile, imageFile2, imageFile3);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("imageUpload", ex.getMessage());
            addFormOptions(model);
            return "product/admin-form";
        }
        productService.update(productId, form);
        redirectAttributes.addFlashAttribute("message", "상품을 수정했습니다.");
        return "redirect:/admin/products";
    }

    private void applyUploadedImages(ProductDto form,
                                     MultipartFile imageFile,
                                     MultipartFile imageFile2,
                                     MultipartFile imageFile3) {
        String main = productImageStorageService.store(imageFile);
        if (main != null) {
            form.setImageUrl(main);
        }
        String second = productImageStorageService.store(imageFile2);
        if (second != null) {
            form.setImageUrl2(second);
        }
        String third = productImageStorageService.store(imageFile3);
        if (third != null) {
            form.setImageUrl3(third);
        }
    }

    @PostMapping("/admin/products/{productId}/deactivate")
    public String deactivate(@PathVariable Long productId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(session, redirectAttributes);
        if (denied != null) {
            return denied;
        }
        productService.deactivate(productId);
        redirectAttributes.addFlashAttribute("message", "상품을 비활성화했습니다.");
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{productId}/delete")
    public String delete(@PathVariable Long productId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(session, redirectAttributes);
        if (denied != null) {
            return denied;
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
     * 관리자만 통과. 미로그인 → /login, 일반회원 → / (권한 안내).
     * 회원 세션 규약: loginUser / loginMember / loginRole / adminDev
     * MemberController 는 수정하지 않음.
     */
    private String denyUnlessAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        if (isAdmin(session)) {
            return null;
        }
        if (isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "관리자 권한이 필요합니다. MySQL에서 role='ADMIN'으로 바꾼 뒤 로그아웃 후 다시 로그인하세요. (임시: /admin/dev-login)");
            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "관리자 상품 메뉴는 로그인 후 이용할 수 있습니다.");
        return "redirect:/login";
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loginUser") != null
                || session.getAttribute("loginMember") != null
                || session.getAttribute("loginMemberName") != null
                || session.getAttribute("loginUsername") != null
                || Boolean.TRUE.equals(session.getAttribute("adminDev"));
    }

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
