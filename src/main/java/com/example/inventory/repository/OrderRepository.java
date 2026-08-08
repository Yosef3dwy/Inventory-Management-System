package com.example.inventory.repository;

import com.example.inventory.model.Customer;
import com.example.inventory.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Order history page    
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);

    // Filter viewing (like "show all pending orders")
    List<Order> findByStatus(String status);
}