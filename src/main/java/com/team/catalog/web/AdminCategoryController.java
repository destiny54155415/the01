package com.team.catalog.web;

import com.team.catalog.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/admin/categories")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.tree());
        return "admin/categories";
    }

    @GetMapping("/admin/categories/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CategoryForm());
        }
        model.addAttribute("parents", categoryService.tree());
        return "admin/category-form";
    }

    @PostMapping("/admin/categories")
    public String create(@Valid @ModelAttribute("form") CategoryForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("parents", categoryService.tree());
            return "admin/category-form";
        }
        try {
            categoryService.save(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("category", ex.getMessage());
            model.addAttribute("parents", categoryService.tree());
            return "admin/category-form";
        }
        redirectAttributes.addFlashAttribute("message", "카테고리를 저장했습니다.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", CategoryForm.from(categoryService.get(id)));
        }
        model.addAttribute("parents", categoryService.tree());
        return "admin/category-form";
    }

    @PostMapping("/admin/categories/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") CategoryForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("parents", categoryService.tree());
            return "admin/category-form";
        }
        try {
            categoryService.save(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("category", ex.getMessage());
            model.addAttribute("parents", categoryService.tree());
            return "admin/category-form";
        }
        redirectAttributes.addFlashAttribute("message", "카테고리를 수정했습니다.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("message", "카테고리를 삭제했습니다.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
