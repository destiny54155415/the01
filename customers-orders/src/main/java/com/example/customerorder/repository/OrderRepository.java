package com.example.customerorder.repository;

import com.example.customerorder.domain.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByOrderedAtDesc(Long customerId);

    @Query("""
            select o
            from Order o
            join fetch o.customer
            where o.id = :id
            """)
    Optional<Order> findWithCustomerById(@Param("id") Long id);

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    boolean existsByOrderNumberAndIdNot(String orderNumber, Long id);

    long countByCustomerId(Long customerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Order o where o.customer.id = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);

    @Query("""
            select o
            from Order o
            join fetch o.customer c
            where c.id = :customerId
            order by o.orderedAt desc, o.id desc
            """)
    List<Order> findOrdersOfCustomer(@Param("customerId") Long customerId);

    @Query("""
            select o
            from Order o
            join fetch o.customer c
            order by o.orderedAt desc, o.id desc
            """)
    List<Order> findAllWithCustomer();
}
