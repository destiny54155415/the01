package com.team.catalog.web;

import com.team.catalog.domain.ProductSort;
import com.team.catalog.domain.ProductStatus;
import com.team.catalog.service.CategoryService;
import com.team.catalog.service.ProductService;
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
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("result", productService.search(q, categoryId, ProductSort.NEWEST, page, true));
        model.addAttribute("categories", categoryService.tree());
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("categoryId", categoryId);
        return "admin/products";
    }

    @GetMapping("/admin/products/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ProductForm());
        }
        addFormOptions(model);
        return "admin/product-form";
    }

    @PostMapping("/admin/products")
    public String create(@Valid @ModelAttribute("form") ProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        rejectSalePrice(form, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "admin/product-form";
        }
        try {
            productService.save(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("product", ex.getMessage());
            addFormOptions(model);
            return "admin/product-form";
        }
        redirectAttributes.addFlashAttribute("message", "상품을 저장했습니다.");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", ProductForm.from(productService.get(id)));
        }
        addFormOptions(model);
        return "admin/product-form";
    }

    @PostMapping("/admin/products/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") ProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        form.setId(id);
        rejectSalePrice(form, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "admin/product-form";
        }
        try {
            productService.save(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("product", ex.getMessage());
            addFormOptions(model);
            return "admin/product-form";
        }
        redirectAttributes.addFlashAttribute("message", "상품을 수정했습니다.");
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("message", "상품을 삭제했습니다.");
        return "redirect:/admin/products";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("categories", categoryService.tree());
        model.addAttribute("statuses", ProductStatus.values());
    }

    private void rejectSalePrice(ProductForm form, BindingResult bindingResult) {
        if (form.getPrice() != null && form.getSalePrice() != null && form.getSalePrice() >= form.getPrice()) {
            bindingResult.rejectValue("salePrice", "salePrice", "할인가는 정가보다 낮아야 합니다.");
        }
    }
}
