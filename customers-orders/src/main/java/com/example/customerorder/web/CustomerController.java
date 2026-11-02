package com.example.customerorder.web;

import com.example.customerorder.domain.Customer;
import com.example.customerorder.service.CustomerService;
import com.example.customerorder.service.DuplicateFieldException;
import com.example.customerorder.service.OrderService;
import com.example.customerorder.web.form.CustomerForm;
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
public class CustomerController {

    private final CustomerService customers;
    private final OrderService orders;

    public CustomerController(CustomerService customers, OrderService orders) {
        this.customers = customers;
        this.orders = orders;
    }

    @GetMapping("/customers")
    public String list(Model model) {
        model.addAttribute("customers", customers.findAll());
        return "customers/list";
    }

    @GetMapping("/customers/new")
    public String createForm(Model model) {
        model.addAttribute("customerForm", new CustomerForm());
        model.addAttribute("editing", false);
        return "customers/form";
    }

    @PostMapping("/customers")
    public String create(@Valid @ModelAttribute("customerForm") CustomerForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            return "customers/form";
        }
        try {
            Customer saved = customers.create(form);
            redirectAttributes.addFlashAttribute("message", "고객을 등록했습니다.");
            return "redirect:/customers/" + saved.getId();
        } catch (DuplicateFieldException exception) {
            bindingResult.rejectValue(exception.getField(), "duplicate", exception.getMessage());
            model.addAttribute("editing", false);
            return "customers/form";
        }
    }

    @GetMapping("/customers/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customers.get(id));
        model.addAttribute("orders", orders.findByCustomer(id));
        return "customers/detail";
    }

    @GetMapping("/customers/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Customer customer = customers.get(id);
        CustomerForm form = new CustomerForm();
        form.setName(customer.getName());
        form.setEmail(customer.getEmail());
        form.setPhone(customer.getPhone());
        model.addAttribute("customerForm", form);
        model.addAttribute("customerId", id);
        model.addAttribute("editing", true);
        return "customers/form";
    }

    @PostMapping("/customers/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("customerForm") CustomerForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customerId", id);
            model.addAttribute("editing", true);
            return "customers/form";
        }
        try {
            customers.update(id, form);
            redirectAttributes.addFlashAttribute("message", "고객 정보를 수정했습니다.");
            return "redirect:/customers/" + id;
        } catch (DuplicateFieldException exception) {
            bindingResult.rejectValue(exception.getField(), "duplicate", exception.getMessage());
            model.addAttribute("customerId", id);
            model.addAttribute("editing", true);
            return "customers/form";
        }
    }

    @PostMapping("/customers/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customers.delete(id);
        redirectAttributes.addFlashAttribute("message", "고객과 연결된 주문을 삭제했습니다.");
        return "redirect:/customers";
    }
}
