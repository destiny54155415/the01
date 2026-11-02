package com.example.customerorder.service;

import com.example.customerorder.domain.Customer;
import com.example.customerorder.domain.Order;
import com.example.customerorder.repository.CustomerRepository;
import com.example.customerorder.repository.OrderRepository;
import com.example.customerorder.web.form.OrderForm;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orders;
    private final CustomerRepository customers;

    public OrderService(OrderRepository orders, CustomerRepository customers) {
        this.orders = orders;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orders.findAllWithCustomer();
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomer(Long customerId) {
        return orders.findOrdersOfCustomer(customerId);
    }

    @Transactional(readOnly = true)
    public Order get(Long id) {
        return orders.findWithCustomerById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public long count() {
        return orders.count();
    }

    public Order create(OrderForm form) {
        String orderNumber = form.getOrderNumber().trim();
        if (orders.existsByOrderNumber(orderNumber)) {
            throw new DuplicateFieldException("orderNumber", "이미 등록된 주문번호입니다.");
        }
        Order order = new Order();
        apply(order, form, orderNumber);
        return orders.save(order);
    }

    public void update(Long id, OrderForm form) {
        Order order = get(id);
        String orderNumber = form.getOrderNumber().trim();
        if (orders.existsByOrderNumberAndIdNot(orderNumber, id)) {
            throw new DuplicateFieldException("orderNumber", "이미 등록된 주문번호입니다.");
        }
        apply(order, form, orderNumber);
    }

    public void delete(Long id) {
        if (!orders.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다.");
        }
        orders.deleteById(id);
    }

    private void apply(Order order, OrderForm form, String orderNumber) {
        Customer customer = customers.findById(form.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 고객이 없습니다."));
        order.setCustomer(customer);
        order.setOrderNumber(orderNumber);
        order.setTotalPrice(form.getTotalPrice().setScale(2, RoundingMode.HALF_UP));
        order.setStatus(form.getStatus());
    }
}
