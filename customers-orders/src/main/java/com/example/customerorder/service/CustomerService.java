package com.example.customerorder.service;

import com.example.customerorder.domain.Customer;
import com.example.customerorder.repository.CustomerRepository;
import com.example.customerorder.repository.OrderRepository;
import com.example.customerorder.web.form.CustomerForm;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customers;
    private final OrderRepository orders;

    public CustomerService(CustomerRepository customers, OrderRepository orders) {
        this.customers = customers;
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public List<CustomerSummary> findAll() {
        List<CustomerSummary> summaries = new ArrayList<>();
        for (Object[] row : customers.findSummaries()) {
            summaries.add(new CustomerSummary(
                    (Long) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (LocalDateTime) row[4],
                    ((Number) row[5]).longValue()
            ));
        }
        return summaries;
    }

    @Transactional(readOnly = true)
    public Customer get(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "고객을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public long count() {
        return customers.count();
    }

    public Customer create(CustomerForm form) {
        String email = form.getEmail().trim();
        if (customers.existsByEmail(email)) {
            throw new DuplicateFieldException("email", "이미 등록된 이메일입니다.");
        }
        Customer customer = new Customer();
        apply(customer, form, email);
        return customers.save(customer);
    }

    public void update(Long id, CustomerForm form) {
        Customer customer = get(id);
        String email = form.getEmail().trim();
        if (customers.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateFieldException("email", "이미 등록된 이메일입니다.");
        }
        apply(customer, form, email);
    }

    public void delete(Long id) {
        if (!customers.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "고객을 찾을 수 없습니다.");
        }
        orders.deleteByCustomerId(id);
        customers.deleteById(id);
    }

    private void apply(Customer customer, CustomerForm form, String email) {
        customer.setName(form.getName().trim());
        customer.setEmail(email);
        String phone = form.getPhone() == null ? "" : form.getPhone().trim();
        customer.setPhone(phone.isEmpty() ? null : phone);
    }
}
