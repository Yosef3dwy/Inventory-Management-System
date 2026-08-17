package com.example.inventory.repository;

import com.example.inventory.model.Customer;
import com.example.inventory.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Order history page
    @Query("SELECT o FROM Order o WHERE o.customer = :customer ORDER BY o.OrderDate DESC")
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);

    // Filter viewing (like "show all pending orders")
    @Query("SELECT o FROM Order o WHERE o.Status = :status")
    List<Order> findByStatus(String status);
}
