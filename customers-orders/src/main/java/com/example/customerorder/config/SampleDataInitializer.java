package com.example.customerorder.config;

import com.example.customerorder.domain.Customer;
import com.example.customerorder.domain.Order;
import com.example.customerorder.domain.OrderStatus;
import com.example.customerorder.repository.CustomerRepository;
import com.example.customerorder.repository.OrderRepository;
import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SampleDataInitializer implements ApplicationRunner {

    private final CustomerRepository customers;
    private final OrderRepository orders;

    public SampleDataInitializer(CustomerRepository customers, OrderRepository orders) {
        this.customers = customers;
        this.orders = orders;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (customers.count() > 0) {
            return;
        }
        Customer customer = new Customer();
        customer.setName("홍길동");
        customer.setEmail("hong@test.com");
        customer.setPhone("010-1234-5678");
        customers.save(customer);

        saveOrder(customer, "20261011-0001", "25000", OrderStatus.PAID);
        saveOrder(customer, "20261011-0002", "18000", OrderStatus.PENDING);
    }

    private void saveOrder(Customer customer, String orderNumber, String price, OrderStatus status) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderNumber(orderNumber);
        order.setTotalPrice(new BigDecimal(price));
        order.setStatus(status);
        orders.save(order);
    }
}
