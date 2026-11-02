package com.example.customerorder.web;

import com.example.customerorder.service.CustomerService;
import com.example.customerorder.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CustomerService customers;
    private final OrderService orders;

    public HomeController(CustomerService customers, OrderService orders) {
        this.customers = customers;
        this.orders = orders;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("customerCount", customers.count());
        model.addAttribute("orderCount", orders.count());
        return "index";
    }
}
