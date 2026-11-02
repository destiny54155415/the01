package com.example.customerorder.repository;

import com.example.customerorder.domain.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
            select c.id, c.name, c.email, c.phone, c.createdAt, count(o.id)
            from Customer c
            left join c.orders o
            group by c.id, c.name, c.email, c.phone, c.createdAt
            order by c.id desc
            """)
    List<Object[]> findSummaries();
}
