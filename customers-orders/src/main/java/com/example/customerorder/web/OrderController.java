package com.example.customerorder.web;

import com.example.customerorder.domain.Order;
import com.example.customerorder.domain.OrderStatus;
import com.example.customerorder.service.CustomerService;
import com.example.customerorder.service.DuplicateFieldException;
import com.example.customerorder.service.OrderService;
import com.example.customerorder.web.form.OrderForm;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

    private final OrderService orders;
    private final CustomerService customers;

    public OrderController(OrderService orders, CustomerService customers) {
        this.orders = orders;
        this.customers = customers;
    }

    @GetMapping("/orders")
    public String list(Model model) {
        model.addAttribute("orders", orders.findAll());
        return "orders/list";
    }

    @GetMapping("/orders/new")
    public String createForm(Model model) {
        OrderForm form = new OrderForm();
        form.setStatus(OrderStatus.PENDING);
        form.setOrderNumber(suggestOrderNumber());
        model.addAttribute("orderForm", form);
        prepareForm(model, false, null);
        return "orders/form";
    }

    @PostMapping("/orders")
    public String create(@Valid @ModelAttribute("orderForm") OrderForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, false, null);
            return "orders/form";
        }
        try {
            Order saved = orders.create(form);
            redirectAttributes.addFlashAttribute("message", "주문을 등록했습니다.");
            return "redirect:/customers/" + saved.getCustomer().getId();
        } catch (DuplicateFieldException exception) {
            bindingResult.rejectValue(exception.getField(), "duplicate", exception.getMessage());
            prepareForm(model, false, null);
            return "orders/form";
        }
    }

    @GetMapping("/orders/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Order order = orders.get(id);
        OrderForm form = new OrderForm();
        form.setCustomerId(order.getCustomer().getId());
        form.setOrderNumber(order.getOrderNumber());
        form.setTotalPrice(order.getTotalPrice());
        form.setStatus(order.getStatus());
        model.addAttribute("orderForm", form);
        prepareForm(model, true, id);
        return "orders/form";
    }

    @PostMapping("/orders/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("orderForm") OrderForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, true, id);
            return "orders/form";
        }
        try {
            orders.update(id, form);
            redirectAttributes.addFlashAttribute("message", "주문을 수정했습니다.");
            return "redirect:/orders";
        } catch (DuplicateFieldException exception) {
            bindingResult.rejectValue(exception.getField(), "duplicate", exception.getMessage());
            prepareForm(model, true, id);
            return "orders/form";
        }
    }

    @PostMapping("/orders/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        orders.delete(id);
        redirectAttributes.addFlashAttribute("message", "주문을 삭제했습니다.");
        return "redirect:/orders";
    }

    private void prepareForm(Model model, boolean editing, Long orderId) {
        model.addAttribute("customers", customers.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("editing", editing);
        model.addAttribute("orderId", orderId);
    }

    private String suggestOrderNumber() {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return today + "-" + String.format("%04d", orders.count() + 1);
    }
}
